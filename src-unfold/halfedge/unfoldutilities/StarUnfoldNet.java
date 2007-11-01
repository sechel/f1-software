package halfedge.unfoldutilities;

import halfedge.Edge;
import halfedge.Face;
import halfedge.HalfEdgeDataStructure;
import halfedge.HalfEdgeUtility;
import halfedge.Vertex;
import halfedge.decorations.HasAngle;
import halfedge.decorations.HasCurvature;
import halfedge.decorations.HasLength;
import halfedge.decorations.HasRadius;
import halfedge.decorations.HasXY;
import halfedge.decorations.HasXYZW;
import halfedge.decorations.IsBoundary;
import halfedge.decorations.IsFlippable;
import halfedge.decorations.IsHidable;
import halfedge.surfaceutilities.EmbeddedEdge;
import halfedge.triangulationutilities.AcuteTriangulationException;
import halfedge.triangulationutilities.TriangulationException;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.collections15.BidiMap;

public class StarUnfoldNet {

	public static <
		V extends Vertex<V, E, F> & HasXY & HasRadius & HasXYZW & HasCurvature,
		E extends Edge<V, E, F> & IsBoundary & HasLength & IsFlippable & IsHidable & HasAngle,
		F extends Face<V, E, F>
	> HalfEdgeDataStructure<V, E, F> getStarUnfoldNet(HalfEdgeDataStructure<V,E,F> graph, V source, boolean safe) throws TriangulationException{
		
		BidiMap<V, EmbeddedEdge<V,E,F>> paths = StarTreeFinder.getStarTree(graph, source);

		HalfEdgeDataStructure<V,E,F> joinTree = null;
		HashMap<F,V> dualMap = new HashMap<F,V>();
		HashMap<E,E> edgeToEdgeMap = new HashMap<E,E>();
		
//		HashMap<V, EmbeddedEdge<V,E,F>> vertexToEEMap = new HashMap<V, EmbeddedEdge<V,E,F>>();
//		
//		for(EmbeddedEdge<V,E,F> ee : paths.values()) {
//			if(ee.getEmbeddedVertices().size() > 2) {
//				vertexToEEMap.put(ee.getEndVertex(), ee);
//			}
//		}
	
		try {
			joinTree = Unfolder.constructVoronoi(graph, dualMap, edgeToEdgeMap);
			System.err.println(dualMap);
		} catch(TriangulationException e) {
			System.err.println(e.getMessage());
		}
	
		HashMap<F, Boolean> cutFaces = new HashMap<F, Boolean>();
		List<E> crossedEdges = new LinkedList<E>();
		List<E> wantedEdges = new LinkedList<E>();
		int size = joinTree.getNumEdges();
		
		try {
		
			for(EmbeddedEdge<V,E,F> edge : paths.values()) {
	//			if(edge.getEmbeddedVertices().size() > 7)
//					crossedEdges.addAll(traverseVoronoiCells(edge, cutFaces));
				GeodesicUtilities.traverseVoronoiCells(edge, cutFaces, crossedEdges, wantedEdges, safe);
			}
			
		} catch (AcuteTriangulationException e) {
			System.err.println(e.getMessage());
		}
		
		
		int i = 0;
		
		for(F f : graph.getFaces()) {
			
			// fixNode(f, crossedEdges);
			
			// take care of nodes
			List<E> contact = GeodesicUtilities.getContact(f, crossedEdges);
			if(contact.size() > 2) {	// if node

				for(E e : contact) {
					V vl = e.getTargetVertex();
					V vr = e.getStartVertex();
					
					if(vl == source || vr == source) {
						
					} else if(paths.containsKey(vl) && paths.containsKey(vr)){
					
						EmbeddedEdge<V,E,F> el = paths.get(vl);
						EmbeddedEdge<V,E,F> er = paths.get(vr);
						
						int sl = el.getEmbeddedVertices().size();
						int sr = er.getEmbeddedVertices().size();
						
						List<E> crossed = GeodesicUtilities.getCrossedEdges(el.getEmbeddedVertex(sl-1), el.getEmbeddedVertex(sl-3));
						crossed.addAll(GeodesicUtilities.getCrossedEdges(er.getEmbeddedVertex(sr-1), er.getEmbeddedVertex(sr-3)));
						
						if(crossed.contains(e) || crossed.contains(e.getOppositeEdge())) {
							//local edge, do nothing
						} else {
							i++;
							crossedEdges.remove(e);
							crossedEdges.remove(e.getOppositeEdge());
						}
					}
				}
			}
		}

		System.err.println("Joined " + i + " extra faces.");
		
		int j = 0;
		// make jointree
		for(E e : crossedEdges) {
			F f1 = e.getLeftFace();
			F f2 = e.getRightFace();
			
			V v1 = dualMap.get(f1);
			V v2 = dualMap.get(f2);
			for(E de : HalfEdgeUtility.findEdgesWithTarget(v1)){
				if(de.getStartVertex() == v2) {	// good one
//					de.setLength(500.0);
//					de.getOppositeEdge().setLength(500.0);
					if(joinTree.removeEdgeAndOppositeEdge(de)){ 
//						System.err.println("Yup good removing");
						j++;
					}
				}
			}
		}

		System.err.println("Removed " + j + " edges.");
		
		// hide join-edges
		for(E e : graph.getEdges()) {
			if(joinTree.getEdges().contains(edgeToEdgeMap.get(e))) {
//				System.err.println("Hiding edge " + e);
				e.setHidden(true);
			}
		}
				
		System.err.println("Removed " + (size - joinTree.getNumEdges()) + " edges from jointree");
		return joinTree;
//		return Unfolder.getIndexUnfolding(graph);
		
	}
		
}