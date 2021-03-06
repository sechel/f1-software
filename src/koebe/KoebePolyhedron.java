package koebe;

import halfedge.Edge;
import halfedge.Face;
import halfedge.HalfEdgeDataStructure;
import halfedge.HalfEdgeUtility;
import halfedge.Vertex;
import halfedge.decorations.HasCapitalPhi;
import halfedge.decorations.HasGradientValue;
import halfedge.decorations.HasLabel;
import halfedge.decorations.HasQuadGraphLabeling;
import halfedge.decorations.HasRadius;
import halfedge.decorations.HasRho;
import halfedge.decorations.HasTheta;
import halfedge.decorations.HasXY;
import halfedge.decorations.HasXYZW;
import halfedge.io.HESerializableWriter;
import halfedge.surfaceutilities.ConsistencyCheck;
import halfedge.surfaceutilities.Subdivision;
import halfedge.surfaceutilities.SurfaceException;
import halfedge.surfaceutilities.SurfaceUtility;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.vecmath.Point2d;
import javax.vecmath.Point4d;

import math.optimization.NotConvergentException;
import math.optimization.stepcontrol.ArmijoStepController;
import math.optimization.stepcontrol.StepController;
import circlepatterns.CirclePattern;

/**
 * Calculates the koebe polyhedron
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 */
public class KoebePolyhedron{

	private static double
		rhoBounds = 10;
	
	public static class KoebePolyhedronContext<
		V extends Vertex<V, E, F> & HasXYZW,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> {
		
		public HalfEdgeDataStructure<V, E, F>
			medial = null;
		public HalfEdgeDataStructure<V, E, F>
			circles = null;		
		public HalfEdgeDataStructure<V, E, F>
			polyeder = null;
		public HashMap<E, E>
			edgeEdgeMap = null;
		public HashMap<E, V>
			edgeVertexMap = null;
		public HashMap<F, F>
			faceFaceMap = null;
		public HashMap<V, F>
			vertexFaceMap = new HashMap<V, F>();
		public V
			northPole = null;
		
		public HashMap<E, E> getEdgeEdgeMap() {
			return edgeEdgeMap;
		}
		public HalfEdgeDataStructure<V, E, F> getMedial() {
			return medial;
		}
		public HalfEdgeDataStructure<V, E, F> getCircles() {
			return circles;
		}
		public HalfEdgeDataStructure<V, E, F> getPolyeder() {
			return polyeder;
		}
		public HashMap<E, V> getEdgeVertexMap() {
			return edgeVertexMap;
		}
		public V getNorthPole() {
			return northPole;
		}
		public void setNorthPole(V northPole) {
			this.northPole = northPole;
		}
	}
	
	
	/**
	 * calculates the cone peek of the circle left of medialEdge
	 * @param p the point to store the coordinate
	 * @param medialEdge the medial edge on which's left side p is
	 */
	public static <
		V extends Vertex<V, E, F> & HasXY,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F> & HasRadius & HasXY
	> void calculateConePeek(Point4d p, E medialEdge){
		if (medialEdge.getLeftFace() != null){
			F medialFace = medialEdge.getLeftFace();
			double rq = medialFace.getRadius() * medialFace.getRadius();
			Point2d c = medialFace.getXY();
			Point2d cq = new Point2d(c.x * c.x, c.y * c.y);
			double x = 2 * c.x;
			double y = 2 * c.y;
			double z = -1 + cq.x + cq.y - rq;
			double w =  1 + cq.x + cq.y - rq;
			p.set(x, z, y, w);
		} else {
			Point2d p2 = medialEdge.getStartVertex().getXY();
			Point2d p1 = medialEdge.getTargetVertex().getXY();
			double a = p2.y - p1.y;
			double b = p1.x*p2.y - p1.y*p2.x;
			double c = p1.x - p2.x;
			double d = p1.y*p2.x - p1.x*p2.y;
			p.set(a, b, c, -d);
		}
	}
	
	
	/**
	 * Project stereographically onto the sphere
	 * @param graph
	 * @param scale
	 */
	public static
	<
		InV extends Vertex<InV, InE, InF> & HasXY & HasXYZW,
		InE extends Edge<InV, InE, InF>,
		InF extends Face<InV, InE, InF>
	> void inverseStereographicProjection(HalfEdgeDataStructure<InV, InE, InF> graph, double scale){
		for (InV v : graph.getVertices()){
			double x = v.getXY().x / scale;
			double y = v.getXY().y / scale;
			double nx = 2 * x;
			double ny = x*x + y*y - 1;
			double nz = 2 * y;
			double nw = ny + 2;
			v.getXYZW().set(nx, ny, nz, nw);
		}
	}
	
	
	public static <
		V extends Vertex<V, E, F> & HasXYZW & HasXY & HasQuadGraphLabeling,
		E extends Edge<V, E, F> & HasXYZW & HasTheta,
		F extends Face<V, E, F> & HasLabel & HasRho & HasXYZW & HasXY & HasRadius & HasGradientValue & HasCapitalPhi
	> KoebePolyhedronContext<V, E, F> contructKoebePolyhedron(HalfEdgeDataStructure<V, E, F> graph, double tol, int maxIter) throws SurfaceException{
		KoebePolyhedronContext<V, E, F> context = new KoebePolyhedronContext<V, E, F>();
		try {
			if (!ConsistencyCheck.isValidSurface(graph)) {
				throw new SurfaceException("No valid surface in constructKoebePolyhedron()");
			}
			// medial graph
			HashMap<V, F> vertexFaceMap = new HashMap<V, F>();
			HashMap<F, F> faceFaceMap = new HashMap<F, F>();
			HashMap<E, E> edgeEdgeMap = new HashMap<E, E>();
			HashMap<E, V> edgeVertexMap = new HashMap<E, V>();
			HalfEdgeDataStructure<V, E, F> medial = Subdivision.createMedialGraph(graph, vertexFaceMap, edgeVertexMap, faceFaceMap, edgeEdgeMap);
			HalfEdgeDataStructure<V, E, F> circles = calculateCirclePattern(medial, tol, maxIter);
			
			// fill into graph
			for (F face : graph.getFaces()){
				F f = faceFaceMap.get(face);
				face.setXYZW(f.getXYZW());
			}
			for (V vertex : graph.getVertices()){
				F f = vertexFaceMap.get(vertex);
				vertex.setXYZW(f.getXYZW());
			}
			for (E edge : graph.getEdges()) {
				V v = edgeVertexMap.get(edge);
				edge.setXYZW(v.getXYZW());
			}
			
			// context
			context.edgeEdgeMap = edgeEdgeMap;
			context.edgeVertexMap = edgeVertexMap;
			context.faceFaceMap = faceFaceMap;
			context.vertexFaceMap = vertexFaceMap;
			context.medial = medial;
			context.northPole = medial.getVertex(0);
			context.polyeder = graph;
			context.circles = circles;
		} catch (Exception e){
			throw new SurfaceException(e);
		}
		return context;
	}


