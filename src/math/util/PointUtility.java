package math.util;

import java.util.Arrays;

import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;

public class PointUtility {

	
	/**
	 * TODO: Bugs Bugs Bugs!!!!
	 * @param T
	 * @param p
	 * @return
	 */
	public static double[] projectToAffine(Matrix T, double[] p){
		double[] result = new double[]{p[0], p[1], p[2], 1.0};
		Matrix Tinv = T.getTranspose();
		Tinv.setEntry(0, 3, -Tinv.getEntry(3, 0));
		Tinv.setEntry(1, 3, -Tinv.getEntry(3, 1));
		Tinv.setEntry(2, 3, -Tinv.getEntry(3, 2));
		Tinv.setEntry(3, 0, 0.0);
		Tinv.setEntry(3, 1, 0.0);
		Tinv.setEntry(3, 2, 0.0);
		
		result = Tinv.multiplyVector(result);
		result[2] = 0.0;
		result = T.multiplyVector(result);
		return result;
	}

	
	public static void main(String[] args) {
		Matrix subSpace = MatrixBuilder.euclidean().translate(-1, 0, 0).rotate(Math.PI/2, 0, 1, 0).getMatrix();
		double[] point = {1.0, 1.0, 1.0};
		double[] projection = projectToAffine(subSpace, point);
		System.err.println("Subspace: \n" + subSpace);
		System.err.println("Point: " + Arrays.toString(point));
		System.err.println("Projection: " + Arrays.toString(projection));
	}
	
	
}
