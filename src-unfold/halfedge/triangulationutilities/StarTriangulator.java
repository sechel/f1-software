package halfedge.triangulationutilities;

import halfedge.Edge;
import halfedge.Face;
import halfedge.HalfEdgeDataStructure;
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
import halfedge.surfaceutilities.EmbeddedEdge;
import halfedge.surfaceutilities.EmbeddedVertex;
import halfedge.surfaceutilities.UnfoldSubdivision;

import java.util.Collection;
import java.util.List;

import javax.vecmath.Point4d;

public class StarTriangulator {

	public static <
		V extends Vertex<V, E, F> & HasXYZW & HasRadius & HasCurvature & HasXY,
		E extends Edge<V, E, F> & IsBoundary & HasLength & IsFlippable &  IsHidable & HasAngle,
		F extends Face<V, E, F> 
	> void constructStarTriangulation(HalfEdgeDataStructure<V, E, F> graph, Collection<EmbeddedEdge<V,E,F>> paths) {
		
		
		EmbeddedEdge<V,E,F> path = null;
		EmbeddedEdge<V,E,F> testPath = null;
		
		EmbeddedVertex<V,E,F> endVertex = null;
		
		V source = graph.getVertex(0);
		E sourceEdge = source.getConnectedEdge();
		
		boolean wasVertex = false;
		int z = 0;

		// number of vertices of original graph
		for(EmbeddedEdge<V,E,F> ee : paths) {

			if(ee.getEmbeddedVertices().size() > 4) {
				
//				System.err.println(ee + " passed first test");
				z++;

//				 HERE IS THE PROBLEM
//				sf.prepareEdgeSectors(source, sourceEdge);
				
				testPath = new EmbeddedEdge<V,E,F>();
	
//				wasVertex = sf.walkPath(testPath, source, 0.0, ee.getAngle(), ee.getLength() + 1.0);	//FIXME radius
//				Collection<EmbeddedEdge<V,E,F>> pt = sf.getStarTree(source);
//				for(EmbeddedEdge<V,E,F> ee2 : pt)
//					if(ee2.getEndVertex().getIndex() == ee.getEndVertex().getIndex())
//						testPath = ee2;
//					else
//						System.err.println("Not the same: " + ee2.getEndVertex() + ee.getEndVertex());

				// ok we have the shortest path
						
				// only consider paths where an edge is traversed
				if(testPath.getEmbeddedVertices().size()/2 > 1.5) {
//				System.err.println(testPath + " (retraced) passed second test");
				List<EmbeddedVertex<V,E,F>> vs = testPath.getEmbeddedVertices();
					for(int i = 1; i < vs.size()-2; i += 2) {
						EmbeddedVertex<V,E,F> ev = vs.get(i);
//						E baseEdge = graph.getEdge(ev.getEdgeIndex());
						E baseEdge = ev.getEdge();
		
						Point4d a = baseEdge.getStartVertex().getXYZW();
						Point4d b = baseEdge.getTargetVertex().getXYZW();
		
						double le = baseEdge.getLength();
						double ed = ev.getDistance();
						double d = ed / le;
						System.err.println("Splitting");
						V newVertex = UnfoldSubdivision.splitAtEdge(graph, baseEdge, false);
						newVertex.setCurvature(false);
						newVertex.setXYZW(new Point4d((1-d)*a.x + d*b.x, (1-d)*a.y + d*b.y, (1-d)*a.z + d*b.z,1.0));
//						newVertex.setXYZW(new Point4d(0,0,0,1));
						
//						ev.setEdgeIndex(graph.getEdge(ev.getEdgeIndex()).getOppositeEdge().getNextEdge().getIndex());
//						ev.setDistance(0.0);
					}
				}
			}
		}
	}
}
