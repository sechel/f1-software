package halfedge.unfoldutilities;

import halfedge.Edge;
import halfedge.Face;
import halfedge.HalfEdgeDataStructure;
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
import halfedge.surfaceutilities.SurfaceUtility;
import halfedge.triangulationutilities.TriangulationException;

import java.util.Iterator;

import javax.vecmath.Vector2d;
import javax.vecmath.Vector3d;

import math.util.VecmathTools;

import org.apache.commons.collections15.BidiMap;
import org.apache.commons.collections15.bidimap.DualHashBidiMap;

import alexandrov.math.CPMCurvatureFunctional;

public class StarTreeFinder <
		V extends Vertex<V, E, F> & HasRadius & HasXYZW & HasCurvature,
		E extends Edge<V, E, F> & IsBoundary & HasLength & IsFlippable & IsHidable,
		F extends Face<V, E, F>
	>  {
		
	private static double lastBetaOut = 0.0;
	private static double lastLength = 0.0;
	private static double radius = 20.0;
	private static int recursionLimit = 10;

	public static <
		V extends Vertex<V, E, F> & HasRadius & HasXYZW & HasCurvature & HasXY,
		E extends Edge<V, E, F> & IsBoundary & HasLength & IsFlippable & IsHidable & HasAngle,
		F extends Face<V, E, F>
	> BidiMap<V, EmbeddedEdge<V,E,F>> getStarTree(HalfEdgeDataStructure<V,E,F> graph, V source){

		// todo: caching
		
//		DualTreeBidiMap<EmbeddedEdge<V,E,F>, V>  paths = new DualTreeBidiMap<EmbeddedEdge<V,E,F>, V>(new EdgeAngleComparator<EmbeddedEdge<V,E,F>>(), new VertexIndexComparator<V>());
		BidiMap<V, EmbeddedEdge<V,E,F>> paths = new DualHashBidiMap<V, EmbeddedEdge<V,E,F>>();
		
//		TreeMap<Double, E> sectors = prepareEdgeSectors(source, SurfaceUtility.getCanonicalBaseEdge(source));
//		DualTreeBidiMap<Double, E> sectors = prepareEdgeSectors(source, source.getConnectedEdge());
		
		EmbeddedVertex<V,E,F> endVertex = null;

		double vertexCurvature = 0.0;
		try {
			SurfaceUtility.calculateAngles(graph);
			vertexCurvature = 2*Math.PI - CPMCurvatureFunctional.getGammaAt(source);
		} catch(TriangulationException e) {
			System.err.println(e.getMessage());
		}
		if(vertexCurvature == 0.0) {
			System.err.println("Flat source vertex, probably shouldnt happen");
		}
		
		
//		LoopingIterator<E> edgeIterator = new LoopingIterator<E>(source.getEdgeStar());
		Iterator<E> edgeIterator = source.getEdgeStar().iterator();
		E sourceEdge = edgeIterator.next();
		double beta = 0.0;
		for(double angle = 0.0; angle < 2*Math.PI - vertexCurvature; angle+=0.0001) {	
//		for(double angle = 2*Math.PI - vertexCurvature; angle > 0; angle-=0.0001) {	

			if(angle > sourceEdge.getAngle() && edgeIterator.hasNext()) {
//			if(angle > sourceEdge.getAngle() && edgeIterator.hasNext()) {
				beta = sourceEdge.getAngle();
				sourceEdge = edgeIterator.next();
			}
				
			EmbeddedEdge<V,E,F> testPath = new EmbeddedEdge<V,E,F>();
			
			walkPath2(testPath, sourceEdge, angle, beta, radius);

			if(testPath.getEmbeddedVertices().size() < 2)
				System.err.println("Suspiciously short walked path");
			
			endVertex = testPath.getEmbeddedVertices().getLast().getOpposite();
//			V closest = endVertex.getClosestVertex();
			V closest = testPath.getEndVertex();
			if(endVertex.isVertex() && closest != source && closest.hasCurvature()) {
//				System.err.println("Yes found a vertex..");
				double len  = testPath.getLength();
				if(paths.containsKey(closest)) {
					if(len < paths.get(closest).getLength() ) {
						paths.put(closest, testPath);
					} else {
						// do nothing because new path is longer
					}
				} else {
					paths.put(closest, testPath);
				}
			} else {
//				System.err.println("This one didn't find a vertex: " + testPath);
			}
		}	

		int recursion = 0;
		
		int nrFlat = 0;
		for(V v : graph.getVertices()) {
			if(v.hasCurvature() == false)
				nrFlat++;
		}
		System.err.println("Found " + paths.size() + " vertices");
		if(paths.size() == graph.getVertices().size() - 1 - 2*nrFlat)
			System.err.println("Found shortest path to all vertices (" + nrFlat + " flat ones)");
		else if(paths.size() < graph.getVertices().size() - 1 - 2*nrFlat) {
			System.err.println("Didn't find all vertices (" + nrFlat + " flat ones)");
			if(recursion < recursionLimit) {
				System.err.println("Increasing radius of search");
				radius = radius * 2;
				recursion += 1;
				return getStarTree(graph, source);
			} else {
				System.err.println("Recursion limit reached!");
			}
		} else {
			System.err.println("Found too many vertices.. hmm.. weird!");
		}
		return paths;
	}
	
//	private static <
//		V extends Vertex<V, E, F> & HasRadius & HasXYZW & HasCurvature,
//		E extends Edge<V, E, F> & IsBoundary & HasLength & IsFlippable & IsHidable,
//		F extends Face<V, E, F>
//	> DualTreeBidiMap<Double, E> prepareEdgeSectors(V source, E sourceEdge) {
//		
//		Comparator<Double> a = new DoubleComparator();
//		
//		DualTreeBidiMap<Double, E> sectors = new DualTreeBidiMap<Double, E>(new DoubleComparator(), new EdgeLengthComparator<V, E, F>() );
//		List<E> edgeStar = source.getEdgeStar();
//		
//		if(edgeStar.size() < 1)
//			System.err.println("Bad connection");
//
//		System.err.println("Edgestarsize: " + edgeStar.size());
//		
//		if(sourceEdge.getTargetVertex() != source) {
//			System.err.println("No connection!");
//		}
//		
//		LinkedList<E> nl = new LinkedList<E>();
//		for(Iterator<E> it = edgeStar.listIterator(edgeStar.lastIndexOf(sourceEdge)); it.hasNext(); ) {
//			nl.add(it.next());
//		}
//		nl.addAll(edgeStar.subList(0, edgeStar.lastIndexOf(sourceEdge)));
//		if(nl.size() != edgeStar.size())
//			System.err.println("Wrong with ordering");
//		else
//			edgeStar = nl;
//		
//		System.err.println("Edgestar: " + edgeStar);
//		
//		double traversedAngle = 0.0;
//		try {
//			for(E e : edgeStar) {
//				double eA = Delaunay.getAngle(e);
//				sectors.put(eA+traversedAngle, e.getNextEdge());
//
//				traversedAngle += eA;
//			} 
//		} catch(TriangulationException error) {
//			System.err.println(error.getMessage());
//		}
//		return sectors;
//		
//	}
//	
//	private static <
//		V extends Vertex<V, E, F> & HasRadius & HasXYZW & HasCurvature,
//		E extends Edge<V, E, F> & IsBoundary & HasLength & IsFlippable & IsHidable,
//		F extends Face<V, E, F>
//	> E getEdgeBySector(DualTreeBidiMap<Double, E> sectors, double alpha){
//		for(double angle : sectors.keySet()) {
////			System.err.println("Trying " + alpha + " in " + angle);
//			if(alpha <= angle)
//				return (E)sectors.get(angle);
//		}
//		//defaulting to first edge
////		System.err.println("Didn't find sector (floating point?), defaulting to first edge");
//		return (E)sectors.values().toArray()[0];//FIXME, bad bad
//	}
//	
//	private static <
//		V extends Vertex<V, E, F> & HasRadius & HasXYZW & HasCurvature,
//		E extends Edge<V, E, F> & IsBoundary & HasLength & IsFlippable & IsHidable,
//		F extends Face<V, E, F>
//	> double getAngleBySector(DualTreeBidiMap<Double, E> sectors, double alpha) {
//		double temp = 0.0;
//		for(double angle : sectors.keySet()) {
//			if(alpha <= angle)
//				return temp;
//			temp = angle;
//		}
//		return temp;
//	}
//	
//	private static <
//		V extends Vertex<V, E, F> & HasRadius & HasXYZW & HasCurvature,
//		E extends Edge<V, E, F> & IsBoundary & HasLength & IsFlippable & IsHidable,
//		F extends Face<V, E, F>
//	> double getAngleBySector2(TreeMap<Double, E> sectors, E e) {
//			
//		double temp = 0.0;
//		for(Double d : sectors.keySet()) {
//			if(e == sectors.get(d))
//				temp = d;
//		}
//		return temp;
//	}

	public static <
		V extends Vertex<V, E, F> & HasRadius & HasXYZW & HasCurvature & HasXY,
		E extends Edge<V, E, F> & IsBoundary & HasLength & IsFlippable & IsHidable & HasAngle,
		F extends Face<V, E, F>
	> boolean walkPath2(EmbeddedEdge<V,E,F> path, E sourceEdge, double alpha, double beta, double maxLength) {

		path.setAngle(alpha);
	
		lastBetaOut = alpha - beta;
		lastBetaOut = sourceEdge.getAngle() - alpha;
	
		EmbeddedVertex<V,E,F> startPos = new EmbeddedVertex<V,E,F>(sourceEdge.getHalfEdgeDataStructure(), sourceEdge.getNextEdge(), 0.0, 0.0);
	
		EmbeddedVertex<V,E,F> oldPos = startPos;
		boolean walking = true;
		
		path.addEmbeddedVertexPair(startPos);
		path.setLength(0.0);
		
		boolean wasVertex = false;
		
		while( walking && path.getLength() < maxLength ) {
			
			EmbeddedVertex<V,E,F> newPos = shootRay(oldPos, lastBetaOut);// updates beta_out
			
			path.addEmbeddedVertexPair(newPos);
			
			Vector3d t = new Vector3d(VecmathTools.p4top3(newPos.getXYZW()));
			t.sub(VecmathTools.p4top3(oldPos.getXYZW()));
			double tempLength = t.length();
			path.addLength(tempLength);
	//		path.addLength(lastLength); // FIXME!
			
	//		System.err.println("Walking from " + oldPos.toString() + " to " + newPos.toString());
			
			oldPos = newPos.getOpposite();
			if(newPos.getClosestVertex().hasCurvature()){
	//			System.err.println("Passing a non-flat vertex");
				wasVertex = newPos.isVertex();
				if(wasVertex) {
					walking = false;
			//	newEdge.setTargetVertex(newPos);
				}
			}
		}
		return !walking;
	}
	
//	public static <
//		V extends Vertex<V, E, F> & HasRadius & HasXYZW & HasCurvature & HasXY,
//		E extends Edge<V, E, F> & IsBoundary & HasLength & IsFlippable & IsHidable & HasAngle,
//		F extends Face<V, E, F>
//	> boolean walkPath(EmbeddedEdge<V,E,F> path, DualTreeBidiMap<Double, E>  sectors, V source, double curvature, double dist, double alpha, double maxLength) {
////		path = new EmbeddedEdge<V,E,F>();
//		
////		alpha = alpha % (2*Math.PI - curvature);
//		path.setAngle(alpha);
//		
//		E startEdge = getEdgeBySector(sectors, alpha);
//
//		lastBetaOut = alpha - getAngleBySector(sectors, alpha);
////		lastBetaOut = alpha - getAngleBySector2(sectors, startEdge);
//
////		EmbeddedVertex<V,E,F> startPos = new EmbeddedVertex<V,E,F>(graph, startEdge.getIndex(), dist, 0.0);
//		EmbeddedVertex<V,E,F> startPos = new EmbeddedVertex<V,E,F>(source.getHalfEdgeDataStructure(), startEdge, dist, 0.0);
//
//		EmbeddedVertex<V,E,F> oldPos = startPos;
//		boolean walking = true;
//		
//		path.addEmbeddedVertexPair(startPos);
//		path.setLength(0.0);
//		
//		boolean wasVertex = false;
//		
//		
//		while( walking && path.getLength() < maxLength ) {
//			
//			EmbeddedVertex<V,E,F> newPos = shootRay(oldPos, lastBetaOut);// updates beta_out
//			
//			path.addEmbeddedVertexPair(newPos);
//			
//			Vector3d t = new Vector3d(VecmathTools.p4top3(newPos.getXYZW()));
//			t.sub(VecmathTools.p4top3(oldPos.getXYZW()));
//			double tempLength = t.length();
//			path.addLength(tempLength);
////			path.addLength(lastLength); // FIXME!
//			
////			System.err.println("Walking from " + oldPos.toString() + " to " + newPos.toString());
//			
//			oldPos = newPos.getOpposite();
//			if(newPos.getClosestVertex().hasCurvature()){
////				System.err.println("Passing a non-flat vertex");
//				wasVertex = newPos.isVertex();
//				if(wasVertex) {
//					walking = false;
//			//	newEdge.setTargetVertex(newPos);
//				}
//			}
//		}
//		return !walking;
//	}
	
//	public static <
//		V extends Vertex<V, E, F> & HasRadius & HasXYZW & HasCurvature,
//		E extends Edge<V, E, F> & IsBoundary & HasLength & IsFlippable & IsHidable,
//		F extends Face<V, E, F>
//	> boolean cutPath(EmbeddedEdge<V,E,F> path, V source, double dist, double alpha) {
////		path = new EmbeddedEdge<V,E,F>();
//		
//		double curvature = 0.0;
//		try{
//			curvature = CPMCurvatureFunctional.getCurvature(source);
//		} catch(TriangulationException e) {
//			System.err.println(e.getMessage());
//		}
//		
////		alpha = Math.min(alpha, 2.0*Math.PI - curvature);
//		
//		path.setAngle(alpha);
//		
//		E startEdge = getEdgeBySector(alpha);
//
//		alpha = alpha - getAngleBySector(alpha);
//		lastBetaOut = alpha;
//
////		EmbeddedVertex<V,E,F> startPos = new EmbeddedVertex<V,E,F>(graph, startEdge.getIndex(), dist, 0.0);
//		EmbeddedVertex<V,E,F> startPos = new EmbeddedVertex<V,E,F>(source.getHalfEdgeDataStructure(), startEdge, dist, 0.0);
//
//		EmbeddedVertex<V,E,F> oldPos = startPos;
//		boolean walking = true;
//		
//		path.addEmbeddedVertex(startPos);
//		path.setLength(0.0);
//		
//		boolean wasVertex = false;
//		
//		int num = 0;
//		
//		while( walking && path.getLength() < maxLength ) {
//			num++;			
//			EmbeddedVertex<V,E,F> newPos = shootRay(oldPos, lastBetaOut);// updates beta_out
//
//			if(num > 0) {
//				Point4d pos = newPos.getXYZW();
//				V nv = Subdivision.splitAtEdge(startPos.getGraph(), newPos.getEdge(), false);
//				nv.setXYZW(pos);
//			}
//			
//			path.addEmbeddedVertex(newPos);
//			path.addLength(lastLength); // FIXME!
//			
////			System.err.println("Walking from " + oldPos.toString() + " to " + newPos.toString());
//			
//			oldPos = newPos.getOpposite();
//			if(newPos.getClosestVertex().hasCurvature()){
////				System.err.println("Passing a non-flat vertex");
//				wasVertex = newPos.isVertex();
//				if(wasVertex) {
//					walking = false;
//			//	newEdge.setTargetVertex(newPos);
//				}
//			}
//		}
//		return !walking;
//	}
	
	private static <
		V extends Vertex<V, E, F> & HasRadius & HasXYZW & HasCurvature,
		E extends Edge<V, E, F> & IsBoundary & HasLength & IsFlippable & IsHidable,
		F extends Face<V, E, F>
	> EmbeddedVertex<V,E,F> shootRay(EmbeddedVertex<V,E,F> p, double angle) {
//		int i = p.getEdgeIndex();
//		E e_i = graph.getEdge(i);
		E e_i = p.getEdge();
		double le_i = e_i.getLength();
		
		E e_j = e_i.getNextEdge();
		double le_j = e_j.getLength();
		
		E e_k = e_j.getNextEdge();
		double le_k = e_k.getLength();
		
		double beta = angle;
		double d1 = p.getDistance();
		
		double cos_alpha_j = (le_i*le_i + le_k*le_k - le_j*le_j)/(2*le_i*le_k);
		double alpha_j = Math.acos(cos_alpha_j);
		double sin_alpha_j = Math.sin(alpha_j);
		
		double cos_alpha_k = (le_i*le_i + le_j*le_j - le_k*le_k)/(2*le_i*le_j);
		double alpha_k = Math.acos(cos_alpha_k);
		double sin_alpha_k = Math.sin(alpha_k);
		
		double l = Math.sqrt(le_k*le_k + d1*d1 - 2*cos_alpha_j*le_k*d1);
		
		double gamma = Math.asin((sin_alpha_j*le_k)/l);
		
		double theta = Math.PI - gamma;
		
		Vector2d v = new Vector2d(d1 + le_j*cos_alpha_k - le_i, le_j*sin_alpha_k);
		v.normalize();
		Vector2d w = new Vector2d(-le_i + d1, 0);
		w.normalize();
		
		double cos_theta = v.dot(w);
		theta = Math.acos(cos_theta);

		double d2 = 0.0;
		int intersect_index = 0;
		E intersectEdge = null;
		//insert proper case for vertex here?
		if(beta <= theta) {
//			System.err.println("Going left");
			lastBetaOut = alpha_k + beta;
			lastLength = (le_j*sin_alpha_k)/Math.sin(theta);
			d2 = ((le_i - d1)*Math.sin(beta))/Math.sin(Math.PI - lastBetaOut);
			intersect_index = e_j.getIndex();
			intersectEdge = e_j;
		} else {
//			System.err.println("Going right");
			lastBetaOut = beta - alpha_j;
			double sin_beta_min_alpha_i = Math.sin(lastBetaOut);
			lastLength = (le_k*sin_alpha_j)/Math.sin(Math.PI - theta);
			d2 = le_k - (d1*Math.sin(Math.PI - beta))/sin_beta_min_alpha_i;
			intersect_index = e_k.getIndex();
			intersectEdge = e_k;
		}
		
		if(d2 < 0)
			System.err.println("Wrong calculation for "  + p);
//		return new EmbeddedVertex<V,E,F>(graph, intersect_index, d2, 0.0);
		EmbeddedVertex<V,E,F> ret = new EmbeddedVertex<V,E,F>(p.getGraph(), intersectEdge, d2, 0.0);
		ret.setAngle(lastBetaOut);
		return ret;
	}
}
