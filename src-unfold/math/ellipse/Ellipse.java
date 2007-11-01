package math.ellipse;

import no.uib.cipr.matrix.Matrix;

/** an arclength parametrized ellipse */
public class Ellipse {

	private double e = 0;
	
	public Ellipse(double e) {
		this.e = e;
	}
	
	// An inverse to the incomplete elliptic integral of the second kind, by means of inverse series.
	// http://mathforum.org/kb/message.jspa?messageID=4508328&tstart=0
	private double inverseEllipticE(double d, double m) {
		
		double d3 = Math.pow(d, 3);
		double d5 = Math.pow(d, 5);
		double d7 = Math.pow(d, 7);
		double d9 = Math.pow(d, 9);
		double d11 = Math.pow(d, 11);
		double d13 = Math.pow(d, 13);
		
		double m2 = Math.pow(m, 2);
		double m3 = Math.pow(m, 3);
		double m4 = Math.pow(m, 4);
		double m5 = Math.pow(m, 5);
		double m6 = Math.pow(m, 6);
		
		return d + (d3*m)/6 + (1/120)*d5*(-4*m + 13*m2) +
			(d7*(16*m - 284*m2 + 493*m3))/5040 +
			(d9*(-64*m + 4944*m2 - 31224*m3 + 37369*m4))/362880 +
			(d11*(256*m - 81088*m2 + 1406832*m3 - 5165224*m4 + 4732249*m5))/
			39916800 + (1.6059043836821613e-10)*(d13*(-1024*m + 1306880*m2 - 56084992*m3 +
			474297712*m4 - 1212651548*m5 + 901188997*m6));
	}
	
	// get x (width) at arclength parameter s
	public double getR(double s) {
		double t = 0;
		if(s <= getQuarterCircumference()) {
			
			t = inverseEllipticE(s, e*e);
		}
		else {
//			System.err.println("rev");
			t = inverseEllipticE(getHalfCircumference() - s, e*e);
		}
		double a = Math.sqrt(1/(1-e*e));
		return a*Math.sin(t);
	}
	
	// uses S. Ramanujan's formula from 1914
	public double getCircumference() {
		double b = 1;
		double a = Math.sqrt(1/(1-e*e));
		return Math.PI * (3*(a + b) - Math.sqrt((a + 3*b)*(3*a + b)));
	}
	
	public double getQuarterCircumference() {
		return getCircumference()/4.0;
	}
	
	public double getHalfCircumference() {
		return getCircumference()/2.0;
	}
}
