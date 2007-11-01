/**
 * 
 */
package discreteRiemann;

import halfedge.Edge;
import halfedge.Face;
import halfedge.HalfEdgeDataStructure;
import halfedge.HalfEdgeUtility;
import halfedge.Vertex;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author mercat
 *
 */
public class HomotopyUtility {

	/**
	 * A combinatorial circle.
	 * @param <V>
	 * @param <E>
	 * @param <F>
	 * @param center the vertex at the center
	 * @param radius the combinatorial distance
	 * @return the set of vertices at distance radius from a center vertex.
	 */
	static public 	<
	V extends Vertex<V, E, F>,
	E extends Edge<V, E, F>,
	F extends Face<V, E, F>
	> Set<V> horocycle( V center, int radius ) {
		Set<V> 
		dnm1 = new HashSet<V>(), // Vertices at distance n-1
		dn   = new HashSet<V>(), // Vertices at distance n
		dnp1 = new HashSet<V>(); // Vertices at distance n+1

		int n = 0;
		dn.add(center);

		while(n<radius){
			dnp1.clear();
			for(V v: dn){
				dnp1.addAll(v.getVertexStar());
			}
			dnp1.removeAll(dnm1);
			dnp1.removeAll(dn);
			n++;
			dnm1.clear(); dnm1.addAll(dn); // dn-1 := dn
			dn.clear(); dn.addAll(dnp1);   // dn   := dn+1
		}

		return dn;
	}


	static public 	<
	V extends Vertex<V, E, F>,
	E extends Edge<V, E, F>,
	F extends Face<V, E, F>
	> List<E> spanningTree( V center, Collection<E> ... es) {
		return spanningTree(center,null, es); // No stopping point
	}
	/**
	 * A spanning tree is a set of edges without cycles such that 
	 * every vertex is adjacent to one of these edges.
	 * @param <V>
	 * @param <E>
	 * @param <F>
	 * @param root the seed vertex
	 * @param stop Optional: a vertex where to stop (null accepted).
	 * @param es Optional: a Collection of ORIENTED Edges among which to take the edges of the spanning tree. 
	 * If empty, all the edges are taken. You may want to symetrize the collection before calling.
	 * @param vs forbiddenVertices vertices that don't grow. In particular the edges should not have isthmus.
	 * @return A spanning tree of edges (among the optional collection) pointing towards the center. 
	 * The process stops if the stop vertex is hit. The edges are ordered from the root outwards so an iteration
	 * on the {@link Edge#getStartVertex()} of the tree loops through the vertices.
	 * @see symetrize
	 */

	static public 	<
	V extends Vertex<V, E, F>,
	E extends Edge<V, E, F>,
	F extends Face<V, E, F>
	> List<E> spanningTree( V root, V stop, Collection<E> ... es) {
		Set<E> edges = new HashSet<E>();
		Set<V> fV = new HashSet<V>(); // No forbidden Vertices
		
		for(Collection<E> le: es) edges.addAll(le);
		
		return spanningTree(root, stop, edges, fV);
	}
	static public 	<
	V extends Vertex<V, E, F>,
	E extends Edge<V, E, F>,
	F extends Face<V, E, F>
	> List<E> spanningTree( V root, V stop, Collection<E> edges, Collection<V> forbiddenVertices) {
		root.checkHalfEdgeDataStructure(stop);

		final List<E> tree = new ArrayList<E>(); // The result

		if (edges == null) edges = new ArrayList<E>();
		if (edges.isEmpty()) edges.addAll(root.getHalfEdgeDataStructure().edgeList);
		
		if (forbiddenVertices == null) forbiddenVertices = new ArrayList<V>();
		Set<V> tagged = new HashSet<V>();

		tagged.clear(); // The tagged vertices


		int max = root.getHalfEdgeDataStructure().getNumEdges();


		// If empty, take them all.
		if(edges.isEmpty()) edges.addAll(root.getHalfEdgeDataStructure().edgeList);

		final ArrayList<V> 
		dn   = new ArrayList<V>(), // Vertices at distance n
		dnp1 = new ArrayList<V>(); // Vertices at distance n+1

		dn.add(root);
		tagged.add(root);
		int np1 = 0;

		while((np1++<max) && (!dn.isEmpty())){
			dnp1.clear();
			for(V v: dn){
				List<E> s = v.getEdgeStar();
				s.retainAll(edges);
				for(E e: s) {
					V vnp1 = e.getStartVertex();
					if(!tagged.contains(vnp1)){ // Not reached yet
						tagged.add(vnp1);
						dnp1.add(vnp1);
						tree.add(e);
						if(vnp1 == stop) 
							return tree;
					}
				}
			} 

			// dnp1 is the shell at distance n+1.

			dnp1.removeAll(forbiddenVertices); // Don't grow them
			dn.clear(); dn.addAll(dnp1);   // dn   := dn+1
		}

		return tree;
	}

