package discreteRiemann;

import halfedge.Edge;
import halfedge.Face;
import halfedge.HalfEdgeDataStructure;
import halfedge.Vertex;
import halfedge.decorations.HasXY;
import halfedge.frontend.FullFeaturedEditor;
import halfedge.frontend.action.MainWindowClosing;
import halfedge.frontend.controller.MainController;
import halfedge.surfaceutilities.SurfaceUtility;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.List;

import javax.swing.JFrame;
import javax.vecmath.Point2d;

import de.jtem.blas.ComplexMatrix;
import de.jtem.blas.IntegerMatrix;
import de.jtem.blas.RealMatrix;
import de.jtem.mfc.field.Complex;
import de.jtem.riemann.theta.SiegelReduction;
import discreteRiemann.DirichletFunctional.Factory;
import discreteRiemann.DiscreteConformalStructure.ConfEdge;
import discreteRiemann.DiscreteConformalStructure.ConfFace;
import discreteRiemann.DiscreteConformalStructure.ConfVertex;


public class DiscreteRiemannUtility {

	
	public static 	
	<
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F> & HasRho,
		F extends Face<V, E, F>
	> void star( HalfEdgeDataStructure<V,E,F> G, double [] alpha, double [] rho, double [] starAlpha ) {
		DiscreteConformalStructure g = new DiscreteConformalStructure(G, rho);
	}		
		/**
		 * The Hodge star of a 1-form on the graph
		 * @param <V>
		 * @param <E>
		 * @param <F>
		 * @param G the underlying discrete conformal structure
		 * @param alpha the 1-form given as alpha[e] for each edge
		 * @param starAlpha the dual 1-form result of the star
		 */
		public static 	
		<
			V extends Vertex<V, E, F> ,
			E extends Edge<V, E, F> & HasRho,
			F extends Face<V, E, F>
		> void star( DiscreteConformalStructure<V,E,F> G, double [] alpha, double [] starAlpha ) {
		assert     alpha.length == G.getNumEdges();
		assert starAlpha.length == G.getNumEdges();
		
		for( int i=0; i<alpha.length; i++ ) {
			starAlpha[i] = alpha[i] * ((HasRho) G.getEdge(i)).getRho();
		}
	}
	
	public static 	
	<
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F> & HasRho,
		F extends Face<V, E, F>
	> double [] star(double [] rho, HalfEdgeDataStructure<V,E,F> G, double [] alpha) {
		double [] starAlpha = new double[G.getNumEdges()];
		star( new DiscreteConformalStructure(G, rho), alpha, starAlpha );
		return starAlpha;
	}
	
	public static 	
	<
		V extends Vertex<V, E, F> ,
		E extends Edge<V, E, F> & HasRho,
		F extends Face<V, E, F>
	> double [] star( DiscreteConformalStructure<V,E,F> G, double [] alpha) {
		double [] starAlpha = new double[G.getNumEdges()];
		star( (DiscreteConformalStructure<V, E, F>) G, alpha, starAlpha );
		return starAlpha;
	}
	

	/**
	 * @param <V>
	 * @param <E>
	 * @param <F>
	 * @param G
	 * @param alphas a RealMatrix whose rows are forms
	 * @return another
	 */
	public static 	
	<
		V extends Vertex<V, E, F> ,
		E extends Edge<V, E, F> & HasRho,
		F extends Face<V, E, F>
	> RealMatrix star( DiscreteConformalStructure<V,E,F> G, RealMatrix alphas) {
		double[][] starAlphas = new double[alphas.size()][];
		for(int i = 0; i < alphas.getNumRows(); i++)
		starAlphas[i] = star( (DiscreteConformalStructure<V, E, F>) G, alphas.getRow(i).re);
		return new RealMatrix(starAlphas);
	}
	
	public static 	
	<
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F> & HasRho,
		F extends Face<V, E, F>
	> double [][] star( DiscreteConformalStructure<V,E,F> G, double [][] alpha) {
		double [][] starAlpha = new double[alpha.length][G.getNumEdges()];
		for( int i=0; i<alpha.length; i++ ) {
			star( (DiscreteConformalStructure<V, E, F>) G, alpha[i], starAlpha[i] );
		}
		return starAlpha;
	}
	
	public static 	
	<
		V extends Vertex<V, E, F> ,
		E extends Edge<V, E, F> & HasRho,
		F extends Face<V, E, F>
	> double [][] starDual( DiscreteConformalStructure<V,E,F> G, double [][] alpha) {
		double [][] starAlpha = new double[alpha.length][G.getNumEdges()];
		for( int i=0; i<alpha.length; i++ ) {
			starDual( (DiscreteConformalStructure<V, E, F>) G, alpha[i], starAlpha[i] );
		}
		return starAlpha;
	}
	
