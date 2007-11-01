package circlepatterns.layout;

import halfedge.Edge;
import halfedge.Face;
import halfedge.HalfEdgeDataStructure;
import halfedge.Vertex;
import halfedge.decorations.HasCapitalPhi;
import halfedge.decorations.HasRadius;
import halfedge.decorations.HasRho;
import halfedge.decorations.HasTheta;
import halfedge.decorations.HasXY;
import halfedge.decorations.HasXYZW;
import halfedge.surfaceutilities.Ears;

import java.util.HashSet;
import java.util.List;
import java.util.Stack;

import javax.vecmath.Point2d;

import koebe.KoebePolyhedron;
import math.util.VecmathTools;
import util.debug.DBGTracer;


/**
 * A layouter for circle patterns calculated with 
 * koebe.KoebePolyhedron
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 * @see koebe.KoebePolyhedron
 */
public class CPLayout {

	
	public static interface Rotation <
		V extends Vertex<V, E, F> & HasXY,
		E extends Edge<V, E, F> & HasTheta,
		F extends Face<V, E, F> & HasRho & HasXY & HasRadius & HasCapitalPhi
	> {
		public Point2d rotate(Point2d p, Point2d center, Double phi, Double logScale);
		public double getPhi(E edge);
		public Double getRadius(Double rho);
	}
	

	public static <
		V extends Vertex<V, E, F> & HasXY,
		E extends Edge<V, E, F> & HasTheta,
		F extends Face<V, E, F> & HasRho & HasXY & HasRadius & HasCapitalPhi
	> void calculateEuclidean(HalfEdgeDataStructure<V, E, F> graph){
		EuclideanRotation<V, E, F> rot = new EuclideanRotation<V, E, F>();
		calculateGeneric(graph, rot);
		// set unlayoutable faces
		List<E> ears = Ears.findEarsEdge(graph);
		for (E e : ears)
			e.getTargetVertex().setXY(e.getRightFace().getXY());
	}
	

	public static <
		V extends Vertex<V, E, F> & HasXY & HasXYZW,
		E extends Edge<V, E, F> & HasTheta,
		F extends Face<V, E, F> & HasRho & HasXY & HasRadius & HasCapitalPhi
	> void calculateHyperbolic(HalfEdgeDataStructure<V, E, F> graph){
		HyperbolicRotaion<V, E, F> rot = new HyperbolicRotaion<V, E, F>();
		calculateGeneric(graph, rot);
	}
	
	
	public static <
		V extends Vertex<V, E, F> & HasXY & HasXYZW,
		E extends Edge<V, E, F> & HasTheta,
		F extends Face<V, E, F> & HasXYZW & HasRho & HasXY & HasRadius & HasCapitalPhi
	> void calculateSpherical(HalfEdgeDataStructure<V, E, F> graph){
		SphericalRotaion<V, E, F> rot = new SphericalRotaion<V, E, F>();
		calculateGeneric(graph, rot);
		// project
		KoebePolyhedron.inverseStereographicProjection(graph, 1.0);
		// centers
		for (F f : graph.getFaces()) {
			VecmathTools.projectInverseStereographic(f.getXY(), f.getXYZW(), 1.0);
			f.getXYZW().w /= Math.cos(f.getRadius());
		}
		// set unlayoutable faces
		List<E> ears = Ears.findEarsEdge(graph);
		for (E e : ears)
			e.getTargetVertex().setXYZW(e.getRightFace().getXYZW());
	}
	
	
	private static <
		V extends Vertex<V, E, F> & HasXY,
		E extends Edge<V, E, F> & HasTheta,
		F extends Face<V, E, F> & HasRho & HasXY & HasRadius & HasCapitalPhi
	> void calculateGeneric(HalfEdgeDataStructure<V, E, F> graph, Rotation<V, E, F> rot){
		Stack<E> edgeStack = new Stack<E>();
		HashSet<E> doneEdges = new HashSet<E>();
		HashSet<F> doneFaces = new HashSet<F>();	
		setRadii(graph, rot);
		
		// Init ---------------------------------------
		F rootFace = graph.getFace(0);
		for (F f : graph.getFaces())
			if (f.isInteriorFace()){
				rootFace = f;
				break;
			}
		E rootEdge = rootFace.getBoundaryEdge();
		E firstEdge = rootEdge.getNextEdge();
		
		DBGTracer.msg("Root face is " + rootFace.getIndex());
		DBGTracer.msg("Root edge is " + rootEdge.getIndex());
		
		rootFace.getXY().set(0, 0);
		double firstPlanarRadius = Math.exp(rootFace.getRho());
		rootEdge.getTargetVertex().getXY().set(firstPlanarRadius, 0);
		layoutEdgeCounterClockwise(firstEdge, rot);
		
		if (firstEdge.getRightFace() != null)
			edgeStack.push(firstEdge.getOppositeEdge());
		edgeStack.push(firstEdge);
		
		doneEdges.add(firstEdge);
		doneEdges.add(firstEdge.getOppositeEdge());
		// ---------------------------------------------
		
		
		while (!edgeStack.isEmpty()){
			E edge = edgeStack.pop();
			F face = edge.getLeftFace();
			if (!doneFaces.contains(face)) 
				layoutFace(face, edge, rot, edgeStack, doneEdges, doneFaces);
		}
	}
	
