package minimalsurface.controller;

import minimalsurface.frontend.content.GraphEditor;
import minimalsurface.frontend.content.MinimalSurfacePanel;
import circlepatterns.graph.CPEdge;
import circlepatterns.graph.CPFace;
import circlepatterns.graph.CPVertex;

public class MainController extends halfedge.frontend.controller.MainController<CPVertex, CPEdge, CPFace>{
	
	private MinimalSurfacePanel
		viewer = null;
	private boolean 
		normalize = true;
	private GraphEditor
		graphEditor = null;


	public void fireGraphChanged() {
		super.fireGraphChanged();
	}
	
	
	
	public void setViewer(MinimalSurfacePanel viewer) {
		this.viewer = viewer;
	}

	
	public MinimalSurfacePanel getViewer() {
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
