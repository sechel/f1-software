package discreteRiemann;

import halfedge.frontend.StandardEditor;

import java.util.List;


import junit.framework.TestCase;
import de.jtem.mfc.field.Complex;
import discreteRiemann.DiscreteConformalStructure.ConfEdge;
import discreteRiemann.DiscreteConformalStructure.ConfFace;
import discreteRiemann.DiscreteConformalStructure.ConfVertex;

//TODO: test signs by flipping edges and inverting cycle

public class DirichletFunctionalTest extends TestCase {

	
	/*
	 * Test method for 'discreteRiemann.DirichletFunctional.eval(double[], double[])'
	 */
	public void testEvalTorusQuad() {
		int m=11, n=7;
		for(int twist = m; twist>=0; twist--){
		DiscreteConformalStructure<ConfVertex, ConfEdge, ConfFace> dcs  
			= new DiscreteConformalStructure<ConfVertex, ConfEdge, ConfFace>
		(TorusUtility.createTorusNew(m,n,twist,ConfVertex.class, ConfEdge.class, ConfFace.class));
		DirichletFunctional.Factory<ConfVertex, ConfEdge, ConfFace> factory = new DirichletFunctional.Factory(dcs);
		
		ConfVertex v = dcs.getVertex(0);
		
		List<List<ConfEdge>> basis =  HomotopyUtility.homotopyBasis(v);
		
		List<ConfEdge> alpha  = CycleUtility.cycleToQuad(basis.get(0));
		List<ConfEdge> beta = CycleUtility.cycleToQuad(basis.get(1));
	
		factory.setRho(1);
		
		factory.setQuadCycle(alpha);
		factory.update();
		
		double [] omegaAlpha     = CycleUtility.grad(    dcs, factory.f .f, factory.f .eps );
		double [] omegaAlphaDual = CycleUtility.gradDual(dcs, factory.fs.f, factory.fs.eps );
		
		double [] starOmegaAlpha     = DiscreteRiemannUtility.star    (dcs, omegaAlpha);
		double [] starOmegaAlphaDual = DiscreteRiemannUtility.starDual(dcs, omegaAlphaDual);
		
		for( int i=0; i<omegaAlphaDual.length; i++ )
			System.out.println(omegaAlphaDual[i]);
		
		factory.setQuadCycle(beta);
		factory.update();
		
		double [] omegaBeta     = CycleUtility.grad(    dcs, factory.f .f, factory.f .eps );
		double [] omegaBetaDual = CycleUtility.gradDual(dcs, factory.fs.f, factory.fs.eps );
		
		double [] starOmegaBeta     = DiscreteRiemannUtility.star    (dcs, omegaBeta );
		double [] starOmegaBetaDual = DiscreteRiemannUtility.starDual(dcs, omegaBetaDual);
		
		List<ConfEdge> alphaCycle     = CycleUtility.quadToCycle    (beta);
		List<ConfEdge> alphaCycleDual = CycleUtility.quadToDualCycle(beta);
		List<ConfEdge> betaCycle      = CycleUtility.quadToCycle    (alpha);
		List<ConfEdge> betaCycleDual  = CycleUtility.quadToDualCycle(alpha);
		
		assertEquals( alphaCycle.get(0).getStartVertex() , alphaCycle.get(alphaCycle.size()-1).getTargetVertex());
		assertEquals(alphaCycleDual.get(0).getRightFace() , alphaCycleDual.get(alphaCycleDual.size()-1).getLeftFace());
		assertEquals( betaCycle.get(0).getStartVertex() , betaCycle.get(betaCycle.size()-1).getTargetVertex());
		assertEquals( betaCycleDual.get(0).getRightFace() , betaCycleDual.get(betaCycleDual.size()-1).getLeftFace());

		
		double shiftAlphaDual       = CycleUtility.integrate(starOmegaAlpha, alphaCycleDual); //PseudoPeriodicRealFunction.shift( OmegaAlphaDual     );
		double shiftBetaDual        = CycleUtility.integrate(starOmegaBeta,  alphaCycleDual); //PseudoPeriodicRealFunction.shift( OmegaBetaDual      );
		
		double secondShiftAlphaDual = CycleUtility.integrate(starOmegaAlpha, betaCycleDual);  //PseudoPeriodicRealFunction.secondShift( OmegaAlphaDual );
		double secondShiftBetaDual  = CycleUtility.integrate(starOmegaBeta,  betaCycleDual);  //PseudoPeriodicRealFunction.secondShift( OmegaBetaDual      );
	
		Complex tau = new Complex( - shiftAlphaDual / shiftBetaDual, secondShiftAlphaDual - secondShiftBetaDual * shiftAlphaDual / shiftBetaDual );
		
		System.out.println( tau );
		//System.out.println( CycleUtility.integrate(alpha,TorusUtility.getCycle(e)));
		
		}
	}
	/*
	 * 
	 */
	public void testLaplacian() {
		int m=5, n=7;
		double[]grad = new double[m*n]; // Vertices
		double[] dsdf = new double[m*n];
		int [][] eps = new int[2][];
		eps[0] = new int[4*m*n]; // Oriented Edges
		eps[1] = new int[4*m*n];
		double[] df = new double[4*m*n];
		double[] stardf = new double[4*m*n];

		
		
		for(int twist=m; twist>=0; twist--){
			DiscreteConformalStructure<ConfVertex, ConfEdge, ConfFace> dcs  
			= new DiscreteConformalStructure<ConfVertex, ConfEdge, ConfFace>
			(TorusUtility.createTorusNew(m,n,twist,ConfVertex.class, ConfEdge.class, ConfFace.class));

			DirichletFunctional.Factory<ConfVertex, ConfEdge, ConfFace> factory = 
				new DirichletFunctional.Factory(dcs);
			
			
			double[] f = factory.f.f;
			for (int i = 0; i< factory.f.getNumberOfVariables(); i++)
				f[i] = Math.random();

			ConfEdge e = dcs.getEdge(0);
			
			List<List<ConfEdge>> basis =  HomotopyUtility.homotopyBasis(e.getStartVertex());
			
			List<ConfEdge> alpha  = CycleUtility.cycleToQuad(basis.get(0));
			List<ConfEdge> beta = CycleUtility.cycleToQuad(basis.get(1));
			
			factory.setRho(1);
			
			factory.setQuadCycle(alpha);
			CycleUtility.computeEps(alpha, eps);
			
			factory.f.eval(f, grad);
			

			CycleUtility.grad(dcs, f, eps[0], df);
			DiscreteRiemannUtility.star(dcs, df, stardf);
			CycleUtility.div(dcs, stardf, dsdf);	
			for(ConfVertex v: dcs.vertexList) {
				double gr = 0;
				for(ConfEdge se: v.getEdgeStar())
					gr += df[se.getIndex()]*se.getRho();
				assertEquals(dsdf[v.getIndex()], gr);
				if (v.getIndex() != 0) assertTrue(
						Math.abs(grad[v.getIndex()]- 2*gr)<1e-9);
			}
			
			factory.setQuadCycle(beta);
			CycleUtility.computeEps(beta, eps);

			factory.f.eval(f, grad);
			CycleUtility.grad(dcs, f, eps[0], df);
			DiscreteRiemannUtility.star(dcs, df, stardf);
			CycleUtility.div(dcs, stardf, dsdf);	
			
			for(ConfVertex v: dcs.vertexList) {
				double gr = 0;
				for(ConfEdge se: v.getEdgeStar())
					gr += df[se.getIndex()]*se.getRho();
				assertEquals(dsdf[v.getIndex()], gr);
				if (v.getIndex() != 0) assertTrue(
						Math.abs(grad[v.getIndex()]- 2*gr)<1e-9);
			}

			factory.setQuadCycle(alpha);
			factory.update();
			
			CycleUtility.computeEps(alpha, eps);
			
			factory.f.eval(f, grad);
			

			CycleUtility.grad(dcs, f, eps[0], df);
			DiscreteRiemannUtility.star(dcs, df, stardf);
			CycleUtility.div(dcs, stardf, dsdf);	
			
			for(ConfVertex v: dcs.vertexList) {
				double gr = 0;
				for(ConfEdge se: v.getEdgeStar())
					gr += df[se.getIndex()]*se.getRho();
				assertEquals(dsdf[v.getIndex()], gr);
				if (v.getIndex() != 0) assertTrue(
						Math.abs(grad[v.getIndex()]- 2*gr)<1e-9);
			}
			
			factory.setQuadCycle(beta);
			factory.update();
			
			CycleUtility.computeEps(beta, eps);

			factory.f.eval(f, grad);
			CycleUtility.grad(dcs, f, eps[0], df);
			DiscreteRiemannUtility.star(dcs, df, stardf);
			CycleUtility.div(dcs, stardf, dsdf);	
			
			for(ConfVertex v: dcs.vertexList) {
				double gr = 0;
				for(ConfEdge se: v.getEdgeStar())
					gr += df[se.getIndex()]*se.getRho();
				assertEquals(dsdf[v.getIndex()], gr);
				if (v.getIndex() != 0) assertTrue(
						Math.abs(grad[v.getIndex()]- 2*gr)<1e-9);
			}
			
		}
			
	}
	/*
	 * Test method for 'discreteRiemann.DirichletFunctional.eval(double[], double[])'
	 */
	public void testEvalTorusTri() {
		int m=13, n=11;
		for(int twist=m; twist>=0; twist--){
			DiscreteConformalStructure<ConfVertex, ConfEdge, ConfFace> dcs  
			= new DiscreteConformalStructure<ConfVertex, ConfEdge, ConfFace>
			(TorusUtility.createTorusNew(m,n,twist,ConfVertex.class, ConfEdge.class, ConfFace.class));
			DirichletFunctional.Factory<ConfVertex, ConfEdge, ConfFace> factory = 
				new DirichletFunctional.Factory(dcs);
			
			ConfEdge e = dcs.getEdge(0);
			
			List<List<ConfEdge>> basis =  HomotopyUtility.homotopyBasis(e.getStartVertex());
			
			List<ConfEdge> alpha  = CycleUtility.cycleToQuad(basis.get(0));
			List<ConfEdge> beta = CycleUtility.cycleToQuad(basis.get(1));
			
			factory.setRho(1);
			
			factory.setQuadCycle(alpha);
			factory.update();
			
			double [] omegaAlpha     = CycleUtility.grad(    dcs, factory.f .f, factory.f .eps );
			double [] omegaAlphaDual = CycleUtility.gradDual(dcs, factory.fs.f, factory.fs.eps );
			
			double [] starOmegaAlpha     = DiscreteRiemannUtility.star    (dcs, omegaAlpha);
			double [] starOmegaAlphaDual = DiscreteRiemannUtility.starDual(dcs, omegaAlphaDual);
			
			for( int i=0; i<omegaAlphaDual.length; i++ )
				System.out.println(omegaAlphaDual[i]);
			
			factory.setQuadCycle(beta);
			factory.update();
			
			double [] omegaBeta     = CycleUtility.grad(    dcs, factory.f .f, factory.f .eps );
			double [] omegaBetaDual = CycleUtility.gradDual(dcs, factory.fs.f, factory.fs.eps );
			
			double [] starOmegaBeta     = DiscreteRiemannUtility.star    (dcs, omegaBeta);
			double [] starOmegaBetaDual = DiscreteRiemannUtility.starDual(dcs, omegaBetaDual);
			
			List<ConfEdge> alphaCycle     = CycleUtility.quadToCycle    (beta);
			List<ConfEdge> alphaCycleDual = CycleUtility.quadToDualCycle(beta);
			List<ConfEdge> betaCycle      = CycleUtility.quadToCycle    (alpha);
			List<ConfEdge> betaCycleDual  = CycleUtility.quadToDualCycle(alpha);
			
			assertTrue(HomotopyUtility.isClosed(alphaCycle));
			assertTrue(HomotopyUtility.isDualClosed(alphaCycleDual));
			assertTrue(HomotopyUtility.isClosed(betaCycle));
			assertTrue(HomotopyUtility.isDualClosed(betaCycleDual));
			
			
//			System.out.println( "wa a : " + CycleUtility.integrate( omegaAlpha, alphaCycle ) );
//			System.out.println( "wa b : " + CycleUtility.integrate( omegaAlpha,  betaCycle  ) );
//			System.out.println( "wb a : " + CycleUtility.integrate( omegaBeta,  alphaCycle ) );
//			System.out.println( "wb b : " + CycleUtility.integrate( omegaBeta,   betaCycle  ) );
//			
//			System.out.println( "wa* a* : " + CycleUtility.integrate( omegaAlphaDual, alphaCycleDual ) );
//			System.out.println( "wa* b* : " + CycleUtility.integrate( omegaAlphaDual,  betaCycleDual  ) );
//			System.out.println( "wb* a* : " + CycleUtility.integrate( omegaBetaDual,  alphaCycleDual ) );
//			System.out.println( "wb* b* : " + CycleUtility.integrate( omegaBetaDual,   betaCycleDual  ) );
			
			double shiftAlphaDual       = CycleUtility.integrate(starOmegaAlpha, alphaCycleDual); //PseudoPeriodicRealFunction.shift( OmegaAlphaDual     );
			double shiftBetaDual        = CycleUtility.integrate(starOmegaBeta,  alphaCycleDual); //PseudoPeriodicRealFunction.shift( OmegaBetaDual      );
			
			double secondShiftAlphaDual = CycleUtility.integrate(starOmegaAlpha, betaCycleDual);  //PseudoPeriodicRealFunction.secondShift( OmegaAlphaDual );
			double secondShiftBetaDual  = CycleUtility.integrate(starOmegaBeta,  betaCycleDual);  //PseudoPeriodicRealFunction.secondShift( OmegaBetaDual      );
			
			Complex tau = new Complex( - shiftAlphaDual / shiftBetaDual, secondShiftAlphaDual - secondShiftBetaDual * shiftAlphaDual / shiftBetaDual );
			
			System.out.println( tau );
			//System.out.println( CycleUtility.integrate(alpha,TorusUtility.getCycle(e)));
		}
	}

