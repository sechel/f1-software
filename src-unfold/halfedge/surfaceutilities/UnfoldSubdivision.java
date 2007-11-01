package halfedge.surfaceutilities;

import halfedge.Edge;
import halfedge.Face;
import halfedge.HalfEdgeDataStructure;
import halfedge.Vertex;
import halfedge.decorations.HasLabel;
import halfedge.decorations.HasLength;
import halfedge.decorations.HasXY;
import halfedge.decorations.HasXYZW;
import halfedge.decorations.IsFlippable;
import halfedge.decorations.IsHidable;
import halfedge.triangulationutilities.Delaunay;
import halfedge.triangulationutilities.TriangulationException;

import java.util.HashMap;

import javax.vecmath.Point2d;
import javax.vecmath.Point4d;
import javax.vecmath.Vector2d;

import org.apache.commons.collections15.map.MultiKeyMap;

public class UnfoldSubdivision{


	public static 
	<
		V extends Vertex<V, E, F> & HasXY & HasXYZW,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F> & HasLabel
	> HalfEdgeDataStructure<V, E, F> createEdgeSplitTriangle (HalfEdgeDataStructure<V, E, F> graph) throws SurfaceException{
		return createEdgeSplitTriangle(graph, new HashMap<V, V>(), new HashMap<E, V>(), new HashMap<F, F>(), new HashMap<E, E>(), new HashMap<E,F>());
	}
	
