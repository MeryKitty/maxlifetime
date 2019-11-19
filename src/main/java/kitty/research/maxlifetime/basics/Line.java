package kitty.research.maxlifetime.basics;

/**
 * The class represent a line on Euclid plane
 * 
 * @author MeryKitty
 *
 */
public class Line {
	private final double a, b, c;
	
	/**
	 * 
	 * 
	 * @param a
	 * @param b
	 * @param c
	 */
	public Line(double a, double b, double c) {
		if (a == 0 && b == 0 && c == 0) {
			throw new IllegalArgumentException();
		}
		double norm = Math.sqrt(a * a + b * b + c * c);
		this.a = a / norm;
		this.b = b / norm;
		this.c = c / norm;
	}
	
	public Line(Point A, Point B) {
		double a, b, c;
		if (A.equals(B)) {
			throw new IllegalArgumentException();
		} else if (A.equalX(B)) {
			a = 1; b = 0; c = -A.x();
		} else if (A.equalY(B)) {
			a = 0; b = 1; c = -A.y();
		} else {
			a = 1 / (B.x() - A.x());
			b = -1 / (B.y() - A.y());
			c = -A.x() / (B.x() - A.x()) + A.y() / (B.y() - A.y());
		}
		double norm = Math.sqrt(a * a + b * b + c * c);
		this.a = a / norm;
		this.b = b / norm;
		this.c = c / norm;
	}
	
	public final double a() {
		return this.a;
	}
	
	public final double b() {
		return this.b;
	}
	
	public final double c() {
		return this.c;
	}
	
	@Override
	public int hashCode() {
		return 1000 * (int)(1000 * this.c / this.a + this.c / this.b);
	}
	
	public boolean equals(Object o) {
		if (o instanceof Line) {
			var l = (Line) o;
			return Math.abs(l.c / l.a - this.c / this.a) < 0.0001 && Math.abs(l.c / l.b - this.c / this.b) < 0.0001;
		} else {
			return false;
		}
	}
	
	@Override
	public String toString() {
		return String.format("%.2fx + %.2fy + %.2f = 0", this.a, this.b, this.c);
	}
}