	/*
	 * Test method for 'discreteRiemann.DirichletFunctional.eval(double[], double[])'
	 */
	public void testEvalTorusSchottky() {
		int m=11, n=4;
		
		DiscreteSchottkyOld discreteSchottky = DiscreteSchottkyOld.createSchottkyExample(1);
		DiscreteConformalStructure<ConfVertex, ConfEdge, ConfFace> dcs  
		= new DiscreteConformalStructure<ConfVertex, ConfEdge, ConfFace>
			(discreteSchottky.g);
		
		DirichletFunctional.Factory factory = new DirichletFunctional.Factory(dcs);
		
		ConfEdge e = dcs.getEdge(0);
		
		
		List<List<ConfEdge>> basis =  HomotopyUtility.homotopyBasis(e.getStartVertex());
	
		List<ConfEdge> alpha  = CycleUtility.cycleToQuad(basis.get(0));
		List<ConfEdge> beta = CycleUtility.cycleToQuad(basis.get(1));
	
		factory.setRho(discreteSchottky.rho);
		
		factory.setQuadCycle(alpha);
		factory.update();
		
		double [] omegaAlpha     = CycleUtility.grad(    dcs, factory.f .f, factory.f .eps );
		double [] omegaAlphaDual = CycleUtility.gradDual(dcs, factory.fs.f, factory.fs.eps );
		
		double [] starOmegaAlpha     = DiscreteRiemannUtility.star    (dcs, omegaAlpha);
		double [] starOmegaAlphaDual = DiscreteRiemannUtility.starDual(dcs, omegaAlphaDual);
		
		for( int i=0; i<omegaAlphaDual.length; i++ )
			System.out.println(omegaAlphaDual[i]);
		
		factory.setQuadCycle(beta);
		factory.update();
		
		double [] omegaBeta     = CycleUtility.grad(    dcs, factory.f .f, factory.f .eps );
		double [] omegaBetaDual = CycleUtility.gradDual(dcs, factory.fs.f, factory.fs.eps );
		
		double [] starOmegaBeta     = DiscreteRiemannUtility.star    (dcs, omegaBeta);
		double [] starOmegaBetaDual = DiscreteRiemannUtility.starDual(dcs, omegaBetaDual);
		
		List<ConfEdge> alphaCycle     = CycleUtility.quadToCycle    (beta);
		List<ConfEdge> alphaCycleDual = CycleUtility.quadToDualCycle(beta);
		List<ConfEdge> betaCycle      = CycleUtility.quadToCycle    (alpha);
		List<ConfEdge> betaCycleDual  = CycleUtility.quadToDualCycle(alpha);
		
		assertTrue(HomotopyUtility.isClosed(alphaCycle));
		assertTrue(HomotopyUtility.isDualClosed(alphaCycleDual));
		assertTrue(HomotopyUtility.isClosed(betaCycle));
		assertTrue(HomotopyUtility.isDualClosed(betaCycleDual));
		
		
		System.out.println( "wa a : " + CycleUtility.integrate( omegaAlpha, alphaCycle ) );
		System.out.println( "wa b : " + CycleUtility.integrate( omegaAlpha,  betaCycle  ) );
		System.out.println( "wb a : " + CycleUtility.integrate( omegaBeta,  alphaCycle ) );
		System.out.println( "wb b : " + CycleUtility.integrate( omegaBeta,   betaCycle  ) );
		
		System.out.println( "wa* a* : " + CycleUtility.integrate( omegaAlphaDual, alphaCycleDual ) );
		System.out.println( "wa* b* : " + CycleUtility.integrate( omegaAlphaDual,  betaCycleDual  ) );
		System.out.println( "wb* a* : " + CycleUtility.integrate( omegaBetaDual,  alphaCycleDual ) );
		System.out.println( "wb* b* : " + CycleUtility.integrate( omegaBetaDual,   betaCycleDual  ) );
		
		double shiftAlphaDual       = CycleUtility.integrate(starOmegaAlpha, alphaCycleDual); //PseudoPeriodicRealFunction.shift( OmegaAlphaDual     );
		double shiftBetaDual        = CycleUtility.integrate(starOmegaBeta,  alphaCycleDual); //PseudoPeriodicRealFunction.shift( OmegaBetaDual      );
		
		double secondShiftAlphaDual = CycleUtility.integrate(starOmegaAlpha, betaCycleDual);  //PseudoPeriodicRealFunction.secondShift( OmegaAlphaDual );
		double secondShiftBetaDual  = CycleUtility.integrate(starOmegaBeta,  betaCycleDual);  //PseudoPeriodicRealFunction.secondShift( OmegaBetaDual      );
	
		Complex tau = new Complex( - shiftAlphaDual / shiftBetaDual, secondShiftAlphaDual - secondShiftBetaDual * shiftAlphaDual / shiftBetaDual );
		
		System.out.println( tau );
		//System.out.println( CycleUtility.integrate(alpha,TorusUtility.getCycle(e)));
		
		
		
		
	}
	
