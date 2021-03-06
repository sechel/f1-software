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
import javax.vecmath.Point2d;



/**
 * Adds a vertex to the given graph
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 */
public class AddVertexAction
<
	V extends Vertex<V, E, F> & HasXY,
	E extends Edge<V, E, F>, 
	F extends Face<V, E, F>
>  implements GraphTool<V, E, F> {

	private Icon 
		icon = new ImageIcon(ImageHook.getImage("addvertex.png"));
	private MainController<V, E, F> 
		controller = null;
	private Point2d
		lastMousePos = new Point2d(),
		selectedPos = new Point2d();

	
	@Override
	public Boolean initTool() {
		return true;
	}
	
	@Override
	public void leaveTool() {
		
	}
	
	@Override
	public boolean processEditOperation(EditOperation operation)
			throws EditOperationException {
		
		switch (operation){
			case MOUSE_POS:
				lastMousePos = operation.mousePosition;
				return false;
			case SELECT_POSITION:
				selectedPos = operation.mousePosition;
				return true;
		}
		
		return false;
	}

	
	@Override
	public void commitEdit(HalfEdgeDataStructure<V, E, F> graph) {
		V v = graph.addNewVertex();
		v.getXY().set(selectedPos);
	}
	
	@Override
	public void resetTool() {
		
	}
	
	
	
	@Override
	public String getName() {
		return "Add Vertex";
	}

	@Override
	public Icon getIcon() {
		return icon;
	}

	@Override
	public String getDescription() {
		return "Adds a Vertex to the graph";
	}

	@Override
	public String getShortDescription() {
		return "Add Vertex";
	}

	@Override
	public void paint(GraphGraphics g) {
		g.getGraphics().setColor(controller.getColorController().getVertexActionColor());
		g.drawVertex(lastMousePos);
	}


	@Override
	public boolean needsRepaint() {
		return true;
	}
	
	@Override
	public void setController(MainController<V, E, F> controller) {
		this.controller = controller;
	}

	@Override
	public JPanel getOptionPanel() {
		return null;
	}
	
}
