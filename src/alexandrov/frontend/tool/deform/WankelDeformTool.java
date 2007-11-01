package alexandrov.frontend.tool.deform;

import halfedge.HalfEdgeDataStructure;
import halfedge.frontend.content.GraphGraphics;
import halfedge.frontend.graphtool.EditOperation;
import halfedge.frontend.graphtool.EditOperationException;
import halfedge.frontend.graphtool.GraphTool;
import image.ImageHook;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import alexandrov.frontend.controller.MainController;
import alexandrov.graph.CPMEdge;
import alexandrov.graph.CPMFace;
import alexandrov.graph.CPMVertex;

public class WankelDeformTool implements GraphTool<CPMVertex, CPMEdge, CPMFace> {

	private Icon 
		icon = new ImageIcon(ImageHook.getImage("wankeldeform2.png"));
	private MainController
		controller = null;
	private WankelDeformOptions	
		options = null;
	
	public Boolean initTool() {
		getOptionsPanel().setVisible(true);
		if (getOptionsPanel().getResult() == JOptionPane.OK_OPTION){
			WankelDeform deform = new WankelDeform(getOptionsPanel().getSegments(), getOptionsPanel().getTwist(), getOptionsPanel().getScale());
			controller.setCPMLGraph(deform.getCPML());
		}
		return false;
	}

	public void leaveTool() {
		
	}

	public void setController(halfedge.frontend.controller.MainController<CPMVertex, CPMEdge, CPMFace> controller) {
		this.controller = (MainController)controller;
	}
	
	
	private WankelDeformOptions getOptionsPanel(){
		if (options == null)
			options = new WankelDeformOptions(controller.getMainFrame(), "Wankel Deform");
		return options;
	}
	

	public boolean processEditOperation(EditOperation operation) throws EditOperationException {
		return false;
	}

	public void commitEdit(HalfEdgeDataStructure<CPMVertex, CPMEdge, CPMFace> graph) {
		
	}

	public void resetTool() {
		
	}

	public String getName() {
		return "Wankel Deform";
	}

	public Icon getIcon() {
		return icon;
	}

	public String getDescription() {
		return "Wankel Deform Generator";
	}

	public String getShortDescription() {
		return "Generates A Wankel Deform";
	}

	public void paint(GraphGraphics g) {
	}

	public boolean needsRepaint() {
		return true;
	}

	public JPanel getOptionPanel() {
		return null;
	}

}
