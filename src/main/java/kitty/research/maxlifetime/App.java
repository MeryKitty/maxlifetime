package kitty.research.maxlifetime;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

import org.nd4j.linalg.api.buffer.DataType;
import org.nd4j.linalg.factory.Nd4j;

import kitty.research.maxlifetime.algorithm.Graph;
import kitty.research.maxlifetime.algorithm.SimplexSolverDouble;
import kitty.research.maxlifetime.model.SensorNetwork;

/**
 * Entry point of the algorithm
 *
 */
public class App {
//	private static final String inputFile = "./data/input/sensorNumber/";
//	private static final String outputFile = "./data/output/sensorNumber/";
	// Number of field to conduct the algorithm on for each set of parameters
	//private static final int fields = 25;
	
	public static void main(String[] args) throws IOException {
//		args = new String[] {"sensorNumber", "100", "0", "25"};
		System.out.println(Arrays.toString(args));
		Nd4j.setDefaultDataTypes(DataType.DOUBLE, DataType.DOUBLE);
		String location = args[0];
		String inputFile = "./data/input/" + location + "/";
		String outputFile = "./data/output/" + location + "/";
//		for (int sensorNumber = 175; sensorNumber < 200; sensorNumber += 25) {
		int value = Integer.parseInt(args[1]);
//			PrintStream output = new PrintStream(outputFile + sensorNumber + ".txt");
//			output.println("EXPERIMENT OUTPUT:\n\nSensor Number: " + sensorNumber + "\n");
//			double[] results = new double[fields];
//			long overallStart = System.currentTimeMillis();
			int startField = Integer.parseInt(args[2]);
			int step = Integer.parseInt(args[3]);
			for (int field = startField; field < startField + step; field++) {
				System.out.println("\nSensor Number: " + value + " - Field: " + field);
				long start = System.currentTimeMillis();
				
				// Initialise the network
				var network = SensorNetwork.readData(inputFile + value + "_" + field + ".txt");
				network.initialiseSectors(Math.PI / 2);
				network.updateSectors();
				long mid1 = System.currentTimeMillis();
				System.out.println("    Step 1:\n        Time: " + (mid1 - start));
				
				// Build the graph
				var graph = new Graph(network);
				network = null;
				long mid2 = System.currentTimeMillis();
				System.out.println("    Step 2:\n        Time: " + (mid2 - mid1) + "\n        Vertex Number: " + graph.vertexNumber() + "\n        Edge Number: " + graph.edgeMap().size());
				System.out.println("    Step 3:");
				
				// Build the constraint and target matrices
				int rowNumber = graph.vertexNumber() + graph.clusterList().size();
				int columnNumber = graph.edgeMap().size() + graph.clusterList().size();
				var A = Nd4j.zeros(rowNumber, columnNumber);
				var b = Nd4j.zeros(rowNumber, 1);
				var c = Nd4j.zeros(columnNumber);
				graph.initialiseTensors(A, b, c);
				c.muli(-1);
				long mid3 = System.currentTimeMillis();
				System.out.println("        Configuration Time: " + (mid3 - mid2));
				var spl = new SimplexSolverDouble();
				var resultVertex = spl.execute(A, b, c);
				A.close(); b.close(); c.close();
				double realResult = -resultVertex.first();
				System.out.println("        Real Result: " + realResult);
				double[] vertex = resultVertex.second();
				int result = graph.analyseIntFlow(vertex);
				long end = System.currentTimeMillis();
				System.out.println("        Time: " + (end - mid2) + "\n        Result: " + result);
				Files.write(Paths.get(outputFile + value + ".txt"), (field + ": " + result + " " + (end - start) + "\n").getBytes(), StandardOpenOption.APPEND);
//				results[field] = result;
			}
//			long overallEnd = System.currentTimeMillis();
//			double finalResult = 0;
//			for (double d : results) {
//				finalResult += d;
//			}
//			finalResult /= fields;
//			output.println("Average Flow: " + finalResult);
//			output.println("Average Time: " + ((overallEnd - overallStart) / fields + "\n"));
//			for (double d : results) {
//				output.println(d);
//			}
//			output.close();
//		}
	}
}
