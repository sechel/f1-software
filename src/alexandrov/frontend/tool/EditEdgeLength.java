package alexandrov.frontend.tool;

import halfedge.HalfEdgeDataStructure;
import halfedge.frontend.content.GraphGraphics;
import halfedge.frontend.graphtool.EditOperation;
import halfedge.frontend.graphtool.EditOperationException;
import halfedge.frontend.graphtool.GraphTool;
import image.ImageHook;

import java.awt.Color;
import java.awt.Point;
import java.math.BigDecimal;
import java.math.MathContext;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.vecmath.Point2d;

import alexandrov.frontend.content.LengthEditDialog;
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
public class EditEdgeLength implements GraphTool<CPMVertex, CPMEdge, CPMFace> {

	private Icon 
		icon = new ImageIcon(ImageHook.getImage("edgelength.png"));
	private MainController
		controller = null;
	private Color
		labelColor = Color.RED;
	
	public Boolean initTool() {
		return true;
	}

	public void leaveTool() {

	}

	public void setController(halfedge.frontend.controller.MainController<CPMVertex, CPMEdge, CPMFace> controller) {
		this.controller = (MainController)controller;
	}

	@SuppressWarnings("unchecked")
	public boolean processEditOperation(EditOperation operation) throws EditOperationException {
		switch (operation){
		case SELECT_EDGE:
			double newLength = LengthEditDialog.showEdgeLengthDialog(controller.getMainPanel(), ((CPMEdge)operation.edge).getLength());
			if (newLength == -1)
				break;
			else {
				((CPMEdge)operation.edge).setLength(newLength);
				((CPMEdge)operation.edge).getOppositeEdge().setLength(newLength);
				controller.fireGraphChanged();
			}
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
		return "Edge Length Editor";
	}

	public Icon getIcon() {
		return icon;
	}

	public String getDescription() {
		return "Edit Edge Length";
	}

	public String getShortDescription() {
		return "Edge Length";
	}

	
	public void paint(GraphGraphics g) {
		HalfEdgeDataStructure<CPMVertex, CPMEdge, CPMFace> graph = controller.getEditedGraph();
		for (CPMEdge edge : graph.getEdges()){
			Point2d t = edge.getTargetVertex().getXY();
			Point2d s = edge.getStartVertex().getXY();
			Point2d mid = new Point2d((s.x + t.x) / 2, (s.y + t.y) / 2);
			Point drawPos = g.toViewCoord(mid);
			g.getGraphics().setColor(labelColor);
			g.getGraphics().setFont(controller.getFontController().getIndexFont());
			BigDecimal value = new BigDecimal(edge.getLength());
			value = value.round(new MathContext(3));
			g.getGraphics().drawString(value + "", drawPos.x, drawPos.y);
		}
	}

	public boolean needsRepaint() {
		return true;
	}

	public JPanel getOptionPanel() {
		return null;
	}

	public Color getLabelColor() {
		return labelColor;
	}

	public void setLabelColor(Color labelColor) {
		this.labelColor = labelColor;
	}

}
