package minimalsurface.util;

import halfedge.Edge;
import halfedge.Face;
import halfedge.HalfEdgeDataStructure;
import halfedge.Vertex;
import halfedge.decorations.HasLabel;
import halfedge.decorations.HasQuadGraphLabeling;
import halfedge.decorations.HasXY;
import halfedge.decorations.HasXYZW;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import math.util.VecmathTools;
import util.debug.DBGTracer;
import circlepatterns.graph.CPEdge;
import circlepatterns.graph.CPFace;
import circlepatterns.graph.CPVertex;
import de.jreality.geometry.IndexedFaceSetFactory;
import de.jreality.scene.IndexedFaceSet;

public class GraphUtility {

	
	public static 
	<
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F> & HasLabel
	> void twoColoring(HalfEdgeDataStructure<V, E, F> graph) throws IllegalArgumentException{
		HashSet<F> readyFaces = new HashSet<F>();
		LinkedList<F> queue = new LinkedList<F>();
		F face0 = graph.getFace(0);
		face0.setLabel(true);
		queue.offer(face0);
		readyFaces.add(face0);
		while (!queue.isEmpty()){
			F face = queue.poll();
			Boolean label = face.getLabel();
			for (E e: face.getBoundary()){
				F rightFace = e.getRightFace();
				if (rightFace == null)
					continue;
				if (readyFaces.contains(rightFace)) {
					if (e.getRightFace().getLabel() == label)
						throw new IllegalArgumentException("Graph not two-colorable!");
				} else {
					rightFace.setLabel(!label);
					queue.offer(rightFace);
					readyFaces.add(rightFace);
				}
			}
		}
	}	
	
	
	public static 
	<
		V extends Vertex<V, E, F> & HasXY & HasXYZW & HasQuadGraphLabeling,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> void cutAtEdge(E edge) throws IllegalArgumentException{
		if (!edge.isInteriorEdge())
			throw new IllegalArgumentException("No border edges in cutAtEdge()!");
		HalfEdgeDataStructure<V, E, F> graph = edge.getHalfEdgeDataStructure();
		V v1 = edge.getStartVertex();
		V v2 = edge.getTargetVertex();
		
		E opp = edge.getOppositeEdge();

		boolean splitV1 = v1.isOnBoundary();
		boolean splitV2 = v2.isOnBoundary();
		
		List<E> v1Star = v1.getEdgeStar();
		List<E> v2Star = v2.getEdgeStar();
		
		E new1 = graph.addNewEdge();
		E new2 = graph.addNewEdge();
		
		new1.linkOppositeEdge(opp);
		new2.linkOppositeEdge(edge);
		new1.setTargetVertex(v2);
		new2.setTargetVertex(v1);
		
		new1.linkNextEdge(new2);
		new2.linkNextEdge(new1);
		
		if (splitV1){
			E b = null;
			for (E e : v1Star) {
				if (e.getLeftFace() == null){
					b = e;
					break;
				}
			}
			E b2 = b.getNextEdge();
			List<E> newTargetEdges = new LinkedList<E>();
			E actEdge = opp;
			do {
				newTargetEdges.add(actEdge);
				actEdge = actEdge.getNextEdge().getOppositeEdge();
			} while (actEdge != b);
			newTargetEdges.add(b);
			
			V newV = graph.addNewVertex();
			newV.setXY(v1.getXY());
			newV.setXYZW(v1.getXYZW());
			newV.setVertexLabel(v1.getVertexLabel());
			
			b.linkNextEdge(new1);
			new2.linkNextEdge(b2);
			
			for (E e : newTargetEdges)
				e.setTargetVertex(newV);
		}
		if (splitV2){
			E b = null;
			for (E e : v2Star) {
				if (e.getLeftFace() == null){
					b = e;
					break;
				}
			}
			E b2 = b.getNextEdge();
			List<E> newTargetEdges = new LinkedList<E>();
			E actEdge = edge;
			do {
				newTargetEdges.add(actEdge);
				actEdge = actEdge.getNextEdge().getOppositeEdge();
			} while (actEdge != b);
			newTargetEdges.add(b);
			
			V newV = graph.addNewVertex();
			newV.setXY(v2.getXY());
			newV.setXYZW(v2.getXYZW());
			newV.setVertexLabel(v2.getVertexLabel());
			
			b.linkNextEdge(new2);
			new1.linkNextEdge(b2);
			
			for (E e : newTargetEdges)
				e.setTargetVertex(newV);
		}
		
	}


	public static IndexedFaceSet toIndexedFaceSet(
		HalfEdgeDataStructure<CPVertex, CPEdge, CPFace> surface
	) {
		double[][] vertexData = new double[surface.getNumVertices()][];
		for (CPVertex v : surface.getVertices()){
			VecmathTools.dehomogenize(v.getXYZW());
			if (VecmathTools.isNAN(v.getXYZW())){
				v.getXYZW().set(0, 0, 0, 1);
				DBGTracer.msg("NaN in viewSurface() changed to 0.0!");
			}
			double[] p = new double[]{v.getXYZW().x, v.getXYZW().y, v.getXYZW().z};
			vertexData[v.getIndex()] = p;
		}
		int[][] faceData = new int[surface.getNumFaces()][];
		double[][] faceVertexData = new double[surface.getNumFaces()][];
		for (CPFace f : surface.getFaces()){
			List<CPEdge> b = f.getBoundary();
			faceData[f.getIndex()] = new int[b.size()];
			int counter = 0;
			for (CPEdge e : b){
				faceData[f.getIndex()][counter] = e.getTargetVertex().getIndex();
				counter++;
			}
			//face vertex
			VecmathTools.dehomogenize(f.getXYZW());
			double[] p = new double[]{f.getXYZW().x, f.getXYZW().y, f.getXYZW().z};
			faceVertexData[f.getIndex()] = p;
		}
		
		IndexedFaceSetFactory surfaceFactory = new IndexedFaceSetFactory();
		surfaceFactory.setVertexCount(vertexData.length);
		surfaceFactory.setFaceCount(faceData.length);
		surfaceFactory.setVertexCoordinates(vertexData);
		surfaceFactory.setFaceIndices(faceData);
		surfaceFactory.setGenerateVertexNormals(true);
		surfaceFactory.setGenerateFaceNormals(true);
		surfaceFactory.setGenerateEdgesFromFaces(true);
		surfaceFactory.update();
		IndexedFaceSet ifs = surfaceFactory.getIndexedFaceSet();
		return ifs;
	}
	
	
	
}
