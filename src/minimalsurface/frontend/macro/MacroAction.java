package minimalsurface.frontend.macro;

import halfedge.HalfEdgeDataStructure;
import image.ImageHook;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import minimalsurface.controller.MainController;
import circlepatterns.graph.CPEdge;
import circlepatterns.graph.CPFace;
import circlepatterns.graph.CPVertex;

public abstract class MacroAction {
	
	private ImageIcon
		defaultIcon = new ImageIcon(ImageHook.getImage("run.png"));
	private MainController
		controller = null;
	protected JPanel
		optionPanel = null;
	
	public void setController(MainController controller){
		this.controller = controller;
	}
	
	public MainController getController(){
		return controller;
	}
	
	public abstract HalfEdgeDataStructure<CPVertex, CPEdge, CPFace> 
		process(HalfEdgeDataStructure<CPVertex, CPEdge, CPFace> graph) throws Exception;
	
	public Icon getIcon(){
		return defaultIcon;
	}
	
	public abstract String getName();
	
	public String getDescription(){
		return getName();
	}

	public JPanel getOptionPanel(){
		if (optionPanel == null) {
			optionPanel = new DefaultOptionPanel();
		}
		return optionPanel;
	}
	
	
	private class DefaultOptionPanel extends JPanel{

		private static final long 
			serialVersionUID = 1L;
		
		public DefaultOptionPanel() {
			add(new JLabel("No Options"));
		}
		
	}
	
}
