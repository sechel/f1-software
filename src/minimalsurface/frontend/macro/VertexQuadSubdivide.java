package minimalsurface.frontend.macro;

import halfedge.HalfEdgeDataStructure;
import halfedge.decorations.HasQuadGraphLabeling.QuadGraphLabel;
import halfedge.surfaceutilities.Subdivision;
import image.ImageHook;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import math.util.VecmathTools;
import circlepatterns.graph.CPEdge;
import circlepatterns.graph.CPFace;
import circlepatterns.graph.CPVertex;

public class VertexQuadSubdivide extends MacroAction {

	protected Icon 
		icon = new ImageIcon(ImageHook.getImage("vertexsubdivide.png"));
	private boolean 
		onFaces = true;
	
	@Override
	public String getName() {
		return "Vertex-Quad-Graph Subdividion";
	}

	@Override
	public HalfEdgeDataStructure<CPVertex, CPEdge, CPFace> process(
			HalfEdgeDataStructure<CPVertex, CPEdge, CPFace> graph) throws Exception{
		HashMap<CPVertex, CPVertex> vertexVertexMap = new HashMap<CPVertex, CPVertex>();
		HashMap<CPFace, CPVertex> faceVertexMap = new HashMap<CPFace, CPVertex>();
		HalfEdgeDataStructure<CPVertex, CPEdge, CPFace> quad = Subdivision.createVertexQuadGraph(graph, vertexVertexMap, faceVertexMap);
		
		for (CPVertex v : quad.getVertices())
			v.setVertexLabel(QuadGraphLabel.INTERSECTION);
		
		for (CPFace f : faceVertexMap.keySet()){
			CPVertex v = faceVertexMap.get(f);
			v.setXYZW(f.getXYZW());
			if (onFaces == f.getLabel()){
				VecmathTools.sphereMirror(v.getXYZW());
				v.setVertexLabel(QuadGraphLabel.CIRCLE);
			} else {
				v.setVertexLabel(QuadGraphLabel.SPHERE);
			}
		}
		return quad;
	}
	
	@Override
	public Icon getIcon() {
		return icon;
	}
	
	@Override
	public JPanel getOptionPanel() {
		if (optionPanel == null) {
			optionPanel = new OptionPanel();
		}
		return optionPanel;
	}
	
	
	private class OptionPanel extends JPanel implements ActionListener{

		private static final long 
			serialVersionUID = 1L;
		
		private JRadioButton
			onVerticesButton = new JRadioButton("Vertices", !onFaces),
			onFacesButton = new JRadioButton("Faces", onFaces);
		private JPanel
			whichVertexPanel = new JPanel();
		
		
		public OptionPanel() {
			setLayout(new BorderLayout());
			
			whichVertexPanel.setLayout(new GridLayout(1, 2));
			whichVertexPanel.add(onVerticesButton);
			whichVertexPanel.add(onFacesButton);
			whichVertexPanel.setBorder(BorderFactory.createTitledBorder("Create Disks"));
			
			add(whichVertexPanel, BorderLayout.NORTH);
			
			ButtonGroup group = new ButtonGroup();
			group.add(onVerticesButton);
			group.add(onFacesButton);
			
			onVerticesButton.addActionListener(this);
			onFacesButton.addActionListener(this);
		}


		@Override
		public void actionPerformed(ActionEvent e) {
			onFaces = onFacesButton.isSelected();
		}
		
		
	}
	

}
