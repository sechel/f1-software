package koebe;

import halfedge.HalfEdgeDataStructure;
import halfedge.decorations.HasXYZW;
import halfedge.io.HESerializableReader;
import halfedge.surfaceutilities.SurfaceException;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Random;

import javax.vecmath.Point3d;
import javax.vecmath.Point4d;

import junit.framework.TestCase;
import koebe.KoebePolyhedron.KoebePolyhedronContext;
import koebe.PolyederNormalizer.PolyederOptimizable;
import math.optimization.NotConvergentException;
import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.Vector;
import util.debug.DBGTracer;
import circlepatterns.graph.CPEdge;
import circlepatterns.graph.CPFace;
import circlepatterns.graph.CPVertex;

public class TestPolyederNormalizer extends TestCase {

	public static final Double
		eps = 1E-5,
		error = 1E-2;
	
	
	private HalfEdgeDataStructure<CPVertex, CPEdge, CPFace> getTestInstance(){
		InputStream in = getClass().getResourceAsStream("../data/fussball.heds");
		HESerializableReader<CPVertex, CPEdge, CPFace> reader;
		try {
			reader = new HESerializableReader<CPVertex, CPEdge, CPFace>(in);
			HalfEdgeDataStructure<CPVertex, CPEdge, CPFace> result = reader.readHalfEdgeDataStructure();
			reader.close();
			return result;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
	
	private KoebePolyhedronContext<CPVertex, CPEdge, CPFace> makeContext(){
		HalfEdgeDataStructure<CPVertex, CPEdge, CPFace> g = getTestInstance();
		KoebePolyhedronContext<CPVertex, CPEdge, CPFace> context = null;
		try {
			context = KoebePolyhedron.contructKoebePolyhedron(g, 1E-4, 20);
		} catch (SurfaceException e) {
			e.printStackTrace();
		}
		return context;
	}
	
	
	public void testPolyederNormalizer() throws Exception {
		KoebePolyhedronContext<CPVertex, CPEdge, CPFace> context = makeContext();
		DBGTracer.msg("Center is: " + baryCenter(context.polyeder));
		try{
			PolyederNormalizer.normalize(context);
		} catch (NotConvergentException nce){
			DBGTracer.msg(nce.getMessage());
			assertTrue(false);
		}
		Point3d zero = new Point3d();
		DBGTracer.msg("Center is: " + baryCenter(context.polyeder));
		assertTrue(zero.distance(baryCenter(context.polyeder)) <= error);
	}
	
	
	private Point3d baryCenter(HalfEdgeDataStructure<? extends HasXYZW, ?, ?> graph){
		// compiler bug hack try with a later version
		List<? extends HasXYZW> vertices = graph.getVertices();
		Point3d zero = new Point3d(0,0,0);
		for (HasXYZW v : vertices){
			Point4d p = v.getXYZW();
			zero.x += p.x / p.w;
			zero.y += p.y / p.w;
			zero.z += p.z / p.w;
		}
		zero.scale(1.0 / graph.getNumVertices());
		return zero;
	}
	
	
	
	public void testFunctionalGradient() throws Exception {
		KoebePolyhedronContext<CPVertex, CPEdge, CPFace> context = makeContext();
		PolyederOptimizable<CPVertex, CPEdge, CPFace> opt = new PolyederOptimizable<CPVertex, CPEdge, CPFace>(context);
		
		Random rnd = new Random();
		rnd.setSeed(0);
		
		Vector p = new DenseVector(opt.getDomainDimension());
		for (int i = 0; i < p.size(); i++)
			p.set(i, rnd.nextDouble() / 2);
		
		Vector grad = new DenseVector(opt.getDomainDimension());
		Double zeroValue = opt.evaluate(p, grad);
		
		
		double[] testGrad = new double[opt.getDomainDimension()];
		for (int i = 0; i < opt.getDomainDimension(); i++){
			p.add(i, eps);
			testGrad[i] = zeroValue / -eps - opt.evaluate(p) / -eps;
			p.add(i, -eps);
			assertEquals(testGrad[i], grad.get(i), error);
		}
	}
	
	
	public void testFunctionalHessian() throws Exception {
		KoebePolyhedronContext<CPVertex, CPEdge, CPFace> context = makeContext();
		PolyederOptimizable<CPVertex, CPEdge, CPFace> opt = new PolyederOptimizable<CPVertex, CPEdge, CPFace>(context);
		
		Random rnd = new Random();
		rnd.setSeed(0);
		
		Vector x = new DenseVector(opt.getDomainDimension());
		for (int i = 0; i < x.size(); i++)
			x.set(i, rnd.nextDouble() / 2);
		
		double epseps = eps * eps;
		Matrix hessian = new DenseMatrix(opt.getDomainDimension(), opt.getDomainDimension());
		Double sampleValue = opt.evaluate(x, hessian);
		double[][] testMatrix = new double[opt.getDomainDimension()][opt.getDomainDimension()];
		for (int i = 0; i < opt.getDomainDimension(); i++){
			for (int j = 0; j < opt.getDomainDimension(); j++){
				x.add(i, eps);
				Double sampleValueI = opt.evaluate(x);
				x.add(j, eps);
				Double sampleValueIJ = opt.evaluate(x);				
				x.add(i, -eps);
				Double sampleValueJ = opt.evaluate(x);
				x.add(j, -eps);			

				double testHessian =  sampleValueIJ/epseps - sampleValueI/epseps
					- sampleValueJ/epseps + sampleValue/epseps;
				testMatrix[i][j] = testHessian;
				assertEquals(hessian.get(i,j), testHessian, error);
			}
		}
	}
	
	
	
}