	static public 	<
	V extends Vertex<V, E, F>,
	E extends Edge<V, E, F>,
	F extends Face<V, E, F>
	> List<E> followTree( final V v0, final List<E> orientedTree) {
		return followTree(v0, orientedTree.size(), orientedTree);
	}

	/**
	 * Follows the tree from the given vertex down to the root, optionally in stop steps.
	 * @param <V>
	 * @param <E>
	 * @param <F>
	 * @param v a vertex in the tree
	 * @param stop the number of steps to take
	 * @param orientedTree a rooted tree with edges oriented towards the root
	 * @return the path of edges leading to the root. Empty if v is not in the tree.
	 */
	static public 	<
	V extends Vertex<V, E, F>,
	E extends Edge<V, E, F>,
	F extends Face<V, E, F>
	> List<E> followTree( final V v0, int stop, final List<E> orientedTree) {
		List<E> path = new ArrayList<E>();
		V v = v0;
		boolean isTheRoot = false; // The root is the only vertex from which no edge starts.

		while (!isTheRoot && (stop-- > 0)) {
			isTheRoot = true;
			for(E e: v.getEdgeStar()){ // The star points to v, we need the opposite.
				if(orientedTree.contains(e.getOppositeEdge())){
					path.add(e.getOppositeEdge());
					isTheRoot = false;
					v = e.getStartVertex();
					break;
				}
			}
		}
		return path;
	}
	/**
	 * A path from a to b, optionally where edges are chosen among the given list(s).
	 * @param <V>
	 * @param <E>
	 * @param <F>
	 * @param a
	 * @param b
	 * @param edges (Optional) A set of ORIENTED edges among which to choose to connect a to b. 
	 * If empty, all edges are chosen. You may consider symetrizing the edges before calling.
	 * @return a smallest list of edges that go from a to b (optionally among the given edges).
	 * The list is empty if there is no such path.
	 * @see symetrize
	 */
	static public 	<
	V extends Vertex<V, E, F>,
	E extends Edge<V, E, F>,
	F extends Face<V, E, F>
	> List<E> path( final V v0, final V v1,  Collection <E> ... es) {
		v0.checkHalfEdgeDataStructure(v1);
		Set<E> edges = new HashSet<E>();
		for(Collection<E> le: es) edges.addAll(le);

		return HomotopyUtility.followTree(v0, HomotopyUtility.spanningTree(v1, v0, edges));
	}

