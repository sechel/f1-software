package alexandrov.frontend.calculation;

import halfedge.HalfEdgeDataStructure;
import halfedge.frontend.controller.RemoteControl;
import halfedge.surfaceutilities.ConsistencyCheck;
import halfedge.util.Consistency;
import math.optimization.IterationMonitor;
import alexandrov.Alexandrov;
import alexandrov.Alexandrov2;
import alexandrov.AlexandrovMultiCPU;
import alexandrov.AlexandrovSimple;
import alexandrov.frontend.content.AlexandrovPolytopView;
import alexandrov.frontend.content.CalculationDialog.CalculationMethod;
import alexandrov.frontend.controller.MainController;
import alexandrov.graph.CPMEdge;
import alexandrov.graph.CPMFace;
import alexandrov.graph.CPMVertex;

public class CalculatePolyhedronThread implements IterationMonitor, Runnable, RemoteControl {

	private MainController
		controller = null;
	private Double
		initRadiusFactor = 2.0,
		endError = 1E-2,
		startError = 0.0;
	private Integer
		progress = 0,
		maxIterations = 100,
		iteration = 0;
	private HalfEdgeDataStructure<CPMVertex, CPMEdge, CPMFace>
		graph = null;
	private AlexandrovPolytopView
		viewer = null;
	private boolean 
		stop = false;
	private CalculationMethod
		method = CalculationMethod.FastButDangerous;
	


	public CalculatePolyhedronThread(MainController controller, AlexandrovPolytopView viewer){
		this.controller = controller;
		this.viewer = viewer;
	}
	

	@Override
	public void setIteration(Integer iteration, Double error) {
		this.iteration = iteration;
		this.progress = (int)(100 * (startError - error) / (startError - endError));
		controller.setStatus("Calculation: " + progress + "%, Iteration: " + iteration);
		Thread.yield();
	}

	@Override
	public void done(Double error){
		
	}
	
	@Override
	public void start(Double error){
		startError = error;
	}
	
	
	@Override
	public void setStopAsFastAsPossible(boolean stop) {
		this.stop = stop;
	}
	
	@Override
	public boolean isStopAsFastAsPossible() {
		return stop;
	}
	
	/**
	 * Start a new calculation thread with the given graph
	 * @param graph the graph to be calculated
	 */
	public void calculatePolyhedron(HalfEdgeDataStructure<CPMVertex, CPMEdge, CPMFace> graph){
		this.graph = graph;
		//System.err.println(graph);
		controller.setStatus("checking surface...");
		if (!Consistency.checkConsistency(graph)){
			controller.setStatus("Consistency check failed! Data structure currupted!");
			return;
		}
    	if (!ConsistencyCheck.isValidSurface(graph)){
    		controller.setStatus("No valid surface in calculation!");
    		return;
    	}
    	controller.setStatus("done.");
		new Thread(this, "Calculation Thread").start();
	}

	
	@Override
	public void run() {
		stop = false;
		try {
			long time = System.currentTimeMillis();
			controller.setCalculationRemote(this);
			switch (method) {
				case FastButDangerous:
					Alexandrov2.constructPolyhedron(graph, initRadiusFactor, endError, maxIterations, this, this);
					break;
				case SlowAndSafe:
					Alexandrov.constructPolyhedron(graph, initRadiusFactor, endError, maxIterations, this);
					break;
				case FastMultiCPU:
					AlexandrovMultiCPU.constructPolyhedron(graph, initRadiusFactor, endError, maxIterations, this, this);
					break;
				case Simple:
					AlexandrovSimple.constructPolyhedron(graph, initRadiusFactor, endError, maxIterations, this);
					break;
			}
			controller.setCalculationRemote(null);
			double sec = (System.currentTimeMillis() - time) / 1000.0;
			if (stop == true){
				controller.setStatus("Iterrupted: " + iteration + " Iterations, " + sec + " Seconds");
				return;
			} else 
				controller.setStatus("Polytop successfully constructed in " + iteration + " Iterations, " + sec + " Seconds");
		} catch (Exception e) {
			controller.setStatus(e.getMessage());
			controller.setCalculationRemote(null);
			return;
		}
		viewer.updateGeometry(graph);
		viewer.encompass();
	}

	public Double getError() {
		return endError;
	}
	
	public void setError(Double endError){
		this.endError = endError;
	}
	

	public Integer getMaxIterations() {
		return maxIterations;
	}

	public void setMaxIterations(Integer maxIterations) {
		this.maxIterations = maxIterations;
	}
	
	public void setMethod(CalculationMethod method) {
		this.method = method;
	}
	
	public void setInitRadiusFactor(Double initRadiusFactor) {
		this.initRadiusFactor = initRadiusFactor;
	}
	public Double getInitRadiusFactor() {
		return initRadiusFactor;
	}
	
}
