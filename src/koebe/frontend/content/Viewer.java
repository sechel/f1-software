package koebe.frontend.content;

import javax.swing.JComponent;

import koebe.KoebePolyhedron.KoebePolyhedronContext;
import circlepatterns.graph.CPEdge;
import circlepatterns.graph.CPFace;
import circlepatterns.graph.CPVertex;


/**
 * A basic viewer interface
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 */
public interface Viewer {

	public void update();
	
	public void updateGeometry(KoebePolyhedronContext<CPVertex, CPEdge, CPFace> context);
	
	public void resetGeometry();
	
	public JComponent getViewerComponent();
	
	public de.jreality.scene.Viewer getJRealityViewer();
	
}
