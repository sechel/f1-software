package discreteRiemann;

import halfedge.Edge;
import halfedge.Face;
import halfedge.HalfEdgeDataStructure;
import halfedge.Vertex;
import halfedge.Vertex.Generic;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;
import discreteRiemann.DiscreteConformalStructure.ConfEdge;
import discreteRiemann.DiscreteConformalStructure.ConfFace;
import discreteRiemann.DiscreteConformalStructure.ConfVertex;

public class HomotopyUtilityTest extends TestCase {

	/*
	 * Test method for 'discreteRiemann.HomotopyUtility.horocycle(V, int) <V, E, F>'
	 */
	public void testHorocycle() {
		int m=18, n=33, 
		min = Math.min(m,n), 
		mins2 = min/2, 
		maxs2 = Math.max(m,n)/2;
		HalfEdgeDataStructure<Vertex.Generic, Edge.Generic, Face.Generic> heds = 
			TorusUtility.createTorus(m,n,Vertex.Generic.class, Edge.Generic.class, Face.Generic.class);
		
		Vertex.Generic v = heds.getVertex(0);
		for(int i=1; i<mins2; i++){
			assertEquals(HomotopyUtility.horocycle(v,i).size(), 4*i);
		}
		for(int i=mins2+1; i< maxs2; i++){
			assertEquals(HomotopyUtility.horocycle(v,i).size(), 2*min);
		}
		for(int i=maxs2+1; i< (m+n)/2; i++){
			assertEquals(HomotopyUtility.horocycle(v,i).size(), 2*(m+n-2*i));
		}
		
	}
	
