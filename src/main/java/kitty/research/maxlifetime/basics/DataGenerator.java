package kitty.research.maxlifetime.basics;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
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
	private static final String outputFile = "./data/input/intLifetime0_1/";
	
	private static double gaussian(Random rand, double e, double d) {
		double result = rand.nextGaussian() * d + e;
		if (result < d) {
			return d;
		} else {
			return result;
		}
	}

	public static void main(String[] args) throws IOException {
		Random rand = new Random();
		Files.createDirectories(Paths.get(outputFile));
		for (int value = 2; value < 11; value += 1) {
			for (int field = 0; field < 25; field++) {
				PrintStream output = new PrintStream(outputFile + value + "_" + field + ".txt");
				output.println(W + " " + H);
				for (int i = 0; i < SENSOR_NUMBER; i++) {
					double x = rand.nextDouble() * W;
					double y = rand.nextDouble() * H;
					double alpha = gaussian(rand, E_alpha, d_alpha);
					double r = gaussian(rand, E_r, d_r);
					double lt = gaussian(rand, value, d_lt);//value * (rand.nextInt(3) + 1);//rand.nextInt(2 * value + 1) + value;
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
