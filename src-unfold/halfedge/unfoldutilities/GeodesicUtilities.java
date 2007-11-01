package halfedge.unfoldutilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import halfedge.Edge;
import halfedge.Face;
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
import halfedge.triangulationutilities.DelaunayUtilities;
import halfedge.triangulationutilities.TriangulationException;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import math.util.VecmathTools;

public class GeodesicUtilities {


	public static <
		V extends Vertex<V, E, F> & HasRadius & HasXYZW & HasCurvature & HasXY,
		E extends Edge<V, E, F> & IsBoundary & HasLength & IsFlippable & IsHidable & HasAngle,
		F extends Face<V, E, F>
	> List<E> traverseVoronoiCells(EmbeddedEdge<V,E,F> edge, HashMap<F, Boolean> cutFaces, List<E> traversed, List<E> wanted) throws AcuteTriangulationException, TriangulationException {
	
		if(!DelaunayUtilities.isAcuteTriangulation(edge.getEmbeddedVertex(0).getGraph())) 
			throw new AcuteTriangulationException("Not an acute triangulation");
		
		EmbeddedVertex<V,E,F> prev = null;
	//	LinkedList<E> traversed = new LinkedList<E>();
	//	System.err.println("\n---------------------------\nTraversing a new path, nr vertices: " + edge.getEmbeddedVertices().size() + "\n----------------------------");
		for(Iterator<EmbeddedVertex<V,E,F>> it = edge.getEmbeddedVertices().iterator(); it.hasNext();) {
			EmbeddedVertex<V,E,F> next = it.next();
			
			for(E e : getCrossedEdges(prev, next)) {
				willCauseCycle(e, traversed);
			}
			
			prev = next;
		}
		
	//	System.err.println(traversed);
		return traversed;
	}
	
	public static <
		V extends Vertex<V, E, F> & HasRadius & HasXYZW & HasCurvature,
		E extends Edge<V, E, F> & IsBoundary & HasLength & IsFlippable & IsHidable,
		F extends Face<V, E, F>
	> boolean overCircumcenter(EmbeddedVertex<V,E,F> v1, EmbeddedVertex<V,E,F> v2) {
		try {
			Point3d cc = VecmathTools.p4top3(DelaunayUtilities.getCircumcenter4d(v1.getGraph(), v1.getEdge()));
			
			Vector3d path = new Vector3d(VecmathTools.p4top3(v2.getXYZW()));
			path.sub(VecmathTools.p4top3(v1.getXYZW()));
			path.normalize();
			
//			Vector4d base = new Vector4d(v1.getEdge().getTargetVertex().getXYZW());
			Vector3d base = new Vector3d(VecmathTools.p4top3(v1.getFartherstVertex().getXYZW()));
			base.sub(VecmathTools.p4top3(v1.getXYZW()));
			base.normalize();
			
			Vector3d toCirc = new Vector3d(cc);
			toCirc.sub(VecmathTools.p4top3(v1.getXYZW()));
			toCirc.normalize();
			
			return Math.acos(toCirc.dot(base)) < Math.acos(path.dot(base));
			
			// a > b => cos a < cos b, a,b \in [0, \pi]
//			if(v1.isHome())
//				return Math.acos(path.dot(base)) > Math.acos(toCirc.dot(base));
//			else
//				return Math.acos(path.dot(base)) < Math.acos(toCirc.dot(base));
			
		} catch(TriangulationException e) {
			System.err.println(e.getMessage());
			return false;	//FIXME
		}
			
	}
	
