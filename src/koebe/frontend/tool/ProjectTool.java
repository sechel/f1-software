package koebe.frontend.tool;

import static de.jreality.scene.data.Attribute.COORDINATES;
import halfedge.HalfEdgeDataStructure;
import halfedge.frontend.content.GraphGraphics;
import halfedge.frontend.graphtool.EditOperation;
import halfedge.frontend.graphtool.EditOperationException;
import halfedge.frontend.graphtool.GraphTool;

import javax.swing.Icon;
import javax.swing.JPanel;

import koebe.frontend.content.jrealityviewer.KoebePolyhedronView;
import koebe.frontend.controller.MainController;
import circlepatterns.graph.CPEdge;
import circlepatterns.graph.CPFace;
import circlepatterns.graph.CPVertex;
import de.jreality.math.Rn;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.data.DoubleArrayArray;

public class ProjectTool implements GraphTool<CPVertex, CPEdge, CPFace> {

	private MainController
		controller = null;
	
	
	private KoebePolyhedronView
		jRViewer = null;
	
	public ProjectTool(KoebePolyhedronView viewer) {
		jRViewer = viewer;
	}
	
	
	@Override
	public Boolean initTool() {
		if (jRViewer.getPolyhedron() == null) {
			controller.setStatus("No active polyhedron!");
			return false;
		}
		IndexedFaceSet ifs = jRViewer.getPolyhedron();
		double[][] vertices = ifs.getVertexAttributes(COORDINATES).toDoubleArrayArray(null);
		for (double[] v : vertices)
			Rn.normalize(v, v);
		ifs.setVertexAttributes(COORDINATES, new DoubleArrayArray.Array(vertices));
		jRViewer.setPolyhedron(ifs);
		return true;
	}
	
	@Override
	public void commitEdit(HalfEdgeDataStructure<CPVertex, CPEdge, CPFace> graph) {
		
	}

	@Override
	public String getDescription() {
		return getShortDescription();
	}

	@Override
	public Icon getIcon() {
		return null;
	}

	@Override
	public String getName() {
		return "Project";
	}

	@Override
	public JPanel getOptionPanel() {
		return null;
	}

	@Override
	public String getShortDescription() {
		return getName();
	}


	@Override
	public void leaveTool() {

	}

	@Override
	public boolean needsRepaint() {
		return true;
	}

	@Override
	public void paint(GraphGraphics g) {

	}

	@Override
	public boolean processEditOperation(EditOperation operation)
			throws EditOperationException {
		return false;
	}

	@Override
	public void resetTool() {

	}

	@Override
	public void setController(halfedge.frontend.controller.MainController<CPVertex, CPEdge, CPFace> controller) {
		this.controller = (MainController)controller;
	}

}
