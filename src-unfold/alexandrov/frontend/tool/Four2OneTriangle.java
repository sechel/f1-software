package alexandrov.frontend.tool;

import halfedge.HalfEdgeDataStructure;
import halfedge.decorations.HasQuadGraphLabeling.QuadGraphLabel;
import halfedge.frontend.content.GraphGraphics;
import halfedge.frontend.graphtool.EditOperation;
import halfedge.frontend.graphtool.EditOperationException;
import halfedge.frontend.graphtool.GraphTool;
import halfedge.surfaceutilities.SurfaceException;
import halfedge.surfaceutilities.UnfoldSubdivision;
import image.ImageHook;

import java.util.HashMap;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

import minimalsurface.controller.MainController;
import circlepatterns.graph.CPEdge;
import circlepatterns.graph.CPFace;
import circlepatterns.graph.CPVertex;

public class Four2OneTriangle implements GraphTool<CPVertex, CPEdge, CPFace> {

	protected Icon 
		icon = new ImageIcon(ImageHook.getImage("vertexsubdivide.png"));
	private MainController
		controller = null;
	
	public Boolean initTool() {
		try {
			HalfEdgeDataStructure<CPVertex, CPEdge, CPFace> graph = controller.getEditedGraph();
			
			HashMap<CPVertex, CPVertex> vertexVertexMap = new HashMap<CPVertex, CPVertex>();
			HashMap<CPEdge, CPVertex> edgeVertexMap = new HashMap<CPEdge, CPVertex>();
			HashMap<CPFace, CPFace> faceFaceMap = new HashMap<CPFace, CPFace>();
			HashMap<CPEdge, CPEdge> edgeEdgeMap = new HashMap<CPEdge, CPEdge>();
			HashMap<CPEdge, CPFace> edgeFaceMap = new HashMap<CPEdge, CPFace>();
			HalfEdgeDataStructure<CPVertex, CPEdge, CPFace> quad = UnfoldSubdivision.createEdgeSplitTriangle(graph, vertexVertexMap, edgeVertexMap, faceFaceMap, edgeEdgeMap, edgeFaceMap);
			
//			SurfaceUtility.fillHoles(quad);
			
			for (CPVertex v : quad.getVertices())
				v.setVertexLabel(QuadGraphLabel.INTERSECTION);
			
			
			controller.setEditedGraph(quad);
			controller.fireGraphChanged();
			controller.getViewer().resetGeometry();
			controller.getViewer().addSurface(quad);
			controller.getViewer().encompass();
		} catch (SurfaceException e) {
			controller.setStatus(e.getMessage());
		}
		return false;
	}
	
	
	public void commitEdit(HalfEdgeDataStructure graph) {
		
	}

	public String getDescription() {
		return getShortDescription();
	}

	public Icon getIcon() {
		return icon;
	}

	public String getName() {
		return "421";
	}

	public JPanel getOptionPanel() {
		return null;
	}

	public String getShortDescription() {
		return getName();
	}


	public void leaveTool() {

	}

	public boolean needsRepaint() {
		return false;
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
