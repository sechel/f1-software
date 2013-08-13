package minimalsurface.frontend.macro;

import static java.awt.GridBagConstraints.HORIZONTAL;
import static java.awt.GridBagConstraints.RELATIVE;
import static java.awt.GridBagConstraints.REMAINDER;
import halfedge.HalfEdgeDataStructure;
import halfedge.surfaceutilities.Ears;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import math.optimization.newton.NewtonOptimizer;
import math.optimization.newton.NewtonOptimizer.Solver;
import math.optimization.stepcontrol.ShortGradientStepController;
import math.util.VecmathTools;
import minimalsurface.util.MinimalSurfaceUtility;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Vector;
import util.debug.DBGTracer;
import circlepatterns.frontend.CPTestSuite;
import circlepatterns.frontend.content.spherical.ComputeSphericalCirclePattern.EdgeFlagType;
import circlepatterns.graph.CPEdge;
import circlepatterns.graph.CPFace;
import circlepatterns.graph.CPVertex;
import circlepatterns.layout.CPLayout;
import circlepatterns.math.CPSphericalFunctional.FlagFalse;
import circlepatterns.math.CPSphericalFunctional.FlagGuess;
import circlepatterns.math.CPSphericalFunctional.SpecialEdgeFlag;
import circlepatterns.math.CPSphericalOptimizable;

public class SphericalCirclePattern extends MacroAction {

	public static enum InitMode{
		INIT_ZERO,
		INIT_RANDOM,
		INIT_NONE,
		INIT_CONST
	}
	
	private InitMode
		initMode = InitMode.INIT_CONST;
	private Double
		initConst = -5.0,
		initRandomMax = -20.0,
		stepwidthControlAlpha = 0.5;
	private Integer
		errorExp = -5,
		maxIterations = 20;
	private Boolean
		useStepwidthControl = true;
	private Solver	
		solver = Solver.GMRES;
	private EdgeFlagType
		edgeFlagType = EdgeFlagType.FLAG_GUESS;
	
	private OptionPanel
		optionPanel = null;
	
