package alexandrov.io;

import halfedge.Edge;
import halfedge.Face;
import halfedge.HalfEdgeDataStructure;
import halfedge.Node;
import halfedge.Vertex;
import halfedge.decorations.HasLength;
import halfedge.io.GraphReaderException;
import halfedge.triangulationutilities.ConsistencyCheck;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import util.debug.DBGTracer;
import alexandrov.graph.CPMEdge;
import alexandrov.graph.CPMFace;
import alexandrov.graph.CPMVertex;


/**
 * A reader for the CPML file format. It is an XML format describing 
 * a triangulation with edge lengths. 
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 * @see alexandrov.io.CPMLWriter
 */
public class CPMLReader
<
	VertexClass extends Vertex<VertexClass, EdgeClass, FaceClass>,
	EdgeClass extends Edge<VertexClass, EdgeClass, FaceClass> & HasLength, 
	FaceClass extends Face<VertexClass, EdgeClass, FaceClass> 
>{

	private DocumentBuilder
		documentBuilder = null;
	
	private Class<VertexClass>
		vClass = null;
	private Class<EdgeClass>
		eClass = null;	
	private Class<FaceClass>
		fClass = null;	
	
	private boolean 
		parseProperties = true;
	
	
	private CPMLReader(Class<VertexClass> vClass, Class<EdgeClass> eClass, Class<FaceClass> fClass) {
		this.vClass = vClass;
		this.eClass = eClass;
		this.fClass = fClass;
	}
	
	public static 
	<	
		VC extends Vertex<VC, EC, FC>, 
		EC extends Edge<VC, EC, FC> & HasLength, 
		FC extends Face<VC, EC, FC>
	> 
	CPMLReader<VC, EC, FC> createCPMLReader(Class<VC> vClass, Class<EC> eClass, Class<FC> fClass){
		return new CPMLReader<VC, EC, FC>(vClass, eClass, fClass);
	}
	
	
	public HalfEdgeDataStructure<VertexClass, EdgeClass, FaceClass> 
	readCPML(File input) throws GraphReaderException, FileNotFoundException, IOException {
		FileInputStream fis = new FileInputStream(input);
		return readCPML(fis);
	}
	
	
	public HalfEdgeDataStructure<VertexClass, EdgeClass, FaceClass> 
	readCPML(InputStream input) throws GraphReaderException, FileNotFoundException, IOException {
		HalfEdgeDataStructure<VertexClass, EdgeClass, FaceClass> halfedge = HalfEdgeDataStructure.createHEDS(vClass, eClass, fClass);
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		try {
			documentBuilder = dbf.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		Document doc = null;
		try {
			doc = documentBuilder.parse(input);
		} catch (Exception e) {
			throw new GraphReaderException(e);
		}
		
		NodeList metricList = doc.getElementsByTagName("cpml");
		if (metricList.getLength() == 0)
			throw new GraphReaderException("No <cpml> tag found!");
		Element firstMetric = (Element)metricList.item(0);

		
		/*
		 * reading edges
		 */
		NodeList edgeListList = firstMetric.getElementsByTagName("edgelist");
		if (edgeListList.getLength() == 0)
			throw new GraphReaderException("No <edgelist> tag found!");
		Element firstEdgeList = (Element)edgeListList.item(0);
		NodeList edgeList = firstEdgeList.getElementsByTagName("edge");
		if (edgeList.getLength() == 0)
			throw new GraphReaderException("Empty edge list found!");
		
		HashMap<Integer, EdgeClass> undirectedEdgeMap = new HashMap<Integer, EdgeClass>();
		int numEdges = edgeList.getLength();
		// construct edges
		for (int i = 0; i < numEdges; i++){
			EdgeClass e1 = halfedge.addNewEdge();
			EdgeClass e2 = halfedge.addNewEdge();
			VertexClass v1 = halfedge.addNewVertex();
			VertexClass v2 = halfedge.addNewVertex();
			e1.setTargetVertex(v1);
			e2.setTargetVertex(v2);
			undirectedEdgeMap.put(i, e1);
			e1.linkOppositeEdge(e2);
			Element edgeElement = (Element) edgeList.item(i);
			try {
				Double length = Double.valueOf(edgeElement.getAttribute("length"));
				e1.setLength(length);
				e2.setLength(length);
			} catch (NumberFormatException e){
				DBGTracer.msg("Invalid length at edge " + i);
			}
			if (parseProperties){
				NodeList propList = edgeElement.getElementsByTagName("property");
				for (int j = 0; j < propList.getLength(); j++){
					readProperty(e1, (Element)propList.item(j));
					readProperty(e2, (Element)propList.item(j));
				}
			}
		}

		
		/*
		 * reading triangles
		 */
		NodeList tiangleListList = firstMetric.getElementsByTagName("trianglelist");
		if (tiangleListList.getLength() == 0)
			throw new GraphReaderException("No <trianglelist> tag found!");
		Element firstTriangleList = (Element)tiangleListList.item(0);
		NodeList triangleList = firstTriangleList.getElementsByTagName("triangle");
		if (triangleList.getLength() == 0)
			throw new GraphReaderException("Empty triangle list found!");
		int numTriangles = triangleList.getLength();
		for (int i = 0; i < numTriangles; i++){
			try {
			Element triangleElement = (Element) triangleList.item(i); 
			FaceClass triangle = halfedge.addNewFace();
			Integer a = -1;
			Integer b = -1;
			Integer c = -1;
			try{
				a = Integer.parseInt(triangleElement.getAttribute("a"));
				b = Integer.parseInt(triangleElement.getAttribute("b"));
				c = Integer.parseInt(triangleElement.getAttribute("c"));
			} catch (NumberFormatException nfe){
				throw new GraphReaderException("Invalid attribute at triangle " + i);
			}
			EdgeClass aEdge = undirectedEdgeMap.get(a);
			EdgeClass bEdge = undirectedEdgeMap.get(b);
			EdgeClass cEdge = undirectedEdgeMap.get(c);
			
			if (aEdge == null || bEdge == null || cEdge == null) {
				throw new GraphReaderException("edge not found: " + a + ": " + aEdge + ", " + b + ": " + bEdge + ", " + c + ": " + cEdge);
			}
			
			if (aEdge.getLeftFace() != null)
				aEdge = aEdge.getOppositeEdge();
			if (bEdge.getLeftFace() != null)
				bEdge = bEdge.getOppositeEdge();
			if (cEdge.getLeftFace() != null)
				cEdge = cEdge.getOppositeEdge();
			if (aEdge.getLeftFace() != null)
				throw new GraphReaderException("Double link of edges at triangle " + i + " edge a");
			if (bEdge.getLeftFace() != null)
				throw new GraphReaderException("Double link of edges at triangle " + i + " edge b");
			if (cEdge.getLeftFace() != null)
				throw new GraphReaderException("Double link of edges at triangle " + i + " edge c");
			aEdge.linkNextEdge(bEdge);
			bEdge.linkNextEdge(cEdge);
			cEdge.linkNextEdge(aEdge);
			aEdge.setLeftFace(triangle);
			bEdge.setLeftFace(triangle);
			cEdge.setLeftFace(triangle);
			
			//vertex connection for a-b
			VertexClass vertex = aEdge.getTargetVertex();
			VertexClass obsoleteVertex = bEdge.getOppositeEdge().getTargetVertex();
			if (vertex != obsoleteVertex){
				for (int j = 0; j < halfedge.getNumEdges(); j++){
					EdgeClass edge = halfedge.getEdge(j);
					if (edge.getTargetVertex() == obsoleteVertex)
						edge.setTargetVertex(vertex);
				}
				halfedge.removeVertex(obsoleteVertex);
			}
			
			//vertex connection for a-b
			vertex = bEdge.getTargetVertex();
			obsoleteVertex = cEdge.getOppositeEdge().getTargetVertex();
			if (vertex != obsoleteVertex){
				for (int j = 0; j < halfedge.getNumEdges(); j++){
					EdgeClass edge = halfedge.getEdge(j);
					if (edge.getTargetVertex() == obsoleteVertex)
						edge.setTargetVertex(vertex);
				}
				halfedge.removeVertex(obsoleteVertex);
			}
			
			//vertex connection for a-b
			vertex = cEdge.getTargetVertex();
			obsoleteVertex = aEdge.getOppositeEdge().getTargetVertex();
			if (vertex != obsoleteVertex) {
				for (int j = 0; j < halfedge.getNumEdges(); j++){
					EdgeClass edge = halfedge.getEdge(j);
					if (edge.getTargetVertex() == obsoleteVertex)
						edge.setTargetVertex(vertex);
				}
				halfedge.removeVertex(obsoleteVertex);
			}
			
			if (parseProperties){
				NodeList propList = triangleElement.getElementsByTagName("property");
				for (int j = 0; j < propList.getLength(); j++)
					readProperty(triangle, (Element)propList.item(j));
			}
			} catch (Throwable t) {
				t.printStackTrace();
				throw new RuntimeException(t);
			}
		}
		
		
		return halfedge;
	}
	
	
	private void readProperty(Node<?, ?, ?> node, Element propElem){
		String name = propElem.getAttribute("name");
		if (name.equals("")){
			DBGTracer.msg("No name attribute for property!");
			return;
		}
		String type = propElem.getAttribute("type");
		if (type.equals("")){
			DBGTracer.msg("No type attribute for property!");
			return;
		}
		String value = propElem.getAttribute("value");
		if (value.equals("")){
			DBGTracer.msg("No value attribute for property!");
			return;		
		}
		Class<?> param = null;
		Object realValue = null;
		if (type.toLowerCase().equals("integer")){
			param = Integer.class;
			try {
				realValue = Integer.parseInt(value);
			} catch (NumberFormatException e){}
		}
		if (type.toLowerCase().equals("long")){
			param = Long.class;
			try {
				realValue = Long.parseLong(value);
			} catch (NumberFormatException e){}
		}
		if (type.toLowerCase().equals("float")){
			param = Float.class;
			try {
				realValue = Float.parseFloat(value);
			} catch (NumberFormatException e){}	
		}
		if (type.toLowerCase().equals("double")){
			param = Double.class;
			try {
				realValue = Double.parseDouble(value);
			} catch (NumberFormatException e){}	
		}
		if (type.toLowerCase().equals("boolean")){
			param = Boolean.class;
			try{
				realValue = Boolean.parseBoolean(value);
			} catch (Exception e){};
		}
		if (param == null){
			DBGTracer.msg("Unknown type in attribute!");
			return;
		}
		if (realValue == null){
			DBGTracer.msg("Unknown value in property!");
			return;
		}
		
		String methodName = "set" + name.toUpperCase().charAt(0) + name.substring(1);
		Method setterMethod = null;
		try {
			setterMethod = node.getClass().getMethod(methodName, param);
		} catch (Exception e) {
			DBGTracer.msg("No setter found for property " + name);
		}
		if (setterMethod == null){
			DBGTracer.msg("No setter found for property " + name);
			return;
		}
		try {
			setterMethod.invoke(node, new Object[]{realValue});
		} catch (Exception e) {
			DBGTracer.msg("Error setting attribute value: " + e);
			e.printStackTrace();
		}
	}

	public boolean isParsingProperties() {
		return parseProperties;
	}

	public void setParseProperties(boolean parseProperties) {
		this.parseProperties = parseProperties;
	}
		
	public static void main(String[] args) {
		CPMLReader<CPMVertex, CPMEdge, CPMFace> reader = CPMLReader.createCPMLReader(CPMVertex.class, CPMEdge.class, CPMFace.class);
		File file = new File("data/polyeder/test1.cpml");
		HalfEdgeDataStructure<CPMVertex, CPMEdge, CPMFace> graph = null;
		try {
			graph = reader.readCPML(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		} catch (GraphReaderException e) {
			e.printStackTrace();
			return;
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		System.err.println(graph);
		
		System.err.print("running checks...");
		if (!ConsistencyCheck.isTriangulation(graph))
			System.err.println("Graph is no triangulation!");
		if (!ConsistencyCheck.isSphere(graph))
			System.err.println("Graph is no sphere!");
		if (!ConsistencyCheck.checkEdgeLengths(graph))
			System.err.println("Graph has invalid edge lengths!");
		System.err.println("done.");
	}
	
}

