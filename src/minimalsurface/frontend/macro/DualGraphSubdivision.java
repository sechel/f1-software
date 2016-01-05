package minimalsurface.frontend.macro;

import halfedge.HalfEdgeDataStructure;
import halfedge.surfaceutilities.Subdivision;
import image.ImageHook;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

import circlepatterns.graph.CPEdge;
import circlepatterns.graph.CPFace;
import circlepatterns.graph.CPVertex;

public class DualGraphSubdivision extends MacroAction {

	protected Icon 
		icon = new ImageIcon(ImageHook.getImage("edgesubdivide.png"));
	
	private OptionPanel
		optionPanel = null;
	
	@Override
	public String getName() {
		return "Dual-Graph Subdividion";
	}

	@Override
	public HalfEdgeDataStructure<CPVertex, CPEdge, CPFace> process(
		HalfEdgeDataStructure<CPVertex, CPEdge, CPFace> graph
	) throws Exception{
		HalfEdgeDataStructure<CPVertex, CPEdge, CPFace> r = Subdivision.createDualGraph(graph);
		return r;
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
		
		
		public OptionPanel(){
			setLayout(new BorderLayout());
		}

		@Override
		public void actionPerformed(ActionEvent e) {
		}
		
	}

}
