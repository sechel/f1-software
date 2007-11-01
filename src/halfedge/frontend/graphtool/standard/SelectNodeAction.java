package halfedge.frontend.graphtool.standard;

import halfedge.Edge;
import halfedge.Face;
import halfedge.HalfEdgeDataStructure;
import halfedge.Node;
import halfedge.Vertex;
import halfedge.decorations.HasXY;
import halfedge.frontend.content.GraphGraphics;
import halfedge.frontend.controller.MainController;
import halfedge.frontend.graphtool.EditOperation;
import halfedge.frontend.graphtool.EditOperationException;
import halfedge.frontend.graphtool.GraphTool;
import image.ImageHook;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;



/**
 * Selects the picked node
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 */
public class SelectNodeAction
<
	V extends Vertex<V, E, F> & HasXY,
	E extends Edge<V, E, F>, 
	F extends Face<V, E, F>
>  implements GraphTool<V, E, F> {

	private Icon 
		icon = new ImageIcon(ImageHook.getImage("select.png"));
	private MainController<V, E, F>
		controller = null;
	private boolean 
		needsRepaint = false;
	
	
	public void setController(MainController<V, E, F> controller) {
		this.controller = controller;
	}

	
	public Boolean initTool() {
		return true;
	}
	
	public void leaveTool() {
		
	}

	public boolean processEditOperation(EditOperation operation)
			throws EditOperationException {
		switch (operation){
			case SELECT_VERTEX:
				if (controller.getNodeController().isNodeSelected(operation.vertex))
					controller.getNodeController().unSelectNode(operation.vertex);
				else
					controller.getNodeController().selectNodeExclusiv(operation.vertex);
				controller.getNodeController().showOptions(operation.vertex);
				needsRepaint = true;
				break;
			case SELECT_EDGE:
				if (controller.getNodeController().isNodeSelected(operation.edge))
					controller.getNodeController().unSelectNode(operation.edge);
				else
					controller.getNodeController().selectNodeExclusiv(operation.edge);
				controller.getNodeController().showOptions(operation.edge);
				needsRepaint = true;
				break;
			case SELECT_FACE:
				if (controller.getNodeController().isNodeSelected(operation.face))
					controller.getNodeController().unSelectNode(operation.face);
				else
					controller.getNodeController().selectNodeExclusiv(operation.face);
				controller.getNodeController().showOptions(operation.face);
				needsRepaint = true;
				break;
			case CANCEL:
				controller.getNodeController().unselectAll();
				controller.getNodeController().showOptions((Node<V, E, F>)null);
				needsRepaint = true;
		}
		
		return false;
	}

	
	public void commitEdit(HalfEdgeDataStructure<V, E, F> graph) {
		
	}
	
	public void resetTool() {
		
	}
	
	
	public String getName() {
		return "Select Node";
	}

	public Icon getIcon() {
		return icon;
	}

	public String getDescription() {
		return "Select Node";
	}

	public String getShortDescription() {
		return "Select Node";
	}


	public void paint(GraphGraphics g) {

	}

	public boolean needsRepaint() {
		boolean result = needsRepaint;
		needsRepaint = false;
		return result;
	}
	

	public JPanel getOptionPanel() {
		return null;
	}


}
