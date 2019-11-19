package kitty.research.maxlifetime.algorithm;

import java.util.ArrayList;
import java.util.List;

/**
 * A cluster of vertices represents a unique sensor in the network 
 * with limited capacity
 * 
 * @author MeryKitty
 *
 */
public class Cluster {
	private final List<Vertex> vertices;
	private final double capacity;
	
	/**
	 * Construct a cluster with given capacity
	 * 
	 * @param capacity
	 */
	public Cluster(double capacity) {
		this.vertices = new ArrayList<Vertex>();
		this.capacity = capacity;
	}
	
	public Cluster(double capacity, int vertexNumber) {
		this.vertices = new ArrayList<Vertex>(vertexNumber);
		this.capacity = capacity;
	}
	
	/**
	 * The capacity of the cluster
	 * 
	 * @return
	 */
	public final double capacity() {
		return this.capacity;
	}
	
	/**
	 * The list of vertices in the cluster
	 * 
	 * @return
	 */
	public final List<Vertex> vertices() {
		return this.vertices;
	}
}
