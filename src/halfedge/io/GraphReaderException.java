package halfedge.io;


/**
 * Graph parsing exception
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 */
public class GraphReaderException extends Exception {

	private static final long 
		serialVersionUID = 1L;

	
	public GraphReaderException(String message){
		super(message);
	}
	
	
	public GraphReaderException(Exception e){
		super(e.getMessage(), e.getCause());
	}
	
}
