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

import java.awt.Color;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.vecmath.Point2d;



/**
 * Adds a face to the active graph
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 */
public class AddFaceAction 
<
	V extends Vertex<V, E, F> & HasXY,
	E extends Edge<V, E, F>, 
	F extends Face<V, E, F>
>  
implements GraphTool<V, E, F> {

	private Icon 
		icon = new ImageIcon(ImageHook.getImage("addface.png"));
	private MainController<V, E, F> 
		controller = null;
	private LinkedList<V>
		vertexList = new LinkedList<V>();
	private boolean
		needsRepaint = false;
	private Point2d
		lastMousePos = new Point2d();
	
	
	@Override
	public Boolean initTool() {
		return true;
	}

	@Override
	public void leaveTool() {
		
	}
	
	@Override
	public void setController(MainController<V, E, F> controller) {
		this.controller = controller;
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean processEditOperation(EditOperation operation)
			throws EditOperationException {
		switch (operation){
			case SELECT_VERTEX:
				if (vertexList.contains(operation.vertex)){
					if (vertexList.size() < 3){
						throw new EditOperationException("Select at least 3 vertices"); 
					}
					if (vertexList.get(0) != operation.vertex){
						throw new EditOperationException("No valid face"); 
					}
					needsRepaint = true;
					return true;
				} else {
					vertexList.add((V)operation.vertex);
					needsRepaint = true;
				}
				break;
			case MOUSE_POS:
				lastMousePos = operation.mousePosition;
				if (!vertexList.isEmpty())
					needsRepaint = true;
				break;
			case CANCEL:
				resetTool();
				needsRepaint = true;
		}
		
		
		return false;
	}

	@Override
	public void commitEdit(HalfEdgeDataStructure<V, E, F> graph) {
		// TODO: buggy!
		F face = graph.addNewFace();
		Iterator<V> it = vertexList.iterator();
		V startVertex = vertexList.getLast();
		E firstEdge = null;
		E lastEdge = null;
		
		// Construnct and link inner rim
		while (it.hasNext()){
			V endVertex = it.next();
			E edge = findEdge(graph, startVertex, endVertex);
			if (edge == null){
				edge = graph.addNewEdge();
				E oppEdge = graph.addNewEdge();
				oppEdge.setTargetVertex(startVertex);
				edge.setTargetVertex(endVertex);
				edge.linkOppositeEdge(oppEdge);
			} 
			if (firstEdge == null)
				firstEdge = edge;
			edge.setLeftFace(face);
			if (lastEdge != null)
				edge.linkPreviousEdge(lastEdge);
			startVertex = endVertex;
			lastEdge = edge;
		}
		firstEdge.linkPreviousEdge(lastEdge);
		firstEdge.getOppositeEdge().linkNextEdge(lastEdge.getOppositeEdge());
		
		// Link outer rim
		E actEdge = firstEdge;
		do {
			E edge = actEdge.getOppositeEdge();
			E rimNext = actEdge.getPreviousEdge().getOppositeEdge();
			
			if (edge.getLeftFace() == null){
				if (rimNext.getLeftFace() == null)
					edge.linkNextEdge(rimNext);
				else
					edge.linkNextEdge(rimNext.getPreviousEdge().getOppositeEdge());
			} else {
				if (rimNext.getLeftFace() == null)
					rimNext.linkPreviousEdge(edge.getNextEdge().getOppositeEdge());
			}
			actEdge = actEdge.getNextEdge();
		} while (actEdge != firstEdge);
		
		resetTool();
	}

	
	@Override
	public void resetTool() {
		vertexList.clear();
	}
	
	
	
	private E findEdge(HalfEdgeDataStructure<V, E, F> graph, Node<V, E, F> v1, Node<V, E, F> v2){
		for (E e : graph.getEdges()){
			if (e.getTargetVertex() == v2 && e.getOppositeEdge().getTargetVertex() == v1)
				return e;
		}
		return null;
	}
	
	
	
	
	@Override
	public String getName() {
		return "Add Face";
	}

	@Override
	public Icon getIcon() {
		return icon;
	}

	@Override
	public String getDescription() {
		return "Add Face";
	}

	@Override
	public String getShortDescription() {
		return "Add Face";
	}

	@Override
	public void paint(GraphGraphics g) {
		if (!vertexList.isEmpty()){
			LinkedList<Point2d> pList = new LinkedList<Point2d>();
			for (V v : vertexList)
				pList.add(v.getXY());
			pList.add(lastMousePos);
			g.getGraphics().setColor(controller.getColorController().getFaceActionColor());
			g.fillFace(pList);
			g.getGraphics().setColor(Color.BLACK);
			g.drawFace(pList);
		}
	}


	@Override
	public boolean needsRepaint() {
		boolean result = needsRepaint;
		needsRepaint = false;
		return result;
	}
	
	
	@Override
	public JPanel getOptionPanel() {
		return null;
	}

}
