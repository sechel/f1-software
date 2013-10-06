package alexandrov.graph;

import halfedge.Face;
import halfedge.decorations.HasXYZW;

import javax.vecmath.Point4d;


/**
 * The face class for the alexandrov project
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 */
public class CPMFace extends Face<CPMVertex, CPMEdge, CPMFace> implements HasXYZW{

	private static final long 
		serialVersionUID = 1L;
	private Point4d
		pos = new Point4d();
	
	@Override
	protected CPMFace getThis() {
		return this;
	}
	
	@Override
	public Point4d getXYZW() {
		return pos;
	}
	
	@Override
	public void setXYZW(Point4d p) {
		pos.set(p);
	}
	
}
