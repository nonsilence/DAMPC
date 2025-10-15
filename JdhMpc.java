package iwhr.swmm.mpcModel;

import iwhr.swmm.element.SWMM;
import iwhr.swmm.optModel.OptMethod;
import iwhr.swmm.swmmtools.CreatInpFile;
import iwhr.swmm.swmmtools.ReadAllResult;
import iwhr.swmm.swmmtools.RewriteInpFile;
import iwhr.swmm.swmmtools.UpdateInpFile;
import iwhr.swmm.util.CurveData;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author fww
 * DAMPC optimization for Jingdian-Qinting Lake flood flow - real-time gate/pump control values
 */
public class JdhMpc {
    protected static Logger logger = LogManager.getLogger(JdhMpc.class);

    public static void main(String[] args) throws Exception {

        // Get SWMM model file path
        File swmmFile = new File(framework_directory+"/swmm.inp");
        List<String> timeList = readDateList(swmmFile);
        CurveData curveData = new CurveData();

		//Model Predictive Control Parameters
        int[] parameters = new int[]{120, 10, 10, 5};
        int prediction_step = parameters[0] / parameters[1];

        for (int i = 0; i < timeList.size() - prediction_step; i++) {
            // Prediction
            UpdateInpFile.rewriteTime(swmmFile, timeList, i, prediction_step);
            SWMM.initialize(swmmFile);
            SWMM.simulate(swmmFile);

            ArrayList<double[]> fatalist = readSWMMResult(swmmFile);

            long t0 = System.currentTimeMillis();
            // Multi-objective optimization
            double[] output = OptMethod.MOEA(curveData, parameters, fatalist);
            // Single-objective optimization
//            int priority = calculatePriority(swmmFile);
//            double[] output = OptMethod.jdhGA(curveData, parameters, fatalist, priority);

            double[] result = new double[output.length];
            for (int j = 0; j < output.length; j++) {
                BigDecimal bd = BigDecimal.valueOf(output[j] / 4.5);
                result[j] = bd.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
            }
            RewriteInpFile.rewriteMpcInp(swmmFile, result, i, parameters[3]);
            System.out.println("MPC calculation for time period " + i + " completed successfully!");
            long t1 = System.currentTimeMillis();

            RewriteInpFile.rewriteMpcInp(swmmFile, result, i, 5);
            CreatInpFile.createInpFile(swmmFile);

            logger.error("Single optimization algorithm computation time (ms): " + (t1 - t0));
        }
    }

    private static int calculatePriority(File swmmFile) throws IOException {

        double[] jDHLevel = ReadAllResult.readOutFile(swmmFile, "JDH", "Head");
        double[] Lake_QTH_Level = ReadAllResult.readOutFile(swmmFile, "Lake_QTH", "Head");
        double[] JFX_1480_Flow = ReadAllResult.readOutFile(swmmFile, "JFX_1480", "TotalInflow");
        double[] YTX_x3_Flow = ReadAllResult.readOutFile(swmmFile, "YTX_x3", "TotalInflow");
        double[] Lake_QTH_Flow = ReadAllResult.readOutFile(swmmFile, "Lake_QTH", "TotalInflow");

        double[] JDH_inti_volume = ReadAllResult.readOutFile(swmmFile, "JDH", "Volume");
        double[] QTH_inti_volume = ReadAllResult.readOutFile(swmmFile, "Lake_QTH", "Volume");

        double JFX_Flood = Arrays.stream(JFX_1480_Flow).sum() * 5 * 60;
        double YTX_Flood = Arrays.stream(YTX_x3_Flow).sum() * 5 * 60;
        double QTH_Flood = Arrays.stream(Lake_QTH_Flow).sum() * 5 * 60;

        // Get reservoir water level-storage curve; handle negative values
        double jdhVolume = 96852 - JDH_inti_volume[0];
        double qthVolume = 569015.25 - QTH_inti_volume[0];

        if (jdhVolume < 0) {
            jdhVolume = 0.00001;
        }
        if (qthVolume < 0) {
            qthVolume = 0.00001;
        }

        // Tributary i's share of current available storage capacity
        double p11 = JFX_Flood / jdhVolume;
        double p12 = YTX_Flood / jdhVolume;
        double p13 = QTH_Flood / qthVolume;

        // Lead time for flood to reach reservoir
        double p21 = 0;
        double p22 = 0;
        double p23 = 0;
        if (Arrays.stream(jDHLevel).max().getAsDouble() > 12.5) {
            p21 = calculateTime(JFX_1480_Flow) / calculateTime(jDHLevel); // seconds
            p22 = calculateTime(YTX_x3_Flow) / calculateTime(jDHLevel); // seconds
        }
        if (Arrays.stream(Lake_QTH_Level).max().getAsDouble() > 5.5) {
            p23 = calculateTime(Lake_QTH_Flow) / calculateTime(Lake_QTH_Level); // seconds
        }

        // Risk level of downstream area for tributary i
        double p31 = 0;
        double p32 = 0;
        double p33 = 0;
        double temp1 = calcuteResilience(Lake_QTH_Level, 5.5);
        double temp2 = calcuteResilience(jDHLevel, 12.5);
        if (temp1 > temp2) {
            p31 = temp2 / temp1;
            p32 = temp2 / temp1;
            p33 = 1.0;
        } else if (temp1 < temp2) {
            p31 = 1.0;
            p32 = 1.0;
            p33 = temp1 / temp2;
        }

        // Jiefang Stream 1
        double p1 = 0.135 * p11 + 0.146 * p21 + 0.719 * p31;
        // Yangting Stream 2
        double p2 = 0.135 * p12 + 0.146 * p22 + 0.719 * p32;
        // Qinting Lake 3
        double p3 = 0.135 * p13 + 0.146 * p23 + 0.719 * p33;

        int optPriority = 1;
        if (p2 > p1 && p2 > p3) {
            optPriority = 2;
        } else if (p3 > p1) {
            optPriority = 3;
        }
        return optPriority;
    }

