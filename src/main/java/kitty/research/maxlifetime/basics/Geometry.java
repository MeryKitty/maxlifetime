package kitty.research.maxlifetime.basics;

/**
 * Utility class to calculate several relationship of elementary geometric shape
 * 
 * @author MeryKitty
 *
 */
public final class Geometry {
	/**
	 * Limit a random directed angle into the interval [0, 2pi)
	 * 
	 * @param phi the angle to be standardised
	 * @return the standardised angle
	 */
	public static double standardiseAngle(double phi) {
		while (phi < 0) {
			phi += Math.PI * 2;
		}
		while (phi >= Math.PI * 2) {
			phi -= Math.PI * 2;
		}
		return phi;
	}
	
	/**
	 * Calculate the Euclid distance between 2 points
	 * 
	 * @param A the first point
	 * @param B the second point
	 * @return the distance between {@code A} and {@code B}
	 */
	public static double pointDistance(Point A, Point B) {
		double ax = A.x(), ay = A.y();
		double bx = B.x(), by = B.y();
		return Math.sqrt((ax - bx) * (ax - bx) + (ay - by) * (ay - by));
	}
	
	/**
	 * The directed angle from a point to another
	 * 
	 * @param A the first point
	 * @param B the second point
	 * @return the directed standardised angle from {@code A} to {@code B}
	 */
	public static double pointDirection(Point A, Point B) {
		double ax = A.x(), ay = A.y();
		double bx = B.x(), by = B.y();
		var distancex = bx - ax;
		var distancey = by - ay;
		double result = Math.atan2(distancey, distancex);
		return standardiseAngle(result);
	}
	
	/**
	 * Check if a ray is in the interval [start, end]. The interval is denoted
	 * in positive direction from start to end
	 * 
	 * @param start the start angle
	 * @param end the end angle
	 * @param phi the considered angle
	 * @return if {@code phi} is lying in the interval [{@code start}, {@code end}]
	 */
	public static boolean vectorBetween(double start, double end, double phi) {
		if (start <= end) {
			return start <= phi + 0.0001 && phi <= end + 0.0001;
		} else {
			return start <= phi + 0.0001 || phi <= end + 0.0001;
		}
	}
	
	/**
	 * Check if a point is inside a circular sector
	 * 
	 * @param A the considered point
	 * @param arc the circular sector
	 * @return a boolean value
	 */
	public static boolean pointCircularSectorIntersection(Point A, CircularSector arc) {
		double distance = pointDistance(A, arc.centre());
		if (distance > arc.r() + 0.0001) {
			return false;
		}
		double direction = pointDirection(arc.centre(), A);
		return vectorBetween(arc.startPhi(), arc.endPhi(), direction);
	}
	
	/**
	 * Check if a segment intersect a circular sector
	 * 
	 * @param seg the considered segment
	 * @param arc the circular sector
	 * @return a boolean value
	 */
	public static boolean segmentCircularSectorIntersection(Segment seg, CircularSector arc) {
		if (pointLineDistance(arc.centre(), seg.line()) > arc.r() + 0.0001) {
			return false;
		}
		if (pointCircularSectorIntersection(seg.A(), arc) ||
				pointCircularSectorIntersection(seg.B(), arc)) {
			return true;
		}
		if (segmentIntersection(seg, arc.startBound()) ||
				segmentIntersection(seg, arc.endBound())) {
			return true;
		}
		Point A = pointProjection(arc.centre(), seg.line());
		if (!pointLineIntersection(A, seg.line())) {
			throw new AssertionError();
		}
		if (pointSegmentIntersection1(A, seg) && vectorBetween(arc.startPhi(), arc.endPhi(), pointDirection(arc.centre(), A))) {
			return true;
		} else {
			return false;
		}
	}
	
