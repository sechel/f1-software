package halfedge.surfaceutilities;

import halfedge.Edge;
import halfedge.Face;
import halfedge.HalfEdgeDataStructure;
import halfedge.Vertex;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;


/**
 * Finds vertices of degree 2 at the border of a surface
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 */
public class Ears {

	public static 	
	<
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> List<E> findEarsEdge(HalfEdgeDataStructure<V, E, F> graph){
		ArrayList<E> result = new ArrayList<E>();
		for (E e : graph.getEdges()){
			if (!e.isInteriorEdge() && e.getLeftFace() == null)
				if (e.getRightFace() == e.getNextEdge().getRightFace())
					result.add(e);
		}
		return result;
	}
	
	
	
	public static 	
	<
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> List<F> findEarsFaces(HalfEdgeDataStructure<V, E, F> graph){
		ArrayList<F> result = new ArrayList<F>();
		for (F f : graph.getFaces()){
			for (E e : f.getBoundary()){
				if (isEarEdge(e)){
					result.add(f);
					break;
				}
			}
		}
		return result;

	}
	
	
	private static boolean isEarEdge(Edge<?, ?, ?> e){
		boolean next = e.getNextEdge().getRightFace() == null && e.getRightFace() == null;
		boolean before = e.getPreviousEdge().getRightFace() == null && e.getRightFace() == null;
		return next || before;
	}
	
	
	public static 
	<
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> Collection<V> findEars(HalfEdgeDataStructure<V, E, F> mesh) {
		LinkedList<V> result = new LinkedList<V>();
		for (V v : mesh.getVertices()) {
			if (!v.isOnBoundary()) 
				continue;
			boolean isEar = true;
			for (V s : v.getVertexStar()) {
				if (!s.isOnBoundary())
					isEar = false;
			}
			if (isEar)
				result.add(v);
		}
		return result;
	}
	
	
	private static 
	<
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> V findEar(HalfEdgeDataStructure<V, E, F> mesh) {
		for (V v : mesh.getVertices()) {
			if (!v.isOnBoundary()) 
				continue;
			boolean isEar = true;
			for (V s : v.getVertexStar()) {
				if (!s.isOnBoundary())
					isEar = false;
			}
			if (isEar)
				return v;
		}
		return null;
	}
	
	
	
	public static 	
	<
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> void cutEars(HalfEdgeDataStructure<V, E, F> graph){
		V ear = findEar(graph);
		while (ear != null) {
			List<E> b = ear.getEdgeStar();
			if (b.size() < 2) break;
			E b1 = b.get(0);
			E b2 = b.get(1);
			if (b1.getLeftFace() != null)
				b1.getLeftFace().setBoundaryEdge(b1);
			if (b2.getLeftFace() != null)
				b2.getLeftFace().setBoundaryEdge(b2);
			E b1o = b1.getOppositeEdge();
			E b2o = b2.getOppositeEdge();
			b1.setTargetVertex(b2o.getTargetVertex());
			b2.setTargetVertex(b1o.getTargetVertex());
			b1.linkNextEdge(b2o.getNextEdge());
			b2.linkNextEdge(b1o.getNextEdge());
			b1.linkOppositeEdge(b2);
			graph.removeVertex(ear);
			graph.removeEdge(b1o);
			graph.removeEdge(b2o);
			ear = findEar(graph);
		}
	}
	
}