	public static 	
	<
		V extends Vertex<V, E, F> & HasXY & HasXYZW,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F> & HasLabel
	>  HalfEdgeDataStructure<V, E, F> createEdgeSplitTriangle
			(
				HalfEdgeDataStructure<V, E, F> graph, 
				HashMap<V, V> vertexVertexMap,
				HashMap<E, V> edgeVertexMap,
				HashMap<F, F> faceFaceMap,
				HashMap<E, E> edgeEdgeMap,
				HashMap<E, F> edgeFaceMap
			) throws SurfaceException
	{
		HalfEdgeDataStructure<V, E, F> triangle = HalfEdgeDataStructure.createHEDS(graph.getVertexClass(), graph.getEdgeClass(), graph.getFaceClass());
		
		// vertices
		for (V v : graph.getVertices()){
			V newVertex = triangle.addNewVertex();
			Subdivision.setVertexPosition(newVertex, v);
			vertexVertexMap.put(v, newVertex);
		}
		
		// edges
		for (E e : graph.getPositiveEdges()){
			V newVertex = triangle.addNewVertex();
			Subdivision.setMeanEdgePosition(newVertex, e);
			edgeVertexMap.put(e, newVertex);
			edgeVertexMap.put(e.getOppositeEdge(), newVertex);
			
			// faces
			F newFace = triangle.addNewFace();
			edgeFaceMap.put(e, newFace);
		}
		
		// faces
		for (F f : graph.getFaces()) {
			F newFace = triangle.addNewFace();
			faceFaceMap.put(f, newFace);
		}
		
//		DualHashMap<V, V, E> vveMap = new DualHashMap<V, V, E>();
//		DualHashMap<V, V, E> vveMapNeg = new DualHashMap<V, V, E>();
		
		MultiKeyMap<V,E> vveMap = new MultiKeyMap<V,E>();
		MultiKeyMap<V,E> vveMapNeg = new MultiKeyMap<V,E>();
		
		for(E e : graph.getEdges()) {
			
			if(e.isPositive()) {
				V vO  = edgeVertexMap.get(e);
				V vE  = edgeVertexMap.get(e.getOppositeEdge().getNextEdge());
				V vNE = edgeVertexMap.get(e.getOppositeEdge().getPreviousEdge());
				V vNW = vertexVertexMap.get(e.getTargetVertex());
				V vW  = edgeVertexMap.get(e.getNextEdge());
				V vSW = edgeVertexMap.get(e.getPreviousEdge());
				V vSE = vertexVertexMap.get(e.getStartVertex());
				
				E eE = vveMap.get(vO, vE);
				if(eE == null) {
					eE  = triangle.addNewEdge();
					vveMap.put(vO, vE, eE);
				}
				
				E eNE = vveMap.get(vNE, vO);
				if(eNE == null) {
					eNE = triangle.addNewEdge();
					vveMap.put(vNE, vO, eNE);
				}
				
				E eNW = vveMap.get(vO, vNW);
				if(eNW == null) {
					eNW = triangle.addNewEdge();
					vveMap.put(vO, vNW, eNW);
				}
				
				E eW = vveMap.get(vW, vO);
				if(eW == null) {
					eW  = triangle.addNewEdge();
					vveMap.put(vW, vO, eW);
				}
				
				E eSW = vveMap.get(vO, vSW);
				if(eSW == null) {
					eSW = triangle.addNewEdge();
					vveMap.put(vO, vSW, eSW);
				}
				
				E eSE = vveMap.get(vSE, vO);
				if(eSE == null) {
					eSE = triangle.addNewEdge();
					vveMap.put(vSE, vO, eSE);
				}
				
				edgeEdgeMap.put(e, eSE);
				
				eE.setTargetVertex(vE);
				eNE.setTargetVertex(vO);
				eNW.setTargetVertex(vNW);
				eW.setTargetVertex(vO);
				eSW.setTargetVertex(vSW);
				eSE.setTargetVertex(vO);
				
				eNE.linkNextEdge(eE);
				eW.linkNextEdge(eNW);
				eSE.linkNextEdge(eSW);
				
			} else {
				V vO  = edgeVertexMap.get(e.getOppositeEdge());
				V vE  = edgeVertexMap.get(e.getNextEdge());
				V vNE = edgeVertexMap.get(e.getPreviousEdge());
				V vNW = vertexVertexMap.get(e.getOppositeEdge().getTargetVertex());
				V vW  = edgeVertexMap.get(e.getOppositeEdge().getNextEdge());
				V vSW = edgeVertexMap.get(e.getOppositeEdge().getPreviousEdge());
				V vSE = vertexVertexMap.get(e.getOppositeEdge().getStartVertex());
				
				E eE = vveMap.get(vE, vO);
				if(eE == null) {
					eE  = triangle.addNewEdge();
					vveMap.put(vE, vO, eE);
				}
				
				E eNE = vveMap.get(vO, vNE);
				if(eNE == null) {
					eNE = triangle.addNewEdge();
					vveMap.put(vO, vNE, eNE);
				}
				
				E eNW = vveMap.get(vNW, vO);
				if(eNW == null) {
					eNW = triangle.addNewEdge();
					vveMap.put(vNW, vO, eNW);
				}
				
				E eW = vveMap.get(vO, vW);
				if(eW == null) {
					eW  = triangle.addNewEdge();
					vveMap.put(vO, vW, eW);
				}
				
				E eSW = vveMap.get(vSW, vO);
				if(eSW == null) {
					eSW = triangle.addNewEdge();
					vveMap.put(vSW, vO, eSW);
				}
				
				E eSE = vveMap.get(vO, vSE);
				if(eSE == null) {
					eSE = triangle.addNewEdge();
					vveMap.put(vO, vSE, eSE);
				}
				
				edgeEdgeMap.put(e, eNW);	// eSE?
				
				eE.setTargetVertex(vO);
				eNE.setTargetVertex(vNE);
				eNW.setTargetVertex(vO);
				eW.setTargetVertex(vW);
				eSW.setTargetVertex(vO);
				eSE.setTargetVertex(vSE);
				
				eE.linkNextEdge(eSE);
				eNW.linkNextEdge(eNE);
				eSW.linkNextEdge(eW);
				
			}

		}
	

		for(E e : graph.getPositiveEdges()){
			
			E smallEdge = edgeEdgeMap.get(e);
			E smallEdgeOpp = edgeEdgeMap.get(e.getOppositeEdge()).getNextEdge();
			smallEdge.linkOppositeEdge(smallEdgeOpp);
			
			E sE2 = vveMap.get(smallEdge.getTargetVertex(), vertexVertexMap.get(e.getTargetVertex()));
			E sE2O = vveMap.get(sE2.getTargetVertex(), smallEdge.getTargetVertex());
			sE2.linkOppositeEdge(sE2O);
			
			V vSW = edgeEdgeMap.get(e.getPreviousEdge()).getTargetVertex();
			E smallEdgeNext = vveMap.get(smallEdge.getTargetVertex(), vSW);
			E smallEdgeNextOpp = vveMap.get(smallEdgeNext.getTargetVertex(), smallEdge.getTargetVertex());
			smallEdgeNext.linkOppositeEdge(smallEdgeNextOpp);
			
			F f = edgeFaceMap.get(e);
			
			smallEdge.setLeftFace(f);
			smallEdgeNext.setLeftFace(f);
			
			sE2.setLeftFace(edgeFaceMap.get(e.getNextEdge()));
			
//			E smallEdgePrev = (E)vveMap.get(vSW, smallEdgeOpp.getTargetVertex());
//			E smallEdgePrevOpp = (E)vveMap.get(smallEdgeOpp.getTargetVertex(), vSW);
//			smallEdgePrev.linkOppositeEdge(smallEdgePrevOpp);

		}
		
		for(F f : graph.getFaces()) {
			E e = f.getBoundaryEdge();
			edgeEdgeMap.get(e).getNextEdge().getOppositeEdge().setLeftFace(faceFaceMap.get(f));
		}
		
		
		return triangle;
		
	}
	