	// TODO potential problem with degenerated CircularSector
	/**
	 * Check if 2 circular sector intersect each other
	 * 
	 * @param arc1 the first circular sector
	 * @param arc2 the second circular sector
	 * @return a boolean value
	 */
	public static boolean circularSectorIntersection(CircularSector arc1, CircularSector arc2) {
		double r1 = arc1.r(), r2 = arc2.r(), d = pointDistance(arc1.centre(), arc2.centre());
		if (d > r1 + r2 + 0.0001) {
			return false;
		}
		if (segmentCircularSectorIntersection(arc1.startBound(), arc2) ||
				segmentCircularSectorIntersection(arc1.endBound(), arc2) ||
				segmentCircularSectorIntersection(arc2.startBound(), arc1) ||
				segmentCircularSectorIntersection(arc2.endBound(), arc1)) {
			return true;
		}
		double cos, phi, direction;
		direction = pointDirection(arc1.centre(), arc2.centre());
		cos = (r1 * r1 + d * d - r2 * r2) / (2 * r1 * d);
		phi = standardiseAngle(direction + Math.acos(cos));
		if (!vectorBetween(arc1.startPhi(), arc1.endPhi(), phi)) {
			return false;
		}
		direction = standardiseAngle(direction + Math.PI);
		cos = (r2 * r2 + d * d - r1 * r1) / (2 * r2 * d);
		phi = standardiseAngle(direction + Math.acos(cos));;
		if (!vectorBetween(arc2.startPhi(), arc2.endPhi(), phi)) {
			return false;
		}
		return true;
	}
	
	/**
	 * Get the projection of a point on a line
	 * 
	 * @param P the considered point
	 * @param a the considered line
	 * @return the projection
	 */
	public static Point pointProjection(Point P, Line a) {
		// ax + by + c = 0
		// -bx + ay + b.px - a.py = 0
		double A = a.a(), B = a.b(), C = a.c();
		double X = P.x(), Y = P.y();
		double x = (-A * C + B * X * B - A * Y * B) / (A * A + B * B);
		double y = (-B * C - A * B * X + A * A * Y) / (A * A + B * B);
		return new Point(x, y);
	}
	
	/**
	 * Check if a point lies on a segment
	 * 
	 * @param P the point
	 * @param seg the segment
	 * @return a boolean value
	 */
	public static boolean pointSegmentIntersection(Point P, Segment seg) {
		if (pointLineIntersection(P, seg.line())) {
			return pointSegmentIntersection1(P, seg);
		} else {
			return false;
		}
	}
	
	/**
	 * It is known that the point lies on the line corresponding to a segment.
	 * Check if it lies on the segment
	 * 
	 * @param P the point
	 * @param seg the segment
	 * @return a boolean value
	 */
	private static boolean pointSegmentIntersection1(Point P, Segment seg) {
		if (seg.A().equalX(seg.B())) {
			return (P.y() - seg.A().y()) * (P.y() - seg.B().y()) <= 0.0001;
		} else {
			return (P.x() - seg.A().x()) * (P.x() - seg.B().x()) <= 0.0001;
		}
	}
	
	/**
	 * Check if a point lies on a line
	 * 
	 * @param P the point
	 * @param a the line
	 * @return a boolean value
	 */
	public static boolean pointLineIntersection(Point P, Line a) {
		return Math.abs(a.a() * P.x() + a.b() * P.y() + a.c()) < 0.0001;
	}
	
	/**
	 * Get the distance between a line and a point
	 * 
	 * @param P the point
	 * @param a the line
	 * @return the distance between {@code P} and {@code a}
	 */
	public static double pointLineDistance(Point P, Line a) {
		double A = a.a(), B = a.b(), C = a.c();
		return Math.abs(A * P.x() + B * P.y() + C) / Math.sqrt(A * A + B * B);
	}
	
	/**
	 * Compute a line equation on an arbitrary point. 2 point that has the
	 * same sign when plugged in this function will lie on a same half-plane 
	 * in accordance to the line
	 * 
	 * @param P the point
	 * @param a the line
	 * @return
	 */
	public static double pointLinePosition(Point P, Line a) {
		return (a.a() * P.x() + a.b() * P.y() + a.c());
	}
	
	/**
	 * Check if 2 segment intersect each other
	 * 
	 * @param AB the first segment
	 * @param CD the second segment
	 * @return a boolean value
	 */
	public static boolean segmentIntersection(Segment AB, Segment CD) {
		Point A = AB.A(), B = AB.B();
		Point C = CD.A(), D = CD.B();
		if (pointLinePosition(C, AB.line()) * pointLinePosition(D, AB.line()) <= 0.0001 &&
				pointLinePosition(A, CD.line()) * pointLinePosition(B, CD.line()) <= 0.0001) {
			return true;
		} else {
			return false;
		}
	}
}
