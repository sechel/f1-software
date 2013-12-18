package minimalsurface.frontend.macro;

import halfedge.HalfEdgeDataStructure;
import image.ImageHook;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import koebe.KoebePolyhedron;
import circlepatterns.graph.CPEdge;
import circlepatterns.graph.CPFace;
import circlepatterns.graph.CPVertex;

public class CircularPolyhedron extends MacroAction {

	protected Icon 
		icon = new ImageIcon(ImageHook.getImage("koebe.png"));
	private double
		tolerance = 1E-9;
	private int 
		maxIterations = 100; 
	
	@Override
	public String getName() {
		return "Create Circular Polyhedron";
	}

	@Override
	public HalfEdgeDataStructure<CPVertex, CPEdge, CPFace> process(
		HalfEdgeDataStructure<CPVertex, CPEdge, CPFace> graph
	) throws Exception {
		KoebePolyhedron.calculateCirclePattern(graph, tolerance, maxIterations);
		return graph;
	}
	
	
	@Override
	public Icon getIcon() {
		return icon;
	}

}
