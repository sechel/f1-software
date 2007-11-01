package halfedge.io;

import halfedge.Edge;
import halfedge.Face;
import halfedge.HalfEdgeDataStructure;
import halfedge.Vertex;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;


/**
 * A reader class for the HEDS(Half Edge Data Serialized) file format.
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 */
public class HESerializableReader 
<
	V extends Vertex<V, E, F>,
	E extends Edge<V, E, F>, 
	F extends Face<V, E, F>
> extends ObjectInputStream {

	
	public HESerializableReader(InputStream in) throws IOException {
		super(in);
	}

	@SuppressWarnings("unchecked")
	public HalfEdgeDataStructure<V, E, F> readHalfEdgeDataStructure() throws IOException, ClassNotFoundException{
		HalfEdgeDataStructure<V, E, F> result = null;
		try{
			result = (HalfEdgeDataStructure<V, E, F>) readObject();
		} catch (ClassCastException e){
			throw new ClassNotFoundException("Wrong Halfedge member class for reading " + this);
		}
		// update connections for legacy data files
		for (Edge<V, E, F> e : result.getEdges()) {
			e.setTargetVertex(e.getTargetVertex());
			e.setLeftFace(e.getLeftFace());
		}
		return result;
	}

}
