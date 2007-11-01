package discreteRiemann;

import halfedge.Edge;
import halfedge.Face;
import halfedge.HalfEdgeDataStructure;
import halfedge.Vertex;

import java.util.Arrays;
import java.util.List;


import discreteRiemann.DiscreteConformalStructure.ConfEdge;
import discreteRiemann.DiscreteConformalStructure.ConfFace;
import discreteRiemann.DiscreteConformalStructure.ConfVertex;

import junit.framework.TestCase;

public class CycleUtilityTest extends TestCase {

	/*
	 * Test method for 'discreteRiemann.CycleUtility.supplementCycle(List<E>) <V, E, F>'
	 */
	/**
	 * 
	 */
	public void testQuadToCycle() {
		int m=23, n=17;
		HalfEdgeDataStructure<Vertex.Generic, Edge.Generic, Face.Generic> heds = 
			TorusUtility.createTorus(m,n,0,Vertex.Generic.class, Edge.Generic.class, Face.Generic.class);
		
		Vertex.Generic v = heds.getVertex(0);
		
		
		
		List<List<Edge.Generic>> basis =  HomotopyUtility.homotopyBasis(v);
		
		assertEquals(basis.size(), 2);
		
		for(List<Edge.Generic> cycle : basis){
			List<Edge.Generic> quads = CycleUtility.cycleToQuad(cycle);
			assertTrue(quads.size() > cycle.size());
			
			List<Edge.Generic> cycle2 = CycleUtility.quadToCycle(quads);
			assertEquals(cycle2.size(), cycle.size());
					
			for(Edge.Generic e: cycle) assertTrue(cycle2.contains(e));
			
//			assertTrue(HomotopyUtility.isClosed(cycle2));
//			assertFalse(HomotopyUtility.isTrivial(cycle2));
			
			cycle2 = CycleUtility.quadToDualCycle(quads);

			assertTrue(HomotopyUtility.isDualClosed(cycle2));
//			assertFalse(HomotopyUtility.isTrivial(cycle2));
		}

	}

	/*
	 */
	public void testCycleToQuad() {

	}

	/*
	 * Test method for 'discreteRiemann.CycleUtility.extractDualCycle(List<E>) <V, E, F>'
	 */
	public void testExtractDualCycle() {

	}

	/*
	 * Test method for 'discreteRiemann.DiscreteRiemannUtility.derivateDual(HalfEdgeDataStructure<V, E, F>, double[], int[]) <V, E, F>'
	 */
	public void testDerivateDualHalfEdgeDataStructureOfVEFDoubleArrayIntArray() {
	
	}

	/*
	 * Test method for 'discreteRiemann.DiscreteRiemannUtility.derivateDual(HalfEdgeDataStructure<V, E, F>, double[], int[], double[]) <V, E, F>'
	 */
	public void testDerivateDualHalfEdgeDataStructureOfVEFDoubleArrayIntArrayDoubleArray() {
	
	}

	/*
	 * Test method for 'discreteRiemann.DiscreteRiemannUtility.derivate(HalfEdgeDataStructure<V, E, F>, double[], int[]) <V, E, F>'
	 */
	public void testDerivateHalfEdgeDataStructureOfVEFDoubleArrayIntArray() {
	
	}

	/*
	 * Test method for 'discreteRiemann.DiscreteRiemannUtility.derivate(HalfEdgeDataStructure<V, E, F>, double[], int[], double[]) <V, E, F>'
	 */
	public void testDerivateHalfEdgeDataStructureOfVEFDoubleArrayIntArrayDoubleArray() {
	
	}

