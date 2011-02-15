package discreteRiemann;

import de.jtem.blas.IntegerMatrix;

public class HomologyUtility {

	
	
	/**
	 * @param omega the antisymmetric matrix of intersections of an unormalized homology basis, 
	 * given by the periods of the harmonic cohomology basis on the homology basis, 
	 * rows indexed by forms and colums by graph cycles
	 * @return the matrix of linear combinations such that P.omega.P^t is the standard 
	 * symplectic matrix
	 */
	static public IntegerMatrix createNormalizedBasis( IntegerMatrix omega ) {
		
		final int dim = omega.getNumCols();
		final IntegerMatrix P = IntegerMatrix.id(dim);
		final int[][] p = P.re;
		final int[][] o = omega.re;
		
		
		for( int indexOfA=0; indexOfA<dim; indexOfA+=2 ) {
			int indexOfB = indexOfA+1;
			
			for(;true; indexOfB++ ) {
				if( indexOfB >= dim )
					throw new RuntimeException("No intersection number = +/-1");
				
				int in = intersectionNumber( o, p, indexOfA, indexOfB );
				
				if( in == -1 || in == 1 ) {
					swapRows( p, indexOfA+1, indexOfB);
					indexOfB=indexOfA+1;
					scaleRow(p, indexOfB, in );
					break;
				}
			}
			
			for( int r=indexOfA+2; r<dim; r++ ) {
				int lambda =  intersectionNumber( o, p, indexOfB, r );
				int mu     = -intersectionNumber( o, p, indexOfA, r );
				
				for( int s=0; s<dim; s++) {
					p[r][s] += lambda * p[indexOfA][s] + mu * p[indexOfB][s];
				}
			}
		}
		
		IntegerMatrix test = P.times(omega).times(P.transpose());
		 test.assignMinus(symplectic(test.numCols));
		 assert test.normSqr()< 1e-5;
		
		
		return P;
	}

	/**
	 * @param P the matrix of linear combinations such that P.omega.P^t is the standard 
	 * symplectic matrix, omega being the intersection matrix
	 * @return a gx2g matrix rows indexed by a-cycles, columns by unnormalized cycles
	 */
	public static IntegerMatrix extractACycles( IntegerMatrix P ) {
		final int g = P.getNumRows() / 2;
		assert 2*g == P.getNumCols();
		IntegerMatrix aP = new IntegerMatrix(g,2*g);
		for( int i=0; i<g; i++) {
			aP.setRow(i, P.getRow(2*i));
		}
		return aP;
	}
	
	/**
	 * @param P the matrix of linear combinations such that P.omega.P^t is the standard 
	 * symplectic matrix
	 * @return a gx2g matrix rows indexed by a-cycles, columns by unnormalized cycles
	 */
	public static IntegerMatrix extractBCycles( IntegerMatrix P ) {
		final int g = P.getNumRows() / 2;
		assert 2*g == P.getNumCols();
		IntegerMatrix bP = new IntegerMatrix(g,2*g);
		for( int i=0; i<g; i++) {
			bP.setRow(i, P.getRow(2*i+1));
		}
		return bP;
	}
	
	static private void scaleRow( final int[][] p, int row, int scale) {
		if( scale==1 )
			return;
		final int dim = p.length;
		for( int s=0; s<dim; s++) {
			p[row][s] *= scale;
		}
	}


	static private void swapRows(final int[][] p, int row1, int row2) {
		final int dim = p.length;
		if( row2 != row1) {
			for( int s=0; s<dim; s++) {
				int dummy = p[row2][s];
				p[row2][s] = p[row1][s];
				p[row1][s] = dummy;
			}
		}
	}

	
	static private int intersectionNumber( int[][] o, int p[][], int indexOfA, int indexOfB ) {
		final int dim = o.length;
		
		int in = 0;
		
		for( int i=0; i<dim; i++ ) {
			for( int j=0; j<dim; j++ ) {
				in += p[indexOfA][i] * p[indexOfB][j] * o[i][j]; //TODO: what order ???
			}
		}
		if( false && in != -1 && in != 1 && in != 0 )
			throw new RuntimeException( "intersection number = " + in );
		return in;
	}
	

	/**
	 * @param dim
	 * @return a random antisymetric matrix
	 */
	static public IntegerMatrix createIntersectionMatrix( int dim ) {
		IntegerMatrix omega = new IntegerMatrix( dim );
		
			do {
			for( int i=0; i<dim; i++ ) {
				for( int j=0; j<i; j++ ) {
					int value = (int)(Math.random()*3)-1;
					omega.set(i,j, value);
					omega.set(j,i,-value);
				}
			}
			
			} while( Math.abs(omega.determinant()) != 1 );
			
			return omega;
	}
	
	/**
	 * @param dim
	 * @return the standard symplectic matrix
	 */
	static public IntegerMatrix symplectic( int dim ) {
		assert dim % 2 == 0;
		IntegerMatrix omega = new IntegerMatrix( dim );
		
			for( int i=0; i<dim; i += 2 ) {
					omega.set(i,i+1, 1);
					omega.set(i+1,i,-1);
			}
				
			return omega;
	}
	
	public static void main(String[] args) {
		
		IntegerMatrix omega = createIntersectionMatrix(2*2);
		
		System.out.println( "omega="+omega);
		
		IntegerMatrix p = createNormalizedBasis(omega);
		System.out.println( "p="+p);
		
		System.out.println( "test=\n"+p.times(omega).times( p.transpose()));
	}
	
}
