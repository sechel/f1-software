package math.util;

import java.util.LinkedList;
import java.util.List;

import javax.vecmath.Point2d;
import javax.vecmath.Point4d;
import javax.vecmath.Vector3d;

import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.MatrixSingularException;
import no.uib.cipr.matrix.Vector;
import no.uib.cipr.matrix.Vector.Norm;
import no.uib.cipr.matrix.sparse.CGS;
import no.uib.cipr.matrix.sparse.CompRowMatrix;
import no.uib.cipr.matrix.sparse.IterativeSolverNotConvergedException;
import util.debug.DBGTracer;
import de.jreality.math.MatrixBuilder;


/**
 * Circles in space utility. Find eavenly spaces points on a circle
 * given three initial points. Find the tranformation needed to
 * transform a unit circle into a circle through the given three points.
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 */
public class Circles {

	
	/**
	 * Construct vertices on a circle through three points in space
	 * @param res the number of points on the circle
	 * @return a list of points on the circle through the given three points, eavenly spaced and sorted
	 */
	public static List<Point4d> getCircle(Point4d p1, Point4d p2, Point4d p3, int res, Point4d center){
		LinkedList<Point4d> result = new LinkedList<Point4d>();
		Vector vp3 = new DenseVector(new double[]{p1.x / p1.w, p1.y / p1.w, p1.z / p1.w});
		Vector vp2 = new DenseVector(new double[]{p2.x / p2.w, p2.y / p2.w, p2.z / p2.w});
		Vector vp1 = new DenseVector(new double[]{p3.x / p3.w, p3.y / p3.w, p3.z / p3.w});
		
		// finding the center
		Vector v1 = vp1.copy();
		v1.add(-1, vp2).scale(1 / v1.norm(Norm.Two));
		Vector v2 = vp3.copy();
		v2.add(-1, vp2).scale(1 / v2.norm(Norm.Two));
		
		Vector m1 = vp2.copy();
		m1.add(vp1).scale(0.5);
		Vector m2 = vp2.copy();
		m2.add(vp3).scale(0.5);
		
		Vector tmp = v1.copy();
		tmp.scale(VecmathTools.dot(v2, v1));
		Vector V1 = v2.copy();
		V1.add(-1, tmp);

		tmp = v2.copy();
		tmp.scale(VecmathTools.dot(v1, v2));
		Vector V2 = v1.copy();
		V2.scale(-1).add(tmp);
		
		Matrix V = new DenseMatrix(new Vector[]{V1, V2, new DenseVector(3)});
		Vector m = m1.copy();
		m.scale(-1).add(m2);
		
		Vector template = new DenseVector(3);
		CGS solver = new CGS(template);
//		solver.setPreconditioner(new ILU(V));
		
		Vector mulabda = new DenseVector(3);
		try {
			solver.solve(V, m, mulabda);
		} catch (IterativeSolverNotConvergedException e) {
			DBGTracer.msg("Error calculating circle through:");
			DBGTracer.msg("p1: " + p1);
			DBGTracer.msg("p2: " + p2);
			DBGTracer.msg("p3: " + p3);
			DBGTracer.msg("defaulting to zero.");
			for (int i = 0; i < res; i++)
				result.add(new Point4d());
			return result;
		}
		
		double lambda = mulabda.get(0);
		Vector c = m1.copy();
		c.add(lambda, V1);
		
		// the radius
		Vector radius = c.copy();
		radius.add(-1, vp2);
		double r = radius.norm(Norm.Two);
		
		// finding the orientation
		v1 = vp1.copy();
		v1.add(-1, vp2);
		v2 = vp3.copy();
		v2.add(-1, vp2);
		Vector v3 = VecmathTools.cross(v1, v2);
		
		Vector j1 = v1.copy();
		j1.scale(1 / j1.norm(Norm.Two));
		Vector j2 = v2.copy();
		j2.add(-VecmathTools.dot(v2, j1), j1).scale(1 / j2.norm(Norm.Two));
		Vector j3 = v3.copy();
		j3.add(-VecmathTools.dot(v3, j2), j2).add(-VecmathTools.dot(v3, j1), j1).scale(1 / j3.norm(Norm.Two));
		
		Matrix J = new DenseMatrix(new Vector[]{j1, j2, j3});
		
		Vector v = new DenseVector(3);
		Vector p = new DenseVector(3);
		for (int i = 0; i < res; i++){
			v.set(0, Math.cos(Math.PI * 2 * i / res));
			v.set(1, Math.sin(Math.PI * 2 * i / res));
			v.set(2, 0);
			v.scale(r);
			p.zero();
			J.mult(v, p);
			p.add(c);
			result.add(new Point4d(p.get(0), p.get(1), p.get(2), 1));
		}
		center.set(c.get(0), c.get(1), c.get(2), 1);
		return result;
	}
	

