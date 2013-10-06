package koebe.frontend.tool;

import halfedge.HalfEdgeDataStructure;
import halfedge.frontend.content.GraphGraphics;
import halfedge.frontend.graphtool.EditOperation;
import halfedge.frontend.graphtool.EditOperationException;
import halfedge.frontend.graphtool.GraphTool;
import halfedge.surfaceutilities.ConsistencyCheck;
import halfedge.surfaceutilities.Subdivision;
import halfedge.surfaceutilities.SurfaceException;
import halfedge.surfaceutilities.SurfaceUtility;
import image.ImageHook;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

import koebe.KoebePolyhedron;
import koebe.KoebePolyhedron.KoebePolyhedronContext;
import koebe.PolyederNormalizer;
import koebe.frontend.controller.MainController;
import circlepatterns.graph.CPEdge;
import circlepatterns.graph.CPFace;
import circlepatterns.graph.CPVertex;

public class VertexQuadSubdivide implements GraphTool<CPVertex, CPEdge, CPFace> {

	protected Icon 
		icon = new ImageIcon(ImageHook.getImage("vertexsubdivide.png"));
	private MainController
		controller = null;
	
	
	@Override
	public Boolean initTool() {
		try {
			HalfEdgeDataStructure<CPVertex, CPEdge, CPFace> quad = Subdivision.createVertexQuadGraph(controller.getEditedGraph());
			SurfaceUtility.fillHoles(quad);
			if (!ConsistencyCheck.isValidSurface(quad)){
				controller.setStatus("No valid surface contructed!");
				return false;
			}
			KoebePolyhedronContext<CPVertex, CPEdge, CPFace> context = null;
			try {
				context = KoebePolyhedron.contructKoebePolyhedron(quad, 1E-4, 20);
				PolyederNormalizer.normalize(context);
				controller.setStatus("successfully subdivided");
			} catch (Exception e) {
				controller.setStatus(e.getMessage());
				return false;
			}	
			controller.getKoebeViewer().updateGeometry(context);
			controller.setEditedGraph(quad);
		} catch (SurfaceException e) {
			controller.setStatus(e.getMessage());
		}
		return true;
	}
	
	@Override
	public void commitEdit(HalfEdgeDataStructure<CPVertex, CPEdge, CPFace> graph) {
		
	}

	@Override
	public String getDescription() {
		return getShortDescription();
	}

	@Override
	public Icon getIcon() {
		return icon;
	}

	@Override
	public String getName() {
		return "Vertex-Quad Subdivide";
	}

	@Override
	public JPanel getOptionPanel() {
		return null;
	}

	@Override
	public String getShortDescription() {
		return "Subdivide with quads";
	}


	@Override
	public void leaveTool() {

	}

	@Override
	public boolean needsRepaint() {
		return true;
	}

	@Override
	public void paint(GraphGraphics g) {

	}

	@Override
	public boolean processEditOperation(EditOperation operation)
			throws EditOperationException {
		return false;
	}

	@Override
	public void resetTool() {

	}

	@Override
	public void setController(halfedge.frontend.controller.MainController<CPVertex, CPEdge, CPFace> controller) {
		this.controller = (MainController)controller;
	}

}
