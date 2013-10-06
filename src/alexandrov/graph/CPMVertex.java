package alexandrov.graph;

import halfedge.Vertex;
import halfedge.decorations.HasCurvature;
import halfedge.decorations.HasRadius;
import halfedge.decorations.HasXY;
import halfedge.decorations.HasXYZW;

import javax.vecmath.Point2d;
import javax.vecmath.Point4d;

/**
 * The vertex class for the alexandrov project
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 */
public class CPMVertex extends Vertex<CPMVertex, CPMEdge, CPMFace> implements HasXYZW, HasXY, HasRadius, HasCurvature{

	private static final long 
		serialVersionUID = 1L;
	private Point4d
		pos4d = new Point4d();
	private Point2d
		pos2d = new Point2d();
	private Double
		radius = 100.0;
	
//	/*
//	 * Unfold project only
//	 */
//	private Boolean curvature = true;
//	
	
	@Override
	public Boolean hasCurvature() {
		return true;
	}
	
	@Override
	public void setCurvature(Boolean c) {
//		curvature = c;
	}
	
	@Override
	protected CPMVertex getThis() {
		return this;
	}
	
	@Override
	public Point4d getXYZW() {
		return pos4d;
	}

	@Override
	public void setXYZW(Point4d p) {
		pos4d.set(p);
	}

	@Override
	public void setXY(Point2d p) {
		pos2d.set(p);
	}

	@Override
	public Point2d getXY() {
		return pos2d;
	}

	@Override
	public Double getRadius() {
		if (radius == null)
			radius = 100.0;
		return radius;
	}

	@Override
	public void setRadius(Double radius) {
		this.radius = radius;
	}

	@Override
	public String toString() {
		return super.toString() + " radius = " + radius;
	}
	
}
