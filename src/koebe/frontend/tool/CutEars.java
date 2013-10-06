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
	
	
	@Override
	public Boolean initTool() {
		HalfEdgeDataStructure<CPVertex, CPEdge, CPFace> graph = controller.getEditedGraph();
		Ears.cutEars(graph);
		controller.fireGraphChanged();
		return false;
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
		return null;
	}

	@Override
	public String getName() {
		return "Cut Ears";
	}

	@Override
	public JPanel getOptionPanel() {
		return null;
	}

	@Override
	public String getShortDescription() {
		return "Cut Ears";
	}

	@Override
	public void leaveTool() {

	}

	@Override
	public boolean needsRepaint() {
		return false;
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
