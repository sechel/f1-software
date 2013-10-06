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
import minimalsurface.util.GraphUtility;
import minimalsurface.util.MinimalSurfaceUtility;
import circlepatterns.graph.CPEdge;
import circlepatterns.graph.CPFace;
import circlepatterns.graph.CPVertex;
import circlepatterns.layout.CPLayout;

public class EnnepersSurface implements GraphTool<CPVertex, CPEdge, CPFace> {

	protected Icon 
		icon = new ImageIcon(ImageHook.getImage("enneper.png"));
	private MainController
		controller = null;
	
	@Override
	public Boolean initTool() {
		try {
			HalfEdgeDataStructure<CPVertex, CPEdge, CPFace> graph = SquareGridGenerator.generate(30, 30, CPVertex.class, CPEdge.class, CPFace.class);
			SquareGridGenerator.setSquareGridThetas(graph, 0, 0);
			for (CPFace f : graph.getFaces()){
				f.setRho(0.0);
				f.setRadius(10.0);
			}
			CPLayout.calculateEuclidean(graph);
			KoebePolyhedron.normalizeBeforeProjection(graph, 1.5);
			
			KoebePolyhedron.inverseStereographicProjection(graph, 1.0);
			for (CPFace f : graph.getFaces())
				KoebePolyhedron.calculateConePeek(f.getXYZW(), f.getBoundaryEdge());
			
			GraphUtility.twoColoring(graph);
			
			HalfEdgeDataStructure<CPVertex, CPEdge, CPFace> quadGraph = MinimalSurfaceUtility.createFromMedial(graph);
			
			SurfaceUtility.rescaleSurface(quadGraph, 2.0);
			controller.setEditedGraph(quadGraph);
			controller.fireGraphChanged();
			controller.getViewer().resetGeometry();
			controller.getViewer().addSurface(quadGraph);
			controller.getViewer().encompass();
		} catch (SurfaceException e) {
			controller.setStatus(e.getMessage());
		}
		return true;
	}
	
	
	@Override
	public void commitEdit(HalfEdgeDataStructure<CPVertex, CPEdge, CPFace> graph) {
		
	}

	@Override
	public String getDescription() {
		return getShortDescription();
	}

	@Override
	public Icon getIcon() {
		return icon;
	}

	@Override
	public String getName() {
		return "Enneper's Surface Advanced";
	}

	@Override
	public JPanel getOptionPanel() {
		return null;
	}

	@Override
	public String getShortDescription() {
		return "Create Enneper's surface";
	}


	@Override
	public void leaveTool() {

	}

	@Override
	public boolean needsRepaint() {
		return true;
	}

	@Override
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

	@Override
	public boolean processEditOperation(EditOperation operation)
			throws EditOperationException {
		return false;
	}

	@Override
	public void resetTool() {

	}

	@Override
	public void setController(halfedge.frontend.controller.MainController<CPVertex, CPEdge, CPFace> controller) {
		this.controller = (MainController)controller;
	}

}
