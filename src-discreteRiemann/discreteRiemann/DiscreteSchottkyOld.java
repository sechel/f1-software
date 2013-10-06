package discreteRiemann;

import halfedge.Edge;
import halfedge.Face;
import halfedge.HalfEdgeDataStructure;
import halfedge.Vertex;
import halfedge.generator.FaceByFaceGenerator;
import halfedge.surfaceutilities.ConsistencyCheck;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.geom.GeneralPath;
import java.lang.reflect.Array;

import javax.swing.JFrame;
import javax.swing.border.BevelBorder;

import de.jtem.java2d.CoordinateGrid;
import de.jtem.java2d.SceneComponent;
import de.jtem.java2d.Viewer2D;
import de.jtem.java2d.ViewportChangeEvent;
import de.jtem.mfc.field.Complex;
import de.jtem.mfc.group.Moebius;
import de.jtem.riemann.schottky.Schottky;
import de.jtem.riemann.schottky.SchottkyDomain;
import discreteRiemann.DiscreteConformalStructure.ConfEdge;
import discreteRiemann.DiscreteConformalStructure.ConfFace;
import discreteRiemann.DiscreteConformalStructure.ConfVertex;

public class DiscreteSchottkyOld <
V extends Vertex<V, E, F>,
E extends Edge<V, E, F> & HasRho, 
F extends Face<V, E, F> 
>  {

	Schottky schottky;
	
	double [] rho;
	
	HalfEdgeDataStructure<V,E,F> g;
	
	GeneralPath graph  = new GeneralPath();
	GeneralPath border = new GeneralPath();
	GeneralPath dual   = new GeneralPath();
	
	DiscreteSchottkyOld( Schottky schottky, Class<V> vClass, Class<E> eClass, Class<F> fClass ) {

		g = HalfEdgeDataStructure.createHEDS(vClass, eClass, fClass);
			
		this.schottky = schottky;
		
		SchottkyDomain domain = computeSchottkyDomain(schottky);
		
		int nov   = domain.getNumOfPoints();
		int cd    = domain.getCircleDiscr();
		int genus = schottky.getNumGenerators();
		
		V []  vertex = (V[])  Array.newInstance(vClass, new int[]{nov}  ); 
		
		for( int i=0; i<nov; i++ ) {
			
			// here we do the idenfication of the circles:
			// there are 2*genus many, each consist of cd many vertices.
			// this circles come first in the array of vertices of the triangulation
			// and they are pairwise identified. this results in ....
			V v = i<2*genus*cd && (i/cd)%2==1 ? vertex[i-cd] : g.addNewVertex();
		
			vertex[i] = v;
		}
		
		FaceByFaceGenerator<V,E,F> generator = new FaceByFaceGenerator<V,E,F>(g);
		
		int [] index = domain.getIndices();
		
		for( int i=0; i<index.length/3; i++ ) {
			generator.addFace( vertex[index[3*i]], vertex[index[3*i+1]], vertex[index[3*i+2]]);
		}
		
		if( !ConsistencyCheck.isValidSurface(g) )
			throw new RuntimeException();
		
		if( g.getNumEdges() != index.length )
			throw new RuntimeException();
		
		this.rho = new double[g.getNumEdges()];
		
		double [][] rho = computeRho( domain, graph, border, dual );
		
		for( int i=0; i<rho.length; i++ ) {
			for( int j=0; j<3; j++ ) {
				E e = generator.findEdge( vertex[index[3*i+(j+1)%3]], vertex[index[3*i+(j+2)%3]]);
				
				this.rho[e.getIndex()] = rho[i][j];
			}
		}
		
		// check 
		for( int i=0; i<this.rho.length; i++ ) {
			if( this.rho[i] == 0 )
				throw new RuntimeException();
			
			E e = g.edgeList.get(i);
			if( this.rho[e.getIndex()] != this.rho[e.getOppositeEdge().getIndex()] )
				throw new RuntimeException();			
		}
		
	}
	
	public static 
	<	VC extends Vertex<VC, EC, FC>, 
		EC extends Edge<VC, EC, FC> & HasRho, 
		FC extends Face<VC, EC, FC>
	> 
	DiscreteSchottkyOld<VC, EC, FC> createSchottkyExample( Schottky schottky, Class<VC> vClass, Class<EC> eClass, Class<FC> fClass){
		DiscreteSchottkyOld<VC, EC, FC> result = new DiscreteSchottkyOld<VC, EC, FC>(schottky, vClass, eClass, fClass);
		return result;
	}
	
	public static DiscreteSchottkyOld createSchottkyExample( Schottky schottky ){
		return createSchottkyExample(schottky, ConfVertex.class, ConfEdge.class, ConfFace.class);
	}
	
	public static DiscreteSchottkyOld createSchottkyExample( int i ) {
		return createSchottkyExample(new Schottky(i));
	}
	


	public void display() {

		Viewer2D viewer = createViewer2dWithGrid();
		
		viewer.getRoot().addChild( createSC( graph,  2, Color.BLACK ));
		viewer.getRoot().addChild( createSC( border, 2, Color.GREEN ));
		viewer.getRoot().addChild( createSC( dual,   2, Color.PINK ));
		
		JFrame frame = new JFrame();
		frame.setSize(new Dimension(400,400));
		
		frame.add( viewer );
		
		frame.setVisible(true);
	}
	
	public static Complex circumCenter( Complex A, Complex B, Complex C ) {
	  double a = B.dist(C);
	  double b = A.dist(C);
	  double c = A.dist(B);

	  double alpha = Math.acos( ( b*b + c*c - a*a ) / (2*b*c) );
	  double beta  = Math.acos( ( c*c + a*a - b*b ) / (2*c*a) );
	  double gamma = Math.acos( ( a*a + b*b - c*c ) / (2*a*b) );

	  return         A.times( Math.sin(2*alpha ) )
		.plus( B.times( Math.sin(2*beta  ) ) )
	 	.plus( C.times( Math.sin(2*gamma ) ) ).divide( Math.sin(2*alpha ) + Math.sin(2*beta  ) +  Math.sin(2*gamma ));

	}
	
	static void addTriangle( GeneralPath path, Complex a, Complex b, Complex c ) {
		if( path == null ) 
			return;
		path.moveTo( (float)a.re, (float)a.im );
	    path.lineTo( (float)b.re, (float)b.im );
	    path.lineTo( (float)c.re, (float)c.im );
	    path.lineTo( (float)a.re, (float)a.im );
	}
	
	static void addEdge( GeneralPath path, Complex a, Complex b ) {
		if( path == null ) 
			return;
		path.moveTo( (float)a.re, (float)a.im );
	    path.lineTo( (float)b.re, (float)b.im );
	}
	
	static double [][] computeRho( SchottkyDomain domain, GeneralPath graph, GeneralPath borderTriangles, GeneralPath dualEdges  ) {
		
		Schottky schottky = domain.getSchottky();
		
		int discr = domain.getCircleDiscr();

		int [] index = domain.getIndices();
		double [] xy = domain.getPoints();
		int [] neighbor = domain.getNeighbors();
		int noe = index.length / 3;

		double [][] rho = new double[noe][3];

		 Complex [][] mappedCenter = new Complex[schottky.getNumGenerators()][discr];
		 int[][] bt  = new int[schottky.getNumGenerators()][discr];
		 int[][] btl = new int[schottky.getNumGenerators()][discr];

		 Complex z = new Complex();
		 Complex q = schottky.getCenters()[0][0];
		 double r = schottky.getRadius(0);
		 Moebius m = new Moebius(  0, 0, r, 0, 1, 0, -q.re, -q.im );
		 Moebius [] t = new Moebius[schottky.getNumGenerators()];

		 for( int i=0;i<schottky.getNumGenerators(); i++ ) {
		   t[i] = new Moebius( schottky.getA(i), schottky.getB(i), schottky.getMu(i) );
		   t[i].assignTimes(m,t[i]);
		   t[i].assignDivide(m);
		 }

		for( int i=0;i<noe; i++ ) {
		  for( int j=0; j<3; j++ ) {
		    int a = index[3*i+(j+1)%3];
		    int b = index[3*i+(j+2)%3];
		    int c = index[3*i+(j+0)%3];

		   Complex za = new Complex( xy[2*a], xy[2*a+1]);
		   Complex zb = new Complex( xy[2*b], xy[2*b+1]);
		   Complex zc = new Complex( xy[2*c], xy[2*c+1]); 

		   if( j==0 )
			   addTriangle( graph, za, zb, zc );
		   
		   int n =  neighbor[3*i+j];
		   if( n != -1 ) {
		     int o=0;
		     for( ; o<3; o++ ) {
		       if( neighbor[3*n+o] == i ) break;
		     }
		     if( o==3 ) 
		       throw new IllegalStateException("wrong neighborhood");

		     int d = index[3*n+o];
		      Complex zd = new Complex( xy[2*d], xy[2*d+1]); 
		      rho[i][j] = rho[n][o] =
		      circumCenter(za,zb,zc).dist(circumCenter(za,zb,zd)) / za.dist(zb);
		      
		      addEdge( dualEdges, circumCenter(za,zb,zc), circumCenter(za,zb,zd) );
		   } else {
		    if( (a / discr) % 2 == 0 ) {

		      int I = (a / discr) / 2;

		      int J = b-2*I*discr;

		      t[I].applyTo(za,za);
		      t[I].applyTo(zb,zb);
		      t[I].applyTo(zc,zc);

		      mappedCenter[I][J] = circumCenter(za,zb,zc);// za.plus(zb.plus(zc)).divide(3);
		      bt [I][J] = i;
		      btl[I][J] = j;

		      addTriangle( borderTriangles, za, zb, zc );

		      }
		    }
		  }
		}


		for( int i=0;i<noe; i++ ) {
		  for( int j=0; j<3; j++ ) {
		   if( neighbor[3*i+j] == -1 ) {
		    int a = index[3*i+(j+1)%3];
		    int b = index[3*i+(j+2)%3];
		    int c = index[3*i+(j+0)%3];

		    if( (a / discr) % 2 == 1 ) {

		      int I = (a / discr) / 2;

		      int J = a -(2*I+1)*discr;

		      Complex za = new Complex( xy[2*a], xy[2*a+1]);
		      Complex zb = new Complex( xy[2*b], xy[2*b+1]);
		      Complex zc = new Complex( xy[2*c], xy[2*c+1]); 

		      Complex center =  circumCenter(za,zb,zc);// za.plus(zb.plus(zc)).divide(3);

		      rho[i][j] = rho[bt [I][J]][btl[I][J]] =
		      center.dist(mappedCenter[I][J]) / za.dist(zb);

		      addEdge( dualEdges, center, mappedCenter[I][J] );
		      
		      }
		    }
		  }
		}

		return rho;
	}
	
	static SchottkyDomain computeSchottkyDomain( Schottky schottky ) {

		final Complex z = new Complex();
		final Complex c = schottky.getCenters()[0][0];
		final double r = schottky.getRadius(0);
		final Moebius m = new Moebius(  0, 0, r, 0, 1, 0, -c.re, -c.im );
		    
		 SchottkyDomain domain = new SchottkyDomain() {

		   @Override
		public double [][] getBoundary() {
		     double [][] b = super.getBoundary();

		     double [][] newB = new double[b.length-1][];
		     System.arraycopy(b,1,newB,0,newB.length);
		 
		     for( int i=0; i<newB.length; i++ ) {

		      for( int j=0; j<newB[i].length; j+=2 ) {
		        z.assign( newB[i][j], newB[i][j+1] );
		/*
		 * z.assignMinus(c); z.assignInvert(); z.assignTimes(r);
		 */

		        m.applyTo(z,z);
		      
		        newB[i][j]   = z.re;
		        newB[i][j+1] = z.im;
		      }

		     };
		     return newB;
		   }
		};

		domain.setSchottky(schottky);

		//domain.setMinAngle( 22 );
		domain.setCircleDiscr( 28 );
		//domain.setMaxArea(0.01 );
		domain.update();

		for( int index=0; index<schottky.getNumGenerators(); index++ ) {

		      Moebius g = new Moebius( schottky.getA(index), schottky.getB(index), schottky.getMu(index) );
		         
		      final int circleDiscr = domain.getCircleDiscr();

		      final Complex Z = new Complex();
		      final Complex R = new Complex();

		      final double rad1 = schottky.getRadius( index ) * (1+ Math.PI/circleDiscr*1.41);
		      final double rad2 = schottky.getRadius( index ) * (1- Math.PI/circleDiscr*1.41);
		   
		      final double cx = schottky.getCenters()[index][0].re;
		      final double cy = schottky.getCenters()[index][0].im;

		      for( int j=0, k=0; j<circleDiscr; j++ ) {
			
		         Z.re = cx + rad1 * Math.sin( 2 * (j+0.5) * Math.PI / circleDiscr );
		         Z.im = cy + rad1 * Math.cos( 2 * (j+0.5) * Math.PI / circleDiscr );
			
		         m.applyTo(Z,R);
		         //domain.addPoint(R.re,R.im);

		         Z.re = cx + rad2 * Math.sin( 2 * (j+0.5) * Math.PI / circleDiscr );
		         Z.im = cy + rad2 * Math.cos( 2 * (j+0.5) * Math.PI / circleDiscr );

		         g.applyTo(Z,Z);
		         m.applyTo(Z,R);
		         //domain.addPoint(R.re,R.im);
		      }
		}

		domain.refine();
		
		return domain;
	}
	
	static SceneComponent createSC( GeneralPath path, int width, Color color ) {
		SceneComponent self = new de.jtem.java2d.SceneComponent();
		self.setStroke(
		  new BasicStroke(
		    width,
		    BasicStroke.CAP_ROUND,
		    BasicStroke.JOIN_ROUND
		  )
		);
		self.setOutlinePaint( color );
		self.setFilled( Boolean.FALSE );
		self.setShape(path);
		return self;
	}
	
	static Viewer2D createViewer2dWithGrid() {
		Viewer2D self = new Viewer2D(); //Viewer2D.ENCOMPASS_ON_FIRST_RESIZE);
		self.setBorder(
		  new javax.swing.border.BevelBorder(
		    BevelBorder.LOWERED,
		    java.awt.Color.white,
		    java.awt.Color.black
		  )
		);
		self.setBackground(new java.awt.Color(225,225,225));
		self.setMaximumSize(
		  new java.awt.Dimension(32767,32767)
		);

// self.setKeepingAspectRatio(false);
		//self.setEncompassMargin(25);

		final CoordinateGrid grid = new CoordinateGrid();
// grid.setHorizontalUnit(" "+Character.toString('\u03C0'));
// grid.setVerticalUnit(" "+Character.toString('\u03C0'));
// grid.setKeepingAspectRatio(false);
		self.addViewportChangeListener(
		  new de.jtem.java2d.ViewportChangeListener() {
		    @Override
			public void viewportChange(ViewportChangeEvent event) {
		      grid.setRectangle(event.getViewport());
		      grid.fireAppearanceChange();
		    }
		  }
		);
		self.getBackdrop().addChild(grid);
		return self;

	}
	
	
	public static void main(String[] args) {
	
		DiscreteSchottkyOld example = createSchottkyExample(1);
		
		example.display();
	}
	
}
