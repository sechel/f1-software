package alexandrov.frontend.viewer;

import halfedge.Edge;
import halfedge.Face;
import halfedge.HalfEdgeDataStructure;
import halfedge.Vertex;
import halfedge.decorations.HasCurvature;
import halfedge.decorations.HasLength;
import halfedge.decorations.HasRadius;
import halfedge.decorations.HasXYZW;
import halfedge.decorations.IsBoundary;
import halfedge.decorations.IsFlippable;
import halfedge.decorations.IsHidable;
import de.jreality.scene.SceneGraphComponent;

public abstract class ViewAddon <
		V extends Vertex<V, E, F> & HasRadius & HasXYZW & HasCurvature,
		E extends Edge<V, E, F> & IsBoundary & HasLength & IsFlippable & IsHidable,
		F extends Face<V, E, F>

	> {
	
	protected SceneGraphComponent sgc = null;
	protected HalfEdgeDataStructure<V, E, F> graph = null;
	
	public SceneGraphComponent getSceneGraphComponent(){
		return sgc;
		
	}
	public void generateSceneGraphComponent() {
		
		sgc  = new SceneGraphComponent();
		sgc.setVisible(true);
	}
	public void update() {

	}
	
	public void setGraph(HalfEdgeDataStructure<V,E,F> graph) {
		this.graph = graph;
	}
	

}
