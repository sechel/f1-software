package discreteRiemann;

import halfedge.HalfEdgeDataStructure;

import java.util.List;

import de.jtem.blas.ComplexMatrix;
import de.jtem.blas.IntegerMatrix;
import de.jtem.blas.RealMatrix;
import de.jtem.mfc.field.Complex;
import de.jtem.riemann.theta.SiegelReduction;
import discreteRiemann.DiscreteConformalStructure.ConfEdge;
import discreteRiemann.DiscreteConformalStructure.ConfFace;
import discreteRiemann.DiscreteConformalStructure.ConfVertex;
import junit.framework.TestCase;

public class DiscreteRiemannUtilityTest extends TestCase {

	public void testShow() {
//		int m=11, n=17;
//		int twist = 2;
//		
//		DiscreteRiemann<ConfVertex, ConfEdge, ConfFace> s  
//		= new DiscreteRiemann<ConfVertex, ConfEdge, ConfFace>
//		(TorusUtility.createTorusNew(m,n,twist,ConfVertex.class, ConfEdge.class, ConfFace.class));
	
		DiscreteSchottkyOld discreteSchottky = DiscreteSchottkyOld.createSchottkyExample(2);

		HalfEdgeDataStructure dcs = discreteSchottky.g;
			
//		HalfEdgeDataStructure dcs = (SilholsGenusExamples.create3QuadExample(ConfVertex.class,ConfEdge.class,ConfFace.class));

//		HalfEdgeDataStructure dcs = (SilholsGenusExamples.create4QuadExample(ConfVertex.class,ConfEdge.class,ConfFace.class));
	
		for( int i=0; i<1; i++ ) {
			dcs = SubdivisionUtility.createSubdivisionOfGraph(dcs);
		}
		
		DiscreteRiemann<ConfVertex, ConfEdge, ConfFace> s = 
			new DiscreteRiemann<ConfVertex, ConfEdge, ConfFace>(new DiscreteConformalStructure(dcs));
		
		s.setRho(1);
		
		RealMatrix starAlphaOnGraph = new RealMatrix( DiscreteRiemannUtility.star(s.dcs, s.alphaOnGraph.re));

		RealMatrix unnormalizedStarAlphaOnGraphPeriods = new RealMatrix( 
				CycleUtility.periods(starAlphaOnGraph.re, s.basisOnDual) );

		RealMatrix starAlphaOnGraphAPeriods = s.aCycleP.times(unnormalizedStarAlphaOnGraphPeriods.transpose());
		RealMatrix starAlphaOnGraphBPeriods = s.bCycleP.times(unnormalizedStarAlphaOnGraphPeriods.transpose());

		RealMatrix Cinv = starAlphaOnGraphAPeriods.invert();
		RealMatrix B = starAlphaOnGraphBPeriods;

//		ComplexMatrix periodMatrixOnDual = new ComplexMatrix( C.invert().times(B).times(-1).re, C.invert().re );

	
		RealMatrix starAlphaOnDual = new RealMatrix( DiscreteRiemannUtility.starDual(s.dcs, s.alphaOnDual.re));

		RealMatrix unnormalizedStarAlphaOnDualPeriods = new RealMatrix( 
				CycleUtility.periods(starAlphaOnDual.re, s.basisOnGraph) );

		RealMatrix starAlphaOnDualAPeriods = s.aCycleP.times(unnormalizedStarAlphaOnDualPeriods.transpose());
		RealMatrix starAlphaOnDualBPeriods = s.bCycleP.times(unnormalizedStarAlphaOnDualPeriods.transpose());

		RealMatrix CDualinv = starAlphaOnDualAPeriods.invert();
		RealMatrix BDual = starAlphaOnDualBPeriods;

		ComplexMatrix zetasOnGraph = new ComplexMatrix(CDualinv.times(starAlphaOnDual).times(-1).re,
				Cinv.times(s.alphaOnGraph).re);
	
//		// SilholsGenusExamples.create3QuadExample(ConfVertex.class,ConfEdge.class,ConfFace.class)
//		DiscreteRiemannUtility.integrate(s, zetasOnGraph.getRow(0).times(new Complex(0,1)).minus(zetasOnGraph.getRow(1)).toArray());

		// SilholsGenusExamples.create4QuadExample(ConfVertex.class,ConfEdge.class,ConfFace.class)
		DiscreteRiemannUtility.integrate(s, zetasOnGraph.getRow(0).toArray());
		
		DiscreteRiemannUtility.show(s.dcs);
		int i = 1;
		DiscreteRiemannUtility.integrate(s, zetasOnGraph.getRow(1).toArray());
	
//		DiscreteRiemannUtility.integrate(s, zetasOnGraph.getRow(0).times(new Complex(0,3)).minus(zetasOnGraph.getRow(1)).toArray());

		DiscreteRiemannUtility.show(s.dcs);

		i += 1;
//		periodMatrixOnGraph = new ComplexMatrix( C.invert().times(B).times(-1).re, C.invert().re );

	}
	
