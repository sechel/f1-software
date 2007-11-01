package alexandrov.frontend.tool.egg;

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

public class EggGeneratorTool implements GraphTool<CPMVertex, CPMEdge, CPMFace> {

	private Icon 
		icon = new ImageIcon(ImageHook.getImage("egg.png"));
	private MainController
		controller = null;
	private EggGeneratorOptions	
		options = null;
	
	public Boolean initTool() {
		getOptionsPanel().setVisible(true);
		if (getOptionsPanel().getResult() == JOptionPane.OK_OPTION){
			EggGenerator egg = new EggGenerator<CPMVertex, CPMEdge, CPMFace>(
					getOptionsPanel().getSegments(), 
					getOptionsPanel().getRes(), 
					getOptionsPanel().getEccentricity(), 
					CPMVertex.class, 
					CPMEdge.class, 
					CPMFace.class);
			controller.setCPMLGraph(egg.getCPML());
		}
		return false;
	}

	public void leaveTool() {
		
	}

	public void setController(halfedge.frontend.controller.MainController<CPMVertex, CPMEdge, CPMFace> controller) {
		this.controller = (MainController)controller;
	}

	
	private EggGeneratorOptions getOptionsPanel(){
		if (options == null)
			options = new EggGeneratorOptions(controller.getMainFrame(), "Egg breeder");
		return options;
	}
	
	public boolean processEditOperation(EditOperation operation) throws EditOperationException {
		return false;
	}

	public void commitEdit(HalfEdgeDataStructure graph) {
		
	}

	public void resetTool() {
		
	}

	public String getName() {
		return "Egg breeder";
	}

	public Icon getIcon() {
		return icon;
	}

	public String getDescription() {
		return "Egg breeder";
	}

	public String getShortDescription() {
		return "Egg breeder";
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
