package kitty.research.maxlifetime.basics;

/**
 * A segment on the Euclidean plane
 * 
 * @author MeryKitty
 *
 */
public class Segment{
	private final Point A, B;
	private final Line line;
	
	public Segment(Point A, Point B) {
		this.A = A;
		this.B = B;
		this.line = new Line(A, B);
	}
	
	public final Point A() {
		return this.A;
	}
	
	public final Point B() {
		return this.B;
	}
	
	public final Line line() {
		return this.line;
	}
	
	@Override
	public int hashCode() {
		return this.A.hashCode() + this.B.hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof Segment) {
			var s = (Segment) o;
			return this.A.equals(s.A) && this.B.equals(s.B) || this.A.equals(s.B) && this.B.equals(s.A);
		} else {
			return false;
		}
	}
	
	@Override
	public String toString() {
		return "[" + this.A().toString() + ", " + this.B().toString() + "]";
	}
}
