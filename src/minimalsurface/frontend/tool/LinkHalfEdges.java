package minimalsurface.frontend.tool;

import halfedge.HalfEdgeDataStructure;
import halfedge.HalfEdgeUtility;
import halfedge.frontend.content.GraphGraphics;
import halfedge.frontend.graphtool.EditOperation;
import halfedge.frontend.graphtool.EditOperationException;
import halfedge.frontend.graphtool.GraphTool;
import halfedge.surfaceutilities.ConsistencyCheck;
import halfedge.surfaceutilities.SurfaceUtility;
import image.ImageHook;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

import minimalsurface.controller.MainController;
import circlepatterns.graph.CPEdge;
import circlepatterns.graph.CPFace;
import circlepatterns.graph.CPVertex;

public class LinkHalfEdges implements GraphTool<CPVertex, CPEdge, CPFace> {

	protected Icon 
		icon = new ImageIcon(ImageHook.getImage("link.png"));
	private MainController
		controller = null;
	
	public Boolean initTool() {
		try {
			HalfEdgeDataStructure<CPVertex, CPEdge, CPFace> graph = controller.getEditedGraph();
			HalfEdgeUtility.removeAllFaces(graph);
			SurfaceUtility.linkAllEdges(graph);
			SurfaceUtility.fillHoles(graph);
			
			if (!ConsistencyCheck.isThreeConnected(graph)){
				controller.setStatus("Graph not three-connected!");
				return true;
			}			
			controller.fireGraphChanged();
		} catch (Exception e) {
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
		return "Link Graph";
	}

	public JPanel getOptionPanel() {
		return null;
	}

	public String getShortDescription() {
		return "Create a fully linked graph";
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
