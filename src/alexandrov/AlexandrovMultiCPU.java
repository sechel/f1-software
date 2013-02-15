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

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
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
public class AlexandrovMultiCPU {

	private static Double
		solverError = 1E-10;
	private static OperatingSystemMXBean 
		osBean = ManagementFactory.getOperatingSystemMXBean();
	
	@SuppressWarnings("unchecked")
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

		int numCPU = osBean.getAvailableProcessors();
		
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
		
		NewtonSolver[] solver = new NewtonSolver[numCPU];
		for (int i = 0; i < solver.length; i++) {
			solver[i] = new NewtonSolver();
			solver[i].setError(solverError);
		}
		
		double max_delta = 0.75;
		double boost = 1;
		double delta = max_delta;
		Vector kappa = CPMCurvatureFunctional.getCurvature(graph);
		DBGTracer.msg("start kappas:");
		DBGTracer.msg(kappa.toString());
		Vector[] stepKappa = new Vector[numCPU]; 
		for (int i = 0; i < stepKappa.length; i++) {
			double cpuDelta = Math.pow(max_delta, 1.0 / (i + 1));
			stepKappa[i] = kappa.copy().add(-cpuDelta, kappa);
			DBGTracer.msg("CPU " + i + " is using start delta = " + cpuDelta);
		}	
			
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
				throw new NotConvergentException("Dead end! ", kappa.norm(Norm.Two));
			}
			
			// parallelization happens only here
			
			Vector oldRadii = newRadii.copy();
			
			Vector[] multiCPURadii = new Vector[numCPU];
			MultiCPUSolver<V, E, F>[] parallelSolver = new MultiCPUSolver[numCPU];
			CPMLinearizable<V, E, F>[] fun = new CPMLinearizable[numCPU];
			HalfEdgeDataStructure<V, E, F>[] cpuGraph = new HalfEdgeDataStructure[numCPU];
			for (int i = 0; i < parallelSolver.length; i++) {
				multiCPURadii[i] = newRadii.copy();
				cpuGraph[i] = new HalfEdgeDataStructure<V, E, F>(graph);
				fun[i] = new CPMLinearizable<V, E, F>(cpuGraph[i]);
				parallelSolver[i] = new MultiCPUSolver<V, E, F>(solver[i], fun[i], multiCPURadii[i], stepKappa[i]);
			}
			MultiCPUWatcher watcher = new MultiCPUWatcher(parallelSolver);
			watcher.startAllAndWaitForReturn();
			
			int lastSuccessfullCPU = watcher.getLastSuccessfullCPUIndex();
			DBGTracer.msg("Successfull CPU: " + lastSuccessfullCPU);
			
			if (lastSuccessfullCPU == -1){
				delta = Math.pow(delta, 2 * numCPU);
				for (int i = 0; i < stepKappa.length; i++) {
					double cpuDelta = Math.pow(delta, 1.0 / (i + 1));
					stepKappa[i] = kappa.copy().add(-cpuDelta, kappa);
					DBGTracer.msg("CPU " + i + " is using start delta = " + cpuDelta);
				}	
				newRadii = oldRadii;
				DBGTracer.msg("No successfull CPU: delta = " + delta);
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
			
			
			setRadii(graph, multiCPURadii[lastSuccessfullCPU]);
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
					DBGTracer.msg("Edge " + flipEdge + " flipped");
				}
				DBGTracer.msg("done.");
				boost = 1;
				max_delta = delta;
			} else {
				flipUndoStack.clear();
				max_delta = delta;
				kappa.set(stepKappa[lastSuccessfullCPU]);
				delta = Math.pow(delta, 1.0 / boost);
				DBGTracer.msg("resetting or boosting delta to " + delta);
				for (int i = 0; i < stepKappa.length; i++) {
					double cpuDelta = Math.pow(delta, 1.0 / (i + 1));
					stepKappa[i] = kappa.copy().add(-cpuDelta, kappa);
					DBGTracer.msg("CPU " + i + " is using start delta = " + cpuDelta);
				}					
				DBGTracer.msg("|Kappa|: " + kappa.norm(Norm.Two) + " boost: " + boost);
				boost *= 2;
//				Vector radii = new DenseVector(graph.getNumVertices());
//				getRadii(graph, radii);
//				DBGTracer.msg("Radii:" + radii);
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

	
	private static class MultiCPUSolver <
		V extends Vertex<V, E, F> & HasXYZW & HasRadius,
		E extends Edge<V, E, F> & IsFlippable,
		F extends Face<V, E, F>
	>  extends Thread{
		
		private boolean 
			isReady = false;
		private boolean 
			succeeded = false;
		private NewtonSolver 
			solver = null;
		private CPMLinearizable<V, E, F> 
			fun = null;
		private Vector	
			x = null,
			y = null;
		
		public MultiCPUSolver(NewtonSolver solver, CPMLinearizable<V, E, F> fun, Vector x, Vector y) {
			this.solver = solver;
			this.fun = fun;
			this.x = x;
			this.y = y;
		}
		
		@Override
		public void run() {
			isReady = false;
			try {
				solver.solve(fun, x, y);
				succeeded = true;
			} catch (FunctionNotDefinedException te){
				succeeded = false;
			} catch (NotConvergentException nce){
				succeeded = false;
			}
			isReady = true;
		}
		
		public boolean isReady(){
			return isReady;
		}
		
		public boolean hasSucceeded(){
			return succeeded;
		}
		
	}
	
	
	private static class MultiCPUWatcher {
		
		MultiCPUSolver<?,?,?>[] cpus = null;
		
		public MultiCPUWatcher(MultiCPUSolver<?,?,?>[] cpus) {
			this.cpus = cpus;
		}
		
		public void startAllAndWaitForReturn(){
			for (MultiCPUSolver<?,?,?> cpu : cpus)
				cpu.start();
			while (!allHaveFinished()){
				try {
					Thread.sleep(20);
				} catch (InterruptedException e) {}
			}
		}
		
		public boolean allHaveFinished(){
			for (MultiCPUSolver<?,?,?> cpu : cpus)
				if (!cpu.isReady())
					return false;
			return true;
		}
		
		public int getLastSuccessfullCPUIndex(){
			for (int i = 0; i < cpus.length; i++) {
				if (!cpus[i].hasSucceeded()){
					if (i == 0) 
						return -1;
					else
						return i - 1;
				}
			}
			return cpus.length - 1;
		}
		
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
