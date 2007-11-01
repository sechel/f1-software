package halfedge.surfaceutilities;

import halfedge.Edge;
import halfedge.Face;
import halfedge.HalfEdgeDataStructure;
import halfedge.Vertex;
import halfedge.decorations.HasAngle;
import halfedge.decorations.HasLength;
import halfedge.decorations.HasRadius;
import halfedge.decorations.HasXY;
import halfedge.decorations.HasXYZW;
import halfedge.decorations.IsBoundary;
import halfedge.decorations.IsFlippable;
import halfedge.decorations.IsHidable;
import halfedge.triangulationutilities.Delaunay;
import halfedge.triangulationutilities.DelaunayUtilities;
import halfedge.triangulationutilities.TriangulationException;
import halfedge.unfoldutilities.Unfolder;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;

import javax.vecmath.Point3d;
import javax.vecmath.Point4d;
import javax.vecmath.Vector3d;

import math.util.VecmathTools;
import alexandrov.math.CPMCurvatureFunctional;

public class EmbeddedHalfEdgeUtility {

	public static 
	<
		V extends Vertex<V, E, F> & HasXYZW,
		E extends Edge<V, E, F> & IsBoundary & HasLength & IsFlippable,
		F extends Face<V, E, F>
	> Point4d getEmbeddedCoordinate(EmbeddedVertex<V,E,F> v){
	
//		E baseEdge = v.getGraph().getEdge(v.getEdgeIndex());
		E baseEdge = v.getEdge();
				
		Point4d baseStart = baseEdge.getStartVertex().getXYZW();
		Point4d baseEnd = baseEdge.getTargetVertex().getXYZW();
//		
//		// dir
//		baseEnd.sub(baseStart);
//		Vector4d dir = new Vector4d(baseEnd);
//		dir.normalize();
//		dir.scale(v.getDistance());
//		
//		baseStart.add(dir);
//		Point4d coordinate = new Point4d(baseStart);
//		return coordinate;

		Point4d coord = new Point4d();

		double edgeLen = baseEdge.getLength();
		double distFact = (edgeLen - v.getDistance())/edgeLen;
		distFact = 0.5;
		
		coord.x = baseStart.x + distFact * baseEnd.x;
		coord.y = baseStart.z + distFact * baseEnd.y;
		coord.z = baseStart.z + distFact * baseEnd.z;
		coord.w = 1.0;
		
		return coord;
		
	}
	
	public static 
	<
		V extends Vertex<V, E, F> & HasXYZW & HasXY,
		E extends Edge<V, E, F> & IsBoundary & HasLength & IsFlippable & IsHidable & HasAngle,
		F extends Face<V, E, F>
	> Double getAngle(EmbeddedEdge<V,E,F> e1, EmbeddedEdge<V,E,F> e2) {
		
		Vector3d t = new Vector3d(VecmathTools.p4top3(e1.controls.get(1).getXYZW()));
		t.sub(VecmathTools.p4top3(e1.getSourceVertex().getXYZW()));
		
		Vector3d u = new Vector3d(VecmathTools.p4top3(e2.controls.get(1).getXYZW()));
		u.sub(VecmathTools.p4top3(e2.getSourceVertex().getXYZW()));
		
		t.normalize();
		u.normalize();
		
		return Math.acos(t.dot(u));
		
	}
	
