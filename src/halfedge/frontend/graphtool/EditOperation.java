package halfedge.frontend.graphtool;

import halfedge.Edge;
import halfedge.Node;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.Collection;

import javax.vecmath.Point2d;

/**
 * Defines an editor operation
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 */
public enum EditOperation {

	SELECT_EDGE,
	SELECT_HALFEDGE,
	SELECT_VERTEX,
	SELECT_FACE,
	
	SELECT_EDGE_MULTI,
	SELECT_HALFEDGE_MULTI,
	SELECT_VERTEX_MULTI,
	SELECT_FACE_MULTI,
	
	SELECT_POSITION,
	
	DRAG_BEGIN,
	DRAG_TO,
	DRAG_END,
	
	CANCEL,
	
	MOUSE_POS,
	
	KEY_PRESSED,
	KEY_TYPED,
	KEY_RELEASED;
	
	
	public int
		mouseModifiersEx = 0;
	public Point2d 
		mousePosition = new Point2d();
	public Edge<?, ?, ?>
		edge = null;
	public Node<?, ?, ?>
		face = null;
	public Node<?, ?, ?>
		vertex = null;
	public Collection<Edge<?, ?, ?>>
		edgeMulti = null;
	public Collection<Node<?, ?, ?>>
		vertexMulti = null,
		faceMulti = null;
	public MouseEvent
		mouseEvent = null;
	public KeyEvent
		keyEvent = null;
}
