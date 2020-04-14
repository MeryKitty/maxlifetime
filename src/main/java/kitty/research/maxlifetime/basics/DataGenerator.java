package kitty.research.maxlifetime.basics;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Random;

/**
 * Generate data, currently not used
 * 
 * @author MeryKitty
 *
 */
public class DataGenerator {
	private static final double W = 300, H = 150;
	private static final double E_alpha = 60, d_alpha = 10;
	private static final double E_r = 30, d_r = 5;
	private static final double E_lt = 2, d_lt = 0.5;
	private static final int DIRECTIONS = 6;
	private static final int SENSOR_NUMBER = 100;
	private static final String outputFile = "./data/input/lifetime/";
	
	private static double gaussian(Random rand, double e, double d) {
		double result = rand.nextGaussian() * d + e;
		if (result < d) {
			return d;
		} else {
			return result;
		}
	}

	public static void main(String[] args) throws FileNotFoundException {
		Random rand = new Random();
		for (int value = 1; value < 10; value += 1) {
			for (int field = 0; field < 25; field++) {
				PrintStream output = new PrintStream(outputFile + value + "_" + field + ".txt");
				output.println(W + " " + H);
				for (int i = 0; i < SENSOR_NUMBER; i++) {
					double x = rand.nextDouble() * W;
					double y = rand.nextDouble() * H;
					double alpha = gaussian(rand, E_alpha, d_alpha);
					double r = gaussian(rand, E_r, d_r);
					double lt = (rand.nextInt(value * 2 + 1) + value);
					output.println(x + " " + y + " " + r + " " + alpha + " " + lt);
					double step = 360. / DIRECTIONS;
					double base = 0;
					var tempDirections = new ArrayList<String>();
					for (int j = 0; j < DIRECTIONS; j++) {
						tempDirections.add(Double.toString(base + step * j));
					}
					output.println(String.join(" ", tempDirections));
				}
				output.close();
			}
		}
	}
}
