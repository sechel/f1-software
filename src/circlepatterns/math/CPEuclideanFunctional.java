package circlepatterns.math;

import halfedge.Edge;
import halfedge.Face;
import halfedge.HalfEdgeDataStructure;
import halfedge.Vertex;
import halfedge.decorations.HasCapitalPhi;
import halfedge.decorations.HasGradientValue;
import halfedge.decorations.HasRho;
import halfedge.decorations.HasTheta;
import math.util.Clausen;


/**
 * The functional to be minimized for koebes polyhedron
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 * @see koebe.KoebePolyhedron
 * @see <br><a href="http://opus.kobv.de/tuberlin/volltexte/2003/668/">Variational principles for circle patterns</a>
 */
public class CPEuclideanFunctional {

	
	/**
	 * Evaluates the euclidean functional and sets the gradient
	 * @param graph
	 * @return the value
	 */
	public static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F> & HasTheta,
		F extends Face<V, E, F> & HasRho & HasGradientValue & HasCapitalPhi
	> Double evaluate(HalfEdgeDataStructure<V, E, F> graph) {
		Double result = 0.0;
		for (F face : graph.getFaces()) {
			face.setGradientValue(face.getCapitalPhi());
			result += face.getCapitalPhi() * face.getRho();
		}

		for (E edge : graph.getEdges()) {
			F leftFace = edge.getLeftFace();
			if (leftFace == null) {
				continue;
			}
			Double theta = edge.getTheta();
			Double thStar = Math.PI - theta;
			Double leftRho = leftFace.getRho();
			F rightFace = edge.getOppositeEdge().getLeftFace();
			if (rightFace == null) {
				// boundary face
				result -= 2 * thStar * leftRho;
				Double leftGradientValue = leftFace.getGradientValue();
				leftFace.setGradientValue(leftGradientValue - 2 * thStar);
			} else {
				// interior face
				Double rightRho = rightFace.getRho();
				Double diffRho = rightRho - leftRho;
				Double p = p(thStar, diffRho);
				result += 0.5 * p * diffRho;
				result += Clausen.valueAt(thStar + p);
//				result -= 0.5*Clausen.valueAt(2*thStar);
				result -= thStar * leftRho;
				Double leftGradientValue = leftFace.getGradientValue();
				leftFace.setGradientValue(leftGradientValue - p - thStar);
			}

		}

		return result;
	}

    private static Double p(Double thStar, Double diffRho) {
        Double exp = Math.exp(diffRho);
        Double tanhDiffRhoHalf = (exp - 1.0) / (exp + 1.0);
        return  2.0 * Math.atan(Math.tan(0.5 * thStar) * tanhDiffRhoHalf);
    }
    
	
}
