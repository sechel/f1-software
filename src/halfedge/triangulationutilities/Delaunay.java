package halfedge.triangulationutilities;

import halfedge.Edge;
import halfedge.Face;
import halfedge.HalfEdgeDataStructure;
import halfedge.Vertex;
import halfedge.decorations.IsFlippable;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;


/**
 * Construct a delaunay triangulation from a given triangulation
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 */
public class Delaunay {

	
	
	public static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F> & IsFlippable,
		F extends Face<V, E, F>
	> Integer getNumFlips(HalfEdgeDataStructure<V, E, F> graph){
		Integer result = 0;
		for (E e : graph.getEdges())
			result += e.getFlipCount();
		return result;
	}
	
	public static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F> & IsFlippable,
		F extends Face<V, E, F>
	> Integer getNumEffectiveFlips(HalfEdgeDataStructure<V, E, F> graph){
		Integer result = 0;
		for (E e : graph.getEdges())
			if (e.getFlipCount() > 0 && (e.getFlipCount() % 2) != 0)
				result++;
		return result;
	}
	
	
	/**
	 * Calculates the angle between edge and edge.getNextEdge()
	 * Sets the angle property of edge to the result
	 * @param edge
	 * @return the angle at edge
	 * @throws TriangulationException
	 */
	public static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F> & IsFlippable,
		F extends Face<V, E, F>
	> double getAngle(E edge) throws TriangulationException{
		double a = edge.getLength();
		double b = edge.getNextEdge().getLength();
		double c = edge.getPreviousEdge().getLength();
		if ((a*a + b*b - c*c) / (2*a*b) > 1)
			throw new TriangulationException("Triangle inequation doesn't hold for " + edge);
		double result = Math.abs(StrictMath.acos((a*a + b*b - c*c) / (2*a*b)));
		return result;
	}
	
	
	/**
	 * Checks wether this edge is locally delaunay
	 * @param edge the edge to check
	 * @return the check result
	 * @throws TriangulationException
	 */
	public static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F> & IsFlippable,
		F extends Face<V, E, F>
	> boolean isDelaunay(E edge) throws TriangulationException{
		double gamma = getAngle(edge.getNextEdge());
		double delta = getAngle(edge.getOppositeEdge().getNextEdge());
		return gamma + delta <= Math.PI;
	}
	
	public static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F> & IsFlippable,
		F extends Face<V, E, F>
	> boolean isDelaunay(HalfEdgeDataStructure<V,E,F> graph) {
		for(E edge : graph.getEdges()){
			try{
				if(!isDelaunay(edge))
				return false;
			} catch (TriangulationException e) {
				return false;
			}
		}
		return true;
	}
	
	
	/**
	 * Returns the positive edges belonging to the kite of the edge 
	 */
	public static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F> & IsFlippable,
		F extends Face<V, E, F>
	> List<E> getPositiveKiteBorder(E edge){
		LinkedList<E> result = new LinkedList<E>();
		E e1 = edge.getNextEdge();
		E e2 = edge.getPreviousEdge();
		E e3 = edge.getOppositeEdge().getNextEdge();
		E e4 = edge.getOppositeEdge().getPreviousEdge();
		if (!e1.isPositive())
			e1 = e1.getOppositeEdge();
		if (!e2.isPositive())
			e2 = e2.getOppositeEdge();
		if (!e3.isPositive())
			e3 = e3.getOppositeEdge();
		if (!e4.isPositive())
			e4 = e4.getOppositeEdge();
		result.add(e1);
		result.add(e2);
		result.add(e3);
		result.add(e4);
		return result;
	}
	
	
	/**
	 * Constructs the delaunay triangulation of the given structure.
	 * @param graph must be a triangulation
	 * @throws TriangulationException if the given graph is no triangulation or 
	 * if the trangle inequation doesn't hold for some triangle
	 */
	public static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F> & IsFlippable,
		F extends Face<V, E, F>
	> void constructDelaunay(HalfEdgeDataStructure<V, E, F> graph) throws TriangulationException{
		if (!ConsistencyCheck.isTriangulation(graph))
			throw new TriangulationException("Graph is no triangulation!");
		
		HashSet<E> markSet = new HashSet<E>();
		Stack<E> stack = new Stack<E>();
		for (E positiveEdge : graph.getPositiveEdges()){
			markSet.add(positiveEdge);
			stack.push(positiveEdge);
		}
		while (!stack.isEmpty()){
			E ab = stack.pop();
			markSet.remove(ab);
			if (!isDelaunay(ab)){
				ab.flip();
				for (E xy : getPositiveKiteBorder(ab)){
					if (!markSet.contains(xy)){
						markSet.add(xy);
						stack.push(xy);
					}
				}
			}
		}
	}	
}
