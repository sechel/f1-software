package minimalsurface.frontend.macro;

import halfedge.HalfEdgeDataStructure;
import minimalsurface.util.MinimalSurfaceUtility;
import circlepatterns.graph.CPEdge;
import circlepatterns.graph.CPFace;
import circlepatterns.graph.CPVertex;

public class CreateFaceLables extends MacroAction {

	@Override
	public String getName() {
		return "Create Face Labels";
	}

	@Override
	public HalfEdgeDataStructure<CPVertex, CPEdge, CPFace> 
		process(HalfEdgeDataStructure<CPVertex, CPEdge, CPFace> graph) throws Exception {
		MinimalSurfaceUtility.createFaceLabels(graph, false);
		return graph;
	}
	
}
