package halfedge.surfaceutilities;

import halfedge.Edge;
import halfedge.HalfEdgeDataStructure;
import halfedge.Node;
import halfedge.Vertex;

import java.util.HashSet;
import java.util.Set;

import util.debug.DBGTracer;


/**
 * Basic consistency checks for HalfedgeDataStructures
 * <p>
 * Copyright 2005 <a href="http://www.math.tu-berlin.de/~springb/">Boris Springborn</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Boris Springborn
 */
public final class ConsistencyCheck {

	// Don't instatiate.
	private ConsistencyCheck() {}
	
	public static boolean isValidSurface(HalfEdgeDataStructure<?, ?, ?> heds) {
		
		// must have at least one edge
		if (heds.getEdges().isEmpty()) {
			message("getEdges().isEmpty == true");
			return false;
		}
		
		// check for null references
		for (Edge<?,?,?> e : heds.getEdges()) {
			if (e.getNextEdge() == null) {
				message("getNextEdge() returns null on " + e);
				return false;
			}
			if (e.getPreviousEdge() == null) {
				message("getPreviousEdge() returns null on " + e);
				return false;
			}
			if (e.getOppositeEdge() == null) {
				message("getOppositeEdge() returns null on " + e);
				return false;
			}
			if (e.getTargetVertex() == null) {
				message("getTargetVertex() returns null on " + e);
				return false;
			}
			// either left face or right face may be null but not both
			if (e.getLeftFace() == null && e.getRightFace() == null) {
				message("Left face and right face are null for edge " + e + ".");
				return false;
			}
		}
		
		for (Edge<?,?,?> e : heds.getEdges()) {
			if (e.getLeftFace() != e.getNextEdge().getLeftFace()) {
				message("e.getLeftFace() != e.getNextEdge().getLeftFace() for e=" + e);
				return false;
			}
			if (e.getTargetVertex() != e.getNextEdge().getOppositeEdge().getTargetVertex()) {
				message("e.getTargetVertex() != e.getNextEdge().getOppositeEdge().getTargetVertex() for e=" + e);
			}
		}		
		
		for (Node<?,?,?> f : heds.getFaces()) {
			// collect edges with left face f
			Set<Edge<?,?,?>> b = new HashSet<Edge<?,?,?>>();
			for (Edge<?,?,?> e : heds.getEdges()) {
				if (f == e.getLeftFace()) {
					b.add(e);
				}
			}
			// there must be at least one
			if (b.isEmpty()) {
				message("There is no edge whose left face is " + f + ".");
				return false;
			}
			// they must form a connected edge cycle
			Edge<?,?,?> e = b.iterator().next(); // some element of b
			assert e != null && e.getLeftFace() == f;
			while (b.remove(e)) {
				e = e.getNextEdge();
				assert e != null && e.getLeftFace() == f;				
			}
			if (!b.isEmpty()) {
				message("Boundary of face " + f + " does not form connected edge cycle.");
				return false;
			}
		}
		
		for (Node<?, ?, ?> v : heds.getVertices()) {
			// collect edges with target vertex v
			Set<Edge<?,?,?>> b = new HashSet<Edge<?,?,?>>();
			for (Edge<?,?,?> e : heds.getEdges()) {
				if (v == e.getTargetVertex()) {
					b.add(e);
				}
			}
			// there must be at least one
			if (b.isEmpty()) {
				message("There is no edge whose target vertex is " + v + ".");
				return false;
			}
			// they must form a connected edge cocycle
			// and there may be at most one edge with target v and left face null
			Edge<?,?,?> e = b.iterator().next(); // some element of b
			boolean encounteredBoundaryEdge = false;
			assert e != null && e.getTargetVertex() == v;
			while (b.remove(e)) {
				if (e.getLeftFace() == null) {
					if (encounteredBoundaryEdge) {
						message("There is more than one edge with target " + v + " and left face null.");
						return false;
					}
					encounteredBoundaryEdge = true;
				}
				e = e.getNextEdge().getOppositeEdge();
//				assert e != null && e.getTargetVertex() == v;				
			}
			if (!b.isEmpty()) {
				message("Coboundary of vertex " + v + " does not form connected edge cocycle.");
				return false;
			}
		}
		
		// passed all tests
		return true;
	}

	public static boolean isThreeConnected(HalfEdgeDataStructure<?, ?, ?> heds){
		for (Vertex<?, ?, ?> v : heds.getVertices()){
			if (v.getEdgeStar().size() < 3)
				return false;
		}
		return true;
	}
	
	
	public static boolean getDebug() {
		return DBGTracer.isActive();
	}

	
	private static void message(String m) {
		if (ConsistencyCheck.getDebug()) {
			System.err.println(ConsistencyCheck.class.getSimpleName() + ": " + m);
		}
	}
	
}
