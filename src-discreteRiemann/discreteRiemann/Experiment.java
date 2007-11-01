package discreteRiemann;

import java.util.HashMap;



import de.jtem.blas.ComplexMatrix;
import de.jtem.mfc.field.Complex;
import de.jtem.numericalMethods.util.ArrayUtilities;
import de.jtem.riemann.theta.SiegelReduction;

	
public class Experiment {

	final static String NAME = "name";
	final static String IS_INTRINSIC = "is intrinsic";
	final static String NOV = "#vertices";
	final static String L_INF_ERROR ="l-inf-norm error";
	final static String L_2_ERROR = "l2-norm error";
	static final String L_INF_ERROR_MOD_SIGN = "l-inf-norm error mod sign";
	static final String L_2_ERROR_MOD_SIGN = "l-inf-norm error mod sign";
	
	ComplexMatrix expected, actual;
	
	ComplexMatrix reducedExpected, reducedActual;
	
	HashMap<String,Object> descriptor = new HashMap<String,Object>();
	
	Experiment( String name ) {
		addDescription(NAME, name );
	}
	
	static ComplexMatrix transformedPeriodMatrixToRN( ComplexMatrix pm ) {
		return pm.times( new Complex( 0, 2*Math.PI ) );
	}
	
	static ComplexMatrix transformedPeriodMatrixFromRN( ComplexMatrix pm ) {
		return pm.divide( new Complex( 0, 2*Math.PI ) );
	}
	
	static ComplexMatrix reduced( ComplexMatrix pm ) {
		
		ComplexMatrix pm_russian = transformedPeriodMatrixToRN(pm);
		
		ComplexMatrix reduced = new SiegelReduction( pm_russian ).getReducedPeriodMatrix();
		
		return transformedPeriodMatrixFromRN( reduced );
	}
	
	public void setExpectedInRN( ComplexMatrix pmRN ) {
		setExpected( transformedPeriodMatrixFromRN(pmRN) );
	}
	
	public void setExpected( ComplexMatrix pm ) {
		expected = new ComplexMatrix(pm);
		
		reducedExpected = reduced( expected );
	}
	
	public void setActualInRN( ComplexMatrix pmRN ) {
		setActual( transformedPeriodMatrixFromRN(pmRN) );
	}
	
	public void setActual( ComplexMatrix pm ) {
		actual   = new ComplexMatrix(pm);//pm.plus(pm.transpose()).divide(2));
		
		reducedActual = reduced(actual);
	}
	
	public ComplexMatrix getActual() {
		return new ComplexMatrix(actual);
	}
	
	public ComplexMatrix getExpected() {
		return new ComplexMatrix( expected );
	}
	
	public ComplexMatrix getReducedActual() {
		return new ComplexMatrix(reducedActual);
	}
	
	public ComplexMatrix getReducedExpected() {
		return new ComplexMatrix( reducedExpected );
	}
	
	public double l2NormSqr( double [][] array ) {
		double sum=0;
		
		for( int i=0; i<array.length; i++ ) {
			for( int j=0; j<array[i].length; j++ ) {
				sum += array[i][j]*array[i][j];
			}
		}
		
		return sum;
	}
	
	public double maxNorm( double [][] array ) {
		double max = Double.MIN_VALUE;
		
		for( int i=0; i<array.length; i++ ) {
			for( int j=0; j<array[i].length; j++ ) {
				if( Math.abs(array[i][j]) > max )
					max = Math.abs(array[i][j]);
			}
		}
		
		return max;
	}
	
	public double [][] mod1( double [][] array ) {
		double [][] result = new double[array.length][array[0].length];
		for( int i=0; i<array.length; i++ ) {
			for( int j=0; j<array[i].length; j++ ) {
				double v = Math.floor(array[i][j]) + 1 - array[i][j];
				while( v>0.5 )
					v-=1;
				result[i][j] = v;
			}
		}
		return result;
	}
		
	public double [][] abs( double [][] array ) {
		double [][] result = new double[array.length][array[0].length];
		for( int i=0; i<array.length; i++ ) {
			for( int j=0; j<array[i].length; j++ ) {
				result[i][j] = Math.abs(array[i][j]);
			}
		}
		return result;
	}
		
	public double [][] minus( double [][] array, double [][] array2 ) {
		double [][] result = new double[array.length][array[0].length];
		for( int i=0; i<array.length; i++ ) {
			for( int j=0; j<array[i].length; j++ ) {
				result[i][j] = array[i][j] - array2[i][j];
			}
		}
		return result;
	}
	public void compute() {
		
		addDescription( L_INF_ERROR, maxNormError() );
		addDescription(L_2_ERROR,  l2NormError() );
		addDescription( L_INF_ERROR_MOD_SIGN, maxNormErrorModSign() );
		addDescription(L_2_ERROR_MOD_SIGN,  l2NormErrorModSign() );
	}
	
	public void addDescription( String key, Object value ) {
		descriptor.put(key, value);
	}
	
	
	public double l2NormError() {
		ComplexMatrix tmp = reducedActual.minus(reducedExpected);
		
		return( Math.sqrt( l2NormSqr(mod1( tmp.re ))+ l2NormSqr( tmp.im) ) );
	}
	
	public double maxNormError() {
		ComplexMatrix tmp = reducedActual.minus(reducedExpected);
		
		return( Math.max( maxNorm(mod1( tmp.re )), maxNorm( tmp.im) ) );
	}
	
	public double l2NormErrorModSign() {
		double [][] re = minus(abs(reducedActual.re), abs( reducedExpected.re) );
		double [][] im = minus(abs(reducedActual.im), abs( reducedExpected.im) );
		return( Math.sqrt( l2NormSqr(mod1( re ))+ l2NormSqr( im) ) );
	}
	
	public double maxNormErrorModSign() {
		double [][] re = minus(abs(reducedActual.re), abs( reducedExpected.re) );
		double [][] im = minus(abs(reducedActual.im), abs( reducedExpected.im) );
		return( Math.max( maxNorm(mod1( re )), maxNorm( im) ) );
	}
	
	public static String title( String ...strings ) {
		String row = "";
		for( String string : strings ) {
			row += string + "  ";
		}
		return row;
	}
	public String row( String ...strings ) {
		compute();
		String row = "";
		for( String string : strings ) {
			if( descriptor.containsKey(string)) {
				Object value = descriptor.get(string);
				
				
				if( value.getClass() == Double.class ) {
					row += String.format("%e", value);
				} else if( value.getClass() == Integer.class ) {
					row += String.format("%6d", value);
				} else {
					
					row += value.toString();
				}
			} else
				row += "???";
			
			row += " ";
		}
		return row;
	}
	
	public static void printTable( Experiment [] experiments , String ...keys ) {
		System.out.println(title(keys));
		for( Experiment e : experiments ) {
			System.out.println(e.row(keys));
		}
	}
	
	public static ComplexMatrix toMatrix( Complex z ) {
		ComplexMatrix matrix = new ComplexMatrix(1);
			matrix.set(0,0,z );
		return matrix;
	}

	public static void printStandardTable(Experiment[] es) {
		printTable(es, Experiment.NAME, Experiment.NOV, Experiment.L_2_ERROR, Experiment.L_INF_ERROR, Experiment.L_2_ERROR_MOD_SIGN, Experiment.L_INF_ERROR_MOD_SIGN, Experiment.IS_INTRINSIC );
		// TODO Auto-generated method stub
		
	}
}
