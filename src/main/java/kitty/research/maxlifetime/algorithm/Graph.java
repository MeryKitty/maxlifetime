package kitty.research.maxlifetime.algorithm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.nd4j.linalg.api.ndarray.INDArray;
import kitty.research.maxlifetime.model.Sector;
import kitty.research.maxlifetime.model.Sensor;
import kitty.research.maxlifetime.model.SensorNetwork;

/**
 * The graph represents the sensor network, with each vertex corresponds
 * to a sector, a cluster is a sensor, and an edge is a connection of 
 * overlapped sectors
 * 
 * @author MeryKitty
 *
 */
public class Graph {
	private final List<Cluster> clusterList;
	private final Map<Edge, Integer> edgeMap;
	private final List<Edge> edgeList;
	private final Vertex startVer, endVer;

	/**
	 * Construct the graph represents the sensor network
	 * 
	 * @param network
	 */
	public Graph(SensorNetwork network) {
		int sensorNumber = network.sensorList().size();
		int edgeNumber = network.countOverlaps();
		this.edgeMap = new HashMap<Edge, Integer>(edgeNumber * 3 / 2);
		this.edgeList = new ArrayList<>(edgeNumber);
		this.clusterList = new ArrayList<Cluster>(sensorNumber + 1);
		this.startVer = new Vertex();
		this.endVer = new Vertex();
		
		// A map to map each sector with its corresponding vertex
		var sectorVertexMap = new HashMap<Sector, Vertex>();
		// Initialise the clusters and vertices of the graph
		for (Sensor S : network.sensorList()) {
			Cluster tempClus = new Cluster(S.lifetime(), S.sectorList().size() + 1);
			for (Sector sec : S.sectorList()) {
				var temp = new Vertex();
				sectorVertexMap.put(sec, temp);
				tempClus.vertices().add(temp);
			}
			this.clusterList.add(tempClus);
		}
		
		// List of sectors intersects the boundaries of the sensing field
		var leftOverlap = network.sectorsOverlapLeftEdge();
		var rightOverlap = network.sectorsOverlapRightEdge();
		
		// The array contains 1 element, used for lambda expression
		// Represent the position of the new edge in the total edge list
		int i[] = new int[1];
		// Add the edges go from the start edge
		for (Sector s : leftOverlap) {
			Vertex current = sectorVertexMap.get(s);
			Edge temp = new Edge(this.startVer, current);
			edgeMap.put(temp, i[0]++);
			edgeList.add(temp);
			current.in().put(this.startVer, temp);
			this.startVer.out().put(current, temp);
		}
		// Add the edges go to the end edges
		for (Sector s : rightOverlap) {
			Vertex current = sectorVertexMap.get(s);
			Edge temp = new Edge(current, this.endVer);
			edgeMap.put(temp, i[0]++);
			edgeList.add(temp);
			current.out().put(this.endVer, temp);
			this.endVer.in().put(current, temp);
		}
		// Add the edges between any pair of connected vertices, note that (A, B)
		// and (B, A) are different
		network.iterateSectors((Sector s) -> {
			Vertex current = sectorVertexMap.get(s);
			for (Sector s1 : s.intersectedSectors()) {
				if (s.centre().x() < s1.centre().x()) {
					Vertex current1 = sectorVertexMap.get(s1);
					Edge temp = new Edge(current, current1);
					edgeMap.put(temp, i[0]++);
					edgeList.add(temp);
					current.out().put(current1, temp);
					current1.in().put(current, temp);
				}
			}
		});
	}
	
	/**
	 * The list of cluster of the graph
	 * 
	 * @return
	 */
	public List<Cluster> clusterList() {
		return this.clusterList;
	}
	
	/**
	 * The map map each edge with its position in the edge list, used for
	 * constraint matrix initialisation
	 * 
	 * @return
	 */
	public Map<Edge, Integer> edgeMap() {
		return this.edgeMap;
	}
	
	/**
	 * Iterate over all vertices of the graph
	 * 
	 * @param action
	 */
	public void iterateVertices(Consumer<? super Vertex> action) {
		for (var clus : this.clusterList) {
			for (var ver : clus.vertices()) {
				action.accept(ver);
			}
		}
	}
	
	/**
	 * The number of vertices in the graph
	 * 
	 * @return
	 */
	public int vertexNumber() {
		int[] i = new int[1];
		this.iterateVertices((Object o) -> i[0]++);
		return i[0];
	}
	
