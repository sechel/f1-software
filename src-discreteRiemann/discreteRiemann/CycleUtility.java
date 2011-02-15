package discreteRiemann;

import halfedge.Edge;
import halfedge.Face;
import halfedge.HalfEdgeDataStructure;
import halfedge.HalfEdgeUtility;
import halfedge.Vertex;

import java.util.ArrayList;
import java.util.List;

import de.jtem.blas.RealMatrix;
import de.jtem.numericalMethods.util.Arrays;
import discreteRiemann.DiscreteConformalStructure.ConfEdge;

public class CycleUtility {

	
	/** used to be slot 0 */
	static 	
	<
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> 
	int [] signsOnGraph( List<E> quadCycle ) {
		final int n=quadCycle.size();
		int [] signs = new int[n];
		for( int i=0; i<n; i++ ) {
			E pe = quadCycle.get((i+n-1)%n);
			E ce = quadCycle.get(i);
			E ne = quadCycle.get((i+1)%n);
			
			signs[i] = ce.getRightFace()   == HalfEdgeUtility.findCommonFace  (pe,ce) ? +1 : -1;
			
			// do an overwrite for zeros
			if( HalfEdgeUtility.findCommonFace(pe,ce,ne) != null ) {
				signs[i] = 0;  // common face => sign on graph equals 0
			}	
		}
		return signs;
	}
	
	/** used to be slot 1 */
	static 
	<
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> 
	int [] signsOnDual( List<E> quadCycle ) {
		final int n=quadCycle.size();
		int [] signs = new int[n];
		for( int i=0; i<n; i++ ) {
			E pe = quadCycle.get((i+n-1)%n);
			E ce = quadCycle.get(i);
			E ne = quadCycle.get((i+1)%n);
			
			signs[i] = ce.getStartVertex() == HalfEdgeUtility.findCommonVertex(pe,ce) ? -1 : +1;
			
			// do an overwrite for zeros
			if( HalfEdgeUtility.findCommonVertex(pe,ce,ne) != null ) {
				signs[i] = 0;  // common vertex => sign on dual graph equals 0
			}
		}
		return signs;
	}
	
	/** @deprecated */
	static 	
	<
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> int [][] computeSigns( List<E> quadCycle ) {
		final int n=quadCycle.size();
		final int [][] signs = new int[][] { signsOnGraph(quadCycle), signsOnDual (quadCycle) };
		return signs;
	}

	/** @deprecated */
	static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> int[][] computeEps( List<E> quadCycle) {
		HalfEdgeDataStructure<V, E, F> heds = quadCycle.get(0).getHalfEdgeDataStructure();
		
		int [][] eps = new int[2][];
		eps[0] = new int[heds.getNumEdges()];
		eps[1] = new int[heds.getNumEdges()];
		computeEps(quadCycle, eps);
		return eps;
	}
	
	/** @deprecated */
	static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> void computeEps( List<E> quadCycle, int [][] eps ) {
		
		int [][] signs = computeSigns( quadCycle );
		Arrays.fill(eps[0],0);
		Arrays.fill(eps[1],0);
		
		for( int i=0; i<quadCycle.size(); i++ ) {
			E e = quadCycle.get(i);
			
			eps[0][e.getIndex()] = signs[0][i];
			eps[1][e.getIndex()] = signs[1][i];
			
			eps[0][e.getOppositeEdge().getIndex()] = -signs[0][i];
			eps[1][e.getOppositeEdge().getIndex()] = -signs[1][i];
		}
	}
	
	static void computeEps( List<? extends Edge> quadCycle, int [] eps, int [] signs ) {
		
		Arrays.fill(eps,0);
		
		for( int i=0; i<quadCycle.size(); i++ ) {
			Edge e = quadCycle.get(i);
			
			eps[e.getIndex()] = signs[i];
			
			eps[e.getOppositeEdge().getIndex()] = -signs[i];
		}
	}
	
	static void computeEpsOnGraph( List<ConfEdge> quadCycle, int [] eps ) {
		computeEps( quadCycle, eps, signsOnGraph( quadCycle ) );
	}
	
	static void computeEpsOnDual( List<ConfEdge> quadCycle, int [] eps ) {
		computeEps( quadCycle, eps, signsOnDual( quadCycle ) );
	}
	
