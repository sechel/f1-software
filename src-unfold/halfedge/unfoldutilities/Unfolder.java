package halfedge.unfoldutilities;

import halfedge.Edge;
import halfedge.Face;
import halfedge.GraphOperations;
import halfedge.HalfEdgeDataStructure;
import halfedge.HalfEdgeUtility;
import halfedge.Vertex;
import halfedge.decorations.HasAngle;
import halfedge.decorations.HasCurvature;
import halfedge.decorations.HasLength;
import halfedge.decorations.HasRadius;
import halfedge.decorations.HasXY;
import halfedge.decorations.HasXYZW;
import halfedge.decorations.IsBoundary;
import halfedge.decorations.IsFlippable;
import halfedge.decorations.IsHidable;
import halfedge.triangulationutilities.ConsistencyCheck;
import halfedge.triangulationutilities.DelaunayUtilities;
import halfedge.triangulationutilities.TriangulationException;

import java.util.HashMap;
import java.util.Random;
import java.util.TreeMap;

import javax.vecmath.Point4d;
import javax.vecmath.Vector4d;

import sun.security.x509.AlgIdDSA;


public class Unfolder {
	
	private static String[] algorithmNames = {"Random subtree", "Index subtree", "Star subtree (safe)", "Star subtree", "Crab subtree"};
	

	public static <
		V extends Vertex<V, E, F> & HasXY & HasXYZW,
		E extends Edge<V, E, F> & IsFlippable & IsHidable,
		F extends Face<V, E, F>
	> HalfEdgeDataStructure<V, E, F> constructVoronoi(HalfEdgeDataStructure<V, E, F> graph) throws TriangulationException{

	
		return constructVoronoi(graph, new HashMap<F,V>(), new HashMap<E,E>());
	
	}
	

	public static <
		V extends Vertex<V, E, F> & HasXY & HasXYZW,
		E extends Edge<V, E, F> & IsFlippable & IsHidable,
		F extends Face<V, E, F>
	> HalfEdgeDataStructure<V, E, F> constructVoronoi(HalfEdgeDataStructure<V, E, F> graph, HashMap<F,V> dualMap) throws TriangulationException{
	
		return constructVoronoi(graph, dualMap, new HashMap<E,E>());

	}
	
	/**
	 * Returns the corresponding Voronoi graph of the Delaunay triangulation of the graph
	 * @param preferably a delaunay triangulated graph
	 * @return Voronoi graph
	 * @throws TriangulationException
	 * @author Kristoffer Josefsson
	 */
	public static <
		V extends Vertex<V, E, F> & HasXY & HasXYZW,
		E extends Edge<V, E, F> & IsFlippable & IsHidable,
		F extends Face<V, E, F>
	> HalfEdgeDataStructure<V, E, F> constructVoronoi(HalfEdgeDataStructure<V, E, F> graph, HashMap<F,V> dualMap, HashMap<E,E> edgeToEdgeMap) throws TriangulationException{

		
		HalfEdgeDataStructure<V, E, F> dual =  HalfEdgeUtility.getDual(graph, dualMap, edgeToEdgeMap);
//		System.err.println("dual vor" + dualMap);
			
		// create a new graph
//		HalfEdgeDataStructure<V, E, F> voronoi = HalfEdgeDataStructure.createHEDS(graph.getVertexClass(), graph.getEdgeClass(), graph.getFaceClass());		

		for(F f : graph.getFaces()){

			E edge = f.getBoundaryEdge();
			
//			V v = voronoi.addNewVertex();

			V v = dualMap.get(f);
			v.setXYZW(DelaunayUtilities.getCircumcenter4d(graph, edge));
			v.setXY(DelaunayUtilities.getCircumcenter2d(graph, edge));

		}
		
//		// connect new edges
//		for(E e : dual.getEdges()){
//			E e1 = voronoi.addNewEdge();
//			E e2 = voronoi.addNewEdge();
//			
//			V v1 = e.getStartVertex();
//			V v2 = e.getTargetVertex();
//			e1.setTargetVertex(voronoi.getVertex(v1.getIndex()));
//			e2.setTargetVertex(voronoi.getVertex(v2.getIndex()));
//			
//			e1.linkOppositeEdge(e2);
//			e2.linkOppositeEdge(e1);
//		}
		
//		return voronoi;
		return dual;
		
	}
	
	
	public static <
		V extends Vertex<V, E, F> & HasXY & HasXYZW & HasRadius & HasCurvature,
		E extends Edge<V, E, F> & IsFlippable & IsHidable & IsBoundary & HasLength & HasAngle,
		F extends Face<V, E, F>
	> HalfEdgeDataStructure<V, E, F> getUnfolding(HalfEdgeDataStructure<V, E, F> graph, V source, String alg) throws TriangulationException{
		if (!ConsistencyCheck.isTriangulation(graph))
			throw new TriangulationException("Graph is no triangulation!");
		
			if(alg == algorithmNames[0])
				return getRandomUnfolding(graph, source);
			if(alg == algorithmNames[1])
				return getIndexUnfolding(graph, source);
			if(alg == algorithmNames[2])
				return getStarUnfolding(graph, source, true);
			if(alg == algorithmNames[3])
				return getStarUnfolding(graph, source, false);
			if(alg == algorithmNames[4])
				return getCrabUnfolding(graph, source);
			else
				return null;
	
	}
	
