package circlepatterns.frontend.content.euclidean;

import static circlepatterns.frontend.content.euclidean.ComputeEuclideanCirclePattern.InitMode.INIT_NONE;
import static circlepatterns.frontend.content.euclidean.ComputeEuclideanCirclePattern.InitMode.INIT_RANDOM;
import static circlepatterns.frontend.content.euclidean.ComputeEuclideanCirclePattern.InitMode.INIT_ZERO;
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
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import math.optimization.IterationMonitor;
import circlepatterns.frontend.content.ShrinkPanel;


/**
 * The calculation controls panel
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 */
@SuppressWarnings("serial")
public class EuclideanComputationShrinker extends ShrinkPanel implements ActionListener, ChangeListener, IterationMonitor{

	private ComputeEuclideanCirclePattern
		computeAction = new ComputeEuclideanCirclePattern();
	private JButton
		calculateEuclideanButton = new JButton(computeAction);
	private SpinnerNumberModel
		errorSpinnerModel = new SpinnerNumberModel(computeAction.getErrorExponent().intValue(), -20, -1, -1),
		maxIterationsSpinnerModel = new SpinnerNumberModel(computeAction.getMaxIterations().intValue(), 1E0, 1E3, 1E0),
		maxRandomInitModel = new SpinnerNumberModel(computeAction.getMaxRandomValue().doubleValue(), 0, 2 * Math.PI, 1E-2),
		alphaStepModel = new SpinnerNumberModel(computeAction.getStepwidthControlAlpha().doubleValue(), 0, 1, 0.001),
		betaStepModel = new SpinnerNumberModel(computeAction.getStepwidthControlBeta().doubleValue(), 0, 1, 0.001);
	private JSpinner
		errorSpinner = new JSpinner(errorSpinnerModel),
		maxIterationSpinner = new JSpinner(maxIterationsSpinnerModel),
		maxRandomInitSpinner = new JSpinner(maxRandomInitModel),
		alphaStepSpinner = new JSpinner(alphaStepModel),
		betaStepSpinner = new JSpinner(betaStepModel);
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
		noInitChecker = new JRadioButton("No Init");
	private JCheckBox
		useStepControlChecker = new JCheckBox("Use Step Control", computeAction.getUseStepwithControl());
	
	
	public EuclideanComputationShrinker() {
		super("Euclidean Computing");
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
		add(stepwidthControlPanel, c);
		add(calculateEuclideanButton, c);
		add(statusPanel, c);
		
		stepwidthControlPanel.setBorder(BorderFactory.createTitledBorder("Step Control"));
		stepwidthControlPanel.setLayout(new GridBagLayout());
		c.gridwidth = REMAINDER;
		stepwidthControlPanel.add(useStepControlChecker, c);
		c.gridwidth = RELATIVE;
		stepwidthControlPanel.add(new JLabel("Alpha"), c);
		c.gridwidth = REMAINDER;
		stepwidthControlPanel.add(alphaStepSpinner, c);
		c.gridwidth = RELATIVE;
		stepwidthControlPanel.add(new JLabel("Beta"), c);
		c.gridwidth = REMAINDER;
		stepwidthControlPanel.add(betaStepSpinner, c);
		
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
		
		alphaStepSpinner.addChangeListener(this);
		betaStepSpinner.addChangeListener(this);
		maxRandomInitSpinner.addChangeListener(this);
		randomInitChecker.addActionListener(this);
		errorSpinner.addChangeListener(this);
		maxIterationSpinner.addChangeListener(this);
		computeAction.setIterationMonitor(this);
		randomInitChecker.addActionListener(this);
		zeroInitChecker.addActionListener(this);
		noInitChecker.addActionListener(this);
		useStepControlChecker.addActionListener(this);
		
		ButtonGroup initButtonGroup = new ButtonGroup();
		initButtonGroup.add(randomInitChecker);
		initButtonGroup.add(zeroInitChecker);
		initButtonGroup.add(noInitChecker);
		
		updateStates();
	}

	public void stateChanged(ChangeEvent e) {
		if (e.getSource() == errorSpinner)
			computeAction.setErrorExponent(errorSpinnerModel.getNumber().intValue());
		if (e.getSource() == maxIterationSpinner)
			computeAction.setMaxIterations(maxIterationsSpinnerModel.getNumber().intValue());
		if (e.getSource() == maxRandomInitSpinner)
			computeAction.setMaxRandomValue(maxRandomInitModel.getNumber().doubleValue());
		if (e.getSource() == alphaStepSpinner)
			computeAction.setStepwidthControlAlpha(alphaStepModel.getNumber().doubleValue());
		if (e.getSource() == betaStepSpinner)
			computeAction.setStepwidthControlBeta(betaStepModel.getNumber().doubleValue());
	}

	
	public void setProgress(Integer percent) {
		
	}

	public void setIteration(Integer iteration, Double error) {
		iterationStatus.setText(iteration.toString());
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == randomInitChecker)
			computeAction.setInitMode(INIT_RANDOM);
		if (e.getSource() == zeroInitChecker)
			computeAction.setInitMode(INIT_ZERO);
		if (e.getSource() == noInitChecker)
			computeAction.setInitMode(INIT_NONE);
		if (e.getSource() == useStepControlChecker)
			computeAction.setUseStepwithControl(useStepControlChecker.isSelected());
		updateStates();
	}
	
	private void updateStates(){
		switch (computeAction.getInitMode()){
			case INIT_NONE:
				noInitChecker.setSelected(true);
				break;
			case INIT_RANDOM:
				randomInitChecker.setSelected(true);
				break;
			case INIT_ZERO:
				zeroInitChecker.setSelected(true);
				break;
		}
		maxRandomInitSpinner.setEnabled(randomInitChecker.isSelected());
		alphaStepSpinner.setEnabled(useStepControlChecker.isSelected());
		betaStepSpinner.setEnabled(useStepControlChecker.isSelected());
	}


	public void done(Double error) {
		
	}
	
	public void start(Double error){
		
	}
	
}