	/*
	 * Layouts all edges and surrounding faces of face
	 * edge and face must have been layouted already
	 */
	private static <
		V extends Vertex<V, E, F> & HasXY,
		E extends Edge<V, E, F> & HasTheta,
		F extends Face<V, E, F> & HasRho & HasXY & HasRadius & HasCapitalPhi
	> void layoutFace(F face, E edge, Rotation<V, E, F> rot, Stack<E> edgeStack, HashSet<E> doneEdges, HashSet<F> doneFaces){
		doneFaces.add(face);
		DBGTracer.msg("Layouting face " + face.getIndex());
		boolean stoppedAtBoundary = false;
		E actEdge = edge.getNextEdge();
		// layout clockwise
		while (actEdge != edge) {
			if (!actEdge.isInteriorEdge()) {
				stoppedAtBoundary = true;
				break;
			}
			if (!doneEdges.contains(actEdge)){
				layoutEdgeCounterClockwise(actEdge, rot);
				if (actEdge.getRightFace() != null)
					edgeStack.push(actEdge.getOppositeEdge());
				doneEdges.add(actEdge);
				doneEdges.add(actEdge.getOppositeEdge());
			}
			actEdge = actEdge.getNextEdge();
		}
		if (!stoppedAtBoundary)
			return;
		// layout counter clockwise if we need to
		actEdge = edge.getPreviousEdge();
		while (actEdge != edge) {
			if (!actEdge.isInteriorEdge())
				return;
			if (!doneEdges.contains(actEdge)){
				layoutEdgeClockwise(actEdge, rot);
				if (actEdge.getRightFace() != null)
					edgeStack.push(actEdge.getOppositeEdge());
				doneEdges.add(actEdge);
				doneEdges.add(actEdge.getOppositeEdge());
			}
			actEdge = actEdge.getPreviousEdge();
		}
	}
	
	
	
	/*
	 * Layout startVertex of edge and its right face if non null
	 */
	private static <
		V extends Vertex<V, E, F> & HasXY,
		E extends Edge<V, E, F> & HasTheta,
		F extends Face<V, E, F> & HasRho & HasXY & HasRadius & HasCapitalPhi
	> void layoutEdgeClockwise(E edge, Rotation<V, E, F> rot){
		DBGTracer.msg("Layouting edge clockwise " + edge.getIndex());
		F leftFace = edge.getLeftFace();
		F rightFace = edge.getRightFace();
		V t = edge.getTargetVertex();
		V s = edge.getStartVertex();
		Double phi = -rot.getPhi(edge);
		s.setXY(rot.rotate(t.getXY(), leftFace.getXY(), 2*phi, 0.0));	
		if (rightFace != null){
			Double theta = -edge.getTheta();
			Double logScale = rightFace.getRho() - leftFace.getRho();
			rightFace.setXY(rot.rotate(leftFace.getXY(), s.getXY(), theta, logScale));
		}
		
	}
	
	
	/*
	 * Layout startVertex of edge and its right face if non null
	 */
	private static <
		V extends Vertex<V, E, F> & HasXY,
		E extends Edge<V, E, F> & HasTheta,
		F extends Face<V, E, F> & HasRho & HasXY & HasRadius & HasCapitalPhi
	> void layoutEdgeCounterClockwise(E edge, Rotation<V, E, F> rot){
		DBGTracer.msg("Layouting edge counter-clockwise " + edge.getIndex());
		F leftFace = edge.getLeftFace();
		F rightFace = edge.getRightFace();
		V t = edge.getTargetVertex();
		V s = edge.getStartVertex();
		Double phi = rot.getPhi(edge);
		t.setXY(rot.rotate(s.getXY(), leftFace.getXY(), 2*phi, 0.0));	
		if (rightFace != null){
			Double theta = edge.getTheta();
			Double logScale = rightFace.getRho() - leftFace.getRho();
			rightFace.setXY(rot.rotate(leftFace.getXY(), t.getXY(), theta, logScale));
		}
		
	}
	
	
	
	private static <
		V extends Vertex<V, E, F> & HasXY,
		E extends Edge<V, E, F> & HasTheta,
		F extends Face<V, E, F> & HasRho & HasXY & HasRadius & HasCapitalPhi
	> void setRadii(HalfEdgeDataStructure<V, E, F> graph, Rotation<V, E, F> rot) {
		for (F f : graph.getFaces())
			f.setRadius(rot.getRadius(f.getRho()));
	}
	
}
