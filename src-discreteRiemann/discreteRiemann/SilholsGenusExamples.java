package discreteRiemann;

import halfedge.Edge;
import halfedge.Face;
import halfedge.HalfEdgeDataStructure;
import halfedge.Vertex;
import halfedge.generator.FaceByFaceGenerator;
import halfedge.surfaceutilities.ConsistencyCheck;

import java.lang.reflect.Array;

import de.jtem.blas.IntegerMatrix;
import de.jtem.blas.RealMatrix;
import discreteRiemann.DiscreteConformalStructure.ConfEdge;
import discreteRiemann.DiscreteConformalStructure.ConfFace;
import discreteRiemann.DiscreteConformalStructure.ConfVertex;

import java.util.List;

public class SilholsGenusExamples {

		
	public static 	
	<
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> HalfEdgeDataStructure<V, E, F> create3QuadExample( Class<V> vClass, Class<E> eClass, Class<F> fClass){
		HalfEdgeDataStructure<V, E, F> ds = HalfEdgeDataStructure.createHEDS(vClass, eClass, fClass);
	
		int nov = 25;
		;
		V []   v = (V[])  Array.newInstance(vClass, new int[]{nov}  ); //new Vertex[m][n];
		
		for( int j=0; j<nov; j++ ) {
			v[j] = ds.addNewVertex();
		}
		
		FaceByFaceGenerator<V,E,F> generator = new FaceByFaceGenerator<V,E,F>(ds);
		
		generator.addFace( v[ 0], v[ 1], v[ 6], v[ 5] );
		generator.addFace( v[ 5], v[ 6], v[12], v[11] );
		generator.addFace( v[11], v[12], v[ 1], v[ 0] );
		generator.addFace( v[ 1], v[ 2], v[ 7], v[ 6] );
		generator.addFace( v[ 6], v[ 7], v[13], v[12] );
		generator.addFace( v[12], v[13], v[ 2], v[ 1] );
		generator.addFace( v[ 2], v[ 0], v[ 8], v[ 7] );
		generator.addFace( v[ 7], v[ 8], v[14], v[13] );
		generator.addFace( v[13], v[14], v[ 0], v[ 2] );
		generator.addFace( v[ 0], v[ 3], v[ 9], v[ 8] );
		generator.addFace( v[ 8], v[ 9], v[15], v[14] );
		generator.addFace( v[14], v[15], v[17], v[ 0] );
		generator.addFace( v[ 0], v[17], v[20], v[19] );
		generator.addFace( v[19], v[20], v[23], v[22] );
		generator.addFace( v[22], v[23], v[ 3], v[ 0] );
		generator.addFace( v[ 3], v[ 4], v[10], v[ 9] );
		generator.addFace( v[ 9], v[10], v[16], v[15] );
		generator.addFace( v[15], v[16], v[18], v[17] );
		generator.addFace( v[17], v[18], v[21], v[20] );
		generator.addFace( v[20], v[21], v[24], v[23] );
		generator.addFace( v[23], v[24], v[ 4], v[ 3] );
		generator.addFace( v[ 4], v[ 0], v[ 5], v[10] );
		generator.addFace( v[10], v[ 5], v[11], v[16] );
		generator.addFace( v[16], v[11], v[ 0], v[18] );
		generator.addFace( v[18], v[ 0], v[19], v[21] );
		generator.addFace( v[21], v[19], v[22], v[24] );
		generator.addFace( v[24], v[22], v[ 0], v[ 4] );
		
		if( !ConsistencyCheck.isValidSurface(ds) )
			throw new RuntimeException();
		
		return ds;
	}

