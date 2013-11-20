package minimalsurface.frontend.macro;

import javax.vecmath.Point4d;

import math.util.VecmathTools;

import org.junit.Assert;
import org.junit.Test;

public class CalculateDiagonalIntersectionsTest {

	@Test
	public void testCalculateDiagonalIntersection2D() throws Exception {
		Point4d a = new Point4d(0, 0, 0, 1);
		Point4d b = new Point4d(1, 0, 0, 1);
		Point4d c = new Point4d(1, 1, 0, 1);
		Point4d d = new Point4d(0, 1, 0, 1);
		Point4d s = CalculateDiagonalIntersections.calculateDiagonalIntersection(a, b, c, d);
		VecmathTools.dehomogenize(s);
		Assert.assertEquals(0.5, s.x, 1E-12);
		Assert.assertEquals(0.5, s.y, 1E-12);
		Assert.assertEquals(0.0, s.z, 1E-12);
		Assert.assertEquals(1.0, s.w, 1E-12);
	}

	@Test
	public void testCalculateDiagonalIntersection3D() throws Exception {
		Point4d a = new Point4d(0, 0, 0, 1);
		Point4d b = new Point4d(1, 0, -10, 1);
		Point4d c = new Point4d(1, 1, 0, 1);
		Point4d d = new Point4d(0, 1, 10, 1);
		Point4d s = CalculateDiagonalIntersections.calculateDiagonalIntersection(a, b, c, d);
		VecmathTools.dehomogenize(s);
		Assert.assertEquals(0.5, s.x, 1E-12);
		Assert.assertEquals(0.5, s.y, 1E-12);
		Assert.assertEquals(1.0, s.w, 1E-12);
	}
	
}
