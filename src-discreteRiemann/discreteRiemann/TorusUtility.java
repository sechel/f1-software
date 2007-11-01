package discreteRiemann;

import halfedge.Edge;
import halfedge.Face;
import halfedge.HalfEdgeDataStructure;
import halfedge.HalfEdgeUtility;
import halfedge.Vertex;
import halfedge.generator.FaceByFaceGenerator;
import halfedge.surfaceutilities.ConsistencyCheck;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class TorusUtility {


	/**
	 * @param <V>
	 * @param <E>
	 * @param <F>
	 * @param m the width
	 * @param n the height
	 * @param vClass
	 * @param eClass
	 * @param fClass
	 * @return an nxm rectangular torus
	 */
	public static 	
		<
			V extends Vertex<V, E, F>,
			E extends Edge<V, E, F>,
			F extends Face<V, E, F>
		> HalfEdgeDataStructure<V, E, F> createTorus( int m, int n, Class<V> vClass, Class<E> eClass, Class<F> fClass){
		return createTorus(m, n, 0, vClass,eClass, fClass);
	}
	
	/**
	 * @param <V>
	 * @param <E>
	 * @param <F>
	 * @param m the width
	 * @param n the height
	 * @param twist The ith top face is glued back to the i+twist bottom face. 
	 * @param vClass
	 * @param eClass
	 * @param fClass
	 * @return an nxm rectangular torus
	 */
	public static 	
		<
			V extends Vertex<V, E, F>,
			E extends Edge<V, E, F>,
			F extends Face<V, E, F>
		> HalfEdgeDataStructure<V, E, F> createTorus( int m, int n, int twist, Class<V> vClass, Class<E> eClass, Class<F> fClass){
			HalfEdgeDataStructure<V, E, F> ds = HalfEdgeDataStructure.createHEDS(vClass, eClass, fClass);
		
		V [][]   vertices = (V[][])  Array.newInstance(vClass, new int[]{m,n}  ); //new Vertex[m][n];
		E [][][] vEdges   = (E[][][])Array.newInstance(eClass, new int[]{m,n,2}); //new Edge[m][n][2];
		E [][][] hEdges   = (E[][][])Array.newInstance(eClass, new int[]{m,n,2}); //new Edge[m][n][2];
		F [][]   faces    = (F[][])  Array.newInstance(fClass, new int[]{m,n}  ); //new Face[m][n];
		
		for( int j=0; j<n; j++ ) {
			for( int i=0; i<m; i++ ) {
				vertices[i][j] = ds.addNewVertex();
				faces[i][j] = ds.addNewFace();
			}
		}
		
		for( int j=0; j<n; j++ ) {
			for( int i=0; i<m; i++ ) {
				E vp = vEdges[i][j][0] = ds.addNewEdge();
				E vn = vEdges[i][j][1] = ds.addNewEdge();
				E hp = hEdges[i][j][0] = ds.addNewEdge();
				E hn = hEdges[i][j][1] = ds.addNewEdge();
				
				vp.linkOppositeEdge(vn);
				hp.linkOppositeEdge(hn);

				vp.setIsPositive(true);
				hp.setIsPositive(true);
				
				hp.setTargetVertex( vertices[(i+1)%m][ j     ] );
				vp.setTargetVertex( vertices[ (i + ((j == n-1)? m+twist: 0)) % m ][(j+1)%n] );
				vn.setTargetVertex( vertices[ i     ][ j     ] );
				hn.setTargetVertex( vertices[ i     ][ j     ] );

				vp.setLeftFace( faces[(i+m-1)%m][ j     ] );
				hn.setLeftFace( faces[ (i - (j == 0? twist -m : 0)   ) %m ][(j+n-1)%n] );
				vn.setLeftFace( faces[ i     ][ j     ] );
				hp.setLeftFace( faces[ i     ][ j     ] );
				
			}
		}
		
		for( int j=0; j<n; j++ ) {
			for( int i=0; i<m; i++ ) {
				hEdges[i][j][0].linkNextEdge(vEdges[(i+1)%m][j][0]);	
				vEdges[(i+1)%m][j][0].linkNextEdge(hEdges[(i + ((j == n-1)? m+twist: 0)) % m][(j+1)%n][1]);
				hEdges[(i + ((j == n-1)? m+twist: 0)) % m][(j+1)%n][1].linkNextEdge(vEdges[i][j][1]);
				vEdges[i][j][1].linkNextEdge(hEdges[i][j][0]);
			}
		}
			
		if( !ConsistencyCheck.isValidSurface(ds) )
			throw new RuntimeException();
		
		return ds;
	}

	/**
	 * @param <V>
	 * @param <E>
	 * @param <F>
	 * @param m the width
	 * @param n the height
	 * @param twist The ith top face is glued back to the i+twist bottom face. 
	 * @param vClass
	 * @param eClass
	 * @param fClass
	 * @return an nxm rectangular torus
	 */
	public static 	
		<
			V extends Vertex<V, E, F>,
			E extends Edge<V, E, F>,
			F extends Face<V, E, F>
		> HalfEdgeDataStructure<V, E, F> createTorusNew( int m, int n, int twist, Class<V> vClass, Class<E> eClass, Class<F> fClass){
			HalfEdgeDataStructure<V, E, F> ds = HalfEdgeDataStructure.createHEDS(vClass, eClass, fClass);
		
		V [][]   vertex = (V[][])  Array.newInstance(vClass, new int[]{m,n}  ); //new Vertex[m][n];
		
		for( int j=0; j<n; j++ ) {
			for( int i=0; i<m; i++ ) {
				vertex[i][j] = ds.addNewVertex();
			}
		}
		
		FaceByFaceGenerator<V,E,F> generator = new FaceByFaceGenerator<V,E,F>(ds);
		
		for( int j=0; j<n-1; j++ ) {
			for( int i=1; i<m+1; i++ ) {
				generator.addFace(
						vertex[i-1][j  ],vertex[i%m][j],
						vertex[i%m][j+1],vertex[i-1][j+1] );
			}
		}
		
		//last row we add the twist
		for( int i=1; i<=m; i++ ) {
			generator.addFace(
					vertex[i-1][n-1],vertex[i%m][n-1],
					vertex[(i+twist)%m][0],vertex[(i+twist-1)%m][0] );
		}
		
		if( !ConsistencyCheck.isValidSurface(ds) )
			throw new RuntimeException();
		
		return ds;
	}

	/**
	 * @param <V>
	 * @param <E>
	 * @param <F>
	 * @param m the width
	 * @param n the height
	 * @param twist The ith top face is glued back to the i+twist bottom face. 
	 * @param vClass
	 * @param eClass
	 * @param fClass
	 * @return an nxm rectangular torus
	 */
	public static 	
		<
			V extends Vertex<V, E, F>,
			E extends Edge<V, E, F>,
			F extends Face<V, E, F>
		> HalfEdgeDataStructure<V, E, F> createTriangularTorus( int m, int n, int twist, Class<V> vClass, Class<E> eClass, Class<F> fClass){
			HalfEdgeDataStructure<V, E, F> ds = HalfEdgeDataStructure.createHEDS(vClass, eClass, fClass);
		
		V [][]   vertex = (V[][])  Array.newInstance(vClass, new int[]{m,n}  ); //new Vertex[m][n];
		
		for( int j=0; j<n; j++ ) {
			for( int i=0; i<m; i++ ) {
				vertex[i][j] = ds.addNewVertex();
			}
		}
		
		FaceByFaceGenerator<V,E,F> generator = new FaceByFaceGenerator<V,E,F>(ds);
		
		for( int j=0; j<n-1; j++ ) {
			for( int i=1; i<m+1; i++ ) {
				generator.addFace(
						vertex[i-1][j],vertex[i%m][j],
						vertex[i%m][j+1] );
				generator.addFace(
						vertex[i-1][j],
						vertex[i%m][j+1],vertex[i-1][j+1] );
			}
		}
		
		//last row we add the twist
		for( int i=1; i<=m; i++ ) {
			generator.addFace(
					vertex[i-1][n-1],vertex[i%m][n-1],
					vertex[(i+twist)%m][0] );
			generator.addFace(
					vertex[i-1][n-1],
					vertex[(i+twist)%m][0],vertex[(i+twist-1)%m][0] );
		}
		
		if( !ConsistencyCheck.isValidSurface(ds) )
			throw new RuntimeException();
		
		return ds;
	}

	public static 	
	<
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> HalfEdgeDataStructure<V, E, F> createTriangularTorus( int m, int n, Class<V> vClass, Class<E> eClass, Class<F> fClass){
	return createTriangularTorus(m, n, 0, vClass,eClass, fClass);
}
	
	/**
	 * @param <V>
	 * @param <E>
	 * @param <F>
	 * @param edge the edge to start with
	 * @return a sequence of zic-zac quads (really edges) next & next.opposite
	 */
	public static 
	<
	V extends Vertex<V, E, F>,
	E extends Edge<V, E, F>,
	F extends Face<V, E, F>
	> ArrayList<E> getZicZacCycle( E edge ){
	
		ArrayList<E> eV = new ArrayList<E>();
		
		E e = edge;
		
		int mod4 = 0;

		// Vertical strip
		 do {
			eV.add(e);
			switch (mod4){
			case 0:
				e = e.getNextEdge();
				break;
			case 1:	
				e = e.getNextEdge().getOppositeEdge();
				break;
			case 2:
				e = e.getPreviousEdge().getOppositeEdge();
				break;
			case 3:
				e = e.getOppositeEdge().getPreviousEdge().getOppositeEdge();
			}
			mod4 = (mod4 + 1) %4;
		} while( !eV.contains(e));
		
		 if( e != edge )
			 throw new RuntimeException( "zic zac failed to create proper cycle");
		 
		 return eV;	
	}

	/**
	 * Gives a cycle along which to integrate. This is simply a "drunken man cycle": wander in a direction
	 * until you hit the 1 
	 * @param <V>
	 * @param <E>
	 * @param <F>
	 * @param edge the edge along which to go
	 * @return the cycle given by a sequence of next.opposite.next 
	 */
	public static 
	<
	V extends Vertex<V, E, F>,
	E extends Edge<V, E, F>,
	F extends Face<V, E, F>
	> ArrayList<E> getCycle( E edge ){
	
		ArrayList<E> eL = new ArrayList<E>();
		
		E e = edge;
		boolean flag = true;
	
		 do {
			eL.add(e);
			if(flag = !flag){
			e = e.getNextEdge().getOppositeEdge().getNextEdge();
			}
			else {
			e = e.getOppositeEdge().getPreviousEdge().getOppositeEdge().getPreviousEdge().getOppositeEdge();
			}
		} while( !eL.contains(e));
		
		 if( e != edge )
			 throw new RuntimeException( "zic zac failed to create propper cycle");
		 
		 return eL;	
	}
}
