package alexandrov;

import halfedge.HalfEdgeDataStructure;
import junit.framework.TestCase;
import util.TestData;
import alexandrov.graph.CPMEdge;
import alexandrov.graph.CPMFace;
import alexandrov.graph.CPMVertex;

public class TestCapLayouter extends TestCase {

	private final int 
		maxIterations = 100;
	private final double 
		error = 1E-20,
		lengthError = 1E-6;
	
	public void testLayout() throws Exception {
		HalfEdgeDataStructure<CPMVertex, CPMEdge, CPMFace> graph = TestData.getTestGraph("zeltCap.cpm");
		AlexandrovCap.constructCap_internal(graph, error, maxIterations);
		AlexandrovUtility.layoutCap(graph, false);
		
		for (CPMEdge e : graph.getEdges()){
			double length = e.getTargetVertex().getXYZW().distance(e.getStartVertex().getXYZW());
			assertEquals(e.getLength(), length, lengthError);
		}
		
	}
	
	
	
}
