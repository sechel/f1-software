package circlepatterns;

import halfedge.Edge;
import halfedge.Face;
import halfedge.HalfEdgeDataStructure;
import halfedge.Vertex;
import halfedge.decorations.HasCapitalPhi;
import halfedge.decorations.HasGradientValue;
import halfedge.decorations.HasRadius;
import halfedge.decorations.HasRho;
import halfedge.decorations.HasTheta;
import halfedge.decorations.HasXY;
import halfedge.decorations.HasXYZW;
import math.optimization.IterationMonitor;
import math.optimization.NotConvergentException;
import math.optimization.Optimizable;
import math.optimization.newton.NewtonOptimizer;
import math.optimization.newton.NewtonOptimizer.Solver;
import math.optimization.stepcontrol.StepController;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Vector;
import no.uib.cipr.matrix.VectorEntry;
import circlepatterns.layout.CPLayout;
import circlepatterns.math.CPEuclideanOptimizable;
import circlepatterns.math.CPSphericalFunctional.SpecialEdgeFlag;
import circlepatterns.math.CPSphericalOptimizable;

public class CirclePattern {


	public static <
		V extends Vertex<V, E, F> & HasXY,
		E extends Edge<V, E, F> & HasTheta,
		F extends Face<V, E, F> & HasRho & HasXY & HasRadius & HasGradientValue & HasCapitalPhi
	>  void computeEuclidean(
			HalfEdgeDataStructure<V, E, F> graph, 
			StepController stepController, 
			Double error, 
			Integer maxIterations, 
			IterationMonitor monitor
	) throws NotConvergentException{
		Optimizable func = new CPEuclideanOptimizable<V, E, F>(graph);
		Vector initGuess = new DenseVector(func.getDomainDimension());
		computeGeneric(func, initGuess, stepController, error, maxIterations, monitor);
		CPLayout.calculateEuclidean(graph);
	}
	
	
	public static <
		V extends Vertex<V, E, F> & HasXYZW & HasXY,
		E extends Edge<V, E, F> & HasTheta,
		F extends Face<V, E, F> & HasRho & HasXYZW & HasXY & HasRadius & HasGradientValue & HasCapitalPhi
	>  void computeSpherical(
			HalfEdgeDataStructure<V, E, F> graph, 
			SpecialEdgeFlag flag, 
			StepController stepController, 
			Double error, 
			Integer maxIterations, 
			IterationMonitor monitor) throws NotConvergentException{
		Optimizable func = new CPSphericalOptimizable<V, E, F>(graph, flag);
		Vector initGuess = new DenseVector(func.getDomainDimension());
		for (VectorEntry e : initGuess)
			e.set(-5.0);
		computeGeneric(func, initGuess, stepController, error, maxIterations, monitor);
		CPLayout.calculateSpherical(graph);
	}
	

	private static void computeGeneric(
			Optimizable func, 
			Vector guess,
			StepController stepC, 
			Double err, 
			Integer maxIter, 
			IterationMonitor mon
	) throws NotConvergentException{
		NewtonOptimizer optimizer = new NewtonOptimizer();
		optimizer.setSolver(Solver.GMRES);
		optimizer.setIterationMonitor(mon);
		optimizer.setError(err);
		optimizer.setMaxIterations(maxIter);
		if (stepC != null){
			optimizer.setStepController(stepC);
		}
		optimizer.minimize(guess, func);
	}
	
}
