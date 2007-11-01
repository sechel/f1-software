package teamgeist.calculation;

import halfedge.HalfEdgeDataStructure;
import halfedge.triangulationutilities.ConsistencyCheck;
import halfedge.triangulationutilities.HaussdorfDistance;
import halfedge.triangulationutilities.TriangulationException;

import java.util.LinkedList;
import java.util.Random;

import math.optimization.IterationMonitor;
import teamgeist.combinatorics.EdgeLengthMap;
import teamgeist.frontend.controller.MainController;
import util.debug.DBGTracer;
import alexandrov.Alexandrov2;
import alexandrov.graph.CPMEdge;
import alexandrov.graph.CPMFace;
import alexandrov.graph.CPMVertex;
import alexandrov.math.CPMCurvatureFunctional;

public class CalculateTeamgeistThread implements IterationMonitor, Runnable {

	private MainController
		controller = null;
	private Double
		endError = 1E-10,
		startError = 0.0;
	private Integer
		progress = 0,
		maxIterations = 100,
		iteration = 0;
	private HalfEdgeDataStructure<CPMVertex, CPMEdge, CPMFace>
		graph = null;
	private Random 
		rnd = new Random();
	private EdgeLengthMap
		lengthMap = null;
	private boolean 
		stop = false;
	private LinkedList<ResultListener>
		resultListeners = new LinkedList<ResultListener>();
	
	
	public static interface ResultListener{
		
		public void success(HalfEdgeDataStructure<CPMVertex, CPMEdge, CPMFace> result);
		
		public void error(String message);
		
	}
	
	public void addResultListener(ResultListener l){
		resultListeners.add(l);
	}
	
	public boolean removeresultListener(ResultListener l){
		return resultListeners.remove(l);
	}
	
	public void removeAllResultListeners(){
		resultListeners.clear();
	}
	
	private void fireResultSuccess(HalfEdgeDataStructure<CPMVertex, CPMEdge, CPMFace> result){
		for (ResultListener l : resultListeners)
			l.success(result);
	}
	
	private void fireResultError(String message){
		for (ResultListener l : resultListeners)
			l.error(message);
	}
	
	public CalculateTeamgeistThread(MainController controller, EdgeLengthMap lengthMap){
		this.controller = controller;
		this.lengthMap = lengthMap;
		rnd.setSeed(123568765785L);
	}
	

	public void setIteration(Integer iteration, Double error) {
		this.iteration = iteration;
		this.progress = (int)(100 * (startError - error) / (startError - endError));
		controller.setStatus("Calculation: " + progress + "%, Iteration: " + iteration);
		Thread.yield();
	}

	public void done(Double error){
		
	}
	
	public void start(Double error){
		startError = error;
	}
	
	public void setRandomSeed(long seed){
		rnd.setSeed(seed);
	}
	
	
	/**
	 * Start a new calculation thread with the given graph
	 * @param graph the graph to be calculated
	 */
	public void calculatePolyhedron(HalfEdgeDataStructure<CPMVertex, CPMEdge, CPMFace> graph){
		this.graph = graph;
		new Thread(this, "Calculation Thread").start();
	}
	
	
	public void run() {
		stop = false;
		// randomize lengths
		while (true){
			try {
				DBGTracer.msg("Finding valid edge lengths for teamgeist");
				int c = 0;
				do{
					c++;
					if (c < 100)
						DBGTracer.print(".");
					else {
						c = 0;
						DBGTracer.println(".");
					}
					lengthMap.randomize();
					for (CPMEdge e : graph.getEdges())
						e.setLength(lengthMap.getLength(e.getIndex()));
				} while (!ConsistencyCheck.checkEdgeLengths(graph) || !CPMCurvatureFunctional.isMetricConvex(graph));
			} catch (TriangulationException e) {
				e.printStackTrace();
				continue;
			}
			
			HalfEdgeDataStructure<CPMVertex, CPMEdge, CPMFace> newGraph = new HalfEdgeDataStructure<CPMVertex, CPMEdge, CPMFace>(graph);
			try {
				long time = System.currentTimeMillis();
				Alexandrov2.constructPolyhedron(newGraph, endError, maxIterations, this, null);
				Double sphereDistance = HaussdorfDistance.getDistanceToSphere(newGraph);
				double sec = (System.currentTimeMillis() - time) / 1000.0;
				if (stop == true){
					String message = "Iterrupted: " + iteration + " Iterations, " + sec + " Seconds";
					controller.setStatus(message);
					fireResultError(message);
					return;
				} else {
//					if (hasBeenFlipped(newGraph)){
//						controller.setStatus("Wrong combinatorics: still trying...");
//						continue;
//					} else {
						controller.setStatus("Teamgeist successfully constructed in " + iteration + " Iterations, " + sec + " Seconds, Haussdorf Distance: " + sphereDistance);
//					}
				}
			} catch (Exception e) {
				controller.setStatus(e.getMessage());
				fireResultError(e.getMessage());
				continue;
			}
			if (resultListeners.isEmpty()){
				controller.getViewer().viewTeamgeist(newGraph);
				controller.getViewer().encompass();
			} else {
				fireResultSuccess(newGraph);
			}
			break;
		}
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
	
}
