package halfedge;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;


public abstract class Face	
	<
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>
	>
extends Node<V, E, F> {

	private static final long 
		serialVersionUID = 1L;
	
	protected E
		boundaryEdge = null;
	
	public static class Generic extends Face<Vertex.Generic, Edge.Generic, Face.Generic> {
		
		private static final long
			serialVersionUID = 1L;

		protected Face.Generic getThis() {
			return this;
		}
	}

	abstract protected F getThis();
	
	public E getBoundaryEdge() {
		return boundaryEdge;
	}

	public void setBoundaryEdge(E boundary) {
		checkHalfEdgeDataStructure(boundary);
		this.boundaryEdge = boundary;
		if (boundary != null && boundary.getLeftFace() != getThis())
			boundary.setLeftFace(getThis());
	};
	
	
	public List<E> getBoundary(){
		if (getBoundaryEdge() == null || !getBoundaryEdge().isValid())
			return Collections.emptyList();
		LinkedList<E> result = new LinkedList<E>();
		E e = getBoundaryEdge();
		do {
			if (e == null)
				//TODO: Think about open faces.
				return Collections.emptyList();
			result.add(e);
			e = e.getNextEdge();
		} while (e != getBoundaryEdge());
		return result;
	}

	
	public boolean isInteriorFace(){
		for (E e : getBoundary())
			if (!e.isInteriorEdge())
				return false;
		return true;
	}
	
	public boolean isBoundaryFace(){
		return !isInteriorFace();
	}
	
}
