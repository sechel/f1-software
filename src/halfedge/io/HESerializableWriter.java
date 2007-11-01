package halfedge.io;

import halfedge.HalfEdgeDataStructure;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;


/**
 * A writer class for the HEDS(Half Edge Data Serialized) file format.
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 */
public class HESerializableWriter extends ObjectOutputStream {

	public HESerializableWriter(OutputStream out) throws IOException {
		super(out);
	}

	public void writeHalfEdgeDataStructure(HalfEdgeDataStructure<?, ?, ?> heds) throws IOException{
		writeObject(heds);
	}
	
}
