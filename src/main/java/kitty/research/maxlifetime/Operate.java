package kitty.research.maxlifetime;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

public class Operate {
	private static final int fields = 25;
	private static final int step = 5;
	private static final String location = "lifetime";
	private static final String outputFile = "./data/output/" + location + "/";

	public static void main(String[] args) throws IOException, InterruptedException {
		for (int value = 1; value < 10; value += 1) {
			var output = new PrintStream(outputFile + value + ".txt");
			output.close();
			for (int field = 0; field < fields; field += step) {
				System.out.println("Start computing field: " + field);
				String[] childArgs = new String[] {"mvn", "exec:java", "-Dexec.mainClass=kitty.research.maxlifetime.App", "-Dexec.args=" + location + " " + value + " " + field + " " + step};
				System.out.println(String.join(" ", Arrays.asList(childArgs)));
				Process proc = new ProcessBuilder(childArgs).start();
				while (true) {
					var pipe = new BufferedReader(new InputStreamReader(proc.getInputStream()));
					String temp = pipe.readLine();
					while (temp != null && temp.length() > 1) {
						System.out.println(temp);
						temp = pipe.readLine();
					}
					if (!proc.isAlive()) {
						break;
					}
					Thread.sleep(10000);
				}
				proc.waitFor();
			}
			double[] results = new double[fields];
			long[] times = new long[fields];
			var resultString = Files.readAllLines(Paths.get(outputFile + value + ".txt"));
			for (int i = 0; i < fields; i++) {
				var tempResult = resultString.get(i).split(" ");
				results[i] = Double.parseDouble(tempResult[1]);
				times[i] = Long.parseLong(tempResult[2]);
			}
			output = new PrintStream(outputFile + value + ".txt");
			output.println("EXPERIMENT OUTPUT:\n\nSensor Number: " + value + "\n");
			double finalResult = 0;
			for (double d : results) {
				finalResult += d;
			}
			finalResult /= fields;
			long finalTime = 0;
			for (long l : times) {
				finalTime += l;
			}
			finalTime /= fields;
			output.println("Average Flow: " + finalResult);
			output.println("Average Time: " + finalTime + "\n");
			for (double d : results) {
				output.println(d);
			}
			output.close();
		}
		
	}

}
