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
import halfedge.decorations.IsBoundary;
import halfedge.decorations.IsFlippable;
import halfedge.triangulationutilities.Delaunay;
import halfedge.triangulationutilities.TriangulationException;

import java.util.LinkedList;
import java.util.List;

import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.Vector;

public class CapCurvatureFunctional {

	private static double
		eps = 1E-2;
	
	public static  <
		V extends Vertex<V, E, F> & HasXYZW & HasRadius,
		E extends Edge<V, E, F> & IsFlippable & IsBoundary,
		F extends Face<V, E, F>
	> List<V> getInnerVertices(HalfEdgeDataStructure<V, E, F> graph){
		LinkedList<V> list = new LinkedList<V>();
		for (V v : graph.getVertices())
			if (!isBorderVertex(v))
				list.add(v);
		return list;
	}
	
	
	private static double cot(double phi){
		return -tan(phi + PI/2);
	}
	
	
	public static  <
		V extends Vertex<V, E, F> & HasXYZW & HasRadius,
		E extends Edge<V, E, F> & IsFlippable & IsBoundary,
		F extends Face<V, E, F>
	> boolean isDegenerated(E edge_ki){
		try {
			if (edge_ki.getLeftFace() != null)
				return getOmega(edge_ki.getNextEdge()) > Math.PI - eps;
			else
				return getOmega(edge_ki.getOppositeEdge().getNextEdge()) > Math.PI - eps;
		} catch (TriangulationException e){
			return false;
		}
	}
	
	
	public static  <
		V extends Vertex<V, E, F> & HasXYZW & HasRadius,
		E extends Edge<V, E, F> & IsFlippable & IsBoundary,
		F extends Face<V, E, F>
	> boolean isFaceDegenerated(E edge) {
		if (isDegenerated(edge))
			return true;
		if (isDegenerated(edge.getNextEdge()))
			return true;
		if (isDegenerated(edge.getPreviousEdge()))
			return true;
		return false;
	}	
	
	
	public static  <
		V extends Vertex<V, E, F> & HasXYZW & HasRadius,
		E extends Edge<V, E, F> & IsFlippable & IsBoundary,
		F extends Face<V, E, F>
	> boolean isMetricConvex(HalfEdgeDataStructure<V, E, F> graph) throws TriangulationException{
		for (V v : graph.getVertices()){
			double gamma = getGammaAt(v);
			if (isBorderVertex(v)){
				if (gamma > PI + eps)
					return false;
			} else {
				if (gamma >= 2*PI + eps)
					return false;
			}
		}
		return true;
	}
	
	public static  <
		V extends Vertex<V, E, F> & HasXYZW & HasRadius,
		E extends Edge<V, E, F> & IsFlippable & IsBoundary,
		F extends Face<V, E, F>
	> double getGammaAt(V vertex) throws TriangulationException{
		List<E> cocycle = vertex.getEdgeStar();
		double gamma = 0.0;
		for (E e : cocycle){
			if (e.getLeftFace() != null)
				gamma += Delaunay.getAngle(e);
		}
		return gamma;
	}

	
	public static  <
		V extends Vertex<V, E, F> & HasXYZW & HasRadius,
		E extends Edge<V, E, F> & IsFlippable & IsBoundary,
		F extends Face<V, E, F>
	> boolean isLocallyConvex(E edge) throws TriangulationException{
		double theta = getTheta(edge);
		if (edge.isBoundary()){
			return theta <= PI/2 + eps;
		} else {
			return theta <= PI + eps;
		}
	}	
	
	public static  <
		V extends Vertex<V, E, F> & HasXYZW & HasRadius,
		E extends Edge<V, E, F> & IsFlippable & IsBoundary,
		F extends Face<V, E, F>
	> boolean isConvex(HalfEdgeDataStructure<V, E, F> graph) throws TriangulationException{
		for (E edge : graph.getEdges())
			if (!isLocallyConvex(edge))
				return false;
		return true;
	}
	
	
	public static <
		V extends Vertex<V, E, F> & HasXYZW & HasRadius,
		E extends Edge<V, E, F> & IsFlippable & IsBoundary,
		F extends Face<V, E, F>
	> Vector getCurvature(HalfEdgeDataStructure<V, E, F> graph) throws TriangulationException{
		List<V> innerVertices = getInnerVertices(graph);
		Vector result = new DenseVector(innerVertices.size());
		int index = 0;
		for (V v : innerVertices){
			result.set(index, getKappa(v, graph));
			index++;
		}
		return result;
	}