	public static 	
	<
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F> & HasRho,
		F extends Face<V, E, F>
	> void starDual( HalfEdgeDataStructure<V,E,F> G, double [] alpha, double [] rho, double [] starAlpha ) {
		assert       rho.length == G.getNumEdges();
	}		
	/**
	 * The Hodge star of a 1-form on the dual graph
	 * @param <V>
	 * @param <E>
	 * @param <F>
	 * @param G the underlying discrete conformal structure
	 * @param alpha the dual 1-form given as alpha[e^*] for each dual edge e^*
	 * @param starAlpha the 1-form on the graph result of the star
	 */
	public static 	
		<
			V extends Vertex<V, E, F>,
			E extends Edge<V, E, F> & HasRho,
			F extends Face<V, E, F>
		> void starDual( DiscreteConformalStructure<V,E,F> G, double [] alpha, double [] starAlpha ) {
		assert     alpha.length == G.getNumEdges();
		assert starAlpha.length == G.getNumEdges();
		
		for( int i=0; i<alpha.length; i++ ) {
			starAlpha[i] = -alpha[i] / G.getEdge(i).getRho();
		}
	}
		
		public static 	
		<
			V extends Vertex<V, E, F>,
			E extends Edge<V, E, F> & HasRho,
			F extends Face<V, E, F>
		> double [] starDual( DiscreteConformalStructure<V,E,F> G, double [] alpha) {
			double [] starAlpha = new double[G.getNumEdges()];
			starDual( G, alpha, starAlpha );
			return starAlpha;
		}
	
