package minimalsurface.frontend.macro;

import halfedge.HalfEdgeDataStructure;
import halfedge.surfaceutilities.Subdivision;
import image.ImageHook;

import java.util.HashMap;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import math.util.VecmathTools;
import circlepatterns.graph.CPEdge;
import circlepatterns.graph.CPFace;
import circlepatterns.graph.CPVertex;

public class CentralExtensionSubdivide extends MacroAction {

	protected Icon 
		icon = new ImageIcon(ImageHook.getImage("edgesubdivide.png"));
	
	
	@Override
	public String getName() {
		return "Central Extension Subdivision";
	}

	@Override
	public HalfEdgeDataStructure<CPVertex, CPEdge, CPFace> process(HalfEdgeDataStructure<CPVertex, CPEdge, CPFace> graph) throws Exception {
		Map<CPVertex, CPVertex> vertexVertexMap = new HashMap<CPVertex, CPVertex>();
		Map<CPEdge, CPVertex> edgeVertexMap = new HashMap<CPEdge, CPVertex>();
		Map<CPFace, CPVertex> faceVertexMap = new HashMap<CPFace, CPVertex>();
		HalfEdgeDataStructure<CPVertex, CPEdge, CPFace> quad = Subdivision.createEdgeQuadGraph(graph, vertexVertexMap, edgeVertexMap, faceVertexMap);
		for (CPFace f : faceVertexMap.keySet()){
			CPVertex v = faceVertexMap.get(f);
			v.setXYZW(f.getXYZW());
			VecmathTools.sphereMirror(v.getXYZW());
		}
		for (CPEdge e : edgeVertexMap.keySet()) {
			CPVertex v = edgeVertexMap.get(e);
			VecmathTools.sphereMirror(v.getXYZW());
		}
		return quad;
	}
	
	@Override
	public Icon getIcon() {
		return icon;
	}

}
