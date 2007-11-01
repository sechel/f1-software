package halfedge.io;

import halfedge.Edge;
import halfedge.Face;
import halfedge.HalfEdgeDataStructure;
import halfedge.Node;
import halfedge.Vertex;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Method;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import util.debug.DBGTracer;


/**
 * A reader class for the HEML(Half Edge Markup Language) file format.
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 */
public class HEMLReader 
<
	V extends Vertex<V, E, F>,
	E extends Edge<V, E, F>, 
	F extends Face<V, E, F>
>{

	private DocumentBuilder
		documentBuilder = null;
	
	private Class<V>
		vClass = null;
	private Class<E>
		eClass = null;	
	private Class<F>
		fClass = null;	
	
	private boolean 
		parseProperties = true;
	
	
	private HEMLReader(Class<V> vClass, Class<E> eClass, Class<F> fClass) {
		this.vClass = vClass;
		this.eClass = eClass;
		this.fClass = fClass;
	}
	
	public static 
	<	
		VC extends Vertex<VC, EC, FC>, 
		EC extends Edge<VC, EC, FC>, 
		FC extends Face<VC, EC, FC>
	> 
	HEMLReader<VC, EC, FC> createHEMLReader(Class<VC> vClass, Class<EC> eClass, Class<FC> fClass){
		return new HEMLReader<VC, EC, FC>(vClass, eClass, fClass);
	}
	
	
	public HalfEdgeDataStructure<V, E, F> 
	readHEML(File input) throws GraphReaderException, FileNotFoundException, IOException {
		HalfEdgeDataStructure<V, E, F> halfedge = HalfEdgeDataStructure.createHEDS(vClass, eClass, fClass);
		FileInputStream fis = new FileInputStream(input);
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		try {
			documentBuilder = dbf.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		Document doc = null;
		try {
			doc = documentBuilder.parse(fis);
		} catch (Exception e) {
			throw new GraphReaderException(e);
		}
		
		NodeList graphList = doc.getElementsByTagName("heGraph");
		if (graphList.getLength() == 0)
			throw new GraphReaderException("No <heGraph> tag found!");
		Element firstGraph = (Element)graphList.item(0);

		
		/*
		 * reading vertices
		 */
		NodeList vertexListList = firstGraph.getElementsByTagName("vertexlist");
		if (vertexListList.getLength() == 0)
			throw new GraphReaderException("No <vertexlist> tag found!");
		Element firstVertexList = (Element)vertexListList.item(0);
		NodeList vertexList = firstVertexList.getElementsByTagName("vertex");
		if (vertexList.getLength() == 0)
			throw new GraphReaderException("Empty vertex list found!");
		int numVertices = vertexList.getLength();
		for (int i = 0; i < numVertices; i++){
			V v = halfedge.addNewVertex();
			if (parseProperties){
				Element vertexElement = (Element) vertexList.item(i);
				NodeList propList = vertexElement.getElementsByTagName("property");
				for (int j = 0; j < propList.getLength(); j++)
					readProperty(v, (Element)propList.item(j));
			}
		}

		/*
		 * reading faces
		 */
		NodeList faceListList = firstGraph.getElementsByTagName("facelist");
		if (faceListList.getLength() == 0)
			throw new GraphReaderException("No <facelist> tag found!");
		Element firstFaceList = (Element)faceListList.item(0);
		NodeList faceList = firstFaceList.getElementsByTagName("face");
		if (faceList.getLength() == 0)
			throw new GraphReaderException("Empty face list found!");
		int numFaces = faceList.getLength();
		for (int i = 0; i < numFaces; i++){
			F f = halfedge.addNewFace();
			if (parseProperties){
				Element faceElement = (Element) faceList.item(i);
				NodeList propList = faceElement.getElementsByTagName("property");
				for (int j = 0; j < propList.getLength(); j++)
					readProperty(f, (Element)propList.item(j));
			}
		}
		
		/*
		 * reading edges
		 */
		NodeList edgeListList = firstGraph.getElementsByTagName("edgelist");
		if (edgeListList.getLength() == 0)
			throw new GraphReaderException("No <edgelist> tag found!");
		Element firstEdgeList = (Element)edgeListList.item(0);
		NodeList edgeList = firstEdgeList.getElementsByTagName("edge");
		if (edgeList.getLength() == 0)
			throw new GraphReaderException("Empty edge list found!");
		
		int numEdges = edgeList.getLength();
		// construct edges
		for (int i = 0; i < numEdges; i++){
			E e = halfedge.addNewEdge();
			if (parseProperties){
				Element edgeElement = (Element) edgeList.item(i);
				NodeList propList = edgeElement.getElementsByTagName("property");
				for (int j = 0; j < propList.getLength(); j++)
					readProperty(e, (Element)propList.item(j));
			}
		}
		// connect edges
		for (int i = 0; i < numEdges; i++){
			E theEdge = halfedge.getEdge(i);
			Element edge = (Element)edgeList.item(i);
//			int prev;
			int next;
			int opposite;
			int target;
			int left;
			try {
//				prev = Integer.parseInt(edge.getAttribute("prev"));
				next = Integer.parseInt(edge.getAttribute("next"));
				opposite = Integer.parseInt(edge.getAttribute("opposite"));
				target = Integer.parseInt(edge.getAttribute("target"));
				left = Integer.parseInt(edge.getAttribute("left"));
			} catch (NumberFormatException e) {
				throw new GraphReaderException("Invalid atribute at edge " + i);
			}
//			EdgeClass p = null;
			E n = null;
			E o = null;
			V t = null;
			F l = null;
			try {
//				p = halfedge.getEdge(prev);
				n = halfedge.getEdge(next);
				o = halfedge.getEdge(opposite);
				t = halfedge.getVertex(target);
				if (left >= 0)
					l = halfedge.getFace(left);
			} catch (IndexOutOfBoundsException e){
				throw new GraphReaderException("Index out of range at edge " + i);
			}
//			if (p == null)
//				throw new GraphReaderException("Invalid prev index at edge " + i);
			if (n == null)
				throw new GraphReaderException("Invalid next index at edge " + i);
			if (o == null)
				throw new GraphReaderException("Invalid opposite index at edge " + i);
			if (t == null)
				throw new GraphReaderException("Invalid target index at edge " + i);
			if (l == null && left >= 0)
				throw new GraphReaderException("Invalid leftFace index at edge " + i);
			theEdge.setLeftFace(l);
//			if (l != null)
//				l.setBoundingEdge(theEdge);
			theEdge.linkNextEdge(n);
			theEdge.linkOppositeEdge(o);
//			theEdge.setPreviousEdge(p);
			theEdge.setTargetVertex(t);
		}
		return halfedge;
	}
	
	
	private void readProperty(Node<V, E, F> node, Element propElem){
		String name = propElem.getAttribute("name");
		if (name == null){
			DBGTracer.msg("No name attribute for property!");
			return;
		}
		String type = propElem.getAttribute("type");
		if (type == null){
			DBGTracer.msg("No type attribute for property!");
			return;
		}
		String value = propElem.getAttribute("value");
		if (value == null){
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
		if (param == null){
			DBGTracer.msg("Unknown type in attribute!");
			return;
		}
		if (value == null){
			DBGTracer.msg("Unknown value in property!");
			return;
		}
		
		String methodName = "set" + name.toUpperCase().charAt(0) + name.substring(1);
		Method setterMethod = null;
		try {
			setterMethod = node.getClass().getMethod(methodName, new Class[]{param});
		} catch (Exception e) {
			DBGTracer.msg("No getter found for property " + name);
		}
		if (setterMethod == null){
			DBGTracer.msg("No getter found for property " + name);
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
	
	
}
