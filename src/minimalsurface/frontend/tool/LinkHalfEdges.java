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
	
	@Override
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
		return "Link Graph";
	}

	@Override
	public JPanel getOptionPanel() {
		return null;
	}

	@Override
	public String getShortDescription() {
		return "Create a fully linked graph";
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
