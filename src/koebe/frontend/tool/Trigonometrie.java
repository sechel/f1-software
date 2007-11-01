package koebe.frontend.tool;

import static de.jreality.math.Rn.crossProduct;
import static de.jreality.math.Rn.euclideanAngle;
import static de.jreality.math.Rn.euclideanDistance;
import static de.jreality.math.Rn.normalize;
import static de.jreality.math.Rn.subtract;
import static java.lang.Math.PI;
import static java.lang.Math.sin;
import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.Vector;
import de.jreality.math.Rn;

public class Trigonometrie {

	public static class CircumCircle {
		
		public double[]
		    center = null,
		    normal = null;
		public double
			radius = 0.0;
		     
	}

	/**
	 * Claculates the radius of the circumcircle of three points in 3-space
	 * @param triangle the three points
	 * @return the radius of the circumcircle
	 */
	public static double getCircumRadius(double[][] triangle)  throws IllegalArgumentException {
		double[] xy = subtract(null, triangle[1], triangle[0]);
		double[] xz = subtract(null, triangle[2], triangle[0]);
		double alpha = euclideanAngle(xy, xz);
		if (alpha < 1E-8 || alpha + 1E-8 > PI)
			throw new IllegalArgumentException("Collinear triangle vertices in getCircumCenterRadius()");
		double a = euclideanDistance(triangle[1], triangle[2]);
		return a / 2.0 / sin(alpha);
	}

	/**
	 * Calculates the circumcenter of three points in 3-space
	 * @param three points in 3-space
	 * @return center, this triangle's circumcenter
	 */
	public static double[] getCircumCenter(double[][] triangle) throws IllegalArgumentException{
		double[] xy = subtract(null, triangle[1], triangle[0]);
		double[] xz = subtract(null, triangle[2], triangle[0]);
		double[] n = crossProduct(null, xy, xz);
		double dxy = 0.5 * (Rn.euclideanNormSquared(triangle[1]) - Rn.euclideanNormSquared(triangle[0]));
		double dxz = 0.5 * (Rn.euclideanNormSquared(triangle[2]) - Rn.euclideanNormSquared(triangle[0]));
		double dn = Rn.innerProduct(n, triangle[2]);
		
		Vector E1 = new DenseVector(xy);
		Vector E2 = new DenseVector(xz);
		Vector E3 = new DenseVector(n);
		Vector d = new DenseVector(new double[] {dxy, dxz, dn});
		
		Matrix A = new DenseMatrix(new Vector[] {E1, E2, E3}).transpose();
		Vector x = new DenseVector(3);
		A.solve(d, x);
		return new double[] {x.get(0), x.get(1), x.get(2)};
	}

	
	/**
	 * Calculates the circum circle of a triangle in 3-space
	 * @param triangle
	 * @return circum circle
	 * @throws IllegalArgumentException
	 */
	public static CircumCircle getCircumCircle(double[][] triangle) throws IllegalArgumentException {
		CircumCircle result = new CircumCircle();
		result.center = getCircumCenter(triangle);
		result.radius = getCircumRadius(triangle);
		double[] xy = subtract(null, triangle[1], triangle[0]);
		double[] xz = subtract(null, triangle[2], triangle[0]);
		double[] n = crossProduct(null, xy, xz);
		normalize(n, n);
		result.normal = n;
		return result;
	}
	
	/**
	 * Calculates the center a circle in 3-space which is uniquely determined by two points
	 * <i>x</i> and <i>y</i> together with the tangent spanned by <i>x</i> and <i>dx</i>.
	 * 
	 * @param two points and a tangent defining offset
	 * @return center
	 */
	public static double[] getCircleCenterFromTwoPointsAndTangent(double[] x, double[] y, double[] dx) throws IllegalArgumentException{
		double[] xy = subtract(null, y, x);
		double[] n = crossProduct(null, dx, xy);
		double spannedArea = Rn.euclideanNorm(n);
		if (Math.abs(spannedArea) < 10E-8)
			throw new IllegalArgumentException("Collinear points in getCircleCenterFromTwoPointsAndTangent()");
		double c_dx = Rn.innerProduct(x, dx);
		double c_xy = 0.5 * (Rn.innerProduct(y,y) - Rn.innerProduct(x,x));
		double c_n  = Rn.innerProduct(x, n);
		
		Vector E1 = new DenseVector(dx);
		Vector E2 = new DenseVector(xy);
		Vector E3 = new DenseVector(n);
		Vector d = new DenseVector(new double[] {c_dx, c_xy, c_n});
		
		Matrix A = new DenseMatrix(new Vector[] {E1, E2, E3}).transpose();
		Vector solution = new DenseVector(3);
		A.solve(d, solution);
		return new double[] {solution.get(0), solution.get(1), solution.get(2)};
	}
	

}