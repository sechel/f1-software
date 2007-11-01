package halfedge.frontend.graphtool;


/**
 * Editor exception
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 */
@SuppressWarnings("serial")
public class EditOperationException extends Exception {

	public EditOperationException() {
	
	}
	
	public EditOperationException(String message) {
		super(message);
	}
	
	
}
