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
	
	@Override
	public Boolean initTool() {
		getOptionsPanel().setVisible(true);
		if (getOptionsPanel().getResult() == JOptionPane.OK_OPTION){
			WankelDeform deform = new WankelDeform(getOptionsPanel().getSegments(), getOptionsPanel().getTwist(), getOptionsPanel().getScale());
			controller.setCPMLGraph(deform.getCPML());
		}
		return false;
	}

	@Override
	public void leaveTool() {
		
	}

	@Override
	public void setController(halfedge.frontend.controller.MainController<CPMVertex, CPMEdge, CPMFace> controller) {
		this.controller = (MainController)controller;
	}
	
	
	private WankelDeformOptions getOptionsPanel(){
		if (options == null)
			options = new WankelDeformOptions(controller.getMainFrame(), "Wankel Deform");
		return options;
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
		return "Wankel Deform";
	}

	@Override
	public Icon getIcon() {
		return icon;
	}

	@Override
	public String getDescription() {
		return "Wankel Deform Generator";
	}

	@Override
	public String getShortDescription() {
		return "Generates A Wankel Deform";
	}

	@Override
	public void paint(GraphGraphics g) {
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