	public static <
		V extends Vertex<V, E, F> & HasXYZW & HasXY & HasQuadGraphLabeling, 
		E extends Edge<V, E, F> & HasXYZW & HasTheta, 
		F extends Face<V, E, F> & HasLabel & HasRho & HasXYZW & HasXY & HasRadius & HasGradientValue & HasCapitalPhi,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> HDS calculateCirclePattern(
		HalfEdgeDataStructure<V, E, F> medial,
		double tol, 
		int maxIter
	) throws SurfaceException {
		Set<F> auxFaces = SurfaceUtility.fillHoles(medial);
		if (!ConsistencyCheck.isValidSurface(medial)) {
			throw new SurfaceException("No valid medial graph could be contructed in constructKoebePolyhedron()");
		}
		
		// store old medial combinatorics
		HashMap<V, V> medialVertexMap = new HashMap<V, V>();
		HashMap<E, E> medialEdgeMap = new HashMap<E, E>();
		HashMap<F, F> medialFaceMap = new HashMap<F, F>();
		HalfEdgeDataStructure<V, E, F> medialCirclePattern = new HalfEdgeDataStructure<V, E, F>(medial, medialVertexMap, medialEdgeMap, medialFaceMap);
		
		V infinityVertex = medialCirclePattern.getVertex(0);
		
		if (infinityVertex.getEdgeStar().size() != 4) {
			for (V v : medialCirclePattern.getVertices()) {
				if (v.getEdgeStar().size() == 4) {
					infinityVertex = v;
					break;
				}
			}
		}
		
		if (infinityVertex.getEdgeStar().size() != 4) {
			throw new SurfaceException("no vertex of degree 4 found");
		}
		System.out.println("North index: " + infinityVertex.getIndex());
		V medialNorth = medial.getVertex(infinityVertex.getIndex());
		
		// remove north pole to make a disk
		HalfEdgeUtility.removeVertex(infinityVertex);
		
		// we want an orthogonal circle pattern
		for (E e : medialCirclePattern.getEdges()) {
			e.setTheta(Math.PI / 2);
		}
		
		//debug output
		try {
			HESerializableWriter writer = new HESerializableWriter(new FileOutputStream("data/koebeMedialOut.heds"));
			writer.writeHalfEdgeDataStructure(medialCirclePattern);
			writer.close();
		} catch (IOException e) {
			System.out.println(e);
		}
		if (!ConsistencyCheck.isValidSurface(medialCirclePattern)) {
			throw new SurfaceException("No surface after altering medial graph in constructKoebePolyhedron()");
		}
		
		// optimization
		StepController stepc = new ArmijoStepController();
		try {
			CirclePattern.computeEuclidean(medialCirclePattern, stepc, tol, maxIter, null);
		} catch (NotConvergentException e){
			throw new SurfaceException("minimization did not succeed in constructKoebePolyhedron(): " + e.getMessage());
		}
//		for (F f : medialCirclePattern.getFaces()){
//			if (f.getRho() < -rhoBounds || f.getRho() > rhoBounds) {
//				throw new SurfaceException("radii out of bounds");
//			}
//		}
		
		normalizeBeforeProjection(medialCirclePattern);

//			if (!ConsistencyCheck.isValidSurface(medialCirclePattern))
//				throw new SurfaceException("No surface after circlepattern layout in constructKoebePolyhedron()");
		
		// projection vertices
		inverseStereographicProjection(medialCirclePattern, 1.0);
		// projection faces
		HashSet<F> readyFaces = new HashSet<F>();
		for (E e : medial.getEdges()){
			E circlePatternEdge = medialEdgeMap.get(e);
			if (!circlePatternEdge.isValid()) 
				continue;
			F f = e.getLeftFace();
			if (readyFaces.contains(f)) 
				continue;
			calculateConePeek(f.getXYZW(), circlePatternEdge);
			readyFaces.add(f);
		}
		
		// fill medial information
		for (V v : medial.getVertices()){
			if (v == medialNorth) continue;
			V vertex = medialVertexMap.get(v);
			v.setXYZW(vertex.getXYZW());
			v.setXY(vertex.getXY());
		}
		medialNorth.getXYZW().set(0, 1, 0, 1);
		
		// remove auxiliary
		for (F f : auxFaces) {
			medial.removeFace(f);
		}
		
		return (HDS)medialCirclePattern;
	}
	
	
	private static Point2d
		offset = new Point2d();
	
