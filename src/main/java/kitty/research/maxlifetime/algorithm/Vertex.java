package kitty.research.maxlifetime.algorithm;

import java.util.HashSet;
import java.util.Set;

/**
 * A vertex represents a sector of a certain sensor in the network
 * 
 * @author MeryKitty
 *
 */
public class Vertex {
	private final Set<Edge> in, out;
	
	public Vertex() {
		this.in = new HashSet<Edge>();
		this.out = new HashSet<Edge>();
	}
	
	/**
	 * The set of edges go in to this vertex
	 * 
	 * @return
	 */
	public Set<Edge> in() {
		return this.in;
	}
	
	/**
	 * The set of edges go out from this vertexx
	 * 
	 * @return
	 */
	public Set<Edge> out() {
		return this.out;
	}
}
