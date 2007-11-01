package minimalsurface.frontend.macro;

import halfedge.HalfEdgeDataStructure;
import halfedge.surfaceutilities.SurfaceUtility;
import image.ImageHook;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import minimalsurface.util.MinimalSurfaceUtility;
import circlepatterns.graph.CPEdge;
import circlepatterns.graph.CPFace;
import circlepatterns.graph.CPVertex;

public class Dualize extends MacroAction {

	protected Icon 
		icon = new ImageIcon(ImageHook.getImage("schwarz.png"));
	
	public String getName() {
		return "Dualize Surface";
	}

	public HalfEdgeDataStructure<CPVertex, CPEdge, CPFace> 
		process(HalfEdgeDataStructure<CPVertex, CPEdge, CPFace> graph) throws Exception {
		MinimalSurfaceUtility.createEdgeLabels(graph);
		MinimalSurfaceUtility.dualizeSurface(graph, true);
		SurfaceUtility.rescaleSurface(graph, 5.0);
		return graph;
	}
	
	public Icon getIcon() {
		return icon;
	}

}
