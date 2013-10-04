package halfedge.surfaceutilities;

import halfedge.Edge;
import halfedge.Face;
import halfedge.HalfEdgeDataStructure;
import halfedge.Node;
import halfedge.Vertex;
import halfedge.decorations.HasLabel;
import halfedge.decorations.HasXY;
import halfedge.decorations.HasXYZW;
import halfedge.generator.FaceByFaceGenerator;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.vecmath.Point2d;
import javax.vecmath.Point4d;

import math.util.VecmathTools;
import util.DualHashMap;

public class Subdivision {

	private static 	
	<
		V extends Vertex<V, E, F> & HasXY & HasXYZW,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> void setMeanFacePosition(V vertex, E faceBoundary){
		Point2d p2d = new Point2d();
		Point4d p4d = new Point4d();
		E actEdge = faceBoundary;
		int numVertices = 0;
		do {
			Point2d actP2d = actEdge.getTargetVertex().getXY();
			Point4d actP4d = actEdge.getTargetVertex().getXYZW();
			VecmathTools.dehomogenize(actP4d);
			p2d.x += actP2d.x;
			p2d.y += actP2d.y;
			p4d.x += actP4d.x;
			p4d.y += actP4d.y;
			p4d.z += actP4d.z;
			numVertices++;
			actEdge = actEdge.getNextEdge();
		} while (actEdge != faceBoundary);
		p2d.x /= numVertices;
		p2d.y /= numVertices;
		p4d.x /= numVertices;
		p4d.y /= numVertices;
		p4d.z /= numVertices;
		p4d.w = 1.0;
		vertex.setXY(p2d);
		vertex.setXYZW(p4d);
	}

	public static 	
	<
		V extends Vertex<V, E, F> & HasXY & HasXYZW,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> void setMeanEdgePosition(V vertex, E faceBoundary){
		Point2d p2d = new Point2d();
		Point4d p4d = new Point4d();
		V v1 = faceBoundary.getStartVertex();
		V v2 = faceBoundary.getTargetVertex();
		VecmathTools.dehomogenize(v1.getXYZW());
		VecmathTools.dehomogenize(v2.getXYZW());
		p2d.x = (v1.getXY().x + v2.getXY().x) / 2;
		p2d.y = (v1.getXY().y + v2.getXY().y) / 2;
		p4d.x = (v1.getXYZW().x + v2.getXYZW().x) /2;
		p4d.y = (v1.getXYZW().y + v2.getXYZW().y) /2;
		p4d.z = (v1.getXYZW().z + v2.getXYZW().z) /2;
		p4d.w = 1.0;
		vertex.setXY(p2d);
		vertex.setXYZW(p4d);
	}
	
	
	public static 
	<
		V extends Vertex<V, E, F> & HasXY & HasXYZW,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> HalfEdgeDataStructure<V, E, F> createStripSubdivision(HalfEdgeDataStructure<V, E, F> graph) throws SurfaceException{
		HashMap<E, V> edgeVertexMap = new HashMap<E, V>();
		HashMap<V, V> vertexVertexMap = new HashMap<V, V>();
		HashMap<F, V> faceVertexMap = new HashMap<F, V>();
		return createStripSubdivision(graph, vertexVertexMap, edgeVertexMap, faceVertexMap);
	}

