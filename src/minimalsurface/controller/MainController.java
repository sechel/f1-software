package minimalsurface.controller;

import minimalsurface.frontend.content.GraphEditor;
import minimalsurface.frontend.content.MinimalSurfaceContent;
import circlepatterns.graph.CPEdge;
import circlepatterns.graph.CPFace;
import circlepatterns.graph.CPVertex;

public class MainController extends halfedge.frontend.controller.MainController<CPVertex, CPEdge, CPFace>{
	
	private MinimalSurfaceContent
		viewer = null;
	private boolean 
		normalize = true;
	private GraphEditor
		graphEditor = null;


	@Override
	public void fireGraphChanged() {
		super.fireGraphChanged();
	}
	
	
	
	public void setViewer(MinimalSurfaceContent viewer) {
		this.viewer = viewer;
	}

	
	public MinimalSurfaceContent getViewer() {
		return viewer;
	}


	public boolean isNormalize() {
		return normalize;
	}


	public void setNormalize(boolean normalize) {
		this.normalize = normalize;
	}



	public GraphEditor getGraphEditor() {
		return graphEditor;
	}



	public void setGraphEditor(GraphEditor graphEditor) {
		this.graphEditor = graphEditor;
	}
	
	
}