    private static ArrayList<double[]> readSWMMResult(File swmmFile) throws IOException {

        ArrayList<double[]> list = new ArrayList<>();
        list.add(ReadAllResult.readOutFile(swmmFile, "JDH", "Head"));
        list.add(ReadAllResult.readOutFile(swmmFile, "JFX_1480", "Head"));
        list.add(ReadAllResult.readOutFile(swmmFile, "YTX_x3", "Head"));
        list.add(ReadAllResult.readOutFile(swmmFile, "YTX_x2", "Head"));
        list.add(ReadAllResult.readOutFile(swmmFile, "Lake_QTH", "Head"));
        list.add(ReadAllResult.readOutFile(swmmFile, "t", "Head"));

        list.add(ReadAllResult.readOutFile(swmmFile, "JFX_1480", "TotalInflow"));
        list.add(ReadAllResult.readOutFile(swmmFile, "YTX_x3", "TotalInflow"));
        list.add(ReadAllResult.readOutFile(swmmFile, "JFX_1870", "TotalInflow"));
        list.add(ReadAllResult.readOutFile(swmmFile, "Lake_QTH", "TotalInflow"));

        // Gate flow rates
        list.add(ReadAllResult.readOutFile(swmmFile, "JDHGATE1", "Flow"));
        list.add(ReadAllResult.readOutFile(swmmFile, "JDHGATE2", "Flow"));
        list.add(ReadAllResult.readOutFile(swmmFile, "JDHGATE4", "Flow"));
        list.add(ReadAllResult.readOutFile(swmmFile, "W4", "Flow"));
        list.add(ReadAllResult.readOutFile(swmmFile, "P1", "Flow"));

        System.out.println("Calculation results read successfully!");

        return list;
    }

    private static List<String> readDateList(File swmmFile) throws IOException {

        InputStreamReader isr = new InputStreamReader(new FileInputStream(swmmFile + "/data/timeList.txt"), "utf-8");
        BufferedReader br = new BufferedReader(isr);
        String lineTxt = null;
        List<String> dateList = new ArrayList<String>();
        while ((lineTxt = br.readLine()) != null) {
            dateList.add(lineTxt);
        }
        br.close();
        return dateList;
    }

    private static double calcuteResilience(double[] jDHLevel, double r) {
        double R = 0;
        if (r == 12.5) {
            if (Arrays.stream(jDHLevel).max().getAsDouble() > r) {
                R = 223.24;
            }
        } else if (r == 5.5) {
            if (Arrays.stream(jDHLevel).max().getAsDouble() > r) {
                R = 2842.77;
            }
        }
        return R;
    }

    private static double calculateTime(double[] JFX_1480_Flow) {

        double max = Arrays.stream(JFX_1480_Flow).max().getAsDouble();
        int index = 0;
        for (int i = 0; i < JFX_1480_Flow.length; i++) {
            if (max == JFX_1480_Flow[i]) {
                index = i;
            }
        }
        if (index == 0) {
            return 0.01;
        } else {
            return index * 5;
        }
    }
}