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
	
	public Boolean initTool() {
		return true;
	}

	public void leaveTool() {

	}

	public void setController(halfedge.frontend.controller.MainController<CPMVertex, CPMEdge, CPMFace> controller) {
		this.controller = (MainController)controller;
	}

	public boolean processEditOperation(EditOperation operation) throws EditOperationException {
		switch (operation){
		case SELECT_EDGE:
			try {
					((CPMEdge)operation.edge).flip();
				} catch (TriangulationException e) {
					e.printStackTrace();
				}
			controller.fireGraphChanged();
			break;
		}
		controller.refreshEditor();
		return false;
	}

	public void commitEdit(HalfEdgeDataStructure<CPMVertex, CPMEdge, CPMFace> graph) {

	}

	public void resetTool() {

	}

	public String getName() {
		return "Edge Flip Tool";
	}

	public Icon getIcon() {
		return icon;
	}

	public String getDescription() {
		return "Flip An Edge";
	}

	public String getShortDescription() {
		return "Flip";
	}

	
	public void paint(GraphGraphics g) {

	}

	public boolean needsRepaint() {
		return true;
	}

	public JPanel getOptionPanel() {
		return null;
	}


}
