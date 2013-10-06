package circlepatterns.math;

import static java.lang.Math.PI;
import static java.lang.Math.atan;
import static java.lang.Math.exp;
import static java.lang.Math.sqrt;
import static java.lang.Math.tan;
import halfedge.Edge;
import halfedge.Face;
import halfedge.HalfEdgeDataStructure;
import halfedge.Vertex;
import halfedge.decorations.HasCapitalPhi;
import halfedge.decorations.HasGradientValue;
import halfedge.decorations.HasRho;
import halfedge.decorations.HasTheta;
import halfedge.io.HESerializableReader;

import java.io.InputStream;

import math.util.Clausen;
import circlepatterns.graph.CPEdge;
import circlepatterns.graph.CPFace;
import circlepatterns.graph.CPVertex;
import de.jreality.geometry.CoordinateSystemFactory;
import de.jreality.geometry.QuadMeshFactory;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.ui.viewerapp.ViewerApp;

public class CPSphericalFunctional {
	
	
	public static interface SpecialEdgeFlag{
		public Boolean specialEdge(Edge<?, ?, ?> edge);
	}
	
	
	public static class FlagFalse implements SpecialEdgeFlag{
		@Override
		public Boolean specialEdge(Edge<?, ?, ?> edge) {
			return false;
		}
		
	}
	