	public static 	
	<
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> HalfEdgeDataStructure<V, E, F> create4QuadExample( Class<V> vClass, Class<E> eClass, Class<F> fClass){
		HalfEdgeDataStructure<V, E, F> ds = HalfEdgeDataStructure.createHEDS(vClass, eClass, fClass);
	
		int nov = 14;
		;
		V []   v = (V[])  Array.newInstance(vClass, new int[]{nov}  ); //new Vertex[m][n];
		
		for( int j=0; j<nov; j++ ) {
			v[j] = ds.addNewVertex();
		}
		
		FaceByFaceGenerator<V,E,F> generator = new FaceByFaceGenerator<V,E,F>(ds);
		
		generator.addFace( v[ 0], v[ 1], v[ 5], v[ 4] );
		generator.addFace( v[ 1], v[ 2], v[ 6], v[ 5] );
		generator.addFace( v[ 4], v[ 5], v[ 8], v[ 2] );
		generator.addFace( v[ 5], v[ 6], v[ 0], v[ 8] );
		
		generator.addFace( v[ 2], v[ 3], v[ 7], v[ 6] );
		generator.addFace( v[ 3], v[ 0], v[ 4], v[ 7] );
		generator.addFace( v[ 6], v[ 7], v[ 9], v[ 0] );
		generator.addFace( v[ 7], v[ 4], v[ 2], v[ 9] );
		
		generator.addFace( v[ 0], v[ 9], v[11], v[10] );
		generator.addFace( v[ 9], v[ 2], v[12], v[11] );
		generator.addFace( v[10], v[11], v[ 3], v[ 2] );
		generator.addFace( v[11], v[12], v[ 0], v[ 3] );
		
		generator.addFace( v[ 2], v[ 1], v[13], v[12] );
		generator.addFace( v[ 1], v[ 0], v[10], v[13] );
		generator.addFace( v[12], v[13], v[ 8], v[ 0] );
		generator.addFace( v[13], v[10], v[ 2], v[ 8] );
		
		CycleUtility.printVertices( ds.vertexList.get(0).getEdgeStar() );
		
		if( !ConsistencyCheck.isValidSurface(ds) )
			throw new RuntimeException();
		
		return ds;
	}
	
	public static 	
	<
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> HalfEdgeDataStructure<V, E, F> create6QuadExample( Class<V> vClass, Class<E> eClass, Class<F> fClass){
		HalfEdgeDataStructure<V, E, F> ds = HalfEdgeDataStructure.createHEDS(vClass, eClass, fClass);
	
		int nov = 22;
		
		V []   v = (V[])  Array.newInstance(vClass, new int[]{nov}  ); //new Vertex[m][n];
		
		for( int j=0; j<nov; j++ ) {
			v[j] = ds.addNewVertex();
		}
		
		FaceByFaceGenerator<V,E,F> generator = new FaceByFaceGenerator<V,E,F>(ds);
		
		
		generator.addFace( v[ 0], v[ 7], v[11], v[ 4] );
		generator.addFace( v[ 7], v[ 1], v[17], v[11] );
		generator.addFace( v[11], v[17], v[ 2], v[ 9] );
		generator.addFace( v[ 4], v[11], v[ 9], v[ 3] );
		
		generator.addFace( v[ 1], v[ 8], v[12], v[17] );
		generator.addFace( v[ 8], v[ 0], v[ 4], v[12] );
		generator.addFace( v[12], v[ 4], v[ 3], v[20] );
		generator.addFace( v[17], v[12], v[20], v[ 2] );
		
		generator.addFace( v[ 2], v[20], v[13], v[ 5] );
		generator.addFace( v[20], v[ 3], v[18], v[13] );
		generator.addFace( v[13], v[18], v[ 0], v[ 8] );
		generator.addFace( v[ 5], v[13], v[ 8], v[ 1] );
		
		generator.addFace( v[ 3], v[10], v[14], v[18] );
		generator.addFace( v[10], v[ 2], v[ 5], v[14] );
		generator.addFace( v[14], v[ 5], v[ 1], v[21] );
		generator.addFace( v[18], v[14], v[21], v[ 0] );
		
		generator.addFace( v[ 0], v[21], v[15], v[ 6] );
		generator.addFace( v[21], v[ 1], v[19], v[15] );
		generator.addFace( v[15], v[19], v[ 2], v[10] );
		generator.addFace( v[ 6], v[15], v[10], v[ 3] );
		
		generator.addFace( v[ 1], v[ 7], v[16], v[19] );
		generator.addFace( v[ 7], v[ 0], v[ 6], v[16] );
		generator.addFace( v[16], v[ 6], v[ 3], v[ 9] );
		generator.addFace( v[19], v[16], v[ 9], v[ 2] );
		
		
		CycleUtility.printVertices( ds.vertexList.get(0).getEdgeStar() );
		
		if( !ConsistencyCheck.isValidSurface(ds) )
			throw new RuntimeException();
		
		return ds;
	}

