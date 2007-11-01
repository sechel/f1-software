package math.optimization.stepcontrol;

import math.optimization.Optimizable;
import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.Vector;


/**
 * A step controller wich does nothing
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 */
public class VoidStepController implements StepController {

	public Double step(Vector x, Double value, Vector dx, Optimizable func, Vector grad, Matrix hess) {
		x.add(dx);
		return func.evaluate(x, grad, hess);
	}

}
