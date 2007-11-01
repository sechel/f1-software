package minimalsurface.frontend.macro;

import halfedge.HalfEdgeDataStructure;
import halfedge.surfaceutilities.Ears;
import image.ImageHook;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import circlepatterns.graph.CPEdge;
import circlepatterns.graph.CPFace;
import circlepatterns.graph.CPVertex;

public class CutEars extends MacroAction {

	protected Icon 
		icon = new ImageIcon(ImageHook.getImage("edgesubdivide.png"));
	
	@Override
	public String getName() {
		return "Cut Ears";
	}

	@Override
	public HalfEdgeDataStructure<CPVertex, CPEdge, CPFace> 
		process(HalfEdgeDataStructure<CPVertex, CPEdge, CPFace> graph) throws Exception {
		Ears.cutEars(graph);
		return graph;
	}
	
//	@Override
//	public Icon getIcon() {
//		return icon;
//	}

}
