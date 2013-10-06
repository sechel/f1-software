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
	
	@Override
	public Boolean initTool() {
		return true;
	}

	@Override
	public void leaveTool() {

	}

	@Override
	public void setController(halfedge.frontend.controller.MainController<CPVertex, CPEdge, CPFace>  controller) {
		this.controller = (MainController)controller;
	}

	@Override
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

	@Override
	public void commitEdit(HalfEdgeDataStructure<CPVertex, CPEdge, CPFace> graph) {

	}

	@Override
	public void resetTool() {

	}

	@Override
	public String getName() {
		return "Cut";
	}

	@Override
	public Icon getIcon() {
		return null;
	}

	@Override
	public String getDescription() {
		return "Cut At Edge";
	}

	@Override
	public String getShortDescription() {
		return "Cut";
	}

	@Override
	public void paint(GraphGraphics g) {

	}

	@Override
	public boolean needsRepaint() {
		return false;
	}

	@Override
	public JPanel getOptionPanel() {
		return null;
	}


}
