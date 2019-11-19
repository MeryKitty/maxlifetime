package kitty.research.maxlifetime.basics;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Random;

/**
 * Generate data, currently not used
 * 
 * @author MeryKitty
 *
 */
public class DataGenerator {
	private static final double W = 300, H = 150;
	private static final double E_alpha = Math.PI / 2, d_alpha = 0;
	private static final double E_r = 40, d_r = 0;
//	private static final double E_lt = 2, d_lt = 1;
//	private static final int sensorNum = 100;
	private static final String outputFile = "./data/input/";

	public static void main(String[] args) throws FileNotFoundException {
		Random rand = new Random();
		for (int sensorNumber = 50; sensorNumber < 350; sensorNumber += 50) {
			for (int field = 0; field < 100; field++) {
				PrintStream output = new PrintStream(outputFile + sensorNumber + "_" + field + ".txt");
				output.println(W + " " + H);
				for (int i = 0; i < sensorNumber; i++) {
					double x = rand.nextDouble() * W;
					double y = rand.nextDouble() * H;
					double alpha = rand.nextGaussian() * d_alpha + E_alpha;
					double r = rand.nextGaussian() * d_r + E_r;
					double lt = rand.nextInt(3) + 1; 
					output.println(x + " " + y + " " + r + " " + alpha + " " + lt);
				}
				output.close();
			}
		}
	}

}
