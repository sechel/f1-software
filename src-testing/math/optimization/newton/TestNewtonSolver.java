package math.optimization.newton;

import junit.framework.TestCase;
import math.optimization.Linearizable;
import math.optimization.Solver;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.Vector;

public class TestNewtonSolver extends TestCase {

	public static final Double
		eps = 1E-5,
		error = 1E-3;
	
	public void testNewtonSolver() throws Exception {
		TestLinearizable testFunc = new TestLinearizable();
		Solver solver = new NewtonSolver();
		
		Vector xGuess = new DenseVector(1);
		xGuess.set(0, 1);
		Vector b = new DenseVector(1);
		b.set(0, 8);
		
		solver.solve(testFunc, xGuess, b);
		System.err.println(xGuess);
	}
	
	
	
	private class TestLinearizable implements Linearizable{

		
		private double fun(double x){
			return x*x*x;
		}
		
		private double funDeriv(double x){
			return 3*x*x;
		}
		
		
		public void evaluate(Vector x, Vector fx, Vector offset) {
			double xVal = x.get(0);
			fx.set(0, fun(xVal) - offset.get(0));
		}

		public void evaluate(Vector x, Vector fx, Vector offset, Matrix jacobian) {
			evaluate(x, fx, offset);
			jacobian.set(0, 0, funDeriv(x.get(0)));
		}

		public void evaluate(Vector x, Matrix jacobian) {
			jacobian.set(0, 0, funDeriv(x.get(0)));
		}

		public Integer getDomainDimension() {
			return 1;
		}

		public Integer getCoDomainDimension() {
			return 1;
		}
		
		
	}
	
	
	
	
}
