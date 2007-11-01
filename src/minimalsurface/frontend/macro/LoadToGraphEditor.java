package minimalsurface.frontend.macro;

import halfedge.HalfEdgeDataStructure;
import circlepatterns.graph.CPEdge;
import circlepatterns.graph.CPFace;
import circlepatterns.graph.CPVertex;

public class LoadToGraphEditor extends MacroAction {

	@Override
	public String getName() {
		return "Load Result To Graph Editor";
	}

	@Override
	public HalfEdgeDataStructure<CPVertex, CPEdge, CPFace> 
		process(HalfEdgeDataStructure<CPVertex, CPEdge, CPFace> graph) throws Exception {
		return graph;
	}
	
}
