package alexandrov.graph;

import halfedge.Edge;
import halfedge.decorations.HasAngle;
import halfedge.decorations.IsBoundary;
import halfedge.decorations.IsFlippable;
import halfedge.decorations.IsHidable;
import halfedge.surfaceutilities.ConsistencyCheck;
import halfedge.triangulationutilities.Delaunay;
import halfedge.triangulationutilities.TriangulationException;
import halfedge.util.Consistency;


/**
 * The edge class for the alexandrov project
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 */
public class CPMEdge extends Edge<CPMVertex, CPMEdge, CPMFace> implements HasAngle, IsFlippable, IsBoundary, IsHidable{

	private static final long 
		serialVersionUID = 1L;
	private Double
		length = 1.0,
		gamma = 0.0;
	private int
		flipCount = 0;
	private Boolean
		border = false;
	private boolean
		isHidden = false;
	
	@Override
	protected CPMEdge getThis() {
		return this;
	}

	@Override
	public Double getLength() {
		return length;
	}

	@Override
	public void setLength(Double length) {
		this.length = length;
	}
	
	@Override
	public String toString() {
		return super.toString() + " length = " + length + "S: " + getStartVertex() + " T: " + getTargetVertex(); 
	}

	@Override
	public Double getAngle() {
		return gamma;
	}

	@Override
	public void setAngle(Double angle) {
		gamma = angle;
	}


	@Override
	public void flip() throws TriangulationException{
		if (!ConsistencyCheck.isValidSurface(getHalfEdgeDataStructure()))
			System.err.println("No valid surface before flip()");
		if (!Consistency.checkConsistency(getHalfEdgeDataStructure()))
			System.err.println("surface corrupted before flip()");
		CPMFace leftFace = getLeftFace();
		CPMFace rightFace = getRightFace();
		if (leftFace == rightFace)
			return;
		CPMEdge a1 = getOppositeEdge().getNextEdge();
		CPMEdge a2 = a1.getNextEdge();
		CPMEdge b1 = getNextEdge();
		CPMEdge b2 = b1.getNextEdge();
		
		CPMVertex v1 = getStartVertex();
		CPMVertex v2 = getTargetVertex();
		CPMVertex v3 = a1.getTargetVertex();
		CPMVertex v4 = b1.getTargetVertex();

		//new length for edge
		Double la2 = a2.getLength();
		Double lb1 = b1.getLength();
		Double alpha = Delaunay.getAngle(this) + Delaunay.getAngle(a2);
		Double newLength = Math.sqrt(la2*la2 + lb1*lb1 - 2*lb1*la2*StrictMath.cos(alpha));
		setLength(newLength);
		getOppositeEdge().setLength(newLength);
		
		//new connections
		linkNextEdge(a2);
		linkPreviousEdge(b1);
		getOppositeEdge().linkNextEdge(b2);
		getOppositeEdge().linkPreviousEdge(a1);
		setTargetVertex(v3);
		getOppositeEdge().setTargetVertex(v4);
		
		a2.linkNextEdge(b1);
		b2.linkNextEdge(a1);
		
		//set faces
		b2.setLeftFace(rightFace);
		a2.setLeftFace(leftFace);
		
		//fix vertex edge connections
		//:TODO check constantly
		b2.setTargetVertex(v1);
		a2.setTargetVertex(v2);
		a1.setTargetVertex(v3);
		b1.setTargetVertex(v4);
//		v1.setConnectedEdge(b2);
//		v2.setConnectedEdge(a2);
//		v3.setConnectedEdge(a1);
//		v4.setConnectedEdge(b1);
		flipCount++;
		
		if (!ConsistencyCheck.isValidSurface(getHalfEdgeDataStructure()))
			System.err.println("No valid surface after flip()");
		if (!Consistency.checkConsistency(getHalfEdgeDataStructure()))
			System.err.println("surface corrupted after flip()");
	}

	@Override
	public int getFlipCount() {
		return flipCount;
	}

	@Override
	public void resetFlipCount() {
		flipCount = 0;
	}

	@Override
	public Boolean isBoundary() {
		return border;
	}

	@Override
	public void setBoundary(Boolean border) {
		this.border = border;
	}

	@Override
	public Boolean isHidden() {
		return isHidden;
	}
	
	@Override
	public void setHidden(Boolean hide){
		isHidden = hide;
	}

}