	@SuppressWarnings("unchecked")
	public static 
	<
		V extends Vertex<V, E, F> & HasXY & HasXYZW,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> HalfEdgeDataStructure<V, E, F> createStripSubdivision(HalfEdgeDataStructure<V, E, F> graph, HashMap<V, V> vertexVertexMap, HashMap<E, V> edgeVertexMap, HashMap<F, V> faceVertexMap) throws SurfaceException{
		HalfEdgeDataStructure<V, E, F> quad = HalfEdgeDataStructure.createHEDS(graph.getVertexClass(), graph.getEdgeClass(), graph.getFaceClass());
		
		for (V v : graph.getVertices()){
			V newV = quad.addNewVertex();
			setVertexPosition(newV, v);
			vertexVertexMap.put(v, newV);
		}
		for (E e : graph.getPositiveEdges()){
			V newV = quad.addNewVertex();
			setMeanEdgePosition(newV, e);
			edgeVertexMap.put(e, newV);
			edgeVertexMap.put(e.getOppositeEdge(), newV);
		}
		for (F f : graph.getFaces()){
			if (f.getBoundary().size() < 4) 
				continue;
			V newVertex = quad.addNewVertex();
			setMeanFacePosition(newVertex, f.getBoundaryEdge());
			faceVertexMap.put(f, newVertex);
		}
		
		FaceByFaceGenerator<V, E, F> g = new FaceByFaceGenerator<V, E, F>(quad);
		for (F f : graph.getFaces()){
			if (f.getBoundary().size() >= 4){
				for (E b : f.getBoundary()){
					V v1 = edgeVertexMap.get(b);
					V v2 = vertexVertexMap.get(b.getTargetVertex());
					V v3 = edgeVertexMap.get(b.getNextEdge());
					V v4 = faceVertexMap.get(f);
					g.addFace(v1, v2, v3, v4);
				}
			} else if (f.getBoundary().size() == 3){
				if (f.isInteriorFace())
					throw new SurfaceException("Cannot subdivide inner triangles consistently, in createStripSubdivision()");
				E b = f.getBoundaryEdge();
				for (E e : f.getBoundary())
					if (!e.isInteriorEdge())
						b = e;
				if (!b.getNextEdge().isInteriorEdge()){
					b = b.getNextEdge();
				}
//				 check if next in boundary is a triangle
				if (!b.getPreviousEdge().isInteriorEdge()){
					F nextInBoundary = b.getOppositeEdge().getPreviousEdge().getRightFace();
					if (nextInBoundary.getBoundary().size() != 3)
						b = b.getPreviousEdge();
				}
				V v1 = vertexVertexMap.get(b.getStartVertex());
				V v2 = edgeVertexMap.get(b);
				V v3 = vertexVertexMap.get(b.getTargetVertex());
				V v4 = edgeVertexMap.get(b.getNextEdge());
				V v5 = vertexVertexMap.get(b.getNextEdge().getTargetVertex());
				V v6 = edgeVertexMap.get(b.getPreviousEdge());
				g.addFace(v1, v2, v6);
				g.addFace(v2, v3, v4);
				g.addFace(v4, v5, v6, v2);
			} else {
				throw new SurfaceException("Cant handle face " + f + ", in createStripSubdivision()");
			}
		}
		SurfaceUtility.linkBoundary(quad);
		return quad;
	}
	
	

	public static 	
	<
		V extends Node<?,?,?> & HasXY & HasXYZW
	> void setVertexPosition(V vertex, V v){
		VecmathTools.dehomogenize(v.getXYZW());
		vertex.setXY(v.getXY());
		vertex.setXYZW(v.getXYZW());
	}

	public static 
	<
		V extends Vertex<V, E, F> & HasXY & HasXYZW,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> HalfEdgeDataStructure<V, E, F> createEdgeQuadGraph(HalfEdgeDataStructure<V, E, F> graph) throws SurfaceException{
		HashMap<E, V> edgeVertexMap = new HashMap<E, V>();
		HashMap<F, V> faceVertexMap = new HashMap<F, V>();
		HashMap<V, V> vertexVertexMap = new HashMap<V, V>();
		return createEdgeQuadGraph(graph, vertexVertexMap, edgeVertexMap, faceVertexMap);
	}

