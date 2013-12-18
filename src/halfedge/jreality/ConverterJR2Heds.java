package halfedge.jreality;

import halfedge.Edge;
import halfedge.Face;
import halfedge.HalfEdgeDataStructure;
import halfedge.Vertex;
import halfedge.decorations.HasXYZW;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.vecmath.Point4d;

import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.data.Attribute;


public class ConverterJR2Heds {

	public <
		V extends Vertex<V, E, F> & HasXYZW,
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void ifs2heds(IndexedFaceSet ifs, HDS heds) {
		ifs2heds(ifs, heds, null);
	}
	
	public <
		V extends Vertex<V, E, F> & HasXYZW,
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void ifs2heds(IndexedFaceSet ifs, HDS heds, Map<Integer, Edge<?,?,?>> edgeMap) {
		heds.clear();
		if (edgeMap != null) edgeMap.clear();
		
		double[][] coords = ifs.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);	
		int[][] indices = ifs.getFaceAttributes(Attribute.INDICES).toIntArrayArray(null);

		/// some facts:
		int numV = coords.length;
		int numF = indices.length;

		if (numV == 0) return;
		
		/// vertices
		for (int i = 0; i < numV; i++){
			V v = heds.addNewVertex();
			double[] p = coords[v.getIndex()];
			if (p.length < 4) {
				p = Arrays.copyOf(p, 4);
				p[3] = 1.0;
			}
			v.setXYZW(new Point4d(p));
		}
		
		// edges (from faces)
		DualHashMap<Integer, Integer, E> vertexEdgeMap = new DualHashMap<Integer, Integer, E>();
		for (int i = 0; i < numF; i++){
			int[] f = indices[i];
			if (f.length < 3) continue;
			for (int j = 0; j < f.length; j++){
				int s = f[j];
				int t = f[(j + 1) % f.length];
				if (s == t) continue;
				if (vertexEdgeMap.containsKey(s,t)) {
					throw new RuntimeException("Inconsistently oriented face found in ifs2HEDS, discontinued!");
				}
				E e = heds.addNewEdge();
				e.setTargetVertex(heds.getVertex(t));
				if (vertexEdgeMap.get(t, s) == e) {
					System.out.println("ConverterJR2Heds.ifs2heds()");
				}
				vertexEdgeMap.put(s, t, e);
			}
		}
		
		// faces, linkage, and boundary edges
		for (int i = 0; i < numF; i++){
			int[] face = indices[i];
			if (face.length < 3) continue;
			F f = heds.addNewFace();
			for (int j = 0; j < face.length; j++){
				int s = face[j];
				int t = face[(j + 1) % face.length];
				if (s == t) continue;
				int next = face[(j + 2) % face.length];
				if (next == t) {
					next = face[(j + 3) % face.length];
				}
				E faceEdge = vertexEdgeMap.get(s, t);
				E oppEdge = vertexEdgeMap.get(t, s);
				if (oppEdge == null){
					oppEdge = heds.addNewEdge();
					oppEdge.setTargetVertex(heds.getVertex(s));
					vertexEdgeMap.put(t, s, oppEdge);
				}
				E nextEdge = vertexEdgeMap.get(t, next);
				if (faceEdge == oppEdge) {
					System.out.println("ConverterJR2Heds.ifs2heds()");
				}
				faceEdge.linkOppositeEdge(oppEdge);
				faceEdge.linkNextEdge(nextEdge);
				faceEdge.setLeftFace(f);
			}	
		}
		
		// link boundary
		for (E e : heds.getEdges()) {
			if (e.getLeftFace() != null) continue;
			E temp = e.getOppositeEdge();
			while (temp.getLeftFace()!=null){
				temp = temp.getPreviousEdge();
				temp = temp.getOppositeEdge();
			}
			e.linkNextEdge(temp);
		}		
	}


	private static class DualHashMap<K1, K2, V> implements Cloneable{

		private HashMap<K1, HashMap<K2, V>>
		map = new HashMap<K1, HashMap<K2,V>>();


		public boolean containsKey(K1 key1, K2 key2){
			HashMap<K2, V> vMap = map.get(key1);
			if (vMap == null)
				return false;
			else
				return vMap.get(key2) != null;
		}


		public V put(K1 key1, K2 key2, V value){
			V previous = get(key1, key2);
			HashMap<K2, V> vMap = map.get(key1);
			if (vMap == null){
				vMap = new HashMap<K2, V>();
				map.put(key1, vMap);
			}
			vMap.put(key2, value);
			return previous;
		}

		public V get(K1 key1, K2 key2){
			HashMap<K2, V> vMap = map.get(key1);
			if (vMap == null)
				return null;
			else
				return vMap.get(key2);
		}

	}
}
