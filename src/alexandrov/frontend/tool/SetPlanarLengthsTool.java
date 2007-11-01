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
	
	
	public Boolean initTool() {
		List<CPMEdge> edges = controller.getEditedGraph().getEdges();
		for (CPMEdge e : edges){
			Point2d s = e.getStartVertex().getXY();
			Point2d t = e.getTargetVertex().getXY();
			e.setLength(s.distance(t) / 200);
		}
		return true;
	}

	public void leaveTool() {
		
	}

	public void setController(halfedge.frontend.controller.MainController<CPMVertex, CPMEdge, CPMFace> controller) {
		this.controller = (MainController)controller;
		edgeLengthTool.setController(controller);
		edgeLengthTool.setLabelColor(Color.DARK_GRAY);
	}

	public boolean processEditOperation(EditOperation operation) throws EditOperationException {
		return false;
	}

	public void commitEdit(HalfEdgeDataStructure<CPMVertex, CPMEdge, CPMFace> graph) {
		
	}

	public void resetTool() {
		
	}

	public String getName() {
		return "Edge length generator";
	}

	public Icon getIcon() {
		return icon;
	}

	public String getDescription() {
		return "Edge length generator";
	}

	public String getShortDescription() {
		return "Sets the edge lengths to their lengths in the plane";
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
