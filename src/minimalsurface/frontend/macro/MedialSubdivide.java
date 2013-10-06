package minimalsurface.frontend.macro;

import halfedge.HalfEdgeDataStructure;
import halfedge.HalfEdgeUtility;
import halfedge.surfaceutilities.Subdivision;
import halfedge.surfaceutilities.SurfaceUtility;
import image.ImageHook;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import circlepatterns.graph.CPEdge;
import circlepatterns.graph.CPFace;
import circlepatterns.graph.CPVertex;

public class MedialSubdivide extends MacroAction {

	protected Icon 
		icon = new ImageIcon(ImageHook.getImage("medialsubdivide.png"));
	
	private boolean
		removeDualsOnBoundary = false;
	private OptionPanel
		optionPanel = null;
	
	@Override
	public String getName() {
		return "Medial-Graph Subdividion";
	}

	@Override
	public HalfEdgeDataStructure<CPVertex, CPEdge, CPFace> process(
			HalfEdgeDataStructure<CPVertex, CPEdge, CPFace> graph) throws Exception{
		HashMap<CPVertex, CPFace> vertexFaceMap = new HashMap<CPVertex, CPFace>();
		HashMap<CPEdge, CPVertex> edgeVertexMap = new HashMap<CPEdge, CPVertex>();
		HashMap<CPEdge, CPEdge> edgeEdgeMap = new HashMap<CPEdge, CPEdge>();
		HashMap<CPFace, CPFace> faceFaceMap = new HashMap<CPFace, CPFace>();
		HalfEdgeDataStructure<CPVertex, CPEdge, CPFace> medial = Subdivision.createMedialGraph(graph, vertexFaceMap, edgeVertexMap, faceFaceMap, edgeEdgeMap);
		
		for (CPFace f : faceFaceMap.keySet()) {
			f.setCapitalPhi(faceFaceMap.get(f).getCapitalPhi());
		}
		if (removeDualsOnBoundary)
			removeDualsOnBoundary(medial);
		return medial;
	}
	
	
	private void removeDualsOnBoundary(HalfEdgeDataStructure<CPVertex, CPEdge, CPFace> graph) throws Exception{
		for (int i = 0; i < graph.getNumFaces(); i++){
			CPFace f = graph.getFace(i);
			if (f.isInteriorFace())
				continue;
			if (f.getBoundary().size() != 2)
				continue;
			CPEdge b = f.getBoundaryEdge();
			if (b.isBoundaryEdge())
				HalfEdgeUtility.removeEdge(b);
			else
				HalfEdgeUtility.removeEdge(b.getNextEdge());
			i--;
		}
		SurfaceUtility.linkBoundary(graph);
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
		
		private JCheckBox
			removeDualsChecker = new JCheckBox("Remove Duals On Boundary", removeDualsOnBoundary);
		
		public OptionPanel(){
			setLayout(new BorderLayout());
			add(removeDualsChecker);
			
			removeDualsChecker.addActionListener(this);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == removeDualsChecker)
				removeDualsOnBoundary = removeDualsChecker.isSelected();
		}
		
	}

}
