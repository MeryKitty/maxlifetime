package kitty.research.maxlifetime.algorithm;

import java.util.HashMap;
import java.util.Map;

/**
 * A vertex represents a sector of a certain sensor in the network
 * 
 * @author MeryKitty
 *
 */
public class Vertex {
	private final Map<Vertex, Edge> in, out;
	
	public Vertex() {
		this.in = new HashMap<Vertex, Edge>();
		this.out = new HashMap<Vertex, Edge>();
	}
	
	/**
	 * The set of edges go in to this vertex
	 * 
	 * @return
	 */
	public Map<Vertex, Edge> in() {
		return this.in;
	}
	
	/**
	 * The set of edges go out from this vertexx
	 * 
	 * @return
	 */
	public Map<Vertex, Edge> out() {
		return this.out;
	}
}
