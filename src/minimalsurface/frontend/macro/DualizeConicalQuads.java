package minimalsurface.frontend.macro;

import halfedge.HalfEdgeDataStructure;
import image.ImageHook;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import minimalsurface.util.MinimalSurfaceUtility;
import circlepatterns.graph.CPEdge;
import circlepatterns.graph.CPFace;
import circlepatterns.graph.CPVertex;

public class DualizeConicalQuads extends MacroAction {

	protected Icon 
		icon = new ImageIcon(ImageHook.getImage("schwarz.png"));
	
	@Override
	public String getName() {
		return "Dualize Conical Quads";
	}

	@Override
	public HalfEdgeDataStructure<CPVertex, CPEdge, CPFace> process(HalfEdgeDataStructure<CPVertex, CPEdge, CPFace> graph) throws Exception {
		MinimalSurfaceUtility.dualizeSurfaceKoenigs(graph, true);
		return graph;
	}

	@Override
	public Icon getIcon() {
		return icon;
	}

}
