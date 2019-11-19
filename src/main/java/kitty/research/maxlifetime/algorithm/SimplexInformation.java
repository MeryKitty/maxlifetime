package kitty.research.maxlifetime.algorithm;

import java.io.Closeable;

import org.nd4j.linalg.api.ndarray.INDArray;

/**
 * The class contains the essential information to conduct the Simplex algorithm,
 * which is the current constraint matrix, the current vertex of the simplex, and
 * the current basis of the simplex space
 * 
 * @author MeryKitty
 *
 */
public class SimplexInformation implements Closeable {
	private final INDArray A, x;
	private final int[] B;
	
	public SimplexInformation(INDArray A, int[]B, INDArray x) {
		this.A = A;
		this.x = x;
		this.B = B;
	}
	
	/**
	 * The current constraint matrix
	 * 
	 * @return
	 */
	public final INDArray A() {
		return this.A;
	}
	
	/**
	 * The current vertex of the simplex, note that x[i] >= 0 with every i
	 * 
	 * @return
	 */
	public final INDArray x() {
		return this.x;
	}

	/**
	 * The current basis of the algorithm, contains the indices of the basis in
	 * the initial vector list
	 * 
	 * @return
	 */
	public final int[] B() {
		return this.B;
	}
	
	/**
	 * Close the matrix A and x to prevent memory leaking
	 */
	@Override
	public final void close() {
		if (this.A.closeable() && !this.A.wasClosed()) {
			this.A.close();
		}
		if (this.x.closeable() && !this.x.wasClosed()) {
			this.x.close();
		}
	}
}
