package halfedge.generator;

import static java.lang.Math.PI;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import halfedge.Edge;
import halfedge.Face;
import halfedge.HalfEdgeDataStructure;
import halfedge.HalfEdgeUtility;
import halfedge.Vertex;
import halfedge.decorations.HasLength;
import halfedge.decorations.HasXY;
import halfedge.decorations.IsHidable;
import halfedge.surfaceutilities.SurfaceException;
import halfedge.surfaceutilities.SurfaceUtility;

import java.io.File;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import javax.vecmath.Point2d;

import alexandrov.graph.CPMEdge;
import alexandrov.graph.CPMFace;
import alexandrov.graph.CPMVertex;
import alexandrov.io.CPMLWriter;

public class SphericalTriangleGenerator 	
<
	V extends Vertex<V, E, F> & HasXY,
	E extends Edge<V, E, F> & HasLength & IsHidable,
	F extends Face<V, E, F>
> {

	private int
		edgesPerSide = 2;
	private HalfEdgeDataStructure<V, E, F>
		graph = null;
	private HashSet<E>
		boundaryEdges = new HashSet<E>();
	private double 
		scale = 1.0;
	
	public SphericalTriangleGenerator(int edgePerSide, double scale, Class<V> vClass, Class<E> eClass, Class<F> fClass) {
		this.edgesPerSide = edgePerSide;
		this.scale = scale;
		graph = HalfEdgeDataStructure.createHEDS(vClass, eClass, fClass);
		initGraph();
	}
	
	
	private void initGraph() {
		LinkedList<V> vl1 = new LinkedList<V>();
		initVertices(vl1);
		triangulateVertexList(vl1);
		
		LinkedList<V> vl2 = new LinkedList<V>();
		initVertices(vl2);
		triangulateVertexList(vl2);
		
		LinkedList<V> vl3 = new LinkedList<V>();
		initVertices(vl3);
		triangulateVertexList(vl3);

		LinkedList<V> vl4 = new LinkedList<V>();
		initVertices(vl4);
		triangulateVertexList(vl4);
		
		
		// connect ends
		vl1.add(vl1.getFirst());
		vl2.add(vl2.getFirst());
		vl3.add(vl3.getFirst());
		vl4.add(vl4.getFirst());
		
		int c0 = 0;
		int c1 = edgesPerSide;
		int c2 = edgesPerSide * 2;
		int c3 = edgesPerSide * 3; // this is again corner0 
		glueReverse(vl1.subList(c0, c1 + 1), vl2.subList(c0, c1 + 1));
		validateList(vl1); validateList(vl2);
		glueReverse(vl1.subList(c2, c3 + 1), vl3.subList(c0, c1 + 1));
		validateList(vl1); validateList(vl3);
		glueReverse(vl1.subList(c1, c2 + 1), vl4.subList(c0, c1 + 1));
		validateList(vl1); validateList(vl4);
		glueReverse(vl2.subList(c1, c2 + 1), vl3.subList(c2, c3 + 1));
		validateList(vl2); validateList(vl3);
		glueReverse(vl3.subList(c1, c2 + 1), vl4.subList(c2, c3 + 1));
		validateList(vl3); validateList(vl4);
		glueReverse(vl4.subList(c1, c2 + 1), vl2.subList(c2, c3 + 1));
		validateList(vl4); validateList(vl2);
		
		removeDoubleEdges();
		
		try {
			SurfaceUtility.fillHoles(graph);
		} catch (SurfaceException e) {
			e.printStackTrace();
		}
	}

	
	private void validateList(LinkedList<V> list){
		if (!list.getFirst().isValid()){
			list.removeFirst();
			list.addFirst(list.getLast());
		} else {
			list.removeLast();
			list.addLast(list.getFirst());
		}
	}
	
	
	private E linkVertices(V v1, V v2){
		E e1 = graph.addNewEdge();
		E e2 = graph.addNewEdge();
		e1.linkOppositeEdge(e2);
		e1.setTargetVertex(v1);
		e2.setTargetVertex(v2);
		double length = v1.getXY().distance(v2.getXY());
		BigDecimal round = new BigDecimal(length);
		double roundLength = round.round(new MathContext(10)).doubleValue();
		e1.setLength(roundLength);
		e2.setLength(roundLength);
		e1.setHidden(true);
		e2.setHidden(true);
		return e2;
	}
	
	
	private void glueReverse(List<V> list1, List<V> list2){
		for (int i = 0; i < list1.size(); i++) {
			V v1 = list1.get(i);
			V v2 = list2.get(list1.size() - i - 1);
			if (v1 == v2) continue;
			List<E> eStar1 = HalfEdgeUtility.findEdgesWithTarget(v1);
			List<E> eStar2 = HalfEdgeUtility.findEdgesWithTarget(v2);
			if (eStar1.size() < eStar2.size()){
				for (E e : eStar1)
					e.setTargetVertex(v2);
				graph.removeVertex(v1);
				list1.remove(v1);
				list1.add(i, v2);
			} else {
				for (E e : eStar2)
					e.setTargetVertex(v1);
				graph.removeVertex(v2);
				list2.remove(v2);
				list2.add(list1.size() - i - 1, v1);
			}
		}
	}
	
	
	private E findEdge(V start, V target, E exclude){
		for (E e : graph.getEdges()){
			if (e == exclude)
				continue;
			if (e.getTargetVertex() == target && e.getStartVertex() == start)
				return e;
		}
		return null;
	}
	
	
	
	private void removeDoubleEdges(){
		for (int i = 0; i < graph.getNumEdges(); i++){
			E e = graph.getEdge(i);
			if (boundaryEdges.contains(e)){
				E opp = e.getOppositeEdge();
				E ne = findEdge(opp.getTargetVertex(), opp.getStartVertex(), e);
				graph.removeEdge(e);
				graph.removeEdge(ne.getOppositeEdge());
				ne.linkOppositeEdge(opp);
				ne.setTargetVertex(ne.getTargetVertex());
				opp.setTargetVertex(opp.getTargetVertex());
				i--;
			}
		}
	}
	
	
	private void triangulateVertexList(LinkedList<V> vertexList){
		for (int i = 0; i < vertexList.size(); i++) {
			V v1 = vertexList.get(i % vertexList.size());
			V v2 = vertexList.get((i + 1) % vertexList.size());
			E boundaryEdge = linkVertices(v1, v2);
			boundaryEdge.setHidden(false);
			boundaryEdge.getOppositeEdge().setHidden(false);
			boundaryEdges.add(boundaryEdge);
		}
		for (int j = 0; j < 3; j++){
			int start = j * edgesPerSide;
			for (int i = 0; i < edgesPerSide / 2; i++) {
				V v1 = vertexList.get(start + i + edgesPerSide / 2);
				V v2 = vertexList.get((start + edgesPerSide + edgesPerSide / 2 - i) % vertexList.size());
				linkVertices(v1, v2);
			}
		}
		for (int j = 0; j < 3; j++){
			int start = j * edgesPerSide;
			for (int i = 0; i < edgesPerSide / 2 - 1; i++) {
				V v1 = vertexList.get(start + i + edgesPerSide / 2);
				V v2 = vertexList.get((start + edgesPerSide + edgesPerSide / 2 - i - 1) % vertexList.size());
				linkVertices(v1, v2);
			}
		}
		try {
			SurfaceUtility.linkAllEdges(graph);
		} catch (SurfaceException e) {
			e.printStackTrace();
		}
	}
	
	
	private void initVertices(LinkedList<V> vertexList){
		int sideSegments = edgesPerSide;
		double size = scale;
		// right side
		for (int i = 0; i < sideSegments; i++){
			double x = cos(PI/3 * i / sideSegments);
			double y = sin(PI/3 * i / sideSegments);
			V v = graph.addNewVertex();
			Point2d p = new Point2d(x, y);
			p.scale(size);
			v.setXY(p);
			vertexList.add(v);
		}
		//left side
		for (int i = 0; i < sideSegments; i++){
			double x = cos(PI * (i / (sideSegments * 3.0) + 2/3.0));
			double y = sin(PI * (i / (sideSegments * 3.0) + 2/3.0));
			V v = graph.addNewVertex();
			Point2d p = new Point2d(x + 1, y);
			p.scale(size);
			v.setXY(p);
			vertexList.add(v);
		}
		//bottom
		Point2d bottomMid = new Point2d(cos(PI/3), sin(PI/3));
		for (int i = 0; i < sideSegments; i++){
			double x = cos(PI * (i / (sideSegments * 3.0) + 4/3.0));
			double y = sin(PI * (i / (sideSegments * 3.0) + 4/3.0));
			V v = graph.addNewVertex();
			Point2d p = new Point2d(x + bottomMid.x, y + bottomMid.y);
			p.scale(size);
			v.setXY(p);
			vertexList.add(v);
		}	
	}


	public HalfEdgeDataStructure<V, E, F> getGraph(){
		return graph;
	}
	
	
	public static void main(String[] args) {
		SphericalTriangleGenerator<CPMVertex, CPMEdge, CPMFace> gen = new SphericalTriangleGenerator<CPMVertex, CPMEdge, CPMFace>(40, 1.0, CPMVertex.class, CPMEdge.class, CPMFace.class);
		File file = new File("test40.cpml"); 
		try {
			CPMLWriter.writeCPML(file, gen.getGraph());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
}
