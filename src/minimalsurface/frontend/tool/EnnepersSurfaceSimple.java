package minimalsurface.frontend.tool;

import halfedge.HalfEdgeDataStructure;
import halfedge.frontend.content.GraphGraphics;
import halfedge.frontend.graphtool.EditOperation;
import halfedge.frontend.graphtool.EditOperationException;
import halfedge.frontend.graphtool.GraphTool;
import halfedge.generator.SquareGridGenerator;
import halfedge.surfaceutilities.SurfaceException;
import halfedge.surfaceutilities.SurfaceUtility;
import image.ImageHook;

import java.awt.Point;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

import koebe.KoebePolyhedron;
import minimalsurface.controller.MainController;
import minimalsurface.util.MinimalSurfaceUtility;
import circlepatterns.graph.CPEdge;
import circlepatterns.graph.CPFace;
import circlepatterns.graph.CPVertex;
import circlepatterns.layout.CPLayout;

public class EnnepersSurfaceSimple implements GraphTool<CPVertex, CPEdge, CPFace> {

	protected Icon 
		icon = new ImageIcon(ImageHook.getImage("enneper.png"));
	private MainController
		controller = null;
	
	public Boolean initTool() {
		try {
			HalfEdgeDataStructure<CPVertex, CPEdge, CPFace> graph = SquareGridGenerator.generate(50, 50, CPVertex.class, CPEdge.class, CPFace.class);
			SquareGridGenerator.setSquareGridThetas(graph, 0, 0);
			for (CPFace f : graph.getFaces()){
				f.setRho(0.0);
				f.setRadius(10.0);
			}
			CPLayout.calculateEuclidean(graph);
			MinimalSurfaceUtility.createEdgeLabels(graph);
			KoebePolyhedron.normalizeBeforeProjection(graph);
			KoebePolyhedron.inverseStereographicProjection(graph, 1);
			MinimalSurfaceUtility.dualizeSurfaceConformal(graph, true);
			SurfaceUtility.rescaleSurface(graph, 2.0);
			controller.setEditedGraph(graph);
			controller.fireGraphChanged();
			controller.getViewer().resetGeometry();
			controller.getViewer().addSurface(graph);
			controller.getViewer().encompass();
		} catch (SurfaceException e) {
			controller.setStatus(e.getMessage());
		}
		return true;
	}
	
	
	public void commitEdit(HalfEdgeDataStructure<CPVertex, CPEdge, CPFace> graph) {
		
	}

	public String getDescription() {
		return getShortDescription();
	}

	public Icon getIcon() {
		return icon;
	}

	public String getName() {
		return "Enneper's Surface";
	}

	public JPanel getOptionPanel() {
		return null;
	}

	public String getShortDescription() {
		return "Create Enneper's surface";
	}


	public void leaveTool() {

	}

	public boolean needsRepaint() {
		return true;
	}

	public void paint(GraphGraphics g) {
		for (CPEdge e : controller.getEditedGraph().getPositiveEdges()){
			g.getGraphics().setFont(controller.getFontController().getIndexFont());
			g.getGraphics().setColor(controller.getColorController().getIndexColor());
			Point p1 = g.toViewCoord(e.getTargetVertex().getXY());
			Point p2 = g.toViewCoord(e.getOppositeEdge().getTargetVertex().getXY());
			String label = e.getLabel() ? "1" : "0";
			g.getGraphics().drawString(label, (p1.x + p2.x) / 2, (p1.y + p2.y) / 2);
		}
	}

	public boolean processEditOperation(EditOperation operation)
			throws EditOperationException {
		return false;
	}

	public void resetTool() {

	}

	public void setController(halfedge.frontend.controller.MainController<CPVertex, CPEdge, CPFace> controller) {
		this.controller = (MainController)controller;
	}

}
