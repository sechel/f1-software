package minimalsurface.util;

import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static math.util.VecmathTools.toVector4d;
import halfedge.Edge;
import halfedge.Face;
import halfedge.HalfEdgeDataStructure;
import halfedge.Vertex;
import halfedge.decorations.HasLabel;
import halfedge.decorations.HasQuadGraphLabeling;
import halfedge.decorations.HasQuadGraphLabeling.QuadGraphLabel;
import halfedge.decorations.HasXY;
import halfedge.decorations.HasXYZW;
import halfedge.surfaceutilities.ConsistencyCheck;
import halfedge.surfaceutilities.Search;
import halfedge.surfaceutilities.Subdivision;
import halfedge.surfaceutilities.SurfaceException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;
import java.util.Vector;

import javax.vecmath.AxisAngle4d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Point4d;
import javax.vecmath.Vector4d;

import math.util.VecmathTools;
import util.debug.DBGTracer;
import circlepatterns.graph.CPEdge;
import circlepatterns.graph.CPFace;
import circlepatterns.graph.CPVertex;
import de.jreality.math.Rn;


public class MinimalSurfaceUtility {
	
	private static final Point4d
		zero = new Point4d();
	
	
	/**
	 * A two-colored cell complex on the sphere
	 * @param <V>
	 * @param <E>
	 * @param <F> FaceClass which has a label
	 */
	public static	
	<
		V extends Vertex<V, E, F> & HasXY & HasXYZW & HasQuadGraphLabeling,
		E extends Edge<V, E, F> & HasLabel,
		F extends Face<V, E, F> & HasLabel & HasXYZW
	> HalfEdgeDataStructure<V, E, F> createFromMedial(HalfEdgeDataStructure<V, E, F> graph) throws SurfaceException{
		HashMap<V, V> vertexVertexMap = new HashMap<V, V>();
		HashMap<F, V> faceVertexMap = new HashMap<F, V>();
		HalfEdgeDataStructure<V, E, F> result = Subdivision.createVertexQuadGraph(graph, vertexVertexMap, faceVertexMap);

		if (!ConsistencyCheck.isValidSurface(result))
			throw new SurfaceException("No valid quad graph in createMinimalSurface()!");
		for (V v : vertexVertexMap.keySet()){
			v.setVertexLabel(QuadGraphLabel.INTERSECTION);
			vertexVertexMap.get(v).setXYZW(v.getXYZW());
			vertexVertexMap.get(v).setXY(v.getXY());
		}
		for (F f : faceVertexMap.keySet()){
			V v = faceVertexMap.get(f);
			v.setXYZW(f.getXYZW());
			if (f.getLabel()) {
				VecmathTools.sphereMirror(v.getXYZW());
				v.setVertexLabel(QuadGraphLabel.CIRCLE);
			} else {
				v.setVertexLabel(QuadGraphLabel.SPHERE);
			}
		}
		MinimalSurfaceUtility.createEdgeLabels(result);
		try {
			dualizeSurfaceConformal(result, true);
		} catch (SurfaceException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	
	public static	
	<
		V extends Vertex<V, E, F> & HasXYZW,
		E extends Edge<V, E, F> & HasLabel,
		F extends Face<V, E, F>
	> void dualizeSurfaceConformal(HalfEdgeDataStructure<V, E, F> graph, boolean signature) throws SurfaceException{
		Map<V, Point4d> newCoordsMap = new HashMap<V, Point4d>();
		Set<V> readyVertices = new HashSet<V>();
		Queue<V> vertexQueue = new LinkedList<V>();
		
		V v0 = graph.getVertex(0);
		newCoordsMap.put(v0, new Point4d(0,0,0,1));
		vertexQueue.offer(v0);
		while (!vertexQueue.isEmpty()){
			V v = vertexQueue.poll();
			Point4d startCoord = newCoordsMap.get(v);
			List<E> star = v.getEdgeStar();
			for (E e : star){
				V v2 = e.getStartVertex();
				if (readyVertices.contains(v2))
					continue;
				else {
					vertexQueue.offer(v2);
					readyVertices.add(v2);
				}
				VecmathTools.dehomogenize(v.getXYZW());
				VecmathTools.dehomogenize(v2.getXYZW());
				VecmathTools.dehomogenize(startCoord);
				Point4d vec = new Point4d(v2.getXYZW());
				vec.sub(v.getXYZW()); // w = 0
				double norm = vec.distance(zero);
				double scale = (e.getLabel() ? -1 : 1) * (signature ? 1 : -1) / (norm * norm);
				vec.x *= scale;
				vec.y *= scale;
				vec.z *= scale;
				vec.w = 0;
				vec.add(startCoord); // w = 1;
				newCoordsMap.put(v2, vec);
			}
		}
		for (V v : graph.getVertices()){
			Point4d p = newCoordsMap.get(v);
			if (p != null)
				v.setXYZW(p);
		}
	}
	

	public static double[][] koenigsDual(
		double[][] vertices, 
		double[] diagInters, 
		int baseVertex, 
		double[] basePosition, 
		int refEdge, 
		double edgeLength
	){
		double[] A = vertices[0].clone();
		double[] B = vertices[1].clone();
		double[] C = vertices[2].clone();
		double[] D = vertices[3].clone();
		double[] M = diagInters.clone();
		
		double[] e1 = Rn.subtract(null, A, M);
		double[] e2 = Rn.subtract(null, B, M);
		Rn.normalize(e1, e1);
		Rn.normalize(e2, e2);
		
		double a = Rn.innerProduct(Rn.subtract(null, A, M), e1);
		double b = Rn.innerProduct(Rn.subtract(null, B, M), e2);
		double c = Rn.innerProduct(Rn.subtract(null, C, M), e1);
		double d = Rn.innerProduct(Rn.subtract(null, D, M), e2);
		
		Rn.times(A, -1/a, e2);
		Rn.times(B, -1/b, e1);
		Rn.times(C, -1/c, e2);
		Rn.times(D, -1/d, e1);
		
		double scaleFactor;
		double norm;
		switch (refEdge){
		case 0:
			norm = Rn.euclideanNorm(Rn.subtract(null, B, A));
			scaleFactor = edgeLength / norm;
			break;
		case 1:
			norm = Rn.euclideanNorm(Rn.subtract(null, C, B));
			scaleFactor = edgeLength / norm;
			break;
		case 2:
			norm = Rn.euclideanNorm(Rn.subtract(null, D, C));
			scaleFactor = edgeLength / norm;
			break;
		case 3:
			norm = Rn.euclideanNorm(Rn.subtract(null, A, D));
			scaleFactor = edgeLength / norm;
			break;
		default:
			throw new IllegalArgumentException("Illegal edge index");
		}
		
		Rn.times(A, scaleFactor, A);
		Rn.times(B, scaleFactor, B);
		Rn.times(C, scaleFactor, C);
		Rn.times(D, scaleFactor, D);
		
		double[] translation = basePosition.clone();
		switch (baseVertex){
		case 0:
			Rn.subtract(translation, translation, A);
			break;
		case 1:
			Rn.subtract(translation, translation, B);
			break;
		case 2:
			Rn.subtract(translation, translation, C);
			break;
		case 3:
			Rn.subtract(translation, translation, D);
			break;
		default:
			throw new IllegalArgumentException("Illegal vertex index");
		}

		Rn.add(A, A, translation);
		Rn.add(B, B, translation);
		Rn.add(C, C, translation);
		Rn.add(D, D, translation);
		
		double[][] returnCoords = new double[4][];
		
		returnCoords[0] = A;
		returnCoords[1] = B;
		returnCoords[2] = C;
		returnCoords[3] = D;
		
		return returnCoords;
	}
	
	
	
	public static <
		V extends Vertex<V, E, F> & HasXYZW,
		E extends Edge<V, E, F> & HasLabel,
		F extends Face<V, E, F> & HasXYZW
	> void dualizeSurfaceKoenigs(HalfEdgeDataStructure<V, E, F> S, Map<CPEdge, Vector4d> edgeNormals, double associatedPsi) throws SurfaceException{
		Map<V, Point4d> dualMap = new HashMap<V, Point4d>();
		Set<E> doneEdges = new HashSet<E>();
		
		Queue<E> edgeQueue = new LinkedList<E>();
		Map<E, Double> factorMap = new HashMap<E, Double>();

		E e0 = S.getEdge(0);
		for (E e : S.getPositiveEdges()) {
			if (e.getLeftFace() != null && e.getRightFace() != null) {
				e0 = e;
				break;
			}
		}
		E e0Opp = e0.getOppositeEdge();
		V v0 = e0.getStartVertex();
		V v1 = e0.getTargetVertex();
		Vector4d g = edgeNormals.get(e0);
		Vector4d n = toVector4d(e0.getLeftFace().getXYZW());
		Vector4d eVec = new Vector4d();
		eVec.sub(v1.getXYZW(), v0.getXYZW());
		eVec = associatedEdgeRotation(eVec, g, n, associatedPsi);
		Point4d v1s = new Point4d(v0.getXYZW());
		v1s.add(eVec);
		
		dualMap.put(v0, v0.getXYZW());
		dualMap.put(v1, v1s);
		factorMap.put(e0, 1.0);
		factorMap.put(e0.getOppositeEdge(), 1.0);
		if (e0.getLeftFace() != null) edgeQueue.offer(e0);
		if (e0Opp.getLeftFace() != null) edgeQueue.offer(e0Opp);
		
		while (!edgeQueue.isEmpty()){
			E e = edgeQueue.poll();
			E ee = e.getPreviousEdge();
			if (doneEdges.contains(e)) {
				continue;
			} else {
				doneEdges.add(e);
			}
			double lambda = factorMap.get(e);
			
			Point4d A = e.getStartVertex().getXYZW();
			Point4d B = e.getTargetVertex().getXYZW();
			Point4d D = ee.getStartVertex().getXYZW();
			Point4d M = e.getLeftFace().getXYZW();
			VecmathTools.dehomogenize(A);
			VecmathTools.dehomogenize(B);
			VecmathTools.dehomogenize(D);
			VecmathTools.dehomogenize(M);
			
			Vector4d AD = new Vector4d();
			AD.sub(D, A);
			Vector4d MD = new Vector4d();
			MD.sub(D, M);
			Vector4d e2 = new Vector4d();
			e2.sub(B, M);

			double beta = e2.length();
			e2.normalize();
			
			double alpha = lambda / beta;
			
			double delta = MD.dot(e2);
			double scale = delta * alpha;

			AD.scale(1 / scale);
			
			g = edgeNormals.get(ee);
			n = toVector4d(ee.getLeftFace().getXYZW());
			AD = associatedEdgeRotation(AD, g, n, associatedPsi);
			
			Point4d As = dualMap.get(e.getStartVertex());
			Point4d Ds = new Point4d(As);
			Ds.add(AD);
			dualMap.put(ee.getStartVertex(), Ds);
			
			edgeQueue.offer(ee);
			factorMap.put(ee, scale);
			if (ee.getOppositeEdge().getLeftFace() != null) {
				edgeQueue.offer(ee.getOppositeEdge());
				factorMap.put(ee.getOppositeEdge(), scale);
			}
		}
		for (V v : S.getVertices()){
			Point4d p = dualMap.get(v);
			if (p != null) v.setXYZW(p);
		}
	}
	
	
	static Vector4d associatedEdgeRotation(Vector4d dualEdgeVec, Vector4d g, Vector4d n, double psi) {
		double alpha = g.angle(n);
		double phi = atan2(sin(psi), cos(psi)*cos(alpha));
		double sinPsi = sin(psi);
		double tanAlpha = Math.tan(alpha);
		double scale = Math.sqrt(1 + sinPsi*sinPsi*tanAlpha*tanAlpha);
		Matrix4d R = new Matrix4d();
		R.set(new AxisAngle4d(g.x, g.y, g.z, -phi));
		Vector4d r = new Vector4d(dualEdgeVec);
		R.transform(r);
		r.scale(scale);
		return r;
	}

	public static <
		V extends Vertex<V, E, F> & HasXYZW,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> Vector4d getEdgeNormal(E e) {
		Point4d s = e.getStartVertex().getXYZW();
		Point4d t = e.getTargetVertex().getXYZW();
		Vector4d eVec = new Vector4d();
		eVec.sub(t, s);
		eVec.normalize();
		Vector4d sVec = new Vector4d(s);
		VecmathTools.dehomogenize(sVec);
		sVec.w = 0.0;
		double lambda = sVec.dot(eVec);
		eVec.scale(lambda);
		sVec.sub(eVec);
		sVec.normalize();
		double check = Math.abs(sVec.dot(eVec));
		assert check < 1E-7 : "edge normal assertion: " + check;
		return sVec;
	}
	
	
	private static	
	<
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F> & HasLabel,
		F extends Face<V, E, F>
	>  void labelFaceOf(E edge) throws SurfaceException{
		E actEdge = edge.getNextEdge();
		while (actEdge != edge){
			actEdge.setLabel(!actEdge.getPreviousEdge().getLabel());
			actEdge.getOppositeEdge().setLabel(actEdge.getLabel());
			actEdge = actEdge.getNextEdge();
		}
		if (edge.getPreviousEdge().getLabel() == edge.getLabel()){
			DBGTracer.msg("could not label face " + edge.getLeftFace() + " correctly, continuing...");
		}
	}
	
	
	public static 
	<
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F> & HasLabel,
		F extends Face<V, E, F>
	> void createEdgeLabels(HalfEdgeDataStructure<V, E, F> graph) throws SurfaceException{
		HashSet<E> pendingEdges = new HashSet<E>();
		Stack<E> edgeStack = new Stack<E>();
		for (E e : graph.getEdges())
			pendingEdges.add(e);
		E edge0 = graph.getEdge(0);
		if (edge0.getLeftFace() != null)
			edgeStack.push(edge0);
		if (edge0.getRightFace() != null)
			edgeStack.push(edge0.getOppositeEdge());
		while (!edgeStack.isEmpty()){
			E edge = edgeStack.pop();
			labelFaceOf(edge);
			for (E e : edge.getLeftFace().getBoundary()){
				if (pendingEdges.contains(e)){
					if (e.getRightFace() != null)
							edgeStack.push(e.getOppositeEdge());
					pendingEdges.remove(e);
				}
			}
		}
	}
	
	
	
	public static 
	<
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F> & HasLabel
	> void createFaceLabels(HalfEdgeDataStructure<V, E, F> graph, boolean flag) throws SurfaceException{
		HashSet<F> pendingFaces = new HashSet<F>();
		Stack<F> faceStack = new Stack<F>();
		for (F f : graph.getFaces())
			pendingFaces.add(f);
		graph.getFace(0).setLabel(flag);
		faceStack.add(graph.getFace(0));
		while (!faceStack.isEmpty()) {
			F f = faceStack.pop();
			for (E b : f.getBoundary()) {
				if (b.getRightFace() != null && pendingFaces.contains(b.getRightFace())) {
					F bf = b.getRightFace();
					bf.setLabel(!f.getLabel());
					faceStack.push(bf);
				}
			}
			pendingFaces.remove(f);
		}
	}
	
	
	
	public static void cutOddPoints(HalfEdgeDataStructure<CPVertex, CPEdge, CPFace> graph) throws SurfaceException{
		// find odd points
		LinkedList<CPVertex> oddVertices = new LinkedList<CPVertex>();
		for (CPVertex v : graph.getVertices()){
			if (v.isOnBoundary())
				continue;
			if (v.getEdgeStar().size() % 2 != 0)
				oddVertices.add(v);
		}

		// create first cut
		if (oddVertices.size() < 2)
			return;
		CPVertex v0 = oddVertices.removeFirst();
		CPVertex v1 = oddVertices.removeFirst();
		Vector<CPEdge> path = Search.bFS(v0, v1, true);
		cutPath(path);
		HashSet<CPVertex> borderSet = new HashSet<CPVertex>();
		for (CPVertex v : graph.getVertices()){
			if (v.isOnBoundary())
				borderSet.add(v);
		}
		borderSet.addAll(getPathVertices(path));
		for (CPVertex v : oddVertices){
			path = Search.bFS(v, borderSet, true);
			cutPath(path);
			borderSet.addAll(getPathVertices(path));
		}
	}
	
	/**
	 * Needs the graph to have a boundary!
	 * @param graph
	 * @throws SurfaceException
	 */
	public static void cutOddPointsToBoundary(HalfEdgeDataStructure<CPVertex, CPEdge, CPFace> graph) throws SurfaceException{
		// find odd points
		LinkedList<CPVertex> oddVertices = new LinkedList<CPVertex>();
		for (CPVertex v : graph.getVertices()){
			if (v.isOnBoundary())
				continue;
			if (v.getEdgeStar().size() % 2 != 0)
				oddVertices.add(v);
		}

		// create first cut
		HashSet<CPVertex> borderSet = new HashSet<CPVertex>();
		for (CPVertex v : graph.getVertices()){
			if (v.isOnBoundary())
				borderSet.add(v);
		}
		if (borderSet.size() == 0)
			throw new SurfaceException("Surface has no boundary, in cutOddPointsToBoundary()!");
		for (CPVertex v : oddVertices){
			Vector<CPEdge> path = Search.bFS(v, borderSet, true);
			cutPath(path);
		}
	}
	
	
	public static  List<CPVertex> getPathVertices(Vector<CPEdge> path){
		LinkedList<CPVertex> result = new LinkedList<CPVertex>(); 
		result.add(path.get(0).getStartVertex());
		for (CPEdge e : path)
			result.add(e.getTargetVertex());
		return result;
	}
	
	
	
	public static void cutPath(Vector<CPEdge> path){
		for (CPEdge e : path)
			GraphUtility.cutAtEdge(e);
	}
	
	
	
}
