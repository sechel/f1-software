package minimalsurface.frontend.tool;

import halfedge.HalfEdgeDataStructure;
import halfedge.frontend.content.GraphGraphics;
import halfedge.frontend.graphtool.EditOperation;
import halfedge.frontend.graphtool.EditOperationException;
import halfedge.frontend.graphtool.GraphTool;

import java.awt.Color;
import java.awt.Point;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JPanel;
import javax.vecmath.Point2d;

import minimalsurface.controller.MainController;
import circlepatterns.graph.CPEdge;
import circlepatterns.graph.CPFace;
import circlepatterns.graph.CPVertex;



/**
 * Edge length edit tool for the graph editor
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 */
public class ShowFaceIndex implements GraphTool<CPVertex, CPEdge, CPFace> {

	private MainController
		controller = null;
	private Color
		labelColor = Color.RED;
	
	@Override
	public Boolean initTool() {
		return true;
	}

	@Override
	public void leaveTool() {

	}

	@Override
	public void setController(halfedge.frontend.controller.MainController<CPVertex, CPEdge, CPFace> controller) {
		this.controller = (MainController)controller;
	}

	@Override
	public boolean processEditOperation(EditOperation operation) throws EditOperationException {
		controller.refreshEditor();
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
		return "Show Face Index";
	}


	@Override
	public String getDescription() {
		return "Show Face Index";
	}

	@Override
	public String getShortDescription() {
		return "Edge Index";
	}

	
	@Override
	public void paint(GraphGraphics g) {
		HalfEdgeDataStructure<CPVertex, CPEdge, CPFace> graph = controller.getEditedGraph();
		for (CPFace f : graph.getFaces()){
			Point2d mean = new Point2d();
			List<CPEdge> boundary = f.getBoundary();
			for (CPEdge b : boundary)
				mean.add(b.getTargetVertex().getXY());
			mean.scale(1.0 / boundary.size());
			Point drawPos = g.toViewCoord(mean);
			g.getGraphics().setColor(labelColor);
			g.getGraphics().setFont(controller.getFontController().getIndexFont());
			g.getGraphics().drawString(f.getIndex() + "", drawPos.x, drawPos.y);
		}
	}

	@Override
	public boolean needsRepaint() {
		return true;
	}

	@Override
	public JPanel getOptionPanel() {
		return null;
	}

	public Color getLabelColor() {
		return labelColor;
	}

	public void setLabelColor(Color labelColor) {
		this.labelColor = labelColor;
	}

	@Override
	public Icon getIcon() {
		return null;
	}

}
