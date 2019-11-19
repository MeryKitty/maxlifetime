package kitty.research.maxlifetime;

import java.io.IOException;
import java.io.PrintStream;

import org.nd4j.linalg.api.buffer.DataType;
import org.nd4j.linalg.factory.Nd4j;

import kitty.research.maxlifetime.algorithm.Graph;
import kitty.research.maxlifetime.algorithm.SimplexSolverDouble;
import kitty.research.maxlifetime.model.SensorNetwork;

/**
 * Entry point of the algorithm
 *
 */
public class App 
{
	private static final String inputFile = "./data/input/S1/";
	private static final String outputFile = "./data/output/S1/";
	// Number of field to conduct the algorithm on for each set of parameters
	private static final int fields = 100;
	
	public static void main(String[] args) throws IOException {
		Nd4j.setDefaultDataTypes(DataType.DOUBLE, DataType.DOUBLE);
		for (int sensorNumber = 100; sensorNumber <= 450; sensorNumber += 50) {
			PrintStream output = new PrintStream(outputFile + sensorNumber + ".txt");
			output.println("EXPERIMENT OUTPUT:\n\nSensor Number: " + sensorNumber + "\n");
			double[] results = new double[fields];
			long overallStart = System.currentTimeMillis();
			for (int field = 0; field < fields; field++) {
				System.out.println("\nSensor Number: " + sensorNumber + " - Field: " + field);
				long start = System.currentTimeMillis();
				
				// Initialise the network
				var network = SensorNetwork.readData(inputFile + sensorNumber + "/" + sensorNumber + "_4_40_90.0_" + field + ".INP");
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
				graph = null;
				long mid3 = System.currentTimeMillis();
				System.out.println("        Configuration Time: " + (mid3 - mid2));
				var spl = new SimplexSolverDouble();
				double result = -spl.execute(A, b, c);
				long end = System.currentTimeMillis();
				System.out.println("        Time: " + (end - mid2) + "\n        Result: " + result);
				results[field] = result;
			}
			long overallEnd = System.currentTimeMillis();
			double finalResult = 0;
			for (double d : results) {
				finalResult += d;
			}
			finalResult /= fields;
			output.println("Average Flow: " + finalResult);
			output.println("Average Time: " + ((overallEnd - overallStart) / fields + "\n"));
			for (double d : results) {
				output.println(d);
			}
			output.close();
		}
	}
}