	public void testDual() {
		int m=3, n=4;
		for(int twist = m; twist>=0; twist--){
		DiscreteConformalStructure<ConfVertex, ConfEdge, ConfFace> dcs  
			= new DiscreteConformalStructure<ConfVertex, ConfEdge, ConfFace>
		(
				SubdivisionUtility.createSubdivisionOfGraph( TorusUtility.createTorusNew(m,n,twist,ConfVertex.class, ConfEdge.class, ConfFace.class)));
		DirichletFunctional.Factory<ConfVertex, ConfEdge, ConfFace> factory = new DirichletFunctional.Factory(dcs);
		
		ConfVertex v = dcs.getVertex(0);
		
		List<List<ConfEdge>> homotopyBasis = HomotopyUtility.homotopyBasis(v);
		List<List<ConfEdge>> quadBasis =  CycleUtility.cyclesToQuads(homotopyBasis);
		
		List<ConfEdge> alpha  = (quadBasis.get(0));
		List<ConfEdge> beta = (quadBasis.get(1));
	
		factory.setRho(1);
		
	
		double[][] form = HarmonicUtility.dualHarmonicForm(alpha, factory);
	
		
		assertTrue(
				Math.abs(CycleUtility.integrate(form[0], 
				CycleUtility.quadToCycle(alpha))) < 1e-9);	
		assertTrue(
				Math.abs(CycleUtility.integrate(form[1], 
				CycleUtility.quadToDualCycle(alpha))) < 1e-9);	
		assertTrue(
				Math.abs(Math.abs(CycleUtility.integrate(form[0], 
				CycleUtility.quadToCycle(beta)))-1.) < 1e-9);	
		assertTrue(
				Math.abs(Math.abs(CycleUtility.integrate(form[1], 
				CycleUtility.quadToDualCycle(beta)))-1.) < 1e-9);	
		
		form = HarmonicUtility.dualHarmonicForm(beta, factory);
	
		
		assertTrue(
				Math.abs(Math.abs(CycleUtility.integrate(form[0], 
				CycleUtility.quadToCycle(alpha)))-1.) < 1e-9);	
		assertTrue(
				Math.abs(Math.abs(CycleUtility.integrate(form[1], 
				CycleUtility.quadToDualCycle(alpha)))-1.) < 1e-9);	
		assertTrue(
				Math.abs(CycleUtility.integrate(form[0], 
				CycleUtility.quadToCycle(beta))) < 1e-9);	
		assertTrue(
				Math.abs(CycleUtility.integrate(form[1], 
				CycleUtility.quadToDualCycle(beta))) < 1e-9);	
		}
	
}
	