	public static <
		V extends Vertex<V, E, F> & HasXY & HasXYZW & HasRadius & HasCurvature,
		E extends Edge<V, E, F> & IsFlippable & IsHidable & IsBoundary & HasLength,
		F extends Face<V, E, F>
	> F getBottom(HalfEdgeDataStructure<V,E,F> graph, V source) {
		F bottomFace = null;
		try {
			// find a fold base
			// pick by largest diameter
			Point4d top = source.getXYZW();
			TreeMap<Double, F> sortMap = new TreeMap<Double, F>();
			for(F f : graph.getFaces()) {
				Vector4d bottom = new Vector4d(DelaunayUtilities.getCircumcenter4d(graph, f.getBoundaryEdge()));
				bottom.sub(top);
				sortMap.put(bottom.lengthSquared(), f);
			}
			
			bottomFace = sortMap.get(sortMap.lastKey());
			
		} catch (TriangulationException e) {
			System.err.println(e.getMessage());
		}
		
		return bottomFace;
	}
	
	public static String[] getAlgorithmNames() {
		return algorithmNames;
	}
	
	public static String getDefaultAlgorithm() {
		return algorithmNames[0];
	}
	
	public static <
		V extends Vertex<V, E, F> & HasXY & HasXYZW & HasRadius & HasCurvature,
		E extends Edge<V, E, F> & IsFlippable & IsHidable & IsBoundary & HasLength & HasAngle,
		F extends Face<V, E, F>
	> HalfEdgeDataStructure<V, E, F> getStarUnfolding(HalfEdgeDataStructure<V, E, F> graph, V source, boolean safe) throws TriangulationException{
	
//		if (!ConsistencyCheck.isTriangulation(graph))
//			throw new TriangulationException("Graph is no triangulation!");
	
		HalfEdgeDataStructure<V,E,F> voronoi = StarUnfoldNet.getStarUnfoldNet(graph, source, safe);

		return voronoi;

	}
	
	public static <
		V extends Vertex<V, E, F> & HasXY & HasXYZW & HasRadius & HasCurvature,
		E extends Edge<V, E, F> & IsFlippable & IsHidable & IsBoundary & HasLength & HasAngle,
		F extends Face<V, E, F>
	> HalfEdgeDataStructure<V, E, F> getCrabUnfolding(HalfEdgeDataStructure<V, E, F> graph, V source) throws TriangulationException{

		CrabUnfolder<V,E,F> cuf = new CrabUnfolder<V, E, F>();
		HalfEdgeDataStructure<V,E,F> voronoi = cuf.getCrabUnfoldNet(graph, source);
		return voronoi;
	}
	
	
	public static <
		V extends Vertex<V, E, F> & HasXY & HasXYZW,
		E extends Edge<V, E, F> & IsFlippable & IsHidable,
		F extends Face<V, E, F>
	> HalfEdgeDataStructure<V, E, F> getIndexUnfolding(HalfEdgeDataStructure<V, E, F> graph, V source) throws TriangulationException{
	
		if (!ConsistencyCheck.isTriangulation(graph))
			throw new TriangulationException("Graph is no triangulation!");
	
		HalfEdgeDataStructure<V, E, F> voronoi = constructVoronoi(graph);
		
		Random rnd = new Random();
		
		// distribute weights
		double i = 1.0;
		for(E e : voronoi.getEdges()){
			e.setLength(i);
			i = i + 1.0;
		}
		
		// we choose a random start vertex
		int numVertices = voronoi.getVertices().size();
		V start = voronoi.getVertex(rnd.nextInt(numVertices));
		
		return GraphOperations.getSubtreePrim(voronoi, start);

	}
	
	public static <
		V extends Vertex<V, E, F> & HasXY & HasXYZW,
		E extends Edge<V, E, F> & IsFlippable & IsHidable,
		F extends Face<V, E, F>
	> HalfEdgeDataStructure<V, E, F> getRandomUnfolding(HalfEdgeDataStructure<V, E, F> graph, V source) throws TriangulationException{
		if (!ConsistencyCheck.isTriangulation(graph))
			throw new TriangulationException("Graph is no triangulation!");
		
		HalfEdgeDataStructure<V, E, F> voronoi = constructVoronoi(graph);
		
		Random rnd = new Random();
		
		// distribute weights
		for(E e : voronoi.getEdges()){
			e.setLength(rnd.nextDouble());
		}
		
		// we choose a random start vertex
		int numVertices = voronoi.getVertices().size();
		V start = voronoi.getVertex(rnd.nextInt(numVertices));
		
		return GraphOperations.getSubtreePrim(voronoi, start);
	
	}
}
