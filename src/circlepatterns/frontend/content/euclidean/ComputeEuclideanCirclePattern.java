package circlepatterns.frontend.content.euclidean;

import image.ImageHook;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Random;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import math.optimization.IterationMonitor;
import math.optimization.newton.NewtonOptimizer;
import math.optimization.newton.NewtonOptimizer.Solver;
import math.optimization.stepcontrol.ArmijoStepController;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Vector;
import util.debug.DBGTracer;
import circlepatterns.frontend.CPTestSuite;
import circlepatterns.graph.CPEdge;
import circlepatterns.graph.CPFace;
import circlepatterns.graph.CPVertex;
import circlepatterns.layout.CPLayout;
import circlepatterns.math.CPEuclideanOptimizable;


/**
 * Computes the circle pattern from the active topology
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 */
@SuppressWarnings("serial")
public class ComputeEuclideanCirclePattern extends AbstractAction {

	private Integer 
		errorExp = -4;
	private Integer
		maxIterations = 20,
		iterations = 0;
	private Float
		seconds = 0.0f;
	private IterationMonitor
		monitor = null;
	private InitMode
		initMode = InitMode.INIT_NONE;
	private Double
		maxRandomValue = Math.PI;
	private Boolean 
		useStepwithControl = false;
	private Double
		stepwidthControlAlpha = 0.2,
		stepwidthControlBeta = 0.5;

	public static enum InitMode{
		INIT_ZERO,
		INIT_RANDOM,
		INIT_NONE;
	}
	

	public ComputeEuclideanCirclePattern() {
		putValue(Action.NAME, "Compute Circle Pattern");
		putValue(Action.SMALL_ICON, new ImageIcon(ImageHook.getImage("process.gif")));
		putValue(Action.MNEMONIC_KEY, KeyEvent.VK_C);
	}
	
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if (CPTestSuite.getTopology() == null){
			JOptionPane.showMessageDialog(CPTestSuite.getMainFrame(), "No topology loaded!");
			return;
		}
		CPEuclideanOptimizable<CPVertex, CPEdge, CPFace> func = new CPEuclideanOptimizable<CPVertex, CPEdge, CPFace>(CPTestSuite.getTopology());
		Vector initGuess = new DenseVector(func.getDomainDimension());
		switch (initMode){
			case INIT_RANDOM:
				Random rnd = new Random();
				for (int i = 0; i < initGuess.size(); i++)
					initGuess.set(i, rnd.nextDouble() * maxRandomValue);
				break;
			case INIT_ZERO:
				break;
			case INIT_NONE:
				for (int i = 0; i < initGuess.size(); i++)
					initGuess.set(i, CPTestSuite.getTopology().getFace(i + 1).getRho());
		}
		
		DBGTracer.msg("optimizing...");
		Double error = Math.pow(10, errorExp);
		ArmijoStepController stepController = null;
		if (useStepwithControl){
			stepController = new ArmijoStepController();
			stepController.setAlpha(stepwidthControlAlpha);
			stepController.setBeta(stepwidthControlBeta);
		}
		
		Solver solver = Solver.CG;
		NewtonOptimizer optimizer = new NewtonOptimizer();
		optimizer.setSolver(solver);
		optimizer.setIterationMonitor(monitor);
		optimizer.setError(error);
		optimizer.setMaxIterations(maxIterations);
		if (stepController != null){
			optimizer.setStepController(stepController);
		}
		try {
			optimizer.minimize(initGuess, func);
		} catch (Exception e1) {
			CPTestSuite.showError(e1.toString());
			return;
		}
		CPLayout.calculateEuclidean(CPTestSuite.getTopology());
		CPTestSuite.updateEuclidean();
		
		DBGTracer.msg("done.");
	}

	
	
	public Integer getErrorExponent() {
		return errorExp;
	}


	public void setErrorExponent(Integer errorExp) {
		this.errorExp = errorExp;
	}


	public Integer getMaxIterations() {
		return maxIterations;
	}


	public void setMaxIterations(Integer maxIterations) {
		this.maxIterations = maxIterations;
	}
	


	public Integer getIterations() {
		return iterations;
	}


	
	public void setIterations(Integer iterations) {
		this.iterations = iterations;
	}


	
	public Float getSeconds() {
		return seconds;
	}


	
	public void setSeconds(Float seconds) {
		this.seconds = seconds;
	}
	
	public IterationMonitor getIterationMonitor() {
		return monitor;
	}

	public void setIterationMonitor(IterationMonitor monitor) {
		this.monitor = monitor;
	}


	public InitMode getInitMode() {
		return initMode;
	}


	public void setInitMode(InitMode initMode) {
		this.initMode = initMode;
	}


	public Double getMaxRandomValue() {
		return maxRandomValue;
	}


	public void setMaxRandomValue(Double maxRandomValue) {
		this.maxRandomValue = maxRandomValue;
	}


	public Boolean getUseStepwithControl() {
		return useStepwithControl;
	}


	public void setUseStepwithControl(Boolean useStepwithControl) {
		this.useStepwithControl = useStepwithControl;
	}


	public Double getStepwidthControlAlpha() {
		return stepwidthControlAlpha;
	}


	public void setStepwidthControlAlpha(Double stepwidthControlAlpha) {
		this.stepwidthControlAlpha = stepwidthControlAlpha;
	}


	public Double getStepwidthControlBeta() {
		return stepwidthControlBeta;
	}


	public void setStepwidthControlBeta(Double stepwidthControlBeta) {
		this.stepwidthControlBeta = stepwidthControlBeta;
	}

}
