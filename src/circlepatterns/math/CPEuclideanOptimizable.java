package circlepatterns.math;

import halfedge.Edge;
import halfedge.Face;
import halfedge.HalfEdgeDataStructure;
import halfedge.Vertex;
import halfedge.decorations.HasGradientValue;
import halfedge.decorations.HasRadius;
import halfedge.decorations.HasRho;
import halfedge.decorations.HasTheta;
import halfedge.decorations.HasXY;
import math.optimization.Optimizable;
import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.Vector;

/**
 * An implementation of math.optimization.Optimizable. This implements 
 * the hessian and the gradient of the CPEuclideanFunctional
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 * @see circlepatterns.math.CPEuclideanFunctional
 * @see math.optimization.Optimizable
 */
public class CPEuclideanOptimizable <
	V extends Vertex<V, E, F> & HasXY,
	E extends Edge<V, E, F> & HasTheta,
	F extends Face<V, E, F> & HasRho & HasXY & HasRadius & HasGradientValue
> implements Optimizable {

	
	private HalfEdgeDataStructure<V, E, F> 
		graph = null;
	
	
	public CPEuclideanOptimizable(HalfEdgeDataStructure<V, E, F> graph) {
		this.graph = graph;
	}
		
		   
    private void updateRhos(Vector rhos){
    	graph.getFace(0).setRho(0.0);
		for (Integer i = 0; i < rhos.size(); i++)
			graph.getFace(i + 1).setRho(rhos.get(i));
    }
    

    private void fillGradient(Vector grad){
		for (Integer i = 0; i < grad.size(); i++)
			grad.set(i, graph.getFace(i + 1).getGradientValue());
    }
    
    
    private void fillHessian(Matrix hessian){
		hessian.zero();
	    for (E edge : graph.getPositiveEdges()) {
			if (!edge.isInteriorEdge())
				continue;
			F jFace = edge.getLeftFace();
			F kFace = edge.getOppositeEdge().getLeftFace();
			Integer j = jFace.getIndex();
			Integer k = kFace.getIndex();
			
			Double rhoDiff = kFace.getRho() - jFace.getRho();
			Double hjk = Math.sin(edge.getTheta())
					/ (Math.cosh(rhoDiff) - Math.cos(edge.getTheta()));

			if (j > 0)
				hessian.add(j - 1, j - 1, hjk);
			if (k > 0)
				hessian.add(k - 1, k - 1, hjk);
			if (k > 0 && j > 0){
				hessian.add(j - 1, k - 1, -hjk);
				hessian.add(k - 1, j - 1, -hjk);
			}
		}
    }
    
    
	public Double evaluate(Vector x, Vector gradient, Matrix hessian) {
		// set the domain value
		updateRhos(x);
		// the value
		Double result = CPEuclideanFunctional.evaluate(graph);
		// the gradient
		fillGradient(gradient);
		// the hessian
		fillHessian(hessian);
		return result;
	}
	
	public Double evaluate(Vector x, Vector gradient){
		// set the domain value
		updateRhos(x);
		// the value
		Double result = CPEuclideanFunctional.evaluate(graph);
		// the gradient
		fillGradient(gradient);
		return result;
	}
	
	public Double evaluate(Vector x, Matrix hessian){
		// set the domain value
		updateRhos(x);
		// the value
		Double result = CPEuclideanFunctional.evaluate(graph);
		// the hessian
		fillHessian(hessian);
		return result;
	}
	
	public Double evaluate(Vector x){
		// set the domain value
		updateRhos(x);
		// the value
		return CPEuclideanFunctional.evaluate(graph);
	}

	
	public Integer getDomainDimension() {
		return graph.getNumFaces() - 1;
	}

}
