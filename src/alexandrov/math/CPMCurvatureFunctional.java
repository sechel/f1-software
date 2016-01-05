package alexandrov.math;

import static java.lang.Math.PI;
import static java.lang.Math.acos;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.tan;
import halfedge.Edge;
import halfedge.Face;
import halfedge.HalfEdgeDataStructure;
import halfedge.Vertex;
import halfedge.decorations.HasRadius;
import halfedge.decorations.HasXYZW;
import halfedge.decorations.IsFlippable;
import halfedge.triangulationutilities.Delaunay;
import halfedge.triangulationutilities.TriangulationException;

import java.util.LinkedList;
import java.util.List;

import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.Vector;



/**
 * A discrete curvature functional and its derivative
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 * @see alexandrov.math.CPMLinearizable
 */
public class CPMCurvatureFunctional {

	private static final double
		eps = 1E-5;
	
	private static double cot(double phi){
		return -tan(phi + PI/2);
	}


	public static  <
		V extends Vertex<V, E, F> & HasXYZW & HasRadius,
		E extends Edge<V, E, F> & IsFlippable,
		F extends Face<V, E, F>
	> boolean isMetricConvex(HalfEdgeDataStructure<V, E, F> graph) throws TriangulationException{
		for (V v : graph.getVertices()){
			double gamma = getGammaAt(v);
			if (gamma >= 2*PI + eps){
				return false;
			}
		}
		return true;
	}
	

	public static  <
		V extends Vertex<V, E, F> & HasXYZW & HasRadius,
		E extends Edge<V, E, F> & IsFlippable,
		F extends Face<V, E, F>
	> double getGammaAt(V vertex) throws TriangulationException{
		List<E> cocycle = vertex.getEdgeStar();
		double gamma = 0.0;
		for (E e : cocycle){
			gamma += Delaunay.getAngle(e);
		}
		return gamma;
	}

	
	public static  <
		V extends Vertex<V, E, F> & HasXYZW & HasRadius,
		E extends Edge<V, E, F> & IsFlippable,
		F extends Face<V, E, F>
	> Vector getGamma(HalfEdgeDataStructure<V, E, F> graph) throws TriangulationException{
		Vector result = new DenseVector(graph.getNumVertices());
		for (V v : graph.getVertices()){
			List<E> cocycle = v.getEdgeStar();
			double gamma = 0.0;
			for (E e : cocycle){
				gamma += Delaunay.getAngle(e);
			}
			result.set(v.getIndex(), gamma);
		}
		return result;
	}
	
	
	public static  <
		V extends Vertex<V, E, F> & HasXYZW & HasRadius,
		E extends Edge<V, E, F> & IsFlippable,
		F extends Face<V, E, F>
	> boolean isLocallyConvex(E edge) throws TriangulationException{
		return getAlpha(edge) + getAlpha(edge.getOppositeEdge()) <= PI + eps;
	}
	
	
	public static  <
		V extends Vertex<V, E, F> & HasXYZW & HasRadius,
		E extends Edge<V, E, F> & IsFlippable,
		F extends Face<V, E, F>
	> boolean isConvex(HalfEdgeDataStructure<V, E, F> graph) throws TriangulationException{
		for (E edge : graph.getEdges())
			if (!isLocallyConvex(edge))
				return false;
		return true;
	}

	
	
	public static <
		V extends Vertex<V, E, F> & HasXYZW & HasRadius,
		E extends Edge<V, E, F> & IsFlippable,
		F extends Face<V, E, F>	
	> Matrix getCurvatureDerivative(HalfEdgeDataStructure<V, E, F> graph) throws TriangulationException{
		Matrix result = new DenseMatrix(graph.getNumVertices(), graph.getNumVertices());
		for (int i = 0; i < graph.getNumVertices(); i++){
			for (int j = 0; j < graph.getNumVertices(); j++){
				result.set(i, j, getCurvaturePartialDerivative(graph, i, j));
			}
		}
		return result;
	}
	
	
	/**
	 * Returns the partial derivative of kappa_i with respect to the radus j
	 * @param graph
	 * @param i
	 * @param j
	 * @throws TriangulationException
	 */
	protected static <
		V extends Vertex<V, E, F> & HasXYZW & HasRadius,
		E extends Edge<V, E, F> & IsFlippable,
		F extends Face<V, E, F>
	> double getCurvaturePartialDerivative(HalfEdgeDataStructure<V, E, F> graph, int i, int j) throws TriangulationException{
		if (i == j){
			V v = graph.getVertex(i);
			List<E> cocycle = v.getEdgeStar();
			double result = 0.0;
			for (E ki : cocycle){
				int k = ki.getStartVertex().getIndex();
				double sum = 0.0;
				if (k == i){ //loop
					double sin_rho_e = sin(getRho(ki));
					double l = ki.getLength();
					double alphaE = getAlpha(ki);
					double alphaEMin = getAlpha(ki.getOppositeEdge());
					double ri = v.getRadius();
					sum =  l * (cot(alphaE) + cot(alphaEMin)) / (ri*ri * sin_rho_e*sin_rho_e) / 2;
				} else {    //no loop
					double phi_ij = getPhi(ki);
					sum = cos(phi_ij)*getCurvaturePartialDerivative(graph, i, k);
				}
				result += sum;
			}
			return -result;
		} else {
			//find edge ij
			List<E> jiList = new LinkedList<E>();
			for (E edge : graph.getEdges()){
				if (edge.getTargetVertex().getIndex() == i && edge.getStartVertex().getIndex() == j)
					jiList.add(edge);
			}
			if (jiList.size() == 0)
				return 0.0;
			double result = 0.0;
			for (E ji : jiList){
				E ij = ji.getOppositeEdge(); 
				double alpha_ij = getAlpha(ji);
				double alpha_ji = getAlpha(ij);
				double rho_ij = getRho(ji);
				double rho_ji = getRho(ij);
				double lij = ij.getLength();
				result += (cot(alpha_ij) + cot(alpha_ji)) / (lij * sin(rho_ij) * sin(rho_ji));
			}
			return result;
		}
	}
	
	
	