	public static <
		V extends Vertex<V, E, F> & HasRadius & HasXYZW & HasCurvature,
		E extends Edge<V, E, F> & IsBoundary & HasLength & IsFlippable & IsHidable,
		F extends Face<V, E, F>
	> List<E> getCrossedEdges(EmbeddedVertex<V,E,F> prev, EmbeddedVertex<V,E,F> next){
	
		ArrayList<E> crossed = new ArrayList<E>();
	
		if(prev != null) {
			
			E e_i = prev.getEdge(); 
			E e_j = e_i.getNextEdge();
			E e_k = e_j.getNextEdge();
			
			boolean overCirc = GeodesicUtilities.overCircumcenter(prev, next);
			
			if(next.getEdge() == e_i) {
	//			System.err.println("Bad path");
			} 
			
			else if(next.getEdge() == e_j) {
				if(prev.isHome()){
					if(next.isHome()) {
						if(overCirc) {
							
	//						System.err.println("Add e_k^, e_j^");
	
							crossed.add(e_k.getOppositeEdge());
							crossed.add(e_j.getOppositeEdge());
							
	//						System.err.println(e_k.getOppositeEdge());
	//						System.err.println(e_j.getOppositeEdge());	// consider
							
						} else {
							
	//						System.err.println("Add e_i");
	
							crossed.add(e_i);
							
	//						System.err.println(e_i);
							
						}
					} else {	//next.isHome()
						if(overCirc) {
							
	//						System.err.println("Add e_k^");
	
							crossed.add(e_k.getOppositeEdge());
							
							System.err.println(e_k.getOppositeEdge());
							
						} else {
							
	//						System.err.println("Add e_i, e_j");
														
							crossed.add(e_i);
							crossed.add(e_j);
							
	//						System.err.println(e_i);
	//						System.err.println(e_j);
							
						}
					}
				} else {	//prev.isHome()
					if(next.isHome()) {
						if(overCirc) {
	//						System.err.println("Add nothing, staying in (j)");
							
						} else {
							System.err.println("Can't happen");
						}
					} else {	//next.isHome()
						if(overCirc) {
							
	//						System.err.println("Add e_j");
							
							crossed.add(e_j);
							
	//						System.err.println(e_j);
	
						} else {
							
	//						System.err.println("Add e_i^, e_k^");
							
							crossed.add(e_i.getOppositeEdge());
							crossed.add(e_k.getOppositeEdge());	// consider
							
	//						System.err.println(e_i.getOppositeEdge());
	//						System.err.println(e_k.getOppositeEdge());
						}
					}
				}
				
			}
			
			else if(next.getEdge() == e_k) {
				if(prev.isHome()){
					if(next.isHome()) {
						if(overCirc) {
							
	//						System.err.println("Add e_k^");
							
							crossed.add(e_k.getOppositeEdge());
							
	//						System.err.println(e_k.getOppositeEdge());
							
						} else {
							
	//						System.err.println("Add e_i, e_j, (2)");
	
							crossed.add(e_i);
							crossed.add(e_j);	// consider
							
	//						System.err.println(e_i);
	//						System.err.println(e_j);
							
						}
					} else {	//next.isHome()
						if(overCirc) {
	//						System.err.println("Add nothing, staying in (i)");
						} else {
							System.err.println("Can't happen (2)");
						}
					}
				} else {	//prev.isHome()
					if(next.isHome()) {
						if(overCirc) {	
							
	//						System.err.println("Add e_j (2)");
							
							crossed.add(e_j);	
							
	//						System.err.println(e_j);
							
						} else {
							
	//						System.err.println("Add e_i^, e_k^ (2)");
											
							crossed.add(e_i.getOppositeEdge());
							crossed.add(e_k.getOppositeEdge());	// consider
							
	//						System.err.println(e_i.getOppositeEdge());
	//						System.err.println(e_k.getOppositeEdge());
							
						}
					} else {	//next.isHome()
						if(overCirc) {
	//						System.err.println("Add e_j, e_k");
							
							crossed.add(e_j);
							crossed.add(e_k);	// consider
							
	//						System.err.println(e_j);
	//						System.err.println(e_k);
							
						} else {
							
	//						System.err.println("Add e_i^");
							
							crossed.add(e_i.getOppositeEdge());
							
	//						System.err.println(e_i.getOppositeEdge());
						}
					}
				}
				
			} else {
	//			System.err.println("Even worse path (every 2nd time)");
			}
		}
		
		return crossed;
		
	}
	
