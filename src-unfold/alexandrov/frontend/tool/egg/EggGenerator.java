package alexandrov.frontend.tool.egg;

import halfedge.Edge;
import halfedge.Face;
import halfedge.HalfEdgeDataStructure;
import halfedge.Vertex;
import halfedge.decorations.HasLength;
import halfedge.decorations.HasXY;
import halfedge.decorations.IsHidable;

import java.io.File;
import java.util.LinkedList;

import math.ellipse.Ellipse;

import alexandrov.graph.CPMEdge;
import alexandrov.graph.CPMFace;
import alexandrov.graph.CPMVertex;
import alexandrov.io.CPMLWriter;

public class EggGenerator 
	<
		V extends Vertex<V, E, F> & HasXY,
		E extends Edge<V, E, F> & HasLength & IsHidable,
		F extends Face<V, E, F>
	> {
	
	
	
	Ellipse ellipse = null;

	private int n = 3;
	private int m = 1;
	
	private HalfEdgeDataStructure<V,E,F> graph = null;
	
	LinkedList<LinkedList<V>> layers = null;
	
	LinkedList<LinkedList<LinkedList<E>>> triangles = null;
	
	public EggGenerator(int m, int n, double e, Class<V> vClass, Class<E> eClass, Class<F> fClass) {
		this.n = n;
		this.m = m;
		
		graph = HalfEdgeDataStructure.createHEDS(vClass, eClass, fClass);
		
		layers = new LinkedList<LinkedList<V>>();
		triangles = new LinkedList<LinkedList<LinkedList<E>>>();
		
		ellipse = new Ellipse(e);
		
		makeVertices();
		makeEdges();
		connectData();
		metrizeData();
		System.err.println("No. edges: " + graph.getEdges().size());
	}
	
	public HalfEdgeDataStructure<V,E,F> getGraph(){
		return graph;
	}
	
	private void makeVertices() {
		
		// base
		layers.add(new LinkedList<V>());
		layers.get(0).add(graph.addNewVertex());
		
		// circles
		for(int i = 1; i <= m; i++) {
			layers.add(new LinkedList<V>());
			for(int j = 0; j < n; j++) {
				layers.get(i).add(graph.addNewVertex());
			}
		}
		
		// peak
		layers.add(new LinkedList<V>());
		layers.get(m+1).add(graph.addNewVertex());
	}
	
	private void makeEdges() {

		// bottom pyramid
		triangles.add(new LinkedList<LinkedList<E>>());
		for(int j = 0; j < n; j++) {
			triangles.get(0).add(new LinkedList<E>());
			
			triangles.get(0).get(j).add(graph.addNewEdge());
			triangles.get(0).get(j).add(graph.addNewEdge());
			triangles.get(0).get(j).add(graph.addNewEdge());
		}
		
		// middle layers
		for(int i = 1; i < m; i++) {
			triangles.add(new LinkedList<LinkedList<E>>());
			
			for(int j = 0; j < n; j++) {
				
				triangles.get(i).add(new LinkedList<E>());
				
				triangles.get(i).get(2*j).add(graph.addNewEdge());
				triangles.get(i).get(2*j).add(graph.addNewEdge());
				triangles.get(i).get(2*j).add(graph.addNewEdge());
				
				triangles.get(i).add(new LinkedList<E>());
				
				triangles.get(i).get(2*j+1).add(graph.addNewEdge());
				triangles.get(i).get(2*j+1).add(graph.addNewEdge());
				triangles.get(i).get(2*j+1).add(graph.addNewEdge());
				
			}
		}
		
		// top pyramid
		triangles.add(new LinkedList<LinkedList<E>>());
		for(int j = 0; j < n; j++) {
			triangles.get(m).add(new LinkedList<E>());
			
			triangles.get(m).get(j).add(graph.addNewEdge());
			triangles.get(m).get(j).add(graph.addNewEdge());
			triangles.get(m).get(j).add(graph.addNewEdge());
		}
	}

	private void co(E e, E o) {
		if(e.getOppositeEdge() != null)
			System.err.println("Already linked");
		
		System.err.println("connecting: " + e.getIndex() + " opp: " + e.getOppositeEdge() + " --> " + o.getIndex());
		e.linkOppositeEdge(o);
	}

	private void st(E e, V t) {
		System.err.println("hatching: " + e.getIndex() + " target: " + t.getIndex());
		e.setTargetVertex(t);
	}
	
	private void ne(E e, E n) {
		System.err.println("nexting: " + e.getIndex() + " target: " + n.getIndex());
		e.linkNextEdge(n);
	}
	
	private void sl(E e, double l) {
		System.err.println("scaling: " + e.getIndex() + " to: " + l);
		e.setLength(l);
		e.getOppositeEdge().setLength(l);
	}
	
	private void connectData() {
		
		// connect bottom vertex to first layer
		// and edges horizontally
		System.err.println("\nConnecting bottom\n");
		E old_l = triangles.get(0).get(n-1).get(2);
		for(int j = 0; j < n-1; j++) {
			E r = triangles.get(0).get(j).get(0);
			E t = triangles.get(0).get(j).get(1);
			E l = triangles.get(0).get(j).get(2);
			st(r, layers.get(1).get(j));
			st(t, layers.get(1).get(j+1));
			st(l, layers.get(0).get(0));
			ne(r, t);
			ne(t, l);
			ne(l, r);
			co(r, old_l);
			old_l = l;
		}
		E r = triangles.get(0).get(n-1).get(0);
		E t = triangles.get(0).get(n-1).get(1);
		E l = triangles.get(0).get(n-1).get(2);
		st(r, layers.get(1).get(n-1));
		st(t, layers.get(1).get(0));
		st(l, layers.get(0).get(0));
		ne(r, t);
		ne(t, l);
		ne(l, r);
		co(r,triangles.get(0).get(n-2).get(2));	////!!!!!!!!!!!!!
		
		// connect middle layers horizontally
		for(int i = 1; i < m; i++) {
			System.err.println("\nConnecting layer: " + i + "\n");
			old_l = triangles.get(i).get(2*n-1).get(2);	// check!!!!!
			for(int j = 0; j < n-1; j++) {
				  r  = triangles.get(i).get(2*j).get(0);
				  l  = triangles.get(i).get(2*j).get(1);
				E b	 = triangles.get(i).get(2*j).get(2);
				E r2 = triangles.get(i).get(2*j+1).get(0);
				  t  = triangles.get(i).get(2*j+1).get(1);
				E l2 = triangles.get(i).get(2*j+1).get(2);
				
				st(r, layers.get(i+1).get(j));
				st(l, layers.get(i).get(j+1));
				st(b, layers.get(i).get(j));
				st(r2, layers.get(i+1).get(j));
				st(t, layers.get(i+1).get(j+1));
				st(l2, layers.get(i).get(j+1));
				
				ne(r, l);
				ne(l, b);
				ne(b, r);
				ne(r2, t);
				ne(t, l2);
				ne(l2, r2);
				
				co(r, old_l);
				co(r2, l);
				
				old_l = l2;
				
			}
			
			  r  = triangles.get(i).get(2*n-2).get(0);
			  l  = triangles.get(i).get(2*n-2).get(1);
			E b	 = triangles.get(i).get(2*n-2).get(2);
			E r2 = triangles.get(i).get(2*n-1).get(0);
			  t  = triangles.get(i).get(2*n-1).get(1);
			E l2 = triangles.get(i).get(2*n-1).get(2);
			
			st(r, layers.get(i+1).get(n-1));
			st(l, layers.get(i).get(0));
			st(b, layers.get(i).get(n-1));
			st(r2, layers.get(i+1).get(n-1));
			st(t, layers.get(i+1).get(0));
			st(l2, layers.get(i).get(0));
			
			ne(r, l);
			ne(l, b);
			ne(b, r);
			ne(r2, t);
			ne(t, l2);
			ne(l2, r2);
			
			co(r, triangles.get(i).get(2*n-3).get(2));	// check!
			co(r2, l);
			
		}
		
		// connect top vertex to last layer
		// and edges horizontally
		System.err.println("\nConnecting top\n");
		old_l = triangles.get(m).get(n-1).get(1);	//2
		for(int j = 0; j < n-1; j++) {
			  r = triangles.get(m).get(j).get(0);
			  l = triangles.get(m).get(j).get(1);
			E b = triangles.get(m).get(j).get(2);
			st(r, layers.get(m+1).get(0));
			st(l, layers.get(m).get(j+1));
			st(b, layers.get(m).get(j));
			ne(r, l);
			ne(l, b);
			ne(b, r);
			co(r, old_l);
			old_l = l;
		}
		  r = triangles.get(m).get(n-1).get(0);
		  l = triangles.get(m).get(n-1).get(1);
		E b = triangles.get(m).get(n-1).get(2);
		st(r, layers.get(m+1).get(0));
		st(l, layers.get(m).get(0));
		st(b, layers.get(m).get(n-1));
		ne(r, l);
		ne(l, b);
		ne(b, r);
		co(r, triangles.get(m).get(n-2).get(1));

		
		// connect layers vertically
		for(int g = 0; g < m; g++) {
			// no middle layers:
			if(m < 2) {
				System.err.println("\nConnecting bottom and top vertically\n");
				for(int h = 0; h < n; h += 1) {
					co(triangles.get(g).get(h).get(g+1), triangles.get(g+1).get(h).get(2));
				}
			// if middle layers
			} else {
				// base pyramid to middle layer
				if(g == 0) {
					System.err.println("\nConnecting bottom vertically\n");
					for(int h = 0; h < n; h += 1) {
						co(triangles.get(g).get(h).get(1), triangles.get(g+1).get(2*h).get(2));
					}
				// middle layer to middle layer
				} if(g > 0 && g < m-1){
					System.err.println("\nConnecting layer " + g + " vertically\n");
					for(int h = 0; h < n-1; h += 1) {
						co(triangles.get(g).get(2*h+1).get(1), triangles.get(g+1).get(2*h).get(2));
					}
					co(triangles.get(g).get(2*n-1).get(1), triangles.get(g+1).get(2*n-2).get(2));
				}
				// top pyramid
				if(g == m - 1) {
					System.err.println("\nConnecting top vertically\n");
					for(int h = 0; h < n-1; h += 1) {
						co(triangles.get(g).get(2*h+1).get(1), triangles.get(g+1).get(h).get(2));
					}
					co(triangles.get(g).get(2*n-1).get(1), triangles.get(g+1).get(n-1).get(2));
				}
			}
		}
		
	}
	
	private void metrizeData() {
		for(LinkedList<LinkedList<E>> ll : triangles) {
			for(LinkedList<E> l : ll) {
				for(E e: l) {
					e.setLength(ellipse.getHalfCircumference()/(m+1));
				}
			}
		}
		
		int i = 0;
		for(int j = 0; j < n; j++) {
			E e = triangles.get(i).get(j).get(1);
			sl(e, getL(i+1));
		}
		
		for(i = 1; i < m; i++) {
			for(int j = 0; j < n; j++) {
				E e = triangles.get(i).get(2*j+1).get(1);
				sl(e,  getL(i+1));
			}
		}
		
//		i = 1;
//		for(int j = 0; j < n; j++) {
//			E e = triangles.get(i).get(2*j+1).get(1);
//			sl(e,  getL(i));
//		}
		
	}

	private double getL(int i) {
		double r = ellipse.getR((i*ellipse.getHalfCircumference())/(m+1));
		System.err.println("Radius: " + r);
//		return Math.sqrt(2*r)*Math.sqrt(r-2*Math.cos(2*Math.PI/n));
		return Math.sqrt(2*r*r*(1-Math.cos(2*Math.PI/n)));
	}
	public String getCPML() {
		try {
			return CPMLWriter.convertToCPML(graph);
		}
		catch(Exception e) {
			System.err.println(e.getMessage());
		}
		return null;
	}
	
	public static void main(String[] args) {
		EggGenerator<CPMVertex,CPMEdge,CPMFace> deform = new EggGenerator<CPMVertex,CPMEdge,CPMFace>(3, 3, 0.5, CPMVertex.class, CPMEdge.class, CPMFace.class);
		File deformTestFile = new File("data/polyeder/cigarrTest.cpml");
		try {
//			DataOutputStream das = new DataOutputStream(new FileOutputStream(deformTestFile));
//			das.writeBytes(deform.getCPML());
			CPMLWriter.writeCPML(deformTestFile, deform.getGraph());
			System.out.println(CPMLWriter.convertToCPML(deform.getGraph()));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
}
