package minimalsurface.frontend.macro;

import static java.awt.GridBagConstraints.HORIZONTAL;
import static java.awt.GridBagConstraints.RELATIVE;
import static java.awt.GridBagConstraints.REMAINDER;
import halfedge.HalfEdgeDataStructure;
import halfedge.generator.SquareGridGenerator;
import halfedge.surfaceutilities.Subdivision;
import image.ImageHook;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import koebe.KoebePolyhedron;
import minimalsurface.util.GraphUtility;
import circlepatterns.graph.CPEdge;
import circlepatterns.graph.CPFace;
import circlepatterns.graph.CPVertex;
import circlepatterns.layout.CPLayout;

public class CatenoidCirclesGenerator extends MacroAction {

	protected Icon 
		icon = new ImageIcon(ImageHook.getImage("enneper.png"));
	protected OptionPanel
		optionPanel = null;
	private int
		uLines = 20,
		vLines = 20;
	private double 
		scale = 1.5;
	private boolean
		useMedial = true;
	
	@Override
	public String getName() {
		return "Catenoid Circle Generator";
	}

	@Override
	public HalfEdgeDataStructure<CPVertex, CPEdge, CPFace> process(HalfEdgeDataStructure<CPVertex, CPEdge, CPFace> in) throws Exception {
		HalfEdgeDataStructure<CPVertex, CPEdge, CPFace> graph = SquareGridGenerator.generate(uLines, vLines, CPVertex.class, CPEdge.class, CPFace.class);

		if (useMedial){
			graph = Subdivision.createMedialGraph(graph);
		}
		
		for (CPFace f : graph.getFaces()){
			f.setRho(0.0);
			f.setRadius(10.0);
		}
		CPLayout.calculateEuclidean(graph);
		KoebePolyhedron.normalizeBeforeProjection(graph, scale);
		
		KoebePolyhedron.inverseStereographicProjection(graph, 1.0);
		for (CPFace f : graph.getFaces())
			KoebePolyhedron.calculateConePeek(f.getXYZW(), f.getBoundaryEdge());
		
		GraphUtility.twoColoring(graph);
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

	
	private class OptionPanel extends JPanel implements ChangeListener, ActionListener{

		private static final long serialVersionUID = 1L;
		
		private SpinnerNumberModel
			uSpinnerModel = new SpinnerNumberModel(uLines, 1, 1000, 1),
			vSpinnerModel = new SpinnerNumberModel(vLines, 1, 1000, 1),
			scaleSpinnerModel = new SpinnerNumberModel(scale, 0.01, 1000.0, 0.01);
		private JSpinner
			uSpinner = new JSpinner(uSpinnerModel),
			vSpinner = new JSpinner(vSpinnerModel),
			scaleSpinner = new JSpinner(scaleSpinnerModel);
		private JCheckBox
			useMedialChecker = new JCheckBox("Use Medial Combinatorics", useMedial);
		private GridBagConstraints 
			c = new GridBagConstraints();
		
		public OptionPanel() {
			setLayout(new GridBagLayout());
			c.fill = HORIZONTAL;
			c.weightx = 1.0;
			c.insets = new Insets(2,2,2,2);
			
			c.gridwidth = REMAINDER;
			add(useMedialChecker, c);
			c.gridwidth = RELATIVE;
			add(new JLabel("U Lines"), c);
			c.gridwidth = REMAINDER;
			add(uSpinner, c);
			c.gridwidth = RELATIVE;
			add(new JLabel("V Lines"), c);
			c.gridwidth = REMAINDER;
			add(vSpinner, c);
			c.gridwidth = RELATIVE;
			add(new JLabel("Scale"), c);
			c.gridwidth = REMAINDER;
			add(scaleSpinner, c);	
			
			uSpinner.addChangeListener(this);
			vSpinner.addChangeListener(this);
			scaleSpinner.addChangeListener(this);
			useMedialChecker.addActionListener(this);
		}

		@Override
		public void stateChanged(ChangeEvent e) {
			uLines = uSpinnerModel.getNumber().intValue();
			vLines = vSpinnerModel.getNumber().intValue();
			scale = scaleSpinnerModel.getNumber().doubleValue();
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			useMedial = useMedialChecker.isSelected();			
		}
		
	}
	
	
}
