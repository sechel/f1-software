package halfedge;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * A Generic Half Edge Data Structure
 <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 *
 * @param <E> The edge class this graph should work with
 * @param <F>  The face class this graph should work with
 * @param <V>  The vertex class this graph should work with
 */
public class HalfEdgeDataStructure
	<
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F> 
	> implements Serializable{

	private static final long 
		serialVersionUID = 1L;
	public List<V>
		vertexList = new ArrayList<V>();
	public List<F>
		faceList = new ArrayList<F>();
	public List<E>
		edgeList = new ArrayList<E>();		
	
	private Class<V>
		vClass = null;
	private Class<E>
		eClass = null;	
	private Class<F>
		fClass = null;	
	
	protected HalfEdgeDataStructure(Class<V> vClass, Class<E> eClass, Class<F> fClass) {
		this.vClass = vClass;
		this.eClass = eClass;
		this.fClass = fClass;
	}
	
	
	public HalfEdgeDataStructure(HalfEdgeDataStructure<V, E, F> heds){
		this(heds, new HashMap<V, V>(), new HashMap<E, E>(), new HashMap<F, F>());
	}
	
	
	/**
	 * Copy constructor constructs a deep copy of the HalfEdgeStructure
	 * @param heds the structure to copy
	 */
	@SuppressWarnings("unchecked")
	public HalfEdgeDataStructure(HalfEdgeDataStructure<V, E, F> heds, HashMap<V, V> vertexMap, HashMap<E, E> edgeMap, HashMap<F, F> faceMap){
		// cloning nodes
		try{
			for (V v : heds.getVertices()){
				V cV = (V)v.clone();
				cV.setHalfEdgeDataStructure(this);
				vertexMap.put(v, cV);
				vertexList.add(cV);
			}
			for (E e : heds.getEdges()){
				E cE = (E)e.clone();
				cE.setHalfEdgeDataStructure(this);
				edgeMap.put(e, cE);
				edgeList.add(cE);
			}
			for (F f : heds.getFaces()){
				F cF = (F)f.clone();
				cF.setHalfEdgeDataStructure(this);
				faceMap.put(f, cF);
				faceList.add(cF);
			}
		} catch (CloneNotSupportedException cnse){}
		// linking new nodes
		for (V v : vertexList){
			v.connectedEdge = edgeMap.get(v.connectedEdge);
		}
		for (E e : edgeList){
			e.leftFace = faceMap.get(e.leftFace);
			e.nextEdge = edgeMap.get(e.nextEdge);
			e.oppositeEdge = edgeMap.get(e.oppositeEdge);
			e.previousEdge = edgeMap.get(e.previousEdge);
			e.targetVertex = vertexMap.get(e.targetVertex);
		}
		for (F f : faceList){
			f.boundaryEdge = edgeMap.get(f.boundaryEdge);
		}
		vClass = heds.vClass;
		eClass = heds.eClass;
		fClass = heds.fClass;
	}
	
	public  Class<E> getEdgeClass() {
		if (eClass == null)
			throw new RuntimeException("HEDS not initialized from createHEDS");
		return eClass;
	}
	
	public  Class<F> getFaceClass() {
		if (fClass == null)
			throw new RuntimeException("HEDS not initialized from createHEDS");
		return fClass;
	}

	public Class<V> getVertexClass() {
		if (vClass == null)
			throw new RuntimeException("HEDS not initialized from createHEDS");
		return vClass;
	}


	public static 
	<	VC extends Vertex<VC, EC, FC>, 
		EC extends Edge<VC, EC, FC>, 
		FC extends Face<VC, EC, FC>
	> 
	HalfEdgeDataStructure<VC, EC, FC> createHEDS(Class<VC> vClass, Class<EC> eClass, Class<FC> fClass){
		HalfEdgeDataStructure<VC, EC, FC> result = new HalfEdgeDataStructure<VC, EC, FC>(vClass, eClass, fClass);
		return result;
	}
	
	
	public 	<
		VC extends Vertex<VC, EC, FC>, 
		EC extends Edge<VC, EC, FC>, 
		FC extends Face<VC, EC, FC>
	> HalfEdgeDataStructure() {
	
		
	}
	
	
	public static HalfEdgeDataStructure <Vertex.Generic, Edge.Generic, Face.Generic> createHEDS() {
		return createHEDS(Vertex.Generic.class, Edge.Generic.class, Face.Generic.class);
	}
	
	public HalfEdgeDataStructure<V, E, F> newInstance(){
		return createHEDS(vClass, eClass, fClass);
	}
	
	public V addNewVertex() throws RuntimeException{
		V vertex = null;
		try {
			vertex = getVertexClass().newInstance();
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
		vertex.setIndex(vertexList.size());
		vertexList.add(vertex);
		vertex.setHalfEdgeDataStructure(this);
		return vertex;
	}
	
	public E addNewEdge(){
		E edge = null;
		try {
			edge = getEdgeClass().newInstance();
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
		edge.setIndex(edgeList.size());
		edgeList.add(edge);
		edge.setHalfEdgeDataStructure(this);
		return edge;
	}
	
	public F addNewFace(){
		F face = null;
		try {
			face = getFaceClass().newInstance();
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
		face.setIndex(faceList.size());
		faceList.add(face);
		face.setHalfEdgeDataStructure(this);
		return face;
	}

	
	public void clear(){
		vertexList.clear();
		edgeList.clear();
		faceList.clear();
	}
	
	
	
	/**
	 * Remove a face from this halfedge data structure. 
	 * Does nothing if <code>face</code> is <code>null</code> or 
	 * does not belong to this halfedge data structure. Otherwise, 
	 * edges with <code>face</code> as left face will have 
	 * <code>null</code> as left face after execution of this method.
	 * @param face the face to remove.
	 * @return true if this halfedge data structure changed due to this operation, false otherwise.
	 */
	public boolean removeFace(F face) {
		if (face == null)
			return false;
		if (removeNode(faceList, face)){
			face.boundaryEdge = null;
			face.setHalfEdgeDataStructure(null);
			return true;
		}
		return false;
	}
	
	/**
	 * Remove an edge from this halfedge data structure.
	 * Does nothing if <code>edge</code> is <code>null</code>
	 * or does not belong to this halfedge data structure. Otherwise,
	 * edge will not be linked with any edges, vertices, faces after
	 * execution of this method.
	 * @param edge the edge to remove.
	 * @return true if this halfedge data structure changed due to this operation, false otherwise.
	 */
	public boolean removeEdge(E edge){
		if (edge == null)
			return false;
		if (removeNode(edgeList, edge)) {
			edge.setLeftFace(null);
			edge.setTargetVertex(null);
			edge.linkOppositeEdge(null);
			edge.linkNextEdge(null);
			edge.linkPreviousEdge(null);
			edge.setHalfEdgeDataStructure(null);
			return true;
		}
		return false;
	}
	
	
	/**
	 * Removes both edge and edge.getOppositeEdge if not null
	 * @param edge
	 * @return true if this halfedge data structure changed due to this operation, false otherwise.
	 */
	public boolean removeEdgeAndOppositeEdge(E edge){
		E oppE = (edge == null) ? null : edge.getOppositeEdge();
		return removeEdge(oppE) | removeEdge(edge);
	}
	
	/**
	 * Remove a vertex from this halfedge data structure. 
	 * Does nothing if <code>vertex</code> is <code>null</code> or 
	 * does not belong to this halfedge data structure. Otherwise, 
	 * edges with <code>vertex</code> as target vertex will have
	 * <code>null</code> as target vertex after execution of this method.
	 * @param vertex the vertex to remove.
	 * @return true if this halfedge data structure changed due to this operation, false otherwise.
	 */
	public boolean removeVertex(V vertex){
		if (vertex == null)
			return false;
		if (removeNode(vertexList, vertex)){
			vertex.connectedEdge = null;
			vertex.setHalfEdgeDataStructure(null);
			return true;
		}
		return false;
	}
	
	private boolean removeNode(List<? extends Node<?,?,?>> nodeList, Node<?,?,?> n) {
		if (n == null)
			return false;
		if (nodeList.remove(n)) {
			reindexNodes(nodeList);
			return true;
		}
		return false;
	}
	
	private void reindexNodes(List<? extends Node<?,?,?>> nodeList){
		int i = 0;
		for (Node<?,?,?> n : nodeList){
			n.setIndex(i++);
		}
	}
	
	public V getVertex(int index){
		V v = vertexList.get(index);
		assert v.getIndex() == index;
		return v;
	}
	
	public E getEdge(int index){
		E e = edgeList.get(index);
		assert e.getIndex() == index;
		return e;
	}

	public F getFace(int index){
		F f = faceList.get(index);
		assert f.getIndex() == index;
		return faceList.get(index);
	}
	
	public int getNumFaces(){
		return faceList.size();
	}
	
	public int getNumEdges(){
		return edgeList.size();
	}
	
	public int getNumVertices(){
		return vertexList.size();
	}
	
	public List<F> getFaces(){
		return Collections.unmodifiableList(faceList);
	}
	
	public List<V> getVertices(){
		return Collections.unmodifiableList(vertexList);
	}
	
	public List<E> getEdges(){
		return Collections.unmodifiableList(edgeList);
	}
	
	
	public boolean hasVertex(V v) {
		return vertexList.contains(v);
	}
	
	public boolean hasEdge(E e) {
		return edgeList.contains(e);
	}
	
	public boolean hasFace(F f) {
		return faceList.contains(f);
	}
	
	private class SignatureEdgeIterator implements Iterator<E>{

		private boolean
			signature = false;
		private int
			actIntex = -1;
		private E
			nextEdge = null;
				
		public SignatureEdgeIterator(boolean signature) {
			this.signature = signature;
			nextEdge = getNextEdge();
		}
		
		private E getNextEdge(){
			while (++actIntex < edgeList.size()){
				E edge = edgeList.get(actIntex);
				if (edge.isPositive() == signature)
					return edge;
			}
			return null;
		}
		
		public boolean hasNext() {
			return nextEdge != null;
		}

		public E next() {
			E result = nextEdge;
			nextEdge = getNextEdge();
			return result;
		}

		public void remove() {
		}
		
	}
		
	public Iterable<E> getPositiveEdges(){
		return new Iterable<E>(){
			public Iterator<E> iterator() {
				return new SignatureEdgeIterator(true);
			}
		};
	}
	
	public Iterable<E> getNegativeEdges(){
		return new Iterable<E>(){
			public Iterator<E> iterator() {
				return new SignatureEdgeIterator(false);
			}
		};
	}
	
	@Override
	public String toString() {
		StringBuffer result = new StringBuffer();
		result.append("HEGraph --------\n");
		result.append("Vertices:\n");
		for (V vertex : vertexList)
			result.append("\t" + vertex + "\n");
		result.append("Faces:\n");
		for (F face : faceList)
			result.append("\t" + face + "\n");
		result.append("Edges:\n");
		for (E edge : edgeList)
			result.append("\t" + edge + "\t->" + "\t" + edge.targetVertex  + "\n");
		result.append("----------------");
		return result.toString();
	}
	
}