	/**
	 * 
	 */
	public void testSpanningTree() {
		int m=21, n=15;
		HalfEdgeDataStructure<Vertex.Generic, Edge.Generic, Face.Generic> heds = 
			TorusUtility.createTorus(m,n,Vertex.Generic.class, Edge.Generic.class, Face.Generic.class);
		
		Vertex.Generic v = heds.getVertex(0);
		
		List<Edge.Generic> sE = HomotopyUtility.spanningTree(v);
		
		Set<Vertex.Generic> sV = new HashSet<Vertex.Generic>();
		
		// Except from the root, 
		sV.add(v);
		// each vertex has exactly one edge going away from it.
		for(Edge e: sE){
			assertTrue(sV.add((Generic) e.getStartVertex()));
		}
		// And all the vertex are spanned.
		assertTrue(sV.containsAll(heds.vertexList));
		
	}

	
	public void testFollowTreeOpposite(){
		int m=21, n=15;
		HalfEdgeDataStructure<Vertex.Generic, Edge.Generic, Face.Generic> heds = 
			TorusUtility.createTorus(m,n,Vertex.Generic.class, Edge.Generic.class, Face.Generic.class);
		
		Vertex.Generic v = heds.getVertex(0);
		
		List<Edge.Generic> sE = HomotopyUtility.spanningTree(v);

		Vertex.Generic w = heds.getVertex(m*n/2);
		
		List<Edge.Generic> path =  HomotopyUtility.followTree(w,sE);
		
		assertTrue(HomotopyUtility.isPath(path));
		
		Vertex.Generic vc = w;
		
		for(Edge.Generic e: path){
			assertEquals(e.getStartVertex(),vc);
			vc = e.getTargetVertex();
		}

		assertEquals(path.get(path.size()-1).getTargetVertex(),v);
	
		List<Edge.Generic> oPath = HomotopyUtility.opposite(path);

		int i = path.size();

		for(Edge.Generic e: path){
			assertEquals(e.getOppositeEdge(), oPath.get(--i));
		}
		
		
		// 2 points spanning-tree 
		
		sE = HomotopyUtility.spanningTree(v,w);

		
		oPath =  HomotopyUtility.followTree(w,5,sE);

		assertEquals(5, oPath.size());
			
		oPath =  HomotopyUtility.followTree(w,sE);

		assertEquals(path.size(), oPath.size());
			
		i = 0;
		for(Edge.Generic e: path){
			assertEquals(e, oPath.get(i++));
		}

		sE = HomotopyUtility.spanningTree(w,v);

		
		oPath =  HomotopyUtility.followTree(v,sE);
		

		assertEquals(path.size(), oPath.size());
			
		oPath.addAll(path);
		
		assertTrue(HomotopyUtility.isClosed(oPath)); // That's all what can be said.
	}
	
	
	public void testConnect(){
		int m=5, n=5;
		HalfEdgeDataStructure<Vertex.Generic, Edge.Generic, Face.Generic> heds = 
			TorusUtility.createTorus(m,n,Vertex.Generic.class, Edge.Generic.class, Face.Generic.class);
		
		Vertex.Generic v = heds.getVertex(0);
		
		List<Edge.Generic> sE = HomotopyUtility.symetrize(HomotopyUtility.spanningTree(v));

		Vertex.Generic u = heds.getVertex(2*m*n/3);
		Vertex.Generic w = heds.getVertex(m*n/3);
		
		List<Edge.Generic> path =  HomotopyUtility.path(u,w,sE);
		
		
		Vertex.Generic vc = u;
		
		for(Edge.Generic e : path){
			assertEquals(e.getStartVertex(),vc);
			vc = e.getTargetVertex();
		}
		
		assertEquals(vc,w);
	
	}
	public void testIsTrivial(){
		int m=3, n=4;
		HalfEdgeDataStructure<Vertex.Generic, Edge.Generic, Face.Generic> heds = 
			TorusUtility.createTorus(m,n,Vertex.Generic.class, Edge.Generic.class, Face.Generic.class);
		Edge.Generic e;
		
		List<Edge.Generic> cycle = new ArrayList<Edge.Generic>();
		
		e = heds.getEdge(0);
		
		cycle.add(e);
		e = e.getNextEdge().getOppositeEdge().getNextEdge();
		cycle.add(e);
		
		e = e.getNextEdge();
		cycle.add(e);
		e = e.getNextEdge().getOppositeEdge().getNextEdge();
		cycle.add(e);
		
		e = e.getNextEdge();
		cycle.add(e);
		e = e.getNextEdge().getOppositeEdge().getNextEdge();
		cycle.add(e);

		e = e.getNextEdge();
		cycle.add(e);
		e = e.getNextEdge().getOppositeEdge().getNextEdge();
		cycle.add(e);

		assertTrue(HomotopyUtility.isTrivial(cycle));
		
		cycle =  heds.getFace(0).getBoundary();
		
		assertTrue(HomotopyUtility.isTrivial(cycle));

		cycle =  heds.getFace(0).getBoundary();
		
		assertTrue(HomotopyUtility.isTrivial(cycle));
		
		e = cycle.get(0);

		cycle.remove(e); e = e.getOppositeEdge();

		for(int i = 0; i<3; i++)
			cycle.add(i, e = e.getNextEdge());
					
		assertTrue(HomotopyUtility.isTrivial(cycle));
		
	}
	
	public void testHomotopyBasis(){
		int m=31, n=29;
		HalfEdgeDataStructure<ConfVertex, ConfEdge, ConfFace> heds = 
			TorusUtility.createTorus(m,n,ConfVertex.class, ConfEdge.class, ConfFace.class);
		
		ConfVertex v = heds.getVertex(0);
		
		v.p.set(1, 0);
		
//		DiscreteRiemannUtility.show(heds);
		
		
		List<List<ConfEdge>> basis =  HomotopyUtility.homotopyBasis(v);
	
		List<ConfEdge> alpha = basis.get(0);
		
		assertEquals(basis.size(), 2);
		
		for(List<ConfEdge> cycle : basis){
			assertTrue(cycle.size() >= Math.min(m,n));
			assertTrue(HomotopyUtility.isClosed(cycle));
			assertFalse(HomotopyUtility.isTrivial(cycle));
		}
	}

		

}
