package alexandrov;

import halfedge.Edge;
import halfedge.Face;
import halfedge.HalfEdgeDataStructure;
import halfedge.Vertex;
import halfedge.decorations.HasRadius;
import halfedge.decorations.HasXYZW;
import halfedge.decorations.IsBoundary;
import halfedge.decorations.IsFlippable;
import halfedge.io.HESerializableReader;
import halfedge.triangulationutilities.ConsistencyCheck;
import halfedge.triangulationutilities.TriangulationException;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashSet;
import java.util.List;

import math.optimization.FunctionNotDefinedException;
import math.optimization.NotConvergentException;
import math.optimization.newton.NewtonSolver;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.Vector;
import no.uib.cipr.matrix.Vector.Norm;
import util.debug.DBGTracer;
import alexandrov.graph.CPMEdge;
import alexandrov.graph.CPMFace;
import alexandrov.graph.CPMVertex;
import alexandrov.math.CapCurvatureFunctional;
import alexandrov.math.CapLinearizable;

public class AlexandrovCap {

	private static Double
		solverError = 1E-10;
	
	/**
	 * Caution! this works on inner vertices only
	 * @param <V>
	 * @param <E>
	 * @param <F>
	 * @param graph
	 * @param error
	 * @param maxInterations
	 * @throws TriangulationException
	 * @throws NotConvergentException
	 */
	public static <
		V extends Vertex<V, E, F> & HasXYZW & HasRadius,
		E extends Edge<V, E, F> & IsFlippable & IsBoundary,
		F extends Face<V, E, F>
	> void constructCap(HalfEdgeDataStructure<V, E, F> graph, Double error, Integer maxInterations) throws TriangulationException, NotConvergentException{
		constructCap_internal(graph, error, maxInterations);
		DBGTracer.msg("layouting...");
		AlexandrovUtility.layoutCap(graph, true);
	}
		