	public static 
	<
		V extends Vertex<V, E, F> & HasXYZW & HasRadius & HasXY,
		E extends Edge<V, E, F> & IsBoundary & HasLength & IsFlippable & IsHidable & HasAngle,
		F extends Face<V, E, F>
	> Double getAngle2(EmbeddedEdge<V,E,F> e1, EmbeddedEdge<V,E,F> e2) throws TriangulationException{
		
			EmbeddedVertex<V,E,F> v1 = e1.getEmbeddedVertex(1);
			E e1start = v1.getEdge().getNextEdge();
		
			double le1 = e1start.getLength();
			double d1 = v1.getDistance();
			Vector3d t = new Vector3d(VecmathTools.p4top3(v1.getXYZW()));
			t.sub(VecmathTools.p4top3(e1.getSourceVertex().getXYZW()));
			double k1 = t.length();
			
			double alpha1 = Math.acos((le1*le1 + k1*k1 - d1*d1)/(2*le1*k1)); 

			EmbeddedVertex<V,E,F> v2 = e2.getEmbeddedVertex(1);
			E e2start = v2.getEdge().getNextEdge();
			
			double le2 = e2start.getLength();
			double d2 = v2.getDistance();
			Vector3d u = new Vector3d(VecmathTools.p4top3(v2.getXYZW()));
			u.sub(VecmathTools.p4top3(e2.getSourceVertex().getXYZW()));
			double k2 = u.length();
			
			double alpha2 = Math.acos((le2*le2 + k2*k2 - d2*d2)/(2*le2*k2));
			
			Collection<E> star = e1.getSourceVertex().getEdgeStar();
			
			double gamma_sum = 0.0;
			for(E e : star) {
				gamma_sum += Delaunay.getAngle(e);
				if(e == e2start || e == e2start.getOppositeEdge())
					break;
			}
			double total = gamma_sum - (Delaunay.getAngle(e1start) - alpha1) - (Delaunay.getAngle(e2start) - alpha2);
			return Math.min(total, CPMCurvatureFunctional.getGammaAt(e1.getSourceVertex()) - total);
//			return alpha2 + Delaunay.getAngle(e1start.getPreviousEdge()) - alpha1;
	}
	
//	public static 
//	<
//		V extends Vertex<V, E, F> & HasXYZW & HasXY & HasRadius,
//		E extends Edge<V, E, F> & IsBoundary & HasLength & IsFlippable & IsHidable & HasAngle,
//		F extends Face<V, E, F>
//	> Double getAngle3(EmbeddedEdge<V,E,F> e1, EmbeddedEdge<V,E,F> e2) throws TriangulationException {
//		
//		E b1 = e1.getEmbeddedVertex(1).getEdge().getNextEdge();
//		E b2 = e2.getEmbeddedVertex(1).getEdge().getNextEdge();
//		
//		double rel = CPMCurvatureFunctional.getGammaBetween(b1, b2);
//		
////		double l1 = e1.getEmbeddedVertex(1).getLength();
////		double d1 = e1.getEmbeddedVertex(1).getDistance();
////		double alpha1 = Math.acos((b1.getLength()*b1.getLength() + l1*l1 - d1*d1)/(2*l1*d1));
////		
////		double l2 = e2.getEmbeddedVertex(1).getLength();
////		double d2 = e2.getEmbeddedVertex(1).getDistance();
////		double alpha2 = Math.acos((b2.getLength()*b2.getLength() + l2*l2 - d2*d2)/(2*l2*d2));
//		
////		double alpha1 = e1.getEmbeddedVertex(2).getAngle();
////		double alpha2 = e2.getEmbeddedVertex(2).getAngle();
//		
//		Vector3d t = new Vector3d(VecmathTools.p4top3(e1.controls.get(1).getXYZW()));
//		t.sub(VecmathTools.p4top3(e1.getSourceVertex().getXYZW()));
//		Vector3d tb = new Vector3d(VecmathTools.p4top3(b1.getStartVertex().getXYZW()));
//		tb.sub(VecmathTools.p4top3(b1.getTargetVertex().getXYZW()));
//		
//		Vector3d u = new Vector3d(VecmathTools.p4top3(e2.controls.get(1).getXYZW()));
//		u.sub(VecmathTools.p4top3(e2.getSourceVertex().getXYZW()));
//		Vector3d ub = new Vector3d(VecmathTools.p4top3(b2.getStartVertex().getXYZW()));
//		ub.sub(VecmathTools.p4top3(b2.getTargetVertex().getXYZW()));
//		
//		t.normalize(); tb.normalize();
//		u.normalize(); ub.normalize();
//		
//		double alpha1 = Math.acos(t.dot(tb));
//		double alpha2 = Math.acos(u.dot(ub));
//		
//		return rel - alpha1 + alpha2;
////		return rel;
//		
//	}
	
	public static 
	<
		V extends Vertex<V, E, F> & HasXYZW & HasXY,
		E extends Edge<V, E, F> & IsBoundary & HasLength & IsFlippable & IsHidable & HasAngle,
		F extends Face<V, E, F>
	> Vector3d getEmbeddedEdge(E e) {
		Point3d t = VecmathTools.p4top3(e.getTargetVertex().getXYZW());
		Point3d s = VecmathTools.p4top3(e.getStartVertex().getXYZW());
		
		Vector3d r = new Vector3d(t);
		t.sub(s);
		return r;
	}
	
	public static 
	<
		V extends Vertex<V, E, F> & HasXYZW & HasXY,
		E extends Edge<V, E, F> & IsBoundary & HasLength & IsFlippable & IsHidable & HasAngle,
		F extends Face<V, E, F>
	> Collection<EmbeddedEdge<V,E,F>> embeddVoronoi(HalfEdgeDataStructure<V,E,F> graph) throws TriangulationException{

		Collection<EmbeddedEdge<V,E,F>> paths = new LinkedList<EmbeddedEdge<V,E,F>>();
		
		HashMap<F,V> dualMap = new HashMap<F,V>();
		HashMap<E,E> edgeToEdgeMap = new HashMap<E,E>();
		HalfEdgeDataStructure<V,E,F> voronoi = Unfolder.constructVoronoi(graph, dualMap, edgeToEdgeMap);
		
		for(E e : graph.getEdges()) {
			EmbeddedVertex<V,E,F> mid = new EmbeddedVertex<V,E,F>(graph, e, 0.5, 0);
			EmbeddedVertex<V,E,F> left = DelaunayUtilities.getCircumcenterEV(graph, e);
			EmbeddedVertex<V,E,F> right = DelaunayUtilities.getCircumcenterEV(graph, e.getOppositeEdge());
			
			EmbeddedEdge<V,E,F> ee = new EmbeddedEdge<V, E, F>();
			ee.addEmbeddedVertexPair(left);
			ee.addEmbeddedVertexPair(mid);
			ee.addEmbeddedVertexPair(right);
			
			paths.add(ee);
		}
		
		return paths;

	}
	
}
