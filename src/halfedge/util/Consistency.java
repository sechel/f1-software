package halfedge.util;

import halfedge.Edge;
import halfedge.Face;
import halfedge.HalfEdgeDataStructure;
import halfedge.HalfEdgeUtility;
import halfedge.Vertex;

import java.util.List;

import util.debug.DBGTracer;

public class Consistency {

	public static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> boolean checkConsistency(HalfEdgeDataStructure<V, E, F> heds){
		for (V v : heds.getVertices()){
			List<E> cocycle1 = v.getEdgeStar(); 
			List<E> cocycle2 = HalfEdgeUtility.findEdgesWithTarget(v);
			for (E e : cocycle1)
				if (!cocycle2.contains(e) || cocycle1.size() != cocycle2.size()){
					DBGTracer.msg("Consistency: " + "e.getEdgeStar != he.findEdgesWithTarget");
					return false;
				}
			if (v.getConnectedEdge() != null && v.getConnectedEdge().getTargetVertex() != v){
				DBGTracer.msg("Consistency: " + "e.getEdgeStar != he.findEdgesWithTarget");
				return false;
			}
		}
		for (F f : heds.getFaces()){
			List<E> boundary1 = f.getBoundary();
			List<E> boundary2 = HalfEdgeUtility.boundary(f);
			for (E e : boundary1)
				if (!boundary2.contains(e) || boundary1.size() != boundary2.size()){
					DBGTracer.msg("Consistency: " + "f.getBoundary != he.boundary()");
					return false;
				}	
		}
			
		return true;
	}
	
	
	
}
