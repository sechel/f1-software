package halfedge;


/**
 * The abstract edge class 
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 */
abstract public class Edge
	<
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>
	> 
extends Node<V, E, F> {

	private static final long 
		serialVersionUID = 2940868667290894997L;
	protected E
		nextEdge = null,
		previousEdge = null,
		oppositeEdge = null;
	protected V
		targetVertex = null;
	protected F
		leftFace = null;
	protected boolean
		isPositive = false;
	
	public static class Generic extends Edge<Vertex.Generic, Edge.Generic, Face.Generic> {
		
		private static final long 
			serialVersionUID = 1L;

		@Override
		protected Edge.Generic getThis() {
			return this;
		}

	}; 
	
	
	abstract protected E getThis();
	
	public F getLeftFace() {
		if (leftFace != null && leftFace.getHalfEdgeDataStructure() == null) {
			// face has been removed.
			leftFace = null;
		}
		assert leftFace == null || leftFace.getHalfEdgeDataStructure() == this.getHalfEdgeDataStructure();
		return leftFace;
	}

	public F getRightFace(){
		if (oppositeEdge == null)
			return null;
		return oppositeEdge.getLeftFace();
	}

	public void setLeftFace(F f) {
		checkHalfEdgeDataStructure(f);
//		 release old connection if present
		if (leftFace != null && leftFace.getBoundaryEdge() == getThis())
			leftFace.setBoundaryEdge(null);
		this.leftFace = f;
		if (leftFace != null && leftFace.getBoundaryEdge() != getThis())
			leftFace.setBoundaryEdge(getThis());
	}

	public E getNextEdge() {
		assert nextEdge == null || (this == this.nextEdge.previousEdge 
									&& this.getHalfEdgeDataStructure() == nextEdge.getHalfEdgeDataStructure());
		return nextEdge;
	}

	public void linkNextEdge(E nextEdge) {
		checkHalfEdgeDataStructure(nextEdge);
		if (this.nextEdge != null) {
			this.nextEdge.previousEdge = null;
		}
		if (nextEdge != null) {
			if (nextEdge.previousEdge != null) {
				nextEdge.previousEdge.nextEdge = null;
			}
			nextEdge.previousEdge = getThis();
		}
		this.nextEdge = nextEdge;
	}
	
	public void linkPreviousEdge(E previousEdge) {
		checkHalfEdgeDataStructure(previousEdge);
		if (this.previousEdge != null) {
			this.previousEdge.nextEdge = null;
		}
		if (previousEdge != null) {
			if (previousEdge.nextEdge != null) {
				previousEdge.nextEdge.previousEdge = null;
			}
			previousEdge.nextEdge = getThis();
		}
		this.previousEdge = previousEdge;
	}
	
	public E getOppositeEdge() {
		assert oppositeEdge == null || (oppositeEdge != this 
										&& this == oppositeEdge.oppositeEdge
										&& this.getHalfEdgeDataStructure() == oppositeEdge.getHalfEdgeDataStructure());
		return oppositeEdge;
	}

	/**
	 * Set opposite edge, and if <code>oppositeEdge!=null</code>
	 * adjust value of <code>oppositeEdge</code>.{@link #isPositive()}.
	 * @param oppositeEdge may be null.
	 */
	public void linkOppositeEdge(E oppositeEdge) {
		checkHalfEdgeDataStructure(oppositeEdge);
		if (this.oppositeEdge == oppositeEdge) {
			return;
		}
		if (this == oppositeEdge) {
			throw new RuntimeException("Opposite edge cannot be this edge.");
		}
		if (this.oppositeEdge != null) {
			this.oppositeEdge.oppositeEdge = null;
		}
		if (oppositeEdge != null) {
			if (oppositeEdge.oppositeEdge != null) {
				oppositeEdge.oppositeEdge.oppositeEdge = null;
			}
			oppositeEdge.oppositeEdge = this.getThis();
			oppositeEdge.isPositive = ! isPositive;
		}
		this.oppositeEdge = oppositeEdge;
	}

	public E getPreviousEdge() {
		assert previousEdge == null || (this == previousEdge.nextEdge
										&& this.getHalfEdgeDataStructure() == previousEdge.getHalfEdgeDataStructure());
		return previousEdge;
	}

	public V getTargetVertex() {
		if (targetVertex != null && targetVertex.getHalfEdgeDataStructure() == null)
			targetVertex = null;
		assert targetVertex == null || this.getHalfEdgeDataStructure() == targetVertex.getHalfEdgeDataStructure();
		return targetVertex;
	}


	public void setTargetVertex(V v) {
		checkHalfEdgeDataStructure(v);
		// release old connection if present
		if (targetVertex != null && targetVertex.getConnectedEdge() == getThis())
			getTargetVertex().setConnectedEdge(null);
		this.targetVertex = v;
		if (targetVertex != null && targetVertex.getConnectedEdge() != getThis())
			targetVertex.setConnectedEdge(this.getThis());
	}
	
	
	public V getStartVertex(){
		if (oppositeEdge == null) 
			return null;
		return oppositeEdge.getTargetVertex();
	}
	
	public boolean isInteriorEdge(){
		return getLeftFace() != null && getOppositeEdge().getLeftFace() != null;
	}
	
	public boolean isBoundaryEdge(){
		return getLeftFace() == null || getOppositeEdge().getLeftFace() == null;
	}

	public boolean isPositive() {
		assert oppositeEdge == null || this.isPositive != oppositeEdge.isPositive;
		return isPositive;
	}

	/**
	 * Set value returned by {@link #isPositive()}, and if {@link #getOppositeEdge()} 
	 * is not <code>null</code> adjust value returned by {@link #getOppositeEdge()}.{@link #isPositive()}.  
	 * @param signature true for positive, false for negative.
	 */	
	public void setIsPositive(boolean signature) {
		this.isPositive = signature;
		if (oppositeEdge != null) {
			oppositeEdge.isPositive = !signature;
		}
	}

}
