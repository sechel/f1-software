package halfedge.generator;

import halfedge.Edge;
import halfedge.Face;
import halfedge.HalfEdgeDataStructure;
import halfedge.Vertex;
import halfedge.surfaceutilities.Ears;

import java.util.List;

import circlepatterns.graph.CPEdge;
import circlepatterns.graph.CPFace;
import circlepatterns.graph.CPVertex;


/**
 * A generator for a grid graph
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 */
public abstract class SquareGridGenerator {

	
	public static 	
	<
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> HalfEdgeDataStructure<V, E, F> generate(Integer width, Integer height, Class<V> vClass, Class<E> eClass, Class<F> fClass){
		HalfEdgeDataStructure<V, E, F> ds = HalfEdgeDataStructure.createHEDS(vClass, eClass, fClass);
		E firstTopEdge = null;
		E actTopEdge = null;
		E actLeftEdge = null;
		for (Integer i = 0; i < height; i++){
			for (Integer j = 0; j < width; j++){
				if (j == 0){
					actTopEdge = firstTopEdge;
					actLeftEdge = null;
				}
				actLeftEdge = attachSquareFace(ds, actTopEdge, actLeftEdge);
				if (i != 0)
					actTopEdge = actLeftEdge.getPreviousEdge();
				else
					actTopEdge = null;
				if (j == 0)
					firstTopEdge = actLeftEdge.getNextEdge();
			}
		}
		
		return ds;
	}
	
	public static HalfEdgeDataStructure<Vertex.Generic, Edge.Generic, Face.Generic> generate(Integer width, Integer height){
		return generate(width, height, Vertex.Generic.class, Edge.Generic.class, Face.Generic.class);
	}
	
	
	
	private static 	
	<
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
    > E attachSquareFace(HalfEdgeDataStructure<V, E, F> ds, E top, E left){
		F rootFace = ds.addNewFace();
		
		// create vertices or use existing
		V vertex1 = null;
		if (left != null)
			vertex1 = left.getTargetVertex();
		else
			vertex1 = ds.addNewVertex();
		V vertex2 = null;
		if (left == null && top == null)
			vertex2 = ds.addNewVertex();
		else {
			if (left != null)
				vertex2 = left.getStartVertex();
			else
				vertex2 = top.getTargetVertex();
		}
		V vertex3 = null;
		if (top != null)
			vertex3 = top.getStartVertex();
		else
			vertex3 = ds.addNewVertex();
		V vertex4 = ds.addNewVertex();
		
		// create the left edges or use existing
		E edgeL1 = null;
		if (left != null)
			edgeL1 = left;
		else 
			edgeL1 = ds.addNewEdge();
		E edgeL2 = null;
		if (top != null)
			edgeL2 = top;
		else 
			edgeL2 = ds.addNewEdge();	
		E edgeL3 = ds.addNewEdge();
		E edgeL4 = ds.addNewEdge();

		// the right edges or use existing
		E edgeR1 = null;
		if (left != null)
			edgeR1 = left.getOppositeEdge();
		else
			edgeR1 = ds.addNewEdge();
		E edgeR2 = null;
		if (top != null)
			edgeR2 = top.getOppositeEdge();
		else
			edgeR2 = ds.addNewEdge();
		E edgeR3 = ds.addNewEdge();
		E edgeR4 = ds.addNewEdge();
		
		// find next and pref right edges
		E nextR4 = null;
		if (left == null)
			nextR4 = edgeR1;
		else 
			nextR4 = left.getNextEdge();
		E nextR1 = null;
		if (left == null){
			if (top == null)
				nextR1 = edgeR2;
			else
				nextR1 = top.getNextEdge();
		} else 
			nextR1 = edgeR1.getNextEdge();
		E nextR2 = null;
		if (top == null)
			nextR2 = edgeR3;
		else 
			nextR2 = edgeR2.getNextEdge();
		
		E prevR3 = null;
		if (top == null)
			prevR3 = edgeR2;
		else
			prevR3 = top.getPreviousEdge();	
		E prevR2 = null;
		if (top == null){
			if (left == null)
				prevR2 = edgeR1;
			else
				prevR2 = left.getPreviousEdge();
		} else
			prevR2 = edgeR2.getPreviousEdge();
		
		
		// connect left edges
		edgeL1.linkNextEdge(edgeL4);
		edgeL2.linkNextEdge(edgeL1);
		edgeL3.linkNextEdge(edgeL2);
		edgeL4.linkNextEdge(edgeL3);
		edgeL1.setLeftFace(rootFace);
		edgeL2.setLeftFace(rootFace);
		edgeL3.setLeftFace(rootFace);
		edgeL4.setLeftFace(rootFace);
		edgeL1.setTargetVertex(vertex1);
		edgeL2.setTargetVertex(vertex2);
		edgeL3.setTargetVertex(vertex3);
		edgeL4.setTargetVertex(vertex4);
		
		// connect right edges
		edgeR1.setTargetVertex(vertex2);
		edgeR2.setTargetVertex(vertex3);
		edgeR3.setTargetVertex(vertex4);
		edgeR4.setTargetVertex(vertex1);
		edgeR1.linkNextEdge(nextR1);
		edgeR2.linkNextEdge(nextR2);
		edgeR3.linkNextEdge(edgeR4);
		edgeR4.linkNextEdge(nextR4);

		// connect the border
		prevR3.linkNextEdge(edgeR3);
		prevR2.linkNextEdge(edgeR2);
		
		// fix opposite edges
		edgeL1.linkOppositeEdge(edgeR1);
		edgeL2.linkOppositeEdge(edgeR2);
		edgeL3.linkOppositeEdge(edgeR3);
		edgeL4.linkOppositeEdge(edgeR4);
		
		return edgeR3;
	}

	public static void setSquareGridThetas(HalfEdgeDataStructure<CPVertex, CPEdge, CPFace> ds, double eps1, double eps2){
		
		for (CPEdge edge : ds.getPositiveEdges()){
			if (edge.isInteriorEdge())
				edge.setTheta(Math.PI / 2);
			else
				edge.setTheta(3.0 * Math.PI / 4.0);
		} 
		
		List<CPEdge> ears = Ears.findEarsEdge(ds);
		ears.get(0).setTheta(Math.PI * 3. / 4 + eps1);
		ears.get(0).getNextEdge().setTheta(Math.PI * 3. / 4 + eps1);
		ears.get(1).setTheta(Math.PI  * 3. / 4 + eps2);
		ears.get(1).getNextEdge().setTheta(Math.PI * 3. / 4 + eps2);
		ears.get(2).setTheta(Math.PI * 3. / 4 - eps1);
		ears.get(2).getNextEdge().setTheta(Math.PI * 3. / 4 - eps1);
		ears.get(3).setTheta(Math.PI * 3. / 4 - eps2);
		ears.get(3).getNextEdge().setTheta(Math.PI * 3. / 4 - eps2);

		ears.get(0).getRightFace().setCapitalPhi(2*Math.PI + eps1);
		ears.get(1).getRightFace().setCapitalPhi(2*Math.PI + eps1);
		ears.get(2).getRightFace().setCapitalPhi(2*Math.PI + eps1);
		ears.get(3).getRightFace().setCapitalPhi(2*Math.PI + eps1);
	}
	
	
}