	public void testPeriodMatrices() {
		int m=11, n=17;
		for(int twist = 0; twist<m; twist++){
			DiscreteConformalStructure<ConfVertex, ConfEdge, ConfFace> dcs  
			= new DiscreteConformalStructure<ConfVertex, ConfEdge, ConfFace>
			(TorusUtility.createTorusNew(m,n,twist,ConfVertex.class, ConfEdge.class, ConfFace.class));
			DirichletFunctional.Factory<ConfVertex, ConfEdge, ConfFace> factory = new DirichletFunctional.Factory(dcs);

			ConfVertex v = dcs.getVertex(0);
			double t = System.currentTimeMillis();
			List<List<ConfEdge>> basisOnGraph =  HomotopyUtility.homotopyBasis(v);
			System.out.println( "finished basis:  " + (System.currentTimeMillis()-t));t = System.currentTimeMillis();
			factory.setRho(1);


//			for( List<ConfEdge> cycle : basisOnGraph ) {
//			CycleUtility.printVertices(cycle);
//			}

			List<List<ConfEdge>> quadBasis   = CycleUtility.cyclesToQuads(basisOnGraph);
			List<List<ConfEdge>> basisOnDual = CycleUtility.quadsToDualCycles(quadBasis);

//			for( List<ConfEdge> cycle : basisOnDual ) {
//			CycleUtility.printVertices(cycle);
//			}

			RealMatrix  w = new RealMatrix( HarmonicUtility.cohomologyBasisOnGraph(quadBasis,factory) );

			IntegerMatrix im = IntegerMatrix.round( 
					new RealMatrix( CycleUtility.periods(w.re, basisOnGraph) ) );

			IntegerMatrix P       = HomologyUtility.createNormalizedBasis( im );
			IntegerMatrix aCycleP = HomologyUtility.extractACycles(P);
			IntegerMatrix bCycleP = HomologyUtility.extractBCycles(P);

//			System.out.println("P=\n"+P);
//			System.out.println("aCycleP=\n"+aCycleP);
//			System.out.println("bCycleP =\n"+bCycleP);

			RealMatrix alpha = aCycleP.times(w); // w.times( aCycleP.transpose() );
			RealMatrix beta  = bCycleP.times(w); //.times( bCycleP.transpose() );

			RealMatrix unnormalizedAlphaPeriods = new RealMatrix( CycleUtility.periods(alpha.re,basisOnGraph) );
			RealMatrix unnormalizedBetaPeriods  = new RealMatrix( CycleUtility.periods(beta .re,basisOnGraph) );

			RealMatrix alphaAPeriods = aCycleP.times(unnormalizedAlphaPeriods.transpose());
			RealMatrix alphaBPeriods = bCycleP.times(unnormalizedAlphaPeriods.transpose());
			RealMatrix betaAPeriods  = aCycleP.times(unnormalizedBetaPeriods .transpose());
			RealMatrix betaBPeriods  = bCycleP.times(unnormalizedBetaPeriods .transpose());

//			System.out.println("alphaAPeriods=\n"+alphaAPeriods);
//			System.out.println("alphaBPeriods=\n"+alphaBPeriods);
//			System.out.println("betaAPeriods=\n"+betaAPeriods);
//			System.out.println("betaBPeriods=\n"+betaBPeriods);

			//System.out.println("unnormAlphaPeriods=\n"+unnormalizedAlphaPeriods);
			//System.out.println("unnormBetaPeriods=\n"+unnormalizedBetaPeriods);

			//RealMatrix alphaPeriods = bCycleP.times(unnormalizedAlphaPeriods.transpose());
			//RealMatrix betaPeriods  = aCycleP.times(unnormalizedBetaPeriods .transpose());

			//System.out.println("alphaPeriods=\n"+alphaPeriods);
			//System.out.println("betaPeriods=\n"+betaPeriods);

			RealMatrix starAlpha = new RealMatrix( DiscreteRiemannUtility.star(dcs, alpha.re));
			RealMatrix starBeta  = new RealMatrix( DiscreteRiemannUtility.star(dcs, beta .re));

			RealMatrix unnormalizedStarAlphaPeriods = new RealMatrix( CycleUtility.periods(starAlpha.re,basisOnDual) );
			RealMatrix unnormalizedStarBetaPeriods  = new RealMatrix( CycleUtility.periods(starBeta.re,basisOnDual) );

//			System.out.println( "unnormalizedStarAlphaPeriods=\n"+unnormalizedStarAlphaPeriods );
//			System.out.println( "unnormalizedStarBetaPeriods=\n"+unnormalizedStarBetaPeriods );

			RealMatrix C = aCycleP.times(unnormalizedStarAlphaPeriods.transpose());
			RealMatrix B = bCycleP.times(unnormalizedStarAlphaPeriods.transpose());
			RealMatrix Ba  = aCycleP.times(unnormalizedStarBetaPeriods .transpose());
			RealMatrix A  = bCycleP.times(unnormalizedStarBetaPeriods .transpose());

//			System.out.println("staralphaAPeriods=\n"+C);
//			System.out.println("staralphaBPeriods=\n"+B);
//			System.out.println("starbetaAPeriods=\n"+Ba);
//			System.out.println("starbetaBPeriods=\n"+A);

//			System.out.println("starAlphaAPeriods-1=\n"+C.invert());

			Complex tau = new Complex(C.invert().times(B).times(-1).re[0][0],C.invert().re[0][0]);


			if(twist<=m/2) {
				System.out.println("twist: "+twist+" t: "+(-twist*1./m)+"+"+(n*1./m)+"i tau: "+tau+Math.abs(twist*1./m + tau.re));
				assertTrue(Math.abs(twist*1./m + tau.re)<1e-6);		
			}
			else {
				System.out.println("twist: "+twist+" t: "+((m-twist)*1./m)+"+"+(n*1./m)+"i tau: "+tau+Math.abs((twist-m)*1./m + tau.re));
				assertTrue(Math.abs((twist-m)*1./m + tau.re)<1e-6);
			}

			assertTrue(Math.abs(n*1./m- tau.im)<1e-6);

			System.out.println( "finished matrix:  " + (System.currentTimeMillis()-t));t = System.currentTimeMillis();
		}

	}

