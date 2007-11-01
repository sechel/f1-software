package circlepatterns.layout;

import static java.lang.Math.PI;
import halfedge.Edge;
import halfedge.Face;
import halfedge.Vertex;
import halfedge.decorations.HasCapitalPhi;
import halfedge.decorations.HasRadius;
import halfedge.decorations.HasRho;
import halfedge.decorations.HasTheta;
import halfedge.decorations.HasXY;

import javax.vecmath.Point2d;

import circlepatterns.layout.CPLayout.Rotation;
import circlepatterns.math.CPMath;
import de.jtem.mfc.field.Complex;
import de.jtem.mfc.geometry.ComplexProjective1;
import de.jtem.mfc.group.Moebius;

public class EuclideanRotation 
<
	V extends Vertex<V, E, F> & HasXY,
	E extends Edge<V, E, F> & HasTheta,
	F extends Face<V, E, F> & HasRho & HasXY & HasRadius & HasCapitalPhi
> implements Rotation<V, E, F> {

	
	public Point2d rotate(Point2d p, Point2d center, Double phi, Double logScale) {
		Moebius rot = new Moebius();
		Complex c1 = new Complex(center.x, center.y);
		Complex c2 = new Complex(1,0);
		ComplexProjective1 c = new ComplexProjective1(c1, c2);
		
		rot.assignEuclideanLogScaleRotation(c, logScale, phi);
		Complex pc = new Complex(p.x, p.y);
		pc = rot.applyTo(pc);
		return new Point2d(pc.re, pc.im);
	}

	
	public double getPhi(E edge) {
		double theta = edge.getTheta();
		double thStar = PI - theta;
		if (edge.getLeftFace() == null || edge.getRightFace() == null)
			return thStar;
		double leftRho = edge.getLeftFace().getRho();
		double rightRho = edge.getRightFace().getRho();
		double p = CPMath.p(thStar, rightRho - leftRho);
		return 0.5*(p + thStar);
	}

	
	public Double getRadius(Double rho) {
		return Math.exp(rho);
	}
	
	
}