	/**
	 * A homotopy basis is a list of cycles spanning the homology.
	 * @param <V>
	 * @param <E>
	 * @param <F>
	 * @param root the center of the fundamental polygon.
	 * @return a list of cycles.
	 */
	static public 	<
	V extends Vertex<V, E, F>,
	E extends Edge<V, E, F>,
	F extends Face<V, E, F>
	> List<List<E>> homotopyBasis( final V root ) {	
		return homotopyBasis(root, spanningTree(root));
	}
		/**
		 * A homotopy basis is a list of cycles spanning the homology.
		 * @param <V>
		 * @param <E>
		 * @param <F>
		 * @param root the center of the fundamental polygon.
		 * @param tree a spanning tree of edges pointing to the root, listed from the root outwards.
		 * @return a list of cycles.
		 */
		static public 	<
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
		> List<List<E>> homotopyBasis( final V root, List<E> tree) {
		final HalfEdgeDataStructure<V, E, F> G = root.getHalfEdgeDataStructure();
		// Suppose it is a closed surface
		//TODO: what happens if it is not the case.
		int genus = 1 -(G.getNumFaces()-G.getNumEdges()+G.getNumVertices())/2;

		final List<List<E>> basis = new ArrayList<List<E>>(2*genus); // The result
		
		final List<E> symTree =  symetrize(tree);

		// Each face has some edges in the spanning tree.
		// We are going to close the faces.
		final Map<F,List<E>> toClose = new HashMap<F, List<E>>(G.getNumFaces());
		final ArrayList<List<E>> toCloseList = new ArrayList<List<E>>(G.getNumFaces());

		for(E e: tree){ // From the root outwards
			V v = e.getStartVertex();
			for(F lf : v.getFaceStar())
			if(!toClose.containsKey(lf)) {
				List<E> b =  lf.getBoundary();
				b.removeAll(symTree);
				if(b.isEmpty()) throw new RuntimeException("The tree has cycles!");
				toCloseList.add(b);
				toClose.put(lf, b);
			}
			
		}

		class Pruner {
			final Set<F> toErase = new HashSet<F>();
			void prune(){
				boolean isPruned;
				do {
					for(F f: toErase) {
						toCloseList.remove(toClose.get(f));
						toClose.remove(f);
					}
					toErase.clear();
					isPruned = true;
					for(List<E> bf: toCloseList){
						// If the face is almost closed,
						if(bf.size() == 1){
							E e = bf.get(0);
							// Close it by adding the edge to the selected set.
							close(e);
							isPruned = false;
						} 
					}
					for(F f: toErase) {
						toCloseList.remove(toClose.get(f));
						toClose.remove(f);
					}
				} while (!isPruned);
			}

			void close(E e){
				List<E> lEF = toClose.get(e.getLeftFace());
				if (lEF != null){ 
					lEF.remove(e);
					if(lEF.isEmpty()) toErase.add(e.getLeftFace());
				}

				lEF = toClose.get(e.getRightFace());
				if (lEF != null) { 
					lEF.remove(e.getOppositeEdge());
					if(lEF.isEmpty()) toErase.add(e.getRightFace());
				}
			}
		}

		Pruner pruner = new Pruner();

		pruner.prune();

		while(!toClose.isEmpty()){

			//Select the first edge of a remaining non closed face.
			E e  = toCloseList.get(0).get(0);

			if(e == null) throw new RuntimeException("I couldn't find an edge to close!"+toClose.size()+toCloseList.get(0).size());

			List<E> cycle = HomotopyUtility.path(e.getTargetVertex(), e.getStartVertex(), symTree);

			cycle.add(e);

			basis.add(cycle );

			pruner.close(e);

			pruner.prune();
		}



		return basis;
	}

	/**
	 * A simple cycle is trivial iff it disconnects the graph in two connected component.
	 * @param <V>
	 * @param <E>
	 * @param <F>
	 * @param cycle A SIMPLE CONNECTED cycle: does not cross itself.
	 * @return true if the spanning tree on one side doesn't reach the other side.
	 */
	static public 	<
	V extends Vertex<V, E, F>,
	E extends Edge<V, E, F>,
	F extends Face<V, E, F>
	> boolean isTrivial(final List<E> cycle) {
		if(!isClosed(cycle)) 
			throw new IllegalArgumentException("Give me a cycle!");
		final HalfEdgeDataStructure<V, E, F> G = cycle.get(0).getHalfEdgeDataStructure();

		Set<V> vertices = new HashSet();
		Set<E> edges    = new HashSet(G.edgeList);


		for(E e: cycle){
			vertices.add(e.getTargetVertex());
			edges.remove(e); edges.remove(e.getOppositeEdge());
		}

		V v0 = null;
		for(V v1: G.vertexList) 
			if(!vertices.contains(v1)) {
				v0 = v1; break;
			}

		if(v0 == null) 
			throw new RuntimeException("Sorry, I can't tell, the cycle contains all the vertices.");

		final V root = v0; // All this to find a root not on the cycle!

		// We look for a rooted spanning tree, stopping nowhere, staying among the edges 
		// and avoiding the vertices.
		List<E> tree = spanningTree(root, null, edges, vertices);
		final List<E>  symCycle = symetrize(cycle);
		final List<E>   symTree  = symetrize(tree);


		// Each face has some edges in the spanning tree.
		// We are going to close the faces.
		final Map<F,List<E>> toClose = new HashMap<F, List<E>>(G.getNumFaces());

		for(F f: G.faceList){
			List<E> b =  f.getBoundary();
			b.removeAll(symTree);
			if(!b.isEmpty()) // throw new RuntimeException("The tree has cycles!");
				toClose.put(f, b);
		}

		class Pruner {
			final Set<F> toErase = new HashSet();
			void prune(){
				boolean isPruned;
				do {
					isPruned = true;
					toErase.clear();
					for(F f: toClose.keySet()){
						// If the face is almost closed,
						if(toClose.get(f).size() == 1){
							E e=toClose.get(f).get(0);
							if(!symCycle.contains(e)){
								// Close it
								close(e);
								isPruned = false;
							}
						} 
					}
					erase();				
				} while (!isPruned);
			}

			void erase() {
				for(F f: toErase){
					toClose.remove(f);
				}
			}
			void close(E e){
				List<E> lEF = toClose.get(e.getLeftFace());
				if (lEF != null){ 
					lEF.remove(e);
					if(lEF.isEmpty()) toErase.add(e.getLeftFace());
				}
				lEF = toClose.get(e.getRightFace());
				if (lEF != null){ 
					lEF.remove(e.getOppositeEdge());
					if(lEF.isEmpty()) toErase.add(e.getRightFace());
				}
			}
		}

		Pruner pruner = new Pruner();


		E e;

		do {
			pruner.prune();

			e = null;

			//Select the first edge of a remaining non closed but open face.
			search:
				for(F f : toClose.keySet()) {
					if(toClose.get(f).size() < f.getBoundary().size())
						for(E e0: toClose.get(f)){
							if(!symCycle.contains(e0)) {
								e = e0;
								pruner.close(e);
								pruner.erase();
								break search;					
							}
						}
				}						

		} while (e != null);

		for(F f: toClose.keySet()) {
			if(toClose.get(f).size() == f.getBoundary().size()) return true; // We missed a face.

		}	
		return false;
	}

