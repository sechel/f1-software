package halfedge.surfaceutilities;

import halfedge.Edge;
import halfedge.Face;
import halfedge.Vertex;

public class EmbeddedFace  <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	>
	extends Face<EmbeddedVertex<V,E,F>, EmbeddedEdge<V,E,F>, EmbeddedFace<V,E,F>>{

	private static final long 
	serialVersionUID = 1L;
	
	@Override
	protected EmbeddedFace<V,E,F> getThis() {
		return this;
	}
}