	public static 
	<
		V extends Vertex<V, E, F> & HasXY & HasXYZW,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> HalfEdgeDataStructure<V, E, F> createEdgeQuadGraph(HalfEdgeDataStructure<V, E, F> graph, HashMap<V, V> vertexVertexMap, HashMap<E, V> edgeVertexMap, HashMap<F, V> faceVertexMap) throws SurfaceException{
		HalfEdgeDataStructure<V, E, F> quad = HalfEdgeDataStructure.createHEDS(graph.getVertexClass(), graph.getEdgeClass(), graph.getFaceClass());
		
		// vertices
		for (V v : graph.getVertices()){
			V newVertex = quad.addNewVertex();
			setVertexPosition(newVertex, v);
			vertexVertexMap.put(v, newVertex);
		}
		for (E e : graph.getPositiveEdges()){
			V newVertex = quad.addNewVertex();
			setMeanEdgePosition(newVertex, e);
			edgeVertexMap.put(e, newVertex);
			edgeVertexMap.put(e.getOppositeEdge(), newVertex);
		}
		for (F f : graph.getFaces()){
			V newVertex = quad.addNewVertex();
			setMeanFacePosition(newVertex, f.getBoundaryEdge());
			faceVertexMap.put(f, newVertex);
		}
		
		// edges vertex connections
		DualHashMap<V, V, E> quadEdgeMap = new DualHashMap<V, V, E>();
		for (E e : graph.getPositiveEdges()){
			V v = edgeVertexMap.get(e);
			V v1 = vertexVertexMap.get(e.getTargetVertex());
			V v3 = vertexVertexMap.get(e.getStartVertex());
			V v4 = faceVertexMap.get(e.getLeftFace());
			V v2 = faceVertexMap.get(e.getRightFace());
			
			E e1 = quad.addNewEdge();
			E e2 = quad.addNewEdge();
			E e3 = quad.addNewEdge();
			E e4 = quad.addNewEdge();
			E e5 = quad.addNewEdge();
			E e6 = quad.addNewEdge();
			E e7 = quad.addNewEdge();
			E e8 = quad.addNewEdge();
			
			e1.setTargetVertex(v1);
			e2.setTargetVertex(v);
			e3.setTargetVertex(v2);
			e4.setTargetVertex(v);
			e5.setTargetVertex(v3);
			e6.setTargetVertex(v);
			e7.setTargetVertex(v4);
			e8.setTargetVertex(v);
			
			e2.linkNextEdge(e3);
			e4.linkNextEdge(e5);
			e6.linkNextEdge(e7);
			e8.linkNextEdge(e1);
		
			e1.linkOppositeEdge(e2);
			e3.linkOppositeEdge(e4);
			e5.linkOppositeEdge(e6);
			e7.linkOppositeEdge(e8);
			
			quadEdgeMap.put(v, v1, e1);
			quadEdgeMap.put(v1, v, e2);
			quadEdgeMap.put(v, v2, e3);
			quadEdgeMap.put(v2, v, e4);
			quadEdgeMap.put(v, v3, e5);
			quadEdgeMap.put(v3, v, e6);
			quadEdgeMap.put(v, v4, e7);
			quadEdgeMap.put(v4, v, e8);
		}
		
		// face vertex connections
		HashSet<F> readyFaces = new HashSet<F>();
		for (E bEdge : graph.getEdges()){
			F f = bEdge.getLeftFace();
			if (readyFaces.contains(f))
				continue;
			V v = faceVertexMap.get(f);
			V bVertex = edgeVertexMap.get(bEdge);
			E lastEdge = quadEdgeMap.get(bVertex, v);
			E actEdge = bEdge;
			do {
				actEdge = actEdge.getNextEdge();
				V vertex = edgeVertexMap.get(actEdge);
				E edge =  quadEdgeMap.get(vertex, v);
				edge.linkNextEdge(lastEdge.getOppositeEdge());
				lastEdge = edge;
			} while (actEdge != bEdge);
			readyFaces.add(f);
		}
		// vertex vertex connections
		for (V v : graph.getVertices()){
			V vertex = vertexVertexMap.get(v);
			Collection<E> vStar = quadEdgeMap.get(vertex);
			for (E edge : vStar){
				E linkEdge = edge.getNextEdge().getNextEdge().getNextEdge(); 
				linkEdge.linkNextEdge(edge);
			}
		}
		return quad;
	}

	public static 
	<
		V extends Vertex<V, E, F> & HasXY & HasXYZW,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> HalfEdgeDataStructure<V, E, F> createVertexQuadGraph(HalfEdgeDataStructure<V, E, F> graph) throws SurfaceException{
		HashMap<F, V> faceVertexMap = new HashMap<F, V>();
		HashMap<V, V> vertexVertexMap = new HashMap<V, V>();
		return createVertexQuadGraph(graph, vertexVertexMap, faceVertexMap);
	}

