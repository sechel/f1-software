package minimalsurface.frontend.macro;

import halfedge.HalfEdgeDataStructure;
import halfedge.surfaceutilities.Subdivision;
import image.ImageHook;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import circlepatterns.graph.CPEdge;
import circlepatterns.graph.CPFace;
import circlepatterns.graph.CPVertex;

public class ConsistentStripSubdivide extends MacroAction {

	protected Icon 
		icon = new ImageIcon(ImageHook.getImage("edgesubdivide.png"));
	
	@Override
	public String getName() {
		return "Consistent Strip Subdivision";
	}

	@Override
	public HalfEdgeDataStructure<CPVertex, CPEdge, CPFace> 
		process(HalfEdgeDataStructure<CPVertex, CPEdge, CPFace> graph) throws Exception {
		return Subdivision.createStripSubdivision(graph);
	}
	

}
