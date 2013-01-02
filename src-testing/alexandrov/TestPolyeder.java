package alexandrov;

import halfedge.HalfEdgeDataStructure;
import halfedge.triangulationutilities.Delaunay;
import junit.framework.TestCase;
import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.Vector;
import no.uib.cipr.matrix.Vector.Norm;
import util.TestData;
import util.debug.DBGTracer;
import alexandrov.graph.CPMEdge;
import alexandrov.graph.CPMFace;
import alexandrov.graph.CPMVertex;
import alexandrov.math.CPMCurvatureFunctional;

public class TestPolyeder extends TestCase {

	public static final Double
		eps = 1E-10,
		error = 1E-4;
	
	
	public void testJacobian() throws Exception {
		HalfEdgeDataStructure<CPMVertex, CPMEdge, CPMFace> graph = TestData.getTestGraph("flippyramide.cpm");
	
		double start = 10;
		for (CPMVertex v : graph.getVertices()){
			v.setRadius(start += 0.1);
		}
		Matrix Dk = CPMCurvatureFunctional.getCurvatureDerivative(graph);
		for (CPMVertex v : graph.getVertices()){
			int i = v.getIndex();
			Vector k = CPMCurvatureFunctional.getCurvature(graph);
			v.setRadius(v.getRadius() + eps);
			Vector deltak = CPMCurvatureFunctional.getCurvature(graph);
			Vector dkdri = k.add(-1, deltak).scale(1 / -eps);
			for (int j = 0; j < graph.getNumVertices(); j++){
				assertEquals(dkdri.get(j), Dk.get(j, i), error);
			}
			v.setRadius(v.getRadius() - eps);
		}
	}
	
	
	public void testloopJacobian() throws Exception {
		Double error = 1E-1; // numerical problems
		HalfEdgeDataStructure<CPMVertex, CPMEdge, CPMFace> graph = TestData.getTestGraph("looppyramide.cpm");
		Delaunay.constructDelaunay(graph);
		
		double start = 10;
		for (CPMVertex v : graph.getVertices()){
			v.setRadius(start += 0.1);
		}
		Matrix Dk = CPMCurvatureFunctional.getCurvatureDerivative(graph);
		for (CPMVertex v : graph.getVertices()){
			int i = v.getIndex();
			Vector k = CPMCurvatureFunctional.getCurvature(graph);
			v.setRadius(v.getRadius() + eps);
			Vector deltak = CPMCurvatureFunctional.getCurvature(graph);
			Vector dkdri = k.add(-1, deltak).scale(1 / -eps);
			for (int j = 0; j < graph.getNumVertices(); j++){
				assertEquals(dkdri.get(j), Dk.get(j, i), error);
			}
			v.setRadius(v.getRadius() - eps);
		}
	}
	
	
	public void testPolyeder() throws Exception {
		HalfEdgeDataStructure<CPMVertex, CPMEdge, CPMFace> graph = TestData.getTestGraph("flippyramide.cpm");
		Alexandrov.constructPolyhedron(graph, 2.0, 1E-10, 30, null);
		Vector kappa = CPMCurvatureFunctional.getCurvature(graph);
		DBGTracer.msg("End error: " + kappa.norm(Norm.Two));
		assertTrue(kappa.norm(Norm.Two) <= error);
		
		DBGTracer.msg("Radii:");
		for (CPMVertex v : graph.getVertices())
			DBGTracer.msg(v.getIndex() + ": " + v.getRadius());
	}
	
	
	
}