	public static 
	<
		V extends Vertex<V, E, F> & HasXY & HasXYZW,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> HalfEdgeDataStructure<V, E, F> createVertexQuadGraph(HalfEdgeDataStructure<V, E, F> graph, HashMap<V, V> vertexVertexMap, HashMap<F, V> faceVertexMap) throws SurfaceException{
		HalfEdgeDataStructure<V, E, F> quad = HalfEdgeDataStructure.createHEDS(graph.getVertexClass(), graph.getEdgeClass(), graph.getFaceClass());
		HashMap<E, E> leftQuadEdgeMap = new HashMap<E, E>();
		HashMap<E, F> edgeFaceMap = new HashMap<E, F>();
		
		// vertices
		for (V v : graph.getVertices()){
			// quads only for inner edges
			boolean isNewVertex = false;
			for (E e : v.getEdgeStar())
				if (e.isInteriorEdge())
					isNewVertex = true;
			if (!isNewVertex) continue;
			V newVertex = quad.addNewVertex();
			setVertexPosition(newVertex, v);
			vertexVertexMap.put(v, newVertex);
		}
		for (F f : graph.getFaces()){
			V newVertex = quad.addNewVertex();
			setMeanFacePosition(newVertex, f.getBoundaryEdge());
			faceVertexMap.put(f, newVertex);
		}
		for (E e : graph.getPositiveEdges()){
//			 quads only for inner edges
			if (!e.isInteriorEdge())
				continue;
			F newFace = quad.addNewFace();
			edgeFaceMap.put(e, newFace);
			edgeFaceMap.put(e.getOppositeEdge(), newFace);
		}
		
		
		// create inner edges
		for (E e : graph.getPositiveEdges()){
			// quads only for inner edges
			if (!e.isInteriorEdge()) continue;
			V v1 = vertexVertexMap.get(e.getStartVertex());
			V v2 = vertexVertexMap.get(e.getTargetVertex());
			V v3 = faceVertexMap.get(e.getRightFace());
			V v4 = faceVertexMap.get(e.getLeftFace());
			F f = edgeFaceMap.get(e);
			
			E e1 = quad.addNewEdge();
			E e2 = quad.addNewEdge();
			E e3 = quad.addNewEdge();
			E e4 = quad.addNewEdge();
			
			e1.linkNextEdge(e2);
			e2.linkNextEdge(e3);
			e3.linkNextEdge(e4);
			e4.linkNextEdge(e1);
			
			e1.setTargetVertex(v2);
			e2.setTargetVertex(v4);
			e3.setTargetVertex(v1);
			e4.setTargetVertex(v3);
			
			e1.setLeftFace(f);
			e2.setLeftFace(f);
			e3.setLeftFace(f);
			e4.setLeftFace(f);
			
			leftQuadEdgeMap.put(e, e2);
			leftQuadEdgeMap.put(e.getOppositeEdge(), e4);
		}
		
		// connections inside
		for (E e : graph.getEdges()){
			if (!e.isInteriorEdge()) continue;
			E e1 = leftQuadEdgeMap.get(e);
			if (!e.getNextEdge().isInteriorEdge()) continue;
			E e2 = leftQuadEdgeMap.get(e.getNextEdge()).getNextEdge();
			e1.linkOppositeEdge(e2);
		}
		
		// create boundary edges
		for (int i = 0; i < quad.getNumEdges(); i++){
			E e = quad.getEdge(i);
			if (e.getOppositeEdge() != null) continue;
			E opp = quad.addNewEdge();
			
			opp.linkOppositeEdge(e);
		}
		
		// connect boundary
		for (E e : quad.getEdges()){
			if (e.getLeftFace() != null) continue;
			E prev = e;
			do {
				prev = prev.getOppositeEdge().getNextEdge();
			} while (prev.getRightFace() != null);
			prev = prev.getOppositeEdge();
			e.linkPreviousEdge(prev);
		}
		
		for (E e : quad.getEdges()){
			if (e.getLeftFace() != null) continue;
			e.setTargetVertex(e.getNextEdge().getStartVertex());
		}
		
		return quad;
	}

	
	
	public static 
	<
		V extends Vertex<V, E, F> & HasXY & HasXYZW,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> HalfEdgeDataStructure<V, E, F> createMedialGraph (HalfEdgeDataStructure<V, E, F> graph) throws SurfaceException{
		return createMedialGraph(graph, new HashMap<V, F>(), new HashMap<E, V>(), new HashMap<F, F>(), new HashMap<E, E>());
	}
	
	
	
