package alexandrov.frontend.tool;

import halfedge.HalfEdgeDataStructure;
import halfedge.frontend.content.GraphGraphics;
import halfedge.frontend.graphtool.EditOperation;
import halfedge.frontend.graphtool.EditOperationException;
import halfedge.frontend.graphtool.GraphTool;
import halfedge.triangulationutilities.TriangulationException;
import image.ImageHook;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

import alexandrov.frontend.controller.MainController;
import alexandrov.graph.CPMEdge;
import alexandrov.graph.CPMFace;
import alexandrov.graph.CPMVertex;



/**
 * Edge length edit tool for the graph editor
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 */
public class EdgeFlipTool implements GraphTool<CPMVertex, CPMEdge, CPMFace> {

	private Icon 
		icon = new ImageIcon(ImageHook.getImage("flip.png"));
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
	public void setController(halfedge.frontend.controller.MainController<CPMVertex, CPMEdge, CPMFace> controller) {
		this.controller = (MainController)controller;
	}

	@Override
	public boolean processEditOperation(EditOperation operation) throws EditOperationException {
		switch (operation){
		case SELECT_EDGE:
			try {
					CPMEdge.class.cast(operation.edge).flip();
				} catch (TriangulationException e) {
					e.printStackTrace();
				}
			controller.fireGraphChanged();
			break;
		}
		controller.refreshEditor();
		return false;
	}

	@Override
	public void commitEdit(HalfEdgeDataStructure<CPMVertex, CPMEdge, CPMFace> graph) {

	}

	@Override
	public void resetTool() {

	}

	@Override
	public String getName() {
		return "Edge Flip Tool";
	}

	@Override
	public Icon getIcon() {
		return icon;
	}

	@Override
	public String getDescription() {
		return "Flip An Edge";
	}

	@Override
	public String getShortDescription() {
		return "Flip";
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