	/**
	 * Return radius and center of the circle through three points in the plane
	 * @param p1
	 * @param p2
	 * @param p3
	 * @param centerOUT
	 * @return
	 */
	public static double getCircleCenterAndRadius(Point2d p1, Point2d p2, Point2d p3, Point2d centerOUT) throws MatrixSingularException{
		Matrix M = new DenseMatrix(3,3);
		M.set(0, 0, 1);
		M.set(0, 1, -p1.x);
		M.set(0, 2, -p1.y);
		M.set(1, 0, 1);
		M.set(1, 1, -p2.x);
		M.set(1, 2, -p2.y);
		M.set(2, 0, 1);
		M.set(2, 1, -p3.x);
		M.set(2, 2, -p3.y);
		Vector b = new DenseVector(3);
		b.set(0, -(p1.x*p1.x + p1.y*p1.y));
		b.set(1, -(p2.x*p2.x + p2.y*p2.y));
		b.set(2, -(p3.x*p3.x + p3.y*p3.y));
		Vector x = new DenseVector(3);
		M.solve(b, x);
		centerOUT.x = x.get(1) / 2;
		centerOUT.y = x.get(2) / 2;
		return Math.sqrt(centerOUT.x*centerOUT.x + centerOUT.y*centerOUT.y - x.get(0));
	}
	
	
	
	
	/**
	 * Returns points on the circle when (center - point) perpendicular to normal 
	 * @param center the center of the circle
	 * @param normal the normal of the circle
	 * @param point a point on the circle
	 * @return list of points on the circle, point is not neccessarily contained
	 */
	public static List<Point4d> getCircle(Point4d center, Point4d normal, Point4d point, int res){
		LinkedList<Point4d> result = new LinkedList<Point4d>();
		Vector C = new DenseVector(new double[]{center.x / center.w, center.y / center.w, center.z / center.w});
		Vector N = new DenseVector(new double[]{normal.x / normal.w, normal.y / normal.w, normal.z / normal.w});
		Vector P = new DenseVector(new double[]{point.x / point.w, point.y / point.w, point.z / point.w});

		//find ONB
		Vector T = P.copy(); 
		T.add(-1, C);
		double r = T.norm(Norm.Two);
		Vector B = VecmathTools.cross(T, N);
		
		//normalize
		T.scale(1 / T.norm(Norm.Two));
		N.scale(1 / N.norm(Norm.Two));
		B.scale(1 / B.norm(Norm.Two));
		
		//construct matrix
		Matrix J = new DenseMatrix(new Vector[]{T, B, N});
		Vector v = new DenseVector(3);
		Vector p = new DenseVector(3);
		for (int i = 0; i < res; i++){
			v.set(0, Math.cos(Math.PI * 2 * i / res));
			v.set(1, Math.sin(Math.PI * 2 * i / res));
			v.set(2, 0);
			v.scale(r);
			p.zero();
			J.mult(v, p);
			p.add(C);
			result.add(new Point4d(p.get(0), p.get(1), p.get(2), 1));
		}
		
		return result;
	}
	
	
	
	public static de.jreality.math.Matrix getTransform(Point4d c, Vector3d N, Double r){
		//find ONB
		VecmathTools.dehomogenize(c);

		Vector3d T = null;
		if (N.x == 0.0)
			T = new Vector3d(1, 0, 0);
		else
			T = new Vector3d(0, 1, 0);
		T.cross(T, N);
		Vector3d B = new Vector3d();
		B.cross(T, N);
		
		//normalize
		T.scale(1 / T.length());
		B.scale(1 / B.length());		
		N.scale(1 / N.length());
		
		de.jreality.math.Matrix Offset = MatrixBuilder.euclidean().translate(c.x, c.y, c.z).getMatrix();
		de.jreality.math.Matrix S = MatrixBuilder.euclidean().scale(r, r, 1.0).getMatrix();
		de.jreality.math.Matrix R = new de.jreality.math.Matrix();
		R.setColumn(0, new double[]{T.x, T.y, T.z, 0.0});
		R.setColumn(1, new double[]{B.x, B.y, B.z, 0.0});
		R.setColumn(2, new double[]{N.x, N.y, N.z, 0.0});
		R.setColumn(3, new double[]{0.0, 0.0, 0.0, 1.0});
		
		S.multiplyOnLeft(R);
		S.multiplyOnLeft(Offset);
		
		return S;
	}
	
	
	
