package discreteRiemann;

import halfedge.Edge;
import halfedge.Face;
import halfedge.HalfEdgeDataStructure;
import halfedge.Vertex;
import halfedge.decorations.HasLength;
import halfedge.generator.FaceByFaceGenerator;
import halfedge.surfaceutilities.ConsistencyCheck;

import java.lang.reflect.Array;

import de.jtem.blas.ComplexMatrix;
import de.jtem.mfc.field.Complex;
import de.jtem.mfc.group.Moebius;
import de.jtem.mfc.vector.Real3;
import de.jtem.riemann.schottky.Schottky;
import de.jtem.riemann.schottky.SchottkyDomain;
import de.jtem.riemann.theta.SiegelReduction;
import discreteRiemann.DiscreteConformalStructure.ConfEdge;
import discreteRiemann.DiscreteConformalStructure.ConfFace;
import discreteRiemann.DiscreteConformalStructure.ConfVertex;

public class DiscreteSchottky <
V extends Vertex<V, E, F>,
E extends Edge<V, E, F> & HasRho & HasLength, 
F extends Face<V, E, F> 
>  extends DiscreteImmersionR3 {

	Schottky schottky;
	SchottkyDomain domain;
	
	
	
	int n = 40; // anzahl breitengrade zum aequator;
	
	double deltaAlpha = Math.PI / 2 / n;
	
	double D = r(Math.PI - deltaAlpha );
	
	int [] index;
	int [] neighbor;
	double [] xy;
	

	final int NOV_ON_OUTER_BOUND = 6;
	int genus;
	final int discr = 16; //16; //20; //28;
	final int K     = 20; //11; //15; //21;
	
	public DiscreteSchottky( Schottky schottky ) {
		this.schottky = schottky;
		genus = schottky.getNumGenerators();
	}
	
	public static DiscreteSchottky createDiscreteSchottkySimple() {
		
		double c = Math.cos(Math.PI/3);
		double s = Math.sin(Math.PI/3);
		
		return createDiscreteSchottky( new Schottky(
		
			
		new double[] { 
			 1, 0, -1, 0, 0.01, 0
		}, 1e-7
		
		));
	}
	
	public static DiscreteSchottky createDiscreteSchottkyWente() {
		
		double c = Math.cos(Math.PI/3);
		double s = Math.sin(Math.PI/3);
		
		return createDiscreteSchottky( new Schottky(
		
			
		new double[] { 
			 c, s,-c, -s, -0.01, 0,
			-c, s, c, -s, -0.01, 0,
		}, 1e-7
		
		));
		
		
//		
//		period matrix: ((-0.09783470295282617+0.45965709569772767i, 0.8829869562274179-0.035971666782177045i),
//				(0.8813032237052356-0.03598412987499189i, 0.13470357421841783+0.44974034137327223i))
//				period matrix: ((-2.8881107100288035-0.6147135681254767i, 0.2260166482005348+5.547970669799336i),
//				(0.22609495612219102+5.53739146635474i, -2.8258019049624754+0.8463675183537378i))
//				reduced: ((0.4997839192347809+1.9567192200218073i, -0.49658686404660163+0.1752036483609479i),
//				(0.4961853490821532+0.17485667563607785i, -0.49996673477956566+1.9546654808225181i))
	}
	
	public static DiscreteSchottky createDiscreteSchottky( int genus ) {
		return createDiscreteSchottky( new Schottky(genus));
	}
	
	public static class SchottkyEdge 
	extends ConfEdge
	implements HasRho, HasLength {

		double length = 1;
		
		@Override
		public Double getLength() {
			return length;
		}

		@Override
		public void setLength(Double length) {
			this.length = length;
		}
		
		boolean isOnIsometricCircle = false;
		boolean isOnPrimeIsometricCircle = false;
		int circleComponent;
		
		int getCircleComponent() {
			if( !isOnIsometricCircle )
				throw new IllegalStateException();
			
			return circleComponent;
		}		
	}
	
	public static DiscreteSchottky createDiscreteSchottky( Schottky schottky ) {
		DiscreteSchottky ds = new DiscreteSchottky(schottky);
			
		ds.createHEDS( ConfVertex.class, SchottkyEdge.class, ConfFace.class );
		
		try {
		ds.computeRho();
		} catch( Exception e ) {
			System.out.println("failed to compute rhos");
		}
		return ds;
	}
	
	boolean vertexIsOnOuterBound( int index ) {
		return index<NOV_ON_OUTER_BOUND;
	}
	
	boolean vertexIsOnCircleBound( int index ) {
		return !vertexIsOnOuterBound(index) && index-NOV_ON_OUTER_BOUND < 2 * genus * discr;
	}
	
	int getCircleBoundComponent( int index ) {
		if( !vertexIsOnCircleBound(index))
			throw new IllegalStateException("vertex is not a circle bound" );
		
		return (index-NOV_ON_OUTER_BOUND) / 2 / discr;
	}
	
	boolean vertexIsOnPrimeCircleBound(int index ) {
		return vertexIsOnCircleBound(index) && identifiedIndex(index) != index;
	}
	
	int identifiedIndex( int index ) {
		
		if( index < NOV_ON_OUTER_BOUND )
			return index;
		
			
		if( ( index - NOV_ON_OUTER_BOUND )/ discr >= 2*genus )
			return index;
		
		return (( index - NOV_ON_OUTER_BOUND ) / discr ) % 2 == 0 ? index : index - discr;			
	}
	
	protected Real3 xyz( ConfVertex s) {
		return new Real3( s.p.x, s.p.y, 0 );
	}
	
	@Override
	double rho( Edge edge_ ) {
		SchottkyEdge edge = (SchottkyEdge)edge_;
		
		ConfVertex S = (ConfVertex)edge.getStartVertex();
		ConfVertex T = (ConfVertex)edge.getTargetVertex();
		ConfVertex L = (ConfVertex)edge.getNextEdge().getTargetVertex();
		ConfVertex R = (ConfVertex)edge.getOppositeEdge().getNextEdge().getTargetVertex();
		
		if( !edge.isOnIsometricCircle ) {
			return rho( xyz(S), xyz(T), xyz(L), xyz(R) );
		}
			
		if( edge.isOnPrimeIsometricCircle )
			return rho(edge.getOppositeEdge() );
		
		Complex zR = new Complex( R.p.x, R.p.y );
		
		Complex ZR = m.applyInverseTo(zR);
		schottky.getGenerator( edge.getCircleComponent() ).applyInverseTo(  ZR, ZR  );
		m.applyTo(ZR, ZR);
		
		Real3 xyzR = new Real3( ZR.re, ZR.im, 0 );
		
//		Complex ZR = schottky.getGenerator( edge.getCircleComponent() ).applyInverseTo(  zR  );
//		
//		Real3 xyzR = stereoGraphicProjection(ZR.re, ZR.im);
			
		double rho = rho( xyz(S), xyz(T), xyz(L), xyzR );
		
		if( rho < 0 )
			System.out.println("negative circle rho" );
		
		return rho;
	}
	
	void createHEDS( Class<V> vClass, Class<E> eClass, Class<F> fClass) {
			
		createSchottkyDomain();
		
		//index = domain.getIndices();
		//xy    = domain.getPoints();
		
		int nov   = domain.getNumOfPoints();
		int cd    = domain.getCircleDiscr();
		
		g = HalfEdgeDataStructure.createHEDS(vClass, eClass, fClass);
		
		V []  vertex = (V[])  Array.newInstance(vClass, new int[]{nov}  ); 
		
		for( int i=0; i<nov; i++ ) {
			
			// here we do the idenfication of the circles:
			// there are 2*genus many, each consist of cd many vertices.
			// this circles come first in the array of vertices of the triangulation
			// and they are pairwise identified. this results in ....
			V v = vertexIsOnPrimeCircleBound(i) ? vertex[i-cd] : (V)g.addNewVertex();
				
			vertex[i] = v;		
			
			if( !vertexIsOnPrimeCircleBound(i))
			
				((ConfVertex)v).setXY( xy[2*i], xy[2*i+1] );
		
		}
		
//		Vertex VI = g.addNewVertex();
//		((ConfVertex)VI).setXY( Double.POSITIVE_INFINITY, 0 );
		
		xyz = new Real3[g.getNumVertices()];
		
		for( int i=0; i<xyz.length; i++ ) {
			ConfVertex v = (ConfVertex) g.vertexList.get(i);
		
			//xyz[i] = stereoGraphicProjection( v.p.x, v.p.y );
			//xyz[i] = new Real3( v.p.x, v.p.y, 0 );
			xyz[i] = new Real3( v.p.x, v.p.y, -0.01 * v.p.x*v.p.x + -0.01 * v.p.y *v.p.y );
		}
		
		//xyz[g.getNumVertices()-1] = new Real3( 0,0,1);
		
		FaceByFaceGenerator<V,E,F> generator = new FaceByFaceGenerator<V,E,F>(g);
		
		int [] index = domain.getIndices();
		
		for( int i=0; i<index.length/3; i++ ) {
			
			int i0 = index[3*i];
			int i1 = index[3*i+1];
			int i2 = index[3*i+2];
			
			V V0 = vertex[i0];
			V V1 = vertex[i1];
			V V2 = vertex[i2];
			
			F newf = generator.addFace( V0, V1, V2);
			
			Real3 P0 = stereoGraphicProjection( xy[2*i0], xy[2*i0+1]  );
			Real3 P1 = stereoGraphicProjection( xy[2*i1], xy[2*i1+1]  );
			Real3 P2 = stereoGraphicProjection( xy[2*i2], xy[2*i2+1]  );
		
			assignSchottkyEdgePropperties(generator, V0, i0, V1, i1 );
			assignSchottkyEdgePropperties(generator, V1, i1, V2, i2 );
			assignSchottkyEdgePropperties(generator, V2, i2, V0, i0 );
		}
	
		
//		for( int i=0; i<6; i++ ) {
//			int j = (i+1)%6;		
//			generator.addFace( (V)VI, (V)g.vertexList.get(j),(V)g.vertexList.get(i) );
//		}
//		
		
		
		F f0 = generator.addFace((V)g.vertexList.get(2), (V)g.vertexList.get(0),(V)g.vertexList.get(4));
		F f1 = generator.addFace((V)g.vertexList.get(2), (V)g.vertexList.get(1),(V)g.vertexList.get(0));
		F f2 = generator.addFace((V)g.vertexList.get(3), (V)g.vertexList.get(2),(V)g.vertexList.get(4));
		F f3 = generator.addFace((V)g.vertexList.get(5), (V)g.vertexList.get(4),(V)g.vertexList.get(0));
	
		
		
		if( !ConsistencyCheck.isValidSurface(g) )
			throw new RuntimeException();
			
	}

	private void assignSchottkyEdgePropperties(FaceByFaceGenerator<V, E, F> generator, V V0, int i0, V V1, int i1) {
		SchottkyEdge edge = (SchottkyEdge)generator.findEdge(V0, V1);
		
		if( vertexIsOnCircleBound( i0 ) && vertexIsOnCircleBound(i1) ) {
			edge.isOnIsometricCircle = true;
			edge.isOnPrimeIsometricCircle = vertexIsOnPrimeCircleBound(i0);
			if( edge.isOnPrimeIsometricCircle ) {
				if( !vertexIsOnPrimeCircleBound(i1) )
					throw new IllegalStateException("ups");
			}
			edge.circleComponent = getCircleBoundComponent(i0);
			if( edge.circleComponent != getCircleBoundComponent(i1) )
				throw new IllegalStateException("ups");
		}
	}

	
	static Real3 stereoGraphicProjection( double re, double im ) {
		double denom = 1 + re*re + im*im;
		
		return new Real3( re/denom, im/denom, ( re*re + im*im ) / denom  );
		//return new Real3( 2 * re/denom, 2 * im/denom, 2 * ( re*re + im*im ) / denom - 1 );
	}
	
	static Complex stereoGraphicProjection( Real3 s ) {
		
		double norm = new Real3(s.x, s.y, s.z-0.5).times(2).norm();
		
		if( Math.abs(norm-1)>1e-12)
			throw new IllegalArgumentException();
		
		Complex z = stereoGraphicProjection_raw(s);
		
		Real3 test = stereoGraphicProjection(z.re, z.im);
		
		if( s.minus(test).norm() > 1e-6 ) 
			throw new RuntimeException( "ups " + s + " " + z + "  " + test + "  " + s.minus(test).norm() );
		
		return z;
	}
	
	static Complex stereoGraphicProjection_raw( Real3 s ) {
		
		double x = s.x; // / 2;
		double y = s.y; // / 2;
		double z = s.z; // / 2 + 0.5;
		
		if( z == 0 )	 
			return new Complex();
		
		if( x == 0 )
			return new Complex( 0, z / y );
			
		if( y == 0 ) 
			return new Complex( z / x, 0 );
		
		double denom = z / ( x*x + y*y );
		
		return new Complex( x * denom, y * denom );
	}
	
	void createSchottkyDomain() {
		
		domain = new SchottkyDomain() {
			 @Override
			public double [][] getBoundary() {
			     
				 double [][] b = super.getBoundary();
		     
			     b[0] = new double[] {
			    	D * Math.cos( Math.PI/3), D * Math.sin( Math.PI/3), 0,0,
			    	D * Math.cos( Math.PI  ), D * Math.sin( Math.PI  ), 0,0,
			    	D * Math.cos(-Math.PI/3), D * Math.sin(-Math.PI/3), 0,0,
			     };
			     b[0][ 2] = (b[0][0]+b[0][4])/2;
			     b[0][ 3] = (b[0][1]+b[0][5])/2;
			     b[0][ 6] = (b[0][8]+b[0][4])/2;
			     b[0][ 7] = (b[0][9]+b[0][5])/2;
			     b[0][10] = (b[0][8]+b[0][0])/2;
			     b[0][11] = (b[0][9]+b[0][1])/2;
	
				 
//				 b[0] = new double[12];
//				 
//				 for( int i=0; i<b[0].length; i+=2 ) {
//					 b[0][i+0] = D * Math.cos(Math.PI / 3 * i / 2);
//					 b[0][i+1] = D * Math.sin(Math.PI / 3 * i / 2);		 
//				 }
//				 
			     return b;
			 }
		 };
		 
		 
		 domain.setSchottky(schottky);
		 domain.setMinAngle(0);
		 domain.setCircleDiscr(discr);
		 domain.setMaxArea(Double.MAX_VALUE);
		 
		 domain.update();
		 
		 //addRandomPoints( 100 );
		
		 addPaddingCircles(1);
		 addPaddingCircles(2);
		 addPaddingCircles(5);
		 addPaddingCircles(10);
		 addPaddingCircles(21);
		 addPaddingCircles(42);
		 addPaddingCircles(83);
		 //for( int k=1; k<=K; k+=1)  // 15 with 20
			 //addPaddingCircles(k);	 
		 
//		 domain.addPoint( 0, 0 );
//		 
//		 int discr = 6;
//		 
//		 for( int i=1; i<=n; i++ ) {	
//			 addPaddingOnCircle( r(i*deltaAlpha),         new Complex(0,0), discr, 0 );
//			 discr*=2;
//		 }
//		 
//		 int discr=12;
//		 for( int i=4; i<10; i+=2 ) {
//			 addPaddingOnCircle( r(Math.PI-(i+0.5)*deltaAlpha), new Complex(0,0), discr, 0 );
//			 if( discr < this.discr + 6 )
//				 discr+=6;
//		 }
//		 
		 //domain.setMinAngle(7);
		 
		 xy = domain.getPoints();
		 index = domain.getIndices();
		 neighbor = domain.getNeighbors();
		 
		 applyMobiusTransformToDomain();
		 
		 //harmonicMean();
		 
		 
		 flipEdges();
		 
		 splitNonDelaunayEdges();
		 
		 domain.refine();
		 
		 xy = domain.getPoints();
		 index = domain.getIndices();
		 neighbor = domain.getNeighbors();
		
		 applyMobiusTransformToDomain();
		 
		 //harmonicMean();
		 
		 
		 flipEdges();
		 
		 
		 
	}

	private void splitNonDelaunayEdges() {
		
		for( int i=0; i<index.length/3; i++ ) {
			for( int j=0; j<3; j++ ) {
				int g = neighbor[3*i+j];
				if( g != -1 ) {
					int S = index[3*i+(j+1)%3];
					int T = index[3*i+(j+2)%3];
					int L = index[3*i+ j     ];
					int R = oppositeVertex( g, S, T );					
					
					if( rho( S, T, L, R ) < 0 ) {
					//if( dist(xyz(S),xyz(T)) > dist(xyz(R),xyz(L))) {
						Real3 s = xyz(S);
						Real3 t = xyz(T);
						
						Complex z = new Complex( (s.x + t.x)/2, (s.y + t.y)/2);
						
						System.out.println("split edge");
						applyInverseTo(z);
						System.out.println("split edge:" + z);
						addPoint( z );
					
					}
				}
			}
		}
	}

	private void harmonicMean() {
		int nov = xy.length/2;
		int noe = index.length/3;
		
		boolean [] boundaryVertex = new boolean[nov];
		
		for( int i=0; i<noe; i++ ) {
			for( int j=0; j<3; j++ ) {
				if( neighbor[3*i+j]==-1) {
					boundaryVertex[index[3*i+(j+1)%2]] = true;
					boundaryVertex[index[3*i+(j+2)%2]] = true;
				}
			}
		}
		int [] count = new int[nov];
		
		
		double newXY [] = new double[xy.length];
		
		for( int i=0; i<noe; i++ ) {
			for( int j=0; j<3; j++ ) {
				int v0 = index[3*i+(j+0)%3];
				int v1 = index[3*i+(j+1)%3];
				int v2 = index[3*i+(j+2)%3];
							
				newXY [2*v0+0] += xy[2*v1+0] + xy[2*v2+0];
				newXY [2*v0+1] += xy[2*v1+1] + xy[2*v2+1];
				count[v0]+=2;
			}
		}
		
		for( int i=6+2*discr*4; i<nov; i++ ) {
			if( !boundaryVertex[i]) {
				xy[2*i+0] = newXY [2*i+0] / count[i];
				xy[2*i+1] = newXY [2*i+1] / count[i];
			}
		}
		
	}

	private void flipEdges() {
		flipEdges(100);
	}
	
	private void flipEdges( int maxCount) {
		
		boolean edgeWasFlipped;
		int count =0;
		do {
			count++;
			if( count > maxCount ) {
				System.out.println("reached max number of iterations in flip edges");
				break;
			}
			edgeWasFlipped = false;
			
			for( int i=0; i<index.length/3; i++ ) {
				for( int j=0; j<3; j++ ) {
					int g = neighbor[3*i+j];
					if( g != -1 ) {
						int S = index[3*i+(j+1)%3];
						int T = index[3*i+(j+2)%3];
						int L = index[3*i+ j     ];
						int R = oppositeVertex( g, S, T );					
						
						if( rho( S, T, L, R ) < 0 ) {
						//if( dist(xyz(S),xyz(T)) > dist(xyz(R),xyz(L))) {
							
							edgeWasFlipped = edgeWasFlipped | flippEdge(i,j);
						
						}
					}
				}
			}
		} while(edgeWasFlipped);
		
	}

	private void setTriple( int [] array, int pos, int v0, int v1, int v2 ) {
		if( v0 == v1 && v0 != -1 || v1 == v2 && v1 != -1|| v0 == v2 && v2 != -1)
			throw new IllegalArgumentException();
		
		array[ 3*pos + 0 ] = v0;
		array[ 3*pos + 1 ] = v1;
		array[ 3*pos + 2 ] = v2;
	}
	
	private boolean flippEdge(int f, int j) {
		
		int g = neighbor[3*f+j];
		
		int S = index[3*f+(j+1)%3];
		int T = index[3*f+(j+2)%3];
		
		System.out.println( "flipp edge " + S + " " + T  );
		
		int L = index[3*f+ j     ];
		int R = oppositeVertex( g, S, T );
		
		int fTL = neighbor[3*f + localOppositeVertex(f, T, L)];
		int fLS = neighbor[3*f + localOppositeVertex(f, L, S)];
		
		int gSR = neighbor[3*g + localOppositeVertex(g, S, R)];
		int gRT = neighbor[3*g + localOppositeVertex(g, R, T)];
		
		if( fLS == gSR || gRT == fTL )
			return false;
		
		setTriple( index, f, T, L, R );
		if( T==L || L==R || R==T )
			throw new IllegalStateException();
		
		setTriple( index, g, S, R, L );
		if( S==L || L==R || R==S )
			throw new IllegalStateException();
		
		setTriple( neighbor, f, g, gRT, fTL );
		setTriple( neighbor, g, f, fLS, gSR ); 
		
		if( gRT != -1 )
			neighbor[ 3*gRT + localOfTriple( neighbor, gRT, g)] = f;
		if( fLS != -1 )
			neighbor[ 3*fLS + localOfTriple( neighbor, fLS, f)] = g;
		
		return true;
	}

	private double rho(int s, int t, int l, int r) {
		return rho( xyz(s), xyz(t), xyz(l), xyz(r) );
	}

	private Real3 xyz( int s ) {
		return new Real3(xy[2*s], xy[2*s+1], 0 );
	}
	
	private int oppositeVertex(int t, int v1, int v2) {	
		return index[3*t + localOppositeVertex(t, v1, v2)];
	}
	
	private int localOfTriple( int [] array, int t, int v ) {
		for( int j=0; j<3; j++ ) {
			if( array[3*t+j] == v )
				return j;
		}
		return -1;
	}
	
	private int localOppositeVertex(int t, int v1, int v2) {
		if( v1 == v2 )
			throw new IllegalArgumentException();
		
		int lv1 = localOfTriple(index, t, v1);
		int lv2 = localOfTriple(index, t, v2);
		
		if( lv1 == -1 || lv2 == -1  )
			throw new IllegalArgumentException();
		
		for( int j=0; j<3; j++ )
			if( lv1 != j && lv2 != j )
				return j;
		
		throw new IllegalStateException("ups");
	}
	
	private void applyMobiusTransformToDomain() {
		Complex z = new Complex();
		 Complex c = schottky.getCenters()[0][0];
		 double r = schottky.getRadius(0);
		 m = new Moebius(  0, 0, r, 0, 1, 0, -c.re, -c.im );
			
//		 m.assign( q, new Complex(r,0), new Complex(0,r),
//				   new Complex(0,0), new Complex(1,0), new Complex(0,1) );
//		 
		 
		 
		 
		 for( int i=0; i<xy.length; i+=2 ) {
			 z.assign( xy[i], xy[i+1] );			 
			 m.applyTo(z, z );
			 z.assignTimes(1e+1);
			 xy[i+0] = z.re;
			 xy[i+1] = z.im;
		 }
	}
	private void applyInverseTo( Complex z) {
	
			 z.assignTimes(1e-1);
			 m.applyInverseTo( z, z );
			 
	}
	
	double r(double alpha ) {
		return stereoGraphicProjection( new Real3( 
				0.5     * Math.sin( alpha ), 0, 
				0.5-0.5 * Math.cos( alpha ) ) ).re;	
	}
	double [] paddingRadii = null;
	private Moebius m;
	
	void addPaddingOnCircle( double R, Complex c, int discr, double shift ) {
		
		
		for( int j=0; j<discr; j++ ) {
			double alpha = (j+shift)*2*Math.PI / discr;
			
			Complex z = circle(R, alpha, c);
			
			if( validPoint(z) ) {
				try {
					addPoint(z);
				} catch( RuntimeException e ) {
					System.out.println(z + " is in no face ");
				}
			}
			else System.out.println("reject padding at circle");
		}
		
		domain.refine();
	}
	
	void addPaddingCircles( int wave ) {
		int g = schottky.getNumGenerators();
		
		int discr = domain.getCircleDiscr();
		
		if( paddingRadii == null ) paddingRadii = new double[g];
		
		for( int i=0; i<g; i++ ) {
			
			Complex c  = schottky.getCenterOfCircle(i, false);
			Complex c_ = schottky.getCenterOfCircle(i, true);
			
			double  R  = schottky.getRadius(i);
			
			double  r = 1.2* Math.sqrt(3) * Math.PI / discr * R * wave;
			
			Moebius m = schottky.getGenerator(i);
			
			double delta = 2*Math.PI / discr;
			double shift = wave % 2 == 1 ? delta / 2 : 0;
			
			for( int j=0; j<discr; j++ ) {
				
				double alpha = j*delta + shift;
				
				Complex z = circle(R+r, alpha, c);
				
				if( validPoint(z) )
					addPoint(z);
				else System.out.println("reject padding at circle");
				
				Complex w = m.applyTo( circle( R, alpha, c ) );
				
				w.assignMinus(c_);
				//System.out.println(R + "=" + w.abs() );
				
				
				w.assignTimes( (r+R)/R);
				w.assignPlus(c_);
				
				if( validPoint( w ) )
					addPoint(w);
				else System.out.println("reject padding at prime circle");
			}
			
			paddingRadii[i] = Math.max(paddingRadii[i], R+r);
			
		}
		
		domain.refine();
	}

	private void addPoint(Complex z) {
		domain.addPoint(z.re, z.im );
	}
	
	Complex circle( double r, double alpha, Complex center ) {
		Complex z = new Complex(center);
		z.re += (r)*Math.cos(alpha);
		z.im += (r)*Math.sin(alpha);
		
		return z;
	}
	
	boolean validPoint( Complex z ) {
		if ( z.abs() > D/2  )
			 return false;
	
		int g = schottky.getNumGenerators();
		
		int discr = domain.getCircleDiscr();
		
		if( paddingRadii == null )
			return schottky.isInFundamentalDomain(z);
		
		for( int i=0; i<g; i++ ) {
			
			Complex c  = schottky.getCenterOfCircle(i, false);
			Complex c_ = schottky.getCenterOfCircle(i, true);
			
			double  R  = schottky.getRadius(i);
			
			if( z.dist(c)*1.1<paddingRadii[i] || z.dist(c_ )*1.1< paddingRadii[i] )
				return false;
		}
		
		return true;
		 
	}
	
	void addRandomPoints( int numOfPoints ) {
		
		for( int i=0; i<numOfPoints; i++ ) {
			 
			 while(true) {
				 Real3 P = new Real3( 
						 Math.random()-0.5, 
						 Math.random()-0.5, 
						 Math.random()-0.5 );
				 
				 P.normalize();
				 P.assignTimes(0.5);
				 P.z += 0.5;
				 
				 Complex z = stereoGraphicProjection(P);
				 
				 if( !validPoint(z))
					 continue;
				 
				 addPoint(z);
				 
				 break;
			 }
		 }
		 
		 domain.refine();
	}
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//DiscreteSchottky dschottky = createDiscreteSchottkyWente();
		DiscreteSchottky dschottky = createDiscreteSchottkySimple();
		
		System.out.println( "pm = " + dschottky.schottky.getPeriodMatrix() );

		display(dschottky);
		

		HalfEdgeDataStructure ds = dschottky.g; 

		DiscreteRiemann dr = new DiscreteRiemann( new DiscreteConformalStructure(ds),null);
		
		ComplexMatrix pm = dr.getPeriodMatrix();
		System.out.println("period matrix: " + pm );
		pm.assignTimes( new Complex( 0, 2*Math.PI ) );
		System.out.println("period matrix: " + pm );
		ComplexMatrix pmRed = new SiegelReduction( pm ).getReducedPeriodMatrix();
		pmRed = pmRed.divide( new Complex( 0, 2*Math.PI ));
		System.out.println("reduced: " + pmRed);
		
		
		
	}
	
}
