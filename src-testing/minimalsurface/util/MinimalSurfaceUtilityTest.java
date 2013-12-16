package minimalsurface.util;

import static halfedge.HalfEdgeDataStructure.createHEDS;
import static java.lang.Math.PI;
import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.tan;
import static minimalsurface.util.MinimalSurfaceUtility.associatedEdgeRotation;
import static minimalsurface.util.MinimalSurfaceUtility.getEdgeNormal;
import halfedge.HalfEdgeDataStructure;

import javax.vecmath.Point4d;
import javax.vecmath.Vector4d;

import org.junit.Assert;
import org.junit.Test;

import circlepatterns.graph.CPEdge;
import circlepatterns.graph.CPFace;
import circlepatterns.graph.CPVertex;

public class MinimalSurfaceUtilityTest {

	@Test
	public void testAssociatedEdgeRotation() throws Exception {
		Vector4d dualEdge = new Vector4d(1, 0, 0, 0);
		Vector4d edgeNormal = new Vector4d(0, 1, 0, 0);
		double alpha = 0.2;
		Vector4d faceNormal = new Vector4d(0, cos(alpha), sin(alpha), 0);
		double psi = PI / 4;
		
		Vector4d result = associatedEdgeRotation(dualEdge, edgeNormal, faceNormal, psi);
		
		double phi = atan2(tan(psi), cos(alpha));
		double scale = cos(psi) / cos(phi);
		Assert.assertEquals(scale * cos(phi), result.x, 1E-15);
		Assert.assertEquals(0, result.y, 1E-15);
		Assert.assertEquals(scale * sin(phi), result.z, 1E-15);
		Assert.assertEquals(0, result.w, 1E-20);
	}
	
	@Test
	public void testGetEdgeNormal() throws Exception {
		HalfEdgeDataStructure<CPVertex, CPEdge, CPFace> hds = createHEDS(CPVertex.class, CPEdge.class, CPFace.class);
		CPEdge e = hds.addNewEdge();
		CPEdge eOpp = hds.addNewEdge();
		CPVertex v0 = hds.addNewVertex();
		CPVertex v1 = hds.addNewVertex();
		e.linkOppositeEdge(eOpp);
		e.setTargetVertex(v1);
		eOpp.setTargetVertex(v0);
		v0.setXYZW(new Point4d(0.5, 1, 1, 0));
		v1.setXYZW(new Point4d(1, 1, 1, 0));
		
		Vector4d n = getEdgeNormal(e);
		Assert.assertEquals(0.0, n.x, 1E-15);
		Assert.assertEquals(cos(PI / 4), n.y, 1E-15);
		Assert.assertEquals(sin(PI / 4), n.z, 1E-15);
		Assert.assertEquals(0.0, n.w, 1E-15);
	}
	
}
