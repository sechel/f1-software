package halfedge.io;

import halfedge.HalfEdgeDataStructure;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import math.optimization.NotConvergentException;
import math.optimization.newton.NewtonOptimizer;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Vector;
import util.debug.DBGTracer;
import circlepatterns.graph.CPEdge;
import circlepatterns.graph.CPFace;
import circlepatterns.graph.CPVertex;
import circlepatterns.math.CPEuclideanOptimizable;

public class HEMLReaderTest {

	
	public static void main(String[] args) {
		File testFile = new File(args[0]);
		HEMLReader<CPVertex, CPEdge, CPFace> hemlreader = HEMLReader.createHEMLReader(CPVertex.class, CPEdge.class, CPFace.class);
		HalfEdgeDataStructure<CPVertex, CPEdge, CPFace> graph = null;
		if (hemlreader != null){
			try {
				graph = hemlreader.readHEML(testFile);
				System.out.println(graph.toString());
			} catch (GraphReaderException e) {
				e.printStackTrace();
				return;
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				return;
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
		}
		DBGTracer.msg("\nPositive Edges: -----------------------");
		for (CPEdge posEdge : graph.getPositiveEdges())
			DBGTracer.msg(posEdge.toString());
		DBGTracer.msg("Negative Edges: -----------------------");
		for (CPEdge posEdge : graph.getNegativeEdges())
			DBGTracer.msg(posEdge.toString());		
		
		CPEuclideanOptimizable<CPVertex, CPEdge, CPFace> functional = new CPEuclideanOptimizable<CPVertex, CPEdge, CPFace>(graph);
		System.out.println("Minimizing...");
		NewtonOptimizer minimizer = new NewtonOptimizer();
		minimizer.setError(1E-5);
		minimizer.setMaxIterations(1000);
		Vector guess = new DenseVector(functional.getDomainDimension());
		try {
			minimizer.minimize(guess, functional);
		} catch (NotConvergentException e) {
			DBGTracer.msg("Not Convergent!");
		}
		System.out.println("done...");
		Double result = functional.evaluate(guess);
		System.out.println("Functional=" + result);	
//		System.out.println("Rohs: \n" + functional.getGuess().getValue());
	}

	
}
