package math.optimization.newton;

import math.optimization.IterationMonitor;
import math.optimization.NotConvergentException;
import math.optimization.Optimizable;
import math.optimization.Optimizer;
import math.optimization.stepcontrol.StepController;
import math.optimization.stepcontrol.VoidStepController;
import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.NotConvergedException;
import no.uib.cipr.matrix.SVD;
import no.uib.cipr.matrix.Vector;
import no.uib.cipr.matrix.Vector.Norm;
import no.uib.cipr.matrix.sparse.AbstractIterativeSolver;
import no.uib.cipr.matrix.sparse.BiCGstab;
import no.uib.cipr.matrix.sparse.CG;
import no.uib.cipr.matrix.sparse.CGS;
import no.uib.cipr.matrix.sparse.GMRES;
import no.uib.cipr.matrix.sparse.IterativeSolverNotConvergedException;
import no.uib.cipr.matrix.sparse.QMR;


/**
 * This class implements a Newton optimizer with stepwidth control
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 * @see <a href="http://www.stanford.edu/~boyd/cvxbook/">Stephen Boyd 
 * and Lieven Vandenberghe - Convex Optimization </a>
 */
public class NewtonOptimizer implements Optimizer{

	private IterationMonitor
		monitor = null;
	private StepController
		stepController = new VoidStepController();
	private Integer
		maxIterations = 20;
	private Double
		error = 1E-8;
	private Norm
		norm = Norm.Two;
	private Solver
		solver = Solver.Generic;
	
	public static enum Solver{
		Generic,
		CG,
		CGS,
		BiCGstab,
		GMRES,
		SVD,
		QMR
	}
	
	
	@Override
	public void minimize(Vector guess, Optimizable func) throws NotConvergentException{
		Integer iteration = 0;
		/*
		 * A Sparse Matrix here consumes over 60% of calculation time 
		 * in a methon called getIndex()
		 */
		Matrix hess = new DenseMatrix(func.getDomainDimension(), func.getDomainDimension());
		Vector grad = new DenseVector(func.getDomainDimension());
		Double value = func.evaluate(guess, grad, hess);
		
		while (grad.norm(norm) > error && iteration < maxIterations){
			Vector dx = new DenseVector(grad.size());
			Vector templateVector = new DenseVector(grad.size());
			
			if (solver == Solver.SVD) {
				SVD svd = null;
				try {
					svd = SVD.factorize(hess);
				} catch (NotConvergedException e) {
					throw new NotConvergentException("Error 1: Newton's step could not be computed!", grad.norm(norm));
				}
				Matrix V = svd.getVt().transpose();
				Matrix DInv = new DenseMatrix(hess.numRows(), hess.numColumns());
				for (int i = 0; i < svd.getS().length; i++) {
					DInv.set(i, i, 1 / svd.getS()[i]);
				}
				Matrix Ut = svd.getU().transpose(); 
				Matrix tmp = new DenseMatrix(hess.numRows(), hess.numColumns());
				Matrix AInv = new DenseMatrix(hess.numRows(), hess.numColumns());
				V.mult(DInv, tmp);
				tmp.mult(Ut, AInv);
				AInv.mult(grad, dx);
			} else {
				AbstractIterativeSolver S = null;
//				DiagonalPreconditioner preconditioner = new DiagonalPreconditioner(grad.size());
				switch (solver) {
					case CG:
						S = new CG(templateVector);
					break;
					case Generic:
					case CGS:
						S = new CGS(templateVector);
					break;	
					case BiCGstab:
						S = new BiCGstab(templateVector);
						break;
					case GMRES:
						S = new GMRES(templateVector);
						break;
					case QMR:
						S = new QMR(templateVector);
						break;
				}
//				S.setPreconditioner(preconditioner);
				try {
					S.solve(hess, grad, dx);
				} catch (IterativeSolverNotConvergedException e) {
					System.err.println("Hessian: " + hess);
					System.err.println("Gradient: " + grad);
					throw new NotConvergentException("Error 1: Newton's step could not be computed!", grad.norm(norm));
				}
			}
			
			dx.scale(-1);
			value = stepController.step(guess, value, dx, func, grad, hess);
			
			iteration++;
			if (monitor != null){
				monitor.setIteration(iteration, grad.norm(norm));
			}
		}
		if (error < grad.norm(norm) || new Double(grad.norm(norm)).isNaN())
			throw new NotConvergentException("Error 2: Optimization did not converge within the given error or maximum itarations!", grad.norm(norm));
	}
	
	

	@Override
	public void setIterationMonitor(IterationMonitor monitor) {
		this.monitor = monitor;
	}


	
	@Override
	public void setMaxIterations(Integer maxIterations) {
		this.maxIterations = maxIterations;
	}



	@Override
	public Integer getMaxIterations() {
		return maxIterations;
	}



	@Override
	public void setError(Double error) {
		this.error = error;
	}



	@Override
	public Double getError() {
		return error;
	}



	@Override
	public StepController getStepController() {
		return stepController;
	}



	@Override
	public void setStepController(StepController stepController) {
		this.stepController = stepController;
	}



	@Override
	public Norm getNorm() {
		return norm;
	}



	@Override
	public void setNorm(Norm norm) {
		this.norm = norm;
	}



	public void setSolver(Solver solver) {
		this.solver = solver;
	}
	
}