	public static 	
	<
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> HalfEdgeDataStructure<V, E, F> create2x4QuadExample( Class<V> vClass, Class<E> eClass, Class<F> fClass){
		HalfEdgeDataStructure<V, E, F> ds = HalfEdgeDataStructure.createHEDS(vClass, eClass, fClass);
	
		int nov = 28;
		
		V []   v = (V[])  Array.newInstance(vClass, new int[]{nov}  ); //new Vertex[m][n];
		
		for( int j=0; j<nov; j++ ) {
			v[j] = ds.addNewVertex();
		}
		
		FaceByFaceGenerator<V,E,F> generator = new FaceByFaceGenerator<V,E,F>(ds);
		
		
		generator.addFace( v[ 0], v[12], v[20], v[ 4] );
		generator.addFace( v[12], v[ 1], v[ 5], v[20] );
		generator.addFace( v[20], v[ 5], v[ 2], v[16] );
		generator.addFace( v[ 4], v[20], v[16], v[ 3] );
		
		generator.addFace( v[ 1], v[13], v[21], v[ 5] );
		generator.addFace( v[13], v[ 0], v[ 6], v[21] );
		generator.addFace( v[21], v[ 6], v[ 3], v[17] );
		generator.addFace( v[ 5], v[21], v[17], v[ 2] );
		
		generator.addFace( v[ 0], v[14], v[22], v[ 6] );
		generator.addFace( v[14], v[ 1], v[ 7], v[22] );
		generator.addFace( v[22], v[ 7], v[ 2], v[18] );
		generator.addFace( v[ 6], v[22], v[18], v[ 3] );
		
		generator.addFace( v[ 1], v[15], v[23], v[ 7] );
		generator.addFace( v[15], v[ 0], v[ 4], v[23] );
		generator.addFace( v[23], v[ 4], v[ 3], v[19] );
		generator.addFace( v[ 7], v[23], v[19], v[ 2] );
		
		generator.addFace( v[ 2], v[19], v[24], v[ 8] );
		generator.addFace( v[19], v[ 3], v[ 9], v[24] );
		generator.addFace( v[24], v[ 9], v[ 0], v[15] );
		generator.addFace( v[ 8], v[24], v[15], v[ 1] );
		
		generator.addFace( v[ 3], v[18], v[25], v[ 9] );
		generator.addFace( v[18], v[ 2], v[10], v[25] );
		generator.addFace( v[25], v[10], v[ 1], v[14] );
		generator.addFace( v[ 9], v[25], v[14], v[ 0] );
		
		generator.addFace( v[ 2], v[17], v[26], v[10] );
		generator.addFace( v[17], v[ 3], v[11], v[26] );
		generator.addFace( v[26], v[11], v[ 0], v[13] );
		generator.addFace( v[10], v[26], v[13], v[ 1] );
		
		generator.addFace( v[ 3], v[16], v[27], v[11] );
		generator.addFace( v[16], v[ 2], v[ 8], v[27] );
		generator.addFace( v[27], v[ 8], v[ 1], v[12] );
		generator.addFace( v[11], v[27], v[12], v[ 0] );		
		
		CycleUtility.printVertices( ds.vertexList.get(0).getEdgeStar() );
		
		if( !ConsistencyCheck.isValidSurface(ds) )
			throw new RuntimeException();
		
		return ds;
	}

	public static 	
	<
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> void addQuadQuad( FaceByFaceGenerator generator, V v00, V v01, V v02, V v10, V v11, V v12, V v20, V v21, V v22 ) {
		generator.addFace( v00, v01, v11, v10 );
		generator.addFace( v01, v02, v12, v11 );
		generator.addFace( v11, v12, v22, v21 );
		generator.addFace( v10, v11, v21, v20 );
	}
	
	public static 	
	<
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> void addQuadQuad( FaceByFaceGenerator generator, V [] v, int v00, int v01, int v02, int v10, int v11, int v12, int v20, int v21, int v22 ) {
		addQuadQuad(generator, v[v00], v[v01], v[v02], v[v10], v[v11], v[v12], v[v20], v[v21], v[v22]);
	}
	
	public static 	
	<
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> HalfEdgeDataStructure<V, E, F> create2x4bQuadExample( Class<V> vClass, Class<E> eClass, Class<F> fClass){
		HalfEdgeDataStructure<V, E, F> ds = HalfEdgeDataStructure.createHEDS(vClass, eClass, fClass);
	
		int nov = 28;
		
		V []   v = (V[])  Array.newInstance(vClass, new int[]{nov}  ); //new Vertex[m][n];
		
		for( int j=0; j<nov; j++ ) {
			v[j] = ds.addNewVertex();
		}
		
		FaceByFaceGenerator<V,E,F> generator = new FaceByFaceGenerator<V,E,F>(ds);
		
		
		addQuadQuad( generator, v, 16,  0, 17,  7, 20,  9, 18,  4, 19 );
		addQuadQuad( generator, v, 17,  1, 16,  9, 21, 10, 19,  5, 18 ); 
		addQuadQuad( generator, v, 16,  2, 17, 10, 22, 11, 18,  6, 19 );
		addQuadQuad( generator, v, 17,  3, 16, 11, 23,  7, 19, 12, 18 );
		addQuadQuad( generator, v, 19, 12, 18,  8, 24, 13, 17,  1, 16 );
		addQuadQuad( generator, v, 18,  6, 19, 13, 25, 14, 16,  0, 17 );
		addQuadQuad( generator, v, 19,  5, 18, 14, 26, 15, 17,  3, 16 );
		addQuadQuad( generator, v, 18,  4, 19, 15, 27,  8, 16,  2, 17 );
		
		CycleUtility.printVertices( ds.vertexList.get(0).getEdgeStar() );
		
		if( !ConsistencyCheck.isValidSurface(ds) )
			throw new RuntimeException();
		
		return ds;
	}

