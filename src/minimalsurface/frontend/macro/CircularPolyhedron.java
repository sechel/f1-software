package minimalsurface.frontend.macro;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import halfedge.HalfEdgeDataStructure;
import image.ImageHook;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import koebe.KoebePolyhedron;
import circlepatterns.graph.CPEdge;
import circlepatterns.graph.CPFace;
import circlepatterns.graph.CPVertex;

public class CircularPolyhedron extends MacroAction {

	protected Icon 
		icon = new ImageIcon(ImageHook.getImage("koebe.png"));
	private SpinnerNumberModel
		toleranceExpModel = new SpinnerNumberModel(-9, -30, 0, 1),
		maxIterationsModel = new SpinnerNumberModel(200, 1, 10000, 1);
	private JSpinner
		toleracneSpinner = new JSpinner(toleranceExpModel),
		maxIterationsSpinner = new JSpinner(maxIterationsModel);
	private JLabel
		toleranceLabel = new JLabel("Tolerance Exp"),
		maxIterationsLabel = new JLabel("Max Iterations");
	
	public CircularPolyhedron() {
		optionPanel = new JPanel();
		optionPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1.0;
		c.insets = new Insets(2, 2, 2, 2);
		c.gridwidth = GridBagConstraints.RELATIVE;
		optionPanel.add(toleranceLabel, c);
		c.gridwidth = GridBagConstraints.REMAINDER;
		optionPanel.add(toleracneSpinner, c);
		c.gridwidth = GridBagConstraints.RELATIVE;
		optionPanel.add(maxIterationsLabel, c);
		c.gridwidth = GridBagConstraints.REMAINDER;
		optionPanel.add(maxIterationsSpinner, c);
	}
	
	@Override
	public String getName() {
		return "Create Circular Polyhedron";
	}

	@Override
	public HalfEdgeDataStructure<CPVertex, CPEdge, CPFace> process(
		HalfEdgeDataStructure<CPVertex, CPEdge, CPFace> graph
	) throws Exception {
		double tolerance = Math.pow(10, toleranceExpModel.getNumber().doubleValue());
		int maxIterations = maxIterationsModel.getNumber().intValue();
		KoebePolyhedron.calculateCirclePattern(graph, tolerance, maxIterations);
		return graph;
	}
	
	
	@Override
	public Icon getIcon() {
		return icon;
	}

}
