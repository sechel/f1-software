package discreteRiemann;

import halfedge.Edge;
import halfedge.Face;
import halfedge.HalfEdgeDataStructure;
import halfedge.HalfEdgeUtility;
import halfedge.Vertex;
import halfedge.generator.FaceByFaceGenerator;
import halfedge.surfaceutilities.ConsistencyCheck;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import discreteRiemann.HarmonicFunction.OnGraph;

public class SubdivisionUtility <
V extends Vertex<V, E, F>,
E extends Edge<V, E, F>,
F extends Face<V, E, F>
> {

	private HalfEdgeDataStructure<V, E, F> heds;
	
	/** subdivided (sd) heds */
	private HalfEdgeDataStructure<V, E, F> sdHEDS;
	
	/** maps relate vertex, edge, face of heds to vertex in sdHEDS */
	private HashMap<V,V> vertexVertex;
	private HashMap<E,V> edgeVertex;
	private HashMap<F,V> faceVertex;
	
	/** map realtes faces of sdHEDS to subdivided face in heds */
	private HashMap<F,F> faceFace;
	
	//private HashMap<F,List<F>> subFaceList;
	
	private FaceByFaceGenerator<V,E,F> generator;

	private SubdivisionUtility( HalfEdgeDataStructure<V, E, F> heds ) {
		
		this.heds = heds;
		
		Class vClass = heds.getVertex(0).getClass();
		Class eClass = heds.getEdge  (0).getClass();
		Class fClass = heds.getFace  (0).getClass();
		
		sdHEDS = HalfEdgeDataStructure.createHEDS( vClass, eClass, fClass );
		
		if( heds instanceof DiscreteConformalStructure ) {
			sdHEDS = new DiscreteConformalStructure(sdHEDS);
		} 
		
		createVertexVertexMap(heds);
		createEdgeVertexMap(heds);
		createFaceVertexMap(heds);
		
		generator = new FaceByFaceGenerator<V,E,F>(sdHEDS);
		
		generateSubDividedFaces();
	
		if( !ConsistencyCheck.isValidSurface(sdHEDS) )
			throw new RuntimeException();
			
	}

	private void generateSubDividedFaces() {
		
		faceFace    = new HashMap<F,F>();
		//subFaceList = new HashMap<F,List<F>>();
		
		for( F face : heds.faceList ) {
			List<E> boundary = HalfEdgeUtility.boundary(face);
			//List<F> subFaces = new ArrayList<F>();
			//subFaceList.put(face,subFaces);
			for( E e : boundary ) {
				
				E n = e.getNextEdge();
				
				F f = generator.addFace( 
						vertexVertex.get( n.getStartVertex() ),
						edgeVertex  .get( n                  ),
						faceVertex  .get( face               ),
						edgeVertex  .get( e                  )
				);
				
				//subFaces.add(f);
				
				faceFace.put( f, face );
			}		
		}
	}

	private void createFaceVertexMap(HalfEdgeDataStructure<V, E, F> heds) {
		faceVertex = new HashMap<F,V>();
		for( F face : heds.faceList ) {
			V newVertex = sdHEDS.addNewVertex();
			faceVertex.put(face, newVertex );
		}
	}

	private void createEdgeVertexMap(HalfEdgeDataStructure<V, E, F> heds) {
		edgeVertex = new HashMap<E,V>();
		for( E edge : heds.edgeList ) {
			E opposite = edge.getOppositeEdge();
			if( opposite == null )
				throw new IllegalArgumentException( "quad graph must not have a boundary");
			V newVertex = edgeVertex.containsKey(opposite) ? 
					edgeVertex.get(opposite) : sdHEDS.addNewVertex();
			edgeVertex.put(edge, newVertex );
		}
	}

	private void createVertexVertexMap(HalfEdgeDataStructure<V, E, F> heds) {
		vertexVertex = new HashMap<V,V>();
		for( V vertex : heds.vertexList ) {
			V newVertex = sdHEDS.addNewVertex();
			vertexVertex.put(vertex, newVertex );
		}
	}
	
	private List<List<E>> subdivideCycles( List<List<E>> cycles ) {
	
		List<List<E>> subdivideCycles = new ArrayList<List<E>>();
	
		for( List<E> cycle : cycles ) {
			List<E> subdividedCycle = new ArrayList<E>(); 
			for( E edge : cycle ) {
				
				V startVertex  = vertexVertex.get( edge.getStartVertex() );
				V targetVertex = vertexVertex.get( edge.getTargetVertex() );
				V midVertex    = edgeVertex.get( edge );
				
				subdividedCycle.add( generator.findEdge( startVertex, midVertex ) );
				subdividedCycle.add( generator.findEdge(   midVertex, targetVertex ) );
			}
			subdivideCycles.add( subdivideCycles.size(),subdividedCycle);
		}
			
		return subdivideCycles;
	}
	
	public static 	
	<
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> HalfEdgeDataStructure<V, E, F> createSubdivisionOfGraph( 
			HalfEdgeDataStructure<V, E, F> heds ){
		return createSubdivisionOfGraph(heds,null,null);
	}
	
	public static 	
		<
			V extends Vertex<V, E, F>,
			E extends Edge<V, E, F>,
			F extends Face<V, E, F>
		> HalfEdgeDataStructure<V, E, F> createSubdivisionOfGraph( 
				HalfEdgeDataStructure<V, E, F> heds,
				List<List<E>> cycles, List<List<E>> subdivideCycles ){
	 		
		SubdivisionUtility sdu = new SubdivisionUtility(heds);
		
		if( cycles != null ) {
			subdivideCycles.clear();
			subdivideCycles.add(sdu.subdivideCycles(cycles));
		}
		
		return sdu.sdHEDS;
	}

	public static 	
	<
	V extends Vertex<V, E, F>,
	E extends Edge<V, E, F> & HasRho,
	F extends Face<V, E, F>
	> DiscreteRiemann<V, E, F> createSubdivisionOfDiscreteRiemann( 
		DiscreteRiemann<V, E, F> dr){
		
		SubdivisionUtility sdu = new SubdivisionUtility( dr.dcs );

		DiscreteConformalStructure sdDCS = (DiscreteConformalStructure)sdu.sdHEDS;
		
		sdDCS.setRho(1); //TODO: add interpolation scheme for rho; possible computing interface
		
		List<List<E>> sdBasisOnGraph = sdu.subdivideCycles( dr.basisOnGraph );
		
		List<List<E>> sdQuadCycles = CycleUtility.cyclesToQuads( sdBasisOnGraph );
		
		List<HarmonicFunction.OnGraph> harmonicsOnGraph = sdu.interpolateHarmonicsOnGraph( sdQuadCycles, dr.harmonicsOnGraph );
		List<HarmonicFunction.OnDual > harmonicsOnDual  = sdu.interpolateHarmonicsOnDual(  sdQuadCycles, dr.harmonicsOnDual );
		
		
		DiscreteRiemann<V, E, F> sdDR = new DiscreteRiemann( (DiscreteConformalStructure)sdu.sdHEDS, 
				sdBasisOnGraph, sdQuadCycles, harmonicsOnGraph, harmonicsOnDual );
					
		return sdDR;
	}
	
	List<HarmonicFunction.OnGraph> interpolateHarmonicsOnGraph( List<List<E>> quadBasis, List<HarmonicFunction.OnGraph> hf ) {
		List<HarmonicFunction.OnGraph> harmonicsOnGraph = new ArrayList<HarmonicFunction.OnGraph>(); 
		for( HarmonicFunction.OnGraph harmonic : hf ) {
			HarmonicFunction.OnGraph sdHF = new HarmonicFunction.OnGraph( (DiscreteConformalStructure)sdHEDS );
			sdHF.setQuadCycle( quadBasis.get( hf.indexOf(harmonic)));
			interpolateHarmonicsOnGraph( harmonic, sdHF );
			sdHF.compute();
//			HarmonicFunction.OnGraph sdHF = new HarmonicFunction.OnGraph( (DiscreteConformalStructure)sdHEDS, quadBasis.get( hf.indexOf(harmonic)) );
			harmonicsOnGraph.add(sdHF);
		}
		return harmonicsOnGraph;
	}
	
	void interpolateHarmonicsOnGraph( HarmonicFunction.OnGraph hf, HarmonicFunction.OnGraph sdHF) {
		//here you can make use of the eps
		for( V vertex : heds.vertexList ) {
			sdHF.f[vertexVertex.get(vertex).getIndex()] = hf.f[ vertex.getIndex() ];
		}
		
		for( E edge : heds.edgeList ) {
			int indexOfStartVertex  = edge.getStartVertex ().getIndex();
			int indexOfTargetVertex = edge.getTargetVertex().getIndex();
			
			int indexOfEdgeVertex   = edgeVertex.get(edge).getIndex();
			
			sdHF.f[indexOfEdgeVertex] = (hf.f[indexOfStartVertex]+hf.f[indexOfTargetVertex])/2;
		}
		
		for( F face : heds.faceList ) {
			List<E> boundary = HalfEdgeUtility.boundary(face);
			
			double value = 0;
			
			for( E edge : boundary ) {
				int indexOfTargetVertex = edge.getTargetVertex().getIndex();
				
				value += hf.f[indexOfTargetVertex];
			}		
		
			int indexOfFaceVertex = faceVertex.get(face).getIndex();
				
			sdHF.f[indexOfFaceVertex] = value / boundary.size();
		}		
	}
	
	List<HarmonicFunction.OnDual> interpolateHarmonicsOnDual( List<List<E>> quadBasis, List<HarmonicFunction.OnDual> hf ) {
		List<HarmonicFunction.OnDual> harmonicsOnDual = new ArrayList<HarmonicFunction.OnDual>(); 
		for( HarmonicFunction.OnDual harmonic : hf ) {
			HarmonicFunction.OnDual sdHF = new HarmonicFunction.OnDual( (DiscreteConformalStructure)sdHEDS );
			sdHF.setQuadCycle( quadBasis.get( hf.indexOf(harmonic)));
			interpolateHarmonicsOnDual( harmonic, sdHF );
			sdHF.compute();
			
//			HarmonicFunction.OnDual sdHF = new HarmonicFunction.OnDual( (DiscreteConformalStructure)sdHEDS, quadBasis.get( hf.indexOf(harmonic)) );
			harmonicsOnDual.add(sdHF);
		}
		return harmonicsOnDual;
	}
	
	void interpolateHarmonicsOnDual( HarmonicFunction.OnDual hf, HarmonicFunction.OnDual sdHF) {
		//here you can make use of the eps
		for( F sdFACE : sdHEDS.faceList ) {
			
			List<E> boundary = HalfEdgeUtility.boundary(sdFACE);
			
			assert boundary.size() == 4;		
			
			double value = 0;
			
			for( E edge : boundary ) {
				value += hf.f[ faceFace.get(edge.getRightFace()).getIndex()];
			}
			
			sdHF.f[sdFACE.getIndex()] = value / boundary.size();
		}		
	}
	
	
	
}
