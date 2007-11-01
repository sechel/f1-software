package discreteRiemann;

import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.List;

import halfedge.Edge;
import halfedge.Face;
import halfedge.HalfEdgeDataStructure;
import halfedge.Vertex;
import halfedge.generator.FaceByFaceGenerator;
import de.jreality.geometry.IndexedFaceSetFactory;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.ui.viewerapp.ViewerApp;
import de.jreality.writer.WriterOBJ;
import de.jtem.mfc.field.Complex;
import de.jtem.mfc.vector.Real3;
import discreteRiemann.DiscreteConformalStructure.ConfEdge;
import discreteRiemann.DiscreteConformalStructure.ConfFace;
import discreteRiemann.DiscreteConformalStructure.ConfVertex;

public abstract class DiscreteImmersionR3 <
V extends Vertex<V, E, F>,
E extends Edge<V, E, F> & HasRho, 
F extends Face<V, E, F> 
>  {
	
	static boolean useRhoByImmersion = true;
	
	/**
	 * Computes tangent of the half angle of the triangle unsing
	 * the half-angle theorem.
	 * @param a side length opposit of alpha
	 * @param b side length
	 * @param c side length
	 * @return tan(alpha/2)
	 */
	static double tanHalfAlpha( double a, double b, double c ) {
		return Math.sqrt( (a-b+c) * (a+b-c) / (a+b+c) /  (-a+b+c) );
	}
	
	static double cotanAlpha( Real3 A, Real3 B, Real3 C ) {
		return cotanAlpha( dist(B,C), dist(A,B), dist(A,C) );
	}
	
	static double cotanAlpha( double a, double b, double c ) {
		double tanHalfAlpha = tanHalfAlpha(a, b, c);
		
		return (1-tanHalfAlpha*tanHalfAlpha) / (2*tanHalfAlpha);
	}
	
	static double dist( Real3 A, Real3 B ) {
		return A.minus(B).norm();
	}
	
	static double rho(Real3 S, Real3 T, Real3 L, Real3 R) {
		
		double cotanAlpha = cotanAlpha(dist(T,S), dist(L,T), dist(L,S) );
		double cotanBeta  = cotanAlpha(dist(T,S), dist(R,T), dist(R,S) );
		
		double Rho =  2/ (cotanAlpha + cotanBeta);
		
		if( useRhoByImmersion )
			return Math.signum(Rho) / rhoByImmersion(S, T, L, R);
		else
			return 1 / Rho; 
	}

	static double rhoByImmersion(Real3 S, Real3 T, Real3 L, Real3 R) {
		return dist(S,T) / dist( circumCenter(S, T, L), circumCenter(S, T, R) );
	}
	
	static double rho_alt(Real3 S, Real3 T, Real3 L, Real3 R) {
		
		Real3 LS = L.minus(S);
		Real3 RS = R.minus(S);
		Real3 TS = T.minus(S);
		
		double LSN = LS.norm();
		double RSN = RS.norm();
		double TSN = TS.norm();
		
		Complex s = new Complex(0,0 );
		Complex t = new Complex(0,TSN);
		
		double ly = LS.dot(TS)/TSN;
		double lx = Math.sqrt( LSN * LSN - ly * ly);
		
		Complex l = new Complex( -lx, ly );
		
		double ry = RS.dot(TS)/TSN;
		double rx = Math.sqrt( RSN * RSN - ry * ry);
		
		Complex r = new Complex( rx, ry );
		
		//System.out.println(TSN + " = " + t.dist(s));
		//System.out.println(R.minus(S).norm() + " = " + r.dist(s));
		Complex a = t.minus(s);
		Complex b = circumCenter(s,t,r).minus(circumCenter(s,t,l));
		
		Complex rho =a.divide(b);
		//double rho = DiscreteSchottky.circumCenter(s,t,l).dist(DiscreteSchottky.circumCenter(s,l,r)) / s.dist(t);
		//System.out.println(rho);
		
		return rho.im;
	}

	String name = "surface";
	
	protected HalfEdgeDataStructure<V,E,F> g;
	
	protected FaceByFaceGenerator<V,E,F> generator;
	protected Real3 [] xyz;

	protected Real3 xyz(Vertex s) {
		return xyz[s.getIndex()];
	}

	protected double rho(V S, V T, V L, V R) {
		return DiscreteImmersionR3.rho( xyz(S), xyz(T), xyz(L), xyz(R) );
	}
	
	double cotanAlpha(V A, V B, V C) {
		return DiscreteImmersionR3.cotanAlpha(xyz(A), xyz(B), xyz(C));
	}
	
	void writeOBJ() {
		IndexedFaceSetFactory ifsf = createIFSF();
		
		IndexedFaceSet ifs = ifsf.getIndexedFaceSet();
		
		try {
			WriterOBJ.write(ifs, new FileOutputStream( new File( "/tmp/"+name+ifs.getNumPoints()+".obj") ) );
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	IndexedFaceSetFactory createIFSF() {
		IndexedFaceSetFactory ifsf = new IndexedFaceSetFactory();
		
		int nof = g.getNumFaces();
		int nov = g.getNumVertices();
		int noe = g.getNumEdges() / 2;
		
		double [] vertices = new double[nov*3];
		
		for( Vertex vertex : g.getVertices() ) {
			Real3 XYZ = xyz(vertex);
			int j = vertex.getIndex()*3;
			vertices[j+0] = XYZ.x;
			vertices[j+1] = XYZ.y;
			vertices[j+2] = XYZ.z;
		}
		int [][] faces = new int[nof][];		                                
		
		for( Face face : g.getFaces() ) {
			List<Edge> b = face.getBoundary();
			int j = face.getIndex();
			faces[j] = new int[b.size()];
			int i = 0;
			for( Edge fe : b ) {
				faces[j][i] = fe.getTargetVertex().getIndex();
				i++;
			}
			j++;
		}
		
		int [][] edges = new int[noe][2];		                                
		Color [] colors = new Color[noe];
		
		int i=0;
		for( Edge edge : g.getPositiveEdges() ) {
			
			edges[i][0] = edge.getStartVertex().getIndex();
			edges[i][1] = edge.getTargetVertex().getIndex();
			
			colors[i] = ((ConfEdge)edge).getRho() < 0 ? Color.RED : Color.BLACK;
			
			i++;
		}
		
		ifsf.setLineCount( noe );
		ifsf.setEdgeIndices(edges);
		//ifsf.setEdgeColors(colors);
		
		ifsf.setVertexCount(nov);
		ifsf.setVertexCoordinates(vertices);
		
		//ifsf.setLineCount(noe);
		
		ifsf.setFaceCount(nof);
		ifsf.setFaceIndices(faces);
		
		ifsf.setGenerateFaceNormals(true);
		ifsf.setGenerateVertexNormals(true);
		//ifsf.setGenerateEdgesFromFaces(true);
		
		//ifsf.setGenerateFaceLabels(true);
		//ifsf.setGenerateVertexLabels(true);
		
		ifsf.update();
		
		return ifsf;
	}

	
	static void display(DiscreteImmersionR3 dw) {
		ViewerApp app = new ViewerApp(dw.createIFSF().getGeometry());
		
		app.setAttachNavigator(true);
		app.setShowMenu(true);
		
		app.update();
		
		app.display();
	}
	
	protected void computeRho() {
		int negCount = 0;
		
		for( Edge edge : g.getPositiveEdges() ) {
			
			double rho = rho( edge );		
					
			((HasRho)edge).setRho( rho );
			
			((HasRho)edge.getOppositeEdge()).setRho( rho );
			
			if( rho < 0 ) {
				negCount++;
			}
	
		}
		System.out.println( 100.0*negCount / (double)g.getNumEdges()*2 + "% negative rhos");
		
	}

	double rho(Edge edge) {
		V S = (V)edge.getStartVertex();
		V T = (V)edge.getTargetVertex();
		V L = (V)edge.getNextEdge().getTargetVertex();
		V R = (V)edge.getOppositeEdge().getNextEdge().getTargetVertex();
		
		V S_= (V)edge.getNextEdge().getNextEdge().getTargetVertex();
		V T_= (V)edge.getOppositeEdge().getNextEdge().getNextEdge().getTargetVertex();
		
		if( S_ != S || T != T_ ) 
			throw new IllegalStateException("ups: face is not a trianagle?!");
		
		
		return rho( S, T, L, R );
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
		
	public static Real3 circumCenter( Real3 A, Real3 B, Real3 C ) {
		  double a = dist(B,C);
		  double b = dist(A,C);
		  double c = dist(A,B);

		  double alpha = Math.acos( ( b*b + c*c - a*a ) / (2*b*c) );
		  double beta  = Math.acos( ( c*c + a*a - b*b ) / (2*c*a) );
		  double gamma = Math.acos( ( a*a + b*b - c*c ) / (2*a*b) );

		  Real3 cc = new Real3();
		  
		  cc.assignLinearCombination( Math.sin(2*alpha ), A, Math.sin(2*beta), B  );
		  cc.assignLinearCombination( Math.sin(2*gamma ), C, 1, cc );
		  
		  cc.assignDivide( Math.sin(2*alpha ) + Math.sin(2*beta  ) +  Math.sin(2*gamma ) );
		  
		  return  cc;

		}
		
}
