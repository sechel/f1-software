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
import koebe.PolyederNormalizer;
import koebe.KoebePolyhedron.KoebePolyhedronContext;
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

	public Boolean initTool() {
		super.initTool();
		DBGTracer.setActive(true);
		if (!ConsistencyCheck.isValidSurface(medial)){
			System.err.println("no valid surface after medialize!");
			return false;
		}
		KoebePolyhedronContext<CPVertex, CPEdge, CPFace> context = null;
		try {
			context = KoebePolyhedron.contructKoebePolyhedron(medial);
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
	
	public boolean processEditOperation(EditOperation operation) throws EditOperationException {
		return false;
	}	
	
		
	public void paint(GraphGraphics g) {

	}
	
	
	public Icon getIcon() {
		return icon;
	}
	
	public JPanel getOptionPanel() {
		return null;
	}
	
	
	public String getShortDescription() {
		return "Create Medial Graph";
	}
	
}