	@Override
	public String getName() {
		return "Spherical Circle Pattern";
	}

	
	@Override
	public HalfEdgeDataStructure<CPVertex, CPEdge, CPFace> process(
			HalfEdgeDataStructure<CPVertex, CPEdge, CPFace> graph)
			throws Exception {
		
		SpecialEdgeFlag flag = null;
		switch (edgeFlagType){
		case FLAG_FALSE:
			flag = new FlagFalse();
			break;
		case FLAG_GUESS:
			flag = new FlagGuess();
			break;			
		}
		CPSphericalOptimizable<CPVertex, CPEdge, CPFace> func = new CPSphericalOptimizable<CPVertex, CPEdge, CPFace>(graph, flag);
		Vector initGuess = new DenseVector(func.getDomainDimension());
		switch (initMode){
			case INIT_RANDOM:
				Random rnd = new Random();
				for (int i = 0; i < initGuess.size(); i++)
					initGuess.set(i, rnd.nextDouble() * initRandomMax);
				break;
			case INIT_ZERO:
				break;
			case INIT_NONE:
				for (int i = 0; i < initGuess.size(); i++)
					initGuess.set(i, CPTestSuite.getTopology().getFace(i).getRho());
				break;
			case INIT_CONST:
				for (int i = 0; i < initGuess.size(); i++)
					initGuess.set(i, initConst);
				break;
		}
		
		DBGTracer.msg("optimizing...");
		Double error = Math.pow(10, errorExp);
		ShortGradientStepController stepController = null;
		if (useStepwidthControl){
			stepController = new ShortGradientStepController();
			stepController.setAlpha(stepwidthControlAlpha);
		}
		
		NewtonOptimizer optimizer = new NewtonOptimizer();
		optimizer.setSolver(solver);
		optimizer.setError(error);
		optimizer.setMaxIterations(maxIterations);
		if (stepController != null){
			optimizer.setStepController(stepController);
		}
		try {
			optimizer.minimize(initGuess, func);
		} catch (Exception e1) {
			CPTestSuite.showError(e1.toString());
			return null;
		}
		
		CPLayout.calculateSpherical(graph);

		for (CPFace f : graph.getFaces())
			VecmathTools.sphereMirror(f.getXYZW());
		
		
		List<CPEdge> ears = Ears.findEarsEdge(graph);
		for (CPEdge e : ears)
			e.getTargetVertex().setXYZW(e.getRightFace().getXYZW());
		
		MinimalSurfaceUtility.createFaceLabels(graph, false);
		
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
		private SpinnerNumberModel
			errorSpinnerModel = new SpinnerNumberModel(errorExp.intValue(), -20, -1, -1),
			maxIterationsSpinnerModel = new SpinnerNumberModel(maxIterations.intValue(), 1E0, 1E3, 1E0),
			maxRandomInitModel = new SpinnerNumberModel(initRandomMax.doubleValue(), -20, 0.0, 1E-2),
			alphaStepModel = new SpinnerNumberModel(stepwidthControlAlpha.doubleValue(), 0, 1, 0.001),
			constInitModel = new SpinnerNumberModel(initConst.doubleValue(), -20.0, 0.0, 0.001);
		private JSpinner
			errorSpinner = new JSpinner(errorSpinnerModel),
			maxIterationSpinner = new JSpinner(maxIterationsSpinnerModel),
			maxRandomInitSpinner = new JSpinner(maxRandomInitModel),
			alphaStepSpinner = new JSpinner(alphaStepModel),
			constInitSpinner = new JSpinner(constInitModel);
		private JPanel
			initialValuePanel = new JPanel(),
			stepwidthControlPanel = new JPanel();
		private JRadioButton
			randomInitChecker = new JRadioButton("Randomize"),
			zeroInitChecker = new JRadioButton("Zero"),
			noInitChecker = new JRadioButton("No Init"),
			constInitChecker = new JRadioButton("Const Init");
		private JCheckBox
			useStepControlChecker = new JCheckBox("Use Step Control", useStepwidthControl);
		private JComboBox
			solverComboBox = new JComboBox(Solver.values()),
			edgeFlagCombo = new JComboBox(EdgeFlagType.values());
		
		
		public OptionPanel() {
			buildLayout();
		}
		
		private void buildLayout(){
			setLayout(new GridBagLayout());
			GridBagConstraints c = new GridBagConstraints();
			c.fill = HORIZONTAL;
			c.weightx = 1;
			c.insets = new Insets(2, 0, 2, 0);
			
			c.gridwidth = RELATIVE;
			add(new JLabel("Error Exponent"), c);
			c.gridwidth = REMAINDER;
			add(errorSpinner, c);
			
			c.gridwidth = RELATIVE;
			add(new JLabel("Max Iterations"), c);
			c.gridwidth = REMAINDER;
			add(maxIterationSpinner, c);
			
			add(initialValuePanel, c);
			
			c.gridwidth = RELATIVE;
			add(new JLabel("Edge Flags"), c);
			c.gridwidth = REMAINDER;
			add(edgeFlagCombo, c);		
			
			c.gridwidth = RELATIVE;
			add(new JLabel("Solver"), c);
			c.gridwidth = REMAINDER;
			add(solverComboBox, c);
			
			add(stepwidthControlPanel, c);
			
			stepwidthControlPanel.setBorder(BorderFactory.createTitledBorder("Step Control"));
			stepwidthControlPanel.setLayout(new GridBagLayout());
			c.gridwidth = REMAINDER;
			stepwidthControlPanel.add(useStepControlChecker, c);
			c.gridwidth = RELATIVE;
			stepwidthControlPanel.add(new JLabel("Alpha"), c);
			c.gridwidth = REMAINDER;
			stepwidthControlPanel.add(alphaStepSpinner, c);
			
			initialValuePanel.setBorder(BorderFactory.createTitledBorder("Initial Values"));
			initialValuePanel.setLayout(new GridBagLayout());
			c.gridwidth = REMAINDER;
			initialValuePanel.add(noInitChecker, c);
			initialValuePanel.add(zeroInitChecker, c);
			c.gridwidth = RELATIVE;
			initialValuePanel.add(randomInitChecker, c);
			c.gridwidth = REMAINDER;
			initialValuePanel.add(maxRandomInitSpinner, c);
			c.gridwidth = RELATIVE;
			initialValuePanel.add(constInitChecker, c);
			c.gridwidth = REMAINDER;
			initialValuePanel.add(constInitSpinner, c);
			
			alphaStepSpinner.addChangeListener(this);
			maxRandomInitSpinner.addChangeListener(this);
			randomInitChecker.addActionListener(this);
			errorSpinner.addChangeListener(this);
			maxIterationSpinner.addChangeListener(this);
			randomInitChecker.addActionListener(this);
			zeroInitChecker.addActionListener(this);
			noInitChecker.addActionListener(this);
			useStepControlChecker.addActionListener(this);
			constInitChecker.addActionListener(this);
			constInitSpinner.addChangeListener(this);
			solverComboBox.addActionListener(this);
			edgeFlagCombo.addActionListener(this);
			
			ButtonGroup initButtonGroup = new ButtonGroup();
			initButtonGroup.add(randomInitChecker);
			initButtonGroup.add(zeroInitChecker);
			initButtonGroup.add(noInitChecker);
			initButtonGroup.add(constInitChecker);
			
			updateStates();
		}
	
		public void stateChanged(ChangeEvent e) {
			if (e.getSource() == errorSpinner)
				errorExp = errorSpinnerModel.getNumber().intValue();
			if (e.getSource() == maxIterationSpinner)
				maxIterations = maxIterationsSpinnerModel.getNumber().intValue();
			if (e.getSource() == maxRandomInitSpinner)
				initRandomMax = maxRandomInitModel.getNumber().doubleValue();
			if (e.getSource() == alphaStepSpinner)
				stepwidthControlAlpha = alphaStepModel.getNumber().doubleValue();
			if (e.getSource() == constInitSpinner)
				initConst = constInitModel.getNumber().doubleValue();
		}
	
		
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == randomInitChecker)
				initMode = InitMode.INIT_RANDOM;
			if (e.getSource() == zeroInitChecker)
				initMode = InitMode.INIT_ZERO;
			if (e.getSource() == noInitChecker)
				initMode = InitMode.INIT_NONE;
			if (e.getSource() == constInitChecker)
				initMode = InitMode.INIT_CONST;
			if (e.getSource() == useStepControlChecker)
				useStepwidthControl = useStepControlChecker.isSelected();
			if (e.getSource() == solverComboBox)
				solver = (Solver)solverComboBox.getSelectedItem();
			if (e.getSource() == edgeFlagCombo)
				edgeFlagType = (EdgeFlagType)edgeFlagCombo.getSelectedItem();
			updateStates();
		}
		
		private void updateStates(){
			maxRandomInitSpinner.setEnabled(false);
			constInitSpinner.setEnabled(false);
			switch (initMode){
				case INIT_NONE:
					noInitChecker.setSelected(true);
					break;
				case INIT_RANDOM:
					randomInitChecker.setSelected(true);
					maxRandomInitSpinner.setEnabled(true);
					break;
				case INIT_ZERO:
					zeroInitChecker.setSelected(true);
					break;
				case INIT_CONST:
					constInitChecker.setSelected(true);
					constInitSpinner.setEnabled(true);
					break;
			}
			maxRandomInitSpinner.setEnabled(randomInitChecker.isSelected());
			alphaStepSpinner.setEnabled(useStepControlChecker.isSelected());
			solverComboBox.setSelectedItem(solver);
			edgeFlagCombo.setSelectedItem(edgeFlagType);
		}


		
	}
	
	

}
