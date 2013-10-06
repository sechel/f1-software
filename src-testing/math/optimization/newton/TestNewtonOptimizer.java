package math.optimization.newton;

import java.util.Random;

import junit.framework.TestCase;
import math.optimization.NotConvergentException;
import math.optimization.Optimizable;
import math.optimization.Optimizer;
import math.optimization.stepcontrol.ArmijoStepController;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.Vector;
import no.uib.cipr.matrix.Vector.Norm;

public class TestNewtonOptimizer extends TestCase {

		class Example1 implements Optimizable {
			private double eps;
			private int dim;
			private Vector c;
			Example1(double eps, int dim, Vector c) {
				this.eps = eps;
				this.dim = dim;
				this.c = c; 
			}
			
			private double theSqareRoot(Vector x) {
				final double xnorm = x.norm(Norm.Two);
				return Math.sqrt(xnorm * xnorm + eps);
			}
			
			@Override
			public Double evaluate(Vector x, Vector gradient, Matrix hessian) {
				evaluate(x, hessian);
				return evaluate(x, gradient);
			}

			@Override
			public Double evaluate(Vector x, Vector gradient) {
				gradient.set(1 / theSqareRoot(x), x).add(c);
				return evaluate(x);
			}

			@Override
			public Double evaluate(Vector x, Matrix hessian) {
				double tsq = theSqareRoot(x);
				double tsqcube = tsq * tsq * tsq;
				for (int i = 0; i < dim; i++) {
					for (int j = 0; j <= i; j++) {
						double hij = - x.get(i) * x.get(j)/tsqcube;
						hessian.set(i, j, hij);
						hessian.set(j, i, hij);
						hessian.add(i, j, i == j ? 1 / tsq : 0.0);
					}
				}
				return evaluate(x);
			}

			@Override
			public Double evaluate(Vector x) {
				return theSqareRoot(x) + c.dot(x);
			}

			@Override
			public Integer getDomainDimension() {
				return dim;
			}			
		}
	
		public void testExample1() throws NotConvergentException {
			double eps = 0.1;
			int dim = 10;
			Random rnd = new Random();
			rnd.setSeed(2451523562523523452L);
			Vector c = new DenseVector(dim);
			Vector x = new DenseVector(dim);
			Vector g = new DenseVector(dim);
			Optimizable f = new Example1(eps, dim, c);
			Optimizer opt = new NewtonOptimizer();
			opt.setError(1.0E-7);
			for (int i = 0; i < dim; i++) {
				x.set(i, 10 * (rnd.nextDouble() - 0.5));
				c.set(i, rnd.nextDouble() );
			}
			c.scale(0.9 / c.norm(Norm.Two));
			opt.setStepController(new ArmijoStepController());
			opt.minimize(x, f);
			f.evaluate(x, g);
			assertEquals(0.0, g.norm(opt.getNorm()), opt.getError());
		}
		
}
