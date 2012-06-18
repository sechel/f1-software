package koebe.frontend.tool;

import halfedge.HalfEdgeDataStructure;
import halfedge.frontend.content.GraphGraphics;
import halfedge.frontend.graphtool.EditOperation;
import halfedge.frontend.graphtool.EditOperationException;
import halfedge.frontend.graphtool.GraphTool;

import javax.swing.Icon;
import javax.swing.JPanel;

import koebe.frontend.controller.MainController;
import minimalsurface.util.GraphUtility;
import util.debug.DBGTracer;
import circlepatterns.graph.CPEdge;
import circlepatterns.graph.CPFace;
import circlepatterns.graph.CPVertex;



/**
 * Deletes the selected node
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 */
public class CutAtEdge implements GraphTool<CPVertex, CPEdge, CPFace>  {

	private MainController
		controller = null;
	
	public Boolean initTool() {
		return true;
	}

	public void leaveTool() {

	}

	public void setController(halfedge.frontend.controller.MainController<CPVertex, CPEdge, CPFace>  controller) {
		this.controller = (MainController)controller;
	}

	public boolean processEditOperation(EditOperation operation)
			throws EditOperationException {
		CPEdge edge = CPEdge.class.cast(operation.edge);
		switch (operation){
			case SELECT_VERTEX:
				break;
			case SELECT_FACE:
				break;
			case SELECT_EDGE:
				try {
					GraphUtility.cutAtEdge(edge);
				} catch (IllegalArgumentException e){
					DBGTracer.stackTrace(e);
					controller.setStatus(e.getMessage());
				}
				controller.fireGraphChanged();
				break;
		}
		return false;
	}

	public void commitEdit(HalfEdgeDataStructure<CPVertex, CPEdge, CPFace> graph) {

	}

	public void resetTool() {

	}

	public String getName() {
		return "Cut";
	}

	public Icon getIcon() {
		return null;
	}

	public String getDescription() {
		return "Cut At Edge";
	}

	public String getShortDescription() {
		return "Cut";
	}

	public void paint(GraphGraphics g) {

	}

	public boolean needsRepaint() {
		return false;
	}

	public JPanel getOptionPanel() {
		return null;
	}


}
