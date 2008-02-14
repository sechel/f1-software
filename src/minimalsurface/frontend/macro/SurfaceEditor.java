package minimalsurface.frontend.macro;

import static java.awt.GridBagConstraints.RELATIVE;
import static java.awt.GridBagConstraints.REMAINDER;
import static java.awt.GridBagConstraints.WEST;
import halfedge.HalfEdgeDataStructure;
import halfedge.HalfEdgeUtility;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import circlepatterns.graph.CPEdge;
import circlepatterns.graph.CPFace;
import circlepatterns.graph.CPVertex;

public class SurfaceEditor extends MacroAction {

	private boolean
		vertexEdit = false,
		edgeEdit = false,
		faceEdit = false;
	private int
		minValence = 10,
		minBoundary = 10;
	
	@Override
	public String getName() {
		return "Surface Editor";
	}

	@Override
	public HalfEdgeDataStructure<CPVertex, CPEdge, CPFace> 
		process(HalfEdgeDataStructure<CPVertex, CPEdge, CPFace> graph) throws Exception {

		if (vertexEdit) {
			for (int i = 0; i < graph.getNumVertices(); i++) {
				CPVertex v = graph.getVertex(i);
				List<CPEdge> s = v.getEdgeStar();
				if (s.size() >= minValence) {
					HalfEdgeUtility.removeVertex(v);
					i--;
				}
			}
		}
		if (edgeEdit) {
			// nothing yet
		}
		
		if (faceEdit) {
			for (int i = 0; i < graph.getNumFaces(); i++) {
				CPFace f = graph.getFace(i);
				List<CPEdge> b = f.getBoundary();
				if (b.size() >= minBoundary) {
					HalfEdgeUtility.removeFace(f);
					i--;
				}
			}
		}
		
		return graph;
	}
	
	@Override
	public JPanel getOptionPanel() {
		if (optionPanel == null) {
			optionPanel = new OptionPanel();
		}
		return optionPanel;
	}
	
	
	private class OptionPanel extends JPanel implements ActionListener, ChangeListener{

		private static final long 
			serialVersionUID = 1L;
		private JPanel
			vertexPanel = new JPanel(),
			edgePanel = new JPanel(),
			facePanel = new JPanel();
		private JRadioButton
			vertexEditRadio = new JRadioButton("Vertex Remove", true),
			edgeEditRadio = new JRadioButton("Edge Remove"),
			faceFromEdtor = new JRadioButton("Face Remove");
		private SpinnerNumberModel
			valenceModel = new SpinnerNumberModel(minValence, 4, 10000, 1),
			facesSidesModel = new SpinnerNumberModel(minValence, 4, 10000, 1);
		private JSpinner
			valenceSpinner = new JSpinner(valenceModel),
			faceSideSpinner = new JSpinner(facesSidesModel);
		
		public OptionPanel() {
			setLayout(new GridBagLayout());
			GridBagConstraints c = new GridBagConstraints();
			c.gridwidth = GridBagConstraints.REMAINDER;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.weightx = 1.0;
			c.weighty = 1.0;
			c.insets = new Insets(0,2,2,2);
			
			add(vertexEditRadio, c);
			add(vertexPanel, c);
			add(new JSeparator(), c);
			add(edgeEditRadio, c);
			add(edgePanel, c);
			add(new JSeparator(), c);
			add(faceFromEdtor, c);
			add(facePanel, c);
			
			c.insets = new Insets(0,10,2,2);
			
			vertexPanel.setLayout(new GridBagLayout());
			c.anchor = WEST;
			c.gridwidth = 1;
			c.gridy = 1;
			vertexPanel.add(new JLabel("Minimal Valence"), c);
			c.gridy = 2;
			vertexPanel.add(valenceSpinner, c);
			
			edgePanel.setLayout(new GridBagLayout());
			c.gridwidth = REMAINDER;
			c.gridy = RELATIVE;
			
			facePanel.setLayout(new GridBagLayout());
			c.gridwidth = REMAINDER;
			c.gridy = 1;
			facePanel.add(new JLabel("Minimal Boundary"), c);
			c.gridy = 2;
			facePanel.add(faceSideSpinner, c);
			
			
			ButtonGroup modeGroup = new ButtonGroup();
			modeGroup.add(faceFromEdtor);
			modeGroup.add(edgeEditRadio);
			modeGroup.add(vertexEditRadio);
			
			faceFromEdtor.addActionListener(this);
			edgeEditRadio.addActionListener(this);
			vertexEditRadio.addActionListener(this);
			valenceSpinner.addChangeListener(this);
			faceSideSpinner.addChangeListener(this);
			updateStates();
		}

		private void updateStates(){
			vertexEdit = vertexEditRadio.isSelected();
			faceEdit = faceFromEdtor.isSelected();
			edgeEdit = edgeEditRadio.isSelected();
			vertexPanel.setEnabled(vertexEdit);
			facePanel.setEnabled(faceEdit);
			edgePanel.setEnabled(edgeEdit);
			minValence = valenceModel.getNumber().intValue();
			minBoundary = facesSidesModel.getNumber().intValue();
		}
		
		
		public void actionPerformed(ActionEvent e) {
			updateStates();
		}

		public void stateChanged(ChangeEvent e) {
			updateStates();
		}
		
	}
	
	
	
}
