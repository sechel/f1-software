package minimalsurface.frontend.macro;

import halfedge.HalfEdgeDataStructure;
import halfedge.surfaceutilities.Subdivision;
import image.ImageHook;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import circlepatterns.graph.CPEdge;
import circlepatterns.graph.CPFace;
import circlepatterns.graph.CPVertex;

public class EdgeQuadSubdivide extends MacroAction {

	protected Icon 
		icon = new ImageIcon(ImageHook.getImage("edgesubdivide.png"));
	
	@Override
	public String getName() {
		return "Edge-Quad-Graph Subdivision";
	}

	@Override
	public HalfEdgeDataStructure<CPVertex, CPEdge, CPFace> 
		process(HalfEdgeDataStructure<CPVertex, CPEdge, CPFace> graph) throws Exception {

//		HashMap<CPVertex, CPVertex> vertexVertexMap = new HashMap<CPVertex, CPVertex>();
//		HashMap<CPEdge, CPVertex> edgeVertexMap = new HashMap<CPEdge, CPVertex>();
//		HashMap<CPFace, CPVertex> faceVertexMap = new HashMap<CPFace, CPVertex>();
//		HalfEdgeDataStructure<CPVertex, CPEdge, CPFace> quad = Subdivision.createEdgeQuadGraph(graph, vertexVertexMap, edgeVertexMap, faceVertexMap);
//		
//		
//		for (CPFace f : faceVertexMap.keySet()){
//			CPVertex v = faceVertexMap.get(f);
//			v.setXYZW(f.getXYZW());
//			VecmathTools.sphereMirror(v.getXYZW());
//		}
		// :TODO Fix the bugs in Subdivision.createEdgeQuadGraph - this is a workaround
		HalfEdgeDataStructure<CPVertex, CPEdge, CPFace> quad = Subdivision.createMedialGraph(graph);
		quad = Subdivision.createVertexQuadGraph(quad);
		return quad;
	}
	
	@Override
	public Icon getIcon() {
		return icon;
	}

}
