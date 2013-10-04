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
	
	public void commitEdit(HalfEdgeDataStructure<CPVertex, CPEdge, CPFace> graph) {
		
	}

	public String getDescription() {
		return getShortDescription();
	}

	public Icon getIcon() {
		return icon;
	}

	public String getName() {
		return "Vertex-Quad Subdivide";
	}

	public JPanel getOptionPanel() {
		return null;
	}

	public String getShortDescription() {
		return "Subdivide with quads";
	}


	public void leaveTool() {

	}

	public boolean needsRepaint() {
		return true;
	}

	public void paint(GraphGraphics g) {

	}

	public boolean processEditOperation(EditOperation operation)
			throws EditOperationException {
		return false;
	}

	public void resetTool() {

	}

	public void setController(halfedge.frontend.controller.MainController<CPVertex, CPEdge, CPFace> controller) {
		this.controller = (MainController)controller;
	}

}
