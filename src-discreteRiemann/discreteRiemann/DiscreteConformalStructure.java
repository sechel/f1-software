package discreteRiemann;

import halfedge.Edge;
import halfedge.Face;
import halfedge.HalfEdgeDataStructure;
import halfedge.Vertex;
import halfedge.decorations.HasXY;

import java.util.List;

import javax.vecmath.Point2d;

import de.jtem.mfc.field.Complex;

/**
 * A special HalfEdgeDataStructure such that its edges carry
 * a positive real number.
 * @author mercat
 *
 * @param <V>
 * @param <E>
 * @param <F>
 */
public class DiscreteConformalStructure 	<
V extends Vertex<V, E, F> ,
E extends Edge<V, E, F> & HasRho,
F extends Face<V, E, F> > extends HalfEdgeDataStructure<V, E, F> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	
	public static class ConfVertex extends Vertex<ConfVertex, ConfEdge, ConfFace> implements HasXY {
		Point2d p = new Point2d();
		
		private static final long
		serialVersionUID = 1L;
		
		protected ConfVertex getThis() {
			return this;
		}

		public Point2d getXY() {
			return p;
		}

		public void setXY(Point2d p) {
			this.p.set(p.x, p.y);
		}
		
		public void setXY( double x, double y) {
			this.p.set( x, y);
		}
	}
	
	public static class ConfEdge 
	extends Edge<ConfVertex, ConfEdge, ConfFace> 
	implements HasRho {
		
		private static final long
		serialVersionUID = 1L;
		
		private double rho;
		
		protected ConfEdge getThis() {
			return this;
		}

		public double getRho() {
			return rho;
		}

		public void setRho(double rho) {
			ConfEdge e = getOppositeEdge();
			if(e != null)
			e.rho = rho;
			
			this.rho = rho;
		}
	}
	
	public static class ConfFace extends Face<ConfVertex, ConfEdge, ConfFace> {
		
		private static final long
		serialVersionUID = 1L;
		
		protected ConfFace getThis() {
			return this;
		}
	}

	public DiscreteConformalStructure(HalfEdgeDataStructure<V,E,F> heds, double[] rho) {
		super(heds);
		assert       rho.length == getNumEdges();
		for(E e: this.getPositiveEdges()){
			e.setRho(rho[e.getIndex()]);	
		}
	}

	public DiscreteConformalStructure(HalfEdgeDataStructure<V, E, F> heds) {
		super(heds);
	}


	/**
	 * Integrates the form along the spanning tree and fixes the xy coordinates of each vertex
	 * @param form
	 * @see 
	 */
	public 	<
	V extends Vertex<V, E, F> & HasXY,
	E extends Edge<V, E, F> & HasRho,
	F extends Face<V, E, F>
	> void integrate(Complex[] form, List<E> tree){
		boolean isTheRoot = false; // The root is the only vertex from which no edge starts.

		V root = tree.get(0).getStartVertex();
		int stop = vertexList.size();
		
		while (!isTheRoot && --stop>0) {
			isTheRoot = true;
			for(E e: root.getEdgeStar()){ // The star points to v.
				if(tree.contains(e.getOppositeEdge())){
					isTheRoot = false;
					root = e.getStartVertex();
					break;
				}
			}
		}
		
		if(stop == 0) throw new RuntimeException("This 'tree' has no root.");
		
		Point2d p = new Point2d();
		
		root.setXY(p);
		
		for(E e: tree){
			Point2d t = e.getTargetVertex().getXY(); // Oriented inwards.
			Complex dz = form[e.getIndex()];
			p.set(t.x+dz.re, t.y+dz.im);
			e.getStartVertex().setXY(p);
		}
	}
	
	public void setRho( double c ) {
		for( E e: getPositiveEdges() ) e.setRho(c);
	}
}