	public static Matrix getTransformation(Point4d p1, Point4d p2, Point4d p3){
		Vector vp3 = new DenseVector(new double[]{p1.x / p1.w, p1.y / p1.w, p1.z / p1.w});
		Vector vp2 = new DenseVector(new double[]{p2.x / p2.w, p2.y / p2.w, p2.z / p2.w});
		Vector vp1 = new DenseVector(new double[]{p3.x / p3.w, p3.y / p3.w, p3.z / p3.w});
		
		// finding the center
		Vector v1 = vp1.copy();
		v1.add(-1, vp2).scale(1 / v1.norm(Norm.Two));
		Vector v2 = vp3.copy();
		v2.add(-1, vp2).scale(1 / v2.norm(Norm.Two));
		
		Vector m1 = vp2.copy();
		m1.add(vp1).scale(0.5);
		Vector m2 = vp2.copy();
		m2.add(vp3).scale(0.5);
		
		Vector tmp = v1.copy();
		tmp.scale(VecmathTools.dot(v2, v1));
		Vector V1 = v2.copy();
		V1.add(-1, tmp);

		tmp = v2.copy();
		tmp.scale(VecmathTools.dot(v1, v2));
		Vector V2 = v1.copy();
		V2.scale(-1).add(tmp);
		
		CompRowMatrix V = new CompRowMatrix(new DenseMatrix(new Vector[]{V1, V2, new DenseVector(3)}));
		Vector m = m1.copy();
		m.scale(-1).add(m2);
		
		Vector template = new DenseVector(3);
		CGS solver = new CGS(template);
//		solver.setPreconditioner(new ILU(V));
		
		Vector mulabda = new DenseVector(3);
		try {
			solver.solve(V, m, mulabda);
		} catch (IterativeSolverNotConvergedException e) {
			e.printStackTrace();
		}
		
		double lambda = mulabda.get(0);
		Vector c = m1.copy();
		c.add(lambda, V1);
		
		// the radius
		Vector radius = c.copy();
		radius.add(-1, vp2);
		double r = radius.norm(Norm.Two);
		
		// finding the orientation
		v1 = vp1.copy();
		v1.add(-1, vp2);
		v2 = vp3.copy();
		v2.add(-1, vp2);
		Vector v3 = VecmathTools.cross(v1, v2);
		
		Vector j1 = v1.copy();
		j1.scale(1 / j1.norm(Norm.Two));
		Vector j2 = v2.copy();
		j2.add(-VecmathTools.dot(v2, j1), j1).scale(1 / j2.norm(Norm.Two));
		Vector j3 = v3.copy();
		j3.add(-VecmathTools.dot(v3, j2), j2).add(-VecmathTools.dot(v3, j1), j1).scale(1 / j3.norm(Norm.Two));
		
		
		Matrix scale = new DenseMatrix(4,4);
		scale.set(0,0,r);
		scale.set(1,1,r);
		scale.set(2,2,r);
		scale.set(3,3,1);
		
		Vector J1 = new DenseVector(new double[]{j1.get(0), j1.get(1), j1.get(2), 0});
		Vector J2 = new DenseVector(new double[]{j2.get(0), j2.get(1), j2.get(2), 0});
		Vector J3 = new DenseVector(new double[]{j3.get(0), j3.get(1), j3.get(2), 0});
		Vector J4 = new DenseVector(new double[]{c.get(0), c.get(1), c.get(2), 1});
		Matrix rotation = new DenseMatrix(new Vector[]{J1, J2, J3, J4});
		Matrix result = new DenseMatrix(4,4);
		rotation.mult(scale, result);
		return result;
	}
	
	
	
	public static void main(String[] args) {
		Point4d p1 = new Point4d(0.7071065108775612, -0.4999994089660654, 0.5000009733071994, 1.0);
		Point4d p2 = new Point4d(8.111443622271368E-7, -0.9999999999996171, 3.2879019420119847E-7, 1.0);
		Point4d p3 = new Point4d(0.7071070478434008, -0.4999993725114193, -0.5000002503783144, 1.0);
		Point4d center = new Point4d();
		List<Point4d> result = getCircle(p1, p2, p3, 20, center);
		DBGTracer.msg(result.toString());
		DBGTracer.msg("Center: " + center);
	}
	
	
	
}
