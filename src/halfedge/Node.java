package halfedge;

import java.io.Serializable;

/**
 * The base class for all HalfEdge entities
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 */
public abstract class Node 
	<
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> 
implements Serializable, Cloneable{

	private static final long 
		serialVersionUID = 1L;

	private int 
		index = -1;
	
	private HalfEdgeDataStructure<V, E, F> 
		halfEdgeDataStructure = null;
	
	@Override
	protected Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
	
	/**
	 * Returns the index of this Vertex, Edge, or Face in the Vertex-, Edge-,
	 * or Facelist. Or -1 if it's not contained in any HalfedgeDataStructure.
	 * @return the index or -1
	 */
	public int getIndex() {
		checkHalfEdgeDataStructure();
		return index;
	}

	void setIndex(int index) {
		this.index = index;
	}

	public String toString() {
		return getClass().getSimpleName() + (halfEdgeDataStructure == null ? " (removed)" : " " + index);
	}

	void checkHalfEdgeDataStructure() throws RuntimeException {
		if (halfEdgeDataStructure == null) 
			throw new RuntimeException(this + " does not belong to any half-edge data structure.");
	}
	
	public void checkHalfEdgeDataStructure(Node<?,?,?> n) throws RuntimeException {
		checkHalfEdgeDataStructure();
		if (n != null && this.halfEdgeDataStructure != n.halfEdgeDataStructure) {
			throw new RuntimeException(this + " and " + n + " do not belong to the same half-edge data structure.");
		}
	}
	
	public boolean isValid(){
		return halfEdgeDataStructure != null;
	}
	
	void setHalfEdgeDataStructure(HalfEdgeDataStructure<V, E, F> halfEdgeDataStructure) {
		this.halfEdgeDataStructure = halfEdgeDataStructure;
	}

	public HalfEdgeDataStructure<V, E, F> getHalfEdgeDataStructure() {
		return halfEdgeDataStructure;
	}

}
