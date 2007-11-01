package halfedge;

import halfedge.decorations.HasXY;
import halfedge.decorations.HasXYZW;
import halfedge.decorations.IsFlippable;
import halfedge.decorations.IsHidable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class GraphOperations {
	public static <
		V extends Vertex<V, E, F> & HasXY & HasXYZW,
		E extends Edge<V, E, F> & IsFlippable & IsHidable,
		F extends Face<V, E, F>
	> HalfEdgeDataStructure<V, E, F> getSubtreePrim(HalfEdgeDataStructure<V, E, F> graph, V start){
	
	HalfEdgeDataStructure<V, E, F> subTree = new HalfEdgeDataStructure<V,E,F>(graph);
	
	// clear from edges
	while (subTree.getNumEdges() > 0)
		subTree.removeEdge(subTree.getEdge(0));

	List<V> verts = new ArrayList<V>();
	List<E> edges = new ArrayList<E>();
	
	HashMap<E, V> startMap = new HashMap<E, V>();
	HashMap<E, V> targetMap = new HashMap<E, V>();

	verts.add(start);
	
	while(verts.size() < graph.getVertices().size()){
	
		List<E> neighbours = HalfEdgeUtility.findEdgesWithTargets(verts);
		
		// loop through all edges in neighbourhood to find shortest of them
		double min = 1000000.0; 	// FIXME
		E tempEdge = null;
		for(E e : neighbours){
			if(e.getLength() < min){
				
				if(verts.contains(e.getStartVertex())){
					
					// discard this one, because it causes a loop
					graph.removeEdgeAndOppositeEdge(e);
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
		graph.removeEdgeAndOppositeEdge(tempEdge);
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
