package circlepatterns.frontend.content.spherical;

import halfedge.HalfEdgeUtility;
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
import math.optimization.stepcontrol.ShortGradientStepController;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Vector;
import util.debug.DBGTracer;
import circlepatterns.frontend.CPTestSuite;
import circlepatterns.graph.CPEdge;
import circlepatterns.graph.CPFace;
import circlepatterns.graph.CPVertex;
import circlepatterns.layout.CPLayout;
import circlepatterns.math.CPSphericalOptimizable;
import circlepatterns.math.CPSphericalFunctional.FlagFalse;
import circlepatterns.math.CPSphericalFunctional.FlagGuess;
import circlepatterns.math.CPSphericalFunctional.SpecialEdgeFlag;


/**
 * Computes the circle pattern from the active topology
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 */
@SuppressWarnings("serial")
public class ComputeSphericalCirclePattern extends AbstractAction {

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
		initMode = InitMode.INIT_CONST;
	private Double
		maxRandomValue = -0.1,
		constInitValue = -2.0;
	private Boolean 
		useStepwithControl = true;
	private Double
		stepwidthControlAlpha = 0.5;
	private boolean
		guessThetas = false;
	private Solver
		solver = Solver.GMRES;
	private EdgeFlagType 
		edgeFlag = EdgeFlagType.FLAG_GUESS; 

	public static enum InitMode{
		INIT_ZERO,
		INIT_RANDOM,
		INIT_NONE,
		INIT_CONST
	}
	

	public static enum EdgeFlagType{
		FLAG_FALSE,
		FLAG_GUESS
	}
	
	
	public ComputeSphericalCirclePattern() {
		putValue(Action.NAME, "Compute Circle Pattern");
		putValue(Action.SMALL_ICON, new ImageIcon(ImageHook.getImage("process.gif")));
		putValue(Action.MNEMONIC_KEY, KeyEvent.SHIFT_DOWN_MASK | KeyEvent.VK_C);
	}
	
	
	public void actionPerformed(ActionEvent e) {
		if (CPTestSuite.getTopology() == null){
			JOptionPane.showMessageDialog(CPTestSuite.getMainFrame(), "No topology loaded!");
			return;
		}
		SpecialEdgeFlag flag = null;
		switch (edgeFlag){
		case FLAG_FALSE:
			flag = new FlagFalse();
			break;
		case FLAG_GUESS:
			flag = new FlagGuess();
			break;			
		}
		CPSphericalOptimizable<CPVertex, CPEdge, CPFace> func = new CPSphericalOptimizable<CPVertex, CPEdge, CPFace>(CPTestSuite.getTopology(), flag);
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
					initGuess.set(i, CPTestSuite.getTopology().getFace(i).getRho());
				break;
			case INIT_CONST:
				for (int i = 0; i < initGuess.size(); i++)
					initGuess.set(i, constInitValue);
				break;
		}
		
		DBGTracer.msg("optimizing...");
		Double error = Math.pow(10, errorExp);
		ShortGradientStepController stepController = null;
		if (useStepwithControl){
			stepController = new ShortGradientStepController();
			stepController.setAlpha(stepwidthControlAlpha);
		}
		
		if (guessThetas){
			for (CPEdge h : CPTestSuite.getTopology().getEdges()) {
				int ds = HalfEdgeUtility.getDegree(h.getStartVertex());
				int dt = HalfEdgeUtility.getDegree(h.getTargetVertex());
				if (dt != ds) {
					CPTestSuite.showError("Could not guess thetas! Varying vertex degree");
					return;
				}
				h.setTheta(2 * Math.PI / ds);
			}
		}
		
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
		
		CPLayout.calculateSpherical(CPTestSuite.getTopology());
		CPTestSuite.updateSpherical();
		
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


	public boolean isGuessThetas() {
		return guessThetas;
	}


	public void setGuessThetas(boolean guessThetas) {
		this.guessThetas = guessThetas;
	}


	public Double getConstInitValue() {
		return constInitValue;
	}


	public void setConstInitValue(Double constInitValue) {
		this.constInitValue = constInitValue;
	}


	public Solver getSolver() {
		return solver;
	}


	public void setSolver(Solver solver) {
		this.solver = solver;
	}


	public EdgeFlagType getEdgeFlag() {
		return edgeFlag;
	}


	public void setEdgeFlag(EdgeFlagType edgeFlag) {
		this.edgeFlag = edgeFlag;
	}

	
}
