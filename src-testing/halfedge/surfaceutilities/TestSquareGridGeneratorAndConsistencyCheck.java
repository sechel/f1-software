package halfedge.surfaceutilities;

import halfedge.HalfEdgeDataStructure;
import halfedge.generator.SquareGridGenerator;
import junit.framework.TestCase;
import circlepatterns.graph.CPEdge;
import circlepatterns.graph.CPFace;
import circlepatterns.graph.CPVertex;

public class TestSquareGridGeneratorAndConsistencyCheck extends TestCase {
	
	public void test() {
		HalfEdgeDataStructure<CPVertex, CPEdge, CPFace> ds = SquareGridGenerator.generate(10, 10, CPVertex.class, CPEdge.class, CPFace.class);
		SquareGridGenerator.setSquareGridThetas(ds, 0.1, 0.2);
		assertTrue(ConsistencyCheck.isValidSurface(ds));
	}
	
}
