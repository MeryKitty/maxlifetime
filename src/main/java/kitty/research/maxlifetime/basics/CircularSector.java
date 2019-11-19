package kitty.research.maxlifetime.basics;

/**
 * A circular sector is a region limited by 2 radius and the arc between
 * those radius of a circle. The circular arc is consisted of 3 vertices
 * and 3 sides, with 2 segment and an arc. The circular arc is determined
 * uniquely by 3 vertices, with a centre, a starting point, denote the arc
 * going from there in positive direction, and an end point, denote the arc
 * going to there in positive direction
 * 
 * @author MeryKitty
 *
 */
public class CircularSector {
	private final Point centre, startVer, endVer;
	private final double r, startPhi, endPhi;
	private final Segment startBound, endBound;
	
	public CircularSector(Point centre, double r, double startPhi, double endPhi) {
		this.centre = centre;
		this.r = r;
		this.startPhi = Geometry.standardiseAngle(startPhi);
		this.endPhi = Geometry.standardiseAngle(endPhi);
		this.startVer = new Point(this.centre.x() + this.r * Math.cos(this.startPhi), this.centre.y() + this.r * Math.sin(this.startPhi));
		this.endVer = new Point(this.centre.x() + this.r * Math.cos(this.endPhi), this.centre.y() + this.r * Math.sin(this.endPhi));
		this.startBound = new Segment(this.centre, this.startVer);
		this.endBound = new Segment(this.centre, this.endVer);
	}
	
	/**
	 * The centre of the circular arc
	 * @return
	 */
	public final Point centre() {
		return this.centre;
	}
	
	/**
	 * The start vertex of the arc
	 * 
	 * @return
	 */
	public final Point startVer() {
		return this.startVer;
	}
	
	/**
	 * The end vertex of the arc
	 * 
	 * @return
	 */
	public final Point endVer() {
		return this.endVer;
	}
	
	/**
	 * The radius of the circular arc
	 * 
	 * @return
	 */
	public final double r() {
		return this.r;
	}
	
	/**
	 * The directed angle from the centre to the start vertex
	 * 
	 * @return
	 */
	public final double startPhi() {
		return this.startPhi;
	}
	
	/**
	 * The directed angle from the centre to the end vertex
	 * 
	 * @return
	 */
	public final double endPhi() {
		return this.endPhi;
	}
	
	/**
	 * The segment connect the centre and the start vertex
	 * 
	 * @return
	 */
	public final Segment startBound() {
		return this.startBound;
	}
	
	/**
	 * The segment connect the centre and the end vertex
	 * 
	 * @return
	 */
	public final Segment endBound() {
		return this.endBound;
	}
}
