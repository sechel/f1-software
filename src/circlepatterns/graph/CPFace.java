package circlepatterns.graph;

import halfedge.Face;
import halfedge.decorations.HasCapitalPhi;
import halfedge.decorations.HasGradientValue;
import halfedge.decorations.HasLabel;
import halfedge.decorations.HasRadius;
import halfedge.decorations.HasRho;
import halfedge.decorations.HasXY;
import halfedge.decorations.HasXYZW;

import java.util.List;

import javax.vecmath.Point2d;
import javax.vecmath.Point4d;


/**
 * The face class for the koebe project
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 */
public class CPFace extends Face<CPVertex, CPEdge, CPFace> implements HasGradientValue, HasRho, HasLabel, HasRadius, HasXY, HasXYZW, HasCapitalPhi{

	private static final long 
		serialVersionUID = 1L;
	private Double
		rho = 0.0,
		gradientValue = 0.0,
		radius = 0.0,
		capitalPhi = 2 * Math.PI;
	
	private Point2d 
		coord = new Point2d();
	private Point4d
		coord4d = new Point4d(0, 0, 0, 1);
	private Boolean
		label = false;
	
//	@Override
//	protected Object clone() throws CloneNotSupportedException {
//		CPFace clone = (CPFace)super.clone();
//		clone.rho = rho;
//		clone.gradientValue = gradientValue;
//		clone.radius = radius;
//		clone.capitalPhi = capitalPhi;
//		clone.coord = new Point2d(coord);
//		clone.coord4d = new Point4d(coord4d);
//		clone.label = new Boolean(label);
//		return clone;
//	}
	
	@Override
	protected CPFace getThis() {
		return this;
	}
	
	@Override
	public Double getGradientValue() {
		return gradientValue;
	}

	@Override
	public void setGradientValue(Double gradientValue) {
		this.gradientValue = gradientValue;
	}

	@Override
	public Double getRho() {
		return rho;
	}

	@Override
	public void setRho(Double rho) {
		this.rho = rho;
	}
	
	
	@Override
	public String toString() {
		return super.toString();// + ": rho=" + rho;
	}

	@Override
	public void setRadius(Double r) {
		radius = r;
	}

	@Override
	public Double getRadius() {
		return radius;
	}

	@Override
	public void setXY(Point2d p) {
		getXY().set(p);
	}

	@Override
	public Point2d getXY() {
		if (coord == null)
			coord = new Point2d();
		return coord;
	}


	@Override
	public Point4d getXYZW() {
		if (coord4d == null)
			coord4d = new Point4d();
		return coord4d;
	}
	
	@Override
	public void setXYZW(Point4d p) {
		getXYZW().set(p);
	}

	@Override
	public Boolean getLabel() {
		if (label == null) label = false;
		return label;
	}

	@Override
	public void setLabel(Boolean label) {
		this.label = label;
	}

	@Override
	public Double getCapitalPhi() {
		if (capitalPhi == null)
			capitalPhi = 2 * Math.PI;
		return capitalPhi;
	}

	@Override
	public void setCapitalPhi(Double capitalPhi) {
		this.capitalPhi = capitalPhi;
	}
	
	
	public boolean isDegenerate(double threshold) {
		List<CPEdge> b = getBoundary();
		for (CPEdge edge : b) {
			CPVertex v1 = edge.getStartVertex();
			CPVertex v2 = edge.getTargetVertex();
			double length = v1.getXYZW().distance(v2.getXYZW());
			if (length < threshold)
				return true;
		}
		
		return false;
	}
	
	
}
