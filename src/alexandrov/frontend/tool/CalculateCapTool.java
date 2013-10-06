package alexandrov.frontend.tool;

import halfedge.HalfEdgeDataStructure;
import halfedge.HalfEdgeUtility;
import halfedge.frontend.content.GraphGraphics;
import halfedge.frontend.graphtool.EditOperation;
import halfedge.frontend.graphtool.EditOperationException;
import halfedge.frontend.graphtool.GraphTool;
import halfedge.surfaceutilities.ConsistencyCheck;
import halfedge.surfaceutilities.SurfaceException;
import halfedge.surfaceutilities.SurfaceUtility;
import image.ImageHook;

import java.awt.Color;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.vecmath.Point2d;

import alexandrov.AlexandrovCap;
import alexandrov.frontend.content.AlexandrovPolytopView;
import alexandrov.frontend.controller.MainController;
import alexandrov.graph.CPMEdge;
import alexandrov.graph.CPMFace;
import alexandrov.graph.CPMVertex;



/**
 * Calculates the polyhedron in the default mode
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 */
public class CalculateCapTool implements GraphTool<CPMVertex, CPMEdge, CPMFace> {

	private Icon 
		icon = new ImageIcon(ImageHook.getImage("run.png"));
	private MainController
		controller = null;
	private AlexandrovPolytopView
		viewer = null;
	private EditEdgeLength
		edgeLengthTool = new EditEdgeLength();
	
	public CalculateCapTool(AlexandrovPolytopView viewer){
		this.viewer = viewer;
	}
	
	/*
	 * The calculation happens here (non-Javadoc)
	 * @see frontend.halfedgeedit.graphtool.GraphTool#initTool()
	 */
	@Override
	public Boolean initTool() {
		HalfEdgeDataStructure<CPMVertex, CPMEdge, CPMFace> graph = controller.getEditedGraph();
		if (graph == null)
			return false;
		try {
			HalfEdgeUtility.removeAllFaces(graph);
			SurfaceUtility.linkAllEdges(graph);
			SurfaceUtility.fillHoles(graph);
			SurfaceUtility.constructBoundary(graph);
		} catch (SurfaceException e1) {
			controller.setStatus(e1.getMessage());
		}
		if (!ConsistencyCheck.isThreeConnected(graph)){
			controller.setStatus("graph not 3-connected");
			return false;
		}
		if (!halfedge.triangulationutilities.ConsistencyCheck.isDisk(graph)){
			controller.setStatus("Graph is no disk!");
			return false;
		}			
		HalfEdgeDataStructure<CPMVertex, CPMEdge, CPMFace> newGraph = null;
		try {
			newGraph = new HalfEdgeDataStructure<CPMVertex, CPMEdge, CPMFace>(controller.getEditedGraph());
			AlexandrovCap.constructCap(newGraph, 1E-5, 200);
			controller.setStatus("cap successfully constructed");
		} catch (Exception e) {
			controller.setStatus(e.getMessage());
			return false;
		}
		viewer.updateGeometry(newGraph);
		viewer.encompass();
		return false;
	}

	@Override
	public void leaveTool() {

	}

	
	@Override
	public void setController(halfedge.frontend.controller.MainController<CPMVertex, CPMEdge, CPMFace> controller) {
		this.controller = (MainController)controller;
		edgeLengthTool.setController(controller);
		edgeLengthTool.setLabelColor(Color.DARK_GRAY);
	}

	@Override
	public boolean processEditOperation(EditOperation operation)
			throws EditOperationException {
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
		return "Calculate Cap";
	}

	@Override
	public Icon getIcon() {
		return icon;
	}

	@Override
	public String getDescription() {
		return "Calculates a cap";
	}

	@Override
	public String getShortDescription() {
		return "Calculation Cap";
	}

	@Override
	public void paint(GraphGraphics g) {
		edgeLengthTool.paint(g);
		g.getGraphics().setColor(Color.MAGENTA);
		for (CPMEdge e : controller.getEditedGraph().getEdges()){
			if (e.isBoundary()){
				Point2d p1 = e.getTargetVertex().getXY();
				Point2d p2 = e.getOppositeEdge().getTargetVertex().getXY();
				g.drawEdge(p1, p2);
			}
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

}
