package alexandrov.frontend.tool;

import halfedge.HalfEdgeDataStructure;
import halfedge.frontend.content.GraphGraphics;
import halfedge.frontend.graphtool.EditOperation;
import halfedge.frontend.graphtool.EditOperationException;
import halfedge.frontend.graphtool.GraphTool;

import java.awt.BorderLayout;

import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.JPanel;

import alexandrov.frontend.controller.MainController;
import alexandrov.graph.CPMEdge;
import alexandrov.graph.CPMFace;
import alexandrov.graph.CPMVertex;

public class AdvancedGraphEditTool implements GraphTool<CPMVertex, CPMEdge, CPMFace> {

	private MainController controller = null;

	private FullFeaturedEditor
		editor = null;
	private static JFrame 
		frame = new JFrame();
	
	public Boolean initTool() {
//		controller.setUseFaces(true);
		frame.setLayout(new BorderLayout());
		frame.add(editor, BorderLayout.CENTER);
		frame.pack();
		frame.setVisible(true);
		return false;
	}

	public void leaveTool() {
		// TODO Auto-generated method stub

	}

	public void setController(halfedge.frontend.controller.MainController<CPMVertex, CPMEdge, CPMFace> controller) {
		this.controller = (MainController)controller;
		editor = new FullFeaturedEditor(controller.getEditedGraph(), controller);
	}

	public boolean processEditOperation(EditOperation operation)
			throws EditOperationException {
		// TODO Auto-generated method stub
		return false;
	}

	public void commitEdit(HalfEdgeDataStructure<CPMVertex, CPMEdge, CPMFace> graph) {

	}

	public void resetTool() {
		// TODO Auto-generated method stub

	}

	public String getName() {
		// TODO Auto-generated method stub
		return "Edit";
	}

	public Icon getIcon() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getDescription() {
		// TODO Auto-generated method stub
		return "Edit";
	}

	public String getShortDescription() {
		// TODO Auto-generated method stub
		return "Edit";
	}

	public void paint(GraphGraphics g) {

	}

	public boolean needsRepaint() {
		// TODO Auto-generated method stub
		return true;
	}

	public JPanel getOptionPanel() {
		// TODO Auto-generated method stub
		return null;
	}

}
