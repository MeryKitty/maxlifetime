package kitty.research.maxlifetime.algorithm;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.api.ops.impl.indexaccum.IAMax;
import org.nd4j.linalg.api.ops.impl.indexaccum.IMax;
import org.nd4j.linalg.api.ops.impl.indexaccum.IMin;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.NDArrayIndex;
import org.nd4j.linalg.ops.transforms.Transforms;

/**
 * Conduct the algorithm with double accuracy, bad for performance but there
 * is no other choice
 * 
 * @author MeryKitty
 *
 */
public class SimplexSolverDouble {
	// The value of accepted error for every operation
	private final double tolerance;
	
	private static final double DEFAULT_TOLERANCE = 1E-9;
	
	public SimplexSolverDouble(double tolerance) {
		this.tolerance = tolerance;
	}
	
	/**
	 * Construct a {@code SimplexSolverDouble} instance with default {@code tolerance}
	 */
	public SimplexSolverDouble() {
		this(DEFAULT_TOLERANCE);
	}
	
	/**
	 * Apparently it is used to calculate the target value with given c and x
	 * Not use
	 * 
	 * @param c
	 * @param arg
	 * @return
	 */
	public double compute(INDArray c, INDArray arg) {
		if (arg.columns() != c.columns()) {
			throw new AssertionError();
		}
		return c.mmul(arg.transpose()).getDouble(0, 0);
	}
	
	/**
	 * Test the Gaussian elimination for choosing starting vertex of the algorithm.
	 * In process, since the algorithm may offer negative value of x.
	 * 
	 * @param A
	 * @param b
	 * @param c
	 * @return
	 */
	public double executeTest(INDArray A, INDArray b, INDArray c) {
		System.out.println("    Initialisation:");
		long start = System.currentTimeMillis();
		int rowNumber = A.rows();
		int columnNumber = A.columns();
		if (b.rows() != rowNumber || c.columns() != columnNumber) {
			throw new IllegalArgumentException();
		}
		var tempA = A.dup();
		var tempb = b.dup();
		var pivotList = new ArrayList<Integer>();
		int independentNumber = this.GaussianTransformation(pivotList, tempA, tempb);
		if (independentNumber == -1) {
			throw new UnsupportedOperationException("The valid region is empty!");
		} else if (independentNumber == 0) {
			// TODO Other message maybe :/
			throw new UnsupportedOperationException("There is no constraints!");
		}
		
		long mid = System.currentTimeMillis();
		System.out.println("        Total Time: " + (mid - start) + "\n    Computation:");
		
		A = tempA.get(NDArrayIndex.interval(0, independentNumber), NDArrayIndex.all()).dup().reshape(independentNumber, columnNumber);
		b = tempb.get(NDArrayIndex.interval(0, independentNumber), NDArrayIndex.all()).dup().reshape(independentNumber, 1);
		this.close(tempA); this.close(tempb);
		int[] B = new int[independentNumber];
		for (int i = 0; i < B.length; i++) {
			B[i] = pivotList.get(i);
		}
		double result = this.execute1(new SimplexInformation(A, B, b), c);
		long end = System.currentTimeMillis();
		System.out.println("        Total Time: " + (end - mid));
		this.close(A); this.close(b);
		return result;
	}
	
