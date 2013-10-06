package minimalsurface.frontend.tool;

import halfedge.HalfEdgeDataStructure;
import halfedge.frontend.content.GraphGraphics;
import halfedge.frontend.graphtool.EditOperation;
import halfedge.frontend.graphtool.EditOperationException;
import halfedge.frontend.graphtool.GraphTool;
import image.ImageHook;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

import minimalsurface.controller.MainController;
import circlepatterns.graph.CPEdge;
import circlepatterns.graph.CPFace;
import circlepatterns.graph.CPVertex;



/**
 * Creates all faces if the active graph is embedded
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 */
public class ClearEditor implements GraphTool<CPVertex, CPEdge, CPFace> {

	private Icon 
		icon = new ImageIcon(ImageHook.getImage("delete.png"));
	private MainController
		controller = null;
	
	
	@Override
	public Boolean initTool() {
		controller.setEditedGraph(HalfEdgeDataStructure.createHEDS(CPVertex.class, CPEdge.class, CPFace.class));
		return false;
	}

	@Override
	public void leaveTool() {
		

	}

	@Override
	public void setController(halfedge.frontend.controller.MainController<CPVertex, CPEdge, CPFace> controller) {
		this.controller = (MainController)controller;
	}

	@Override
	public boolean processEditOperation(EditOperation operation)
			throws EditOperationException {
		
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
		return "Clear Graph";
	}

	@Override
	public Icon getIcon() {
		return icon;
	}

	@Override
	public String getDescription() {
		return "Clear Graph";
	}

	@Override
	public String getShortDescription() {
		return "Clear Graph";
	}

	@Override
	public void paint(GraphGraphics g) {

	}

	@Override
	public boolean needsRepaint() {
		return true;
	}

	@Override
	public JPanel getOptionPanel() {
		return null;
	}

	
}
