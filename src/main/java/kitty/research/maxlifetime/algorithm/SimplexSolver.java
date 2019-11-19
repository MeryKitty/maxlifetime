package kitty.research.maxlifetime.algorithm;

import java.util.ArrayList;
import java.util.List;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.api.ops.impl.indexaccum.IMax;
import org.nd4j.linalg.api.ops.impl.indexaccum.IMin;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.NDArrayIndex;

/**
 * The class used to conduct the simplex algorithm, not used since float
 * accuracy is not enough for the simplex algorithm
 * 
 * @author MeryKitty
 *
 */
public class SimplexSolver {
	private final float tolerance;
	
	private static final float DEFAULT_TOLERANCE = (float) 1E-7;
	
	public SimplexSolver(float tolerance) {
		this.tolerance = tolerance;
	}
	
	public SimplexSolver() {
		this(DEFAULT_TOLERANCE);
	}
	
	public float compute(INDArray c, INDArray arg) {
		if (arg.columns() != c.columns()) {
			throw new AssertionError();
		}
		return c.mmul(arg.transpose()).getFloat(0, 0);
	}
	
	public float execute(INDArray A, INDArray b, INDArray c) {
		var indeList = this.independentRowList(A);
		int[] handle = new int[indeList.size()];
		for (int i = 0; i < handle.length; i++) {
			handle[i] = indeList.get(i);
		}
		A = A.getRows(handle);
		b = b.getRows(handle);
		int columnNumber = A.columns();
		int rowNumber = A.rows();
		if (columnNumber != c.columns() || rowNumber != b.rows()) {
			throw new AssertionError();
		}
		var startInfo = this.getVertex(A, b);
		var result = this.execute1(startInfo, c);
		startInfo.close();
		this.close(A); this.close(b);
		return result;
	}
	
	private SimplexInformation getVertex(INDArray A, INDArray b) {
		int rowNumber = A.rows();
		int columnNumber = A.columns();
		if (rowNumber != b.rows()) {
			throw new AssertionError();
		}
		INDArray eye = Nd4j.eye(rowNumber);
		A = Nd4j.hstack(A, eye);
		var x = b.dup();
		int[] B = new int[rowNumber];
		for (int i = 0; i < rowNumber; i++) {
			B[i] = columnNumber + i;
		}
		var c = Nd4j.zeros(rowNumber + columnNumber);
		for (int i = 0; i < rowNumber; i++) {
			c.putScalar(columnNumber + i, 1);
		}
		float phase1 = this.execute1(new SimplexInformation(A, B, x), c);
		System.out.println(phase1);
		for (int i = 0; i < rowNumber; i++) {
			if (B[i] >= columnNumber) {
				if (x.getFloat(i) >= this.tolerance) {
					throw new UnsupportedOperationException("The valid region is empty!");
				} else {
					// TODO Handle later
					for (int j = 0; j < columnNumber; j++) {
						float temp = A.getFloat(i, j);
						if (temp >= this.tolerance || temp <= -this.tolerance) {
							var rotateRow = A.getRow(i).dup().reshape(1, columnNumber + rowNumber);
							var rotateColumn = A.getColumn(j).dup().reshape(rowNumber, 1);
							phase1 = this.rotate(A, B, x, phase1, null, i, j, rotateRow, rotateColumn);
							break;
						}
					}
				}
				System.out.println(phase1);
			}
		}
		var temp = A.get(NDArrayIndex.all(), NDArrayIndex.interval(0, columnNumber)).dup();
		this.close(A);
		A = temp;
		this.close(c);
		return new SimplexInformation(A, B, x);
	}	
	
