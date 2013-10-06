package alexandrov.math;

import halfedge.Edge;
import halfedge.Face;
import halfedge.HalfEdgeDataStructure;
import halfedge.Vertex;
import halfedge.decorations.HasRadius;
import halfedge.decorations.HasXYZW;
import halfedge.decorations.IsFlippable;
import halfedge.triangulationutilities.TriangulationException;
import math.optimization.FunctionNotDefinedException;
import math.optimization.Linearizable;
import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.Vector;


/**
 * An implementation of the Linearizable interface for use with the 
 * Solver in math.optimization.
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 * @see math.optimization.Linearizable
 * @see math.optimization.newton.NewtonSolver
 */
public class CPMLinearizable
<
	V extends Vertex<V, E, F> & HasXYZW & HasRadius,
	E extends Edge<V, E, F> & IsFlippable,
	F extends Face<V, E, F>
> implements Linearizable {

	private HalfEdgeDataStructure<V, E, F>
		graph = null;
	
	
	public CPMLinearizable(HalfEdgeDataStructure<V, E, F> graph){
		this.graph = graph;
	}
	
	
	@Override
	public void evaluate(Vector x, Vector fx, Vector offset) throws FunctionNotDefinedException{
		for (int i = 0; i < graph.getNumVertices(); i++)
			graph.getVertex(i).setRadius(x.get(i));
		try {
			fx.set(CPMCurvatureFunctional.getCurvature(graph).add(-1, offset));
		} catch (TriangulationException e) {
			throw new FunctionNotDefinedException(e.getMessage());
		}
	}

	@Override
	public void evaluate(Vector x, Vector fx, Vector offset, Matrix jacobian) throws FunctionNotDefinedException{
		evaluate(x, fx, offset);
		try {
			jacobian.set(CPMCurvatureFunctional.getCurvatureDerivative(graph));
		} catch (TriangulationException e) {
			throw new FunctionNotDefinedException(e.getMessage());
		}
	}

	@Override
	public void evaluate(Vector x, Matrix jacobian) throws FunctionNotDefinedException{
		for (int i = 0; i < graph.getNumVertices(); i++)
			graph.getVertex(i).setRadius(x.get(i));
		try {
			jacobian.set(CPMCurvatureFunctional.getCurvatureDerivative(graph));
		} catch (TriangulationException e) {
			throw new FunctionNotDefinedException(e.getMessage());
		}
	}

	@Override
	public Integer getDomainDimension() {
		return graph.getNumVertices();
	}

	@Override
	public Integer getCoDomainDimension() {
		return graph.getNumVertices();
	}

}
