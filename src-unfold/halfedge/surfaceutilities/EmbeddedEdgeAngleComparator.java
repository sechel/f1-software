package halfedge.surfaceutilities;


	
//public class EmbeddedEdgeAngleComparator 
//<
//	V extends Vertex<V, E, F> & HasXY & HasXYZW,
//	E extends Edge<V, E, F> & IsBoundary & HasLength & IsFlippable & IsHidable,
//	F extends Face<V, E, F>
//> implements Comparator<EmbeddedEdge<V,E,F>>{
//
//	public int compare(EmbeddedEdge<V,E,F> e1, EmbeddedEdge<V,E,F> e2) {
//		Double check = e1.getAngle() - e2.getAngle();
////		Double check = EmbeddedHalfEdgeUtility.getAngle(e1, e2);
//		if (check < 0)
//			return -1;
//		if (check > 0)
//			return 1;
//		return 0;
//	}
//	
//}