	/**
	 * Transforms a cycle on the graph to a quadCycle by adding half stars.
	 * @param <V>
	 * @param <E>
	 * @param <F>
	 * @param cycle
	 * @return
	 */
	static 	
	<   V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	>  List<E> cycleToQuad( List<E> cycle ) {
		List<E> quadCycle = new ArrayList<E>();
		
		final int n=cycle.size();
		
		for( int i=1; i <= n; i++ ) {
			E e = cycle.get(i-1);
			E neOp = cycle.get(i % n).getOppositeEdge();
			if(e.getOppositeEdge().getPreviousEdge() == neOp) // No need to turn around the whole stars.
				quadCycle.add(e);
			else // Turn around the target vertex
				while(e != neOp) {
					quadCycle.add(e);
					e = e.getNextEdge().getOppositeEdge();
			}
		}
		return quadCycle;
	}
	
	static 	
	<   V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	>  List<List<E>> cyclesToQuads( List<List<E>> cycles ) {
		List<List<E>> quadCycles = new ArrayList<List <E>>(cycles.size());
	for(List<E> cycle: cycles) quadCycles.add(cycleToQuad(cycle));
	return quadCycles;
	}
	static 	
	<   V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	>  List<List<E>> quadsToCycles( List<List<E>> quadCycles ) {
		List<List<E>> cycles = new ArrayList<List <E>>(quadCycles.size());
	for(List<E> cycle: quadCycles) cycles.add(quadToCycle(cycle));
	return cycles;
	}
	static 	
	<   V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	>  List<List<E>> quadsToDualCycles( List<List<E>> quadCycles ) {
		List<List<E>> cycles = new ArrayList<List <E>>(quadCycles.size());
	for(List<E> cycle: quadCycles) cycles.add(quadToDualCycle(cycle));
	return cycles;
	}

		/**
	 * Transforms a quadCycle to a cycle on the graph.
	 * @param <V>
	 * @param <E>
	 * @param <F>
	 * @param quadCycle
	 * @return
	 */
	static 	
	<   V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	>  List<E> quadToCycle( List<E> quadCycle ) {
		List<E> cycle = new ArrayList<E>();
		
		final int n=quadCycle.size();
		E pe;
		E ce = quadCycle.get(n-1);
		E ne = quadCycle.get(0);
		for( int i=0; i<n; i++ ) {
			pe = ce;
			ce = ne;
			ne = quadCycle.get((i+1)%n);
		
			if( HalfEdgeUtility.findCommonVertex(pe,ce,ne) != null )
				continue; // do not add the edge

			if( HalfEdgeUtility.findCommonVertex(pe,ce) == ce.getStartVertex() ) {
				cycle.add(ce);
			} else {
				cycle.add(ce.getOppositeEdge());
			}
		}
		return cycle;
	}
	
	static 	
	<   V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	>  List<E> quadToDualCycle( List<E> quadCycle ) {
		List<E> cycle = new ArrayList<E>();
		
		final int n=quadCycle.size();
		E pe;
		E ce = quadCycle.get(n-1);
		E ne = quadCycle.get(0);
		for( int i=0; i<n; i++ ) {
			pe = ce;
			ce = ne;
			ne = quadCycle.get((i+1)%n);
			
			if( HalfEdgeUtility.findCommonFace(pe,ce,ne) != null )
				continue; // do not add the edge
			
			if( HalfEdgeUtility.findCommonFace(pe,ce)== ce.getRightFace() ) {
				cycle.add(ce);
			} else {
				cycle.add(ce.getOppositeEdge());
			}
		}
		return cycle;
	}


	public static 	
	<
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> double [] grad( HalfEdgeDataStructure<V,E,F> G, double [] f, int [] eps ) {
		double [] alpha = new double[G.getNumEdges()];
		grad( G, f, eps, alpha );
		return alpha;
	}


	/**
	 * @param <V>
	 * @param <E>
	 * @param <F>
	 * @param G the surface
	 * @param f the (multivalued) function
	 * @param eps the jumps along each edge
	 * @param alpha the resulting df
	 */
	public static 	
	<
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> void grad( HalfEdgeDataStructure<V,E,F> G, double [] f, int [] eps, double [] alpha ) {
		
		assert f.length == G.getNumVertices();
		assert eps.length == G.getNumEdges();
		assert alpha.length == G.getNumEdges();
		
		for( E e : G.edgeList ) {
			alpha[e.getIndex()]
			      = f[e.getTargetVertex().getIndex()]
			          -f[e.getStartVertex().getIndex()] + eps[e.getIndex()];
		}
	}


	public static 	
	<
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> double [] gradDual( HalfEdgeDataStructure<V,E,F> G, double [] f, int [] eps ) {
		double [] alpha = new double[G.getNumEdges()];
		gradDual( G, f, eps, alpha );
		return alpha;
	}