	/**
	 * Execute the Simplex algorithm and return the minimum of the target function.
	 * The algorithm is to find the minimum of c.x with x satisfied the constraint
	 * A.x = b (the dot represents the matrix multiplication)
	 * 
	 * @param A the LHS constraint matrix, which is a {@code m x n} matrix
	 * @param b the RHS constraint matrix, which is a {@code m x 1} matrix
	 * @param c the target matrix, which is a {@code 1 x n} matrix
	 * @return the minimum value of the target function
	 */
	public double execute(INDArray A, INDArray b, INDArray c) {
		long start = System.currentTimeMillis();
		// Extract the list of independent row vector using echelon transformation
		// The list obtained is the list of indices of A that correspond to the row
		// vectors that are linearly independent
		var indeList = this.independentRowList(A);
		int[] handle = new int[indeList.size()];
		for (int i = 0; i < handle.length; i++) {
			handle[i] = indeList.get(i);
		}
		// Remove the linearly dependent row vectors in the constraint matrices
		A = A.getRows(handle);
		b = b.getRows(handle);
		// Change all RHS elements to non-negative, the rows that has corresponding 
		// b value < tolerance is multiplied by -1
		// |a + d| / (a + d) = -1 if a < -d, 1 if a > -d. The probability that a = d
		// is ignorable
		var temp = b.add(this.tolerance);
		var temp2 = Transforms.abs(temp, true);
		temp2.divi(temp);
		Transforms.abs(b, false);
		A.muli(temp2);
		this.close(temp); this.close(temp2);
		
		long mid1 = System.currentTimeMillis();
		System.out.println("        Reduce Constraints Time: " + (mid1 - start));
		
		// Check the integrity of the input and get the parameter to use for
		// conveniences
		int columnNumber = A.columns();
		int rowNumber = A.rows();
		if (columnNumber != c.columns() || rowNumber != b.rows()) {
			throw new AssertionError();
		}
		System.out.println("        Initialisation:");
		// Get the starting state to conduct the simplex algorithm
		var startInfo = this.getVertex(A, b);
		this.close(A);
		this.close(b);
		long mid2 = System.currentTimeMillis();
		System.out.println("            Total Time: " + (mid2 - mid1) + "\n    Computation:");
		// Conduct the simplex algorithm with found starting state
		var result = this.execute1(startInfo, c);
		long end = System.currentTimeMillis();
		System.out.println("            Total Time: " + (end - mid2));
		startInfo.close();
		return result;
	}
	
	/**
	 * Since the simplex method figures a way to go from a vertex to other vertex along a certain
	 * edge of the siplex, it is crucial to find the starting vertex to conduct the algorithm.
	 * If in every row there is a column that has non-zero value only on that row, the starting
	 * vertices is easy to be determined. This function will find the starting tableau corresponding
	 * to a vertex of the simplex to conduct the simplex algorithm on, the method is to find the
	 * minimum of d with given information that Ax + d = b. The result is expected to be 0, and
	 * the destination tableau of the 1-st phase is also a tableau for the initial problem.
	 * 
	 * @param A the starting LHS constraint matrix
	 * @param b the starting RHS constraint matrix
	 * @return the information of the state which can be used to conduct the simplex algorithm
	 */
	private SimplexInformation getVertex(INDArray A, INDArray b) {
		// Verify the integrity of the input
		int rowNumber = A.rows();
		int columnNumber = A.columns();
		if (rowNumber != b.rows()) {
			throw new AssertionError();
		}
		
		// Create new constraint matrix that satisfy that in every row there is a column that
		// has non-zero value only on that row to solve the side problem of find min d with 
		// Ax + d = b
		INDArray eye = Nd4j.eye(rowNumber);
		A = Nd4j.hstack(A, eye);
		this.close(eye);
		var x = b.dup();
		// The starting basis of the problem
		int[] B = new int[rowNumber];
		for (int i = 0; i < rowNumber; i++) {
			B[i] = columnNumber + i;
		}
		// The target function corresponding to finding min d
		var c = Nd4j.zeros(rowNumber + columnNumber);
		for (int i = 0; i < rowNumber; i++) {
			c.putScalar(columnNumber + i, 1);
		}
		// Execute the simplex method on the side problem
		double phase1 = this.execute1(new SimplexInformation(A, B, x), c);
		// The received basis shouldn't contains the non-original vector (corresponding to the
		// indices >= column number of A
		for (int i = 0; i < rowNumber; i++) {
			if (B[i] >= columnNumber) {
				// If the corresponding coordinate x is non-zero, then the min of d is not 0, which prove that
				// our original region is empty
				if (x.getDouble(i) >= this.tolerance) {
					throw new UnsupportedOperationException("The valid region is empty!");
				}
				// If the corresponding coordinate x is zero, then we only need to rotate the simplex to
				// eliminate the appearance of unwanted basis without changing the current value of d
				else {
					for (int j = 0; j < columnNumber; j++) {
						double temp = A.getDouble(i, j);
						if (temp >= this.tolerance || temp <= -this.tolerance) {
							var rotateRow = A.getRow(i).dup().reshape(1, columnNumber + rowNumber);
							var rotateColumn = A.getColumn(j).dup().reshape(rowNumber, 1);
							phase1 = this.rotate(A, B, x, phase1, null, i, j, rotateRow, rotateColumn);
							this.close(rotateRow); this.close(rotateColumn);
							break;
						}
					}
				}
			}
		}
		// Remove the bonus columns and return the current state of the simplex tableau for
		// the next phase
		var temp = A.get(NDArrayIndex.all(), NDArrayIndex.interval(0, columnNumber)).dup();
		this.close(A);
		A = temp;
		this.close(c);
		return new SimplexInformation(A, B, x);
	}	
	