	public static 	
	<
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F> & HasRho,
		F extends Face<V, E, F>
	> double [] starDual(double [] rho, HalfEdgeDataStructure<V,E,F> G, double [] alpha) {
		double [] starAlpha = new double[G.getNumEdges()];
		starDual( G, alpha, rho, starAlpha );
		return starAlpha;
	}
	


public static
	 <
	V extends Vertex<V, E, F> ,
	E extends Edge<V, E, F> & HasRho,
	F extends Face<V, E, F>
> boolean isCoClosed(DiscreteConformalStructure<V,E,F> G, double[] alpha){
	 for(V v: G.getVertices()) {
		 double ia = 0.;
		 for(E e: v.getEdgeStar()){
			 ia += alpha[e.getIndex()]*e.getRho();
		 }
		 
		 System.out.println( "CoCloseness : " + ia );
		 if (Math.abs(ia) > 1e-5) return false;
	 }
	 return true;
	}


public static
	 <
	V extends Vertex<V, E, F> ,
	E extends Edge<V, E, F> & HasRho,
	F extends Face<V, E, F>
> boolean isDualCoClosed(DiscreteConformalStructure<V,E,F> G, double[] alpha){
	 for(F f: G.getFaces()) {
		 double ia = 0.;
		 for(E e: f.getBoundary()){
			 ia += alpha[e.getIndex()]/e.getRho();
		 }
		 
		 System.out.println( "DualCoCloseness : " + ia );
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
 * @param quadBasis a list of cycles given as quads.
 * @return a ComplexMatrix rows indexed by forms and colums by graph cycles (0..g-1) followed
 * by cycles on dual graph (g..2*g-1).
 */
static public 	<
V extends Vertex<V, E, F> ,
E extends Edge<V, E, F> & HasRho,
F extends Face<V, E, F>
> ComplexMatrix[] periodMatrices(final List<List<E>> quadBasis, final Factory factory) {
	 final List<double[][]> forms =  HarmonicUtility.cohomologyBasis(quadBasis, factory);
	 
	int i = 0,j;
	int g = quadBasis.size();
	ComplexMatrix[] result = new ComplexMatrix[2];
	result[0] = new ComplexMatrix(forms.size(), g);
	result[1] = new ComplexMatrix(forms.size(), g);
	
	for (List<E> quadCyclei : quadBasis){
		double[][] formi = HarmonicUtility.dualHarmonicForm(quadCyclei,
				factory);
		j = 0;
		for(List<E> quadCyclej: quadBasis){
			double[] P = CycleUtility.period(formi, quadCyclej); 
			result[0].set(i,j, P[0], 0.); 
			result[1].set(i,j, P[1], 0.);
			j++;
		}
		i++;
	}
	return result;
}


public static <
V extends Vertex<V, E, F> ,
E extends Edge<V, E, F> & HasRho,
F extends Face<V, E, F>
>  void randomRho(HalfEdgeDataStructure<V,E,F> hds) {
	for (E e : hds.getPositiveEdges()){
		double r = Math.random()+0.5;
			e.setRho(r);
			e.getOppositeEdge().setRho(1/r);
	}
}

/*
 * Test method for 'discreteRiemann.DirichletFunctional.eval(double[], double[])'
 */

	public static 	
	<
		V extends Vertex<V, E, F> ,
		E extends Edge<V, E, F> & HasRho,
		F extends Face<V, E, F>
	> void compute( HalfEdgeDataStructure<V,E,F>ds, double rho ) {
		
		DiscreteConformalStructure<V,E,F> dcs = new DiscreteConformalStructure<V, E, F>(ds);
		
		DirichletFunctional.Factory factory = new DirichletFunctional.Factory(dcs);
		
		factory.setRho(rho);
		
		compute( factory );
	}
	
	public static 	
	<
		V extends Vertex<V, E, F> ,
		E extends Edge<V, E, F> & HasRho,
		F extends Face<V, E, F>
	> void compute( HalfEdgeDataStructure<V,E,F>ds, double [] rho ) {
		
		DiscreteConformalStructure<V,E,F> dcs = new DiscreteConformalStructure<V, E, F>(ds);
		
		DirichletFunctional.Factory factory = new DirichletFunctional.Factory(dcs);
		
		factory.setRho(rho);
		
		compute( factory );
	}
	
	
	public static
	<
		V extends Vertex<V, E, F> ,
		E extends Edge<V, E, F> & HasRho,
		F extends Face<V, E, F>
	> void compute( DirichletFunctional.Factory<V,E,F> factory ) {
	
	DiscreteConformalStructure<V,E,F> dcs = factory.G;
	
	E e = dcs.getEdge(0);
	
	V root = dcs.getVertex(0);
	
	List<List<E>> basisOnGraph =  HomotopyUtility.homotopyBasis(root);
	
	for( List<E> cycle : basisOnGraph ) {
		CycleUtility.printVertices(cycle);
	}

	List<List<E>> quadBasis   = CycleUtility.cyclesToQuads(basisOnGraph);
	List<List<E>> basisOnDual = CycleUtility.quadsToDualCycles(quadBasis);
	
	for( List<E> cycle : basisOnDual ) {
		CycleUtility.printVertices(cycle);
	}
	RealMatrix  w = new RealMatrix( HarmonicUtility.cohomologyBasisOnGraph(quadBasis,factory) );
	
	IntegerMatrix im = IntegerMatrix.round( 
			new RealMatrix( CycleUtility.periods(w.re, basisOnGraph) ) );
	
	
	
	IntegerMatrix P       = HomologyUtility.createNormalizedBasis( im );
	IntegerMatrix aCycleP = HomologyUtility.extractACycles(P);
	IntegerMatrix bCycleP = HomologyUtility.extractBCycles(P);
	
	System.out.println("P=\n"+P);
	System.out.println("aCycleP=\n"+aCycleP);
	System.out.println("bCycleP =\n"+bCycleP);
	
	RealMatrix alpha = aCycleP.times(w); // w.times( aCycleP.transpose() );
	RealMatrix beta  = bCycleP.times(w); //.times( bCycleP.transpose() );
	
	RealMatrix unnormalizedAlphaPeriods = new RealMatrix( CycleUtility.periods(alpha.re,basisOnGraph) );
	RealMatrix unnormalizedBetaPeriods  = new RealMatrix( CycleUtility.periods(beta .re,basisOnGraph) );
	
	RealMatrix alphaAPeriods = aCycleP.times(unnormalizedAlphaPeriods.transpose());
	RealMatrix alphaBPeriods = bCycleP.times(unnormalizedAlphaPeriods.transpose());
	RealMatrix betaAPeriods  = aCycleP.times(unnormalizedBetaPeriods .transpose());
	RealMatrix betaBPeriods  = bCycleP.times(unnormalizedBetaPeriods .transpose());
	
	System.out.println("alphaAPeriods=\n"+alphaAPeriods);
	System.out.println("alphaBPeriods=\n"+alphaBPeriods);
	System.out.println("betaAPeriods=\n"+betaAPeriods);
	System.out.println("betaBPeriods=\n"+betaBPeriods);
	
	//System.out.println("unnormAlphaPeriods=\n"+unnormalizedAlphaPeriods);
	//System.out.println("unnormBetaPeriods=\n"+unnormalizedBetaPeriods);
	
	//RealMatrix alphaPeriods = bCycleP.times(unnormalizedAlphaPeriods.transpose());
	//RealMatrix betaPeriods  = aCycleP.times(unnormalizedBetaPeriods .transpose());
	
	//System.out.println("alphaPeriods=\n"+alphaPeriods);
	//System.out.println("betaPeriods=\n"+betaPeriods);
	
	RealMatrix starAlpha = new RealMatrix( star(dcs, alpha.re));
	RealMatrix starBeta  = new RealMatrix( star(dcs, beta .re));

	RealMatrix unnormalizedStarAlphaPeriods = new RealMatrix( CycleUtility.periods(starAlpha.re,basisOnDual) );
	RealMatrix unnormalizedStarBetaPeriods  = new RealMatrix( CycleUtility.periods(starBeta.re,basisOnDual) );
	
	System.out.println( "unnormalizedStarAlphaPeriods=\n"+unnormalizedStarAlphaPeriods );
	System.out.println( "unnormalizedStarBetaPeriods=\n"+unnormalizedStarBetaPeriods );
	
	RealMatrix starAlphaAPeriods = aCycleP.times(unnormalizedStarAlphaPeriods.transpose());
	RealMatrix starAlphaBPeriods = bCycleP.times(unnormalizedStarAlphaPeriods.transpose());
	RealMatrix starBetaAPeriods  = aCycleP.times(unnormalizedStarBetaPeriods .transpose());
	RealMatrix starBetaBPeriods  = bCycleP.times(unnormalizedStarBetaPeriods .transpose());
	
	System.out.println("staralphaAPeriods=\n"+starAlphaAPeriods);
	System.out.println("staralphaBPeriods=\n"+starAlphaBPeriods);
	System.out.println("starbetaAPeriods=\n"+starBetaAPeriods);
	System.out.println("starbetaBPeriods=\n"+starBetaBPeriods);
	
	RealMatrix C = starAlphaAPeriods;
	RealMatrix B = starAlphaBPeriods;
	
	ComplexMatrix pm = new ComplexMatrix( C.invert().times(B).times(-1).re, C.invert().re );
	
	pm.assignTimes( new Complex( 0, 2*Math.PI ) );
	
	SiegelReduction sr = new SiegelReduction( pm );
	
	System.out.println("period matrix = \n" + pm );
	System.out.println("reduced period matrix = \n" + sr.getReducedPeriodMatrix() );
	
	ComplexMatrix pm1 = new ComplexMatrix( new double[2][2], new double[][] {{5./3, -4./3},{-4./3, 5./3}} );
	
	pm1.assignTimes( new Complex( 0, 2*Math.PI ) );
	
	SiegelReduction sr1 = new SiegelReduction( pm1 );
	
	System.out.println("period matrix = \n" + pm1 );
	System.out.println("reduced period matrix = \n" + sr1.getReducedPeriodMatrix() );
}

	
	
	public static void main(String[] args) {
	
		//DiscreteSchottky discreteSchottky = DiscreteSchottky.createSchottkyExample(1);
		//compute( discreteSchottky, 1 );//discreteSchottky.rho);

		HalfEdgeDataStructure ds = SilholsGenusExamples.create3QuadExample(ConfVertex.class,ConfEdge.class,ConfFace.class);
		
		for( int i=0; i<4; i++ ) {
			ds = SubdivisionUtility.createSubdivisionOfGraph(ds);
		}
		
		compute( ds, 1 );
	}
	
	
	/**
	 * Opens an editor where is displayed the given {@link HalfEdgeDataStructure} 
	 * whose vertices have given coordinates in the plane.
	 * @param <V> vertices with coordinates
	 * @param <E>
	 * @param <F>
	 * @param dcs
	 * @see HasXY
	 */
	public static <
	V extends Vertex<V, E, F> & HasXY,
	E extends Edge<V, E, F> & HasRho,
	F extends Face<V, E, F>
> void integrate(DiscreteRiemann<V,E,F> s, Complex[] form){
		V root = s.spanningTree.get(0).getTargetVertex();
		
		Point2d p = new Point2d();
		root.setXY(p);
		
		for(E e: s.spanningTree){
			Point2d tp = e.getTargetVertex().getXY();
			int i = e.getIndex();
			p.set(tp.x+form[i].re, tp.y+form[i].im);
			e.getStartVertex().setXY(p);
		}
	}
	/**
	 * Opens an editor where is displayed the given {@link HalfEdgeDataStructure} 
	 * whose vertices have given coordinates in the plane.
	 * @param <V> vertices with coordinates
	 * @param <E>
	 * @param <F>
	 * @param dcs
	 * @see HasXY
	 */
	public static <
	V extends Vertex<V, E, F> & HasXY,
	E extends Edge<V, E, F>,
	F extends Face<V, E, F>
> void show(HalfEdgeDataStructure<V,E,F> dcs){
			 MainController
				controller = new MainController();
	SurfaceUtility.rescaleGraph(dcs, 500);
			 FullFeaturedEditor<V,E,F>
					editPanel = new FullFeaturedEditor<V,E,F>
					(dcs, controller);
					 editPanel.getEditPanel().encompass();
					 JFrame frame = new JFrame();
					 controller.setMainFrame(frame);
	
						frame.setLayout(new BorderLayout());
						frame.add(editPanel, BorderLayout.CENTER);
						editPanel.setSize(new Dimension(600,600));
					 frame.setVisible(true);
	//				 frame.setAlwaysOnTop(true);
					frame.setMinimumSize(new Dimension(600,600));
					 frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
					 frame.addWindowListener(new MainWindowClosing());
					}
}