package halfedge.frontend.graphconstraint;

import halfedge.Edge;
import halfedge.Face;
import halfedge.HalfEdgeDataStructure;
import halfedge.Vertex;
import halfedge.decorations.HasXY;

import javax.swing.Icon;


/**
 * The interface for a graph constraint (BETA)
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 */
public interface GraphConstraint 
<
	V extends Vertex<V, E, F> & HasXY,
	E extends Edge<V, E, F>, 
	F extends Face<V, E, F>
> {

	public boolean checkConstraint(HalfEdgeDataStructure<V, E, F> graph);
	
	public Icon getIcon();
	
	public String getName();
	
	public String getDescription();
	
	public String getShortDescription();
	
}
