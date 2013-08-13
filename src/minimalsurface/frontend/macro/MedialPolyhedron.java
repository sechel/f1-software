package minimalsurface.frontend.macro;

import halfedge.HalfEdgeDataStructure;
import halfedge.surfaceutilities.SurfaceUtility;
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
	
	@Override
	public String getName() {
		return "Create Medial Polyhedron";
	}

	@Override
	public HalfEdgeDataStructure<CPVertex, CPEdge, CPFace> process(
			HalfEdgeDataStructure<CPVertex, CPEdge, CPFace> graph) throws Exception {

		KoebePolyhedronContext<CPVertex, CPEdge, CPFace> context = KoebePolyhedron.contructKoebePolyhedron(graph);
		PolyederNormalizer.normalize(context);
		SurfaceUtility.rescaleGraph(context.getMedial(), 300.0);
		return context.getMedial();
	}
	
	@Override
	public Icon getIcon() {
		return icon;
	}

}
