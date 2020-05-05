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
	// private static final String location = "intLifetime1";
	// private static final String outputFile = "./data/output/" + location + "/";

	public static void main(String[] args) throws IOException, InterruptedException {
		String location = args[0];
		String outputFile = "./data/output/" + location + "/";
		int startValue = Integer.parseInt(args[1]);
		int endValue = Integer.parseInt(args[2]);
		int stepValue = Integer.parseInt(args[3]);
		for (int value = startValue; value < endValue; value += stepValue) {
			PrintStream output = new PrintStream(outputFile + value + ".txt");
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
			double[] realResults = new double[fields];
			int[] results = new int[fields];
			long[] totalTimes = new long[fields];
			long[] time1 = new long[fields];
			long[] time2 = new long[fields];
			long[] time3 = new long[fields];
			long[] time4 = new long[fields];
			var resultString = Files.readAllLines(Paths.get(outputFile + value + ".txt"));
			for (int i = 0; i < fields; i++) {
				var tempResult = resultString.get(i).split(" ");
				realResults[i] = Double.parseDouble(tempResult[1]);
				results[i] = Integer.parseInt(tempResult[2]);
				totalTimes[i] = Long.parseLong(tempResult[3]);
				time1[i] = Long.parseLong(tempResult[4]);
				time2[i] = Long.parseLong(tempResult[5]);
				time3[i] = Long.parseLong(tempResult[6]);
				time4[i] = Long.parseLong(tempResult[7]);
			}
			output = new PrintStream(outputFile + value + ".txt");
			output.println("EXPERIMENT OUTPUT:\n\nSensor Number: " + value + "\n");
			double finalRealResult = 0;
			for (double d : realResults) {
				finalRealResult += d;
			}
			finalRealResult /= fields;
			double finalResult = 0;
			for (int i : results) {
				finalResult += i;
			}
			finalResult /= fields;
			long finalTime = 0;
			for (long l : totalTimes) {
				finalTime += l;
			}
			finalTime /= fields;
			long finalTime1 = 0;
			for (long l : time1) {
				finalTime1 += l;
			}
			finalTime1 /= fields;
			long finalTime2 = 0;
			for (long l : time2) {
				finalTime2 += l;
			}
			finalTime2 /= fields;
			long finalTime3 = 0;
			for (long l : time3) {
				finalTime3 += l;
			}
			finalTime3 /= fields;
			long finalTime4 = 0;
			for (long l : time4) {
				finalTime4 += l;
			}
			finalTime4 /= fields;
			output.println("Average Real Flow: " + finalRealResult);
			output.println("Average Flow:      " + finalResult);
			output.println("Average Time:      " + finalTime);
			output.println();
			output.println("Time Step 2:       " + finalTime1);
			output.println("Time Step 3:       " + finalTime2);
			output.println("Time Step 4:       " + finalTime3);
			output.println("Time Step 5:       " + finalTime4);
			output.println();
			for (double d : results) {
				output.println(d);
			}
			output.close();
		}
		
	}

}