	public static 	
	<
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> void gradDual( HalfEdgeDataStructure<V,E,F> G, double [] f, int [] eps, double [] alpha ) {
		
		assert f.length == G.getNumFaces();
		assert eps.length == G.getNumEdges();
		assert alpha.length == G.getNumEdges();
		
		for( E e : G.edgeList ) {
			alpha[e.getIndex()]
			      = f[e.getLeftFace().getIndex()]
			          -f[e.getRightFace().getIndex()] + eps[e.getIndex()];
		}
	}


	/**
	 * @param <V>
	 * @param <E>
	 * @param <F>
	 * @param alpha a form, alpha[e] is its value on the edge e
	 * @param path a list of edges
	 * @return
	 */
	public static 	
	<
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> double integrate( double [] alpha, List<E>path ) {
		
		double sum = 0;
		for(E e: path) {
			sum += alpha[e.getIndex()];
		}
		return sum;
	}



	/**
	 * @param <V>
	 * @param <E>
	 * @param <F>
	 * @param G the surface
	 * @param alpha the 1-form  of size G.getNumEdges()
	 * @return omega=d alpha, a double[] of size G.getNumFaces()
	 */
	public static
	 <
	V extends Vertex<V, E, F>,
	E extends Edge<V, E, F>,
	F extends Face<V, E, F>
> double[] curl(HalfEdgeDataStructure<V,E,F> G, double[] alpha){
		assert alpha.length == G.getNumEdges();
		double[] omega = new double[G.getNumFaces()];
		curl(G,alpha, omega);
		return omega;
	}
		/**
	 * @param <V>
	 * @param <E>
	 * @param <F>
	 * @param G the surface
	 * @param alpha the 1-form  of size G.getNumEdges()
	 * @param omega=d alpha, a double[] of size G.getNumFaces()
	 * @return
	 */
	public static
	 <
	V extends Vertex<V, E, F>,
	E extends Edge<V, E, F>,
	F extends Face<V, E, F>
> void curl(HalfEdgeDataStructure<V,E,F> G, double[] alpha, double[] omega){
		assert alpha.length == G.getNumEdges();
		assert omega.length == G.getNumFaces();
		
		for(F f: G.getFaces()) {
		 double ia = 0.;
		 for(E e: f.getBoundary()){
			 ia += alpha[e.getIndex()];
		 }
		 omega[f.getIndex()] = ia;
	 }
	}

	/**
	 * @param <V>
	 * @param <E>
	 * @param <F>
	 * @param G the surface
	 * @param alpha the 1-form  of size G.getNumEdges()
	 * @return f=div alpha, a double[] of size G.getNumVertices()
	 */
	public static
	 <
	V extends Vertex<V, E, F>,
	E extends Edge<V, E, F>,
	F extends Face<V, E, F>
> double[] div(HalfEdgeDataStructure<V,E,F> G, double[] alpha){
		assert alpha.length == G.getNumEdges();
		double[] f = new double[G.getNumVertices()];
		div(G,alpha, f);
		return f;
	}
		/**
	 * @param <V>
	 * @param <E>
	 * @param <F>
	 * @param G the surface
	 * @param alpha the 1-form  of size G.getNumEdges()
	 * @param f = div alpha, a double[] of size G.getNumVertices()
	 * @return
	 */
	public static
	 <
	V extends Vertex<V, E, F>,
	E extends Edge<V, E, F>,
	F extends Face<V, E, F>
> void div(HalfEdgeDataStructure<V,E,F> G, double[] alpha, double[] f){
		assert alpha.length == G.getNumEdges();
		assert f.length == G.getNumVertices();
		
		for(V v: G.getVertices()) {
		 double ia = 0.;
		 for(E e: v.getEdgeStar()){
			 ia += alpha[e.getIndex()];
		 }
		 f[v.getIndex()] = ia;
	 }
	}

	public static
		 <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> boolean isClosed(HalfEdgeDataStructure<V,E,F> G, double[] alpha){
		assert alpha.length == G.getNumEdges();
		
		for(F f: G.getFaces()) {
			 double ia = 0.;
			 for(E e: f.getBoundary()){
				 ia += alpha[e.getIndex()];
			 }
			 
			 // System.out.println( "Closeness : " + ia );
			 if (Math.abs(ia) > 1e-7) return false;
		 }
		 return true;
		}


