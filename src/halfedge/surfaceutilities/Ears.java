package halfedge.surfaceutilities;

import halfedge.Edge;
import halfedge.Face;
import halfedge.HalfEdgeDataStructure;
import halfedge.Vertex;

import java.util.ArrayList;
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
	> void cutEars(HalfEdgeDataStructure<V, E, F> graph){
		List<F> ears = findEarsFaces(graph);
		for (F ear : ears){
			V v1 = null;
			V v2 = null;
			E pre = null;
			E post = null;
			for (E e : ear.getBoundary()){
				if (isEarEdge(e)) {
					if(!isEarEdge(e.getPreviousEdge())){
						v1 = e.getStartVertex();
						pre = e.getPreviousEdge();
					} 
					if(!isEarEdge(e.getNextEdge())) {	
						v2 = e.getTargetVertex();
						post = e.getNextEdge();
					}
				}
			}
//			if (pre == post){
//				HalfEdgeUtility.removeFace(ear);
//				continue;
//			}
			for (E e : ear.getBoundary()){
				if (isEarEdge(e) && isEarEdge(e.getNextEdge()))
					graph.removeVertex(e.getTargetVertex());
			}		
			LinkedList<E> removeEdges = new LinkedList<E>();
			for (E e : ear.getBoundary()){
				if (isEarEdge(e))
					removeEdges.add(e);
			}
			for (E e : removeEdges){
				graph.removeEdgeAndOppositeEdge(e);
			}
			E e1 = graph.addNewEdge();
			E e2 = graph.addNewEdge();
			
			e1.linkOppositeEdge(e2);
			e1.setLeftFace(ear);
			e1.setTargetVertex(v2);
			e2.setTargetVertex(v1);
			
			e1.linkNextEdge(post);
			e1.linkPreviousEdge(pre);
			e2.linkNextEdge(pre);
			e2.linkPreviousEdge(post);
		}
	}
	
}
