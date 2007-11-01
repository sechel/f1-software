package circlepatterns.math;

import halfedge.Edge;
import halfedge.Face;
import halfedge.HalfEdgeDataStructure;
import halfedge.Vertex;
import halfedge.decorations.HasGradientValue;
import halfedge.decorations.HasRho;
import halfedge.decorations.HasTheta;
import math.util.Clausen;

public class CPHyperbolicFunctional {

	
    public static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F> & HasTheta,
		F extends Face<V, E, F> & HasRho & HasGradientValue
	>  double evaluate(HalfEdgeDataStructure<V, E, F> graph) {
		double value = 0.0;
		
		/* initialize gradient */
		for (F face : graph.getFaces()) {
			face.setGradientValue(2 * Math.PI);
			value += 2 * Math.PI * face.getRho();
		}
		
		/* loop over edges */
		for (E e : graph.getPositiveEdges()) {
			
			/* non-oriented index of left and right face */
			F leftFace  = e.getLeftFace();
			F rightFace = e.getRightFace();
			
			final double th = e.getTheta();
			final double thStar = Math.PI - th;
			final double leftRho = leftFace == null ? 0.0 : leftFace.getRho();
			final double rightRho = rightFace == null ? 0.0 : rightFace.getRho();
			final double diffRho = rightRho - leftRho;
			final double sumRho  = rightRho + leftRho;
			final double p = p(thStar, diffRho);
			final double s = p(thStar, sumRho);
			
			value += p * diffRho;
			value += Clausen.valueAt(thStar + p);
			value += Clausen.valueAt(thStar - p);
			
			value += s * sumRho;
			value += Clausen.valueAt(thStar + s);
			value += Clausen.valueAt(thStar - s);
			
			if (leftFace != null)
				leftFace.setGradientValue(leftFace.getGradientValue() - (p - s));
			if (rightFace != null)
				rightFace.setGradientValue(rightFace.getGradientValue() - (-p - s));
		}
		
		return value;
	}
	
	
    private static Double p(Double thStar, Double diffRho) {
        Double exp = Math.exp(diffRho);
        Double tanhDiffRhoHalf = (exp - 1.0) / (exp + 1.0);
        return  2.0 * Math.atan(Math.tan(0.5 * thStar) * tanhDiffRhoHalf);
    }
    
    
}
