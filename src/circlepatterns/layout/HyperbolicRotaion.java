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

public class HyperbolicRotaion 
<
	V extends Vertex<V, E, F> & HasXY,
	E extends Edge<V, E, F> & HasTheta,
	F extends Face<V, E, F> & HasRho & HasXY & HasRadius & HasCapitalPhi
> implements Rotation<V, E, F> {

	@Override
	public Point2d rotate(Point2d p, Point2d center, Double phi, Double scale) {
		return null;
	}

	
	@Override
	public double getPhi(E edge) {
		double theta = edge.getTheta();
		double leftRho = edge.getLeftFace().getRho();
		double rightRho = edge.getRightFace().getRho();
		double thStar = PI - theta;
		double p = CPMath.p(thStar, rightRho - leftRho);
		double s = CPMath.p(theta, leftRho + rightRho);
		return 0.5*(p - s);
	}


	@Override
	public Double getRadius(Double rho) {
		Double er = Math.exp(rho);
		return Math.log((1 + er) / (1 - er));
	}
	
	
}
