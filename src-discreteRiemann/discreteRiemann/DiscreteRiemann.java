package discreteRiemann;


import halfedge.Edge;
import halfedge.Face;
import halfedge.HalfEdgeDataStructure;
import halfedge.Vertex;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.jtem.blas.ComplexMatrix;
import de.jtem.blas.IntegerMatrix;
import de.jtem.blas.RealMatrix;
import de.jtem.mfc.field.Complex;
import de.jtem.riemann.theta.SiegelReduction;
import discreteRiemann.DiscreteConformalStructure.ConfEdge;
import discreteRiemann.DiscreteConformalStructure.ConfFace;
import discreteRiemann.DiscreteConformalStructure.ConfVertex;
import discreteRiemann.HarmonicFunction.OnDual;
import discreteRiemann.HarmonicFunction.OnGraph;

public class DiscreteRiemann 
<
V extends Vertex<V, E, F>,
E extends Edge<V, E, F> & HasRho,
F extends Face<V, E, F>
> {
	
	DiscreteConformalStructure<V,E,F> dcs;
	
	List<E> spanningTree;

	List<List<E>> basisOnGraph;
	List<List<E>> basisOnDual;
	
	List<List<E>> quadBasis;
	
	/** these harmonic functions jump accross each associated quad cycle
	 *  of the above quad basis.
	 *  */
	List<HarmonicFunction.OnGraph> harmonicsOnGraph;
	List<HarmonicFunction.OnDual > harmonicsOnDual;
		
	/** harmonic 1-Forms */
	RealMatrix omegaOnGraph, omegaOnDual;
	
	IntegerMatrix aCycleP;
	IntegerMatrix bCycleP;
	
	RealMatrix alphaOnGraph, alphaOnDual;
	RealMatrix  betaOnGraph,  betaOnDual;
	
	ComplexMatrix periodMatrixOnGraph, periodMatrixOnDual;
	
	
	DiscreteRiemann( DiscreteConformalStructure<V,E,F>dcs ) {
		
		this.dcs = dcs;
		
		harmonicsOnGraph = new ArrayList();
		harmonicsOnDual  = new ArrayList();
		
		computeSpanningTree();
	}
	
	public DiscreteRiemann( DiscreteConformalStructure<V,E,F>dcs, double rho ) {
		this(dcs);
		setRho(rho);
		computeBasis();
		
		computeHarmonicFunctions();
		
		compute();
	}
	
	public DiscreteRiemann( DiscreteConformalStructure<V,E,F>dcs, double [] rho ) {
		this(dcs);
		if( rho != null )
			setRho(rho);
		
		System.out.println("compute basis");
		computeBasis();
		
		System.out.println("compute harmonic functions");
		computeHarmonicFunctions();
		
		compute();
	}
	
	public DiscreteRiemann( DiscreteConformalStructure<V,E,F>dcs,
			List<List<E>> basisOnGraph, List<List<E>> quadBasis, 
			List<HarmonicFunction.OnGraph> hfg, List<HarmonicFunction.OnDual> hfd) {
		this(dcs);
				
		this.basisOnGraph = basisOnGraph;
		this.quadBasis = quadBasis;
		
		//basisOnGraph = CycleUtility.quadsToCycles(quadBasis);
		basisOnDual = CycleUtility.quadsToDualCycles(quadBasis);
		
		harmonicsOnGraph = hfg;
		harmonicsOnDual  = hfd;
		
		compute();
	}

	private void compute() {
		
		computeHarmonicDifferentials();
		computeNormalizedCyclesAndDifferentials();
		
		computePeriodMatrix();
	}

	void setRho( double c ) {
		for(HasRho e: dcs.getPositiveEdges()) e.setRho(c);
	}
	
	void setRho( double [] rho ) {
		if( rho.length != dcs.getNumEdges())
			throw new IllegalArgumentException("wrong size of array");
		for (E e: dcs.getPositiveEdges()) {
			if( rho[e.getIndex()] != rho[e.getOppositeEdge().getIndex()])
				throw new IllegalArgumentException();
			e.setRho(rho[e.getIndex()]);
		}
	}
	
	
	public ComplexMatrix getPeriodMatrixOnDual() {
		return new ComplexMatrix( periodMatrixOnDual );
	}
	
	public ComplexMatrix getPeriodMatrixOnGraph() {
		return new ComplexMatrix( periodMatrixOnGraph );
	}
	
	public ComplexMatrix getPeriodMatrix() {
		return periodMatrixOnDual.plus(periodMatrixOnGraph).divide(2);
	}
	
	void computePeriodMatrix() {
	
		
		System.out.println("aalpha"+aCycleP.times(new RealMatrix( CycleUtility.periods(alphaOnGraph.re,basisOnGraph) ).transpose())); // 0
		System.out.println("balpha"+bCycleP.times(new RealMatrix( CycleUtility.periods(alphaOnGraph.re,basisOnGraph) ).transpose())); // Id
		System.out.println("aalphad"+aCycleP.times(new RealMatrix( CycleUtility.periods(alphaOnDual.re,basisOnDual) ).transpose())); // 0
		System.out.println("balphad"+bCycleP.times(new RealMatrix( CycleUtility.periods(alphaOnDual.re,basisOnDual) ).transpose())); // Id

		RealMatrix starAlphaOnGraph = new RealMatrix( DiscreteRiemannUtility.star(dcs, alphaOnGraph.re));
//		System.out.println(starAlphaOnGraph.numCols+"x"+starAlphaOnGraph.numRows); // Exg	
		
		RealMatrix unnormalizedStarAlphaOnGraphPeriods = new RealMatrix( CycleUtility.periods(starAlphaOnGraph.re,basisOnDual) );
//		System.out.println(aCycleP.numRows+"x"+aCycleP.numCols); // gx2g
		RealMatrix starAlphaOnGraphAPeriods = aCycleP.times(unnormalizedStarAlphaOnGraphPeriods.transpose());
//		System.out.println(starAlphaOnGraphAPeriods.numRows+"x"+starAlphaOnGraphAPeriods.numCols); //gxg
		RealMatrix starAlphaOnGraphBPeriods = bCycleP.times(unnormalizedStarAlphaOnGraphPeriods.transpose());
		
		RealMatrix C = starAlphaOnGraphAPeriods;
		RealMatrix B = starAlphaOnGraphBPeriods;
		
		System.out.println("C="+C);
		System.out.println("B="+B);
		RealMatrix starAlphaOnDual = new RealMatrix( DiscreteRiemannUtility.starDual(dcs, alphaOnDual.re));
		
		RealMatrix unnormalizedStarAlphaOnDualPeriods = new RealMatrix( CycleUtility.periods(starAlphaOnDual.re,basisOnGraph) );
			
		RealMatrix starAlphaOnDualAPeriods = aCycleP.times(unnormalizedStarAlphaOnDualPeriods.transpose());
		RealMatrix starAlphaOnDualBPeriods = bCycleP.times(unnormalizedStarAlphaOnDualPeriods.transpose());
		
		RealMatrix Cd = starAlphaOnDualAPeriods;
		RealMatrix Bd = starAlphaOnDualBPeriods;

		System.out.println("Cd="+Cd);
		System.out.println("Bd="+Bd);
		
		RealMatrix starBetaOnGraph  = new RealMatrix( DiscreteRiemannUtility.star(dcs, betaOnGraph .re));
		
		RealMatrix unnormalizedStarBetaPeriods  = new RealMatrix( CycleUtility.periods(starBetaOnGraph.re,basisOnDual) );
		
		RealMatrix starBetaAPeriods  = aCycleP.times(unnormalizedStarBetaPeriods .transpose());
		RealMatrix starBetaBPeriods  = bCycleP.times(unnormalizedStarBetaPeriods .transpose());	
		RealMatrix A = starBetaAPeriods;
		
		System.out.println(A);
		System.out.println("A.B"+A.times(B)); 
		System.out.println(B.transpose().times(C)); // Symetric
		
		periodMatrixOnGraph = new ComplexMatrix( C.invert().times(B).times(-1).re,   Cd.invert().re );
		periodMatrixOnDual  = new ComplexMatrix( Cd.invert().times(Bd).times(-1).re, C.invert().re );
	}

	void computeHarmonicFunctions() {

		harmonicsOnGraph.clear();
		harmonicsOnDual.clear();
		
		for( List quadCycle : quadBasis ) {	
			System.out.println( quadBasis.indexOf(quadCycle)+"th cycle.");
				harmonicsOnGraph.add( new HarmonicFunction.OnGraph( dcs, quadCycle) );	
				harmonicsOnDual .add( new HarmonicFunction.OnDual(  dcs, quadCycle) );
			
		}
	}
	
	void computeHarmonicDifferentials() {

		omegaOnGraph = new RealMatrix(quadBasis.size(), dcs.getNumEdges() ); //TODO: this could be done in the constructor
		omegaOnDual  = new RealMatrix(quadBasis.size(), dcs.getNumEdges() );
		
		for( int k=0; k<quadBasis.size(); k++ ) {
				
			HarmonicFunction.OnGraph onGraph = harmonicsOnGraph.get(k);
			HarmonicFunction.OnDual  onDual  = harmonicsOnDual .get(k);
			
			onGraph.computeHarmonic1Form(omegaOnGraph.re[k]);
			onDual .computeHarmonic1Form(omegaOnDual .re[k]);
		}
	}
	
	void computeNormalizedCyclesAndDifferentials() {
		
		IntegerMatrix im = IntegerMatrix.round( 
				new RealMatrix( CycleUtility.periods(omegaOnGraph.re, basisOnGraph) ) );
		System.out.println("periods"+ im );
		
		assert im.plus(im.transpose()).normSqr() < 1e-5; // antisym
	
		IntegerMatrix P       = HomologyUtility.createNormalizedBasis( im );
		System.out.println("Normalization"+ P );
	
		IntegerMatrix Pt = P.transpose();
		System.out.println(P.times(im.times(Pt)));
		
		aCycleP = HomologyUtility.extractACycles(P);  // gx2g matrices
		bCycleP = HomologyUtility.extractBCycles(P);
		
//		System.out.println("P=\n"+P);
//		System.out.println("aCycleP=\n"+aCycleP);
//		System.out.println("bCycleP =\n"+bCycleP);
		
		alphaOnGraph = aCycleP.times(omegaOnGraph); // gxE matrices
		betaOnGraph  = bCycleP.times(omegaOnGraph);
	
		alphaOnDual  = aCycleP.times(omegaOnDual);
		betaOnDual   = bCycleP.times(omegaOnDual);
		
//		im.assignTimes(Pt);
//		System.out.println("aog:"+aCycleP.times(im));
//		System.out.println("bog:"+bCycleP.times(im));
		
	}


	void computeBasis() {
		V root = dcs.getVertex(0);

		basisOnGraph =  HomotopyUtility.homotopyBasis(root);
		
		quadBasis   = CycleUtility.cyclesToQuads(basisOnGraph);
		basisOnDual = CycleUtility.quadsToDualCycles(quadBasis);
	}

	void computeSpanningTree() {
		V root = dcs.getVertex(0);

		spanningTree = HomotopyUtility.spanningTree(root);
	}
	
	void computePeriodMatrixDebug() {
		
		RealMatrix unnormalizedAlphaPeriods = new RealMatrix( CycleUtility.periods(alphaOnGraph.re, basisOnGraph) );
		RealMatrix unnormalizedBetaPeriods  = new RealMatrix( CycleUtility.periods(betaOnGraph .re, basisOnGraph) );
		
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
		
		RealMatrix starAlpha = new RealMatrix( DiscreteRiemannUtility.star(dcs, alphaOnGraph.re));
		RealMatrix starBeta  = new RealMatrix( DiscreteRiemannUtility.star(dcs, betaOnGraph .re));

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
		//HalfEdgeDataStructure ds = discreteSchottky.g; 
		//compute( discreteSchottky, 1 );//discreteSchottky.rho);

		HalfEdgeDataStructure ds = SilholsGenusExamples.create4QuadExample(ConfVertex.class,ConfEdge.class,ConfFace.class);
		
		//HalfEdgeDataStructure ds = TorusUtility.createTorusNew( 10, 10, 0, ConfVertex.class,ConfEdge.class,ConfFace.class);
		
		double t = System.currentTimeMillis();
		
		DiscreteRiemann dr = new DiscreteRiemann( new DiscreteConformalStructure(ds),1);
		
		System.out.println( dr.getPeriodMatrix() );
		
		System.out.println( System.currentTimeMillis() - t );
		
		for( int i=0; i<4; i++ ) {
			
			dr = SubdivisionUtility.createSubdivisionOfDiscreteRiemann(dr);
			
			System.out.println( dr.getPeriodMatrix() );
			
			System.out.println( System.currentTimeMillis() - t );
		}

	}
}
