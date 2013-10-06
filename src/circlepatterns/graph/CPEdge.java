package circlepatterns.graph;

import static java.lang.Math.PI;

import javax.vecmath.Point4d;

import halfedge.Edge;
import halfedge.decorations.HasLabel;
import halfedge.decorations.HasTheta;
import halfedge.decorations.HasXYZW;


/**
 * The edge class for the koebe project
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 */
public class CPEdge extends Edge<CPVertex, CPEdge, CPFace> implements HasXYZW, HasTheta, HasLabel{

	private static final long 
		serialVersionUID = 1L;
	private Double
		theta = PI / 2;
	private Boolean
		label = false;
	private Point4d
		coord4d = new Point4d(0,0,0,1);
	
	
//	@Override
//	protected Object clone() throws CloneNotSupportedException {
//		CPEdge clone = (CPEdge)super.clone();
//		clone.theta = theta;
//		clone.label = label;
//		return clone;
//	}
	
	
	@Override
	public Double getTheta() {
		return theta;
	}

	@Override
	public void setTheta(Double theta) {
		this.theta = theta;
		if (oppositeEdge != null)
			oppositeEdge.theta = theta;
	}
	
	@Override
	public String toString() {
		return super.toString();
	}

	@Override
	protected CPEdge getThis() {
		return this;
	}

	@Override
	public Boolean getLabel() {
		if (label == null)
			label = false;
		return label;
	}

	@Override
	public void setLabel(Boolean label) {
		this.label = label;
	}
	
	public Point4d getXYZW() {
		return coord4d;
	}
	public void setXYZW(Point4d coord4d) {
		this.coord4d.set(coord4d);
	}
	
	
}
