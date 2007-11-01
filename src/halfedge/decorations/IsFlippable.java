package halfedge.decorations;

import halfedge.triangulationutilities.TriangulationException;


/**
 * Implementers will have the flip, getFlipCount and resetFlipCount methods. 
 * It's supposed to work for delaunay flips.
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 */
public interface IsFlippable extends HasLength {

	public void flip() throws TriangulationException;
	
	public int getFlipCount();
	
	public void resetFlipCount();
	
}
