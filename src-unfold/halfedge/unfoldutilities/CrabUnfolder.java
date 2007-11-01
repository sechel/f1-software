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
import halfedge.surfaceutilities.EmbeddedVertex;
import halfedge.triangulationutilities.AcuteTriangulationException;
import halfedge.triangulationutilities.TriangulationException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections15.BidiMap;

public class CrabUnfolder <
		V extends Vertex<V, E, F> & HasXY & HasRadius & HasXYZW & HasCurvature,
		E extends Edge<V, E, F> & IsBoundary & HasLength & IsFlippable & IsHidable & HasAngle,
		F extends Face<V, E, F>
	> {

	private List<E>
		globalCut,
		globalVisit = null;
	
	private BidiMap<V, EmbeddedEdge<V,E,F>>
		paths = null;
	
	
	public CrabUnfolder(){
		
		globalCut = new ArrayList<E>();
		globalVisit = new ArrayList<E>();
		
	}
	
	public HalfEdgeDataStructure<V, E, F> getCrabUnfoldNet(HalfEdgeDataStructure<V,E,F> graph, V source) throws TriangulationException{
		
		paths = StarTreeFinder.getStarTree(graph, source);

		F bottom = Unfolder.getBottom(graph, source);
		
		doFace(bottom);
		
		System.err.println("Size of globalCut is: " + globalCut.size());
		System.err.println("Size of globalVisit is: " + globalVisit.size());
		
		
		HalfEdgeDataStructure<V,E,F> joinTree = null;
		HashMap<F,V> dualMap = new HashMap<F,V>();
		HashMap<E,E> edgeToEdgeMap = new HashMap<E,E>();
	
		try {
			joinTree = Unfolder.constructVoronoi(graph, dualMap, edgeToEdgeMap);
			System.err.println(dualMap);
		} catch(TriangulationException e) {
			System.err.println(e.getMessage());
		}
	
		
		int j = 0;
		// make jointree
		for(E e : globalCut) {
			F f1 = e.getLeftFace();
			F f2 = e.getRightFace();
			
			V v1 = dualMap.get(f1);
			V v2 = dualMap.get(f2);
			for(E de : HalfEdgeUtility.findEdgesWithTarget(v1)){
				if(de.getStartVertex() == v2) {	// good one
					if(joinTree.removeEdgeAndOppositeEdge(de)){ 
						System.err.println("Yup good removing");
						j++;
					}
				}
			}
		}
		
		// hide join-edges
		for(E e : graph.getEdges()) {
			if(joinTree.getEdges().contains(edgeToEdgeMap.get(e))) {
				System.err.println("Hiding edge " + e);
				e.setHidden(true);
			}
		}
		
//		for(E e : graph.getEdges()) {
//			e.setHidden(true);
//		}
//		for(E e : globalCut) {
//			e.setHidden(false);
//		}
				
		return joinTree;
//		return Unfolder.getIndexUnfolding(graph, graph.getVertex(0));
		
	}
	
	private void doFace(F f) {
		
		List<E> boundary = f.getBoundary();
		
		boundary.removeAll(globalCut);
		boundary.removeAll(globalVisit);
		
		
		for(E e : boundary) {
			doEdge(e);
		}
	}
	
	private void doEdge(E e) {

		V v1 = e.getStartVertex();
		V v2 = e.getTargetVertex();
		
		List<V> vs = new ArrayList<V>();
		vs.add(v1);
		vs.add(v2);
		
		List<E> p = getBothCuts(vs);
		
		List<E> stars = new ArrayList<E>(v1.getEdgeStar());
		stars.addAll(v2.getEdgeStar());
		
		p.retainAll(stars);
		
//		Set<E> pt = new HashSet<E>(p);
//		pt.retainAll(e.getRightFace().getBoundary());
		
		// get the two touching edges
		List<E> two = new ArrayList<E>();
		for(E ee : p) {
			if(ee.getTargetVertex() == v1 || ee.getTargetVertex() == v2) {
				two.add(ee);
			}
		}
		
		if(two.size() != 2) {
			System.err.println("2 == 1");
		}
		
		List<E> four = new ArrayList<E>(two);
		for(E ee : two) {
			four.add(ee.getOppositeEdge());
		}
		
		globalCut.addAll(four);
		
		globalVisit.add(e);
		globalVisit.add(e.getOppositeEdge());
		
		List<E> doubleBoundary = new ArrayList<E>();
		for(E ee : e.getRightFace().getBoundary()) {
			doubleBoundary.add(ee);
			doubleBoundary.add(ee.getOppositeEdge());
		}
		
		p.retainAll(doubleBoundary);
		
		if(p.size() == 0) {
			System.err.println("p == {}, going right");
			doFace(e.getRightFace());
		}
		
		if(p.size() == 1) {
			System.err.println("p == 1, going right");
			doFace(e.getRightFace());
		}
		
		if(p.size() == 2) {
			System.err.println("p == 2, doing nothing more");
		}
		
//		if(p.size() > 2) {
//		//	doEdge()
//			System.err.println("p > 2, going right");
//			doFace(e.getRightFace());
//		}
	}
	
	// check orientation
	private List<E> getBothCuts(List<V> vs){
		List<E> p = new ArrayList<E>();

		for(V v : vs) {
			for(E e : GeodesicUtilities.getCuts(paths.get(v))) {
				p.add(e);
				p.add(e.getOppositeEdge());
			}
		}
		System.err.println("Size of p is: " + p.size());
		return p;
	}
}