	protected static <
		V extends Vertex<V, E, F> & HasXYZW & HasRadius,
		E extends Edge<V, E, F> & IsFlippable & IsBoundary,
		F extends Face<V, E, F>
	> void constructCap_internal(HalfEdgeDataStructure<V, E, F> graph, Double error, Integer maxInterations) throws TriangulationException, NotConvergentException{
		if (!ConsistencyCheck.isTriangulation(graph))
			throw new TriangulationException("No triangulation!");
		if (!ConsistencyCheck.isDisk(graph))
			throw new TriangulationException("Triangulation is no sphere!");
		if (!CapCurvatureFunctional.isMetricConvex(graph))
			throw new TriangulationException("Cap metric not convex!");
		
		// flip counters are set to zero
		Alexandrov.resetFlipStates(graph);
		
		List<V> innerVertices = CapCurvatureFunctional.getInnerVertices(graph);
		
		// initial heights = 0
		for (V v : graph.getVertices())
			v.setRadius(0.0);
		for (V v : innerVertices)
			v.setRadius(0.01);
		
//		double delta = 0.1;
		Matrix jacobi = CapCurvatureFunctional.getCurvatureDerivative(graph);
		DBGTracer.msg("Jacobi matrix is:");
		DBGTracer.msg(jacobi.toString());
		
		double max_delta = 0.75;
		double boost = 1;
		double delta = max_delta;
		NewtonSolver solver = new NewtonSolver();
		solver.setError(solverError);
		CapLinearizable<V, E, F> fun = new CapLinearizable<V, E, F>(graph);
		Vector kappa = CapCurvatureFunctional.getCurvature(graph);
		DBGTracer.msg("start kappas:");
		DBGTracer.msg(kappa.toString());
		Vector stepKappa = kappa.copy().add(-delta, kappa);
		Vector newRadii = new DenseVector(innerVertices.size());
		getRadii(graph, newRadii);
		Integer actInteration = 0;
		while (kappa.norm(Norm.Two) > error && actInteration < maxInterations){
			if (delta < 1E-50){
				Vector radii = new DenseVector(graph.getNumVertices());
				getRadii(graph, radii);
				DBGTracer.msg("Radii: ");
				DBGTracer.msg(radii.toString());
				DBGTracer.msg(kappa.toString());
				throw new NotConvergentException("Dead end! Maybe nearly concave geometry", delta);
			}
			Vector oldRadii = newRadii.copy();
			try {
				solver.solve(fun, newRadii, stepKappa);
			} catch (FunctionNotDefinedException te){
				delta = Math.pow(delta, 2);
				stepKappa = kappa.copy().add(-delta, kappa);
				newRadii = oldRadii;
				DBGTracer.msg("triangle inequation! -> delta = " + delta);
				boost = 1;
				continue;
			}
			setRadii(graph, newRadii);
			HashSet<E> concaveEdges = new HashSet<E>();
			for (E e : graph.getPositiveEdges()){
				if (!CapCurvatureFunctional.isLocallyConvex(e))
					concaveEdges.add(e);	
			}
			// flip
			if (concaveEdges.size() == 1){
				E flip = concaveEdges.iterator().next();
				if (!CapCurvatureFunctional.isDegenerated(flip)){
					flip.flip();
					DBGTracer.msg("Edge " + flip + "flipped");
				} else
					DBGTracer.msg("Edge degenerated not flipped");
				boost = 1;
//				max_delta = delta;
				actInteration++;
				continue;
			}
			// step was too large
			if (concaveEdges.size() > 1){
				delta *= 0.2;
				DBGTracer.msg("concave edges: " + concaveEdges);
				DBGTracer.msg("-> delta = " + delta);
				stepKappa = kappa.copy().add(-delta, kappa);
				newRadii = oldRadii;
				boost = 1;
				actInteration++;
				continue;
			}
			kappa.set(stepKappa);
			delta = Math.pow(max_delta, 1.0 / boost);
			DBGTracer.msg("resetting or boosting delta to " + delta);
			stepKappa = stepKappa.copy().add(-delta, stepKappa);
			DBGTracer.msg("|Kappa|: " + kappa.norm(Norm.Two) + " boost: " + boost);
			boost *= 2;
			actInteration++;
			Vector radii = new DenseVector(innerVertices.size());
			getRadii(graph, radii);
			DBGTracer.msg("Radii:" + radii);
		}
		DBGTracer.msg("Needed " + actInteration + " iterations to complete.");
		DBGTracer.msg("Error: " + kappa.norm(Norm.Two));
		if (actInteration == maxInterations)
			throw new NotConvergentException("Polytop has not been constructed within the maximum iterations!", stepKappa.norm(Norm.Two));
	}

	
	protected static <
		V extends Vertex<V, E, F> & HasXYZW & HasRadius,
		E extends Edge<V, E, F> & IsFlippable & IsBoundary,
		F extends Face<V, E, F>
	> void setRadii(HalfEdgeDataStructure<V, E, F> graph, Vector radii){
		int i = 0;
		for (V v : CapCurvatureFunctional.getInnerVertices(graph)){
			v.setRadius(radii.get(i));
			i++;
		}
	}
	
	protected static <
		V extends Vertex<V, E, F> & HasXYZW & HasRadius,
		E extends Edge<V, E, F> & IsFlippable & IsBoundary,
		F extends Face<V, E, F>
	> void getRadii(HalfEdgeDataStructure<V, E, F> graph, Vector radii){
		int i = 0;
		for (V v : CapCurvatureFunctional.getInnerVertices(graph)){
			radii.set(i, v.getRadius());
			i++;
	}
}
	
	
	public static void main(String[] args) {
		HalfEdgeDataStructure<CPMVertex, CPMEdge, CPMFace> graph = null;
		try {
			FileInputStream in = new FileInputStream(new File("testing/data/tetraedercap.cpm"));
			HESerializableReader<CPMVertex, CPMEdge, CPMFace> reader = new HESerializableReader<CPMVertex, CPMEdge, CPMFace>(in);
			graph = reader.readHalfEdgeDataStructure();
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		System.err.println(graph.toString());
		
		try {
			constructCap(graph, 1E-10, 20);
		} catch (TriangulationException e) {
			e.printStackTrace();
		} catch (NotConvergentException e) {
			e.printStackTrace();
		}
		
		
	}
	
	
}
