package kitty.research.maxlifetime.algorithm;

/**
 * An edge represent an intersection of sectors in adjacent sensors
 * in the network, for flow calculation, the edge is denoted as directed.
 * 
 * @author MeryKitty
 *
 */
public class Edge {
	private final Vertex start, end;
	private int capacity;
	
	/**
	 * Construct an edge with given start and end edges
	 * 
	 * @param start
	 * @param end
	 */
	public Edge(Vertex start, Vertex end) {
		this.start = start;
		this.end = end;
		this.capacity = 0;
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
	
	/**
	 * The capacity to analyse the flow
	 * 
	 * @return
	 */
	public int capacity() {
		return this.capacity;
	}
	
	void setCapacity(int capacity) {
		this.capacity = capacity;
	}
}
