package discreteRiemann;

import java.util.ArrayList;

import halfedge.Edge;
import halfedge.Face;
import halfedge.HalfEdgeDataStructure;
import halfedge.Vertex;
import halfedge.surfaceutilities.ConsistencyCheck;
import junit.framework.TestCase;
import discreteRiemann.TorusUtility;

public class TorusUtilityTest extends TestCase {

	/*
	 * Test method for 'discreteRiemann.TorusUtility.createTorus(int, int, Class<V>, Class<E>, Class<F>) <V, E, F>'
	 */
	public void testCreateTorusGetZicZacCycle() {

		int m=5, n=7;
		HalfEdgeDataStructure<Vertex.Generic, Edge.Generic, Face.Generic> heds = 
			TorusUtility.createTorus(m,n,Vertex.Generic.class, Edge.Generic.class, Face.Generic.class);
		
		assertTrue(ConsistencyCheck.isValidSurface(heds));
		
		assertEquals(heds.getNumVertices(), m*n);
		assertEquals(heds.getNumEdges(), 4*m*n);
		assertEquals(heds.getNumFaces(), m*n);

	Edge e, e0 = heds.getEdge(0); //  horizontal
		e = e0;

		
			int i=0;
		 do { // horizontal cycle
			 i++;
			e = e.getNextEdge().getOppositeEdge().getNextEdge();
		 } while((e != e0) && (i<2*m*n));
		
		 assertEquals(i,n);		
		 
	final Edge e1 = e0.getNextEdge(); // vertical edge


	
	e = e1; 
	
	i=0;
	 do { // vertical cycle
		 i++;
		e = e.getNextEdge().getOppositeEdge().getNextEdge();
	} while((e != e1) && (i<2*m*n));
	
	 assertEquals(i,m);	

	 ArrayList zzV = TorusUtility.getZicZacCycle(e1);
	 assertEquals(zzV.size(),2*n);
	 
	 ArrayList zzH = TorusUtility.getZicZacCycle(e0);
	 assertEquals(zzH.size(),2*m);
	
	 
	}
	public void testCreateTorusTwist() {

		int m=7, n=13, twist = 1;
		HalfEdgeDataStructure<Vertex.Generic, Edge.Generic, Face.Generic> heds = 
			TorusUtility.createTorusNew(m,n,twist,Vertex.Generic.class, Edge.Generic.class, Face.Generic.class);
		
		assertTrue(ConsistencyCheck.isValidSurface(heds));
		
		assertEquals(heds.getNumVertices(), m*n);
		assertEquals(heds.getNumEdges(), 4*m*n);
		assertEquals(heds.getNumFaces(), m*n);

	Edge e, e0 = heds.getEdge(2); //  horizontal
		e = e0;

		ArrayList<Edge> eH = new ArrayList<Edge>();
		
			int i=0;
		 do { // horizontal cycle
			 i++;
			e = e.getNextEdge().getOppositeEdge().getNextEdge();
		eH.add(e);
		 } while((e != e0) && (i<m*n));
		
		 assertEquals(i,m);		
		 
	final Edge e1 = e0.getNextEdge(); // vertical edge


	
	e = e1; 
	ArrayList<Edge> eV = new ArrayList<Edge>();
	
	i=0;
	 do { // vertical cycle
		 i++;
		e = e.getNextEdge().getOppositeEdge().getNextEdge();
		eV.add(e);
	} while((e != e1) && (i<2*m*n));
	
	 assertEquals(i,n*m); // lcf(n,m) in general, here gcd(n,m)=1, 	

	 ArrayList zzH = TorusUtility.getZicZacCycle(e1.getOppositeEdge());
	 assertEquals(zzH.size(),2*m);

	 
	}
	
	public void testCreateTriangularTorus() {

		int m=3, n=4;
		HalfEdgeDataStructure<Vertex.Generic, Edge.Generic, Face.Generic> heds = 
			TorusUtility.createTriangularTorus(m,n,Vertex.Generic.class, Edge.Generic.class, Face.Generic.class);
		
		assertTrue(ConsistencyCheck.isValidSurface(heds));
		
		assertEquals(heds.getNumVertices(), m*n);
		assertEquals(heds.getNumEdges(), 6*m*n);
		assertEquals(heds.getNumFaces(), 2*m*n);

		for(Edge e: heds.getEdges()) { // It is a regular triangulation
		assertEquals(e.getLeftFace().getBoundary().size(),3);
		assertEquals(e.getStartVertex().getEdgeStar().size(),6);
		assertEquals(e.getStartVertex().getFaceStar().size(),6);
		}
		
	Edge e, e0 = heds.getEdge(2); //  horizontal
		e = e0;

		ArrayList<Edge> eH = new ArrayList<Edge>();
		
			int i=0;
		 do { // horizontal cycle
			 i++;
			e = e.getNextEdge().getOppositeEdge().getNextEdge().getOppositeEdge().getNextEdge();
		eH.add(e);
		 } while((e != e0) && (i<2*m*n));
		
		 assertEquals(i,m);		
		 
	final Edge e1 = e0.getNextEdge(); // vertical edge


	
	e = e1; 
	ArrayList<Edge> eV = new ArrayList<Edge>();
	
	i=0;
	 do { // vertical cycle
		 i++;
		e = e.getNextEdge().getOppositeEdge().getNextEdge().getOppositeEdge().getNextEdge();
		eV.add(e);
	} while((e != e1) && (i<m*n));
	
	 assertEquals(i,n);
	 
	 
	 
	 e0 = heds.getEdge(0); // Diagonal
		e = e0;

		ArrayList<Edge> eD = new ArrayList<Edge>();
		
			i=0;
		 do { // Diagonal cycle
			 i++;
			e = e.getNextEdge().getOppositeEdge().getNextEdge().getOppositeEdge().getNextEdge();
		eH.add(e);
		 } while((e != e0) && (i<2*m*n));
		
		 assertEquals(i,m*n);		
	
	 
	}

}