	public static 	
	<
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> HalfEdgeDataStructure<V, E, F> createGenus2By2x2QuadExample( Class<V> vClass, Class<E> eClass, Class<F> fClass){
		HalfEdgeDataStructure<V, E, F> ds = HalfEdgeDataStructure.createHEDS(vClass, eClass, fClass);
	
		int nov = 30;
		
		V []   v = (V[])  Array.newInstance(vClass, new int[]{nov}  ); //new Vertex[m][n];
		
		for( int j=0; j<nov; j++ ) {
			v[j] = ds.addNewVertex();
		}
		
		FaceByFaceGenerator<V,E,F> generator = new FaceByFaceGenerator<V,E,F>(ds);
		
		
		addQuadQuad( generator, v,  0,  1,  2,  4,  5,  6,  8,  9, 10 );
		addQuadQuad( generator, v,  2,  3,  0,  6,  7,  4, 10, 11,  8 );
		addQuadQuad( generator, v,  8, 12, 13, 15, 16, 17,  0, 23, 24 );
		addQuadQuad( generator, v, 13, 14,  8, 17, 18, 19, 24, 25,  0 );
		addQuadQuad( generator, v,  8,  9, 10, 19, 20, 21,  0,  1, 2 );
		addQuadQuad( generator, v, 10, 11,  8, 21, 22, 15,  2,  3,  0 );
		addQuadQuad( generator, v,  0, 23, 24, 26, 27, 28,  8, 12, 13 );
		addQuadQuad( generator, v, 24, 25,  0, 28, 29, 26, 13, 14,  8 );
		
		//addQuadQuad( generator, v,   ,   ,   ,   ,   ,   ,   ,   ,    );
		
		
		CycleUtility.printVertices( ds.vertexList.get(0).getEdgeStar() );
		
		if( !ConsistencyCheck.isValidSurface(ds) )
			throw new RuntimeException();
		
		return ds;
	}

	public static void main(String[] args) {
		HalfEdgeDataStructure ds = create2x4bQuadExample(ConfVertex.class, ConfEdge.class, ConfFace.class );
		
		System.out.println( ds.getNumEdges() );
	
		DiscreteRiemann dr = new DiscreteRiemann(  new DiscreteConformalStructure(ds),1);


		DirichletFunctional.Factory<ConfVertex, ConfEdge, ConfFace> factory = new DirichletFunctional.Factory(dr.dcs);
		
		List<List<Edge>> basisOnGraph = dr.basisOnGraph;
		List<List<Edge>> quadBasis   = CycleUtility.cyclesToQuads(basisOnGraph);
		List<List<Edge>> basisOnDual = CycleUtility.quadsToDualCycles(quadBasis);

		RealMatrix  wg = new RealMatrix( HarmonicUtility.cohomologyBasisOnGraph(quadBasis,factory) );
	
		IntegerMatrix im = IntegerMatrix.round( 
				new RealMatrix( CycleUtility.periods(wg.re, basisOnGraph) ) );
		System.out.println("periods"+ im );
	
		IntegerMatrix P       = HomologyUtility.createNormalizedBasis( im );
		System.out.println("Normalization"+ P );
	
		System.out.println("graph:"+P.times(im.times(P.transpose())));
	
		RealMatrix  wd = new RealMatrix( HarmonicUtility.cohomologyBasisOnDual(quadBasis,factory) );
		
		IntegerMatrix imdual = IntegerMatrix.round( 
				new RealMatrix( CycleUtility.periods(wd.re, basisOnDual) ) );
		
//		System.out.println("dual:"+P.transpose().times(imdual.times(P))); // Wrong one, just checking.
		System.out.println("dual:"+P.times(imdual.times(P.transpose())));

		
		System.out.println(dr.periodMatrixOnGraph);
		
//		for( List<Edge> path : list ) {
//			CycleUtility.printVertices(path);
//		}
//		System.out.println( im );
	}
}
