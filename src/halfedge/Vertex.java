package halfedge;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;


public abstract class Vertex 
	<
	    V extends Vertex<V, E, F>,
	    E extends Edge<V, E, F>,
	    F extends Face<V, E, F>
	> 
extends Node<V, E, F> {

    private static final long
        serialVersionUID = 1L;

    protected E
        connectedEdge = null;

    public static class Generic extends Vertex<Vertex.Generic, Edge.Generic, Face.Generic> {

        private static final long
            serialVersionUID = 1L;
        
        @Override
		protected Vertex.Generic getThis() {
            return this;
        }
        
    }
    
    abstract protected V getThis();

    public E getConnectedEdge() {
    	assert connectedEdge == null || this == connectedEdge.getTargetVertex();
        return connectedEdge;
    }

    /**
     * Return the vertex star of the current vertex
     * @return List of vertices adjacent to current vertex
     */
    public List<V> getVertexStar() {
        List<V> vertexStar = new ArrayList<V>();
        E actEdge = getConnectedEdge();
        do {
        	if (actEdge.getStartVertex() != null)
        		vertexStar.add(actEdge.getStartVertex());
        	actEdge = actEdge.getNextEdge().getOppositeEdge();
        } while (actEdge != getConnectedEdge());
        return vertexStar;
    }

    /**
     * Return the edge star of the current vertex
     * @return List of edges adjacent to current vertex, which is their targetVertex
     */
    public List<E> getEdgeStar(){
    	List<E> edgeStar = new LinkedList<E>();
    	if (getConnectedEdge() == null || !getConnectedEdge().isValid())
    		return Collections.emptyList();
        E actEdge = getConnectedEdge();
        do {
        	if (actEdge == null)
        		return Collections.emptyList();
        	edgeStar.add(actEdge);
        	if (actEdge.getNextEdge() == null)
        		return Collections.emptyList();
        	actEdge = actEdge.getNextEdge().getOppositeEdge();
        } while (actEdge != getConnectedEdge());
    	return edgeStar;
    }




    protected void setConnectedEdge(E incidentEdge) {
        checkHalfEdgeDataStructure(incidentEdge);
        if (incidentEdge != null && incidentEdge.getTargetVertex() != getThis())
        	incidentEdge.setTargetVertex(null);
        this.connectedEdge = incidentEdge;
        if (incidentEdge != null && incidentEdge.getTargetVertex() != getThis())
        	incidentEdge.setTargetVertex(getThis());
    }


    /**
     * Return the face star of the current vertex
     * NOTE: untested
     * @return List of faces adjacent to current vertex
     */
    public List<F> getFaceStar() {
        List<F> faceStar = new ArrayList<F>();
        for (E e : getEdgeStar()){
        	if (e.getLeftFace() != null)
        		faceStar.add(e.getLeftFace());
        }
        return faceStar;
    }

    
    public boolean isOnBoundary() {
        for ( E e : getEdgeStar()) {
           if (e.getLeftFace() == null || e.getRightFace() == null) 
        	   	return true;
        }
        return false;
    }

    
    public List<E> getBoundaryEdges(){
    	List<E> edgeStar = new LinkedList<E>();
        E actEdge = getConnectedEdge();
        do {
        	if (actEdge.getLeftFace()==null || actEdge.getRightFace() == null)  edgeStar.add(actEdge);
        	actEdge = actEdge.getNextEdge().getOppositeEdge();
        } while (actEdge != getConnectedEdge());
    	return edgeStar;
    }

}
