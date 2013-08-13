package alexandrov;

import halfedge.Edge;
import halfedge.Face;
import halfedge.HalfEdgeDataStructure;
import halfedge.Vertex;
import halfedge.decorations.HasRadius;
import halfedge.decorations.HasXYZW;
import halfedge.decorations.IsBoundary;
import halfedge.decorations.IsFlippable;
import halfedge.triangulationutilities.TriangulationException;

import java.util.HashSet;
import java.util.Stack;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point4d;

import alexandrov.math.CapCurvatureFunctional;
import de.jtem.numericalMethods.calculus.function.RealVectorValuedFunctionOfSeveralVariablesWithJacobien;
import de.jtem.numericalMethods.calculus.rootFinding.Broyden;

public class AlexandrovUtility {

	public static <
		V extends Vertex<V, E, F> & HasXYZW & HasRadius,
		E extends Edge<V, E, F> & IsFlippable,
		F extends Face<V, E, F>
	> void layoutPolyeder(HalfEdgeDataStructure<V, E, F> graph){
		V firstVertex = graph.getVertex(0);
		E firstEdge = firstVertex.getConnectedEdge();
		V secondVertex = firstEdge.getStartVertex();
		
		Double rx = firstVertex.getRadius();
		Double ry = secondVertex.getRadius();
		Double lxy = firstEdge.getLength();
		Double y1 = (rx*rx + ry*ry - lxy*lxy) / (2*rx);
		Double y2 = Math.sqrt(ry*ry - y1*y1);
		
		firstVertex.setXYZW(new Point4d(rx, 0, 0, 1));
		secondVertex.setXYZW(new Point4d(y1, y2, 0, 1));
		
		Stack<E> layoutEdges = new Stack<E>();
		HashSet<V> readyVertices = new HashSet<V>();
		readyVertices.add(firstVertex);
		readyVertices.add(secondVertex);
		layoutEdges.push(firstEdge);
		layoutEdges.push(firstEdge.getOppositeEdge());
		while (!layoutEdges.isEmpty()){
			E edge = layoutEdges.pop();
			E e1 = edge.getNextEdge();
			E e2 = edge.getPreviousEdge();
			V xVertex = e1.getTargetVertex();
			
			V e1v = e1.getOppositeEdge().getNextEdge().getTargetVertex();
			V e2v = e2.getOppositeEdge().getNextEdge().getTargetVertex();
			if (!readyVertices.contains(e1v)) {
				layoutEdges.push(e1.getOppositeEdge());
			}
			if (!readyVertices.contains(e2v)) {
				layoutEdges.push(e2.getOppositeEdge());
			}
			if (readyVertices.contains(xVertex)) {
				continue;
			}
			xVertex.setXYZW(getPyramideTip(edge));
			readyVertices.add(xVertex);
		}
		if (readyVertices.size() != graph.getNumVertices()) {
			System.err.println("layout failed!");
		}
	} 
	
	
	private static class TipRootFinder <
		V extends Vertex<V, E, F> & HasXYZW & HasRadius,
		E extends Edge<V, E, F> & IsFlippable,
		F extends Face<V, E, F>
	> implements RealVectorValuedFunctionOfSeveralVariablesWithJacobien{
	
		private double[][]
			x = new double[3][4];
		private double[]
		    r = new double[3];
			
		public TipRootFinder(E edge){
			r[0] = edge.getNextEdge().getLength();
			r[1] = edge.getPreviousEdge().getLength();
			r[2] = edge.getNextEdge().getTargetVertex().getRadius();
			edge.getTargetVertex().getXYZW().get(x[0]);
			edge.getStartVertex().getXYZW().get(x[1]);
			x[2][3] = 1;
			for (int i = 0; i < x.length; i++)
				for (int j = 0; j < 3; j++)
					x[i][j] = x[i][j] / x[i][3];
		}
		
		public void eval(double[] p, double[] fx, int offset) {
			for (int i = 0; i < 3; i++){
				fx[i + offset] = (p[0]-x[i][0])*(p[0]-x[i][0]) + 
								 (p[1]-x[i][1])*(p[1]-x[i][1]) + 
								 (p[2]-x[i][2])*(p[2]-x[i][2]) - 
								 r[i]*r[i];
			}
		}
		
		public void eval(double[] p, double[] fx, int offset, double[][] jac) {
			eval(p, fx, offset);
			for (int i = 0; i < jac.length; i++) {
				for (int j = 0; j < jac[i].length; j++) {
					jac[i][j] = 2*(p[j] - x[i][j]);
				}
			}
		}
	
		public int getDimensionOfTargetSpace() {
			return 3;
		}
	
		public int getNumberOfVariables() {
			return 3;
		}
	
	}
	
	
	private static Point4d cross(Point4d x1, Point4d x2){
		Point4d result = new Point4d(x1.y*x2.z - x1.z*x2.y, x1.z*x2.x - x1.x*x2.z, x1.x*x2.y - x1.y*x2.x, 1);
		result.scale(1 / (x1.w*x2.w));
		return result;
	}
	
	
	private static  <
		V extends Vertex<V, E, F> & HasXYZW & HasRadius,
		E extends Edge<V, E, F> & IsFlippable,
		F extends Face<V, E, F>
	> Point4d getPyramideTip(E edge){
		TipRootFinder<V, E, F> rootFinder = new TipRootFinder<V, E, F>(edge);
		Point4d x1 = edge.getTargetVertex().getXYZW();
		Point4d x2 = edge.getStartVertex().getXYZW();
		Point4d guess = cross(x1, x2);
	//	System.out.println(String.format("tr: %s, %s, %s", x1.toString(), x2.toString(), guess.toString()));
		double[] x = new double[]{guess.x, guess.y, guess.z};
		Broyden.search(rootFinder, x);
		if (Double.isNaN(x[0]) || Double.isNaN(x[1]) || Double.isNaN(x[2])) {
			System.out.println("Alexandrov2.getPyramideTip()");
		}
		return new Point4d(x[0], x[1], x[2], 1);
	}


	
	public static <
		V extends Vertex<V, E, F> & HasXYZW & HasRadius,
		E extends Edge<V, E, F> & IsFlippable & IsBoundary,
		F extends Face<V, E, F>
	> void layoutCap(HalfEdgeDataStructure<V, E, F> graph, boolean normalize) throws TriangulationException{
		// find boundary edge
		E firstEdge = null;
		for (E e : graph.getEdges()){
			if (!CapCurvatureFunctional.isFaceDegenerated(e) && e.isBoundary())
				firstEdge = e;
		}
		V firstVertex = firstEdge.getTargetVertex();
		V secondVertex = firstEdge.getStartVertex();
		
		Double rx = firstVertex.getRadius();
		Double ry = secondVertex.getRadius();
		Double lxy = firstEdge.getLength();
		Double dh = ry - rx;
		Double z = Math.sqrt(lxy*lxy - dh*dh);
		
//		System.err.println("Layouting vertices " + firstVertex.getIndex() + " and " + secondVertex.getIndex());
		firstVertex.setXYZW(new Point4d(1, rx, 0, 1));
		secondVertex.setXYZW(new Point4d(1, ry, z, 1));
		
		Stack<E> layoutEdges = new Stack<E>();
		HashSet<V> readyVertices = new HashSet<V>();
		readyVertices.add(firstVertex);
		readyVertices.add(secondVertex);
		layoutEdges.push(firstEdge);
		layoutEdges.push(firstEdge.getOppositeEdge());
		while (!layoutEdges.isEmpty()){
			E edge = layoutEdges.pop();
			if (CapCurvatureFunctional.isFaceDegenerated(edge))
				continue;
			if (edge.getLeftFace() == null)
				continue;
			E e1 = edge.getNextEdge();
			E e2 = edge.getPreviousEdge();
			V xVertex = e1.getTargetVertex();
			V e1v = e1.getOppositeEdge().getNextEdge().getTargetVertex();
			V e2v = e2.getOppositeEdge().getNextEdge().getTargetVertex();
			if (!readyVertices.contains(e1v)) {
				layoutEdges.push(e1.getOppositeEdge());
			}
			if (!readyVertices.contains(e2v)) {
				layoutEdges.push(e2.getOppositeEdge());
			}
			if (readyVertices.contains(xVertex)) {
				continue;
			}
	//		System.err.println("Layouting Vertex " + xVertex.getIndex() + " from edge " + edge.getStartVertex().getIndex() + "<->" + edge.getTargetVertex().getIndex());
			xVertex.setXYZW(getPyramideTipForCap(edge));
			readyVertices.add(xVertex);
		}
		
		
		//glue border to the ground if some accuracy errors occured
		for (V v: graph.getVertices())
			if (CapCurvatureFunctional.isBorderVertex(v))
				v.getXYZW().y = 0.0;
		
		
		// to normalized position
		if (normalize){
			V borderVertex1 = null;
			V borderVertex2 = null;
			for (V v : graph.getVertices())
				if (CapCurvatureFunctional.isBorderVertex(v))
					borderVertex1 = v;
			for (V v : graph.getVertices())
				if (CapCurvatureFunctional.isBorderVertex(v) && v != borderVertex1)
					borderVertex2 = v;
			
			Point4d p1 = borderVertex1.getXYZW();
			Point4d p2 = borderVertex2.getXYZW();
			
			double angle = Math.atan2(p1.z - p2.z, p1.x - p2.x);
			Matrix4d N = new Matrix4d();
			N.setIdentity();
			N.transform(p1);
			N.rotY(angle);
			
				
			for (V v : graph.getVertices()){
				N.transform(v.getXYZW());
			}
			
			
			Point4d sum = new Point4d();
			for (V v : graph.getVertices()){
				sum.add(v.getXYZW());
			}
			sum.scale(1.0 / graph.getNumVertices());
			sum.y = 0;
			sum.w = 0;
			for (V v : graph.getVertices()){
				v.getXYZW().sub(sum);
			}
		}
		
	} 
	
	
	