	/**
	 * Execute the algorithm with known starting tableau and a target function
	 * 
	 * @param info the starting state of the algorithm
	 * @param c the target matrix
	 * @return
	 */
	public double execute1(SimplexInformation info, INDArray c) {
		// Extract the starting states and verify the input integrity
		var A = info.A();
		var B = info.B();
		var x = info.x();
		int rowNumber = A.rows();
		int columnNumber = A.columns();
		if (A.columns() != c.columns() || A.rows() != rowNumber || A.rows() != x.rows()) {
			throw new AssertionError();
		}
		
		// Find the value of delta corresponding to the initial state, the
		// delta of the next step is calculated from the delta of its previous
		// step
		var cB = Nd4j.zeros(B.length, 1, 'c');
		for (int i = 0; i < rowNumber; i++) {
			cB.getScalar(i, 0).assign(c.getDouble(B[i], 1));
		}
		var temp = A.mulColumnVector(cB);
		var delta = temp.sum(true, 0).subi(c);
		this.close(temp);
		temp = x.mul(cB);
		double f = temp.sumNumber().doubleValue();
		this.close(temp); this.close(cB);
		
		// Start iterating the algorithm
		long start = System.currentTimeMillis();
		int repetition = 0;
		long step1 = 0, step2 = 0;
		while (true) {
			long tempStart = System.currentTimeMillis();
			// Get rotate column
			int rotateColumnIndex = Nd4j.getExecutioner().execAndReturn(new IMax(delta)).getFinalResult().intValue();
			if (delta.getDouble(rotateColumnIndex) < this.tolerance) {
				break;
			}
			var rotateColumn = A.getColumn(rotateColumnIndex).dup().reshape(rowNumber, 1);
			temp = rotateColumn.sub(this.tolerance);
			var temp2 = Transforms.relu(temp, true);
			temp2.divi(temp);
			this.close(temp);
			temp = rotateColumn.mul(temp2);
			// Get the theta corresponding to the rotateColumn
			var theta = x.div(temp);
			Transforms.abs(theta, false);	// Eliminate potential -Infinity
			this.close(temp); this.close(temp2);
			// Get rotate row
			int rotateRowIndex = Nd4j.getExecutioner().execAndReturn(new IMin(theta)).getFinalResult().intValue();
			if (!Double.isFinite(theta.getDouble(rotateRowIndex, 0))) {
				f = Double.NEGATIVE_INFINITY;
				break;
			}
			var rotateRow = A.getRow(rotateRowIndex).dup().reshape(1, columnNumber);
			
			long tempMid = System.currentTimeMillis();
			// Rotate the tableau
			f = this.rotate(A, B, x, f, delta, rotateRowIndex, rotateColumnIndex, rotateRow, rotateColumn);

			this.close(rotateRow); this.close(rotateColumn); this.close(theta);
			long tempEnd = System.currentTimeMillis();
			step1 += tempMid - tempStart;
			step2 += tempEnd - tempMid;
			repetition++;
		}
		long end = System.currentTimeMillis();
		System.out.println("            Iteration Time: " + (end - start) + "\n                Step 1: " + step1 + "\n                Step 2: " + step2);
		System.out.println("            Simplex Iterations: " + repetition + "\n            Average Time: " + (end - start) / (repetition + 1));
		return f;
	}
	
