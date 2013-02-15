package alexandrov;

import halfedge.Edge;
import halfedge.Face;
import halfedge.HalfEdgeDataStructure;
import halfedge.Vertex;
import halfedge.decorations.HasLength;
import halfedge.decorations.HasRadius;
import halfedge.decorations.HasXYZW;
import halfedge.decorations.IsFlippable;
import halfedge.triangulationutilities.ConsistencyCheck;
import halfedge.triangulationutilities.Delaunay;
import halfedge.triangulationutilities.TriangulationException;
import halfedge.util.Consistency;

import java.util.HashSet;

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
public class AlexandrovSimple {

	private static Double
		solverError = 1E-10;

	public static <
		V extends Vertex<V, E, F> & HasXYZW & HasRadius,
		E extends Edge<V, E, F> & IsFlippable,
		F extends Face<V, E, F>
	> void constructPolyhedron(HalfEdgeDataStructure<V, E, F> graph, double initRadiusFacor, Double error, Integer maxInterations, IterationMonitor mon) 
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
		System.err.println("Alexandrov Simple!");
		// flip counters are set to zero
		resetFlipStates(graph);
		
		// enshure plane delaunay condition
		Delaunay.constructDelaunay(graph);
		
		if (!Consistency.checkConsistency(graph))
			throw new TriangulationException("Consistency check failed after delaunay, data structure corrupted");
		
		// initial radii for a convex metric
		Vector gamma = CPMCurvatureFunctional.getGamma(graph);
		double initRadius = 1;
		boolean isConvex = false;
		do {
			initRadius *= initRadiusFacor;
			for (V v : graph.getVertices())
				v.setRadius(initRadius);
			try {
				isConvex = CPMCurvatureFunctional.isConvex(graph);
				Vector k = CPMCurvatureFunctional.getCurvature(graph);
				for (int i = 0; i < k.size(); i++)
					if (k.get(i) < (2*Math.PI - gamma.get(i)) / 2)
						isConvex = false;
				CPMCurvatureFunctional.getCurvatureDerivative(graph);
			} catch (TriangulationException fnde){
				isConvex = false;
			}
		} while (!isConvex);
		DBGTracer.msg("Setting initial radii to :" + initRadius);
		
		Matrix jacobi = CPMCurvatureFunctional.getCurvatureDerivative(graph);
		DBGTracer.msg("Jacobi matrix is:");
		DBGTracer.msg(jacobi.toString());
		
		double max_delta = 0.75;
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
		while (kappa.norm(Norm.Two) > error && actInteration < maxInterations){
			if (mon != null)
				mon.setIteration(actInteration, kappa.norm(Norm.Two));
			if (delta < 1E-50){
				Vector radii = new DenseVector(graph.getNumVertices());
				getRadii(graph, radii);
				DBGTracer.msg("Radii: ");
				DBGTracer.msg(radii.toString());
				DBGTracer.msg(kappa.toString());
				throw new NotConvergentException("Dead end! Maybe a loop in the triangulation.", delta);
			}
			Vector oldRadii = newRadii.copy();
			try {
				solver.solve(fun, newRadii, stepKappa);
			} catch (FunctionNotDefinedException te){
				delta = Math.pow(delta, 2);
				stepKappa = kappa.copy().add(-delta, kappa);
				newRadii = oldRadii;
				DBGTracer.msg("triangle inequation! -> delta = " + delta);
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
			if (concaveEdges.size() == 1){
				E flip = concaveEdges.iterator().next();
				flip.flip();
				DBGTracer.msg("Edge " + flip + "flipped");
				actInteration++;
				continue;
			}
			// step was too large
			if (concaveEdges.size() > 1){
				delta = Math.pow(delta, 2);
				DBGTracer.msg("concave edges: " + concaveEdges);
				DBGTracer.msg("-> delta = " + delta);
				stepKappa = kappa.copy().add(-delta, kappa);
				newRadii = oldRadii;
				actInteration++;
				continue;
			}
			kappa.set(stepKappa);
			delta = max_delta;
			DBGTracer.msg("resetting or boosting delta to " + delta);
			stepKappa = stepKappa.copy().add(-delta, stepKappa);
			DBGTracer.msg("|Kappa|: " + kappa.norm(Norm.Two));
			actInteration++;
			Vector radii = new DenseVector(graph.getNumVertices());
			getRadii(graph, radii);
			DBGTracer.msg("Radii:" + radii);
		}
		if (mon != null)
			mon.start(kappa.norm(Norm.Two));
		DBGTracer.msg("Needed " + actInteration + " iterations to complete.");
		if (actInteration == maxInterations)
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
