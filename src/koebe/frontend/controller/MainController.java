package koebe.frontend.controller;

import halfedge.HalfEdgeUtility;
import halfedge.surfaceutilities.ConsistencyCheck;
import halfedge.surfaceutilities.SurfaceException;
import halfedge.surfaceutilities.SurfaceUtility;
import koebe.KoebePolyhedron;
import koebe.PolyederNormalizer;
import koebe.KoebePolyhedron.KoebePolyhedronContext;
import koebe.frontend.content.Viewer;
import circlepatterns.graph.CPEdge;
import circlepatterns.graph.CPFace;
import circlepatterns.graph.CPVertex;

public class MainController extends halfedge.frontend.controller.MainController<CPVertex, CPEdge, CPFace>{
	
	private Viewer
		koebeViewer = null;
	private boolean 
		normalize = true;


	public void fireGraphChanged() {
		super.fireGraphChanged();
		updateKoebePolyhedron(normalize);
	}
	
	
	public void updateKoebePolyhedron(boolean normalize){
		if (koebeViewer == null)
			return;
		if (getEditedGraph().getNumEdges() == 0)
			koebeViewer.resetGeometry();
		KoebePolyhedronContext<CPVertex, CPEdge, CPFace> context = null;
		try {
			HalfEdgeUtility.removeAllFaces(getEditedGraph());
			SurfaceUtility.linkAllEdges(getEditedGraph());
			SurfaceUtility.fillHoles(getEditedGraph());
		} catch (SurfaceException e1) {
			setStatus(e1.getMessage());
		}
		if (!ConsistencyCheck.isThreeConnected(getEditedGraph())){
			setStatus("graph not three-connected");
			return;
		}
		if (!ConsistencyCheck.isValidSurface(getEditedGraph())){
			setStatus("No valid surface! Maybe not embedded");
			return;
		}
		try {
			context = KoebePolyhedron.contructKoebePolyhedron(getEditedGraph());
			if (normalize) 
				PolyederNormalizer.normalize(context);
			setStatus("polyhedron successfully constructed");
		} catch (Exception e) {
			setStatus(e.getMessage());
			return;
		}
		getKoebeViewer().updateGeometry(context);
	}

	
	public void setKoebeViewer(Viewer koebeViewer) {
		this.koebeViewer = koebeViewer;
	}

	
	public Viewer getKoebeViewer() {
		return koebeViewer;
	}


	public boolean isNormalize() {
		return normalize;
	}


	public void setNormalize(boolean normalize) {
		this.normalize = normalize;
	}
	
	
}