	/**
	 * Rotate the simplex tableau at the given rotate column and rotate row
	 * 
	 * @param A The current constraint matrix
	 * @param B The current basis of the simplex space
	 * @param x The current vertex of the simplex denoted through the basis
	 * @param f The current target function value
	 * @param delta The current delta
	 * @param rotateRowIndex
	 * @param rotateColumnIndex
	 * @param rotateRow
	 * @param rotateColumn
	 * @return the new target function value correspond to the new vertex of the simplex
	 */
	private final double rotate(INDArray A, int[] B, INDArray x, double f, INDArray delta, int rotateRowIndex, int rotateColumnIndex, INDArray rotateRow, INDArray rotateColumn) {
		// Get main element of the rotation
		double mainEle = A.getDouble(rotateRowIndex, rotateColumnIndex);
		// Update rotate row
		rotateRow.divi(mainEle);
		// Update the remaining rows
		var updateA = rotateColumn.mmul(rotateRow);
		A.subi(updateA);
		A.putRow(rotateRowIndex, rotateRow);
		this.close(updateA);
		// Update x
		double xPivot = x.getDouble(rotateRowIndex, 0) / mainEle;
		x.subi(rotateColumn.muli(xPivot));
		x.putScalar(rotateRowIndex, xPivot);
		// Update B
		B[rotateRowIndex] = rotateColumnIndex;
		// Update target function
		// Update delta
		if (delta != null) {
			double deltaPivot = delta.getDouble(rotateColumnIndex);
			delta.subi(rotateRow.muli(deltaPivot));
			f -= xPivot * deltaPivot;
		}
		return f;
	}
	
	/**
	 * Close a matrix to prevent memory leaking, use to shorten the syntax
	 * 
	 * @param a
	 * @return
	 */
	private boolean close(INDArray a) {
		if (!a.closeable()) {
			return false;
		} else if (!a.wasClosed()) {
			a.close();
		}
		return true;
	}
	
	/**
	 * Get the list of indices of linearly independent row vectors of a matrix
	 * using the echelon transformation (a.k.a Gaussian elimination). Good luck
	 * with reading, use the Test class to test have a visual operation of the 
	 * function. I'm figuring out how to write the doc since this requires a
	 * little linear algebra knowledge.
	 * 
	 * @param A The matrix contains several row vectors
	 * @return The list contains the indices of the linearly independent row vectors
	 */
	public final List<Integer> independentRowList(INDArray A) {
		int rowNumber = A.rows();
		int columnNumber = A.columns();
		// Echelon transformation
		var A1 = A.dup();
		var pivot = new ArrayList<Integer>();
		int tempPosition = -1;
		int tempPivot = -1;
		while (true) {
			var part = A1.get(NDArrayIndex.interval(tempPivot + 1, rowNumber), NDArrayIndex.interval(tempPosition + 1, columnNumber));
			int tempMin = 0;
			int tempMinIndex = tempPosition;
			for (; tempMin < rowNumber - tempPivot - 1; tempMin++) {
				int maxAbsPosition = Nd4j.getExecutioner().execAndReturn(new IAMax(part.getRow(tempMin))).getFinalResult().intValue();
				if (Math.abs(part.getDouble(tempMin, maxAbsPosition)) > this.tolerance) {
					tempMinIndex = maxAbsPosition + tempPosition + 1;
					break;
				}

			}
//			for (int i = tempPosition + 1; i < columnNumber; i++) {
//				int tempZeros = 0;
//				for (int j = tempPivot; j < rowNumber; j++) {
//					if (A1.getDouble(j, i) == 0) {
//						tempZeros++;
//					} else {
//						break;
//					}
//				}
//				if (tempZeros < tempMin) {
//					tempMin = tempZeros;
//					tempMinIndex = i;
//				}
//			}	
			if (tempMinIndex != tempPosition + 1) {
				var temp = A1.getColumn(tempPosition + 1).dup();
				A1.putColumn(tempPosition + 1, A1.getColumn(tempMinIndex));
				A1.putColumn(tempMinIndex, temp);
				this.close(temp);
			}
			tempPivot += tempMin + 1;
			tempPosition++;
			if (tempPivot == rowNumber) {
				break;
			}
			pivot.add(tempPivot);
			if (tempPosition == columnNumber - 1) {
				break;
			}
			
			var coefficient = A1.getRow(tempPivot).div(A1.getDouble(tempPivot, tempPosition)).reshape(1, columnNumber);
			coefficient.putScalar(tempPosition, 0);
			var minuent = A1.getColumn(tempPosition).reshape(rowNumber, 1).mmul(coefficient);
			A1.subi(minuent);
			this.close(minuent); this.close(coefficient);					
		}
		this.close(A1);
		return pivot;
	}
	
