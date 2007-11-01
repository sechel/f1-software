package halfedge;

import halfedge.generator.SquareGridGenerator;

import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;

public class HalfEdgeUtilityTest extends TestCase {

	public void testFindCommonVertex() throws Exception {
		
		HalfEdgeDataStructure<Vertex.Generic, Edge.Generic, Face.Generic> heds = SquareGridGenerator.generate(10, 10);
		Vertex.Generic v = heds.getVertex(36);
		List<Edge.Generic> edgeList = v.getEdgeStar();
		Vertex.Generic commomV = HalfEdgeUtility.findCommonVertex(edgeList.toArray(new Edge.Generic[edgeList.size()]));
		assertTrue(v == commomV);
		
		LinkedList<Edge.Generic> newList = new LinkedList<Edge.Generic>(edgeList);
		newList.add(heds.getEdge(0));
		commomV = HalfEdgeUtility.findCommonVertex(newList.toArray(new Edge.Generic[newList.size()]));
		assertTrue(null == commomV);
		
	}
	
	public void testFindCommonFace() throws Exception {
		
		HalfEdgeDataStructure<Vertex.Generic, Edge.Generic, Face.Generic> heds = SquareGridGenerator.generate(10, 10);
		Face.Generic f = heds.getFace(34);
		List<Edge.Generic> edgeList = f.getBoundary();
		Face.Generic commomF = HalfEdgeUtility.findCommonFace(edgeList.toArray(new Edge.Generic[edgeList.size()]));
		assertTrue(f == commomF);
		
		LinkedList<Edge.Generic> newList = new LinkedList<Edge.Generic>(edgeList);
		newList.add(heds.getEdge(0));
		commomF = HalfEdgeUtility.findCommonFace(newList.toArray(new Edge.Generic[newList.size()]));
		assertTrue(null == commomF);
		
	}
	
}
