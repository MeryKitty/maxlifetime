package kitty.research.maxlifetime.basics;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class DataGen {
	
	static int W = 300;
	static int H = 150;
	static double alpha = 90;
	
	public static void main(String[] args) {
		//gen(200, 4, 20, "S3");
		for (int n = 50; n <= 400; n+= 50) {
			for (int field = 0; field < 100; field++) {
				gen(n, 4, 40, "S1/" + n, field);
			}	
		}
//		for (int p = 1; p <= 8; p+= 1) {
//			gen(200, p, 40, "S2");
//		}
//		for (int r = 20; r <= 80; r+= 5) {
//			gen(200, 4, r, "S3");
//		}
//		for (int a = 10; a <= 80; a+= 10) {
//			alpha = a * Math.PI / 180;
//			gen(200, 4, 40, "S4");
//		}
	}

	

	public static void gen(int N, int p, int r, String name, int field) {
		Random rand = new Random();
		File f1 = new File("data");
		if (!f1.exists())
			f1.mkdirs();
		f1 = new File("data/" + name);
		if (!f1.exists())
			f1.mkdirs();
		for (int q = 0; q < 1; q++) {
			File f = new File("data/" + name + "/" + N + "_" + p + "_" + r + "_" + alpha + "_" + field + ".INP");
			FileWriter fw = null;
			BufferedWriter bw = null;
			String str = "";
			try {
				fw = new FileWriter(f);
				bw = new BufferedWriter(fw);
				str+= W + " " + H + " " + N + "\n";
				for (int i = 0; i < N; ++i) {
					double x = rand.nextDouble()*W;
					double y = rand.nextDouble()*H;
					int c = rand.nextInt(3)  +1;
					str += x + " " + y + " " + r + " " + c + " " + alpha + " " + p + "\n";
					double vi = rand.nextDouble() * 90;
					for (int j=0;j<p;j++) {
						double tempVi = vi + j * 90;
						str += tempVi + " ";
					}
					str+= "\n";
				}
				bw.write(str);
				bw.flush();
			} catch (Exception ex) {
				System.out.println("write error");
			} finally {
				if (null != fw) {
					try {
						fw.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if (null != bw) {
					try {
						bw.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
}
