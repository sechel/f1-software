package halfedge.decorations;


/**
 * Implementers will have the setGradientValue and getGradientValue methods 
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 */
public interface HasGradientValue {

	public void setGradientValue(Double gValue);
	
	public Double getGradientValue();
	
}
