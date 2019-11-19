package kitty.research.maxlifetime.algorithm;

/**
 * An edge represent an intersection of sectors in adjacent sensors
 * in the network, for flow calculation, the edge is denoted as directed.
 * As a result, between 2 vertices there is always 2 directed edges except
 * the case 1 of it is the start or the end vertex.
 * 
 * @author MeryKitty
 *
 */
public class Edge {
	private final Vertex start, end;
	
	/**
	 * Construct an edge with given start and end edges
	 * 
	 * @param start
	 * @param end
	 */
	public Edge(Vertex start, Vertex end) {
		this.start = start;
		this.end = end;
	}
	
	/**
	 * The start vertex of the edge
	 * 
	 * @return
	 */
	public Vertex start() {
		return this.start;
	}
	
	/**
	 * The end vertex of the edge
	 * 
	 * @return
	 */
	public Vertex end() {
		return this.end;
	}
}
