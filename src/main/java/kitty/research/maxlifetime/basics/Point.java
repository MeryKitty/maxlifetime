package kitty.research.maxlifetime.basics;

/**
 * A point on the Euclidean plane
 * 
 * @author MeryKitty
 *
 */
public class Point {
	private final double x;
	private final double y;

	public Point(double x, double y) {
		this.x = x;
		this.y = y;
	}
	
	public final double x() {
		return this.x;
	}
	
	public final double y() {
		return this.y;
	}
	
	public final double distance(Point other) {
		return Geometry.pointDistance(this, other);
	}
	
	public final double direction(Point other) {
		return Geometry.pointDirection(this, other);
	}
	
	@Override
	public int hashCode() {
		return 1000 * (int)(1000 * x + y);
	}
	
	public boolean equalX(Point other) {
		return Math.abs(this.x - other.x) < 0.0001;
	}
	
	public boolean equalY(Point other) {
		return Math.abs(this.y - other.y) < 0.0001;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof Point) {
			var p = (Point) o;
			return this.equalX(p) && this.equalY(p);
		} else {
			return false;
		}
	}
	
	@Override
	public String toString() {
		return "(" + this.x + ", " + this.y + ")";
	}
}