	public static 	
	<
		V extends Vertex<V, E, F> & HasXYZW,
		E extends Edge<V, E, F> & HasLength & IsFlippable & IsHidable,
		F extends Face<V, E, F>
	>  void splitAtFlip(HalfEdgeDataStructure<V, E, F> graph, E edge){
	
		double alpha_i = 0.0;
		double alpha_j = 0.0;
		double alpha_k = 0.0;
		
		double beta_k = 0.0;
		double beta_j = 0.0;
		double beta_i = 0.0;
		
		try {
			alpha_i = Delaunay.getAngle(edge);
			alpha_j = Delaunay.getAngle(edge.getNextEdge());
			alpha_k = Math.PI - alpha_i - alpha_j;
			
			beta_k = Delaunay.getAngle(edge.getOppositeEdge());
			beta_j = Delaunay.getAngle(edge.getOppositeEdge().getNextEdge());
			beta_i = Math.PI - beta_k - beta_j;
		} catch (TriangulationException e) {
			System.err.println(e.getMessage());
		}
			
		double l = edge.getLength();
		
		Point2d P = new Point2d(0,1);
		P.scale(l);
		Point2d A = new Point2d(Math.cos(Math.PI/2.0 + alpha_k), Math.sin(Math.PI/2.0 + alpha_k));
		A.scale(edge.getNextEdge().getNextEdge().getLength());
		Point2d B = new Point2d(Math.cos(Math.PI/2.0 - beta_k), Math.sin(Math.PI/2.0 - beta_k));
		B.scale(edge.getOppositeEdge().getNextEdge().getLength());
		
		Vector2d AB = new Vector2d(B);
		AB.sub(A);
		
		Vector2d AP = new Vector2d(P);
		AP.sub(A);
		
		double phi = Math.acos(AB.dot(AP));
		double theta = Math.PI - phi - beta_i;
		
		double d = Math.sin(phi) * edge.getNextEdge().getLength() / Math.sin(theta); 
		
		double dist = d/l;
		
		//FIXME
		if(dist > 1)
			dist = 1/dist;
		
		Point4d
			a4 = edge.getStartVertex().getXYZW(),
			b4 = edge.getTargetVertex().getXYZW();
		
		System.err.println("Normalized: " + dist);
		
		V inter = splitAtEdge(graph, edge, true);
		
		inter.setXYZW(new Point4d((1-dist)*a4.x + dist*b4.x, (1-dist)*a4.y + dist*b4.y, (1-dist)*a4.z + dist*b4.z,1.0));
		
	}
	
	
	public static 	
	<
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F> & IsHidable,
		F extends Face<V, E, F>
	>  V splitAtEdge(HalfEdgeDataStructure<V, E, F> graph, E edge, boolean hideOld){
		
		E edgeOpp = edge.getOppositeEdge();
		
		E eNE = edgeOpp.getNextEdge();
		E eSE = edge.getPreviousEdge();
		E eSW = edge.getNextEdge();
		E eNW = edgeOpp.getPreviousEdge();
		
		V v = graph.addNewVertex();
		V vE = edgeOpp.getTargetVertex();
		V vW = edge.getTargetVertex();
		V vN = eNW.getStartVertex();
		V vS = eSE.getStartVertex();
		
		E edgeCont = graph.addNewEdge();
		edgeCont.setTargetVertex(vW);
		edgeOpp.setTargetVertex(v);
		edgeCont.linkOppositeEdge(edgeOpp);
		
		E edgeOppCont = graph.addNewEdge();
		edgeOppCont.setTargetVertex(vE);
		edge.setTargetVertex(v);
		edge.linkOppositeEdge(edgeOppCont);
				
		E eN = graph.addNewEdge();
		E eNOpp = graph.addNewEdge();
		E eS = graph.addNewEdge();
		E eSOpp = graph.addNewEdge();
		
		eN.setTargetVertex(vN);
		eNOpp.setTargetVertex(v);
		eNOpp.linkOppositeEdge(eN);
		eN.linkOppositeEdge(eNOpp);
		
		eS.setTargetVertex(vS);
		eSOpp.setTargetVertex(v);
		eSOpp.linkOppositeEdge(eS);
		eS.linkOppositeEdge(eSOpp);
		
		// link together
		edgeOpp.linkNextEdge(eN);
		eN.linkNextEdge(eNW);
		eNW.linkNextEdge(edgeOpp);
		
		edgeCont.linkNextEdge(eSW);
		eSW.linkNextEdge(eSOpp);
		eSOpp.linkNextEdge(edgeCont);
		
		edge.linkNextEdge(eS);
		eS.linkNextEdge(eSE);
		eSE.linkNextEdge(edge);
		
		edgeOppCont.linkNextEdge(eNE);
		eNE.linkNextEdge(eNOpp);
		eNOpp.linkNextEdge(edgeOppCont);
		
		graph.removeFace(edge.getLeftFace());
		graph.removeFace(edgeOpp.getLeftFace());

		F fSE = graph.addNewFace();
		F fNW = graph.addNewFace();
		
		F fNE = graph.addNewFace();
		F fSW = graph.addNewFace();

		edge.setLeftFace(fSE);
		eS.setLeftFace(fSE);
		eSE.setLeftFace(fSE);
		
		edgeOpp.setLeftFace(fNW);
		eN.setLeftFace(fNW);
		eNW.setLeftFace(fNW);
		
		eSOpp.setLeftFace(fSW);
		edgeCont.setLeftFace(fSW);
		eSW.setLeftFace(fSW);
		
		eNOpp.setLeftFace(fNE);
		edgeOppCont.setLeftFace(fNE);
		eNE.setLeftFace(fNE);
		
		if(hideOld) {
			edge.setHidden(true);
			edgeCont.setHidden(true);
			edgeOpp.setHidden(true);
			edgeOppCont.setHidden(true);
		}
		
		return v;
		
	}
	
}