	public static <
		V extends Vertex<V, E, F> & HasXYZW & HasRadius,
		E extends Edge<V, E, F> & IsFlippable,
		F extends Face<V, E, F>
	> double getCurvature(V v) throws TriangulationException{
		List<E> cocycle = v.getEdgeStar();
		double omega_i = 0.0; 
		for (E e : cocycle)
			omega_i += getOmega(e);
		return 2*PI - omega_i;
	}	
	
	
	
	public static <
		V extends Vertex<V, E, F> & HasXYZW & HasRadius,
		E extends Edge<V, E, F> & IsFlippable,
		F extends Face<V, E, F>
	> Vector getCurvature(HalfEdgeDataStructure<V, E, F> graph) throws TriangulationException{
		Vector result = new DenseVector(graph.getNumVertices());
		for (V v : graph.getVertices())
			result.set(v.getIndex(), getCurvature(v));
		return result;
	}

	
	/**
	 * Returns Rho_ji if edge starts at i and ends at j
	 * @param edge the edge between i and j
	 * @return the angle between edge and the radius on j
	 */
	public static  <
		V extends Vertex<V, E, F> & HasXYZW & HasRadius,
		E extends Edge<V, E, F> & IsFlippable,
		F extends Face<V, E, F>
	> double getRho(E edge) throws TriangulationException{
		double ri = edge.getTargetVertex().getRadius();
		double rj = edge.getStartVertex().getRadius();
		double lij = edge.getLength();
		double cosRho = (lij*lij + ri*ri - rj*rj) / (2*lij*ri);
		if (cosRho > 1)
			throw new TriangulationException("Triangle inequation doesn't hold pyramide side at edge " + edge);
		return acos(cosRho);
	}
	
	/**
	 * Returns the angle between the two radii of i and j
	 * @param edge
	 * @return
	 */
	protected static  <
		V extends Vertex<V, E, F> & HasXYZW & HasRadius,
		E extends Edge<V, E, F> & IsFlippable,
		F extends Face<V, E, F>
	> double getPhi(E edge) throws TriangulationException{
		return PI - getRho(edge) - getRho(edge.getOppositeEdge());
	}
	
	/**
	 * Returns the angle between the triangle of edge and the triangle 
	 * of the radi of i and j and edge. 
	 * @param edge
	 * @return alpha
	 * @throws TriangulationException
	 */
	public static  <
		V extends Vertex<V, E, F> & HasXYZW & HasRadius,
		E extends Edge<V, E, F> & IsFlippable,
		F extends Face<V, E, F>
	> double getAlpha(E edge) throws TriangulationException{
		E edgeji = edge.getOppositeEdge();
		E edgeki = edge.getPreviousEdge();
		double gammajik = Delaunay.getAngle(edgeki);
		double rhoik = getRho(edgeki);
		double rhoij = getRho(edgeji);
		double cosAlpha = (cos(rhoik) - cos(gammajik)*cos(rhoij)) / (sin(gammajik)*sin(rhoij));
		if (cosAlpha > 1)
			throw new TriangulationException("Triangle inequation doesn't hold pyramide side at edge " + edge);
		return acos(cosAlpha);
	}
	
	
	/**
	 * Returns the angle omega
	 * @param edge_ki the edge from k to i
	 * @return
	 * @throws TriangulationException
	 */
	protected static  <
		V extends Vertex<V, E, F> & HasXYZW & HasRadius,
		E extends Edge<V, E, F> & IsFlippable,
		F extends Face<V, E, F>
	> double getOmega(E edge_ki) throws TriangulationException{
		E edge_ji = edge_ki.getNextEdge().getOppositeEdge();
		double gamma_jik = Delaunay.getAngle(edge_ki);
		double rho_ij = getRho(edge_ji);
		double rho_ik = getRho(edge_ki);
		double cosOmega = (cos(gamma_jik) - cos(rho_ij)*cos(rho_ik)) / (sin(rho_ij)*sin(rho_ik));
		if (cosOmega > 1)
			throw new TriangulationException("Triangle inequation doesn't hold pyramide side at edge " + edge_ki);
		return acos(cosOmega);
	}
	
}
