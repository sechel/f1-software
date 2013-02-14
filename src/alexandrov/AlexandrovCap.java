package alexandrov;

import halfedge.Edge;
import halfedge.Face;
import halfedge.HalfEdgeDataStructure;
import halfedge.Vertex;
import halfedge.decorations.HasRadius;
import halfedge.decorations.HasXYZW;
import halfedge.decorations.IsBoundary;
import halfedge.decorations.IsFlippable;
import halfedge.io.HESerializableReader;
import halfedge.triangulationutilities.ConsistencyCheck;
import halfedge.triangulationutilities.TriangulationException;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Stack;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point4d;

import math.optimization.FunctionNotDefinedException;
import math.optimization.NotConvergentException;
import math.optimization.newton.NewtonSolver;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.Vector;
import no.uib.cipr.matrix.Vector.Norm;
import util.debug.DBGTracer;
import alexandrov.graph.CPMEdge;
import alexandrov.graph.CPMFace;
import alexandrov.graph.CPMVertex;
import alexandrov.math.CapCurvatureFunctional;
import alexandrov.math.CapLinearizable;

public class AlexandrovCap {

	private static Double
		solverError = 1E-10;
	
	protected static <
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
		
		System.err.println("Layouting vertices " + firstVertex.getIndex() + " and " + secondVertex.getIndex());
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
			if (readyVertices.contains(xVertex))
				continue;
			System.err.println("Layouting Vertex " + xVertex.getIndex() + " from edge " + edge.getStartVertex().getIndex() + "<->" + edge.getTargetVertex().getIndex());
			xVertex.setXYZW(getPyramideTip(edge));
			layoutEdges.push(e1.getOppositeEdge());
			layoutEdges.push(e2.getOppositeEdge());
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
	> Point4d getPyramideTip(E edge){
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
	
		
	/**
	 * Caution! this works on inner vertices only
	 * @param <V>
	 * @param <E>
	 * @param <F>
	 * @param graph
	 * @param error
	 * @param maxInterations
	 * @throws TriangulationException
	 * @throws NotConvergentException
	 */
	public static <
		V extends Vertex<V, E, F> & HasXYZW & HasRadius,
		E extends Edge<V, E, F> & IsFlippable & IsBoundary,
		F extends Face<V, E, F>
	> void constructCap(HalfEdgeDataStructure<V, E, F> graph, Double error, Integer maxInterations) throws TriangulationException, NotConvergentException{
		constructCap_internal(graph, error, maxInterations);
		DBGTracer.msg("layouting...");
		layoutCap(graph, true);
	}
		
	protected static <
		V extends Vertex<V, E, F> & HasXYZW & HasRadius,
		E extends Edge<V, E, F> & IsFlippable & IsBoundary,
		F extends Face<V, E, F>
	> void constructCap_internal(HalfEdgeDataStructure<V, E, F> graph, Double error, Integer maxInterations) throws TriangulationException, NotConvergentException{
		if (!ConsistencyCheck.isTriangulation(graph))
			throw new TriangulationException("No triangulation!");
		if (!ConsistencyCheck.isDisk(graph))
			throw new TriangulationException("Triangulation is no sphere!");
		if (!CapCurvatureFunctional.isMetricConvex(graph))
			throw new TriangulationException("Cap metric not convex!");
		
		// flip counters are set to zero
		Alexandrov.resetFlipStates(graph);
		
		List<V> innerVertices = CapCurvatureFunctional.getInnerVertices(graph);
		
		// initial heights = 0
		for (V v : graph.getVertices())
			v.setRadius(0.0);
		for (V v : innerVertices)
			v.setRadius(0.01);
		
//		double delta = 0.1;
		Matrix jacobi = CapCurvatureFunctional.getCurvatureDerivative(graph);
		DBGTracer.msg("Jacobi matrix is:");
		DBGTracer.msg(jacobi.toString());
		
		double max_delta = 0.75;
		double boost = 1;
		double delta = max_delta;
		NewtonSolver solver = new NewtonSolver();
		solver.setError(solverError);
		CapLinearizable<V, E, F> fun = new CapLinearizable<V, E, F>(graph);
		Vector kappa = CapCurvatureFunctional.getCurvature(graph);
		DBGTracer.msg("start kappas:");
		DBGTracer.msg(kappa.toString());
		Vector stepKappa = kappa.copy().add(-delta, kappa);
		Vector newRadii = new DenseVector(innerVertices.size());
		getRadii(graph, newRadii);
		Integer actInteration = 0;
		while (kappa.norm(Norm.Two) > error && actInteration < maxInterations){
			if (delta < 1E-50){
				Vector radii = new DenseVector(graph.getNumVertices());
				getRadii(graph, radii);
				DBGTracer.msg("Radii: ");
				DBGTracer.msg(radii.toString());
				DBGTracer.msg(kappa.toString());
				throw new NotConvergentException("Dead end! Maybe nearly concave geometry", delta);
			}
			Vector oldRadii = newRadii.copy();
			try {
				solver.solve(fun, newRadii, stepKappa);
			} catch (FunctionNotDefinedException te){
				delta = Math.pow(delta, 2);
				stepKappa = kappa.copy().add(-delta, kappa);
				newRadii = oldRadii;
				DBGTracer.msg("triangle inequation! -> delta = " + delta);
				boost = 1;
				continue;
			}
			setRadii(graph, newRadii);
			HashSet<E> concaveEdges = new HashSet<E>();
			for (E e : graph.getPositiveEdges()){
				if (!CapCurvatureFunctional.isLocallyConvex(e))
					concaveEdges.add(e);	
			}
			// flip
			if (concaveEdges.size() == 1){
				E flip = concaveEdges.iterator().next();
				if (!CapCurvatureFunctional.isDegenerated(flip)){
					flip.flip();
					DBGTracer.msg("Edge " + flip + "flipped");
				} else
					DBGTracer.msg("Edge degenerated not flipped");
				boost = 1;
//				max_delta = delta;
				actInteration++;
				continue;
			}
			// step was too large
			if (concaveEdges.size() > 1){
				delta *= 0.2;
				DBGTracer.msg("concave edges: " + concaveEdges);
				DBGTracer.msg("-> delta = " + delta);
				stepKappa = kappa.copy().add(-delta, kappa);
				newRadii = oldRadii;
				boost = 1;
				actInteration++;
				continue;
			}
			kappa.set(stepKappa);
			delta = Math.pow(max_delta, 1.0 / boost);
			DBGTracer.msg("resetting or boosting delta to " + delta);
			stepKappa = stepKappa.copy().add(-delta, stepKappa);
			DBGTracer.msg("|Kappa|: " + kappa.norm(Norm.Two) + " boost: " + boost);
			boost *= 2;
			actInteration++;
			Vector radii = new DenseVector(innerVertices.size());
			getRadii(graph, radii);
			DBGTracer.msg("Radii:" + radii);
		}
		DBGTracer.msg("Needed " + actInteration + " iterations to complete.");
		DBGTracer.msg("Error: " + kappa.norm(Norm.Two));
		if (actInteration == maxInterations)
			throw new NotConvergentException("Polytop has not been constructed within the maximum iterations!", stepKappa.norm(Norm.Two));
	}

	
	protected static <
		V extends Vertex<V, E, F> & HasXYZW & HasRadius,
		E extends Edge<V, E, F> & IsFlippable & IsBoundary,
		F extends Face<V, E, F>
	> void setRadii(HalfEdgeDataStructure<V, E, F> graph, Vector radii){
		int i = 0;
		for (V v : CapCurvatureFunctional.getInnerVertices(graph)){
			v.setRadius(radii.get(i));
			i++;
		}
	}
	
	protected static <
		V extends Vertex<V, E, F> & HasXYZW & HasRadius,
		E extends Edge<V, E, F> & IsFlippable & IsBoundary,
		F extends Face<V, E, F>
	> void getRadii(HalfEdgeDataStructure<V, E, F> graph, Vector radii){
		int i = 0;
		for (V v : CapCurvatureFunctional.getInnerVertices(graph)){
			radii.set(i, v.getRadius());
			i++;
	}
}
	
	
	public static void main(String[] args) {
		HalfEdgeDataStructure<CPMVertex, CPMEdge, CPMFace> graph = null;
		try {
			FileInputStream in = new FileInputStream(new File("testing/data/tetraedercap.cpm"));
			HESerializableReader<CPMVertex, CPMEdge, CPMFace> reader = new HESerializableReader<CPMVertex, CPMEdge, CPMFace>(in);
			graph = reader.readHalfEdgeDataStructure();
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		System.err.println(graph.toString());
		
		try {
			constructCap(graph, 1E-10, 20);
		} catch (TriangulationException e) {
			e.printStackTrace();
		} catch (NotConvergentException e) {
			e.printStackTrace();
		}
		
		
	}
	
	
}
