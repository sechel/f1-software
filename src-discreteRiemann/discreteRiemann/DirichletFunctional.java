package discreteRiemann;

import halfedge.Edge;
import halfedge.Face;
import halfedge.HalfEdgeDataStructure;
import halfedge.Vertex;

import java.util.Arrays;
import java.util.List;

import de.jtem.numericalMethods.calculus.function.RealFunctionOfSeveralVariablesWithGradient;

/**
 * @author mercat
 *
 */

public abstract class DirichletFunctional <
V extends Vertex<V, E, F>,
E extends Edge<V, E, F> & HasRho,
F extends Face<V, E, F>
> 
	implements RealFunctionOfSeveralVariablesWithGradient {

	/**
	 * The graph
	 */
	HalfEdgeDataStructure<V,E,F> G;

	/**
	 * The cut: on each oriented edge 0 no cut, +/- 1: a stretch
	 */
	int [] eps;

//	final double [] rho;
	
	double [] f;
	/**
	 * @param g
	 *            the graph
	 */
	DirichletFunctional(HalfEdgeDataStructure<V, E, F> g) {
		G = g;
		eps   = new int[g.getNumEdges()];
		f = new double[getNumberOfVariables()];
	}
	/**
	 * @param g
	 *            the graph
	 */
	DirichletFunctional(DiscreteConformalStructure g) {
		G = g;
		eps   = new int[g.getNumEdges()];
		f = new double[getNumberOfVariables()];
	}
	DirichletFunctional(HalfEdgeDataStructure g, double [] rho) {
		G = g;
		if( rho.length != g.getNumEdges() )
			throw new IllegalArgumentException( "rho has wrong length" );
		
		for(E e: G.getPositiveEdges()){
			e.setRho(rho[e.getIndex()]);	
		}
		eps   = new int[g.getNumEdges()];
		f = new double[getNumberOfVariables()];
	}

	private double[] grad;

	public double eval(double[] f) {
		if (grad == null)
			grad = new double[getNumberOfVariables()];
		return eval(f, grad);
	}

	 static class OnTheGraph <
	 V extends Vertex<V, E, F>,
	 E extends Edge<V, E, F> & HasRho,
	 F extends Face<V, E, F>
	 > extends DirichletFunctional {


			OnTheGraph(DiscreteConformalStructure g) {
				super(g);
			}
		OnTheGraph(HalfEdgeDataStructure g, double [] rho) {
			super(g, rho);
		}

		/*
		 * Evaluates the Dirichlet functional for f and saves the Laplacian of
		 * the function f.
		 * 
		 * @see de.jtem.numericalMethods.calculus.function.RealFunctionOfSeveralVariablesWithGradient#eval(double[],
		 *      double[])
		 */
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

		public int getNumberOfVariables() {
			return G.getNumVertices();
		}	
	}

	static class OnTheDualGraph <
	V extends Vertex<V, E, F>,
	E extends Edge<V, E, F> & HasRho,
	F extends Face<V, E, F>
	>  extends DirichletFunctional {


			OnTheDualGraph(DiscreteConformalStructure g) {
				super(g);
			}

			OnTheDualGraph(HalfEdgeDataStructure g, double [] rho ) {
			super(g, rho);
		}

		/*
		 * Evaluates the Dirichlet functional for f and saves the Laplacian of
		 * the function f.
		 * 
		 * @see de.jtem.numericalMethods.calculus.function.RealFunctionOfSeveralVariablesWithGradient#eval(double[],
		 *      double[])
		 */
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

		public double eval(double[] f) {
			if (grad == null)
				grad = new double[getNumberOfVariables()];
			return eval(f, grad);
		}

		public int getNumberOfVariables() {
			return G.getNumFaces();
		}
	}

	
	void minimize() {
		Arrays.fill(f,0.5);
//		ConjugateGradient.search(f, 1e-15, this);
		ConjugateGradient.search(f, 1e-15, this, 10000, false, null);
		 double[] grad = new double[getNumberOfVariables()];
			eval(f, grad);		
//			System.out.println("Gradient: "+VectorOperations.normSqr(grad));
	}
	
	public static class Factory <
	V extends Vertex<V, E, F> ,
	E extends Edge<V, E, F> & HasRho,
	F extends Face<V, E, F>
	> {
	
		final DiscreteConformalStructure<V,E,F> G;
		final DirichletFunctional.OnTheGraph f;
		final DirichletFunctional.OnTheDualGraph fs;
		
			
		 public Factory( DiscreteConformalStructure<V, E, F> g ) {

			this.G = g;
			
			f  = new DirichletFunctional.OnTheGraph(g);
			fs = new DirichletFunctional.OnTheDualGraph(g);
			
			setRho(1);
		}
		
		public void setRho( double c ) {
			for(HasRho e: G.getPositiveEdges()) e.setRho(c);
		}
		
		public void setRho( double [] rho ) {
			if( rho.length != G.getNumEdges())
				throw new IllegalArgumentException("wrong size of array");
			for (E e: G.getPositiveEdges()) {
				if( rho[e.getIndex()] != rho[e.getOppositeEdge().getIndex()])
					throw new IllegalArgumentException();
				e.setRho(rho[e.getIndex()]);
			}
		}
		
		public void setQuadCycle( List<E> quadCycle ) {
			CycleUtility.computeEps( quadCycle, new int[][] { f.eps, fs.eps } );
		}
		
		public void update() {
			f .minimize();
			fs.minimize();		
		}

		public void updateGraph() {
			f .minimize();
		}
		
		public void updateDual() {
			fs.minimize();
		}
	}

	
	
	
}