	static public 	<
	V extends Vertex<V, E, F>,
	E extends Edge<V, E, F>,
	F extends Face<V, E, F>
	> boolean isClosed(List<E> edges) {
		return isPath(edges) && 
		(edges.get(0).getStartVertex() == edges.get(edges.size()-1).getTargetVertex());
	}
	static public 	<
	V extends Vertex<V, E, F>,
	E extends Edge<V, E, F>,
	F extends Face<V, E, F>
	> boolean isPath(List<E> edges) {
		E e = edges.get(0);
		for(E ne: edges) {
			if ((ne != e) && (e.getTargetVertex() != ne.getStartVertex())) return false;
			e = ne;
		}
		return true;
	}

	static public 	<
	V extends Vertex<V, E, F>,
	E extends Edge<V, E, F>,
	F extends Face<V, E, F>
	> boolean isDualClosed(List<E> edges) {
		return isDualPath(edges) && 
		(edges.get(0).getRightFace() == edges.get(edges.size()-1).getLeftFace());
	}
	static public 	<
	V extends Vertex<V, E, F>,
	E extends Edge<V, E, F>,
	F extends Face<V, E, F>
	> boolean isDualPath(List<E> edges) {
		E e = edges.get(0);
		for(E ne: edges) {
			if ((ne != e) && (e.getLeftFace() != ne.getRightFace())) return false;
			e = ne;
		}
		return true;
	}

	static public 	<
	V extends Vertex<V, E, F>,
	E extends Edge<V, E, F>,
	F extends Face<V, E, F>
	> Set<E> opposite(Set<E> edges) {
		Set<E> opEdges = new HashSet<E>(edges.size()); 
		for(E e: edges) opEdges.add(e.getOppositeEdge());
		return opEdges;
	}
	static public 	<
	V extends Vertex<V, E, F>,
	E extends Edge<V, E, F>,
	F extends Face<V, E, F>
	> List<E> opposite(List<E> edges) {
		List<E> opEdges = new ArrayList<E>(edges.size()); 
		for(E e: edges) opEdges.add(0,e.getOppositeEdge());
		return opEdges;
	}

	static public 	<
	V extends Vertex<V, E, F>,
	E extends Edge<V, E, F>,
	F extends Face<V, E, F>
	> Set<E> symetrize(Set<E> edges) {
		Set<E> opEdges = new HashSet<E>(2*edges.size()); 
		opEdges.addAll(edges);
		for(E e: edges) opEdges.add(e.getOppositeEdge());
		return opEdges;
	}
	static public 	<
	V extends Vertex<V, E, F>,
	E extends Edge<V, E, F>,
	F extends Face<V, E, F>
	> List<E> symetrize(List<E> edges) {
		List<E> opEdges = new ArrayList<E>(2*edges.size()); 
		opEdges.addAll(edges);
		for(E e: edges) opEdges.add(0,e.getOppositeEdge());
		return opEdges;
	}

}

