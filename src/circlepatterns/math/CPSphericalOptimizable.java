package circlepatterns.math;

import static java.lang.Double.NEGATIVE_INFINITY;
import static java.lang.Double.POSITIVE_INFINITY;
import static java.lang.Math.cos;
import static java.lang.Math.cosh;
import static java.lang.Math.sin;
import halfedge.Edge;
import halfedge.Face;
import halfedge.HalfEdgeDataStructure;
import halfedge.Vertex;
import halfedge.decorations.HasCapitalPhi;
import halfedge.decorations.HasGradientValue;
import halfedge.decorations.HasRho;
import halfedge.decorations.HasTheta;
import math.optimization.Optimizable;
import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.Vector;
import util.debug.DBGTracer;
import circlepatterns.math.CPSphericalFunctional.FlagFalse;
import circlepatterns.math.CPSphericalFunctional.SpecialEdgeFlag;

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
public class CPSphericalOptimizable <
	V extends Vertex<V, E, F>,
	E extends Edge<V, E, F> & HasTheta,
	F extends Face<V, E, F> & HasRho &  HasGradientValue & HasCapitalPhi
> implements Optimizable {

	
	private HalfEdgeDataStructure<V, E, F> 
		graph = null;
	private SpecialEdgeFlag
		edgeFlag = new FlagFalse();
	
	
	public CPSphericalOptimizable(HalfEdgeDataStructure<V, E, F> graph, SpecialEdgeFlag edgeFlag) {
		this.graph = graph;
		this.edgeFlag = edgeFlag;
	}
		
		   
    private void updateRhos(Vector rhos){
    	graph.getFace(0).setRho(0.0);
		for (Integer i = 0; i < rhos.size(); i++)
			graph.getFace(i).setRho(rhos.get(i));
    }
    

    private void fillGradient(Vector grad){
		for (Integer i = 0; i < grad.size(); i++)
			grad.set(i, graph.getFace(i).getGradientValue());
    }
    
    
    private void fillHessian(Matrix hessian){
		hessian.zero();
	    for (E edge : graph.getPositiveEdges()) {
			F jFace = edge.getLeftFace();
			F kFace = edge.getRightFace();
			
			Double jRho = jFace == null ? 0.0 : jFace.getRho();
			Double kRho = kFace == null ? 0.0 : kFace.getRho();
			Double theta = edge.getTheta();
			Double c1 = sin(theta) / (cosh(kRho - jRho) - cos(theta));
			Double c2 = -sin(theta) / (cosh(jRho + kRho) + cos(theta));

			Double lambda = edgeFlag.specialEdge(edge) ? 0.5 : 1.0;
			c1 *= lambda;
			c2 *= lambda;
			
			if (c1.equals(POSITIVE_INFINITY) || c1.equals(NEGATIVE_INFINITY) || c2.equals(POSITIVE_INFINITY) || c2.equals(NEGATIVE_INFINITY))
				DBGTracer.msg("infinity in hessian!");
			
			Integer j = jFace == null ? -1 : jFace.getIndex();
			Integer k = kFace == null ? -1 : kFace.getIndex();
			if (j != -1) {
				hessian.add(j, j, c1);
				hessian.add(j, j, c2);
			}
			if (k != -1) {
				hessian.add(k, k, c1);
				hessian.add(k, k, c2);
			}
			if (j != -1 && k != -1) {
				hessian.add(j, k, -c1);
				hessian.add(k, j, -c1);
				hessian.add(j, k, c2);
				hessian.add(k, j, c2);	
			}
		}
    }
    
    
	public Double evaluate(Vector x, Vector gradient, Matrix hessian) {
		// set the domain value
		updateRhos(x);
		// the value
		Double result = CPSphericalFunctional.evaluate(graph, edgeFlag);
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
		Double result = CPSphericalFunctional.evaluate(graph, edgeFlag);
		// the gradient
		fillGradient(gradient);
		return result;
	}
	
	public Double evaluate(Vector x, Matrix hessian){
		// set the domain value
		updateRhos(x);
		// the value
		Double result = CPSphericalFunctional.evaluate(graph, edgeFlag);
		// the hessian
		fillHessian(hessian);
		return result;
	}
	
	public Double evaluate(Vector x){
		// set the domain value
		updateRhos(x);
		// the value
		return CPSphericalFunctional.evaluate(graph, edgeFlag);
	}

	
	public Integer getDomainDimension() {
		return graph.getNumFaces();
	}

}
