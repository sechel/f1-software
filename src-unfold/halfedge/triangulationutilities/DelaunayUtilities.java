package halfedge.triangulationutilities;

import halfedge.Edge;
import halfedge.Face;
import halfedge.HalfEdgeDataStructure;
import halfedge.Vertex;
import halfedge.decorations.HasLength;
import halfedge.decorations.HasXY;
import halfedge.decorations.HasXYZW;
import halfedge.decorations.IsBoundary;
import halfedge.decorations.IsFlippable;
import halfedge.decorations.IsHidable;
import halfedge.surfaceutilities.EmbeddedVertex;
import halfedge.surfaceutilities.UnfoldSubdivision;

import java.util.HashSet;
import java.util.Stack;

import javax.vecmath.Point2d;
import javax.vecmath.Point3d;
import javax.vecmath.Point4d;

import math.util.VecmathTools;

public class DelaunayUtilities {

	public static <
		V extends Vertex<V, E, F> & HasXYZW,
		E extends Edge<V, E, F> & HasLength & IsFlippable,
		F extends Face<V, E, F>
	> boolean isAcuteTriangulation(HalfEdgeDataStructure<V,E,F> graph) throws TriangulationException{
		for(E e : graph.getEdges()) {
			if(Delaunay.getAngle(e) > Math.PI/2.0) {
				message("Triangle is not acute");
				return false;
			}
		}
		
		return true;
	}
	
	private static void message(String m) {
		if (ConsistencyCheck.getDebug()) {
			System.err.println(ConsistencyCheck.class.getSimpleName() + ": " + m);
		}
	}
	
	
	public static <
		V extends Vertex<V, E, F> & HasXY & HasXYZW,	// must be embedded for now, because of strict conv. cond. in alg.
		E extends Edge<V, E, F> & IsFlippable & IsHidable,
		F extends Face<V, E, F>
	> void overlayDelaunay(HalfEdgeDataStructure<V, E, F> graph) throws TriangulationException{
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
			if (!Delaunay.isDelaunay(ab)){
				//ab.flip();
				UnfoldSubdivision.splitAtFlip(graph, ab);
				for (E xy : Delaunay.getPositiveKiteBorder(ab)){
					if (!markSet.contains(xy)){
						markSet.add(xy);
						stack.push(xy);
					}
				}
			}
		}
	}

	public static <
		V extends Vertex<V, E, F>  & HasXYZW,
		E extends Edge<V, E, F> & IsFlippable & IsHidable,
		F extends Face<V, E, F>
	> Point4d getCircumcenter4d(HalfEdgeDataStructure<V, E, F> graph, E edge) throws TriangulationException{

		E a = edge;
		E b = a.getNextEdge();
		E c = b.getNextEdge();
		
//		Point3d A = new Point3d(VecmathTools.p4top3(e1.getTargetVertex().getXYZW()));
//		Point3d B = new Point3d(VecmathTools.p4top3(e2.getTargetVertex().getXYZW()));
//		Point3d C = new Point3d(VecmathTools.p4top3(e3.getTargetVertex().getXYZW()));
		
		Point4d A = new Point4d(b.getTargetVertex().getXYZW());
		Point4d B = new Point4d(c.getTargetVertex().getXYZW());
		Point4d C = new Point4d(a.getTargetVertex().getXYZW());
		
		// barycentric
//		Point4d p = new Point4d(A.x+B.x+C.x, A.y+B.y+C.y, A.z+B.z+C.z, A.w+B.w+C.w);
//		p.scale(1/3.0);
//		return p;

		
		double las = Math.pow(a.getLength(),2);
		double lbs = Math.pow(b.getLength(),2);
		double lcs = Math.pow(c.getLength(),2);

		Point3d cc_bc = new Point3d(las*(-las + lbs + lcs), lbs*(las - lbs + lcs), lcs*(las + lbs - lcs));

		
		A.scale(cc_bc.x);
		B.scale(cc_bc.y);
		C.scale(cc_bc.z);
		
		A.add(B);
		A.add(C);
		
		return new Point4d(A.x, A.y, A.z, A.w);
		
//		double alpha = Delaunay.getAngle(e1);
//		
//		Point2d pA = new Point2d(0,0);
//		Point2d pB = new Point2d(b, 0);
//		Point2d pC = new Point2d(Math.cos(alpha), Math.sin(alpha));
//		pC.scale(a);
//		
//		 // pCentroid is centroid in 2d now
//		Point2d pCentroid = new Point2d(cc_bc.x * pA.x + cc_bc.y * pB.x + cc_bc.z * pC.x,
//										cc_bc.x * pA.y + cc_bc.y * pB.y + cc_bc.z * pC.y);
//		
//		
//		Vector3d AB = new Vector3d(B);
//		AB.sub(A);
//		Vector3d AC = new Vector3d(C);
//		AC.sub(A);
//		
//		AB.normalize();
//		AC.normalize();
//		
//		AB.scale(pCentroid.x);
//		AC.scale(pCentroid.y);
//		
//		A.add(AB);
//		A.add(AC);
	


	}
	
	public static <
		V extends Vertex<V, E, F>  & HasXY,
		E extends Edge<V, E, F> & IsFlippable & IsHidable,
		F extends Face<V, E, F>
	> Point2d getCircumcenter2d(HalfEdgeDataStructure<V, E, F> graph, E edge) throws TriangulationException{
	
		E e1 = edge;
		E e2 = edge.getNextEdge();
		E e3 = e2.getNextEdge();
		
		Point2d A = new Point2d(e1.getTargetVertex().getXY());
		Point2d B = new Point2d(e2.getTargetVertex().getXY());
		Point2d C = new Point2d(e3.getTargetVertex().getXY());
		
		// FIXME
		// gaaaargh helt fel!
		double a = e1.getLength();
		double b = e2.getLength();
		double c = e3.getLength();
	
		Point4d cc_bc = new Point4d(a*(-a*a + b*b + c*c), b*(a*a - b*b + c*c), c*(a*a + b*b - c*c), 1);
		A.scale(cc_bc.x);
		B.scale(cc_bc.y);
		C.scale(cc_bc.z);
		
		A.add(B);
		A.add(C);
		
		return A;
			
	}
	
	public static <
		V extends Vertex<V, E, F>  & HasXYZW,
		E extends Edge<V, E, F> & IsFlippable & IsHidable & IsBoundary & HasLength,
		F extends Face<V, E, F>
	> EmbeddedVertex<V,E,F> getCircumcenterEV(HalfEdgeDataStructure<V, E, F> graph, E edge) throws TriangulationException{
		if (!ConsistencyCheck.isTriangulation(graph))
			throw new TriangulationException("Graph is no triangulation!");
	
		E e1 = edge;
		E e2 = edge.getNextEdge();
		E e3 = e2.getNextEdge();

		double d = VecmathTools.distFromPointToLine(getCircumcenter4d(graph, edge), 
													edge.getTargetVertex().getXYZW(),
													edge.getStartVertex().getXYZW());
		
		double h = VecmathTools.distFromPointToLine(getCircumcenter4d(graph, edge), 
													edge.getNextEdge().getTargetVertex().getXYZW(),
													edge.getStartVertex().getXYZW());
		
		return new EmbeddedVertex<V,E,F>(graph, edge, d, h);
	}	
	
}
