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
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import alexandrov.Alexandrov;
import alexandrov.Alexandrov2;
import alexandrov.frontend.content.AlexandrovPolytopView;
import alexandrov.frontend.content.CalculationDialog;
import alexandrov.frontend.content.CalculationDialog.CalculationMethod;
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
public class CalculateTool implements GraphTool<CPMVertex, CPMEdge, CPMFace> {

	private Icon 
		icon = new ImageIcon(ImageHook.getImage("run.png"));
	private MainController
		controller = null;
	private AlexandrovPolytopView
		viewer = null;
	private EditEdgeLength
		edgeLengthTool = new EditEdgeLength();
	private CalculationDialog
		optionsDialog = null;
	
	public CalculateTool(AlexandrovPolytopView viewer){
		this.viewer = viewer;
	}
	
	/*
	 * The calculation happens here (non-Javadoc)
	 * @see frontend.halfedgeedit.graphtool.GraphTool#initTool()
	 */
	public Boolean initTool() {
		controller.setStatus("starting calculation...");
		HalfEdgeDataStructure<CPMVertex, CPMEdge, CPMFace> graph = controller.getEditedGraph();
		if (graph == null)
			return false;
		getCalculationDialog().setVisible(true);
		if (getCalculationDialog().getResult() == JOptionPane.CANCEL_OPTION)
			return false;
		try {
			HalfEdgeUtility.removeAllFaces(graph);
			SurfaceUtility.linkAllEdges(graph);
			SurfaceUtility.fillHoles(graph);
		} catch (SurfaceException e1) {
			controller.setStatus(e1.getMessage());
		}
		if (!ConsistencyCheck.isThreeConnected(graph)){
			controller.setStatus("graph not 3-connected");
			return false;
		}
		HalfEdgeDataStructure<CPMVertex, CPMEdge, CPMFace> newGraph = null;
		try {
			newGraph = new HalfEdgeDataStructure<CPMVertex, CPMEdge, CPMFace>(controller.getEditedGraph());
			switch (optionsDialog.getMethod()){
			case SlowAndSafe:
				Alexandrov.constructPolyhedron(newGraph, getCalculationDialog().getInitialRadiusFactor(), getCalculationDialog().getError(), getCalculationDialog().getMaxIterations(), null);
				break;
			case FastButDangerous:
				Alexandrov2.constructPolyhedron(newGraph, getCalculationDialog().getInitialRadiusFactor(), getCalculationDialog().getError(), getCalculationDialog().getMaxIterations(), null, null);
				break;
			}
			controller.setStatus("polytop successfully constructed");
		} catch (Exception e) {
			controller.setStatus(e.getMessage());
			return false;
		}
		viewer.updateGeometry(newGraph);
		viewer.encompass();
		return false;
	}

	public void leaveTool() {

	}

	public void setController(halfedge.frontend.controller.MainController<CPMVertex, CPMEdge, CPMFace> controller) {
		this.controller = (MainController)controller;
		edgeLengthTool.setController(controller);
		edgeLengthTool.setLabelColor(Color.DARK_GRAY);
	}

	private CalculationDialog getCalculationDialog(){
		if (optionsDialog == null){
			optionsDialog = new CalculationDialog(controller.getMainFrame());
			optionsDialog.setMethod(CalculationMethod.SlowAndSafe);
		}
		return optionsDialog;
	}
	
	
	public boolean processEditOperation(EditOperation operation)
			throws EditOperationException {
		return false;
	}

	public void commitEdit(HalfEdgeDataStructure<CPMVertex, CPMEdge, CPMFace> graph) {

	}

	public void resetTool() {

	}

	public String getName() {
		return "Calculate Alexandrov AlexandrovPolyhedron";
	}

	public Icon getIcon() {
		return icon;
	}

	public String getDescription() {
		return "Calculates the new polyeder triangulation";
	}

	public String getShortDescription() {
		return "Begin Calculation";
	}

	public void paint(GraphGraphics g) {
		edgeLengthTool.paint(g);
	}

	public boolean needsRepaint() {
		return true;
	}

	public JPanel getOptionPanel() {
		return null;
	}

}
