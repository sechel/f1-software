package halfedge.surfaceutilities;

import halfedge.Edge;
import halfedge.Face;
import halfedge.Vertex;
import halfedge.decorations.HasAngle;
import halfedge.decorations.HasLength;
import halfedge.decorations.HasXY;
import halfedge.decorations.HasXYZW;
import halfedge.decorations.IsBoundary;
import halfedge.decorations.IsFlippable;
import halfedge.decorations.IsHidable;

import java.util.LinkedList;

import javax.vecmath.Vector3d;

import math.util.VecmathTools;

public class EmbeddedEdge  <
		V extends Vertex<V, E, F> & HasXYZW & HasXY,
		E extends Edge<V, E, F> & IsBoundary & HasLength & IsFlippable & IsHidable & HasAngle,
		F extends Face<V, E, F>
	>
	extends Edge<EmbeddedVertex<V,E,F>, EmbeddedEdge<V,E,F>, EmbeddedFace<V,E,F>>
	implements HasLength, HasAngle//,	Comparable// this is total length
	{

	private static final long 
	serialVersionUID = 1L;
	
	private double length;
	private double angle;
	
	LinkedList<EmbeddedVertex<V,E,F>> controls = null;
	
	public EmbeddedEdge(){
		controls = new LinkedList<EmbeddedVertex<V,E,F>>();
	}
	
//	public int compareTo(Object o){
//		EmbeddedEdgeAngleComparator<V, E, F> c = new EmbeddedEdgeAngleComparator<V, E, F>();
//		return c.compare(this, (EmbeddedEdge<V,E,F>)o);
//	}
	
	public Double getAngle() {
		return angle;
	}
	

	public void setAngle(Double angle) {
		this.angle = angle;
	}
	
	public Double getLength2() {
		double length2 = 0;
		for(int i = 0; i < controls.size()-1; i++) {
			Vector3d t = new Vector3d(VecmathTools.p4top3(controls.get(i+1).getXYZW()));
			t.sub(VecmathTools.p4top3(controls.get(i).getXYZW()));
			length2 += t.length();
		}
		return length2;
	}
	
	public Double getLength() {
		return length;
	}
	
	public void setLength(Double length) {
		this.length = length;
	}

	public void addLength(Double length) {
		this.length += length;
	}
	
	public V getSourceVertex() {
		return controls.getFirst().getClosestVertex();
	}
	
	public V getEndVertex() {
//		if(getEmbeddedVertices().getLast().isHome())
			return getEmbeddedVertices().getLast().getClosestVertex();
//		else
//			return getEmbeddedVertices().getLast().getFartherstVertex();
	}
	
	protected EmbeddedEdge<V,E,F> getThis() {
		return this;
	}
	
	public LinkedList<EmbeddedVertex<V,E,F>> getEmbeddedVertices(){
		return controls;
	}
	
	public EmbeddedVertex<V,E,F> getEmbeddedVertex(int i){
		return controls.get(i);
	}
	
	public E getSourceEdge(){
		return controls.get(1).getEdge().getNextEdge();
	}
	
	public void setEmbeddedVertices(LinkedList<EmbeddedVertex<V,E,F>> newControls){
		controls = newControls;
	}
	
	//FIXME needs more rigorous checks. neighbouring faces etc.
	public void addEmbeddedVertexPair(EmbeddedVertex<V,E,F> p) {
		if(controls.size() > 0) {
			if(p.getOpposite() != controls.getLast()) {
//				System.err.println("Trying to add vertex out of order");
//				controls.addLast(p.getOpposite());
				controls.addLast(p);
				controls.addLast(p.getOpposite());
			} 
		} else {
			controls.addLast(p);
		}
	}
	
	public EmbeddedEdge<V,E,F> reverseControls(){
		EmbeddedEdge<V,E,F> r = new EmbeddedEdge<V,E,F>();//FIXME
		LinkedList<EmbeddedVertex<V,E,F>> revControls = (LinkedList<EmbeddedVertex<V,E,F>>)controls.clone();
		while(revControls.size() > 0) {
			r.controls.add(revControls.getLast());
			revControls.removeLast();
		}
		r.controls = revControls;
		return r;
	}
	
	public String toString() {
//		return super.toString() + controls;
		return "EmbeddedEdge: from " + getSourceVertex() + " to " + getEndVertex() + " Length: " + getLength() + " Length2: " + getLength2();
	}
	
}
