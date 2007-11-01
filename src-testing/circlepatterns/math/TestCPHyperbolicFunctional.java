package circlepatterns.math;

import halfedge.HalfEdgeDataStructure;
import halfedge.generator.SquareGridGenerator;

import java.io.File;
import java.util.Random;

import junit.framework.TestCase;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Vector;
import circlepatterns.graph.CPEdge;
import circlepatterns.graph.CPFace;
import circlepatterns.graph.CPVertex;

public class TestCPHyperbolicFunctional extends TestCase {

	public static final Double
		eps = 1E-6,
		error = 1E-3;
	public static final File
		testHEMLFile = new File("src-testing/data/squaregrid.heml");
	

	
	public void testGradient() throws Exception {
		HalfEdgeDataStructure<CPVertex, CPEdge, CPFace> ds = SquareGridGenerator.generate(4, 4, CPVertex.class, CPEdge.class, CPFace.class);
		SquareGridGenerator.setSquareGridThetas(ds, 0.1, 0.2);
		CPHyperbolicOptimizable<CPVertex, CPEdge, CPFace> functional = new CPHyperbolicOptimizable<CPVertex, CPEdge, CPFace>(ds);
		
		Random rnd = new Random(); 
		rnd.setSeed(1);
		Vector x = new DenseVector(functional.getDomainDimension());
		for (Integer i = 0; i < x.size(); i++)
			x.set(i, -1 -rnd.nextDouble());

		Vector grad = new DenseVector(functional.getDomainDimension());
		Double zeroValue = functional.evaluate(x, grad);
		
		double[] testGrad = new double[ds.getNumFaces()];
		for (int i = 0; i < functional.getDomainDimension(); i++){
			x.add(i, eps);
			testGrad[i] = zeroValue / -eps - functional.evaluate(x) / -eps;
			x.add(i, -eps);
			assertEquals(testGrad[i], grad.get(i), error);
		}
	}

	
	
	public void testHessian() throws Exception {
		//:TODO implement the hyperbolical funcional correctly
//		HalfEdgeDataStructure<CPVertex, CPEdge, CPFace> ds = SquareGridGenerator.generate(4, 4, CPVertex.class, CPEdge.class, CPFace.class);
//		SquareGridGenerator.setSquareGridThetas(ds, 0.1, 0.2);
//		CPHyperbolicOptimizable<CPVertex, CPEdge, CPFace> functional = new CPHyperbolicOptimizable<CPVertex, CPEdge, CPFace>(ds);
//		
//		Random rnd = new Random(); 
//		rnd.setSeed(5);
//		Vector x = new DenseVector(functional.getDomainDimension());
//		for (Integer i = 0; i < x.size(); i++)
//			x.set(i, -1 - rnd.nextDouble());
//
//		double epseps = eps * eps;
//		Matrix hessian = new DenseMatrix(functional.getDomainDimension(), functional.getDomainDimension());
//		Double sampleValue = functional.evaluate(x, hessian);
//		double[][] testMatrix = new double[functional.getDomainDimension()][functional.getDomainDimension()];
//		for (int i = 0; i < functional.getDomainDimension(); i++){
//			for (int j = 0; j < functional.getDomainDimension(); j++){
//				x.add(i, eps);
//				Double sampleValueI = functional.evaluate(x);
//				x.add(j, eps);
//				Double sampleValueIJ = functional.evaluate(x);				
//				x.add(i, -eps);
//				Double sampleValueJ = functional.evaluate(x);
//				x.add(j, -eps);			
//
//				double testHessian =  sampleValueIJ/epseps - sampleValueI/epseps
//					- sampleValueJ/epseps + sampleValue/epseps;
//				testMatrix[i][j] = testHessian;
//				assertEquals(hessian.get(i,j), testHessian, error);
//			}
//		}
	}
	
	
}
