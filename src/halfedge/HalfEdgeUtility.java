package halfedge;

import halfedge.decorations.IsFlippable;
import halfedge.surfaceutilities.SurfaceException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class HalfEdgeUtility {

	
	public static int getDegree(Vertex<?,?,?> v) {
		return v.getEdgeStar().size();
	}
	
	public static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> List<V> boundaryVertices(F face) {
		Collection<E> b = boundaryEdges(face);
		LinkedList<V> vList = new LinkedList<V>();
		for (E e : b) {
			vList.add(e.getTargetVertex());
		}
		return vList;
	}
	
	public static <
		E extends Edge<?,E,F>, 
		F extends Face<?,E,F>
	> List<E> boundaryEdges(F face) {
		final E e0 = face.getBoundaryEdge();
		if (e0 == null) {
			return Collections.emptyList();
		}
		LinkedList<E> result = new LinkedList<E>();
		E e = e0;
		do {
			if (face != e.getLeftFace()) {
				throw new RuntimeException("Edge " + e + " does not have face " + face + " as left face, " +
						"although it is the next edge of an edge which does.");
			}
			result.add(e);
			e = e.getNextEdge();
			if (e == null) {
				throw new RuntimeException("Some edge has null as next edge.");
			}
		} while (e != e0);
		return result;
	}
	
	public static 
	<
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> void removeAllFaces(HalfEdgeDataStructure<V, E, F> graph) throws SurfaceException{
		while (graph.getNumFaces() > 0){
			F face = graph.getFace(0);
			removeFace(face);
		}
	}

	public static 	
	<
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	>  void removeFace(F face){
		HalfEdgeDataStructure<V, E, F> graph = face.getHalfEdgeDataStructure();
		List<E> boundary = null;
		try {
			boundary = face.getBoundary();
			for (E e : boundary)
				e.setLeftFace(null);
		} catch (Exception e) {}
		graph.removeFace(face);
	}
	
	
	public static 	
	<
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	>  void removeVertex(V vertex){
		try{
			HalfEdgeDataStructure<V, E, F> graph = vertex.getHalfEdgeDataStructure();
			List<E> edgeStar = vertex.getEdgeStar();
			for (E e : edgeStar){
				E borderPre = e.getOppositeEdge().getNextEdge();
				E borderPost = e.getPreviousEdge();
				borderPost.linkNextEdge(borderPre);
				e.getStartVertex().setConnectedEdge(borderPost);
				HalfEdgeUtility.removeEdge(e);
			}
			graph.removeVertex(vertex);
		} catch (Exception e){
			e.printStackTrace();
		}
	}

	public static 	
	<
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	>  void removeEdge(E edge){
		HalfEdgeDataStructure<V, E, F> graph = edge.getHalfEdgeDataStructure();
		if (edge.getLeftFace() != null)
			graph.removeFace(edge.getLeftFace());
		if (edge.getRightFace() != null)
			graph.removeFace(edge.getRightFace());
		// remove the vertex
		graph.removeEdgeAndOppositeEdge(edge);	
	}

	public static 	
	<
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	>  List<E> findEdgesWithTargets(List<V> vlist) {
		HalfEdgeDataStructure<V, E, F> graph = vlist.get(0).getHalfEdgeDataStructure();
		ArrayList<E> result = new ArrayList<E>();
		for (Node<?,?,?> v : vlist){
			for (E e : graph.getEdges()) {
				if (v == e.getTargetVertex())
					result.add(e);
			}
		}
		return result;
	}

	/**
	 * Find an edge with a given target vertex. May be expensive.
	 * @param v
	 * @return an edge <code>e</code> with target vertex <code>v</code>, 
	 * or <code>null</code> if none exists. 
	 */
	public static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> E findEdgeWithTarget(V v) {
		HalfEdgeDataStructure<V, E, F> surface = v.getHalfEdgeDataStructure();
		for (E e : surface.getEdges()) {
			if (v == e.getTargetVertex())
				return e;
		}
		return null;
	}

	public static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> List<E> findEdgesWithTarget(V v) {
		HalfEdgeDataStructure<V, E, F> surface = v.getHalfEdgeDataStructure();
		ArrayList<E> result = new ArrayList<E>();
		for (E e : surface.getEdges()) {
			if (v == e.getTargetVertex())
				result.add(e);
		}
		return result;
	}

	public static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> E findEdgeInBoundary(F f) {
		HalfEdgeDataStructure<V, E, F> surface = f.getHalfEdgeDataStructure();
		for (E e : surface.getEdges()) {
			if (f == e.getLeftFace())
				return e;
		}
		return null;
	}

	public static < 
	V extends Vertex<V, E, F>,
	E extends Edge<V, E, F>,
	F extends Face<V, E, F>
	> V findCommonVertex(E ... edges) {
		if (edges.length < 2)
			return null;
		E e1 = edges[0];
		E e2 = edges[1];
		V tE1 = e1.getTargetVertex();
		V sE1 = e1.getStartVertex();
		V tE2 = e2.getTargetVertex();
		V sE2 = e2.getStartVertex();
		
		if( tE1 == sE2 || tE1 == tE2 ) {
			for( int i = 2; i < edges.length; i++ ) {
				if( edges[i].getStartVertex()!=tE1 && edges[i].getTargetVertex()!=tE1 )
					return null;
			}
			return tE1;
		} else if( sE1 == sE2 || sE1 == tE2 ) {
			for( int i = 2; i < edges.length; i++ ) {
				if( edges[i].getStartVertex()!=sE1 && edges[i].getTargetVertex()!=sE1 )
					return null;
			}
			return sE1;
		} else return null;
	}
	
	
	public static < 
	V extends Vertex<V, E, F>,
	E extends Edge<V, E, F>,
	F extends Face<V, E, F>
	> F findCommonFace(E ... edges) {
		if (edges.length < 2)
			return null;
		E e1 = edges[0];
		E e2 = edges[1];
		F tE1 = e1.getLeftFace();
		F sE1 = e1.getRightFace();
		F tE2 = e2.getLeftFace();
		F sE2 = e2.getRightFace();
		
		if( tE1 == sE2 || tE1 == tE2 ) {
			for( int i = 2; i < edges.length; i++ ) {
				if( edges[i].getRightFace()!=tE1 && edges[i].getLeftFace()!=tE1 )
					return null;
			}
			return tE1;
		} else if( sE1 == sE2 || sE1 == tE2 ) {
			for( int i = 2; i < edges.length; i++ ) {
				if( edges[i].getRightFace()!=sE1 && edges[i].getLeftFace()!=sE1 )
					return null;
			}
			return sE1;
		} else return null;
	}
	


	public static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> List<E> boundary(F f) {
		List<E> boundary = new LinkedList<E>();
		E e0 = findEdgeInBoundary(f);
		if (e0 == null)
			return boundary;
		boundary.add(e0);
		E e = e0.getNextEdge();
		while (e != e0) {
			boundary.add(e);
			e = e.getNextEdge();
		}
		return boundary;
	}

	/**
	 * Creates the dual of the graph
	 * @return dual graph in <V,E,F> format
	 * @author Kristoffer Josefsson
	 */
	public static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F> & IsFlippable,
		F extends Face<V, E, F>
	> HalfEdgeDataStructure<V,E,F> getDual(HalfEdgeDataStructure<V, E, F> graph, HashMap<F, V> dualMap, HashMap<E,E> edgeToEdgeMap){

		// copy this
		HalfEdgeDataStructure<V,E,F> dual = new HalfEdgeDataStructure<V,E,F>(graph);
		
		// clean this
		while(dual.getVertices().size() > 0)
			dual.removeVertex(dual.getVertex(0));
		
		if(dualMap == null)
			dualMap = new HashMap<F,V>();
		for(F f : graph.getFaces()) {
			V v = dual.addNewVertex();
			dualMap.put(f, v);
		}

		// let edges point between adjacent faces in original graph	
		int i = 0;
		for(E edge : dual.getEdges()){
			F f1 = graph.getEdge(i).getLeftFace();
			F f2 = graph.getEdge(i).getRightFace();
			edge.setTargetVertex(dualMap.get(f1));
			edgeToEdgeMap.put(graph.getEdge(i), edge);
			edge.getOppositeEdge().setTargetVertex(dualMap.get(f2));
			edgeToEdgeMap.put(graph.getEdge(i).getOppositeEdge(), edge.getOppositeEdge());
			i++;
		}
		
		// set order in the dual graph
		for(V v : graph.getVertices()) {
			E pe = null;
			for(E e : v.getEdgeStar()) {
				E ve = edgeToEdgeMap.get(e);
				if(pe != null) {
					pe.linkNextEdge(ve);
				}
				pe = ve;
			}
			pe.linkNextEdge(edgeToEdgeMap.get(v.getEdgeStar().get(0)));
		}
		return dual;
		
	}
	
	public static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F> & IsFlippable,
		F extends Face<V, E, F>
	> HalfEdgeDataStructure<V,E,F> getMinimalSpanningTree(HalfEdgeDataStructure<V, E, F> graph, V start){
	
		
		HalfEdgeDataStructure<V, E, F> graphCopy = new HalfEdgeDataStructure<V,E,F>(graph);
		HalfEdgeDataStructure<V, E, F> subTree = new HalfEdgeDataStructure<V,E,F>(graph);
		
		// clear from edges
		while (subTree.getNumEdges() > 0)
			subTree.removeEdge(subTree.getEdge(0));
	
		List<V> verts = new LinkedList<V>();
		List<E> edges = new LinkedList<E>();
		
		HashMap<E, V> startMap = new HashMap<E, V>();
		HashMap<E, V> targetMap = new HashMap<E, V>();
	
		verts.add(start);
		
		while(verts.size() < graphCopy.getVertices().size()){
		
			List<E> neighbours = HalfEdgeUtility.findEdgesWithTargets(verts);
			
			// loop through all edges in neighbourhood to find shortest of them
			double min = Double.MAX_VALUE;
			E tempEdge = null;
			for(E e : neighbours){
				if(e.getLength() < min){
					
					if(verts.contains(e.getStartVertex())){
						
						// discard this one, because it causes a loop
						graphCopy.removeEdgeAndOppositeEdge(e);
					} else {
						min = e.getLength();
						tempEdge = e;
					//	System.err.println(tempEdge.getStartVertex());
					}
				}
			}
		
			V v1 = tempEdge.getStartVertex();
			V v2 = tempEdge.getTargetVertex();
			if(v1 != null || v2 != null){
				
				// add this one to our list
				edges.add(tempEdge);
				
				verts.add(v1);
			//	verts.add(v2);
	
				startMap.put(tempEdge, v1);
				targetMap.put(tempEdge, v2);
			}
	
			// and remove it from the original so that we don't consider it again
			graphCopy.removeEdgeAndOppositeEdge(tempEdge);
		}

		for(E e : edges){ 	
			E e1 = subTree.addNewEdge();
			E e2 = subTree.addNewEdge();
	
			V startV = startMap.get(e);
		//	if(startV == null)
		//		System.err.println("start null");
	
			V targetV = targetMap.get(e);
		//	if(targetV == null)
		//		System.err.println("target null");
	
			e1.setTargetVertex(subTree.getVertex(targetV.getIndex()));
			e2.setTargetVertex(subTree.getVertex(startV.getIndex()));
	
		//	e1.linkNextEdge(e2);
			
			e1.linkOppositeEdge(e2);
			e2.linkOppositeEdge(e1);
		}
		
		return subTree;
		
		
	}	
}
