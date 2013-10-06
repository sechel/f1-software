package alexandrov.frontend.tool;

import halfedge.HalfEdgeDataStructure;
import halfedge.frontend.content.GraphGraphics;
import halfedge.frontend.graphtool.EditOperation;
import halfedge.frontend.graphtool.EditOperationException;
import halfedge.frontend.graphtool.GraphTool;
import image.ImageHook;

import java.awt.Color;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.vecmath.Point2d;

import alexandrov.frontend.controller.MainController;
import alexandrov.graph.CPMEdge;
import alexandrov.graph.CPMFace;
import alexandrov.graph.CPMVertex;

public class SetPlanarLengthsTool implements GraphTool<CPMVertex, CPMEdge, CPMFace> {

	private Icon 
		icon = new ImageIcon(ImageHook.getImage("edgelengthset.png"));
	private EditEdgeLength
		edgeLengthTool = new EditEdgeLength();
	private MainController
		controller = null;
	
	
	@Override
	public Boolean initTool() {
		List<CPMEdge> edges = controller.getEditedGraph().getEdges();
		for (CPMEdge e : edges){
			Point2d s = e.getStartVertex().getXY();
			Point2d t = e.getTargetVertex().getXY();
			e.setLength(s.distance(t) / 200);
		}
		return true;
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
	public boolean processEditOperation(EditOperation operation) throws EditOperationException {
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
		return "Edge length generator";
	}

	@Override
	public Icon getIcon() {
		return icon;
	}

	@Override
	public String getDescription() {
		return "Edge length generator";
	}

	@Override
	public String getShortDescription() {
		return "Sets the edge lengths to their lengths in the plane";
	}

	@Override
	public void paint(GraphGraphics g) {
		edgeLengthTool.paint(g);
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