	public static Point2d baryCenter(HalfEdgeDataStructure<? extends HasXY, ?, ?> graph){
		List<? extends HasXY> vertices = graph.getVertices();
		Point2d result = new Point2d(0,0);
		for (HasXY v : vertices){
			Point2d p = v.getXY();
			result.x += p.x;
			result.y += p.y;
		}
		result.scale(1.0 / graph.getNumVertices());
		return result;
	}
	
	public static double meanLength(HalfEdgeDataStructure<? extends HasXY, ?, ?> graph){
		List<? extends HasXY> vertices = graph.getVertices();
		double result = 0;
		for (HasXY v : vertices){
			Point2d p = v.getXY();
			result += Math.sqrt(p.x*p.x + p.y*p.y);
		}
		return result / graph.getNumVertices();
	}
	
	
	public static  <
		V extends Vertex<V, E, F> & HasXY,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F> & HasXY & HasRadius
	> void normalizeBeforeProjection(HalfEdgeDataStructure<V, E, F> medial){
		normalizeBeforeProjection(medial, 1.0);
	}

	
	public static  <
		V extends Vertex<V, E, F> & HasXY,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F> & HasXY & HasRadius
	> void normalizeBeforeProjection(HalfEdgeDataStructure<V, E, F> medial, double scale){
		offset = baryCenter(medial);
		for (F f : medial.getFaces()){
			f.getXY().sub(offset);
		}
		for (V v : medial.getVertices()){
			v.getXY().sub(offset);
		}
		scale = meanLength(medial) / scale;
		for (F f : medial.getFaces()){
			f.setRadius(f.getRadius() / scale);
			f.getXY().scale(1 / scale);
		}
		for (V v : medial.getVertices()){
			v.getXY().scale(1 / scale);
		}

	}
	
	static double getRhoBounds() {
		return rhoBounds;
	}

	static void setRhoBounds(double rhoBounds) {
		KoebePolyhedron.rhoBounds = rhoBounds;
	}
	
	
	
}
