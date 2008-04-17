package minimalsurface.frontend.macro;

import halfedge.HalfEdgeDataStructure;
import halfedge.surfaceutilities.SurfaceUtility;
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

import minimalsurface.util.MinimalSurfaceUtility;
import circlepatterns.graph.CPEdge;
import circlepatterns.graph.CPFace;
import circlepatterns.graph.CPVertex;

public class Dualize extends MacroAction {

	protected Icon 
		icon = new ImageIcon(ImageHook.getImage("schwarz.png"));
	protected boolean
		mergeDegenerateFaces = true; 
	
	public String getName() {
		return "Dualize Surface";
	}

	public HalfEdgeDataStructure<CPVertex, CPEdge, CPFace> 
		process(HalfEdgeDataStructure<CPVertex, CPEdge, CPFace> graph) throws Exception {
		
		MinimalSurfaceUtility.createEdgeLabels(graph);
		
		
//		if (mergeDegenerateFaces) {
//			List<CPFace> faces = new LinkedList<CPFace>(graph.getFaces());
//			for (CPFace f : faces) {
//				if (f.isDegenerate(1E-10)) {
//					graph.removeFace(f);
//					System.err.println("Removed Face: " + f);
//				}
//			}
//		}
		
		
		MinimalSurfaceUtility.dualizeSurface(graph, true);
		SurfaceUtility.rescaleSurface(graph, 5.0);
		
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


		public void actionPerformed(ActionEvent e) {
			mergeDegenerateFaces = mergeChecker.isSelected();
		}
		
		
	}
	
	
}
