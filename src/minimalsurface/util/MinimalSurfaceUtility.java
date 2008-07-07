package minimalsurface.util;

import halfedge.Edge;
import halfedge.Face;
import halfedge.HalfEdgeDataStructure;
import halfedge.Vertex;
import halfedge.decorations.HasLabel;
import halfedge.decorations.HasQuadGraphLabeling;
import halfedge.decorations.HasXY;
import halfedge.decorations.HasXYZW;
import halfedge.decorations.HasQuadGraphLabeling.QuadGraphLabel;
import halfedge.surfaceutilities.ConsistencyCheck;
import halfedge.surfaceutilities.Search;
import halfedge.surfaceutilities.Subdivision;
import halfedge.surfaceutilities.SurfaceException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import java.util.Vector;

import javax.vecmath.Point4d;

import math.util.VecmathTools;
import util.debug.DBGTracer;
import circlepatterns.graph.CPEdge;
import circlepatterns.graph.CPFace;
import circlepatterns.graph.CPVertex;


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
		HashMap<V, Point4d> newCoordsMap = new HashMap<V, Point4d>();
		HashSet<V> readyVertices = new HashSet<V>();
		LinkedList<V> vertexQueue = new LinkedList<V>();
		V v0 = graph.getVertex(0);
		vertexQueue.offer(v0);
		//vertex 0 in 0.0;
		newCoordsMap.put(v0, new Point4d(0,0,0,1));
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
	
	
	
	public static	
	<
		V extends Vertex<V, E, F> & HasXYZW,
		E extends Edge<V, E, F> & HasLabel,
		F extends Face<V, E, F>
	> void dualizeSurfaceKoenigs(HalfEdgeDataStructure<V, E, F> graph, boolean signature) throws SurfaceException{
		//:TODO implement Koenigs dualization
		HashMap<V, Point4d> newCoordsMap = new HashMap<V, Point4d>();
		HashSet<V> readyVertices = new HashSet<V>();
		LinkedList<V> vertexQueue = new LinkedList<V>();
		V v0 = graph.getVertex(0);
		vertexQueue.offer(v0);
		
		HashMap<E, Double> scaleMap = new HashMap<E, Double>();
		for (E e : graph.getEdges()) {
			double scale = 1.0;
			
			scaleMap.put(e, scale);
			scaleMap.put(e.getOppositeEdge(), scale);
		}
		
		//vertex 0 in 0.0;
		newCoordsMap.put(v0, new Point4d(0,0,0,1));
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
//				double norm = vec.distance(zero);
				double scale = (e.getLabel() ? -1 : 1) * (signature ? 1 : -1) * scaleMap.get(e);
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
