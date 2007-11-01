package minimalsurface.frontend.macro;

import halfedge.HalfEdgeDataStructure;
import halfedge.surfaceutilities.SurfaceUtility;
import circlepatterns.graph.CPEdge;
import circlepatterns.graph.CPFace;
import circlepatterns.graph.CPVertex;

public class DataPreparation extends MacroAction {

	@Override
	public String getName() {
		return "Data Preparation";
	}

	@Override
	public HalfEdgeDataStructure<CPVertex, CPEdge, CPFace> process(
			HalfEdgeDataStructure<CPVertex, CPEdge, CPFace> graph)
			throws Exception {
		SurfaceUtility.linkAllEdges(graph);
		SurfaceUtility.fillHoles(graph);
		return graph;
	}

	
	
	
	
	
}