	public void testStar() {
		int m=7, n=11;
//		for(int twist = 0; twist<m; twist++)
		int twist = m/3;
		{
			DiscreteConformalStructure<ConfVertex, ConfEdge, ConfFace> dcs  
			= new DiscreteConformalStructure<ConfVertex, ConfEdge, ConfFace>
			(TorusUtility.createTorusNew(m,n,twist,ConfVertex.class, ConfEdge.class, ConfFace.class));
			DirichletFunctional.Factory<ConfVertex, ConfEdge, ConfFace> factory = new DirichletFunctional.Factory(dcs);

			DiscreteRiemannUtility.randomRho(dcs);

			double[] form = new double[dcs.edgeList.size()];

			for (ConfEdge e : dcs.getPositiveEdges()){
				double r = Math.random()-0.5;
				form[e.getIndex()] = r;
				form[e.getOppositeEdge().getIndex()] = -r;
			}

			double[] ssform = DiscreteRiemannUtility.starDual(dcs,DiscreteRiemannUtility.star(dcs, form));

			for (ConfEdge e : dcs.edgeList){
				assertTrue(Math.abs(
						form[e.getIndex()]+ssform[e.getIndex()])<1e-10);
			}
			ssform = DiscreteRiemannUtility.star(dcs,DiscreteRiemannUtility.starDual(dcs, form));

			for (ConfEdge e : dcs.edgeList){
				assertTrue(Math.abs(
						form[e.getIndex()]+ssform[e.getIndex()])<1e-10);
			}

		}



	}

	public void testPeriods() {
		int m=7, n=11;
		for(int twist = 0; twist<m; twist++){
			DiscreteConformalStructure<ConfVertex, ConfEdge, ConfFace> dcs  
			= new DiscreteConformalStructure<ConfVertex, ConfEdge, ConfFace>
			(TorusUtility.createTorusNew(m,n,twist,ConfVertex.class, ConfEdge.class, ConfFace.class));
			DirichletFunctional.Factory<ConfVertex, ConfEdge, ConfFace> factory = new DirichletFunctional.Factory(dcs);

			ConfVertex v = dcs.getVertex(0);

			List<List<ConfEdge>> quadBasis =  CycleUtility.cyclesToQuads(HomotopyUtility.homotopyBasis(v));

			List<ConfEdge> alpha  = (quadBasis.get(0));
			List<ConfEdge> beta = (quadBasis.get(1));

			factory.setRho(1);


			List<double[][]> forms = HarmonicUtility.cohomologyBasis(quadBasis, factory);

			double[][] periods = CycleUtility.periods(forms, quadBasis);
			//System.out.println(new RealMatrix(periods));
			assertEquals(periods.length,forms.size());
			int g = quadBasis.size();
			for(int i = 0; i< periods.length; i++){
				assertEquals(periods[i].length,2*g);
				for(int j = 0; j< g; j++){
					//				System.out.println("tw:"+twist+" i="+i+" j="+j+": "+periods[i][j]+": "+periods[i][j+g]);
					assertTrue(
							Math.abs(Math.abs(periods[i][j])-((i==j)?0.:1.)) < 1e-9);	
					assertTrue(
							Math.abs(periods[i][j+g]-periods[i][j]) < 1e-9);	
				}
			}
		}
	}

}
