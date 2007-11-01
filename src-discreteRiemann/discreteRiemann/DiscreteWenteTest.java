package discreteRiemann;

import de.jtem.mfc.vector.Real3;
import oldCMC.WenteTorus;
import junit.framework.TestCase;

public class DiscreteWenteTest extends TestCase {

	public void testDelaunay() {
		int discr = 11;
		DiscreteWente dw = DiscreteWente.createEmpty( discr );

		dw.wente.set( new WenteTorus() );
		
		dw.wente.setComputeCompleteTorus(true);
		dw.wente.setTransformDomain(false);
		
		dw.wente.setDiscr(discr);
        
		dw.wente.update();
		
		dw.setGrid( dw.wente );
		
		System.out.println("abs x"+dw.x.abs()/dw.m);
		System.out.println("abs y"+dw.y.abs()/dw.n);
		
		Real3 O = new Real3();
		Real3 X = new Real3( dw.x.re, dw.x.im, 0).times(1.0/dw.m);
		Real3 Y = new Real3( dw.y.re, dw.y.im, 0).times(1.0/dw.n);
		
		System.out.println( dw.rho(X,Y,O,X.plus(Y)));
		System.out.println( dw.rho(O,X, X.plus(Y),X.minus(Y)));
		System.out.println( dw.rho(O,Y,Y.minus(X),X));
		
		System.out.println( dw.rho(O,X.plus(Y), Y, X ) );
		System.out.println( dw.rho(O,Y, O.minus(X), X.plus(Y) ) );
		System.out.println( dw.rho(Y,X.plus(Y), Y.plus(Y).plus(X), O ) );
	}
}
