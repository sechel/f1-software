package math.optimization.stepcontrol;

import math.optimization.Optimizable;
import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.Vector;

/**
 * Step controller interface for the NewtonOptimizer
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 * @see math.optimization.newton.NewtonOptimizer
 */
public interface StepController {

	public Double step(Vector x, Double value, Vector dx, Optimizable func, Vector grad, Matrix hess);
	
}
