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

public class KobePolyhedron extends MacroAction {

	protected Icon 
		icon = new ImageIcon(ImageHook.getImage("koebe.png"));
	
	
	@Override
	public String getName() {
		return "Create Koebe Polyhedron";
	}

	@Override
	public HalfEdgeDataStructure<CPVertex, CPEdge, CPFace> process(
			HalfEdgeDataStructure<CPVertex, CPEdge, CPFace> graph) throws Exception {
		KoebePolyhedronContext<CPVertex, CPEdge, CPFace> context = KoebePolyhedron.contructKoebePolyhedron(graph, 1E-4, 20);
		PolyederNormalizer.normalize(context);
		return context.getPolyeder();
	}
	
	
	@Override
	public Icon getIcon() {
		return icon;
	}

}
