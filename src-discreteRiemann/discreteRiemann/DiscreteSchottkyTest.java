package discreteRiemann;

import junit.framework.TestCase;
import de.jtem.mfc.field.Complex;
import de.jtem.mfc.vector.Real3;

public class DiscreteSchottkyTest extends TestCase {

	
	public void testStereoGraphic() {
		
		
		double [][] test = new double[][] {
				{  0, 0, 0.0, 0, 0 },
				{  1, 0, 0.5, 0, 0.5 },
				{  0, 1, 0, 0.5, 0.5 },
				{0.5, 0, 0.4, 0, 0.2},
				{  2, 0, 0.4, 0, 0.8},
		};
		
		
		for( int i=0; i<test.length; i++) {
			double [] t = test[i];
		
			Real3 P = DiscreteSchottky.stereoGraphicProjection( t[0], t[1] );
		
			assertEquals( "Z", t[4], P.z, 1e-12 );
			assertEquals( "X", t[2], P.x, 1e-12 );
			assertEquals( "Y", t[3], P.y, 1e-12 );
			
			
			Complex z = DiscreteSchottky.stereoGraphicProjection(P);
			
			assertEquals( t[0], z.re, 1e-12 );
			assertEquals( t[1], z.im, 1e-12 );
		}
		
		for( int i=0; i<200; i++) {
			
			Complex Z = new Complex( Math.random()*100-50, Math.random()*100-50 );
			
			Real3 P = DiscreteSchottky.stereoGraphicProjection( Z.re, Z.im );
		
			assertEquals( new Real3(P.x, P.y, P.z-0.5).times(2).norm() , 1, 1e-12 );
			
			Complex z = DiscreteSchottky.stereoGraphicProjection(P);
			
			assertEquals( Z.re, z.re, 1e-12 );
			assertEquals( Z.im, z.im, 1e-12 );
		}
		
		double deltaAlpha = Math.PI / 3;
		
		Real3 P = new Real3( 
				0.5 * Math.sin( deltaAlpha ), 0, 
				0.5 * Math.cos( deltaAlpha ) + 0.5 );
		
		assertEquals( new Real3(P.x, P.y, P.z-0.5).times(2).norm() , 1, 1e-12 );
		
		Complex Z = DiscreteSchottky.stereoGraphicProjection( P );
		
		
		Real3 Q = DiscreteSchottky.stereoGraphicProjection(Z.re, Z.im);
		
	}
}