	public float execute1(SimplexInformation info, INDArray c) {
		var A = info.A();
		var B = info.B();
		var x = info.x();
		int rowNumber = A.rows();
		int columnNumber = A.columns();
		if (A.columns() != c.columns() || A.rows() != rowNumber || A.rows() != x.rows()) {
			throw new AssertionError();
		}
		
		var cB = Nd4j.zeros(B.length, 1, 'c');
		for (int i = 0; i < rowNumber; i++) {
			cB.getScalar(i, 0).assign(c.getFloat(B[i], 1));
		}
		var temp = A.mulColumnVector(cB);
		var delta = temp.sum(true, 0).subi(c);
		this.close(temp);
		temp = x.mul(cB);
		float f = (float) temp.sumNumber().floatValue();
		this.close(temp); this.close(cB);
		
		while (true) {
			// Get rotate column
			int rotateColumnIndex = Nd4j.getExecutioner().execAndReturn(new IMax(delta)).getFinalResult().intValue();
			this.close(temp);
			if (delta.getFloat(rotateColumnIndex) < this.tolerance) {
				return f;
			}
			var rotateColumn = A.getColumn(rotateColumnIndex).dup().reshape(rowNumber, 1);
			var theta = Nd4j.zeros(rowNumber, 1);
			for (int row = 0; row < rowNumber; row++) {
				if (rotateColumn.getFloat(row, 1) < this.tolerance) {
					theta.getScalar(row, 0).assign(Float.POSITIVE_INFINITY);
				} else {
					theta.getScalar(row, 0).assign(x.getFloat(row, 1) / rotateColumn.getFloat(row, 1));
				}				
			}
			int rotateRowIndex = Nd4j.getExecutioner().execAndReturn(new IMin(theta)).getFinalResult().intValue();
			if (theta.getFloat(rotateRowIndex, 0) == Float.POSITIVE_INFINITY) {
				return Float.NEGATIVE_INFINITY;
			}
			// Get rotate row and update A
			var rotateRow = A.getRow(rotateRowIndex).dup().reshape(1, columnNumber);
			f = this.rotate(A, B, x, f, delta, rotateRowIndex, rotateColumnIndex, rotateRow, rotateColumn);
			this.close(rotateRow); this.close(rotateColumn); this.close(theta);
		}
	}
	
	private final float rotate(INDArray A, int[] B, INDArray x, float f, INDArray delta, int rotateRowIndex, int rotateColumnIndex, INDArray rotateRow, INDArray rotateColumn) {
		// Get main element of the rotation
		float mainEle = A.getFloat(rotateRowIndex, rotateColumnIndex);
		rotateRow.divi(mainEle);
		var updateA = rotateColumn.mmul(rotateRow);
		A.subi(updateA);
		A.putRow(rotateRowIndex, rotateRow);
		this.close(updateA);
		// Update x
		float xPivot = x.getFloat(rotateRowIndex, 0) / mainEle;
		x.subi(rotateColumn.muli(xPivot));
		x.putScalar(rotateRowIndex, xPivot);
		// Update B
		B[rotateRowIndex] = rotateColumnIndex;
		// Update target function
		// Update delta
		if (delta != null) {
			float deltaPivot = delta.getFloat(rotateColumnIndex);
			delta.subi(rotateRow.muli(deltaPivot));
			f -= xPivot * deltaPivot;
		}
		return f;
	}
	
	private final boolean close(INDArray a) {
		if (!a.closeable()) {
			return false;
		} else if (!a.wasClosed()) {
			a.close();
		}
		return true;
	}
	
	public final List<Integer> independentRowList(INDArray A) {
		int rowNumber = A.rows();
		int columnNumber = A.columns();
		// Echelon transformation
		var A1 = A.dup();
		var pivot = new ArrayList<Integer>();
		var tempPosition = 0;
		var tempPivot = 0;
		pivot.add(0);
		while (true) {
			for (int i = tempPosition + 1; i < columnNumber; i++) {
				if (A1.getFloat(tempPivot, i) != 0) {
					var coefficient = A1.getFloat(tempPivot, tempPosition) / A1.getFloat(tempPivot, i);
					A1.getColumn(i).muli(coefficient).subi(A1.getColumn(tempPosition));
				}
			}
			var tempMin = Integer.MAX_VALUE;
			var tempMinIndex = tempPosition;
			for (int i = tempPosition + 1; i < columnNumber; i++) {
				var tempZeros = 0;
				for (int j = tempPivot; j < rowNumber; j++) {
					if (A1.getFloat(j, i) == 0) {
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
			var temp = A1.getColumn(tempPosition + 1).dup();
			A1.putColumn(tempPosition + 1, A1.getColumn(tempMinIndex));
			A1.putColumn(tempMinIndex, temp);
			this.close(temp);
			tempPivot += tempMin;
			tempPosition++;
			if (tempPivot == rowNumber || tempPosition == columnNumber - 1) {
				break;
			}
			pivot.add(tempPivot);
		}
		this.close(A1);
		return pivot;
	}
}