	public static void main(String[] arg) {
		int m=4, n=5;
		for(int twist = 0; twist>=0; twist--){
		DiscreteConformalStructure<ConfVertex, ConfEdge, ConfFace> dcs  
			= new DiscreteConformalStructure<ConfVertex, ConfEdge, ConfFace>
		(SubdivisionUtility.createSubdivisionOfGraph( 
				TorusUtility.createTorusNew(m,n,twist,ConfVertex.class, ConfEdge.class, ConfFace.class)));
		 dcs  
		= new DiscreteConformalStructure<ConfVertex, ConfEdge, ConfFace>
	( 
			TorusUtility.createTriangularTorus(m,n,twist,ConfVertex.class, ConfEdge.class, ConfFace.class));

		 dcs = new DiscreteConformalStructure<ConfVertex, ConfEdge, ConfFace>
			(SilholsGenusExamples.create3QuadExample(ConfVertex.class, ConfEdge.class, ConfFace.class));
		
		DirichletFunctional.Factory<ConfVertex, ConfEdge, ConfFace> factory = new DirichletFunctional.Factory(dcs);
		
		ConfVertex v = dcs.getVertex(0);
		
		List<List<ConfEdge>> homotopyBasis = HomotopyUtility.homotopyBasis(v);
		List<List<ConfEdge>> quadBasis =  CycleUtility.cyclesToQuads(homotopyBasis);
		
		List<ConfEdge> alpha  = (quadBasis.get(0));
		List<ConfEdge> beta = (quadBasis.get(2));
	
		assertEquals(quadBasis.size(), 4);
		System.out.println(" "+alpha.size()+beta.size()+quadBasis.get(1).size()+quadBasis.get(3).size());
		factory.setRho(1);
		
	
		double[][] form = HarmonicUtility.dualHarmonicForm(alpha, factory);
		for(ConfVertex vp: dcs.vertexList) {
			vp.p.set(factory.f.f[vp.getIndex()],0.);
		}
		
		assertTrue(
				Math.abs(CycleUtility.integrate(form[0], 
				CycleUtility.quadToCycle(alpha))) < 1e-9);	
		assertTrue(
				Math.abs(CycleUtility.integrate(form[1], 
				CycleUtility.quadToDualCycle(alpha))) < 1e-9);	
//if(Math.abs(Math.abs(CycleUtility.integrate(form[0], 
//		CycleUtility.quadToCycle(beta)))-1.) > 1e-9)

//		assertTrue(
//				Math.abs(Math.abs(CycleUtility.integrate(form[0], 
//				CycleUtility.quadToCycle(beta)))-1.) < 1e-9);	
//		assertTrue(
//				Math.abs(Math.abs(CycleUtility.integrate(form[1], 
//				CycleUtility.quadToDualCycle(beta)))-1.) < 1e-9);	
		
		form = HarmonicUtility.dualHarmonicForm(beta, factory);
		for(ConfVertex vp: dcs.vertexList) {
			vp.p.set(vp.p.x,factory.f.f[vp.getIndex()]);
		}

		form = HarmonicUtility.dualHarmonicForm(quadBasis.get(3), factory);
		for(ConfVertex vp: dcs.vertexList) {
			vp.p.set(vp.p.x+ 0.5*factory.f.f[vp.getIndex()],vp.p.y);
		}
	
		
		form = HarmonicUtility.dualHarmonicForm(quadBasis.get(1), factory);
		for(ConfVertex vp: dcs.vertexList) {
			vp.p.set(vp.p.x,vp.p.y + 0.5*factory.f.f[vp.getIndex()]);
		}
	

		DiscreteRiemannUtility.show(dcs);
		
		assertTrue(
				Math.abs(Math.abs(CycleUtility.integrate(form[0], 
				CycleUtility.quadToCycle(alpha)))-1.) < 1e-9);	
		assertTrue(
				Math.abs(Math.abs(CycleUtility.integrate(form[1], 
				CycleUtility.quadToDualCycle(alpha)))-1.) < 1e-9);	
		assertTrue(
				Math.abs(CycleUtility.integrate(form[0], 
				CycleUtility.quadToCycle(beta))) < 1e-9);	
		assertTrue(
				Math.abs(CycleUtility.integrate(form[1], 
				CycleUtility.quadToDualCycle(beta))) < 1e-9);	
		}
	
}
}
