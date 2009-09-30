package halfedge.surfaceutilities;

import halfedge.Edge;
import halfedge.Face;
import halfedge.HalfEdgeDataStructure;
import halfedge.Vertex;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import de.jreality.geometry.IndexedFaceSetFactory;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.DoubleArrayArray;
import de.jreality.scene.data.IntArrayArray;

public class Converter {

	public static interface PositionConverter<V extends Vertex<?,?,?>>{
		
		public double[] getPosition(V v);
		
		public void setPosition(V v, double[] pos);
		
	}
	
	
	public static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> HalfEdgeDataStructure<V, E, F> ifs2heds(IndexedFaceSet ifs, Class<V> vClass, Class<E> eClass, Class<F> fClass, PositionConverter<V> p){
		DoubleArrayArray vData = (DoubleArrayArray)ifs.getVertexAttributes(Attribute.COORDINATES);
		double[][] vertices = vData.toDoubleArrayArray(null); 
		IntArrayArray fData = (IntArrayArray)ifs.getFaceAttributes(Attribute.INDICES);
		int[][] faces = fData.toIntArrayArray(null);
		return ifs2heds(vertices, faces, vClass, eClass, fClass, p);
	}
	
	
	
	public static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> HalfEdgeDataStructure<V, E, F> ifs2heds(double[][] vertices, int[][] faces, Class<V> vClass, Class<E> eClass, Class<F> fClass, PositionConverter<V> p){
		HalfEdgeDataStructure<V, E, F> heds = HalfEdgeDataStructure.createHEDS(vClass, eClass, fClass);
		// vertices
		for (int i = 0; i < vertices.length; i++){
			V v = heds.addNewVertex();
			if (vertices[i].length == 4)
				p.setPosition(v, vertices[i]);
			else {
				double[] pos = new double[]{vertices[i][0], vertices[i][1], vertices[i][2], 1.0};
				p.setPosition(v, pos);
			}
		}
		
		// edges
		DualHashMap<V, V, E> vertexEdgeMap = new DualHashMap<V, V, E>();
		HashMap<V, Collection<E>> startVertexMap = new HashMap<V, Collection<E>>();
		for (int i = 0; i < faces.length; i++){
			int[] f = faces[i];
			for (int j = 0; j < f.length; j++){
				V s = heds.getVertex(f[j]);
				V t = heds.getVertex(f[(j + 1) % f.length]);
				if (vertexEdgeMap.containsKey(s, t))
					throw new RuntimeException("Inconsistently oriented face found in ifs2HEDS, discontinued!");
				E e = heds.addNewEdge();
				e.setTargetVertex(t);
				if (startVertexMap.get(s) == null)
					startVertexMap.put(s, new LinkedList<E>());
				startVertexMap.get(s).add(e);
				vertexEdgeMap.put(s, t, e);
			}
		}
		
		// faces, linkage, and boundary edges
		for (int i = 0; i < faces.length; i++){
			int[] face = faces[i];
			F f = heds.addNewFace();
			for (int j = 0; j < face.length; j++){
				V s = heds.getVertex(face[j]);
				V t = heds.getVertex(face[(j + 1) % face.length]);
				V next = heds.getVertex(face[(j + 2) % face.length]);
				E faceEdge = vertexEdgeMap.get(s, t);
				E oppEdge = vertexEdgeMap.get(t, s);
				if (oppEdge == null){
					oppEdge = heds.addNewEdge();
					oppEdge.setTargetVertex(s);
					if (startVertexMap.get(t) == null)
						startVertexMap.put(t, new LinkedList<E>());
					startVertexMap.get(t).add(oppEdge);
				}
				E nextEdge = vertexEdgeMap.get(t, next);
				faceEdge.linkOppositeEdge(oppEdge);
				faceEdge.linkNextEdge(nextEdge);
				faceEdge.setLeftFace(f);
			}
		}
		
		
		// link boundary
		for (E e : heds.getEdges()) {
			if (e.getLeftFace() != null) 
				continue;
			V t = e.getTargetVertex();
			Collection<E> coll = startVertexMap.get(t);
			for (E n : coll) {
				if (n != e && n.getLeftFace() == null)
					e.linkNextEdge(n);
			}
		}
		
		if (!ConsistencyCheck.isValidSurface(heds))
			throw new RuntimeException("No valid surface constructed in ifs2heds()");
		
		return heds;
	}
	
	
	
	
	public static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> IndexedFaceSet heds2ifs(HalfEdgeDataStructure<V, E, F> heds, PositionConverter<V> p){
		IndexedFaceSetFactory ifs = new IndexedFaceSetFactory();
		ifs.setVertexCount(heds.getVertices().size());
		ifs.setFaceCount(heds.getFaces().size());
		double[][] vertices = new double[heds.getVertices().size()][];
		for (int i = 0; i < heds.getVertices().size(); i++)
			vertices[i] = p.getPosition(heds.getVertex(i));
		int[][] faces = new int[heds.getFaces().size()][];
		for (int i = 0; i < heds.getFaces().size(); i++) {
			F f = heds.getFace(i);
			List<E> b = f.getBoundary();
			int[] face = new int[b.size()];
			int j = 0;
			for (E e : b) {
				face[j] = e.getTargetVertex().getIndex();
				j++;
			}
			faces[i] = face;
		}
		ifs.setVertexCoordinates(vertices);
		ifs.setFaceIndices(faces);
		ifs.setGenerateEdgesFromFaces(true);
		ifs.setGenerateFaceNormals(true);
		ifs.update();
		return ifs.getIndexedFaceSet();
	}

	
	
	
	public static class DualHashMap<K1, K2, V> implements Cloneable{

		private HashMap<K1, HashMap<K2, V>>
			map = new HashMap<K1, HashMap<K2,V>>();
		
		public void clear(){
			map.clear();
		}
		
		public boolean containsKey(K1 key1, K2 key2){
			HashMap<K2, V> vMap = map.get(key1);
			if (vMap == null)
				return false;
			else
				return vMap.get(key2) != null;
		}
		
		public boolean containsValue(V value){
			for (K1 key : map.keySet()){
				HashMap<K2,V> vMap = map.get(key);
				if (vMap.containsValue(value))
					return true;
			}
			return false;
		}
		
		public boolean isEmpty(){
			return map.isEmpty();
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

		public Collection<V> get(K1 key1){
			HashMap<K2, V> vMap = map.get(key1);
			if (vMap == null)
				return Collections.emptyList();
			else
				return vMap.values();
		}
		
		
		public V remove(K1 key1, K2 key2){
			HashMap<K2, V> vMap = map.get(key1);
			if (vMap == null)
				return null;
			else {
				V result = vMap.remove(key2);
				if (vMap.isEmpty())
					map.remove(key1);
				return result;
			}
		}
	}
	
	
	
}