	public static
		 <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> boolean isDualClosed(HalfEdgeDataStructure<V,E,F> G, double[] alpha){
		 for(V v: G.getVertices()) {
			 double ia = 0.;
			 for(E e: v.getEdgeStar()){
				 ia += alpha[e.getIndex()];
			 }
			 
			 System.out.println( "CoCloseness : " + ia );
			 if (Math.abs(ia) > 1e-7) return false;
		 }
		 return true;
		}


	/**
	 * @param <V>
	 * @param <E>
	 * @param <F>
	 * @param forms are given as a list of 2 double[], form[0][e]: one double for the edge e, 
	 * form[1][e]: one double for the dual edge e^*
	 * @param quadCycleList a list of g cycles given as quads.
	 * @return a double[][] indexed by forms and by graph cycles (0..g-1) followed
	 * by cycles on dual graph (g..2*g-1).
	 */
	static public 	<
	V extends Vertex<V, E, F>,
	E extends Edge<V, E, F>,
	F extends Face<V, E, F>
	> double[][] periods( final List<double[][]> forms, final List<List<E>> quadCycleList) {
		int i = 0,j;
		int g = quadCycleList.size(); // Not necessarily the genus of the surface.
		double[][] result = new double[forms.size()][];
		
		for(double[][] form: forms){
			j = 0;
			double[] L = new double[2*g];
			result[i] = L;
			for(List<E> quadCycle: quadCycleList){
				double[] P = CycleUtility.period(form, quadCycle); 
				L[j] = P[0]; L[j+g] = P[1];
				j++;
			}
			i++;
		}
		return result;
	}

	/**
	 * @deprecated
	 * @param <V>
	 * @param <E>
	 * @param <F>
	 * @param forms are given as a list of 2 double[], form[0][e]: one double for the edge e, 
	 * form[1][e]: one double for the dual edge e^*
	 * @param quadBasis a list of cycles given as quads.
	 * @return a RealMatrix rows indexed by forms and colums by graph cycles (0..g-1) followed
	 * by cycles on dual graph (g..2*g-1).
	 */
	static public 	<
	V extends Vertex<V, E, F>,
	E extends Edge<V, E, F>,
	F extends Face<V, E, F>
	> RealMatrix[] periodMatrices( final List<double[][]> forms, final List<List<E>> quadBasis) {
		int i = 0,j;
		int g = quadBasis.size();
		RealMatrix[] result = new RealMatrix[2];
		result[0] = new RealMatrix(forms.size(), g);
		result[1] = new RealMatrix(forms.size(), g);
		
		for(double[][] form: forms){
			j = 0;
			for(List<E> quadCycle: quadBasis){
				double[] P = CycleUtility.period(form, quadCycle); 
				result[0].set(i,j, P[0]); 
				result[1].set(i,j, P[1]);
				j++;
			}
			i++;
		}
		return result;
	}
	/**
	 * @param <V>
	 * @param <E>
	 * @param <F>
	 * @param forms are given as an array[#forms][#edges]
	 * @param basisOnGraph a list of cycles given as edges.
	 * @return a matrix rows indexed by forms and colums by graph cycles 
	 */
	static public 	<
	V extends Vertex<V, E, F>,
	E extends Edge<V, E, F>,
	F extends Face<V, E, F>
	> double [][] periods( double[][] forms, final List<List<E>> basisOnGraph) {
		
		double [][] result = new double [forms.length][basisOnGraph.size()];
		
		for(int i = 0; i<forms.length; i++ ){
			int j=0;
			for(List<E> cycle: basisOnGraph){
				result[i][j] = integrate(forms[i], cycle ); 
				j++;
			}
		}
		return result;
	}


	/**
	 * @deprecated
	 * @param <V>
	 * @param <E>
	 * @param <F>
	 * @param form a 1-form, form[0][e] on the edge e of the graph, form[1][e] on the dual edge e^*.
	 * @param quadCycle a cycle given by a list of quads.
	 * @return a double[2] : {period on the graph, period on the dual}
	 */
	static public 	<
	V extends Vertex<V, E, F>,
	E extends Edge<V, E, F>,
	F extends Face<V, E, F>
	> double[] period( final double[][] form, final List<E> quadCycle) {
		final double[] result = {integrate(form[0],quadToCycle(quadCycle)),
		integrate(form[1],quadToDualCycle(quadCycle))};
		return result;
	}
	
	static public 	<
	V extends Vertex<V, E, F>,
	E extends Edge<V, E, F>,
	F extends Face<V, E, F>
	> void printVertices(List<E> path){
 StringBuffer s = new StringBuffer();
		for(E e: path) s.append(" "+e.getStartVertex().getIndex()+" "+e.getTargetVertex().getIndex());
		System.out.println(s);
	}
	
}
