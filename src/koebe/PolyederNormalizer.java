package koebe;

import static java.lang.Math.log;
import static java.lang.Math.sqrt;
import halfedge.Edge;
import halfedge.Face;
import halfedge.HalfEdgeDataStructure;
import halfedge.Vertex;
import halfedge.decorations.HasXYZW;

import java.util.List;

import javax.vecmath.Point4d;

import koebe.KoebePolyhedron.KoebePolyhedronContext;
import math.optimization.NotConvergentException;
import math.optimization.Optimizable;
import math.optimization.Optimizer;
import math.optimization.newton.NewtonOptimizer;
import math.optimization.stepcontrol.ArmijoStepController;
import math.util.VecmathTools;
import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.Vector;


/**
 * Normalizes a given Polyhedron through lorenz transformation 
 * such that the barry center of vertices is at zero. 
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 */
public abstract class PolyederNormalizer {

	
	public static <
		V extends Vertex<V, E, F> & HasXYZW,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F> & HasXYZW
	> void normalize(KoebePolyhedronContext<V, E, F> context) throws NotConvergentException, Exception{
		PolyederOptimizable<V, E, F> opt = new PolyederOptimizable<V, E, F>(context);
		Optimizer o = new NewtonOptimizer();
		ArmijoStepController stepController = new ArmijoStepController();
		o.setStepController(stepController);
		
		o.setMaxIterations(20);
		o.setError(1E-4);
		Vector result = new DenseVector(opt.getDomainDimension());
		o.minimize(result, opt);
		if (lengthEuclid(result) == Double.NaN)
			throw new NotConvergentException("normalization did not succeed in PolyederNormalizer: NaN", -1.0);
		if (lengthEuclid(result) >= 1)
			throw new NotConvergentException("normalization did not succeed in PolyederNormalizer: |center| >= 1", -1.0);
		int_normalize(result, context.polyeder, context.medial);
	}

	
	private static double lengthEuclid(Vector v){
		double result = 0.0;
		for (int i = 0; i < v.size(); i++)
			result += v.get(i)*v.get(i);
		return Math.sqrt(result);
	}
	

	private static <
		V extends Vertex<V, E, F> & HasXYZW,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F> & HasXYZW
	> void int_normalize(Vector center, HalfEdgeDataStructure<V, E, F> polyhedron, HalfEdgeDataStructure<V, E, F> medial){
		Vector e1 = new DenseVector(new double[]{1,0,0,0});
		Vector e2 = new DenseVector(new double[]{0,1,0,0});
		Vector e3 = new DenseVector(new double[]{0,0,1,0});
		
		Vector a4 = new DenseVector(new double[]{-center.get(0), -center.get(1), -center.get(2), -1});
		a4.scale(1 / length2(a4));
		Vector a3 = new DenseVector(e3); 
		a3.add(ldot(e3, a4), a4).scale(1 / length(a3));
		Vector a2 = new DenseVector(e2);
		a2.add(ldot(e2, a4), a4).add(-ldot(e2, a3), a3).scale(1 / length(a2));
		Vector a1 = new DenseVector(e1); 
		a1.add(ldot(e1, a4), a4).add(-ldot(e1, a3), a3).add(-ldot(e1, a2), a2).scale(1 / length(a1));
		
		Matrix At = new DenseMatrix(new Vector[]{a1, a2, a3, a4}).transpose();
		Matrix I_l = new DenseMatrix(4,4);
		I_l.set(0,0,1);I_l.set(1,1,1);I_l.set(2,2,1);I_l.set(3,3,-1);
		Matrix A_inv = At.mult(I_l, new DenseMatrix(4,4));
		
		Vector test = new DenseVector(a4);
		test = A_inv.mult(test, new DenseVector(4));
		
		// transform polyhedron
		for (V v : polyhedron.getVertices()){
			Point4d p = v.getXYZW();
			Vector v1 = new DenseVector(new double[]{p.x, p.y, p.z, p.w});
			Vector newV = A_inv.mult(v1, new DenseVector(4));
			p.x = newV.get(0) / newV.get(3);
			p.y = newV.get(1) / newV.get(3);
			p.z = newV.get(2) / newV.get(3);
			p.w = 1;
			v.setXYZW(p);
		}

		for (F f : polyhedron.getFaces()){
			Point4d p = f.getXYZW();
			Vector v1 = new DenseVector(new double[]{p.x, p.y, p.z, p.w});
			Vector newV = A_inv.mult(v1, new DenseVector(4));
			p.x = newV.get(0) / newV.get(3);
			p.y = newV.get(1) / newV.get(3);
			p.z = newV.get(2) / newV.get(3);
			p.w = 1;
			f.setXYZW(p);
		}
		
		// transform medial
		for (V v : medial.getVertices()){
			Point4d p = v.getXYZW();
			VecmathTools.dehomogenize(p);
			Vector v1 = new DenseVector(new double[]{p.x, p.y, p.z, p.w});
			Vector newV = A_inv.mult(v1, new DenseVector(4));
			v.getXYZW().set(newV.get(0) / newV.get(3), newV.get(1) / newV.get(3), newV.get(2) / newV.get(3), 1);
		}
		for (F f : medial.getFaces()){
			Point4d p = f.getXYZW();
			VecmathTools.dehomogenize(p);
			Vector v1 = new DenseVector(new double[]{p.x, p.y, p.z, p.w});
			Vector newV = A_inv.mult(v1, new DenseVector(4));
			f.getXYZW().set(newV.get(0) / newV.get(3), newV.get(1) / newV.get(3), newV.get(2) / newV.get(3), 1);
		}
	}
	
	
	private static double length2(Vector x){
		return Math.sqrt(x.get(3)*x.get(3) - x.get(0)*x.get(0) - x.get(1)*x.get(1) - x.get(2)*x.get(2));
	}
	