	public static <
		V extends Vertex<V, E, F> & HasXYZW & HasRadius,
		E extends Edge<V, E, F> & IsFlippable & IsBoundary,
		F extends Face<V, E, F>
	> Matrix getCurvatureDerivative(HalfEdgeDataStructure<V, E, F> graph) throws TriangulationException{
		List<V> innerVertices = getInnerVertices(graph);
		Matrix result = new DenseMatrix(innerVertices.size(), innerVertices.size());
		for (int i = 0; i < innerVertices.size(); i++){
			for (int j = 0; j < innerVertices.size(); j++){
				int realI = innerVertices.get(i).getIndex();
				int realJ = innerVertices.get(j).getIndex();
				result.set(i, j, getCurvaturePartialDerivative(graph, realI, realJ));
			}
		}
		return result;
	}
	
	
	/**
	 * Returns the partial derivative of kappa_i with respect to the height j
	 * @param graph
	 * @param i
	 * @param j
	 * @throws TriangulationException
	 */
	protected static <
		V extends Vertex<V, E, F> & HasXYZW & HasRadius,
		E extends Edge<V, E, F> & IsFlippable & IsBoundary,
		F extends Face<V, E, F>
	> double getCurvaturePartialDerivative(HalfEdgeDataStructure<V, E, F> graph, int i, int j) throws TriangulationException{
		V b = graph.getVertex(i);
		List<E> cocycle = b.getEdgeStar();
		double result = 0.0;
		for (E e : cocycle){
			V a = e.getStartVertex();
			double alpha_e1 = getAlpha(e);
			double alpha_e2 = getAlpha(e.getOppositeEdge());
			double rho_e = getRho(e);
			double dkdrho = (cot(alpha_e1) + cot(alpha_e2)) / sin(rho_e);
			double drhodh = 0.0;
			if (a.getIndex() == j && j != b.getIndex()){
				drhodh = 1 / (e.getLength() * sin(rho_e));
			} else if (a.getIndex() != j && j == b.getIndex()){
				drhodh = -1 / (e.getLength() * sin(rho_e));
			} else {
				drhodh = 0.0;
			}
			result += dkdrho * drhodh;	
		}
		
		return result;
	}
	
	
	public static  <
		V extends Vertex<V, E, F> & HasXYZW & HasRadius,
		E extends Edge<V, E, F> & IsFlippable & IsBoundary,
		F extends Face<V, E, F>
	> double getKappa(V vertex, HalfEdgeDataStructure<V, E, F> graph) throws TriangulationException{
		List<E> cocycle = vertex.getEdgeStar();
		double omega_i = 0.0; 
		for (E e : cocycle)
			omega_i += getOmega(e);
		return 2*PI - omega_i;
	}
	
	
	
	public static  <
		V extends Vertex<V, E, F> & HasXYZW & HasRadius,
		E extends Edge<V, E, F> & IsFlippable & IsBoundary,
		F extends Face<V, E, F>
	> double getFunctional(HalfEdgeDataStructure<V, E, F> graph) throws TriangulationException{
		double result = 0.0;
		// internal vertices
		for (V v : graph.getVertices()){
			if (isBorderVertex(v))
				continue;
			result += v.getRadius() * getKappa(v, graph);
		}
		// edges
		for (E e : graph.getPositiveEdges()){
			result += e.getLength() * (PI - getTheta(e));
		}
		return result;
	}

	
	public static  <
		V extends Vertex<V, E, F> & HasXYZW & HasRadius,
		E extends Edge<V, E, F> & IsFlippable & IsBoundary,
		F extends Face<V, E, F>
	> double getTheta(E edge) throws TriangulationException{
		if (!edge.isBoundary())
			return getAlpha(edge) + getAlpha(edge.getOppositeEdge());
		else {
			if (edge.getLeftFace() != null)
				return getAlpha(edge) - PI/2;
			else
				return getAlpha(edge.getOppositeEdge()) - PI/2;
		}
	}
	
	
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
		if (cosAlpha > 1 + eps)
			throw new TriangulationException("Triangle inequation doesn't hold pyramide side at edge " + edge);
		return acos(cosAlpha);
	}
	
	
	protected static  <
		V extends Vertex<V, E, F> & HasXYZW & HasRadius,
		E extends Edge<V, E, F> & IsFlippable,
		F extends Face<V, E, F>
	> double getRho(E edge) throws TriangulationException{
		double ri = edge.getTargetVertex().getRadius();
		double rj = edge.getStartVertex().getRadius();
		double lij = edge.getLength();
		double cosRho = (ri - rj) / lij;
		if (cosRho > 1 + eps){
			throw new TriangulationException("Triangle inequation doesn't hold pyramide side at edge " + edge);
		}
		return acos(cosRho);
	}
	
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
		if (cosOmega > 1 + eps)
			throw new TriangulationException("Triangle inequation doesn't hold pyramide side at edge " + edge_ki);
		return acos(cosOmega);
	}
	
	
	public static  <
		V extends Vertex<V, E, F> & HasXYZW & HasRadius,
		E extends Edge<V, E, F> & IsFlippable & IsBoundary,
		F extends Face<V, E, F>
	> boolean isBorderVertex(V vertex){
		List<E> cocycle = vertex.getEdgeStar();
		for (E e : cocycle)
			if (e.isBoundary())
				return true;
		return false;
	}
	
	
	
}
