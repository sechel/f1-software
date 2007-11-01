package alexandrov;

import halfedge.HalfEdgeDataStructure;
import junit.framework.TestCase;
import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.Vector;
import util.TestData;
import alexandrov.graph.CPMEdge;
import alexandrov.graph.CPMFace;
import alexandrov.graph.CPMVertex;
import alexandrov.math.CapCurvatureFunctional;

public class TestCap extends TestCase {

	public static final Double
		eps = 1E-10,
		error = 1E-4;
	
	public void testCurvature() throws Exception {
		HalfEdgeDataStructure<CPMVertex, CPMEdge, CPMFace> graph = TestData.getTestGraph("tetraedercap.cpm");
		double start = 0;
		for (CPMVertex v : graph.getVertices())
			v.setRadius(0.0);
		for (CPMVertex v : CapCurvatureFunctional.getInnerVertices(graph))
				v.setRadius(start += 0.1);
		System.err.println(graph.toString());
		Vector Dk = CapCurvatureFunctional.getCurvature(graph);
		int i = 0;
		for (CPMVertex v : CapCurvatureFunctional.getInnerVertices(graph)){
			double k = CapCurvatureFunctional.getFunctional(graph);
			v.setRadius(v.getRadius() + eps);
			double deltak = CapCurvatureFunctional.getFunctional(graph);
			double dkdri = (k - deltak) / -eps;
			assertEquals(dkdri, Dk.get(i), error);
			v.setRadius(v.getRadius() - eps);
			i++;
		}
	}
	
	
	public void testJacobian() throws Exception {
		HalfEdgeDataStructure<CPMVertex, CPMEdge, CPMFace> graph = TestData.getTestGraph("tetraedercap.cpm");
		double start = 0;
		for (CPMVertex v : graph.getVertices())
			v.setRadius(0.0);
		for (CPMVertex v : CapCurvatureFunctional.getInnerVertices(graph))
			v.setRadius(start += 0.1);
		System.err.println(graph.toString());
		Matrix Dk = CapCurvatureFunctional.getCurvatureDerivative(graph);
		int i = 0;
		for (CPMVertex v : CapCurvatureFunctional.getInnerVertices(graph)){
			Vector k = CapCurvatureFunctional.getCurvature(graph);
			v.setRadius(v.getRadius() + eps);
			Vector deltak = CapCurvatureFunctional.getCurvature(graph);
			Vector dkdri = k.add(-1, deltak).scale(1 / -eps);
			for (int j = 0; j < CapCurvatureFunctional.getInnerVertices(graph).size(); j++){
				assertEquals(dkdri.get(j), Dk.get(j, i), error);
			}
			v.setRadius(v.getRadius() - eps);
			i++;
		}
	}
	
}
