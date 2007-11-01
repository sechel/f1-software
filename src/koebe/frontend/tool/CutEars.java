package koebe.frontend.tool;

import halfedge.HalfEdgeDataStructure;
import halfedge.frontend.content.GraphGraphics;
import halfedge.frontend.graphtool.EditOperation;
import halfedge.frontend.graphtool.EditOperationException;
import halfedge.frontend.graphtool.GraphTool;
import halfedge.surfaceutilities.Ears;

import javax.swing.Icon;
import javax.swing.JPanel;

import koebe.frontend.controller.MainController;
import circlepatterns.graph.CPEdge;
import circlepatterns.graph.CPFace;
import circlepatterns.graph.CPVertex;

public class CutEars implements GraphTool<CPVertex, CPEdge, CPFace> {

	private MainController
		controller = null;
	
	
	public Boolean initTool() {
		HalfEdgeDataStructure<CPVertex, CPEdge, CPFace> graph = controller.getEditedGraph();
		Ears.cutEars(graph);
		controller.fireGraphChanged();
		return false;
	}
	
	
	public void commitEdit(HalfEdgeDataStructure<CPVertex, CPEdge, CPFace> graph) {

	}

	public String getDescription() {
		return getShortDescription();
	}

	public Icon getIcon() {
		return null;
	}

	public String getName() {
		return "Cut Ears";
	}

	public JPanel getOptionPanel() {
		return null;
	}

	public String getShortDescription() {
		return "Cut Ears";
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
