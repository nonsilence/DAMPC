package iwhr.swmm.optModel;

import io.jenetics.*;
import io.jenetics.engine.*;
import io.jenetics.ext.SimulatedBinaryCrossover;
import io.jenetics.ext.moea.MOEA;
import io.jenetics.ext.moea.NSGA2Selector;
import io.jenetics.ext.moea.Vec;
import io.jenetics.ext.moea.VecFactory;
import io.jenetics.util.DoubleRange;
import io.jenetics.util.Factory;
import io.jenetics.util.ISeq;
import io.jenetics.util.IntRange;
import iwhr.swmm.mpcModel.Mpc;
import iwhr.swmm.util.CurveData;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import static io.jenetics.engine.EvolutionResult.toBestPhenotype;
import static io.jenetics.engine.Limits.bySteadyFitness;
/**
 * @author fww
 * Jenetics-based optimization for gate and pump control setpoints at subsequent time steps.
 */
public class OptMethod {
	protected static Logger logger = LogManager.getLogger(Mpc.class);

	public static double[] MOEA(CurveData curveData, int[] parameters, ArrayList<double[]> dataList) throws IOException {

		final int VARIABLES = parameters[3] * parameters[0] / parameters[1];
		final int OBJECTIVES = 3;
		InvertibleCodec<double[], DoubleGene> codec = Codecs.ofVector(DoubleRange.of(0.0, 4.5), VARIABLES);

		final VecFactory<double[]> factory = VecFactory.ofDoubleVec(
				Optimize.MINIMUM,
				Optimize.MINIMUM,
				Optimize.MINIMUM
		);

		final Problem<double[], DoubleGene, Vec<double[]>> PROBLEM = Problem.of(
				matrix -> {
					double[] ss = new double[OBJECTIVES];
					try {
						ss = jdhSimModel.jdhFitness(matrix, curveData, parameters, dataList);
					} catch (Exception e) {
						e.printStackTrace();
					}
					return factory.newVec(new double[]{ss[0], ss[1], ss[2]});//添加了聚合权重
				},
				codec
		);

//        final Constraint<DoubleGene, double[]> constraint = Constraint.of(codec,true);

		final Engine<DoubleGene, Vec<double[]>> engine = Engine
				.builder(PROBLEM)
				.populationSize(1000)
				.alterers(
						new Mutator<>(0.3),
						new SinglePointCrossover<>(0.2),
						new SimulatedBinaryCrossover<>(1),
						new Mutator<>(1.0 / 60)
				)
				.offspringSelector(new TournamentSelector<>(60))
				.survivorsSelector(NSGA2Selector.ofVec())
				.minimizing()
				.build();
		final ISeq<Vec<double[]>> front = engine
				.stream()
				.limit(Limits.bySteadyFitness(200))
				.limit(20000)
				.collect(MOEA.toParetoSet(IntRange.of(1, 20000)))
				.map(Phenotype::fitness);

		Factory<Genotype<DoubleGene>> x = codec.encoding();
		String[] s = x.toString().substring(2, x.toString().length() - 2).split(",");
		double[] matrix = new double[s.length];
		for (int i = 0; i < matrix.length; i++) {
			matrix[i] = Double.parseDouble(s[i].substring(1, 6));
		}
		return matrix;

	}

	public static double[] jdhGA(CurveData curveData, int[] parameters, ArrayList<double[]> dataList, int priority) throws IOException {

		final int VARIABLES = parameters[3] * parameters[0] / parameters[1];
		Function<double[], Double> fitness = matrix -> {
			try {
				double s = 0;
//                double s = SimulatedModel.calculateFitness(matrix, curveData, parameters, dataList)[0];
				double[] ss = jdhSimModel.jdhFitness(matrix, curveData, parameters, dataList);
				if (priority == 1) {
					s = ss[0];
				} else if (priority == 2) {
					s = ss[1];
				} else {
					s = ss[2];
				}
				//                logger.warn(s);
				return s;
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		};

		InvertibleCodec<double[], DoubleGene> codec = Codecs.ofVector(DoubleRange.of(0.1, 4.5), VARIABLES);

		final Engine<DoubleGene, Double> engine = Engine
				.builder(
						fitness,
						codec
				).populationSize(1000)
				.optimize(Optimize.MINIMUM)
				.alterers(
						new Mutator<>(0.3),
						new SinglePointCrossover<>(0.2)
				).build();

		final EvolutionStatistics<Double, ?> statistics = EvolutionStatistics.ofNumber();

		List<String> fitList = new ArrayList<>();

		final Phenotype<DoubleGene, Double> best = engine.stream()
				.limit(bySteadyFitness(200))
				.limit(20000)
				.peek(statistics)
				.peek(er -> {
					final int generation = (int) er.generation();
					final String fit = er.population().stream()
							.map(Phenotype::fitness)
							.map(Objects::toString)
							.collect(Collectors.joining(","));
					fitList.add(fit);
				})
				.collect(EvolutionResult.toBestPhenotype());

		double[] gateOpen = codec.decode(best.genotype());
		return gateOpen;

	}
}
