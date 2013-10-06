package circlepatterns.frontend.content.spherical;

import static java.awt.GridBagConstraints.HORIZONTAL;
import static java.awt.GridBagConstraints.RELATIVE;
import static java.awt.GridBagConstraints.REMAINDER;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import math.optimization.IterationMonitor;
import math.optimization.newton.NewtonOptimizer.Solver;
import circlepatterns.frontend.content.ShrinkPanel;
import circlepatterns.frontend.content.spherical.ComputeSphericalCirclePattern.EdgeFlagType;


/**
 * The calculation controls panel
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 */
@SuppressWarnings("serial")
public class SphericalComputationShrinker extends ShrinkPanel implements ActionListener, ChangeListener, IterationMonitor{

	private ComputeSphericalCirclePattern
		computeSphericalAction = new ComputeSphericalCirclePattern();
	private JButton
		calculateSphericalButton = new JButton(computeSphericalAction);
	private SpinnerNumberModel
		errorSpinnerModel = new SpinnerNumberModel(computeSphericalAction.getErrorExponent().intValue(), -20, -1, -1),
		maxIterationsSpinnerModel = new SpinnerNumberModel(computeSphericalAction.getMaxIterations().intValue(), 1E0, 1E3, 1E0),
		maxRandomInitModel = new SpinnerNumberModel(computeSphericalAction.getMaxRandomValue().doubleValue(), -20, 0.0, 1E-2),
		alphaStepModel = new SpinnerNumberModel(computeSphericalAction.getStepwidthControlAlpha().doubleValue(), 0, 1, 0.001),
		constInitModel = new SpinnerNumberModel(computeSphericalAction.getConstInitValue().doubleValue(), -20.0, 0.0, 0.001);
	private JSpinner
		errorSpinner = new JSpinner(errorSpinnerModel),
		maxIterationSpinner = new JSpinner(maxIterationsSpinnerModel),
		maxRandomInitSpinner = new JSpinner(maxRandomInitModel),
		alphaStepSpinner = new JSpinner(alphaStepModel),
		constInitSpinner = new JSpinner(constInitModel);
	private JPanel
		initialValuePanel = new JPanel(),
		statusPanel = new JPanel(),
		stepwidthControlPanel = new JPanel();
	private JTextField
		iterationStatus = new JTextField("-"),
		timeStatus = new JTextField("-");
	private JRadioButton
		randomInitChecker = new JRadioButton("Randomize"),
		zeroInitChecker = new JRadioButton("Zero"),
		noInitChecker = new JRadioButton("No Init"),
		constInitChecker = new JRadioButton("Const Init");
	private JCheckBox
		useStepControlChecker = new JCheckBox("Use Step Control", computeSphericalAction.getUseStepwithControl()),
		guessThetasChecker = new JCheckBox("Guess Thetas", computeSphericalAction.isGuessThetas());
	private JComboBox
		solverComboBox = new JComboBox(Solver.values()),
		edgeFlagCombo = new JComboBox(EdgeFlagType.values());
	
	
	public SphericalComputationShrinker() {
		super("Spherical Computing");
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
		add(guessThetasChecker, c);
		
		c.gridwidth = RELATIVE;
		add(new JLabel("Edge Flags"), c);
		c.gridwidth = REMAINDER;
		add(edgeFlagCombo, c);		
		
		c.gridwidth = RELATIVE;
		add(new JLabel("Solver"), c);
		c.gridwidth = REMAINDER;
		add(solverComboBox, c);
		
		add(stepwidthControlPanel, c);
		add(calculateSphericalButton, c);
		add(statusPanel, c);
		
		stepwidthControlPanel.setBorder(BorderFactory.createTitledBorder("Step Control"));
		stepwidthControlPanel.setLayout(new GridBagLayout());
		c.gridwidth = REMAINDER;
		stepwidthControlPanel.add(useStepControlChecker, c);
		c.gridwidth = RELATIVE;
		stepwidthControlPanel.add(new JLabel("Alpha"), c);
		c.gridwidth = REMAINDER;
		stepwidthControlPanel.add(alphaStepSpinner, c);
		
		statusPanel.setBorder(BorderFactory.createTitledBorder("Status"));
		statusPanel.setLayout(new GridBagLayout());
		c.gridwidth = RELATIVE;
		statusPanel.add(new JLabel("Iterations"), c);
		c.gridwidth = REMAINDER;
		statusPanel.add(iterationStatus, c);
		c.gridwidth = RELATIVE;
		statusPanel.add(new JLabel("Elapsed Time"), c);
		c.gridwidth = REMAINDER;
		statusPanel.add(timeStatus, c);
		iterationStatus.setEditable(false);
		timeStatus.setEditable(false);

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
		guessThetasChecker.addActionListener(this);
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

	@Override
	public void stateChanged(ChangeEvent e) {
		if (e.getSource() == errorSpinner)
			computeSphericalAction.setErrorExponent(errorSpinnerModel.getNumber().intValue());
		if (e.getSource() == maxIterationSpinner)
			computeSphericalAction.setMaxIterations(maxIterationsSpinnerModel.getNumber().intValue());
		if (e.getSource() == maxRandomInitSpinner)
			computeSphericalAction.setMaxRandomValue(maxRandomInitModel.getNumber().doubleValue());
		if (e.getSource() == alphaStepSpinner)
			computeSphericalAction.setStepwidthControlAlpha(alphaStepModel.getNumber().doubleValue());
		if (e.getSource() == constInitSpinner)
			computeSphericalAction.setConstInitValue(constInitModel.getNumber().doubleValue());
	}

	
	public void setProgress(Integer percent) {
		
	}

	@Override
	public void setIteration(Integer iteration, Double error) {
		iterationStatus.setText(iteration.toString());
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == randomInitChecker)
			computeSphericalAction.setInitMode(ComputeSphericalCirclePattern.InitMode.INIT_RANDOM);
		if (e.getSource() == zeroInitChecker)
			computeSphericalAction.setInitMode(ComputeSphericalCirclePattern.InitMode.INIT_ZERO);
		if (e.getSource() == noInitChecker)
			computeSphericalAction.setInitMode(ComputeSphericalCirclePattern.InitMode.INIT_NONE);
		if (e.getSource() == constInitChecker)
			computeSphericalAction.setInitMode(ComputeSphericalCirclePattern.InitMode.INIT_CONST);
		if (e.getSource() == useStepControlChecker)
			computeSphericalAction.setUseStepwithControl(useStepControlChecker.isSelected());
		if (e.getSource() == guessThetasChecker)
			computeSphericalAction.setGuessThetas(guessThetasChecker.isSelected());
		if (e.getSource() == solverComboBox)
			computeSphericalAction.setSolver((Solver)solverComboBox.getSelectedItem());
		if (e.getSource() == edgeFlagCombo)
			computeSphericalAction.setEdgeFlag((EdgeFlagType)edgeFlagCombo.getSelectedItem());
		updateStates();
	}
	
	private void updateStates(){
		maxRandomInitSpinner.setEnabled(false);
		constInitSpinner.setEnabled(false);
		switch (computeSphericalAction.getInitMode()){
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
		solverComboBox.setSelectedItem(computeSphericalAction.getSolver());
		edgeFlagCombo.setSelectedItem(computeSphericalAction.getEdgeFlag());
	}


	@Override
	public void done(Double error) {
		
	}
	
	@Override
	public void start(Double error){
		
	}
	
}
