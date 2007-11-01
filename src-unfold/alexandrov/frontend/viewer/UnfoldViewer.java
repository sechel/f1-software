package alexandrov.frontend.viewer;

import static de.jreality.shader.CommonAttributes.AMBIENT_COLOR;
import static de.jreality.shader.CommonAttributes.DIFFUSE_COLOR;
import static de.jreality.shader.CommonAttributes.POLYGON_SHADER;
import static de.jreality.shader.CommonAttributes.SPECULAR_COLOR;
import static de.jreality.shader.CommonAttributes.SPHERES_DRAW;
import static de.jreality.shader.CommonAttributes.TRANSPARENCY;
import static de.jreality.shader.CommonAttributes.TRANSPARENCY_ENABLED;
import static de.jreality.shader.CommonAttributes.TUBES_DRAW;
import static de.jreality.shader.CommonAttributes.VERTEX_DRAW;
import static java.lang.Math.acos;
import halfedge.Edge;
import halfedge.Face;
import halfedge.HalfEdgeDataStructure;
import halfedge.HalfEdgeUtility;
import halfedge.Vertex;
import halfedge.decorations.HasAngle;
import halfedge.decorations.HasCurvature;
import halfedge.decorations.HasRadius;
import halfedge.decorations.HasXY;
import halfedge.decorations.HasXYZW;
import halfedge.decorations.IsBoundary;
import halfedge.decorations.IsFlippable;
import halfedge.decorations.IsHidable;
import halfedge.surfaceutilities.EmbeddedEdge;
import halfedge.triangulationutilities.Delaunay;
import halfedge.triangulationutilities.TriangulationException;
import halfedge.unfoldutilities.Unfolder;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.vecmath.AxisAngle4d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import math.util.VecmathTools;

import org.apache.commons.collections15.keyvalue.MultiKey;
import org.apache.commons.collections15.map.MultiKeyMap;

import de.jreality.geometry.IndexedFaceSetFactory;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;

