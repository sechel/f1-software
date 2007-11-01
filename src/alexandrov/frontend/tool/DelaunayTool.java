package alexandrov.frontend.tool;

import halfedge.HalfEdgeDataStructure;
import halfedge.frontend.content.GraphGraphics;
import halfedge.frontend.graphtool.EditOperation;
import halfedge.frontend.graphtool.EditOperationException;
import halfedge.frontend.graphtool.GraphTool;
import halfedge.surfaceutilities.ConsistencyCheck;
import halfedge.surfaceutilities.SurfaceException;
import halfedge.surfaceutilities.SurfaceUtility;
import halfedge.triangulationutilities.Delaunay;
import halfedge.triangulationutilities.TriangulationException;

import java.awt.Point;

import javax.swing.Icon;
import javax.swing.JPanel;
import javax.vecmath.Point2d;

import alexandrov.frontend.controller.MainController;
import alexandrov.graph.CPMEdge;
import alexandrov.graph.CPMFace;
import alexandrov.graph.CPMVertex;


/**
 * Calculates the Delaunay triangulation of the active graph.
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 */
public class DelaunayTool implements GraphTool<CPMVertex, CPMEdge, CPMFace> {

	private MainController
		controller = null;
	
	public Boolean initTool() {
		HalfEdgeDataStructure<CPMVertex, CPMEdge, CPMFace> graph = controller.getEditedGraph();
		try {
			SurfaceUtility.linkAllEdges(graph);
			SurfaceUtility.fillHoles(graph);
		} catch (SurfaceException e1) {
			controller.setStatus(e1.getMessage());
			return false;
		}
		try {
			Delaunay.constructDelaunay(graph);
		} catch (TriangulationException e) {
			controller.setStatus(e.getMessage());
		}
		if (!ConsistencyCheck.isValidSurface(graph))
			controller.setStatus("No valid surface after Delaunay construction!");
		if (!halfedge.triangulationutilities.ConsistencyCheck.isTriangulation(graph))
			controller.setStatus("No valid triangulation after Delaunay construction!");		
		controller.refreshEditor();
		return false;
	}

	public void leaveTool() {

	}

	public void setController(halfedge.frontend.controller.MainController<CPMVertex, CPMEdge, CPMFace> controller) {
		this.controller = (MainController)controller;
	}

	@SuppressWarnings("unchecked")
	public boolean processEditOperation(EditOperation operation) throws EditOperationException {

		return false;
	}

	public void commitEdit(HalfEdgeDataStructure<CPMVertex, CPMEdge, CPMFace> graph) {

	}

	public void resetTool() {

	}

	public String getName() {
		return "Delaunay Tool";
	}

	public Icon getIcon() {
		return null;
	}

	public String getDescription() {
		return "Delaunay Tool";
	}

	public String getShortDescription() {
		return "Delaunay Tool";
	}

	public void paint(GraphGraphics g) {
		HalfEdgeDataStructure<CPMVertex, CPMEdge, CPMFace> graph = controller.getEditedGraph();
		for (CPMEdge edge : graph.getEdges()){
			Point2d t = edge.getTargetVertex().getXY();
			Point2d s = edge.getStartVertex().getXY();
			Point2d mid = new Point2d((s.x + t.x) / 2, (s.y + t.y) / 2);
			Point drawPos = g.toViewCoord(mid);
			g.getGraphics().drawString(edge.getLength() + "", drawPos.x, drawPos.y);
		}
	}

	public boolean needsRepaint() {
		return true;
	}

	public JPanel getOptionPanel() {
		return null;
	}

	
	
	protected void makeDelaunay(){

	}
	
	
	
}
