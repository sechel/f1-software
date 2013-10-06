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
	
	
	@Override
	public Boolean initTool() {
		return true;
	}

	@Override
	public void leaveTool() {
		
	}
	
	@Override
	public void setController(MainController<V, E, F> controller) {

	}

	@Override
	public boolean processEditOperation(EditOperation operation)
			throws EditOperationException {
		return false;
	}

	@Override
	public void commitEdit(HalfEdgeDataStructure<V, E, F> graph) {

	}

	@Override
	public void resetTool() {
		
	}
	
	@Override
	public String getName() {
		return "Select";
	}

	@Override
	public Icon getIcon() {
		return icon;
	}

	@Override
	public String getDescription() {
		return "Select";
	}

	@Override
	public String getShortDescription() {
		return "Select";
	}

	@Override
	public void paint(GraphGraphics g) {

	}

	@Override
	public boolean needsRepaint() {
		return false;
	}
	
	@Override
	public JPanel getOptionPanel() {
		return null;
	}

}
