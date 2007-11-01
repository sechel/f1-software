package halfedge.frontend.graphtool.standard;

import halfedge.Edge;
import halfedge.Face;
import halfedge.HalfEdgeDataStructure;
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
 * The default action
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 */
public class NoGraphAction 
<
	V extends Vertex<V, E, F> & HasXY,
	E extends Edge<V, E, F>, 
	F extends Face<V, E, F>
>  implements GraphTool<V, E, F> {

	private Icon 
		icon = new ImageIcon(ImageHook.getImage("select.png"));
	
	
	public Boolean initTool() {
		return true;
	}

	public void leaveTool() {
		
	}
	
	public void setController(MainController<V, E, F> controller) {

	}

	public boolean processEditOperation(EditOperation operation)
			throws EditOperationException {
		return false;
	}

	public void commitEdit(HalfEdgeDataStructure<V, E, F> graph) {

	}

	public void resetTool() {
		
	}
	
	public String getName() {
		return "Select";
	}

	public Icon getIcon() {
		return icon;
	}

	public String getDescription() {
		return "Select";
	}

	public String getShortDescription() {
		return "Select";
	}

	public void paint(GraphGraphics g) {

	}

	public boolean needsRepaint() {
		return false;
	}
	
	public JPanel getOptionPanel() {
		return null;
	}

}