	private static double length(Vector x){
		return Math.sqrt(-x.get(3)*x.get(3) + x.get(0)*x.get(0) + x.get(1)*x.get(1) + x.get(2)*x.get(2));
	}
	
	private static double ldot(Vector x, Vector y){
		return x.get(0)*y.get(0) + x.get(1)*y.get(1) + x.get(2)*y.get(2) - x.get(3)*y.get(3);
	}
	
	
	
	
	protected static class PolyederOptimizable  <
		V extends Vertex<V, E, F> & HasXYZW,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F> 
	> implements Optimizable{
		
		private KoebePolyhedronContext<V, E, F> 
			context = null;
		
		public PolyederOptimizable(KoebePolyhedronContext<V, E, F> context){
			this.context = context;
		}
		
		@Override
		public Double evaluate(Vector x, Vector gradient, Matrix hessian) {
			makeGradient(x, gradient);
			makeHessian(x, hessian);
			return evaluate(x);
		}

		@Override
		public Double evaluate(Vector x, Vector gradient) {
			makeGradient(x, gradient);
			return evaluate(x);
		}

		@Override
		public Double evaluate(Vector x, Matrix hessian) {
			makeHessian(x, hessian);
			return evaluate(x);
		}

		@Override
		public Double evaluate(Vector x) {
			double result = 0;
			double l = myLength(x);
			for (V v : context.getMedial().getVertices())
				result += log( dot(v.getXYZW(), x) / sqrt(l) );
			return result;
		}

		
		private double dot(Point4d p, Vector x){
			return -x.get(0)*p.x - x.get(1)*p.y - x.get(2)*p.z + p.w;
		}
		
		
		private double myLength(Vector x){
			return 1 - x.get(0)*x.get(0) - x.get(1)*x.get(1) - x.get(2)*x.get(2);
		}
		
		private void makeGradient(Vector x, Vector g){
			g.zero();
			List<V> vList = context.getMedial().getVertices(); 
			for (int i = 0; i < 3; i++){ // x
				for (V v : vList){
					double pi = 0;
					if (i == 0) pi = v.getXYZW().x;
					if (i == 1) pi = v.getXYZW().y;
					if (i == 2) pi = v.getXYZW().z;
					double xi = x.get(i);
					
					double dot = dot(v.getXYZW(), x);
					double l = myLength(x);
					
					g.add(i, (-pi/dot + xi/l));
				}
			}
		}
		
		
		private void makeHessian(Vector x, Matrix hess){
			hess.zero();
			List<V> vList = context.getMedial().getVertices(); 
			for (int i = 0; i < 3; i++){
				for (int j = 0; j < 3; j++){
					for (V v : vList){
						double xi = x.get(i);
						double xj = x.get(j);
						double pi = 0;
						if (i == 0) pi = v.getXYZW().x;
						if (i == 1) pi = v.getXYZW().y;
						if (i == 2) pi = v.getXYZW().z;
						double pj = 0;
						if (j == 0) pj = v.getXYZW().x;
						if (j == 1) pj = v.getXYZW().y;
						if (j == 2) pj = v.getXYZW().z;
						
						double d = dot(v.getXYZW(), x);
						double l = myLength(x);
						double diag = i == j ? 1 : 0;
						hess.add(i, j, diag/l + 2*xi*xj/(l*l) - pi*pj/(d*d));
					}
				}	
			}				
		}
		
		
		@Override
		public Integer getDomainDimension() {
			return 3;
		}
		
		
	}
	
	
	
	
}