public class UnfoldViewer <
		V extends Vertex<V, E, F> & HasXY & HasXYZW & HasRadius & HasCurvature,
		E extends Edge<V, E, F> & IsFlippable & IsBoundary & IsHidable & HasAngle,
		F extends Face<V, E, F>
	
	> extends ViewAddon<V,E,F>{
	
	private IndexedFaceSetFactory unfoldFactory = null;
	private IndexedFaceSet unfoldGeometry = null;
	
	private Appearance style = null;
	
	private HalfEdgeDataStructure<V, E, F> joinTree = null;
	private HalfEdgeDataStructure<V, E, F> voronoi = null;
	
	private int indexPosition = 0;
	
	private int step = 0;
	private int depth = 1;
	
	private boolean drawCutTree = true;
	
	private int numVertices = 0;
	private int numIndices = 0;
	
	private int maxVert = 0;
	private int maxInd = 0;

	
	double[][] vertexData = null;
	int[][] indexData = null;
	
	private boolean initedScene = false;
	
	MultiKeyMap foldData = null;
	List<F> leafs = null;
	
	Collection<EmbeddedEdge<V,E,F>> paths = null;
	
	double foldStrength = 0.0;

	public void setFoldStrength(double interp){
		foldStrength = interp;
	}
	
	public void setUnfoldDepth(int d){
		if(d <= numIndices)
			depth = d;
	}
	
	public void setVoronoi(HalfEdgeDataStructure<V,E,F> vor) {
		this.voronoi = vor;
	}
	
	public int getUnfoldDepth(){
		return depth;
	}

	public int getMaximumUnfoldDepth(){
		return numIndices;
	}
	
	V sourceVertex = null;
	
	public void setSource(V s) {
		sourceVertex = s;
	}
	
	public void update(){
		
		numVertices = joinTree.getNumVertices()*3;
		numIndices = joinTree.getNumVertices();

		vertexData = new double[numVertices][3];
		indexData = new int[numIndices][3];

		indexPosition = 0;
		step = 0;
		
		foldData = new MultiKeyMap();
		leafs = new ArrayList<F>();
		
		recursiveUnfold();
		
		maxInd = depth;
		maxVert= 3*indexPosition;
		
		double[][] tvd = new double[maxVert][3];
		int[][] tid = new int[maxInd][3];
		System.arraycopy(vertexData, 0, tvd, 0, maxVert);
		System.arraycopy(indexData, 0, tid, 0, maxInd);
		vertexData = tvd;
		indexData = tid;
			

		if(getSceneGraphComponent() != null && unfoldFactory != null){
			if(vertexData == null)
				System.err.println("This is bad.. vertexData hasnt been generated!");
		}
	}
	
	private boolean isFolded(F f) {
		for(MultiKey pair : (Set<MultiKey>)foldData.keySet()) {
			
			if(f == graph.getFace((Integer)pair.getKey(0)))
				return true;
		}
		return false;
	}
	
	public void recursiveUnfold() {
		
		// base case, because getBottom should always be in the base unfolding
		traverseTreeJoin(joinTree, joinTree.getVertex(Unfolder.getBottom(graph, sourceVertex).getIndex()), null);
		
		for(F f : leafs) {
			for(E e : f.getBoundary()) {
				if(!isFolded(e.getRightFace())) { // check if it really works
//					joinNodeFolded(newNode, oldNode)
				}
			}
		}
		
	}
	
	public MultiKeyMap getFoldData() {
		return foldData;
	}
	
	public void generateSceneGraphComponent(){

		if(!initedScene) {
			unfoldFactory = new IndexedFaceSetFactory();
			sgc = new SceneGraphComponent();
			
			style = new Appearance();
			
			style.setAttribute(POLYGON_SHADER + "." + AMBIENT_COLOR, new Color(255,255,155));
			style.setAttribute(POLYGON_SHADER + "." + DIFFUSE_COLOR, new Color(205,155,105));
			style.setAttribute(POLYGON_SHADER + "." + SPECULAR_COLOR, new Color(205,255,005));
	        style.setAttribute(TRANSPARENCY, 0.3);
	        style.setAttribute(TRANSPARENCY_ENABLED, true);
	        style.setAttribute(TUBES_DRAW, false);
			style.setAttribute(VERTEX_DRAW, false);
			style.setAttribute(SPHERES_DRAW, false);
	        
	        initedScene = true;
		}
		
		update();
		
		unfoldFactory.setVertexCount(maxVert);
		unfoldFactory.setFaceCount(maxInd);
		unfoldFactory.setGenerateEdgesFromFaces(true);
		unfoldFactory.setGenerateFaceNormals(true);
		unfoldFactory.setVertexCoordinates(vertexData);
		
		unfoldFactory.setFaceIndices(indexData);
		unfoldFactory.update();

		unfoldGeometry = unfoldFactory.getIndexedFaceSet();
		unfoldGeometry.setGeometryAttributes("pickable", false);
		
		sgc.setAppearance(style);
		sgc.setGeometry(unfoldGeometry);
		sgc.setVisible(true);
		sgc.setName("Fold net");
        
		
	}
	

	public void setJoinTree(HalfEdgeDataStructure<V, E, F> theJoinTree) {
		joinTree = theJoinTree;
	}
	
	private void traverseTree(HalfEdgeDataStructure<V,E,F> tree, V node, E incoming){
		
		System.err.println(node);
		//!!
		List<E> subtrees = HalfEdgeUtility.findEdgesWithTarget(node);
		
		for(E branch : subtrees){
			// if we havent been here before
			if(branch != incoming && branch.getOppositeEdge() != incoming){
				V nextNode = branch.getOppositeEdge().getTargetVertex();

				// if not a leaf, continue traversal
				List<E> newBranches = HalfEdgeUtility.findEdgesWithTarget(nextNode);
				// this should never fail
				newBranches.remove(branch.getOppositeEdge());
				// found a leaf?
				if (newBranches.isEmpty()){
					System.err.println(nextNode);
				} else {
					traverseTree(tree, nextNode, branch);
				}
			}
		}
	}

	private boolean traverseTreeRemember(HalfEdgeDataStructure<V,E,F> tree, V node, E incoming, List<V> prev){

//		System.err.println(node);
		if(prev.contains(node))
			return false;
		prev.add(node);
		
		List<E> subtrees = HalfEdgeUtility.findEdgesWithTarget(node);
		
		for(E branch : subtrees){
			// if we havent been here before
			if(branch != incoming && branch.getOppositeEdge() != incoming){
				V nextNode = branch.getOppositeEdge().getTargetVertex();

				// if not a leaf, continue traversal
				List<E> newBranches = HalfEdgeUtility.findEdgesWithTarget(nextNode);
				// this should never fail
				newBranches.remove(branch.getOppositeEdge());
				// found a leaf?
				if (newBranches.isEmpty()){
//					System.err.println(nextNode);
					if(prev.contains(nextNode))
						return false;
					prev.add(nextNode);
				} else {
					return traverseTreeRemember(tree, nextNode, branch, prev);
				}
			}
		}
		
		return true;
	}

	private void traverseTreeJoin(HalfEdgeDataStructure<V,E,F> tree, V node, E incoming){
		
//		if(step < depth){
		
			if(incoming == null){
				joinNodeFolded(node, null);
//				System.err.println("Folded the first face, at depth " + step);
			}
			else{
				V oldNode = incoming.getTargetVertex();
				joinNodeFolded(node, oldNode);
//				System.err.println("Joined " + node + " with " + oldNode + " at depth " + step);
			}
			step++;
			
			List<E> subtrees = HalfEdgeUtility.findEdgesWithTarget(node);
			
			for(E branch : subtrees){
				// if we havent been here before
				if(branch != incoming && branch.getOppositeEdge() != incoming){
					V nextNode = branch.getOppositeEdge().getTargetVertex();
		
					// if not a leaf, continue traversal
					List<E> newBranches = HalfEdgeUtility.findEdgesWithTarget(nextNode);
					// this should never fail
					newBranches.remove(branch.getOppositeEdge());
					// found a leaf?
					if (newBranches.isEmpty()){
						V oldNode = branch.getTargetVertex();
						joinNodeFolded(nextNode, oldNode);
						leafs.add(graph.getFace(nextNode.getIndex()));
//						System.err.println("Leaf-joined " + nextNode + " with " + oldNode + " at depth " + step);
					} else {
						traverseTreeJoin(tree, nextNode, branch);
					}
				}
			}
//		}	
	}
	
	private void joinNodeFolded(V newNode, V oldNode){
		boolean start = false;

		if(oldNode == newNode){
			System.err.println("Duplicate rectursion.. skipping");
			return;
		}
		
		if(oldNode == null){
//			System.err.println("No parent node given");
			start = true;
		}

		F newFace = graph.getFace(newNode.getIndex());	
		List<E> newEdges = HalfEdgeUtility.boundary(newFace);

		if(start){
			int j = 0;
			for (E e : newEdges){
				V v4 = e.getTargetVertex();
				Point3d v = VecmathTools.p4top3(v4.getXYZW());
				vertexData[3*indexPosition + j][0] = v.x;
				vertexData[3*indexPosition + j][1] = v.y;
				vertexData[3*indexPosition + j][2] = v.z;
				
				j++;
				foldData.put(newFace.getIndex(), v4, v);
				
			}
		} else {
			
			// the old face, for reference
			F oldFace = graph.getFace(oldNode.getIndex());	
			List<E> oldEdges = HalfEdgeUtility.boundary(oldFace);
			
			// get one common edge and the relative orientation
			E commonEdge = null;
			for(E e : newEdges){
				if (oldEdges.contains(e.getOppositeEdge())){
					commonEdge = e;
				}
			}
			
			// make sure we found one
			if (commonEdge == null)
				System.err.println("Couldn't find a common edge!");

			V leftV = commonEdge.getStartVertex();
			V rightV = commonEdge.getTargetVertex();
			V thirdV = commonEdge.getNextEdge().getTargetVertex();
			
			// FIXME!
			// two of them should already be folded
			// these should pre-exeist by recursion
			Point3d rf = (Point3d)foldData.get(oldFace.getIndex(), rightV);
			Point3d lf = (Point3d)foldData.get(oldFace.getIndex(), leftV);
			if(rf == null || lf == null){
				System.err.println("Recursion error:");
				System.err.println("Looking for (" + oldFace + ", " + rightV + ") and (" + oldFace + ", " + leftV + ") in " + foldData);
				System.err.println("Strange, that the third is" + thirdV);
				return;
			}
			
//			// the third that we need to fold
//			Point3d v = VecmathTools.p4tov3(thirdV.getXYZW());
//			
//			// on the unfolded triangle, find the coordinates
//			// of the two "base" points
//			Point3d l = VecmathTools.p4tov3(leftV.getXYZW());
//			Point3d r = VecmathTools.p4tov3(rightV.getXYZW());
			
			// get the folded base
			Vector3d rlf = new Vector3d();
			rlf.sub(rf, lf);
			
			rlf.normalize();

			
			// calculate |v|, that is, the distance from the coordinate
			// v to the line (l, r)
			//                         |(r - l) x (l - v) |
			// this is given by |v| = -----------------------
			//                              |(r - l)|
			// where a,b are points of the line and c the point to measure
			// TODO can get rid of sqrt if we want, but it is no big deal
//			Vector3d rl = new Vector3d();
//			rl.sub(r, l);
			
//			Vector3d lv = new Vector3d();
//			lv.sub(l, v);
//			
//			Vector3d temp = new Vector3d();
//			temp.cross(rl, lv);
			
//			double dist = temp.length()/rl.length();
			
//			// the "hypotenuse", that is the vector from l to v
//			Vector3d vl = new Vector3d();
//			vl.sub(v, l);
//			// and the length of it
//			double hypl = vl.length();
//			
//			Vector3d vr = new Vector3d();
//			vr.sub(v, r);
//			double hypr = vl.length();
			
			Point3d base = new Point3d(lf);
			
			// now get the lenght of the basepoint of v on the axis from
			// the left vertex, l, by pythagoras theorem
//			double d = sqrt(hypl*hypl - dist*dist);
			
			Point3d vbprim = new Point3d();
			double lenprim = 0.0;
			double dprim = 0.0;
			double distprim = 0.0;
			
			try{
				double ar = Delaunay.getAngle(commonEdge);
				double al = Delaunay.getAngle(commonEdge.getPreviousEdge());
				
//				System.err.println("Intr. left angle is " + al);
//				System.err.println("Intr. right angle is " + ar);
				
				lenprim = commonEdge.getPreviousEdge().getLength();
				dprim = lenprim*Math.cos(al);
//				if(dprim < 0 || dprim > commonEdge.getLength())
//					System.err.println("Base is outside  triangle, good sign");
				
				vbprim.scaleAdd(dprim, rlf, base);
				distprim = Math.sqrt(lenprim*lenprim - dprim*dprim);
				
			} catch (TriangulationException e){
				System.err.println(e.getMessage());
				return;
			}

	
//			double tl = acos(rl.dot(vl));
//			double tr = acos(rl.dot(vr));
//			double sl = Math.signum(Math.PI/2.0 - tl);
//			double sr = Math.signum(Math.PI/2.0 - tr);
//			
//			
//			System.err.println("Extr. left angle is " + tl);
//			System.err.println("Extr. right angle is " + tr);
			
//			System.err.println(sl);
//			System.err.println(sr);

			
//			if(sl + sr < 0) {
//				System.err.println("Changing d");
//				d = -d;
//				base = rf;
//			}
			
			// get the basepoint on the axis
			// this is where v "starts"
//			Point3d vb = new Point3d();
//			vb.scaleAdd(d, rl, l);
			
		
//			drawHelpAbs(l, vb);
			
			// this will be the final folded point
			Point3d vf = new Point3d();
			
			double folding = foldStrength;
			if(step < depth && !drawCutTree)
				folding = 1.0;
			
			double angle = getAngle(newFace, oldFace);

			// this unfolds by rotating the face at the polytop
			// does not work!
//			if(interpUnfold) {
//				AxisAngle4d rotor = new AxisAngle4d(rl.x,
//													rl.y,
//													rl.z,
//													-folding*angle);
//				
//				// set up the rotation matrix
//				Matrix4d rotMat = new Matrix4d();
//				rotMat.set(rotor);
//
//				// translate v to the origin
//				Vector3d vo = new Vector3d();
//				vo.sub(v, vb);
//				
//				// and rotate our vector at origin
//				Vector3d vot= new Vector3d(vo);
//				rotMat.transform(vot);
//				
//				// now add it to the transformed base
//				Point3d vbt = new Point3d();
//				vbt.scaleAdd(d, rlf, base);
//				vf.add(vbt, vot);
			
//			} else {
				// this unfolds using the length of the vector
				// calculated above, and adding that amount of 
				// a ortogonal vector from the base, at the right place
				
				// the vector pointing where the folded vertex shall be placed
				Vector3d w = new Vector3d();
				w.cross(getFoldedFaceNormal(oldFace), rlf);
				w.normalize();
				
				// go to the left vertex of the folded face
				// and add d on the folded axis to get to the
				// base point
				// IMPORTANT: this assumes the the join of the two faces
				// is convex (as it should be by del. triang)
				vf = new Point3d();
//				vf.scaleAdd(dprim, rlf, base); //!
				
//				drawHelpAbs(base, vf);
				
				Vector3d ws = new Vector3d(w);

				// the rotation around the folded axis
				// in opposite angle
				AxisAngle4d rotor2 = new AxisAngle4d(rlf.x,
													 rlf.y,
													 rlf.z,
													 (1.0-folding)*angle);
				
				// set up the rotation matrix
				Matrix4d rotMat2 = new Matrix4d();
				rotMat2.set(rotor2);
				
				// rotate our pointing vector (to get interpolation)
				rotMat2.transform(ws);
				
				// now scale it by the distance of the original vector
				ws.scale(distprim);//!
				
//				drawHelp(vf, w);
				
				Point3d vfprim = new Point3d(vbprim);
				vfprim.add(ws);
				
//				drawHelpAbs(base, vbprim);
				
//				drawHelpAbs(vbprim, vfprim);
				
				// and add it to the base
//				vf.add(ws);
				vf = new Point3d(vfprim);
				
				
				
//			}

			foldData.put(newFace.getIndex(), thirdV, vf);
			foldData.put(newFace.getIndex(), rightV, rf);
			foldData.put(newFace.getIndex(), leftV,  lf);
			
			vertexData[3*indexPosition + 0][0] = rf.x;
			vertexData[3*indexPosition + 0][1] = rf.y;
			vertexData[3*indexPosition + 0][2] = rf.z;
			vertexData[3*indexPosition + 1][0] = lf.x;
			vertexData[3*indexPosition + 1][1] = lf.y;
			vertexData[3*indexPosition + 1][2] = lf.z;		
			vertexData[3*indexPosition + 2][0] = vf.x;
			vertexData[3*indexPosition + 2][1] = vf.y;
			vertexData[3*indexPosition + 2][2] = vf.z;
			
		}

		indexData[indexPosition][0] = 3*indexPosition + 0;
		indexData[indexPosition][1] = 3*indexPosition + 1;
		indexData[indexPosition][2] = 3*indexPosition + 2;
		
		indexPosition ++;

	}	

	// assuming convex polytope!
	// FIXME
	// this is the only extrinsic part and therefore does
	// so that it doesnt work with delaunay
	// (the interpolating part that is)
	// TODO move
	private double getAngle(F f1, F f2){
		double angle = 0.0;
		
		Vector3d n1 = getFaceNormal(f1);
		Vector3d n2  = getFaceNormal(f2);
		
		angle = acos(n1.dot(n2));
		
		return angle;
	}
	
	private Vector3d getFaceNormal(F face){
		
		E e1 = HalfEdgeUtility.findEdgeInBoundary(face);
		
		Vector3d v1 = VecmathTools.p4tov3(e1.getTargetVertex().getXYZW());
		Vector3d v2 = VecmathTools.p4tov3(e1.getStartVertex().getXYZW());
		Vector3d v3 = VecmathTools.p4tov3(e1.getNextEdge().getTargetVertex().getXYZW());
		
		Vector3d a = new Vector3d(v1.x - v2.x, v1.y - v2.y, v1.z - v2.z);
		Vector3d b = new Vector3d(v3.x - v2.x, v3.y - v2.y, v3.z - v2.z);
		
		a.normalize();
		b.normalize();
		
		Vector3d n = new Vector3d();
		n.cross(a, b);
		n.normalize(); // should not be needed

		return n;
	}
	
	private Vector3d getFoldedFaceNormal(F face){
		
		E e1 = HalfEdgeUtility.findEdgeInBoundary(face);
		
		V uv1 = e1.getTargetVertex();
		V uv2 = e1.getStartVertex();
		V uv3 = e1.getNextEdge().getTargetVertex();
		
		Point3d v1 = (Point3d)foldData.get(face.getIndex(), uv1);
		Point3d v2 = (Point3d)foldData.get(face.getIndex(), uv2);
		Point3d v3 = (Point3d)foldData.get(face.getIndex(), uv3);
		
		Vector3d a = new Vector3d(v1.x - v2.x, v1.y - v2.y, v1.z - v2.z);
		Vector3d b = new Vector3d(v3.x - v2.x, v3.y - v2.y, v3.z - v2.z);
		
		a.normalize();
		b.normalize();
		
		Vector3d n = new Vector3d();
		n.cross(a, b);
		n.normalize(); // should not be needed
		
		return n;
	}
	
	// FIXME rewrite and move
	public boolean isSpanningTree(HalfEdgeDataStructure<V,E,F> tree){
		
		ArrayList<V> nodes = new ArrayList<V>();
		return traverseTreeRemember(tree, tree.getVertex(0), null, nodes);

	}

}
