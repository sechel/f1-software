package minimalsurface.frontend.macro;

import halfedge.HalfEdgeDataStructure;
import halfedge.decorations.HasQuadGraphLabeling.QuadGraphLabel;
import halfedge.surfaceutilities.Subdivision;
import image.ImageHook;

import java.awt.FlowLayout;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.vecmath.Point4d;

import math.util.VecmathTools;
import circlepatterns.graph.CPEdge;
import circlepatterns.graph.CPFace;
import circlepatterns.graph.CPVertex;

public class CentralExtensionSubdivide extends MacroAction {

	private static enum GeometryType {
		Koebe,
		Conical
	}
	
	protected Icon 
		icon = new ImageIcon(ImageHook.getImage("edgesubdivide.png"));
	protected JComboBox
		geometryCombo = new JComboBox(GeometryType.values());
	private JPanel
		optionPanel = new JPanel();
	
	public CentralExtensionSubdivide() {
		optionPanel.setLayout(new FlowLayout());
		optionPanel.add(new JLabel("Geometry"));
		optionPanel.add(geometryCombo);
	}
	
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
			if (GeometryType.Koebe == getSelectedGeomtry()) {
				VecmathTools.sphereMirror(f.getXYZW());
				v.setVertexLabel(QuadGraphLabel.CIRCLE);
			} else {
				v.setVertexLabel(QuadGraphLabel.SPHERE);
			}
			v.setXYZW(f.getXYZW());
		}
		for (CPEdge e : edgeVertexMap.keySet()) {
			if (e.isPositive()) continue;
			CPVertex v = edgeVertexMap.get(e);
			if (GeometryType.Koebe == getSelectedGeomtry()) {
				if (e.getXYZW() != null) {
					v.setXYZW(e.getXYZW());
				}
			}
			Point4d ep = new Point4d(v.getXYZW());
			VecmathTools.sphereMirror(ep);
			v.setXYZW(ep);
			v.setVertexLabel(QuadGraphLabel.INTERSECTION);
		}
		for (CPVertex v : vertexVertexMap.keySet()) {
			CPVertex vv = vertexVertexMap.get(v);
			v.setXYZW(vv.getXYZW());
			if (GeometryType.Koebe == getSelectedGeomtry()) {
				v.setVertexLabel(QuadGraphLabel.SPHERE);
			} else {
				v.setVertexLabel(QuadGraphLabel.INTERSECTION);
			}
		}
		return quad;
	}
	
	public GeometryType getSelectedGeomtry() {
		return (GeometryType)geometryCombo.getSelectedItem();
	}
	
	@Override
	public Icon getIcon() {
		return icon;
	}
	
	@Override
	public JPanel getOptionPanel() {
		return optionPanel;
	}

}
