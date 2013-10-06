package koebe.frontend;

import halfedge.HalfEdgeDataStructure;
import halfedge.HalfEdgeUtility;
import halfedge.frontend.StandardEditor;
import halfedge.frontend.controller.MainController.GraphChangedListener;
import halfedge.surfaceutilities.ConsistencyCheck;
import halfedge.surfaceutilities.SurfaceException;
import halfedge.surfaceutilities.SurfaceUtility;

import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import koebe.KoebePolyhedron;
import koebe.KoebePolyhedron.KoebePolyhedronContext;
import koebe.PolyederNormalizer;
import koebe.frontend.content.jrealityviewer.KoebePolyhedronView;
import koebe.frontend.controller.MainController;
import koebe.frontend.tool.EdgeQuadSubdivide;
import koebe.frontend.tool.MedialSubdivide;
import koebe.frontend.tool.VertexQuadSubdivide;
import circlepatterns.graph.CPEdge;
import circlepatterns.graph.CPFace;
import circlepatterns.graph.CPVertex;
import de.jreality.swing.ScenePanel;
import de.jreality.vr.ViewerVR;

public class Koebe3D implements GraphChangedListener, ChangeListener{

	private MainController 
		controller = new MainController();
	private HalfEdgeDataStructure<CPVertex, CPEdge, CPFace> 
		graph = HalfEdgeDataStructure.createHEDS(CPVertex.class, CPEdge.class, CPFace.class);
	private StandardEditor<CPVertex, CPEdge, CPFace>
		editPanel = new StandardEditor<CPVertex, CPEdge, CPFace>(graph, controller);
	private JFrame 
		frame = null;
	private KoebePolyhedronView
		koebeViewer = new KoebePolyhedronView(controller); 
	
	public Koebe3D() throws IOException{
		System.setProperty("jreality.data", "/net/MathVis/data/testData3D");
		controller.addGraphChangedListener(this);
		controller.setUseFaces(false);
		editPanel.getEditPanel().setDrawGrid(false);		
		editPanel.addTool(new MedialSubdivide());
		editPanel.addTool(new EdgeQuadSubdivide());
		editPanel.addTool(new VertexQuadSubdivide());
		
		ScenePanel pan = new ScenePanel();
		pan.setPanelWidth(1);
		frame = pan.getFrame();
		

		controller.setMainPanel(editPanel);
		controller.setMainFrame(frame);
		frame.add(editPanel);

		frame.setSize(400, 500);

		koebeViewer.getSceneRoot().addTool(pan.getPanelTool());
		
		
		ViewerVR vr = new ViewerVR();
		vr.setContent(koebeViewer.getSceneRoot());
		
//		ViewerApp va = vr.display();
//		va.update();
//		va.display();
	}
	

	  @Override
	public void stateChanged(ChangeEvent e) {
	  }
	
	@Override
	public void graphChanged() {
		if (koebeViewer == null)
			return;
		if (controller.getEditedGraph().getNumEdges() == 0)
			koebeViewer.resetGeometry();
		KoebePolyhedronContext<CPVertex, CPEdge, CPFace> context = null;
		try {
			HalfEdgeUtility.removeAllFaces(controller.getEditedGraph());
			SurfaceUtility.linkAllEdges(controller.getEditedGraph());
			SurfaceUtility.fillHoles(controller.getEditedGraph());
		} catch (SurfaceException e1) {
			controller.setStatus(e1.getMessage());
		}
		if (!ConsistencyCheck.isThreeConnected(controller.getEditedGraph())){
			controller.setStatus("graph not three-connected");
			return;
		}
		if (!ConsistencyCheck.isValidSurface(controller.getEditedGraph())){
			controller.setStatus("No valid surface! Maybe not embedded");
			return;
		}
		try {
			context = KoebePolyhedron.contructKoebePolyhedron(controller.getEditedGraph(), 1E-4, 20);
			if (controller.isNormalize()) PolyederNormalizer.normalize(context);
			controller.setStatus("polyhedron successfully constructed");
		} catch (Exception e) {
			controller.setStatus(e.getMessage());
			return;
		}
		koebeViewer.updateGeometry(context);
	}
	
	
	
	public static void main(String[] args)  throws IOException{
		new Koebe3D();
	}
	
}
