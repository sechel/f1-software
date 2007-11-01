package halfedge.frontend.content;

import static halfedge.frontend.graphtool.EditOperation.SELECT_EDGE;
import static halfedge.frontend.graphtool.EditOperation.SELECT_FACE;
import static halfedge.frontend.graphtool.EditOperation.SELECT_VERTEX;
import halfedge.Edge;
import halfedge.Face;
import halfedge.HalfEdgeDataStructure;
import halfedge.Node;
import halfedge.Vertex;
import halfedge.decorations.HasXY;
import halfedge.frontend.controller.MainController;
import halfedge.frontend.graphtool.EditOperation;
import halfedge.frontend.graphtool.EditOperationException;
import halfedge.frontend.graphtool.GraphTool;

import java.awt.Color;
import java.awt.Point;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.util.LinkedList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JPanel;
import javax.vecmath.Point2d;


/**
 * The internal tool for moving, dragging and operation translation
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 */
public class InternalTool 
<
	V extends Vertex<V, E, F> & HasXY,
	E extends Edge<V, E, F>, 
	F extends Face<V, E, F>
>  implements GraphTool<V, E, F> {

	private MainController<V, E, F>
		controller = null;
	private boolean 
		needsRepaint = false;
	private Point2d
		lastMousePos = new Point2d();
	private double
		selectEPS = 5;
	private V
		draggedVertex = null;
	private E
		draggedEdge = null;
	private Point2d
		edgeDraggStart = new Point2d(),
		faceDraggStart = new Point2d();
	private Point
		moveStart = new Point();
	private EditPanel<V, E, F>
		editPanel = null;
	private V
		vertexUnderMouse = null;
	private E
		edgeUnderMouse = null;
	private F
		faceUnderMouse = null,
		draggedFace = null;
	
	public InternalTool(EditPanel<V, E, F> editPanel){
		this.editPanel = editPanel;
	}
	
	
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
			case MOUSE_POS:
				lastMousePos.set(operation.mousePosition);
				recognizeNodeAt(lastMousePos);
				break;
			case SELECT_POSITION:
				return selectNodeAt(operation.mousePosition);
			case DRAG_BEGIN:
				lastMousePos.set(operation.mousePosition);
				moveStart.setLocation(operation.mouseEvent.getPoint());
				draggedVertex = getVertexAt(operation.mousePosition);
				if (draggedVertex != null){
					draggedEdge = null;
					draggedFace = null;
					return false;
				}
				draggedEdge = getEdgeAt(operation.mousePosition);
				if (draggedEdge != null){
					edgeDraggStart.set(operation.mousePosition);
					draggedFace = null;
					return false;
				}
				draggedFace = getFaceAt(operation.mousePosition);
				if (draggedFace != null){
					faceDraggStart.set(operation.mousePosition);
					return false;
				}
				break;
			case DRAG_END:
				if (draggedVertex != null || draggedEdge != null || draggedFace != null)
					controller.fireGraphChanged();
				draggedVertex = null;
				draggedEdge = null;
				draggedFace = null;
				break;
			case DRAG_TO:
					// move vertex
				if (draggedVertex != null){
					draggedVertex.setXY(operation.mousePosition);
					needsRepaint = true;	
				} else // move edge
				if (draggedEdge != null){
					double dx = operation.mousePosition.x - edgeDraggStart.x;
					double dy = operation.mousePosition.y - edgeDraggStart.y;
					V v1 = draggedEdge.getTargetVertex();
					V v2 = draggedEdge.getOppositeEdge().getTargetVertex();
					v1.getXY().set(v1.getXY().x + dx, v1.getXY().y + dy);
					v2.getXY().set(v2.getXY().x + dx, v2.getXY().y + dy);
					edgeDraggStart.set(operation.mousePosition);
					needsRepaint = true;
				} else // move face
				if (draggedFace != null){
					double dx = operation.mousePosition.x - faceDraggStart.x;
					double dy = operation.mousePosition.y - faceDraggStart.y;
					List<Point2d> vertexCoordinateList = makePointList(draggedFace);
					for (Point2d p : vertexCoordinateList)
						p.set(p.x + dx, p.y + dy);
					faceDraggStart.set(operation.mousePosition);
					needsRepaint = true;
				} else { // window move
					editPanel.center.x += moveStart.x - operation.mouseEvent.getPoint().x;
					editPanel.center.y += moveStart.y - operation.mouseEvent.getPoint().y;
					moveStart.setLocation(operation.mouseEvent.getPoint());
					needsRepaint = true;
				}
				break;
			case KEY_TYPED:
				if (operation.keyEvent.getKeyChar() == 'e')
					encompass();
				break;
		}
		return false;
	}

	
	public void commitEdit(HalfEdgeDataStructure<V, E, F> graph) {
		
	}
	
	public void resetTool() {
		
	}
	
	
	public String getName() {
		return null;
	}

	public Icon getIcon() {
		return null;
	}

	public String getDescription() {
		return null;
	}

	public String getShortDescription() {
		return null;
	}

	
	private void recognizeNodeAt(Point2d p){
		V vertex = getVertexAt(p);
		if (vertex != null){
			if (vertexUnderMouse != vertex)
				needsRepaint = true;
			vertexUnderMouse = vertex;
			edgeUnderMouse = null;
			faceUnderMouse = null;
			return;
		} 
		E edge = getEdgeAt(p);
		if (edge != null){
			if (edgeUnderMouse != edge)
				needsRepaint = true;
			edgeUnderMouse = edge;
			vertexUnderMouse = null;
			faceUnderMouse = null;
			return;
		} 
		if (controller.usesFaces()){
			F face = getFaceAt(p);
			if (face != null){
				if (faceUnderMouse != face)
					needsRepaint = true;
				faceUnderMouse = face;
				vertexUnderMouse = null;
				edgeUnderMouse = null;
				return;
			}
		}
		if (vertexUnderMouse != null || edgeUnderMouse != null || faceUnderMouse != null)
			needsRepaint = true;
		vertexUnderMouse = null;
		edgeUnderMouse = null;
		faceUnderMouse = null;
	}
	
	public void encompass(){
//		Dimension size = editPanel.getSize();
//		double x = 0;
//		double y = 0;
//		double maxX = -Double.MAX_VALUE;
//		double minX = Double.MAX_VALUE;
//		double maxY = -Double.MAX_VALUE;
//		double minY = Double.MAX_VALUE;
//		for (HasXY xy :controller.getEditedGraph().getVertices()){
//			x += xy.getXY().x;
//			y += xy.getXY().y;
//			if (xy.getXY().x > maxX)
//				maxX = xy.getXY().x;
//			if (xy.getXY().x < minX)
//				minX = xy.getXY().x; 
//			if (xy.getXY().y > maxY)
//				maxY = xy.getXY().y; 
//			if (xy.getXY().y < minY)
//				minY = xy.getXY().y; 
//		}
//		double scaleX = Math.abs(maxX - minX);
//		double scaleY = Math.abs(maxY - minY);
//		double scale = Math.max(scaleX, scaleY);
//		x /= controller.getEditedGraph().getNumVertices();
//		y /= controller.getEditedGraph().getNumVertices();
//		Point2d vecCenter = new Point2d(x - size.width / 2, y - size.height / 2);
//		editPanel.scale = 200 / scale;
//		Point center = editPanel.graphics.toViewCoord(vecCenter);
//		editPanel.center = center;
//		editPanel.repaint();
	}
	
	private boolean selectNodeAt(Point2d p){
		EditOperation op = null;
		
		V vertex = getVertexAt(p);
		if (vertex != null){
			op = SELECT_VERTEX;
			op.vertex = vertex;
		} else {
			E edge = getEdgeAt(p);
			if (edge != null){
				op = SELECT_EDGE;
				if (!controller.getNodeController().getSelectedNodes().isEmpty()){
					if (controller.getNodeController().getSelectedNodes().get(0) == edge)
						edge = edge.getOppositeEdge();
				}
				op.edge = edge;
			} else {
				F face = getFaceAt(p);
				if (face != null){
					op = SELECT_FACE;
					op.face = face;
				}
			}
		}
		if (op != null){
			needsRepaint = true;
			try {
				if (controller.getToolController().getActiveTool().processEditOperation(op)){
					controller.getToolController().getActiveTool().commitEdit(controller.getEditedGraph());
				}
				return true;
			} catch (EditOperationException e) {
				return false;
			}
		}
		return false;
	}
	
	
	private V getVertexAt(Point2d p){
		for (V v : controller.getEditedGraph().getVertices())
			if (cursorIsAtVertex(v))
				return v;
		return null;
	}
	
	private E getEdgeAt(Point2d p){
		for (E e : controller.getEditedGraph().getEdges())
			if (cursorIsAtEdge(e))
				return e;
		return null;
	}
	
	
	private F getFaceAt(Point2d p){
		if (!controller.usesFaces())
			return null;
		for (F f : controller.getEditedGraph().getFaces())
			if (cursorIsAtFace(f))
				return f;
		return null;
	}
	
	@SuppressWarnings("unused")
	private Node<?, ?, ?> getNodeAt(Point2d p){
		Node<?, ?, ?> n = getVertexAt(p);
		if (n != null)
			return n;
		n = getEdgeAt(p);
		if (n != null)
			return n;
		n = getFaceAt(p);
		if (n != null)
			return n;
		return null;
	}
	
	
	private boolean cursorIsAt(Point2d p){
		return (Math.abs(lastMousePos.x - p.x) <= (selectEPS / editPanel.scale) &&
				Math.abs(lastMousePos.y - p.y) <= (selectEPS / editPanel.scale));
	}
	
	
	private boolean cursorIsAtVertex(V v){
		return cursorIsAt(v.getXY());
	}
	
	private boolean cursorIsAtEdge(E e){
		Point2d s = e.getOppositeEdge().getTargetVertex().getXY();
		Point2d t = e.getTargetVertex().getXY();
		return lastMousePos.distance(s) + lastMousePos.distance(t) < s.distance(t) + 0.1;
	}
	
	
	private boolean cursorIsAtFace(F f){
		GeneralPath poly = editPanel.graphics.makePath(makePointList(f));
		Point2D p = editPanel.graphics.toViewCoord(lastMousePos);
		return poly.contains(p);
	}
	
	
	private List<Point2d> makePointList(F f){
		List<E> boundary = f.getBoundary();
		LinkedList<Point2d> vertexCoords = new LinkedList<Point2d>();
		for (E e : boundary)
			vertexCoords.add(e.getTargetVertex().getXY());
		return vertexCoords;
	}
	
	
	public void paint(GraphGraphics g) {
		g.getGraphics().setColor(controller.getColorController().getHoverColor());
		if (vertexUnderMouse != null && vertexUnderMouse.isValid()){
			g.drawVertex(vertexUnderMouse.getXY());
			return;
		}
		if (edgeUnderMouse != null && edgeUnderMouse.isValid()){
			Point2d s = edgeUnderMouse.getOppositeEdge().getTargetVertex().getXY();
			Point2d t = edgeUnderMouse.getTargetVertex().getXY();
			g.drawEdge(s, t);
			return;
		}
		if (faceUnderMouse != null && faceUnderMouse.isValid()){
			g.fillFace(makePointList(faceUnderMouse));
			g.getGraphics().setColor(Color.BLACK);
			g.drawFace(makePointList(faceUnderMouse));	
			return;
		}
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