	public static <
		V extends Vertex<V, E, F> & HasRadius & HasXYZW & HasCurvature,
		E extends Edge<V, E, F> & IsBoundary & HasLength & IsFlippable & IsHidable,
		F extends Face<V, E, F>
	> void willCauseCycle(E e_i, List<E> crossed) {
	
		E e_j = e_i.getNextEdge();
		E e_k = e_j.getNextEdge();
		
		
		// already there?
		if(crossed.contains(e_i) || crossed.contains(e_i.getOppositeEdge()))
			return;
		
		boolean b_j  = (crossed.contains(e_j) || crossed.contains(e_j.getOppositeEdge()));
		boolean b_jo = (crossed.contains(e_i.getOppositeEdge().getNextEdge()) || crossed.contains(e_i.getOppositeEdge().getNextEdge().getOppositeEdge()));
		
		boolean b_k  = (crossed.contains(e_k) || crossed.contains(e_k.getOppositeEdge()));
		boolean b_ko = (crossed.contains(e_i.getOppositeEdge().getPreviousEdge()) || crossed.contains(e_i.getOppositeEdge().getPreviousEdge().getOppositeEdge()));
		
		// will cause cycle?
		if( (b_j && b_k) || (b_jo && b_ko) ) {
			// add anyway!
	//		crossed.add(e_i);
	//		crossed.add(e_i.getOppositeEdge());
			return;
		}
		else {
			crossed.add(e_i);
			crossed.add(e_i.getOppositeEdge());
		}
	}
	
	public static <
		V extends Vertex<V, E, F> & HasRadius & HasXYZW & HasCurvature,
		E extends Edge<V, E, F> & IsBoundary & HasLength & IsFlippable & IsHidable,
		F extends Face<V, E, F>
	> List<E> getContact(F f, List<E> crossed) {

		ArrayList<E> contact = new ArrayList<E>();
		List<E> boundary = f.getBoundary();
		for(E e : boundary) {
			if(crossed.contains(e)) {
				contact.add(e);
			}
		}
		
		return contact;
	
	}
	
	private static <
		V extends Vertex<V, E, F> & HasRadius & HasXYZW & HasCurvature,
		E extends Edge<V, E, F> & IsBoundary & HasLength & IsFlippable & IsHidable,
		F extends Face<V, E, F>
	> void markAsCrossed(E e_i, List<E> crossed) {

		crossed.add(e_i);
		crossed.add(e_i.getOppositeEdge());
	}
	
	
	public static <
		V extends Vertex<V, E, F> & HasRadius & HasXYZW & HasCurvature & HasXY,
		E extends Edge<V, E, F> & IsBoundary & HasLength & IsFlippable & IsHidable & HasAngle,
		F extends Face<V, E, F>
	> List<E> getCuts(EmbeddedEdge<V,E,F> edge){
		
		List<E> p = new ArrayList<E>();
	
		EmbeddedVertex<V,E,F> prev = null;
		for(Iterator<EmbeddedVertex<V,E,F>> it = edge.getEmbeddedVertices().iterator(); it.hasNext();) {
			EmbeddedVertex<V,E,F> next = it.next();
			
			for(E e : GeodesicUtilities.getCrossedEdges(prev, next)) {
				p.add(e);
			}
			prev = next;
		}
	
		return p;
	}

	
	public static <
		V extends Vertex<V, E, F> & HasRadius & HasXYZW & HasCurvature & HasXY,
		E extends Edge<V, E, F> & IsBoundary & HasLength & IsFlippable & IsHidable & HasAngle,
		F extends Face<V, E, F>
	> List<E> traverseVoronoiCells(EmbeddedEdge<V,E,F> edge, HashMap<F, Boolean> cutFaces, List<E> traversed, List<E> wanted, boolean safe) throws AcuteTriangulationException, TriangulationException {
	
		if(!DelaunayUtilities.isAcuteTriangulation(edge.getEmbeddedVertex(0).getGraph())) 
			throw new AcuteTriangulationException("Not an acute triangulation");
		
		EmbeddedVertex<V,E,F> prev = null;
		
	//	LinkedList<E> traversed = new LinkedList<E>();
	//	System.err.println("\n---------------------------\nTraversing a new path, nr vertices: " + edge.getEmbeddedVertices().size() + "\n----------------------------");
	
//		for(Iterator<EmbeddedVertex<V,E,F>> it = edge.getEmbeddedVertices().iterator(); it.hasNext();) {
//			EmbeddedVertex<V,E,F> next = it.next();
			
			for(E e : getCuts(edge)) {
//			for(E e : GeodesicUtilities.getCrossedEdges(prev, next)) {
				if(safe) {
					willCauseCycle(e, traversed);
				} else {
					markAsCrossed(e, traversed);
				}
			}
			
//			prev = next;
//		}
		
	//	System.err.println(traversed);
		return traversed;
	}
	
}
