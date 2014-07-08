package minimalsurface.frontend.macro;

import halfedge.HalfEdgeDataStructure;
import image.ImageHook;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import circlepatterns.graph.CPEdge;
import circlepatterns.graph.CPFace;
import circlepatterns.graph.CPVertex;

public class DualizeKoenigsQuads extends MacroAction {

	protected Icon 
		icon = new ImageIcon(ImageHook.getImage("schwarz.png"));
	protected boolean
		mergeDegenerateFaces = true;
	private CalculateDiagonalIntersections 
		intersections = new CalculateDiagonalIntersections();
	private DualizeConicalQuads 
		dualizer = new DualizeConicalQuads();
	
	@Override
	public String getName() {
		return "Dualize Koenigs Quads";
	}

	@Override
	public HalfEdgeDataStructure<CPVertex, CPEdge, CPFace> 
		process(HalfEdgeDataStructure<CPVertex, CPEdge, CPFace> graph) throws Exception {
		intersections.process(graph);
		dualizer.process(graph);
		return graph;
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
			mergeChecker = new JCheckBox("Merge Degenerate Faces", mergeDegenerateFaces);
		private JPanel
			preprocessPanel = new JPanel();
		
		public OptionPanel() {
			setLayout(new BorderLayout());
			
			preprocessPanel.setLayout(new GridLayout(1, 1));
			preprocessPanel.add(mergeChecker);
			preprocessPanel.setBorder(BorderFactory.createTitledBorder("Preprocessing"));
			
			add(preprocessPanel, BorderLayout.NORTH);
			
			mergeChecker.addActionListener(this);
		}


		@Override
		public void actionPerformed(ActionEvent e) {
			mergeDegenerateFaces = mergeChecker.isSelected();
		}
		
		
	}
	
	
}
