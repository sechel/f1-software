package minimalsurface.frontend.macro;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import halfedge.HalfEdgeDataStructure;
import minimalsurface.util.MinimalSurfaceUtility;
import circlepatterns.graph.CPEdge;
import circlepatterns.graph.CPFace;
import circlepatterns.graph.CPVertex;

public class AutoCut extends MacroAction {

	private boolean 
		cutToBorderOnly = false;
	private OptionPanel
		optionPanel = null;
	
	public String getName() {
		return "Auto Cut Singularities";
	}

	public HalfEdgeDataStructure<CPVertex, CPEdge, CPFace> 
		process(HalfEdgeDataStructure<CPVertex, CPEdge, CPFace> graph) throws Exception {
		if (cutToBorderOnly)
			MinimalSurfaceUtility.cutOddPointsToBoundary(graph);
		else
			MinimalSurfaceUtility.cutOddPoints(graph);
		return graph;
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
			toBoundaryChecker = new JCheckBox("To Boundary Only", cutToBorderOnly);
		
		public OptionPanel(){
			setLayout(new BorderLayout());
			add(toBoundaryChecker);
			
			toBoundaryChecker.addActionListener(this);
		}

		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == toBoundaryChecker)
				cutToBorderOnly = toBoundaryChecker.isSelected();
		}
		
	}
	
	
}
