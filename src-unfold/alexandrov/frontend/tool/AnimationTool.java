package alexandrov.frontend.tool;

import image.ImageHook;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

import util.debug.DBGTracer;
import halfedge.HalfEdgeDataStructure;
import halfedge.frontend.content.GraphGraphics;
import halfedge.frontend.graphtool.EditOperation;
import halfedge.frontend.graphtool.EditOperationException;
import halfedge.frontend.graphtool.GraphTool;
import alexandrov.frontend.AlexandrovsPolyhedron;
import alexandrov.frontend.action.MainWindowClosing;
import alexandrov.frontend.controller.MainController;
import alexandrov.frontend.controls.AnimationControls;
import alexandrov.frontend.controls.UnfoldControls;
import alexandrov.graph.CPMEdge;
import alexandrov.graph.CPMFace;
import alexandrov.graph.CPMVertex;

public class AnimationTool implements GraphTool<CPMVertex, CPMEdge, CPMFace>{

	private Icon icon = new ImageIcon(ImageHook.getImage("film.png"));
	
	boolean toggle = false;
	boolean first = true;

	private MainController controller = null;

	private AnimationControls
		controlsPanel = null;
	
	public Boolean initTool() {
		if (!controlsPanel.isVisible())
			controlsPanel.setVisible(true);

		return true;
	}

	public void leaveTool() {
		// TODO Auto-generated method stub

	}

	public void setController(halfedge.frontend.controller.MainController<CPMVertex, CPMEdge, CPMFace> controller) {
		this.controller = (MainController)controller;
		controlsPanel = new AnimationControls(this.controller);
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
		return "Export animation";
	}

	public Icon getIcon() {
		// TODO Auto-generated method stub
		return icon;
	}

	public String getDescription() {
		// TODO Auto-generated method stub
		return "Export animation";
	}

	public String getShortDescription() {
		// TODO Auto-generated method stub
		return "Export animation";
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
	
	public static void main(String[] args) {
		DBGTracer.setActive(false);
		AlexandrovsPolyhedron mainApp = new AlexandrovsPolyhedron();
		
		mainApp.setVisible(true);
		mainApp.setSize(1000, 600);
		mainApp.addWindowListener(new MainWindowClosing());
		mainApp.updateView();
		mainApp.validate();
		mainApp.addTool(new UnfoldTool());
		mainApp.addTool(new AdvancedGraphEditTool());
	}	
	
}
