package koebe.frontend.tool;

import halfedge.frontend.content.GraphGraphics;
import halfedge.frontend.graphtool.EditOperation;
import halfedge.frontend.graphtool.EditOperationException;
import halfedge.surfaceutilities.ConsistencyCheck;
import image.ImageHook;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

import koebe.KoebePolyhedron;
import koebe.KoebePolyhedron.KoebePolyhedronContext;
import koebe.PolyederNormalizer;
import util.debug.DBGTracer;
import circlepatterns.graph.CPEdge;
import circlepatterns.graph.CPFace;
import circlepatterns.graph.CPVertex;


/**
 * Generates a medial graph from the active graph and replaces 
 * the current graph
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 */
public class MedialSubdivide extends GenerateMedialGraph  {

	protected Icon 
		icon = new ImageIcon(ImageHook.getImage("medialsubdivide.png"));

	@Override
	public Boolean initTool() {
		super.initTool();
		DBGTracer.setActive(true);
		if (!ConsistencyCheck.isValidSurface(medial)){
			System.err.println("no valid surface after medialize!");
			return false;
		}
		KoebePolyhedronContext<CPVertex, CPEdge, CPFace> context = null;
		try {
			context = KoebePolyhedron.contructKoebePolyhedron(medial, 1E-4, 20);
			PolyederNormalizer.normalize(context);
			controller.setStatus("polyhedron successfully constructed");
		} catch (Exception e) {
			controller.setStatus(e.getMessage());
			return false;
		}
		controller.setEditedGraph(medial);
		controller.getKoebeViewer().updateGeometry(context);
		return false;
	}
	
	@Override
	public boolean processEditOperation(EditOperation operation) throws EditOperationException {
		return false;
	}	
	
		
	@Override
	public void paint(GraphGraphics g) {

	}
	
	
	@Override
	public Icon getIcon() {
		return icon;
	}
	
	@Override
	public JPanel getOptionPanel() {
		return null;
	}
	
	
	@Override
	public String getShortDescription() {
		return "Create Medial Graph";
	}
	
}