	/*
	 * Test method for 'discreteRiemann.DiscreteRiemannUtility.integrate(double[], List<E>) <V, E, F>'
	 */
	public void testIntegrate() {
		int m=3, n=5;
		DiscreteConformalStructure<ConfVertex, ConfEdge, ConfFace> dcs = 
			new DiscreteConformalStructure<ConfVertex, ConfEdge, ConfFace> 
		(TorusUtility.createTorusNew(m,n,0,ConfVertex.class, ConfEdge.class, ConfFace.class));
		DirichletFunctional.Factory factory = new DirichletFunctional.Factory(dcs);
	
		factory.setRho(1);
		
		Edge e = dcs.getEdge(0);
		
		double[] alpha = new double[dcs.getNumEdges()];
		Arrays.fill(alpha, 1.);
		
		List<ConfEdge> a = TorusUtility.getZicZacCycle(e);
		List<ConfEdge> b = TorusUtility.getZicZacCycle(e.getNextEdge());
		
		List<ConfEdge> c;
		c = CycleUtility.quadToCycle(a);
		assertEquals((double) c.size(), CycleUtility.integrate(alpha, c));
		c=CycleUtility.quadToDualCycle(a);
		assertEquals((double) c.size(), CycleUtility.integrate(alpha, c));
		c = CycleUtility.quadToCycle(b);
		assertEquals((double) c.size(), CycleUtility.integrate(alpha, c));
		c=CycleUtility.quadToDualCycle(b);
		assertEquals((double) c.size(), CycleUtility.integrate(alpha, c));
	
		Arrays.fill(alpha, 0.);
		for(Edge ce: CycleUtility.quadToCycle(a)) alpha[ce.getIndex()] = 1.;
		c = CycleUtility.quadToCycle(a);
		assertEquals((double) c.size(), CycleUtility.integrate(alpha, c));
		c = CycleUtility.quadToCycle(b);
		assertEquals((double) 1, CycleUtility.integrate(alpha, c));
		c = CycleUtility.quadToDualCycle(b);
		assertEquals((double) 1, CycleUtility.integrate(alpha, c));
		
	
	
	}

	/*
	 * Test method for 'discreteRiemann.CycleUtility.isClosed(HalfEdgeDataStructure<V, E, F>, double[]) <V, E, F>'
	 */
	public void testIsClosed() {
		int m = 5, n = 7;
		DiscreteConformalStructure<ConfVertex, ConfEdge, ConfFace> dcs = 
			new DiscreteConformalStructure<ConfVertex, ConfEdge, ConfFace> 
		(TorusUtility.createTorusNew(m,n,0,ConfVertex.class, ConfEdge.class, ConfFace.class));
		DirichletFunctional.Factory factory = new DirichletFunctional.Factory(dcs);
	
		factory.setRho(1);
		
		Edge e = dcs.getEdge(0);
		
		List<Edge> a = TorusUtility.getZicZacCycle(e);
		List<Edge> b = TorusUtility.getZicZacCycle(e.getNextEdge());
		
		
		factory.setQuadCycle(a);
		factory.update();
	
		double [] omegaAlpha     = CycleUtility.grad(    dcs, factory.f .f, factory.f .eps );
	
		assertTrue(CycleUtility.isClosed(dcs, omegaAlpha));		
		assertTrue(DiscreteRiemannUtility.isCoClosed(dcs, omegaAlpha));		
		
		double [] omegaAlphaDual = CycleUtility.gradDual(dcs, factory.fs.f, factory.fs.eps );
	
		assertTrue(CycleUtility.isDualClosed(dcs, omegaAlphaDual));
		assertTrue(DiscreteRiemannUtility.isDualCoClosed(dcs, omegaAlphaDual));		
		
	
		double [] starOmegaAlpha     = DiscreteRiemannUtility.star    (dcs, omegaAlpha);
		
		assertTrue(CycleUtility.isDualClosed(dcs, starOmegaAlpha));		

		double [] starOmegaAlphaDual = DiscreteRiemannUtility.starDual(dcs, omegaAlphaDual);

		assertTrue(CycleUtility.isClosed(dcs, starOmegaAlphaDual));		
	
		factory.setQuadCycle(b);
		factory.update();
	
		
	}

	/*
	 * Test method for 'discreteRiemann.CycleUtility.isDualClosed(HalfEdgeDataStructure<V, E, F>, double[]) <V, E, F>'
	 */
	public void testIsDualClosed() {
	
	}

}
