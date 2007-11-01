package alexandrov.io;

import halfedge.Edge;
import halfedge.Face;
import halfedge.HalfEdgeDataStructure;
import halfedge.Vertex;
import halfedge.decorations.HasLength;
import halfedge.decorations.IsHidable;
import halfedge.surfaceutilities.SurfaceUtility;
import halfedge.triangulationutilities.ConsistencyCheck;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;


/**
 * A writer for the CPML file format. It is an XML format describing 
 * a triangulation with edge lengths.
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 * @see alexandrov.io.CPMLReader
 */
public class CPMLWriter {
	
	public static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F> & HasLength, 
		F extends Face<V, E, F> 
	> void writeCPML(File file, HalfEdgeDataStructure<V, E, F> graph) throws Exception{
		if (!ConsistencyCheck.isTriangulation(graph))
			throw new Exception("No Triangulation!");
		BufferedWriter writer = new BufferedWriter(new FileWriter(file));
		writer.write(convertToCPML(graph));
		writer.close();
	}
	
	
	
	public static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F> & HasLength, 
		F extends Face<V, E, F> 
	> String convertToCPML(HalfEdgeDataStructure<V, E, F> graph) throws Exception{
		StringBuffer buffer = new StringBuffer();
		
		// writing header
		buffer.append("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n");
		buffer.append("<!DOCTYPE convexPolyhedralMetric>\n");
		buffer.append("<cpml description=\"exported by alexandrov polytop editor\">\n");
	
		// write edge list
		buffer.append("\t<edgelist>\n");
		HashMap<E, Integer> indexMap = new HashMap<E, Integer>();
		Integer index = 0;
		for (E e : graph.getPositiveEdges()){
			boolean hide = false;
			if (IsHidable.class.isAssignableFrom(e.getClass())){
				hide = ((IsHidable)e).isHidden();
			}
			if (!hide){
				buffer.append("\t\t<edge length=\"" + e.getLength() + "\"/>\n");
			} else {
				buffer.append("\t\t<edge length=\"" + e.getLength() + "\" index=\"" + index + "\">\n");
				buffer.append("\t\t\t<property name=\"hidden\" type=\"boolean\" value=\"true\"/>\n");
				buffer.append("\t\t</edge>\n");
			}
			indexMap.put(e, index);
			indexMap.put(e.getOppositeEdge(), index);
			index++;
		}
		buffer.append("\t</edgelist>\n");
		
		// fill all holes
		SurfaceUtility.fillHoles(graph);
		
		// write triangle list
		buffer.append("\t<trianglelist>\n");
		for (F f : graph.getFaces()){
			E a = f.getBoundaryEdge();
			E b = a.getNextEdge();
			E c = b.getNextEdge();
			buffer.append("\t\t<triangle a=\"" + indexMap.get(a) + "\" b=\"" + indexMap.get(b) + "\" c=\"" + indexMap.get(c) + "\"/>\n");
		}
		
		buffer.append("\t</trianglelist>\n");
		buffer.append("</cpml>\n");
		return buffer.toString();
	}
	
	
}
