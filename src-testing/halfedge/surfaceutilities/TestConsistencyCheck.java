package halfedge.surfaceutilities;

import halfedge.Edge;
import halfedge.Face;
import halfedge.HalfEdgeDataStructure;
import halfedge.Vertex;
import junit.framework.TestCase;

public class TestConsistencyCheck extends TestCase {

	public void testSimpleSurface1() {
		HalfEdgeDataStructure<Vertex.Generic, Edge.Generic, Face.Generic> heds = HalfEdgeDataStructure.createHEDS();
		// build disk with 1 edge, 1 vertex, and 1 face
		Edge.Generic e0 = heds.addNewEdge();
		Edge.Generic e1 = heds.addNewEdge();
		Face.Generic f0 = heds.addNewFace();
		Vertex.Generic v0 = heds.addNewVertex();
		e0.linkNextEdge(e0);
		e0.linkOppositeEdge(e1);
		e0.setLeftFace(f0);
		e0.setTargetVertex(v0);
		e1.linkNextEdge(e1);
		e1.setTargetVertex(v0);
		assertTrue(ConsistencyCheck.isValidSurface(heds));
		// set "outer face" to f0 as well. Not a valid surface!
		e1.setLeftFace(f0);
		assertFalse(ConsistencyCheck.isValidSurface(heds));
		// build sphere with 1 edge, 1 vertex, and 2 faces
		Face.Generic f1 = heds.addNewFace();
		e1.setLeftFace(f1);
		assertTrue(ConsistencyCheck.isValidSurface(heds));
	}
	
}
