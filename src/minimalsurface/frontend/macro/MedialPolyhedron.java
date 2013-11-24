package minimalsurface.frontend.macro;

import halfedge.HalfEdgeDataStructure;
import image.ImageHook;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import koebe.KoebePolyhedron;
import koebe.KoebePolyhedron.KoebePolyhedronContext;
import koebe.PolyederNormalizer;
import circlepatterns.graph.CPEdge;
import circlepatterns.graph.CPFace;
import circlepatterns.graph.CPVertex;

public class MedialPolyhedron extends MacroAction {

	protected Icon 
		icon = new ImageIcon(ImageHook.getImage("koebe.png"));
	private double
		tolerance = 1E-9;
	private int 
		maxIterations = 100; 
	
	@Override
	public String getName() {
		return "Create Medial Polyhedron";
	}

	@Override
	public HalfEdgeDataStructure<CPVertex, CPEdge, CPFace> process(HalfEdgeDataStructure<CPVertex, CPEdge, CPFace> graph) throws Exception {
		KoebePolyhedronContext<CPVertex, CPEdge, CPFace> context = KoebePolyhedron.contructKoebePolyhedron(graph, tolerance, maxIterations);
		PolyederNormalizer.normalize(context);
		return context.getMedial();
	}
	
	@Override
	public Icon getIcon() {
		return icon;
	}

}
