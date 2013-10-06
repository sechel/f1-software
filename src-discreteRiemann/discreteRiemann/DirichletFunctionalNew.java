package discreteRiemann;

import halfedge.Edge;
import halfedge.Face;
import halfedge.Vertex;

import java.util.Arrays;
import java.util.List;

import de.jtem.numericalMethods.calculus.function.RealFunctionOfSeveralVariablesWithGradient;

public abstract class DirichletFunctionalNew <
	V extends Vertex<V, E, F>,
	E extends Edge<V, E, F> & HasRho,
	F extends Face<V, E, F>
	> 
	implements RealFunctionOfSeveralVariablesWithGradient {
		
		/**
		 * The graph
		 */
		DiscreteConformalStructure<V,E,F> G;
		
		/**
		 * The cut: on each oriented edge 0 no cut, +/- 1: a stretch
		 */
		final int [] eps;

		final double [] f;


		abstract public void setQuadCycle(List quadCycle);
		
		/**
		 * @param g
		 *            the graph
		 */
		
		DirichletFunctionalNew(DiscreteConformalStructure g, int [] eps, double [] f ) {
			G = g;
			this.eps = eps;
			this.f = f;
		}
		
		private double[] grad;
		
		@Override
		public double eval(double[] f) {
			if (grad == null)
				grad = new double[getNumberOfVariables()];
			return eval(f, grad);
		}
			
		void minimize() {
			Arrays.fill(f,0.5);
//			ConjugateGradient.search(f, 1e-15, this);
			ConjugateGradient.search(f, 1e-15, this, 10000, false, null);
//			double[] grad = new double[getNumberOfVariables()];
//			eval(f, grad);		
//			System.out.println("Gradient: "+VectorOperations.normSqr(grad));
		}
	
		static class OnGraph <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F> & HasRho,
		F extends Face<V, E, F>
		> extends DirichletFunctionalNew {
			
			OnGraph(DiscreteConformalStructure g) {
				super( g, new int[g.getNumEdges()], new double[ g.getNumVertices()] );
			}
			
			OnGraph(DiscreteConformalStructure g, int [] eps, double [] f ) {
				super( g, eps, f );
			}
			
			@Override
			public void setQuadCycle(List quadCycle) {
				CycleUtility.computeEpsOnGraph( quadCycle, eps );	
				
			}
		
			/*
			 * Evaluates the Dirichlet functional for f and saves the Laplacian of
			 * the function f.
			 * 
			 * @see de.jtem.numericalMethods.calculus.function.RealFunctionOfSeveralVariablesWithGradient#eval(double[],
			 *      double[])
			 */
			@Override
			public double eval(double[] f, double[] grad) {
				if ((f.length != getNumberOfVariables())
						|| (grad.length != getNumberOfVariables()))
					throw new IllegalArgumentException(
							"The arguments should be arrays of size "
							+ getNumberOfVariables() + " not " + f.length);
				double value = 0;
				Arrays.fill(grad, 0.);
				
				double d;
				
				for (int ie = 0; ie < G.getNumEdges(); ie++) {
					E e = (E) G.edgeList.get(ie);
					if( e.isPositive() ) {
						int x0 = e.getStartVertex().getIndex();
						int x1 = e.getTargetVertex().getIndex();
						
						d = e.getRho() * (f[x1] - f[x0] + eps[ie]);
						value += d * (f[x1] - f[x0] + eps[ie]);
						d *= 2;
						grad[x1] += d;
						grad[x0] -= d;
					}
				}
				grad[0] = 0; // Or else too much freedom.
				return value;
			}
			
			@Override
			public int getNumberOfVariables() {
				return G.getNumVertices();
			}

		}
		
		static class OnDual <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F> & HasRho,
		F extends Face<V, E, F>
		>  extends DirichletFunctionalNew {
			

			OnDual(DiscreteConformalStructure g) {
				super( g, new int[g.getNumEdges()], new double[ g.getNumFaces()] );
			}
			
			OnDual(DiscreteConformalStructure g, int [] eps, double [] f ) {
				super( g, eps, f );
			}
			

			@Override
			public void setQuadCycle( List quadCycle ) {
				CycleUtility.computeEpsOnDual( quadCycle, eps );		
			}
		
			/*
			 * Evaluates the Dirichlet functional for f and saves the Laplacian of
			 * the function f.
			 * 
			 * @see de.jtem.numericalMethods.calculus.function.RealFunctionOfSeveralVariablesWithGradient#eval(double[],
			 *      double[])
			 */
			@Override
			public double eval(double[] f, double[] grad) {
				if ((f.length != getNumberOfVariables())
						|| (grad.length != getNumberOfVariables()))
					throw new IllegalArgumentException(
							"The arguments should be arrays of size "
							+ getNumberOfVariables() + " not " + f.length);
				double value = 0;
				Arrays.fill(grad, 0.);
				
				for (int ie = 0; ie < G.getNumEdges(); ie++) {
					E e = (E) G.edgeList.get(ie);
					if( e.isPositive() ) {
						int fL = e.getLeftFace().getIndex();
						int fR = e.getRightFace().getIndex();
						
						final double de = f[fL] - f[fR] + eps[ie];
						final double d = de / e.getRho();
						value += d * de;
						grad[fR] -= 2*d;
						grad[fL] += 2*d;
					}
				}
				grad[0] = 0; // Or else too much freedom.
				return value;
			}
			
			private double[] grad;
			
			@Override
			public double eval(double[] f) {
				if (grad == null)
					grad = new double[getNumberOfVariables()];
				return eval(f, grad);
			}
			
			@Override
			public int getNumberOfVariables() {
				return G.getNumFaces();
			}
		}
		
}