	/**
	 * Generates the medial graph for the given graph
	 * @param graph the graph
	 * @param vClass the vertex class type of the result
	 * @param eClass the edge class type of the result
	 * @param fClass the face class type of the result
	 * @param edgeTable1 this map maps edges of the graph onto edges of the result 
	 * @return the medial
	 * @throws SurfaceException
	 */
	public static 	
	<
		V extends Vertex<V, E, F> & HasXY & HasXYZW,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	>  HalfEdgeDataStructure<V, E, F> createMedialGraph
			(
				HalfEdgeDataStructure<V, E, F> graph, 
				HashMap<V, F> vertexFaceMap,
				HashMap<E, V> edgeVertexMap,
				HashMap<F, F> faceFaceMap,
				HashMap<E, E> edgeEdgeMap1
			) throws SurfaceException
	{
		vertexFaceMap.clear();
		edgeVertexMap.clear();
		faceFaceMap.clear();
		edgeEdgeMap1.clear();
		
		HashMap<E, E> edgeEdgeMap2 = new HashMap<E, E>();
		HalfEdgeDataStructure<V, E, F> result = HalfEdgeDataStructure.createHEDS(graph.getVertexClass(), graph.getEdgeClass(), graph.getFaceClass());
	
		// create faces for faces and vertices
		for (F f : graph.getFaces()){
			F newFace = result.addNewFace();
			if (HasLabel.class.isAssignableFrom(newFace.getClass()))
				((HasLabel)newFace).setLabel(true);
			faceFaceMap.put(f, newFace);
		}
		for (V v : graph.getVertices()){
			F newFace = result.addNewFace();
			if (HasLabel.class.isAssignableFrom(newFace.getClass()))
				((HasLabel)newFace).setLabel(false);
			vertexFaceMap.put(v, newFace);
		}
		
		// make edges and vertices
		for (E e : graph.getEdges()){
			V v = edgeVertexMap.get(e);
			if (v == null) {
				v = result.addNewVertex();
				edgeVertexMap.put(e, v);
				edgeVertexMap.put(e.getOppositeEdge(), v);
				setMeanEdgePosition(v, e);
			}
			E e1 = result.addNewEdge();
			E e2 = result.addNewEdge();
			e1.setTargetVertex(v);
			e2.setTargetVertex(v);
	
			edgeEdgeMap1.put(e, e1);
			edgeEdgeMap2.put(e, e2);
		}
		// link cycles
		for (E e : graph.getEdges()){
			E nextE = e.getNextEdge();
			if (nextE == null)
				throw new SurfaceException("No surface in MedialSurface.generate()");
			E e1 = edgeEdgeMap1.get(e);
			E e2 = edgeEdgeMap2.get(e);
			E e11 = edgeEdgeMap1.get(nextE);
			
			e11.linkOppositeEdge(e2);
			e1.linkNextEdge(e11);
			
			F face = faceFaceMap.get(e.getLeftFace());
			e1.setLeftFace(face);
		}
		// link cocycles
		for (V v : graph.getVertices()){
			E firstEdge = v.getConnectedEdge();
			E actEdge = firstEdge;
			F face = vertexFaceMap.get(v);
			do {
				E nextEdge = actEdge.getNextEdge().getOppositeEdge();
				E e2 = edgeEdgeMap2.get(actEdge);
				E e3 = edgeEdgeMap2.get(nextEdge);
				e3.linkNextEdge(e2);
				actEdge = nextEdge;
				
				e2.setLeftFace(face);
			} while (actEdge != firstEdge);
		}
		return result;
	}
	
	
	
	public static 
	<
		V extends Vertex<V, E, F> & HasXYZW,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F> & HasXYZW
	> HalfEdgeDataStructure<V, E, F> createDualGraph (HalfEdgeDataStructure<V, E, F> graph) throws SurfaceException{
		HalfEdgeDataStructure<V, E, F> r = HalfEdgeDataStructure.createHEDS(graph.getVertexClass(), graph.getEdgeClass(), graph.getFaceClass());
		Map<F, V> faceVertexMap = new HashMap<F, V>();
		Map<E, E> edgeEdgeMap = new HashMap<E, E>();
		Map<V, F> vertexFaceMap = new HashMap<V, F>();
		for (F f : graph.getFaces()) {
			V v = r.addNewVertex();
			v.setXYZW(f.getXYZW());
			faceVertexMap.put(f, v);
		}
		for (E e : graph.getEdges()) {
			if (e.isBoundaryEdge()) continue;
			E ee = r.addNewEdge();
			edgeEdgeMap.put(e, ee);
		}
		for (V v : graph.getVertices()) {
			if (v.isOnBoundary()) continue;
			F f = r.addNewFace();
			f.setXYZW(v.getXYZW());
			vertexFaceMap.put(v, f);
		}
		
		// linkage
		for (E e : graph.getEdges()) {
			if (e.isBoundaryEdge()) continue;
			E ee = edgeEdgeMap.get(e);
			E eeOpp = edgeEdgeMap.get(e.getOppositeEdge());
			ee.linkOppositeEdge(eeOpp);
			ee.setTargetVertex(faceVertexMap.get(e.getLeftFace()));
			ee.setLeftFace(vertexFaceMap.get(e.getStartVertex()));
			E ePrev = e.getPreviousEdge();
			while (ePrev.isBoundaryEdge()) {
				// find next on boundary
				ePrev = ePrev.getPreviousEdge();
			}
			ee.linkNextEdge(edgeEdgeMap.get(ePrev.getOppositeEdge()));
		}
		return r;
	}

	

}
