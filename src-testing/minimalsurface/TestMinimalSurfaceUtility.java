package minimalsurface;

import java.util.Arrays;

import minimalsurface.util.MinimalSurfaceUtility;

import org.junit.Assert;
import org.junit.Test;

import de.jreality.math.Rn;

public class TestMinimalSurfaceUtility {

	@Test
	public void testKoenigsDual() throws Exception {
		double[][] vertices = {
			{0, 0, 0},	
			{1, 0, 0},
			{1.5, 1, 0},
			{-0.5, 1, 0}
		};
		double[] diagInters = {
			0.5, 1.0/3.0, 0.0
		};
		int baseVertex = 0;
		double[] basePosition = {
			0, 0, 0
		};
		int refEdge = 0;
		double edgeLength = 1.0;
		
		double[][] d = MinimalSurfaceUtility.koenigsDual(vertices, diagInters, baseVertex, basePosition, refEdge, edgeLength);
		System.out.println("prim: "+ Arrays.deepToString(vertices));
		System.out.println("dual: "+ Arrays.deepToString(d));
		
		double[] vd1 = Rn.subtract(null, vertices[0], vertices[2]);
		double[] vd2 = Rn.subtract(null, vertices[1], vertices[3]);
		double[] dd1 = Rn.subtract(null, d[0], d[2]);
		double[] dd2 = Rn.subtract(null, d[1], d[3]);
		Assert.assertEquals(0.0, Rn.euclideanNorm(Rn.crossProduct(null, vd1, dd2)), 1E-15);
		Assert.assertEquals(0.0, Rn.euclideanNorm(Rn.crossProduct(null, vd2, dd1)), 1E-15);
	}
	
}