	/**
	 * Testing only
	 * 
	 * @param pivotList
	 * @param A
	 * @param b
	 * @return
	 */
	public int GaussianTransformation(List<Integer> pivotList,@Nonnull INDArray A, INDArray b) {
		int rowNumber = A.rows();
		int columnNumber = A.columns();
		INDArray A1 = A, b1;
		if (b == null) {
			b1 = Nd4j.zeros(rowNumber, 1);
		} else {
			if (b.rows() != rowNumber) {
				throw new IllegalArgumentException();
			}
			b1 = b;
		}
		int tempPosition = -1;
		int tempPivot = 0;
		int returnValue = 0;
		while (true) {
			if (tempPosition == rowNumber - 1) {
				returnValue = rowNumber;
				break;
			}
			int tempMin = Integer.MAX_VALUE;
			int tempMinIndex = tempPosition;
			for (int i = tempPosition + 1; i < rowNumber; i++) {
				int tempZeros = 0;
				for (int j = tempPivot; j < columnNumber; j++) {
					if (A1.getDouble(i, j) == 0) {
						tempZeros++;
					} else {
						break;
					}
				}
				if (tempZeros < tempMin) {
					tempMin = tempZeros;
					tempMinIndex = i;
				}
			}	
			var temp = A1.getRow(tempPosition + 1).dup();
			A1.putRow(tempPosition + 1, A1.getRow(tempMinIndex));
			A1.putRow(tempMinIndex, temp);
			this.close(temp);
			temp = b1.getScalar(tempPosition + 1, 0).dup();
			b1.putRow(tempPosition + 1, b1.getRow(tempMinIndex));
			b1.putRow(tempMinIndex, temp);
			this.close(temp);
			tempPivot += tempMin;
			tempPosition++;
			if (tempPivot == columnNumber) {
				for (int i = tempPosition; i < rowNumber; i++) {
					if (b1.getDouble(i, 0) != 0) {
						returnValue = -1;
						break;
					}
				}
				returnValue = tempPosition;
				break;
			}
			if (pivotList != null) {
				pivotList.add(tempPivot);
			}
			
			var pivotElement = A1.getScalar(tempPosition, tempPivot);
			b1.getScalar(tempPosition, 0).divi(pivotElement);
			A1.getRow(tempPosition).divi(pivotElement);
			var coefficient = A1.getColumn(tempPivot).dup().reshape(rowNumber, 1);
			coefficient.putScalar(tempPosition, 0, 0);
			var minuent = coefficient.mmul(A1.getRow(tempPosition).reshape(1, columnNumber));
			A1.subi(minuent);
			coefficient.muli(b1.getScalar(tempPosition, 0));
			b1.subi(coefficient);
			this.close(minuent); this.close(coefficient);					
		}
		if (b == null) {
			this.close(b1);
		}
		return returnValue;
	}
}
