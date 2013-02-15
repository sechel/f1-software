package alexandrov;

import halfedge.Edge;
import halfedge.Face;
import halfedge.HalfEdgeDataStructure;
import halfedge.Vertex;
import halfedge.decorations.HasLength;
import halfedge.decorations.HasRadius;
import halfedge.decorations.HasXYZW;
import halfedge.decorations.IsFlippable;
import halfedge.frontend.controller.RemoteControl;
import halfedge.triangulationutilities.ConsistencyCheck;
import halfedge.triangulationutilities.Delaunay;
import halfedge.triangulationutilities.TriangulationException;
import halfedge.util.Consistency;

import java.util.HashSet;
import java.util.Stack;

import math.optimization.FunctionNotDefinedException;
import math.optimization.IterationMonitor;
import math.optimization.NotConvergentException;
import math.optimization.newton.NewtonSolver;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.Vector;
import no.uib.cipr.matrix.Vector.Norm;
import util.debug.DBGTracer;
import alexandrov.math.CPMCurvatureFunctional;
import alexandrov.math.CPMLinearizable;

/**
 * Calculates and layouts the polyhedron from the given graph. 
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 */
public class Alexandrov2 {

	private static Double
		solverError = 1E-10;
	
	public static <
		V extends Vertex<V, E, F> & HasXYZW & HasRadius,
		E extends Edge<V, E, F> & IsFlippable,
		F extends Face<V, E, F>
	> void constructPolyhedron(HalfEdgeDataStructure<V, E, F> graph, double initRadiusFacor, Double error, Integer maxInterations, IterationMonitor mon, RemoteControl rc) 
	throws TriangulationException, NotConvergentException{
		if (mon != null)
			mon.setIteration(0, 0.0);
		if (!Consistency.checkConsistency(graph))
			throw new TriangulationException("Consistency check failed, data structure corrupted");
		if (!ConsistencyCheck.isTriangulation(graph))
			throw new TriangulationException("No triangulation!");
		if (!ConsistencyCheck.isSphere(graph))
			throw new TriangulationException("Triangulation is no sphere!");
		if (!CPMCurvatureFunctional.isMetricConvex(graph))
			throw new TriangulationException("Metric not convex!");
		
		// flip counters are set to zero
		resetFlipStates(graph);
		
		// enshure plane delaunay condition
		Delaunay.constructDelaunay(graph);
		
		if (!Consistency.checkConsistency(graph))
			throw new TriangulationException("Consistency check failed after delaunay, data structure corrupted");
		
		// initial radii for a convex metric
		Vector gamma = CPMCurvatureFunctional.getGamma(graph);
		double initRadius = 1;
		boolean polytopeIsValid = false;
		do {
			if (rc != null && rc.isStopAsFastAsPossible())
				return;
			initRadius *= initRadiusFacor;
			for (V v : graph.getVertices())
				v.setRadius(initRadius);
			try {
				polytopeIsValid = CPMCurvatureFunctional.isConvex(graph);
				Vector k = CPMCurvatureFunctional.getCurvature(graph);
				for (int i = 0; i < k.size(); i++) {
					double delta = 2*Math.PI - gamma.get(i);
					if (k.get(i) < -1E-3 || delta < k.get(i)) {
						polytopeIsValid = false;
					}
				}
				CPMCurvatureFunctional.getCurvatureDerivative(graph);
			} catch (TriangulationException fnde){
				polytopeIsValid = false;
			}
			if (Double.isInfinite(initRadius)) {
				throw new TriangulationException("Could not find valid initial radii");
			}
		} while (!polytopeIsValid);
		DBGTracer.msg("Setting initial radii to :" + initRadius);
		
		Matrix jacobi = CPMCurvatureFunctional.getCurvatureDerivative(graph);
		DBGTracer.msg("Jacobi matrix is:");
		DBGTracer.msg(jacobi.toString());
		
		double max_delta = 0.75;
		double boost = 1;
		double delta = max_delta;
		NewtonSolver solver = new NewtonSolver();
		solver.setError(solverError);
		CPMLinearizable<V, E, F> fun = new CPMLinearizable<V, E, F>(graph);
		Vector kappa = CPMCurvatureFunctional.getCurvature(graph);
		DBGTracer.msg("start kappas:");
		DBGTracer.msg(kappa.toString());
		Vector stepKappa = kappa.copy().add(-delta, kappa);
		Vector newRadii = new DenseVector(graph.getNumVertices());
		getRadii(graph, newRadii);
		Integer actInteration = 0;
		if (mon != null)
			mon.start(kappa.norm(Norm.Two));
		Stack<E> flipUndoStack = new Stack<E>();
		while (kappa.norm(Norm.Two) > error && actInteration < maxInterations){
			if (rc != null && rc.isStopAsFastAsPossible())
				return;
			if (mon != null)
				mon.setIteration(actInteration, kappa.norm(Norm.Two));
			if (delta < 1E-50){
				Vector radii = new DenseVector(graph.getNumVertices());
				getRadii(graph, radii);
				DBGTracer.msg("Radii: ");
				DBGTracer.msg(radii.toString());
				DBGTracer.msg(kappa.toString());
				throw new NotConvergentException("Dead end!", kappa.norm(Norm.Two));
			}
			Vector oldRadii = newRadii.copy();
			try {
				solver.solve(fun, newRadii, stepKappa);
			} catch (FunctionNotDefinedException te){
				delta = Math.pow(delta, 2);
				stepKappa = kappa.copy().add(-delta, kappa);
				newRadii = oldRadii;
				DBGTracer.msg(te.getMessage() + ", delta = " + delta);
				boost = 1;
				// undo flips
				if (!flipUndoStack.isEmpty()){
					DBGTracer.msg("Undo flips...");
					while (!flipUndoStack.isEmpty())
						flipUndoStack.pop().flip();
					DBGTracer.msg("done.");
					flipUndoStack.clear();
				}
				actInteration++;
				continue;
			}
			setRadii(graph, newRadii);
			HashSet<E> concaveEdges = new HashSet<E>();
			for (E e : graph.getPositiveEdges()){
				if (!CPMCurvatureFunctional.isLocallyConvex(e))
					concaveEdges.add(e);	
			}
			// flip
			if (concaveEdges.size() != 0){
				flipUndoStack.clear();
				DBGTracer.msg("Filpping " + concaveEdges.size() + " edges...");
				for (E flipEdge : concaveEdges){
					flipEdge.flip();
					flipUndoStack.push(flipEdge);
					DBGTracer.msg("Edge " + flipEdge + "flipped");
				}
				DBGTracer.msg("done.");
				boost = 1;
				max_delta = delta;
			} else {
				flipUndoStack.clear();
				max_delta = delta;
				kappa.set(stepKappa);
				delta = Math.pow(max_delta, 1.0 / boost);
				DBGTracer.msg("resetting or boosting delta to " + delta);
				stepKappa = stepKappa.copy().add(-delta, stepKappa);
				DBGTracer.msg("|Kappa|: " + kappa.norm(Norm.Two) + " boost: " + boost);
				boost *= 2;
				Vector radii = new DenseVector(graph.getNumVertices());
				getRadii(graph, radii);
				DBGTracer.msg("Radii:" + radii);
			}
			actInteration++;
		}
		if (mon != null)
			mon.start(kappa.norm(Norm.Two));
		DBGTracer.msg("Needed " + actInteration + " iterations to complete.");
		if (actInteration >= maxInterations)
			throw new NotConvergentException("Polytop has not been constructed within the maximum iterations! ", kappa.norm(Norm.Two));
	
		DBGTracer.msg("layouting...");
		AlexandrovUtility.layoutPolyeder(graph);
	}

	protected static <
		V extends Vertex<V, E, F> & HasXYZW & HasRadius,
		E extends Edge<V, E, F> & HasLength,
		F extends Face<V, E, F>
	> void setRadii(HalfEdgeDataStructure<V, E, F> graph, Vector radii){
		int i = 0;
		for (V v : graph.getVertices()){
			v.setRadius(radii.get(i));
			i++;
		}
	}
	
	protected static <
		V extends Vertex<V, E, F> & HasXYZW & HasRadius,
		E extends Edge<V, E, F> & HasLength,
		F extends Face<V, E, F>
	> void getRadii(HalfEdgeDataStructure<V, E, F> graph, Vector radii){
		int i = 0;
		for (V v : graph.getVertices()){
			radii.set(i, v.getRadius());
			i++;
		}
	}
	
	protected static <
		V extends Vertex<V, E, F> & HasXYZW & HasRadius,
		E extends Edge<V, E, F> & IsFlippable,
		F extends Face<V, E, F>
	> void resetFlipStates(HalfEdgeDataStructure<V, E, F> graph){
		for (E e : graph.getEdges())
			e.resetFlipCount();
	}
}