	/**
	 * Initialise the constraint and target matrices corresponding to the problem of
	 * maximal flow inside the graph. The matrices are built as follow:<p>
	 * - Each column correspond to a variable, which is an edge with its current flow
	 * in the graph, plus {@code clusterNumber} columns represent the leftover of the
	 * cluster capacity inequalities.<p>
	 * - Each row corresponds to a constraint:<p>
	 *   + The first {@code vertexNumber} rows correspond to the flow rule at each vertex. Which is
	 *   stated that except start and end vertices, all vertices inside the graph must have equal 
	 *   in flow and out flow.<p>
	 *   + The last {@clusterNumber} rows correspond the capacities of the clusters, which are
	 *   calculated as the sum of all flow going in to the cluster. Note that there is no edges
	 *   between vertices of the same cluster.
	 * 
	 * @param A the LHS constraint matrix
	 * @param b the RHS constraint matrix
	 * @param c the target matrix
	 */
	public void initialiseTensors(INDArray A, INDArray b, INDArray c) {
		int clusterNumber = this.clusterList().size();
		int vertexNumber = this.vertexNumber();
		int edgeNumber = this.edgeMap().size();
		// Verify the correct size of the input matrices
		if (A.columns() != c.columns() || A.rows() != b.rows() ||
				A.rows() != vertexNumber + clusterNumber ||
				A.columns() != edgeNumber + clusterNumber) {
			throw new IllegalArgumentException();
		}

		// The array contains 1 element used for lambda expression
		// Denoted the row which are being considered
		int i[] = new int[1];
		// For each vertex, the in edge has weight of 1 while the out edge has weight of 0
		// The RHS constraint is always zero
		this.iterateVertices((Vertex current) -> {
			for (var e : current.in().values()) {
				int column = edgeMap.get(e);
				A.getScalar(i[0], column).assign(1);
			}
			for (var e : current.out().values()) {
				int column = edgeMap.get(e);
				A.getScalar(i[0], column).assign(-1);
			}
			i[0]++;
		});
		
		// For each cluster, the sum of flow go in the cluster doesn't exceed its capacity
		// The current flow through the j-th cluster is calculated as the sum of all flow go
		// in to the vertices in the cluster. 
		for (int j = 0; j < this.clusterList.size(); j++) {
			// The row is counted from the end of last step, which is the number of vertices
			int row = j + vertexNumber;
			var current = this.clusterList.get(j);
			for (var ver : current.vertices()) {
				for (var e : ver.in().values()) {
					int column = edgeMap.get(e);
					A.getScalar(row, column).assign(1);
				}
			}
			// The left over amount of the cluster capacity inequalities.
			// a <= b --> a + x = b, x >= 0
			// The column number is counted from the number of edges
			A.getScalar(row, j + edgeNumber).assign(1);
		}
		// The capacity of the j-th cluster
		for (int j = 0; j < this.clusterList.size(); j++) {
			// The row is counted from the end of last step, which is the number of vertices
			int row = j + vertexNumber;
			b.getScalar(row, 1).assign(this.clusterList.get(j).capacity());
		}
		
		// Initialise the target matrix, which is the sum of all edges go in to the end vertex
		// of the graph
		for (Edge e : this.endVer.in().values()) {
			int column = edgeMap.get(e);
			c.getScalar(column).assign(1);
		}
		
		// Double check (not so sure why it is here tbh)
		if (edgeMap.size() + this.clusterList.size() != c.columns()) {
			throw new AssertionError();
		}
	}
	
	public int analyseIntFlow(double[] vertex) {
		// initialise the flow network
		for (int i = 0; i < this.edgeList.size(); i++) {
			this.edgeList.get(i).setCapacity((int) (vertex[i] + 1E-9));
		}
		//
		int intFlow = 0;
		while (true) {
			var path = this.shortestIntPath();
			if (path == null) {
				break;
			}
			int tempFlow = this.intFlowPush(path);
			intFlow += tempFlow;
		}
		return intFlow;
	}
	
	private List<Vertex> shortestIntPath() {
		var next = new HashMap<Vertex, Vertex>();
		var passed = new HashSet<Vertex>();
		var current = new HashSet<Vertex>();
		passed.add(this.endVer);
		current.add(this.endVer);
		outerLoop:
			while (!current.isEmpty()) {
				var temp = new HashSet<Vertex>();
				for (var ver : current) {
					for (var exam : ver.in().entrySet()) {
						if (exam.getValue().capacity() > 0 && !passed.contains(exam.getKey())) {
							if (temp.add(exam.getKey())) {
								next.put(exam.getKey(), ver);
								if (exam.getKey().equals(this.startVer)) {
									break outerLoop;
								}
							}
						}
					}
				}
				current = temp;
				passed.addAll(current);
			}
		if (next.containsKey(this.startVer)) {
			var tempVertex = this.startVer;
			var result = new ArrayList<Vertex>();
			while (tempVertex != null) {
				result.add(tempVertex);
				tempVertex = next.get(tempVertex);
			}
			return result;
		} else {
			return null;
		}
	}
	
	private int intFlowPush(List<Vertex> path) {
		int flow = Integer.MAX_VALUE;
		for (int i = 0; i < path.size() - 1; i++) {
			var tempEdge = path.get(i).out().get(path.get(i + 1));
			if (tempEdge.capacity() < flow) {
				flow = tempEdge.capacity();
			}
		}
		for (int i = 0; i < path.size() - 1; i++) {
			var start = path.get(i); var end = path.get(i + 1);
			var tempEdge = start.out().get(end);
			tempEdge.setCapacity(tempEdge.capacity() - flow);
			var reverseEdge = end.out().get(start);
			if (reverseEdge == null) {
				reverseEdge = new Edge(end, start);
				end.out().put(start, reverseEdge);
				start.in().put(end, reverseEdge);
			}
			reverseEdge.setCapacity(reverseEdge.capacity() + flow);
		}
		return flow;
	}
}