	public static class FlagGuess implements SpecialEdgeFlag{
		@Override
		public Boolean specialEdge(Edge<?, ?, ?> edge) {
			if (!edge.isInteriorEdge())
				return false;
			if (	(!edge.getNextEdge().isInteriorEdge() && !edge.getOppositeEdge().getPreviousEdge().isInteriorEdge()) 
				 || (!edge.getPreviousEdge().isInteriorEdge() && !edge.getOppositeEdge().getNextEdge().isInteriorEdge()))
				return true;
			return false;
		}
	}
	
	
    public static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F> & HasTheta,
		F extends Face<V, E, F> & HasRho & HasGradientValue & HasCapitalPhi
	>  double evaluate(HalfEdgeDataStructure<V, E, F> graph, SpecialEdgeFlag flag) {
		double value = 0.0;
		
		/* initialize gradient */
		for (F face : graph.getFaces()) {
			face.setGradientValue(face.getCapitalPhi());
			value += face.getCapitalPhi() * face.getRho();
		}
		
		/* loop over edges */
		for (E e : graph.getPositiveEdges()) {
			
			/* non-oriented index of left and right face */
			final F leftFace  = e.getLeftFace();
			final F rightFace = e.getRightFace();
			
			final double th = e.getTheta();
			final double thStar = PI - th;
			final double leftRho = leftFace == null ? 0.0 : leftFace.getRho();
			final double rightRho = rightFace == null ? 0.0 : rightFace.getRho();
			final double diffRho = rightRho - leftRho;
			final double sumRho = rightRho + leftRho;
			final double p = p(thStar, diffRho);
			final double s = p(th, sumRho);
			
			Double edgeValue = 0.0;
			edgeValue += p * diffRho;
			edgeValue += Clausen.valueAt(thStar + p);
			edgeValue += Clausen.valueAt(thStar - p);
//			edgeValue -= Clausen.valueAt(2 * thStar);
			
			edgeValue -= s * sumRho;
			edgeValue -= Clausen.valueAt(th + s);
			edgeValue -= Clausen.valueAt(th - s);
//			edgeValue += Clausen.valueAt(2 * th);
			edgeValue -= PI * sumRho;
			
			Double lambda = flag.specialEdge(e) ? 0.5 : 1.0;
			edgeValue *= lambda;
			
			if (leftFace != null)
				leftFace.setGradientValue(leftFace.getGradientValue() - lambda * (p + s + PI));
			if (rightFace != null)
				rightFace.setGradientValue(rightFace.getGradientValue() - lambda * (-p + s + PI));
			
			value += edgeValue;
		}
		
		return value;
	}

	
    private static Double p(Double thStar, Double diffRho) {
        Double exp = exp(diffRho);
        Double tanhDiffRhoHalf = (exp - 1.0) / (exp + 1.0);
        return  2.0 * atan(tan(0.5 * thStar) * tanhDiffRhoHalf);
    }
    
	
    
    
    /**
     * Creates a graph of the functional gradient with respect 
     * to the first two arguments
     * @param args
     */
    public static void main(String[] args) {
//		InputStream cubeIn = CPSphericalFunctional.class.getResourceAsStream("../../data/fussball.heds");
		InputStream in = CPSphericalFunctional.class.getResourceAsStream("../../data/medialLatticeRaw.heds");
		HESerializableReader<CPVertex, CPEdge, CPFace> reader;
		HalfEdgeDataStructure<CPVertex, CPEdge, CPFace> graph = null;
		try {
			reader = new HESerializableReader<CPVertex, CPEdge, CPFace>(in);
			graph = reader.readHalfEdgeDataStructure();
		} catch (Exception e1) {
			e1.printStackTrace();
			return;
		}
		
//		// set thetas
//		for (CPEdge e: graph.getEdges())
//			e.setTheta(2*PI/3);
//    	
//		CPSphericalOptimizable<CPVertex, CPEdge, CPFace> func = new CPSphericalOptimizable<CPVertex, CPEdge, CPFace>(graph, new FlagFalse());
//		Vector initGuess = new DenseVector(func.getDomainDimension());
//		for (int i = 0; i < initGuess.size(); i++)
//			initGuess.set(i, 0.5);
		
//		NewtonOptimizer optimizer = new NewtonOptimizer();
//		optimizer.setSolver(Solver.BiCGstab);
//		optimizer.setError(1E-4);
//		optimizer.setMaxIterations(20);
//		try {
//			optimizer.minimize(initGuess, func);
//		} catch (Exception e1) {
//			CPTestSuite.showError(e1.toString());
//			return;
//		}
		
		
		for (CPFace f : graph.getFaces())
			f.setRho(-1.0);
		
		FlagFalse flag = new FlagFalse();
		int numSteps = 100;
		double max = 2.0;
		double[][] values = new double[numSteps*numSteps][3];
		double stepSize = max / numSteps;
		int counter = 0;
		for (double x = stepSize; x <= max; x += stepSize) {
			for (double y = stepSize; y <= max; y += stepSize) {
				double rho1 = Math.log(Math.tan(x/2));
				double rho2 = Math.log(Math.tan(y/2));
				graph.getFace(4).setRho(rho1);
				graph.getFace(6).setRho(rho2);
				evaluate(graph, flag);
				values[counter][0] = x;
				values[counter][1] = y;
				values[counter][2] = getGradientLength(graph);
				counter++;
			}
		}
		
		
//		// write result to file
//		File file = new File("data/sphericalFunctionalGraph.graph");
//		try {
//			BufferedWriter out = new BufferedWriter(new FileWriter(file));
//			for (int j = 0; j < values.length; j++)
//				out.write(values[j][0] + "\t\t\t" + values[j][1] + "\t\t\t" + values[j][2] + "\n");
//		} catch (IOException e1) {
//			e1.printStackTrace();
//			return;
//		}
		QuadMeshFactory qmf = new QuadMeshFactory();
		qmf.setClosedInUDirection(false);
		qmf.setClosedInVDirection(false);
		qmf.setULineCount(values.length / numSteps);
		qmf.setVLineCount(values.length / numSteps);
		qmf.setGenerateVertexNormals(true);
		qmf.setVertexCoordinates(values);
		qmf.update();
		SceneGraphComponent c = new SceneGraphComponent();
		c.setGeometry(qmf.getGeometry());
		CoordinateSystemFactory csf = new CoordinateSystemFactory(PI);
		csf.showAxes(true);
		csf.showBox(true);
		c.addChild(csf.getCoordinateSystem());
		ViewerApp.display(c);
		
		
	}
    
    
    private static <
		F extends Face<?, ?, F> & HasGradientValue
	>  double getGradientLength(HalfEdgeDataStructure<?, ?, F> graph) {
    	double sumSq = 0.0;
    	for (F f : graph.getFaces())
    		sumSq += f.getGradientValue()*f.getGradientValue();
    	return sqrt(sumSq);
    }
    
    
    
	
}
