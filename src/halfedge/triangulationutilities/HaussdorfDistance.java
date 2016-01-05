package halfedge.triangulationutilities;

import halfedge.Edge;
import halfedge.Face;
import halfedge.HalfEdgeDataStructure;
import halfedge.Vertex;
import halfedge.decorations.HasRadius;
import halfedge.decorations.HasXYZW;
import halfedge.decorations.IsFlippable;
import alexandrov.math.CPMCurvatureFunctional;

public class HaussdorfDistance {

	
	public static <
		V extends Vertex<V, E, F> & HasXYZW & HasRadius,
		E extends Edge<V, E, F> & IsFlippable,
		F extends Face<V, E, F>
	> double getHeight(F face, HalfEdgeDataStructure<V, E, F> graph) throws TriangulationException{
		E edgeij = face.getBoundaryEdge();
		double rj = edgeij.getTargetVertex().getRadius();
		double hij = rj * Math.sin(CPMCurvatureFunctional.getRho(edgeij));
		double alphaij = CPMCurvatureFunctional.getAlpha(edgeij);
		return hij * Math.sin(alphaij);
	}
	
	
	public static <
		V extends Vertex<V, E, F> & HasXYZW & HasRadius,
		E extends Edge<V, E, F> & IsFlippable,
		F extends Face<V, E, F>
	>  double getMaxRadius(HalfEdgeDataStructure<V, E, F> graph) throws TriangulationException{
		double max = 0.0;
		for (V v : graph.getVertices())
			max = max < v.getRadius() ? v.getRadius() : max;
		return max;
	}
	
	
	public static <
		V extends Vertex<V, E, F> & HasXYZW & HasRadius,
		E extends Edge<V, E, F> & IsFlippable,
		F extends Face<V, E, F>
	>  double getMinHeight(HalfEdgeDataStructure<V, E, F> graph) throws TriangulationException{
		double min = Double.MAX_VALUE;
		for (F f : graph.getFaces()){
			double height = getHeight(f, graph);
			min = min > height ? height : min;
		}
		return min;
	}
	
	
	public static <
		V extends Vertex<V, E, F> & HasXYZW & HasRadius,
		E extends Edge<V, E, F> & IsFlippable,
		F extends Face<V, E, F>
	>  double getDistanceToSphere(HalfEdgeDataStructure<V, E, F> graph) throws TriangulationException{
		double outterRadius = getMaxRadius(graph);
		double innerRadius = getMinHeight(graph);
		double sphereRadius = (outterRadius + innerRadius) / 2;
		return (outterRadius - sphereRadius) / sphereRadius;
	}
		
}
