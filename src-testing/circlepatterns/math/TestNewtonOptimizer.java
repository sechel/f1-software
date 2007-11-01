package circlepatterns.math;

import halfedge.HalfEdgeDataStructure;
import halfedge.generator.SquareGridGenerator;
import halfedge.io.HEMLReader;
import halfedge.io.HESerializableReader;

import java.io.File;
import java.io.InputStream;
import java.util.Random;

import junit.framework.TestCase;
import math.optimization.NotConvergentException;
import math.optimization.Optimizer;
import math.optimization.newton.NewtonOptimizer;
import math.optimization.stepcontrol.ArmijoStepController;
import math.optimization.stepcontrol.ShortGradientStepController;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Vector;
import no.uib.cipr.matrix.Vector.Norm;
import util.debug.DBGTracer;
import circlepatterns.CirclePattern;
import circlepatterns.graph.CPEdge;
import circlepatterns.graph.CPFace;
import circlepatterns.graph.CPVertex;

public class TestNewtonOptimizer extends TestCase {

	public static final File
		testHEMLFile = new File("src-testing/data/threefaces.heml");
	private static long
		randomSeed = 0;
	private final Double
		error = 1E-8;
	private final Integer
		maxIterations = 20;
	
	public void testEuclideanOptimizerFromGenerator() throws Exception {
		HalfEdgeDataStructure<CPVertex, CPEdge, CPFace> ds = SquareGridGenerator.generate(10, 10, CPVertex.class, CPEdge.class, CPFace.class);
		SquareGridGenerator.setSquareGridThetas(ds, 0.1, 0.2);
		testEuclidean(ds, true);
	}
	
	public void testEuclideanOptimizerFromFile() throws Exception {
		HEMLReader<CPVertex, CPEdge, CPFace> reader = HEMLReader.createHEMLReader(CPVertex.class, CPEdge.class, CPFace.class);
		HalfEdgeDataStructure<CPVertex, CPEdge, CPFace> ds = reader.readHEML(testHEMLFile);
		testEuclidean(ds, false);
	}

	
	private void testEuclidean(HalfEdgeDataStructure<CPVertex, CPEdge, CPFace> ds, boolean random){
		CPEuclideanOptimizable<CPVertex, CPEdge, CPFace> func = new CPEuclideanOptimizable<CPVertex, CPEdge, CPFace>(ds);
		Vector guess = new DenseVector(func.getDomainDimension());
		if (random){		
			Random rnd = new Random(); 
			rnd.setSeed(randomSeed);
			for (Integer i = 0; i < guess.size(); i++)
				guess.set(i, rnd.nextDouble());
		}
		Optimizer optimizer = new NewtonOptimizer();
		optimizer.setError(error);
		optimizer.setMaxIterations(maxIterations);
		optimizer.setStepController(new ArmijoStepController());
		try {
			optimizer.minimize(guess, func);
		} catch (NotConvergentException e) {
			assertTrue(false);
		}
		Vector gradient = new DenseVector(func.getDomainDimension());
		func.evaluate(guess, gradient);
		assertTrue(gradient.norm(optimizer.getNorm()) < error);
	}
	
	
	public void testSphericalOptimizerFromFile() throws Exception {
		// read graph
		DBGTracer.setActive(true);
//		InputStream in = getClass().getResourceAsStream("../../data/tetraeder.heds");
		InputStream in = getClass().getResourceAsStream("../../data/medialLatticeRaw.heds");
		HESerializableReader<CPVertex, CPEdge, CPFace> reader = new HESerializableReader<CPVertex, CPEdge, CPFace>(in);
		HalfEdgeDataStructure<CPVertex, CPEdge, CPFace> graph = reader.readHalfEdgeDataStructure();
		
		// compute rhos
		ShortGradientStepController stepC = new ShortGradientStepController();
		stepC.setAlpha(0.5);
		CirclePattern.computeSpherical(graph, new CPSphericalFunctional.FlagGuess(), stepC, error, maxIterations, null);
		
		// verify result
		CPSphericalOptimizable<CPVertex, CPEdge, CPFace> func = new CPSphericalOptimizable<CPVertex, CPEdge, CPFace>(graph,  new CPSphericalFunctional.FlagGuess());
		Vector x = new DenseVector(func.getDomainDimension());
		
		for (CPFace f : graph.getFaces()) {
			x.set(f.getIndex(), f.getRho());
		}
		Vector gradient = new DenseVector(func.getDomainDimension());
		func.evaluate(x, gradient);
		
		for (CPFace f : graph.getFaces()) {
			System.err.println("radius " + f.getIndex() + ": " + f.getRho());
		}
		assertTrue(gradient.norm(Norm.Two) < error);
	}
	
	
	
	
}
