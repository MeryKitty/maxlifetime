package kitty.research.maxlifetime;

import org.nd4j.linalg.api.buffer.DataType;
import org.nd4j.linalg.api.ops.impl.indexaccum.IMax;
import org.nd4j.linalg.api.ops.impl.indexaccum.IMin;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.NDArrayIndex;
import org.nd4j.linalg.ops.transforms.Transforms;

import kitty.research.maxlifetime.algorithm.SimplexSolver;
import kitty.research.maxlifetime.algorithm.SimplexSolverDouble;

@SuppressWarnings("unused")
public class Test {
	public static void main(String[] args) {
		var spl = new SimplexSolverDouble();
		Nd4j.setDefaultDataTypes(DataType.DOUBLE, DataType.DOUBLE);
//		int rowNumber = 10;
//		int columnNumber = 1000;
//		var A = Nd4j.rand(rowNumber, columnNumber).muli(2);
//		var x = Nd4j.rand(rowNumber, 1).muli(columnNumber / 2);
//		var c = Nd4j.rand(columnNumber).muli(-1);
		double[][] AData = {{2, 1, 1, 0, 0},
				{2, 3, 0, 1, 0},
				{3, 1, 0, 0, 1}};
//		double[][] bData = {{18}, {42}, {24}};
//		double[][] AData = {{7, 0, 0, -1, 3},
//				{0, 7, 0, 3, -2},
//				{0, 0, 7, -1, -4}};
//		double[][] bData = {{30}, {78}, {-12}};
		double[] bData = {3, 4, 2, -1, 0};
		double[] cData = {3, 2, 0, 0, 0};
		var A = Nd4j.create(AData);
		var b = Nd4j.create(bData);
		var c = Nd4j.create(cData);
		c.muli(-1);
		long start = System.currentTimeMillis();
//		int min = Nd4j.getExecutioner().execAndReturn(new IMax(c)).getFinalResult().intValue();
//		System.out.println(min);
//		var result = Transforms.greaterThanOrEqual(b, c, true);
//		result.muli(2).subi(1);
		var A1 = A.getRow(1);
		var A2 = A1.reshape(5, 1);
		System.out.println(A2.isView());
		long end = System.currentTimeMillis();
//		System.out.println(end - start);
	}
}
