package discreteRiemann;

import halfedge.Edge;
import halfedge.Face;
import halfedge.HalfEdgeDataStructure;
import halfedge.Vertex;
import halfedge.generator.FaceByFaceGenerator;

import java.lang.reflect.Array;

import oldCMC.CMCTorusImmersionR3;
import oldCMC.WenteTorus;
import de.jtem.blas.ComplexMatrix;
import de.jtem.mfc.field.Complex;
import de.jtem.mfc.vector.Real3;
import de.jtem.riemann.theta.SiegelReduction;
import discreteRiemann.DiscreteConformalStructure.ConfEdge;
import discreteRiemann.DiscreteConformalStructure.ConfFace;
import discreteRiemann.DiscreteConformalStructure.ConfVertex;

public class DiscreteWente <
V extends Vertex<V, E, F>,
E extends Edge<V, E, F> & HasRho, 
F extends Face<V, E, F> 
>  extends DiscreteImmersionR3 {

	CMCTorusImmersionR3 wente = new CMCTorusImmersionR3();
	
	int m;
	int n;
	int twist;
	
	Complex x, y, o;
	
	Complex [] xy;
	
	DiscreteWente( int discr, Class<V> vClass, Class<E> eClass, Class<F> fClass ) {	
		name="wenteTorus";
	}

	
	public static DiscreteWente createEmpty(int discr) {
		DiscreteWente dw = new DiscreteWente(discr, ConfVertex.class, ConfEdge.class, ConfFace.class );
		return dw;
	}
	
	public static DiscreteImmersionR3 createDiscreteWente( int discr ){
		DiscreteWente dw = new DiscreteWente(discr, ConfVertex.class, ConfEdge.class, ConfFace.class );
		
		dw.wente.set( new WenteTorus() );
		
		dw.wente.setComputeCompleteTorus(true);
		dw.wente.setTransformDomain(false);
		
		dw.wente.setDiscr(discr);
        
		dw.wente.update();
		
		dw.setGrid( dw.wente );
		
		System.out.println("Create trianglular torus");
		System.out.println( "Twist = " + dw.wente.getTwist() );
		System.out.println( dw.m + " x " +dw.n );

		//dw.printAngles();
		
		dw.createTriangularTorus( ConfVertex.class, ConfEdge.class, ConfFace.class );
		
		//dw.printFaces();

		//printBoundingBox();
		//printEdgeLength();
		//printGlue();
		
		
		System.out.println("Compute rhos");
		
		dw.computeRho();
		
		return dw;
	}

	
	void setGrid( CMCTorusImmersionR3 wente ) {
		m = wente.getXDiscr();
		n = wente.getYDiscr();
		twist = wente.getTwist();
		
		x = wente.getX();
		y = wente.getY();
		o = wente.getOrigin();
	}
	
	void createTriangularTorus( Class<V> vClass, Class<E> eClass, Class<F> fClass){
		HalfEdgeDataStructure<V, E, F> ds = HalfEdgeDataStructure.createHEDS(vClass, eClass, fClass);
	
	V [][]   vertex = (V[][])  Array.newInstance(vClass, new int[]{m,n}  ); //new Vertex[m][n];
	
	xy  = new Complex[n*m];
	xyz = new Real3[n*m];
	
	for( int j=0; j<n; j++ ) {
		for( int i=0; i<m; i++ ) {
			vertex[i][j] = ds.addNewVertex();
			int index = vertex[i][j].getIndex();
			
			xy [index] = xy(i,j);
			xyz[index] = wente.valueAt( xy[index] );
		}
	}
	
	generator = new FaceByFaceGenerator<V,E,F>(ds);
	
	for( int j=0; j<n-1; j++ ) {
		for( int i=1; i<m+1; i++ ) {
			
			V v00 = vertex[i-1][j];
			V v01 = vertex[i-1][j+1];
			V v10 = vertex[i%m][j];
			V v11 = vertex[i%m][j+1];
			
			addDelaunayTriangles( v00, v01, v10, v11);
		}
	}
	
	//last row we add the twist
	for( int i=1; i<=m; i++ ) {
		V v00 = vertex[i-1][n-1];
		V v01 = vertex[(i+twist-1)%m][0];
		V v10 = vertex[i%m][n-1];
		V v11 = vertex[(i+twist)%m][0];
		
		addDelaunayTriangles( v00, v01, v10, v11);
	}
	
	//if( !ConsistencyCheck.isValidSurface(ds) )
		//throw new RuntimeException();
	
	
	g = ds;
}

	private void addDelaunayTriangles( V v00, V v01, V v10, V v11) {
		
//		generator.addFace( v10, v01, v00 );
//		generator.addFace( v01, v10, v11 );
		
//		generator.addFace( v00, v10, v11, v01 );
		
		if( rho( v00, v11, v01, v10 ) > 0 ) {
			System.out.print("a");
			generator.addFace( v00, v11, v01 );		
			generator.addFace( v00, v10, v11 );
			
		} else if( rho( v10, v01, v00, v11 ) > 0 ) { 
			System.out.print("b");
			generator.addFace( v00, v10, v01 );		
			generator.addFace( v10, v11, v01 );
			
		} else throw new IllegalStateException("ips");
	}
	

	Complex xy( int index ) {
			
		int ix  =  index % m;
		int iy  =  index / m;
		
		return xy(ix, iy);
	}
	
	Complex xy( int ix, int iy ) {
		
		Complex point = new Complex();
		
		point.re  = ix * x.re / m + iy * y.re / n + o.re;
		point.im  = ix * x.im / m + iy * y.im / n + o.im;
		
		return point;
	}

	Complex xy(Vertex s) {
		return xy[s.getIndex()];
	}
	
	public static void main(String[] args) {
	
//		writeOBJ(20);
//		writeOBJ(40);
//		writeOBJ(80);
//		writeOBJ(160);
		
//		display(10);
		result();
		
//		experiment( 10 );
	}

	static void writeOBJ( int discr ) {
		DiscreteImmersionR3 wente = createDiscreteWente( discr );
		
		wente.writeOBJ();
	}
	
	static void display( int discr ) {
		DiscreteImmersionR3 wente = createDiscreteWente( discr );
		
		display(wente);
	}
	
	private static void experiment( int discr ) {
		DiscreteImmersionR3 wente = createDiscreteWente( discr );
		
		display(wente);
		
		HalfEdgeDataStructure ds = wente.g; 

		DiscreteRiemann dr = new DiscreteRiemann( new DiscreteConformalStructure(ds),null);
		
		ComplexMatrix pm = dr.getPeriodMatrix();
		System.out.println("period matrix: " + pm );
		pm.assignTimes( new Complex( 0, 2*Math.PI ) );
		System.out.println("period matrix: " + pm );
		Complex modulus = new SiegelReduction( pm ).getReducedPeriodMatrix().get(0,0);
		modulus = modulus.divide( new Complex( 0, 2*Math.PI ));
		System.out.println("reduced: " + modulus);
	}
	
	public static Experiment createWenteExperiment( double re, double im, int discr, boolean isIntrinsic  ) {
		Experiment experiment = new Experiment("wente" );

		experiment.setExpected( Experiment.toMatrix(new Complex(0.4130046326338964, 0.9107289243228313)) );
		experiment.setActual( Experiment.toMatrix(new Complex(re,im)) );

		experiment.addDescription(Experiment.NOV, discr*discr);
		experiment.addDescription(Experiment.IS_INTRINSIC, isIntrinsic);
		
		return experiment;	
	}
	
	public static void result() {
		
		//intrinsic
		Experiment [] intrinsic = new Experiment[] { 
				createWenteExperiment(  0.4083505300506822  ,  0.913999626845261, 10, true ), //10
				createWenteExperiment(  0.4114411293604199  ,  0.911982965673506, 20, true ), //20
				createWenteExperiment(  0.4122915330250103  ,  0.911164440450821, 30, true ), //30
				createWenteExperiment(  0.4125538864812010  ,  0.910969230260086, 40, true ), //40
				createWenteExperiment(  0.4127853132378131  ,  0.910830436167653, 80, true ), //80
				createWenteExperiment(  0.4128400443644312  ,  0.910803647944067, 160, true ), //160
		};
		
		
		
		//extrinsic
		Experiment [] extrinsic = new Experiment[] { 
				createWenteExperiment( 0.40889049929566657, 0.9135717332757318, 10, false ), //10 
				createWenteExperiment( 0.40769422247573633, 0.9133716217127179, 20, false ), //20 
				createWenteExperiment( 0.4102572480902793,  0.9120174923281238, 30, false ), //30 
				createWenteExperiment( 0.4113283659493725,  0.9115018903825091, 40, false ), //40 
				createWenteExperiment( 0.41245817184079936, 0.9109772451806142, 80, false ), //80 
				createWenteExperiment( 0.4127568478271646,  0.91084126486772,  160, false  ), //160
		};
		
		System.out.println( "wente=" + extrinsic[0].reducedExpected.get(0,0).arg()/Math.PI);
		Experiment.printStandardTable( intrinsic );
		Experiment.printStandardTable( extrinsic );
		
	}
	
	private void printGlue() {
			for( int j=0; j<=n; j++ ) {
				Real3 A = wente.valueAt(xy(0,j) );
				Real3 B = wente.valueAt(xy(m,j) );
				double l = A.minus(B).norm();
				System.out.println( l );
			}
			for( int i=0; i<=m; i++ ) {
				Real3 A = wente.valueAt(xy(i,0));
				Real3 B = wente.valueAt(xy(i+(wente.getTwist()), n ));
				double l = A.minus(B).norm();
				System.out.println( l );
			}
	}
	private void printBoundingBox() {
		double xMin=Double.MAX_VALUE;
		double xMax=Double.MIN_VALUE;
		
		double yMin=Double.MAX_VALUE;
		double yMax=Double.MIN_VALUE;
		
		double zMin=Double.MAX_VALUE;
		double zMax=Double.MIN_VALUE;
		
		for( int i=0; i<=m; i++ ) {	
			for( int j=0; j<=n; j++ ) {
				Real3 A = wente.valueAt(xy(i,j));
				
				xMin = Math.min( xMin, A.x );
				xMax = Math.max( xMax, A.x );
				yMin = Math.min( yMin, A.y );
				yMax = Math.max( yMax, A.y );
				zMin = Math.min( zMin, A.z );
				zMax = Math.max( zMax, A.z );
			}
		}
		
		System.out.println( "box=["+xMin+","+xMax+"]x["+yMin+"," +yMax+"]x["+zMin+","+zMax+"]");
	}
	
	private void printEdgeLength() {
		
		double lMin=Double.MAX_VALUE;
		double lMax=Double.MIN_VALUE;
		
		for( int i=0; i<=m; i++ ) {	
			for( int j=0; j<n; j++ ) {
				Real3 A = wente.valueAt(xy(i,j));
				Real3 B = wente.valueAt(xy(i,j+1));
				
				double l = A.minus(B).norm();
				lMin = Math.min(lMin, l);
				lMax = Math.max(lMax, l);
			}		
		}
		
		for( int j=0; j<=n; j++ ) {	
			for( int i=0; i<m; i++ ) {
				Real3 A = wente.valueAt(xy(i,j));
				Real3 B = wente.valueAt(xy(i+1,j));
				
				double l = A.minus(B).norm();
				lMin = Math.min(lMin, l);
				lMax = Math.max(lMax, l);
			}		
		}
		
		
		
		System.out.println( "edge=["+lMin+","+lMax+"]" );
	}

	
	private void printAngles() {
		
		double lMin=Double.MAX_VALUE;
		double lMax=Double.MIN_VALUE;
		
		for( int i=0; i<m; i++ ) {	
			for( int j=0; j<n; j++ ) {
				Real3 A = wente.valueAt(xy(i,j));
				Real3 B = wente.valueAt(xy(i+1,j));
				Real3 C = wente.valueAt(xy(i,j+1));
		
				Real3 BA = B.minus(A);
				Real3 CA = C.minus(A);
				
				double l = Math.acos( BA.dot(CA)/BA.norm()/CA.norm());
				lMin = Math.min(lMin, l);
				lMax = Math.max(lMax, l);
			}		
		}
		double angle = Math.acos( x.dot(y)/x.abs()/y.abs() );
		System.out.println( "angle=["+lMin/Math.PI*180+","+lMax/Math.PI*180+"]" + "  " + angle/Math.PI*180 );
	}
	
	
}
