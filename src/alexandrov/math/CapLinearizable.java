package alexandrov.math;

import halfedge.Edge;
import halfedge.Face;
import halfedge.HalfEdgeDataStructure;
import halfedge.Vertex;
import halfedge.decorations.HasRadius;
import halfedge.decorations.HasXYZW;
import halfedge.decorations.IsBoundary;
import halfedge.decorations.IsFlippable;
import halfedge.triangulationutilities.TriangulationException;

import java.util.List;

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
public class CapLinearizable
<
	V extends Vertex<V, E, F> & HasXYZW & HasRadius,
	E extends Edge<V, E, F> & IsFlippable & IsBoundary,
	F extends Face<V, E, F>
> implements Linearizable {

	private HalfEdgeDataStructure<V, E, F>
		graph = null;
	private List<V>
		innerVertices = null;
	
	
	public CapLinearizable(HalfEdgeDataStructure<V, E, F> graph){
		this.graph = graph;
		innerVertices = CapCurvatureFunctional.getInnerVertices(graph);
	}
	
	
	@Override
	public void evaluate(Vector x, Vector fx, Vector offset) throws FunctionNotDefinedException{
		for (int i = 0; i < innerVertices.size(); i++)
			innerVertices.get(i).setRadius(x.get(i));
		try {
			fx.set(CapCurvatureFunctional.getCurvature(graph).add(-1, offset));
		} catch (TriangulationException e) {
			throw new FunctionNotDefinedException(e.getMessage());
		}
	}

	@Override
	public void evaluate(Vector x, Vector fx, Vector offset, Matrix jacobian) throws FunctionNotDefinedException{
		evaluate(x, fx, offset);
		try {
			jacobian.set(CapCurvatureFunctional.getCurvatureDerivative(graph));
		} catch (TriangulationException e) {
			throw new FunctionNotDefinedException(e.getMessage());
		}
	}

	@Override
	public void evaluate(Vector x, Matrix jacobian) throws FunctionNotDefinedException{
		for (int i = 0; i < innerVertices.size(); i++)
			innerVertices.get(i).setRadius(x.get(i));
		try {
			jacobian.set(CapCurvatureFunctional.getCurvatureDerivative(graph));
		} catch (TriangulationException e) {
			throw new FunctionNotDefinedException(e.getMessage());
		}
	}

	@Override
	public Integer getDomainDimension() {
		return innerVertices.size();
	}

	@Override
	public Integer getCoDomainDimension() {
		return innerVertices.size();
	}

}
