import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author fww
 * Calculate UFHWS model indicator weights
 */

public class EntropyWeight {
    
    public static double[] entropyWeight(double[][] data) {
        int n = data.length; // Number of samples
        int m = data[0].length; // Number of indicators
        
        // Step 1: Data standardization - divide each column by its sum
        double[][] normalizedData = new double[n][m];
        double[] colSums = new double[m];
        
        // Calculate sum of each column
        for (int j = 0; j < m; j++) {
            colSums[j] = 0;
            for (int i = 0; i < n; i++) {
                colSums[j] += data[i][j];
            }
        }
        
        // Standardize data
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                normalizedData[i][j] = data[i][j] / colSums[j];
            }
        }
        
        // Step 2: Calculate entropy
        double[] entropy = new double[m];
        for (int j = 0; j < m; j++) {
            double sum = 0;
            for (int i = 0; i < n; i++) {
                if (normalizedData[i][j] > 0) { // Avoid log(0)
                    sum += normalizedData[i][j] * Math.log(normalizedData[i][j]);
                }
            }
            entropy[j] = -sum / Math.log(n);
        }
        
        // Step 3: Calculate difference coefficient
        double[] diffCoeff = new double[m];
        double diffSum = 0;
        for (int j = 0; j < m; j++) {
            diffCoeff[j] = 1 - entropy[j];
            diffSum += diffCoeff[j];
        }
        
        // Step 4: Calculate weights
        double[] weights = new double[m];
        for (int j = 0; j < m; j++) {
            weights[j] = diffCoeff[j] / diffSum;
        }
        
        return weights;
    }
    
    public static void main(String[] args) {
        try {
            // Read data file
            BufferedReader reader = new BufferedReader(new FileReader("data.txt"));
            List<double[]> dataList = new ArrayList<>();
            String line;
            
            while ((line = reader.readLine()) != null) {
                String[] values = line.trim().split("\\s+");
                double[] row = new double[values.length];
                for (int i = 0; i < values.length; i++) {
                    row[i] = Double.parseDouble(values[i]);
                }
                dataList.add(row);
            }
            reader.close();
            
            // Convert to 2D array
            double[][] data = new double[dataList.size()][];
            for (int i = 0; i < dataList.size(); i++) {
                data[i] = dataList.get(i);
            }
            
            // Calculate weights
            double[] weights = entropyWeight(data);
            
            // Output results
            System.out.print("Weights: [");
            for (int i = 0; i < weights.length; i++) {
                System.out.printf("%.6f", weights[i]);
                if (i < weights.length - 1) {
                    System.out.print(", ");
                }
            }
            System.out.println("]");
            
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.err.println("Data format error: " + e.getMessage());
        }
    }
}