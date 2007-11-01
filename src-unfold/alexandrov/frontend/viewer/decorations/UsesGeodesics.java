package alexandrov.frontend.viewer.decorations;

import halfedge.Edge;
import halfedge.Face;
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

import org.apache.commons.collections15.BidiMap;


public interface UsesGeodesics <
		V extends Vertex<V, E, F> & HasRadius & HasXYZW & HasCurvature & HasXY,
		E extends Edge<V, E, F> & IsBoundary & HasLength & IsFlippable & IsHidable & HasAngle,
		F extends Face<V, E, F>
	> {

	public BidiMap<V, EmbeddedEdge<V,E,F>> getGeodesics();
	
	public void setGeodesics(BidiMap<V, EmbeddedEdge<V,E,F>> e);
	
}
