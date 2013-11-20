package circlepatterns.graph;

import halfedge.Vertex;
import halfedge.decorations.HasQuadGraphLabeling;
import halfedge.decorations.HasXY;
import halfedge.decorations.HasXYZW;

import javax.vecmath.Point2d;
import javax.vecmath.Point4d;


/**
 * The face class for the koebe project
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 */
public class CPVertex extends Vertex<CPVertex, CPEdge, CPFace> implements HasQuadGraphLabeling, HasXY, HasXYZW{

	private static final long 
		serialVersionUID = 1L;
	private Point2d
		pos2d = new Point2d();	
	private Point4d
		pos4d = new Point4d(0, 0, 0, 1);
	private QuadGraphLabel
		quadGraphLabel = null;
	
	
//	@Override
//	protected Object clone() throws CloneNotSupportedException {
//		CPVertex clone = (CPVertex)super.clone();
//		clone.coord = new Point2d(coord);
//		clone.homCoord = new Point4d(homCoord);
//		clone.quadGraphLabel = quadGraphLabel;
//		return clone;
//	}
	
	@Override
	protected CPVertex getThis() {
		return this;
	}
	
	@Override
	public void setXY(Point2d p) {
		getXY().set(p);		
	}
	@Override
	public Point2d getXY() {
		if (pos2d == null)
			pos2d = new Point2d();
		return pos2d;
	}

	@Override
	public void setXYZW(Point4d p) {
		getXYZW().set(p);		
	}
	@Override
	public Point4d getXYZW() {
		if (pos4d == null)
			pos4d = new Point4d(0, 0, 0, 1);
		return pos4d;
	}

	@Override
	public QuadGraphLabel getVertexLabel() {
		if (quadGraphLabel == null)
			quadGraphLabel = QuadGraphLabel.INTERSECTION;
		return quadGraphLabel;
	}

	@Override
	public void setVertexLabel(QuadGraphLabel l) {
		this.quadGraphLabel = l;
	}

}
