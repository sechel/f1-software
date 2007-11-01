package halfedge.surfaceutilities;

import halfedge.Edge;
import halfedge.Face;
import halfedge.HalfEdgeDataStructure;
import halfedge.Vertex;
import halfedge.decorations.HasAngle;
import halfedge.decorations.HasLength;
import halfedge.decorations.HasXYZW;
import halfedge.decorations.IsBoundary;
import halfedge.decorations.IsFlippable;

import javax.vecmath.Point4d;
import javax.vecmath.Vector4d;

/**
 * This is a vertex in an embedding. This used both for an embedded edge (straight line),
 * intermediately (but NOT in a HEDs). Then we also use it as real vertices for an embedded graph.
 * @author Kristoffer Josefsson
 *
 * @param <V>
 * @param <E>
 * @param <F>
 */

public class EmbeddedVertex  <
		V extends Vertex<V, E, F> & HasXYZW,
		E extends Edge<V, E, F> & IsBoundary & HasLength & IsFlippable,
		F extends Face<V, E, F>
	>
	extends Vertex<EmbeddedVertex<V,E,F>, EmbeddedEdge<V,E,F>, EmbeddedFace<V,E,F>>
	implements HasAngle, HasLength, HasXYZW
	{
	
	private static final long 
	serialVersionUID = 1L;
	private E edge;
	private double dist;
	private double height;
	private double angle;
	private double length;
	private HalfEdgeDataStructure<V,E,F> graph;

	
	@Override
	protected EmbeddedVertex<V,E,F> getThis() {
		return this;
	}
	
	public EmbeddedVertex(HalfEdgeDataStructure<V,E,F> g, E index, double distance, double h) {
		graph = g;
		edge = index;
		dist = distance;
		height = h;
	}
	
	public Point4d getXYZW() {
		Point4d a = edge.getStartVertex().getXYZW();
		Point4d b = edge.getTargetVertex().getXYZW();
		
		Vector4d t = new Vector4d(b);
		t.sub(a);
		t.normalize();
		
//		t.scaleAdd(dist, a);
		t.scale(dist);
		t.add(a);
		
		return new Point4d(t);
	}

	public void setXYZW(Point4d p) {
	}

	
	public Double getLength() {
		return length;
	}
	
	public void setLength(Double length) {
		this.length = length;
	}
	
	public Double getAngle() {
		return angle;
	}
	
	public void setAngle(Double angle) {
		this.angle = angle;
	}
	
	public boolean isHome() {
		if(dist < edge.getLength()/2.0)
			return true;
		else
			return false;
	}
	
	public E getEdge() {
		return edge;
	}
	
	public void setEdge(E nr) {
		edge = nr;
	}
	
	public double getDistance() {
		return dist;
	}
	
	public double getHeight() {
		return height;
	}
	
	public void setDistance(double d) {
		dist = d;
	}
	
	public HalfEdgeDataStructure<V,E,F> getGraph(){
		return graph;
	}
	
	public boolean isVertex() {
		 // FIXME    ... || length-dist < 10e-4) makes bug in findShortestPaths
		return (dist < 10e-4);// TODO pick an appropriate constant here
	}
	
	public EmbeddedVertex<V,E,F> getOpposite() {	//FIXME
		if(height == 0.0) {
//			E oppEdge = graph.getEdge(edgeNr).getOppositeEdge();
			E oppEdge = edge.getOppositeEdge();
//			int newEdgeNr = oppEdge.getIndex();
			E newEdgeNr = oppEdge;
			double newDist = oppEdge.getLength() - dist;
			if(newDist < 0)
				System.err.println("Think again");
		
			return new EmbeddedVertex<V,E,F>(graph, newEdgeNr, newDist, height);
		} else return null;
	}
	
	// needs to be redone when height != 0
	public V getClosestVertex() {
		if(isHome())
			return edge.getStartVertex();
		else
			return edge.getTargetVertex();
	}
	
	// needs to be redone when height != 0
	public V getFartherstVertex() {
		if(isHome())
			return edge.getTargetVertex();
		else
			return edge.getStartVertex();
	}
	
	public String toString() {
		StringBuffer result = new StringBuffer();
		result.append("SkeletonPosition\n");
		result.append("Index: " + edge + "(" + edge.getStartVertex().getIndex() + ", " + edge.getTargetVertex().getIndex() + ")" +  "\n");
		result.append("Distance: " + dist + "\n");
		result.append("Height: " + height + "\n");
		return result.toString();
	}
	
}