	private static  <
		V extends Vertex<V, E, F> & HasXYZW & HasRadius,
		E extends Edge<V, E, F> & IsFlippable & IsBoundary,
		F extends Face<V, E, F>
	> Point4d getPyramideTipForCap(E edge){
		Double p2x = edge.getTargetVertex().getXYZW().x;
		Double p2z = edge.getTargetVertex().getXYZW().z;
		Double p1x = edge.getStartVertex().getXYZW().x;
		Double p1z = edge.getStartVertex().getXYZW().z;
		
		Double lp = edge.getLength();
		Double l2 = edge.getNextEdge().getLength();
		Double l1 = edge.getPreviousEdge().getLength();
		Double rx = edge.getNextEdge().getTargetVertex().getRadius();
		Double rp1 = edge.getStartVertex().getRadius();
		Double rp2 = edge.getTargetVertex().getRadius();
		
		Double r1 = Math.sqrt(l1*l1 - (rx - rp1)*(rx - rp1));
		Double r2 = Math.sqrt(l2*l2 - (rx - rp2)*(rx - rp2));
		Double rp = Math.sqrt(lp*lp - (rp1 - rp2)*(rp1 - rp2));
		Double cosAlpha2 = (r2*r2-r1*r1+rp*rp) / (2*r2*rp);
		Double alpha2 = Math.acos(cosAlpha2);
		Double sinAlpha2 = Math.sin(alpha2);
		
		Double vx = (p2x - p1x); Double vz = (p2z - p1z);
		Double lengthV = Math.sqrt(vx*vx + vz*vz);
		vx /= -lengthV; vz /= -lengthV;
		
		Double x = p2x + r2*(cosAlpha2*vx + sinAlpha2*vz);
		Double z = p2z + r2*(-sinAlpha2*vx + cosAlpha2*vz);
		return new Point4d(x, rx, z, 1);
	}

	
}
