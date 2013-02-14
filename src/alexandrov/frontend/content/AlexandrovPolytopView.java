package alexandrov.frontend.content;

import static de.jreality.shader.CommonAttributes.AMBIENT_COEFFICIENT;
import static de.jreality.shader.CommonAttributes.AMBIENT_COLOR;
import static de.jreality.shader.CommonAttributes.ANTIALIASING_ENABLED;
import static de.jreality.shader.CommonAttributes.BACKGROUND_COLOR;
import static de.jreality.shader.CommonAttributes.BACK_FACE_CULLING_ENABLED;
import static de.jreality.shader.CommonAttributes.DIFFUSE_COLOR;
import static de.jreality.shader.CommonAttributes.FACE_DRAW;
import static de.jreality.shader.CommonAttributes.LINE_SHADER;
import static de.jreality.shader.CommonAttributes.POINT_RADIUS;
import static de.jreality.shader.CommonAttributes.POINT_SHADER;
import static de.jreality.shader.CommonAttributes.POLYGON_SHADER;
import static de.jreality.shader.CommonAttributes.SMOOTH_SHADING;
import static de.jreality.shader.CommonAttributes.SPECULAR_COEFFICIENT;
import static de.jreality.shader.CommonAttributes.SPECULAR_COLOR;
import static de.jreality.shader.CommonAttributes.SPECULAR_EXPONENT;
import static de.jreality.shader.CommonAttributes.SPHERES_DRAW;
import static de.jreality.shader.CommonAttributes.TRANSPARENCY;
import static de.jreality.shader.CommonAttributes.TRANSPARENCY_ENABLED;
import static de.jreality.shader.CommonAttributes.TUBES_DRAW;
import static de.jreality.shader.CommonAttributes.TUBE_RADIUS;
import static de.jreality.shader.CommonAttributes.VERTEX_DRAW;
import halfedge.HalfEdgeDataStructure;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JPanel;

import util.debug.DBGTracer;
import alexandrov.frontend.controller.MainController;
import alexandrov.frontend.tool.EdgeLengthEditor3D;
import alexandrov.frontend.tool.EdgePickTool;
import alexandrov.graph.CPMEdge;
import alexandrov.graph.CPMFace;
import alexandrov.graph.CPMVertex;
import alexandrov.math.CapCurvatureFunctional;
import de.jreality.geometry.IndexedFaceSetFactory;
import de.jreality.geometry.IndexedLineSetFactory;
import de.jreality.io.JrScene;
import de.jreality.io.JrSceneFactory;
import de.jreality.math.MatrixBuilder;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.Light;
import de.jreality.scene.PointLight;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Viewer;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.DoubleArrayArray;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.DefaultGeometryShader;
import de.jreality.shader.DefaultLineShader;
import de.jreality.shader.DefaultPointShader;
import de.jreality.shader.DefaultTextShader;
import de.jreality.shader.ShaderUtility;
import de.jreality.tools.EncompassTool;
import de.jreality.ui.viewerapp.ViewerApp;
import de.jreality.util.CameraUtility;
import de.jreality.util.RenderTrigger;


/**
 * An jReality viewer for polyhedrons
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 * 
 * @param <V> Vertex class this view can display
 * @param <E> Edge class this view can display
 * @param <F> Face class this view can display
 */
@SuppressWarnings("serial")
public class AlexandrovPolytopView extends JPanel{

	private MainController
		controller = null;
	private HalfEdgeDataStructure<CPMVertex, CPMEdge, CPMFace>
		activeGraph = null;
	
	private ViewerApp
		viewerApp = null;
	
	//geometry
	private SceneGraphComponent 
		sceneRoot = null,
		polyederRoot = new SceneGraphComponent(),
		meshRoot = new SceneGraphComponent(),
		zeroRoot = new SceneGraphComponent(),
		basePlaneRoot = new SceneGraphComponent(),
		geometryRoot = new SceneGraphComponent();
	private IndexedFaceSetFactory 
		polyederFactory = new IndexedFaceSetFactory();
	private IndexedLineSetFactory
		meshFactory = new IndexedLineSetFactory();
	private IndexedLineSet
		meshGeometry = meshFactory.getIndexedLineSet();
	private IndexedFaceSet		
		polyederGeometry = polyederFactory.getIndexedFaceSet();
	private RenderTrigger
		renderTrigger = new RenderTrigger();
	
	//tools
	private EdgeLengthEditor3D
		edgeLengthEditor = null;
	private EdgePickTool
		edgePickTool = null;
	
	private boolean
		updateActiveGeometry = false;
	
	//properties
    private Light 
    	light1 = new PointLight(),
    	light2 = new PointLight(),
    	defaultLight = new PointLight();
	
    private Color
		faceColor = new Color(91, 122, 109),
		light1Color = Color.WHITE,
		light2Color = Color.WHITE,
		meshColor = new Color(66, 66, 66),
		basePlaneColor = new Color(100, 100, 100),
		backgroundColor = new Color(0xcecece);
	
	private Appearance 
		polyederApp = new Appearance(),
		meshApp = new Appearance(),
		rootApp = new Appearance();
	
	private boolean
		antialias = true,
		showGraph = false,
		showMedial = false,
		showPolyeder = true,
		showMesh = true,
		showGrid = false,
		normalize = true,
		transparencyPolyeder = true,
		light1On = true,
		light2On = true,
		showFlippedEdges = true,
		showVertexIndices = false,
		showEdgeLengths = false,
		dragEdit = true,
		smoothShading = false,
		hideHiddenEdges = true;
	
	private double
		transparencyPolyederValue = 0.0,
		light1intensity = 8,
		light2intensity = 8,
		defaultLightIntensity = 1,
		meshWidth = 0.015;
	
	private ViewerMode
		viewerMode = ViewerMode.VIEWER_MODE_POLYHEDRON;
	
	private AlexandrovOptionPanel
		viewOptPanel = null;
	
	public AlexandrovPolytopView(MainController controller, ViewerMode mode){
		this.controller = controller;
		this.viewerMode = mode;
		viewOptPanel = new AlexandrovOptionPanel(controller, this);
		edgeLengthEditor = new EdgeLengthEditor3D(this, controller);
		edgePickTool = new EdgePickTool(edgeLengthEditor);
		
		initScene();
		
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.weighty = 0;
		JPanel wrap = new JPanel();
		wrap.setLayout(new BorderLayout());
		wrap.add(edgeLengthEditor, BorderLayout.CENTER);
		add(wrap, c);
		edgeLengthEditor.setShrinked(true);
		
		c.fill = GridBagConstraints.BOTH;
		c.weighty = 1;
		add(viewerApp.getContent(), c);
		
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weighty = 0;
		wrap = new JPanel();
		wrap.setLayout(new BorderLayout());
		wrap.add(viewOptPanel, BorderLayout.CENTER);
		add(wrap, c);
		viewOptPanel.setShrinked(true);

//		updateProperties();
	}

	
	private void initScene(){
		boolean portal = "portal".equals(System.getProperty("de.jreality.scene.tool.Config"));
		JrScene scene = null;
		if (portal)
			scene = JrSceneFactory.getDefaultPortalScene(); 
		else
			scene = JrSceneFactory.getDefaultDesktopScene();
		
		sceneRoot=scene.getSceneRoot();
		rootApp=sceneRoot.getAppearance();
		
		//root appearance
		rootApp.setAttribute(BACKGROUND_COLOR, backgroundColor);
		rootApp.setAttribute(POLYGON_SHADER + "." + ANTIALIASING_ENABLED, antialias);
		
        //geometry node
        geometryRoot.setName("Geometry");
        geometryRoot = scene.getPath("emptyPickPath").getLastComponent();
        
		//base plane
		Appearance planeApp = new Appearance();
		planeApp.setAttribute(POLYGON_SHADER + "." + DIFFUSE_COLOR, basePlaneColor);
		planeApp.setAttribute(VERTEX_DRAW, false);
//		IndexedFaceSet plane = Primitives.plainQuadMesh(0.1, 0.1, 25, 25);
		MatrixBuilder.euclidean().rotateX(Math.PI/2).assignTo(basePlaneRoot);
		basePlaneRoot.setName("Base Plane");
//		basePlaneRoot.setGeometry(plane);
		basePlaneRoot.setAppearance(planeApp);
		basePlaneRoot.setVisible(false);
		geometryRoot.addChild(basePlaneRoot);
		
        //mesh
        Font labelFont = new Font("Arial", Font.BOLD, 40);
        DefaultGeometryShader geomShader = ShaderUtility.createDefaultGeometryShader(meshApp, true);
        DefaultPointShader dps = (DefaultPointShader)geomShader.getPointShader();
        DefaultTextShader vertexTextShader = (DefaultTextShader)dps.getTextShader();
        vertexTextShader.setFont(labelFont);
        vertexTextShader.setDiffuseColor(controller.getColorController().getIndexColor());
        vertexTextShader.setScale(0.0017);
        DefaultLineShader dls = (DefaultLineShader)geomShader.getLineShader();
        DefaultTextShader lengthTextShader = (DefaultTextShader)dls.getTextShader();
        lengthTextShader.setFont(labelFont);
        lengthTextShader.setDiffuseColor(Color.BLACK);
        lengthTextShader.setScale(0.0017);
        meshApp.setAttribute(LINE_SHADER + "." + DIFFUSE_COLOR, meshColor);
        meshApp.setAttribute(LINE_SHADER + "." + SPECULAR_COLOR, Color.WHITE);
        meshApp.setAttribute(LINE_SHADER + "." + SPECULAR_EXPONENT, 30.0);
        meshApp.setAttribute(FACE_DRAW, false);
        meshApp.setAttribute(TUBES_DRAW, true);
        meshApp.setAttribute(TUBE_RADIUS, meshWidth);
//        meshApp.setAttribute(TUBE_STYLE, TubeUtility.FRENET);
        meshApp.setAttribute(VERTEX_DRAW, true);
        meshApp.setAttribute(SPHERES_DRAW, true);
        meshApp.setAttribute(POINT_RADIUS, meshWidth);
        meshApp.setAttribute(POINT_SHADER + "." + DIFFUSE_COLOR, meshColor);
        meshApp.setAttribute(POINT_SHADER + "." + SPECULAR_COLOR, Color.WHITE);
        meshApp.setAttribute(POINT_SHADER + "." + SPECULAR_EXPONENT, 30.0);
        meshRoot.setAppearance(meshApp);
//        meshRoot.setGeometry(meshGeometry);
        meshRoot.setVisible(showMesh);
        meshRoot.setName("Mesh");
        geometryRoot.addChild(meshRoot);
        
        //zero indicator
//        zeroApp.setAttribute(VERTEX_DRAW, true);
//        zeroApp.setAttribute(POINT_SHADER + "." + DIFFUSE_COLOR, Color.RED);
//        zeroApp.setAttribute("pointSize", 5.0);
//        zeroApp.setAttribute(SPHERES_DRAW, false);
//        PointSetFactory psf = new PointSetFactory();
//        psf.setVertexCount(1);
//        psf.setVertexCoordinates(new double[]{0,0,0});
//        psf.update();
//        PointSet point = psf.getPointSet();
//        zeroRoot.setGeometry(point);
//        zeroRoot.setAppearance(zeroApp);
//        zeroRoot.setName("Zero Indicator");
//        zeroRoot.setVisible(false);
//        geometryRoot.addChild(zeroRoot);

        //polyeder
		polyederApp.setAttribute(POLYGON_SHADER + "." + SMOOTH_SHADING, smoothShading);
        polyederApp.setAttribute(POLYGON_SHADER + "." + DIFFUSE_COLOR, faceColor);
        polyederApp.setAttribute(POLYGON_SHADER + "." + SPECULAR_COLOR, Color.WHITE);
        polyederApp.setAttribute(POLYGON_SHADER + "." + SPECULAR_EXPONENT, 30.0);
        polyederApp.setAttribute(POLYGON_SHADER + "." + SPECULAR_COEFFICIENT, 0.7);
        polyederApp.setAttribute(POLYGON_SHADER + "." + AMBIENT_COLOR, faceColor);
        polyederApp.setAttribute(POLYGON_SHADER + "." + AMBIENT_COEFFICIENT, 0.4);
        polyederApp.setAttribute(TRANSPARENCY, transparencyPolyederValue);
        polyederApp.setAttribute(TRANSPARENCY_ENABLED, transparencyPolyeder);
        polyederApp.setAttribute(BACK_FACE_CULLING_ENABLED, true);
        polyederApp.setAttribute(TUBES_DRAW, false);
        polyederApp.setAttribute(VERTEX_DRAW, false);
        polyederApp.setAttribute(SPHERES_DRAW, false);
        polyederRoot.setAppearance(polyederApp);
        polyederRoot.setGeometry(polyederGeometry);
        polyederRoot.setVisible(showPolyeder);
        polyederRoot.setName("Polyhedron");
        geometryRoot.addChild(polyederRoot);
        
        //pick path
        if (dragEdit)
        	meshRoot.addTool(edgePickTool);
        meshRoot.addTool(new EncompassTool());

	    viewerApp = new ViewerApp(scene);
	    viewerApp.update();
	    
	    renderTrigger.addViewer(getViewer());
	    renderTrigger.startCollect();
		renderTrigger.addSceneGraphComponent(sceneRoot);
	}
	
	public void encompass(){
		CameraUtility.encompass(getViewer());
	}
	
	public void resetGeometry(){
		polyederRoot.setGeometry(null);
		meshRoot.setGeometry(null);
	}
	
	public void updateGeometry(HalfEdgeDataStructure<CPMVertex, CPMEdge, CPMFace> graph){
		this.activeGraph = graph;
		switch (viewerMode){
		case VIEWER_MODE_CAP:
			basePlaneRoot.setVisible(true);
	        zeroRoot.setVisible(false);
			makeCap(activeGraph);
			break;
		case VIEWER_MODE_POLYHEDRON:
			basePlaneRoot.setVisible(false);
			zeroRoot.setVisible(true);
			makePolyhedron(graph);
			break;
		}
		encompass();
	}
	
	
	private void makeCap(HalfEdgeDataStructure<CPMVertex, CPMEdge, CPMFace> graph){
		if (graph == null || graph.getNumEdges() == 0){
			resetGeometry();
			return;
		}
		DBGTracer.msg("updating polyeder geometry...");
		
		//vertices 
		double[][] vertexData = new double[graph.getNumVertices()][3];
		String[] vertexLabels = new String[graph.getNumVertices()];
		for (CPMVertex v : graph.getVertices()){
			vertexLabels[v.getIndex()] = "" + v.getIndex();
			double[] p = vertexData[v.getIndex()];
			p[0] = v.getXYZW().x / v.getXYZW().w;
			p[1] = v.getXYZW().y / v.getXYZW().w;
			p[2] = v.getXYZW().z / v.getXYZW().w;
		}
		//indices
		int[][] indexData = new int[graph.getNumFaces()][3];
		int[][] edgeIndexData = new int[graph.getNumEdges() / 2][2];
		double[][] edgeColors = new double[graph.getNumEdges() / 2][];
		String[] edgeLengthLabels = new String[graph.getNumEdges() / 2];
		HashSet<CPMFace> readyFaces = new HashSet<CPMFace>();
		HashMap<Integer, CPMEdge> edgeIDMap = new HashMap<Integer, CPMEdge>();
		int edgeNum = 0;
		for (CPMEdge e : graph.getEdges()){
			if (e.isPositive()){
				edgeIDMap.put(edgeNum, e);
				BigDecimal length = new BigDecimal(e.getLength());
				length = length.round(new MathContext(3));
				edgeLengthLabels[edgeNum] = length.toString();
				edgeIndexData[edgeNum][0] = e.getTargetVertex().getIndex();
				edgeIndexData[edgeNum][1] = e.getStartVertex().getIndex();
				if ((e.getFlipCount() % 2) != 0 && showFlippedEdges)
					edgeColors[edgeNum] = new double[]{1,0.26,0.26};
				else {
					float[] meshColorComp = meshColor.getComponents(new float[4]);
					edgeColors[edgeNum] = new double[]{meshColorComp[0],meshColorComp[1],meshColorComp[2]};
				}
				edgeNum++;
			}
			CPMFace f = e.getLeftFace();
			if (f == null)
				continue;
			if (readyFaces.contains(f))
				continue;
			CPMEdge e1 = e.getNextEdge();
			CPMEdge e2 = e.getPreviousEdge();
		
			if (CapCurvatureFunctional.isFaceDegenerated(e)) {
				CPMEdge degEdge = null;
				if (CapCurvatureFunctional.isDegenerated(e))
					degEdge = e;
				if (CapCurvatureFunctional.isDegenerated(e.getNextEdge()))
					degEdge = e.getNextEdge();
				if (CapCurvatureFunctional.isDegenerated(e.getPreviousEdge()))
					degEdge = e.getPreviousEdge();
				CPMVertex upperVertex = degEdge.getNextEdge().getTargetVertex();
				double[][] newVertexData = new double[vertexData.length + 1][];
				System.arraycopy(vertexData, 0, newVertexData, 0, vertexData.length);
				newVertexData[newVertexData.length - 1] = new double[3];
				newVertexData[newVertexData.length - 1][0] = upperVertex.getXYZW().x / upperVertex.getXYZW().w;
				newVertexData[newVertexData.length - 1][1] = 0.0;
				newVertexData[newVertexData.length - 1][2] = upperVertex.getXYZW().z / upperVertex.getXYZW().w;
				vertexData = newVertexData;
				int[][] newIndexData = new int[indexData.length + 1][3];
				System.arraycopy(indexData, 0, newIndexData, 0, indexData.length);
				newIndexData[newIndexData.length - 1] = new int[3];
				newIndexData[f.getIndex()][2] = degEdge.getStartVertex().getIndex();
				newIndexData[f.getIndex()][1] = newVertexData.length - 1;
				newIndexData[f.getIndex()][0] = degEdge.getNextEdge().getTargetVertex().getIndex();
				newIndexData[newIndexData.length - 1][0] = degEdge.getNextEdge().getTargetVertex().getIndex();
				newIndexData[newIndexData.length - 1][1] = degEdge.getTargetVertex().getIndex();
				newIndexData[newIndexData.length - 1][2] = newVertexData.length - 1;
				indexData = newIndexData;
			} else {
				indexData[f.getIndex()][2] = e2.getTargetVertex().getIndex();
				indexData[f.getIndex()][1] = e.getTargetVertex().getIndex();
				indexData[f.getIndex()][0] = e1.getTargetVertex().getIndex();
			}
			readyFaces.add(f);
		}
		
		//polyhedron
		polyederFactory = new IndexedFaceSetFactory();
		polyederFactory.setVertexCount(vertexData.length);
		polyederFactory.setFaceCount(indexData.length);
		polyederFactory.setGenerateEdgesFromFaces(false);
		polyederFactory.setGenerateFaceNormals(true);
		polyederFactory.setVertexCoordinates(vertexData);
		polyederFactory.setFaceIndices(indexData);
		polyederFactory.update();
		polyederGeometry = polyederFactory.getIndexedFaceSet();
		polyederGeometry.setGeometryAttributes("pickable", false);
		polyederRoot.setGeometry(polyederGeometry);
		
		//outline
		meshFactory = new IndexedLineSetFactory();
		meshFactory.setVertexCount(vertexData.length);
		meshFactory.setEdgeCount(edgeIndexData.length);
		meshFactory.setVertexCoordinates(vertexData);
		meshFactory.setEdgeIndices(edgeIndexData);
		if (showEdgeLengths)
			meshFactory.setEdgeLabels(edgeLengthLabels);
		if (showVertexIndices)
			meshFactory.setVertexLabels(vertexLabels);
		meshFactory.update();
		meshGeometry = meshFactory.getIndexedLineSet();
		meshGeometry.setEdgeAttributes(Attribute.COLORS, new DoubleArrayArray.Array(edgeColors));
		meshRoot.setGeometry(meshGeometry);
		
		edgePickTool.setEdgeIDMap(edgeIDMap);
	}
	
	
	private void makePolyhedron(HalfEdgeDataStructure<CPMVertex, CPMEdge, CPMFace> graph){
		if (graph == null || graph.getNumEdges() == 0){
			resetGeometry();
			return;
		}
		DBGTracer.msg("updating polyeder geometry...");
		
		// vertices 
		double[][] vertexData = new double[graph.getNumVertices()][3];
		String[] vertexLabels = new String[graph.getNumVertices()];
		for (CPMVertex v : graph.getVertices()){
			vertexLabels[v.getIndex()] = "" + v.getIndex();
			double[] p = vertexData[v.getIndex()];
			p[0] = v.getXYZW().x / v.getXYZW().w;
			p[1] = v.getXYZW().y / v.getXYZW().w;
			p[2] = v.getXYZW().z / v.getXYZW().w;
		}

		// faces
		int[][] indexData = new int[graph.getNumFaces()][3];
//		HashSet<CPMFace> readyFaces = new HashSet<CPMFace>();
//		int edgeNum = 0;
//		for (CPMEdge e : graph.getEdges()){
//			CPMFace f = e.getLeftFace();
//			if (f == null)
//				continue;
//			if (readyFaces.contains(f))
//				continue;
//			CPMEdge e1 = e.getNextEdge();
//			CPMEdge e2 = e.getPreviousEdge();
//			
//			indexData[f.getIndex()][2] = e2.getTargetVertex().getIndex();
//			indexData[f.getIndex()][1] = e.getTargetVertex().getIndex();
//			indexData[f.getIndex()][0] = e1.getTargetVertex().getIndex();
//			readyFaces.add(f);
//		}
		for (CPMFace f : graph.getFaces()){
			List<CPMEdge> b = f.getBoundary();
			if (b.size() != 3)
				throw new RuntimeException("No Triangulation in makePolyhedron()!");
			int count = 2; // orientate faces clockwise 
			for (CPMEdge e : b)
				indexData[f.getIndex()][count--] = e.getTargetVertex().getIndex();
		}
		
		
		// edges
		HashMap<Integer, CPMEdge> edgeIDMap = new HashMap<Integer, CPMEdge>();
		LinkedList<CPMEdge> shownEdges = new LinkedList<CPMEdge>();
		for (CPMEdge edge : graph.getPositiveEdges()){
			if (!(edge.isHidden() && hideHiddenEdges))
				shownEdges.add(edge);
		}
		int[][] edgeIndexData = new int[shownEdges.size()][2];
		String[] edgeLengthLabels = new String[shownEdges.size()];
		double[][] edgeColors = new double[shownEdges.size()][];
		int edgeNum = 0;
		for (CPMEdge edge : shownEdges){
			edgeIDMap.put(edgeNum, edge);
			BigDecimal length = new BigDecimal(edge.getLength());
			length = length.round(new MathContext(3));
			if ((edge.getFlipCount() % 2) != 0 && showFlippedEdges)
				edgeColors[edgeNum] = new double[]{1,0.26,0.26};
			else {
				float[] meshColorComp = meshColor.getComponents(new float[4]);
				edgeColors[edgeNum] = new double[]{meshColorComp[0],meshColorComp[1],meshColorComp[2]};
			}
			edgeLengthLabels[edgeNum] = length.toString();
			edgeIndexData[edgeNum][0] = edge.getTargetVertex().getIndex();
			edgeIndexData[edgeNum][1] = edge.getStartVertex().getIndex();
			edgeNum++;
		}
		
		// polyhedron
		polyederFactory = new IndexedFaceSetFactory();
		polyederFactory.setVertexCount(vertexData.length);
		polyederFactory.setFaceCount(indexData.length);
		polyederFactory.setGenerateEdgesFromFaces(false);
		polyederFactory.setGenerateFaceNormals(true);
		polyederFactory.setGenerateVertexNormals(true);
		polyederFactory.setVertexCoordinates(vertexData);
		polyederFactory.setFaceIndices(indexData);
		polyederFactory.update();
		polyederGeometry = polyederFactory.getIndexedFaceSet();
		polyederGeometry.setGeometryAttributes("pickable", false);
		polyederRoot.setGeometry(polyederGeometry);
		
		//outline
		meshFactory = new IndexedLineSetFactory();
		meshFactory.setVertexCount(vertexData.length);
		meshFactory.setEdgeCount(edgeIndexData.length);
		meshFactory.setVertexCoordinates(vertexData);
		meshFactory.setEdgeIndices(edgeIndexData);
		if (showEdgeLengths)
			meshFactory.setEdgeLabels(edgeLengthLabels);
		if (showVertexIndices)
			meshFactory.setVertexLabels(vertexLabels);
		meshFactory.update();
		meshGeometry = meshFactory.getIndexedLineSet();
		meshGeometry.setEdgeAttributes(Attribute.COLORS, new DoubleArrayArray.Array(edgeColors));
		meshRoot.setGeometry(meshGeometry);
		
		edgePickTool.setEdgeIDMap(edgeIDMap);
	}


	public Viewer getViewer(){
		return viewerApp.getCurrentViewer();
	}

	public ViewerApp getViewerApp() {
		return viewerApp;
	}

	public void update() {
		getViewer().render();
	}
	
	public JComponent getViewerComponent() {
		return this;
	}


	public boolean isAntialias() {
		return antialias;
	}


	public double getDefaultLightIntensity() {
		return defaultLightIntensity;
	}


	public Color getFaceColor() {
		return faceColor;
	}


	public Color getLight1Color() {
		return light1Color;
	}


	public double getLight1intensity() {
		return light1intensity;
	}


	public boolean isLight1On() {
		return light1On;
	}


	public Color getLight2Color() {
		return light2Color;
	}


	public double getLight2intensity() {
		return light2intensity;
	}


	public boolean isLight2On() {
		return light2On;
	}


	public Color getMeshColor() {
		return meshColor;
	}


	public double getMeshWidth() {
		return meshWidth;
	}


	public boolean isNormalize() {
		return normalize;
	}



	public boolean isShowGraph() {
		return showGraph;
	}


	public boolean isShowGrid() {
		return showGrid;
	}


	public boolean isShowMedial() {
		return showMedial;
	}


	public boolean isShowMesh() {
		return showMesh;
	}


	public boolean isShowPolyeder() {
		return showPolyeder;
	}




	public boolean isTransparencyPolyeder() {
		return transparencyPolyeder;
	}


	public double getTransparencyPolyederValue() {
		return transparencyPolyederValue;
	}


	public void setAntialias(boolean antialias) {
		this.antialias = antialias;
		sceneRoot.getAppearance().setAttribute(POLYGON_SHADER + "." + ANTIALIASING_ENABLED, antialias);
	}


	public void setDefaultLightIntensity(double defaultLightIntensity) {
		this.defaultLightIntensity = defaultLightIntensity;
	}


	public void setFaceColor(Color faceColor) {
		this.faceColor = faceColor;
		polyederApp.setAttribute(POLYGON_SHADER + "." + DIFFUSE_COLOR, faceColor);
        polyederApp.setAttribute(POLYGON_SHADER + "." + AMBIENT_COLOR, faceColor);
	}


	public void setLight1Color(Color light1Color) {
		this.light1Color = light1Color;
        light1.setColor(light1Color);
	}


	public void setLight1intensity(double light1intensity) {
		this.light1intensity = light1intensity;
		light1.setIntensity(light1intensity);
	}


	public void setLight1On(boolean light1On) {
		this.light1On = light1On;
		if (light1On)
        	light1.setIntensity(light1intensity);
        else
        	light1.setIntensity(0);   
		if (light1On || light2On)
        	defaultLight.setIntensity(0);
        else
        	defaultLight.setIntensity(defaultLightIntensity);
	}


	public void setLight2Color(Color light2Color) {
		this.light2Color = light2Color;
        light2.setColor(light2Color);
	}


	public void setLight2intensity(double light2intensity) {
		this.light2intensity = light2intensity;
		light2.setIntensity(light2intensity);
	}


	public void setLight2On(boolean light2On) {
		this.light2On = light2On;
        if (light2On)
        	light2.setIntensity(light2intensity);
        else
        	light2.setIntensity(0);
		if (light1On || light2On)
        	defaultLight.setIntensity(0);
        else
        	defaultLight.setIntensity(defaultLightIntensity);
	}


	public void setMeshColor(Color meshColor) {
		this.meshColor = meshColor;
        meshApp.setAttribute(LINE_SHADER + "." + DIFFUSE_COLOR, meshColor);
        meshApp.setAttribute(POINT_SHADER + "." + DIFFUSE_COLOR, meshColor);
	}

	public void setSmoothShading(boolean smooth) {
		this.smoothShading = smooth;
		polyederApp.setAttribute(POLYGON_SHADER + "." + CommonAttributes.SMOOTH_SHADING, smooth);
	}
	
	public boolean isSmoothShading(){
		return smoothShading;
	}
	

	public void setMeshWidth(double meshWidth) {
		this.meshWidth = meshWidth;
        meshApp.setAttribute(TUBE_RADIUS, meshWidth);
        meshApp.setAttribute(POINT_RADIUS, meshWidth);
	}


	public void setNormalize(boolean normalize) {
		this.normalize = normalize;
	}


	public void setShowGraph(boolean showGraph) {
		this.showGraph = showGraph;
	}


	public void setShowGrid(boolean showGrid) {
		this.showGrid = showGrid;
	}


	public void setShowMedial(boolean showMedial) {
		this.showMedial = showMedial;
	}


	public void setShowMesh(boolean showMesh) {
		this.showMesh = showMesh;
		meshRoot.setVisible(showMesh);
	}


	public void setShowPolyeder(boolean showPolyeder) {
		this.showPolyeder = showPolyeder;
		polyederRoot.setVisible(showPolyeder);
	}



	public void setTransparencyPolyeder(boolean transparencyPolyeder) {
		this.transparencyPolyeder = transparencyPolyeder;
	}


	public void setTransparencyPolyederValue(double transparencyPolyederValue) {
		this.transparencyPolyederValue = transparencyPolyederValue;
        polyederApp.setAttribute(TRANSPARENCY, transparencyPolyederValue);
	}

	public boolean isShowFlippedEdges() {
		return showFlippedEdges;
	}

	public void setShowFlippedEdges(boolean showFlippedEdges) {
		this.showFlippedEdges = showFlippedEdges;
		updateGeometry(activeGraph);
	}

	public boolean isShowEdgeLengths() {
		return showEdgeLengths;
	}

	public boolean isShowVertexIndices() {
		return showVertexIndices;
	}

	public void setShowEdgeLengths(boolean showEdgeLengths) {
		this.showEdgeLengths = showEdgeLengths;
		updateGeometry(activeGraph);
	}

	public void setShowVertexIndices(boolean showVertexIndeices) {
		this.showVertexIndices = showVertexIndeices;
		updateGeometry(activeGraph);
	}


	public HalfEdgeDataStructure<CPMVertex, CPMEdge, CPMFace> getActiveGraph() {
		return activeGraph;
	}


	public ViewerMode getViewerMode() {
		return viewerMode;
	}
	public void setViewerMode(ViewerMode viewerMode) {
		this.viewerMode = viewerMode;
	}


	public boolean isDragEdit() {
		return dragEdit;
	}


	public void setDragEdit(boolean dragEdit) {
		this.dragEdit = dragEdit;
		if (dragEdit){
			if (!meshRoot.getTools().contains(edgePickTool))
				meshRoot.addTool(edgePickTool);
		} else {
			if (meshRoot.getTools().contains(edgePickTool))
				meshRoot.removeTool(edgePickTool);
		}
			
	}


	public boolean isHideHiddenEdges() {
		return hideHiddenEdges;
	}


	public void setHideHiddenEdges(boolean hideHiddenEdges) {
		this.hideHiddenEdges = hideHiddenEdges;
		updateGeometry(activeGraph);
	}


	public Color getBackgroundColor() {
		return backgroundColor;
	}


	public void setBackgroundColor(Color backgroundColor) {
		this.backgroundColor = backgroundColor;
		rootApp.setAttribute(BACKGROUND_COLOR, backgroundColor);
	}


	public SceneGraphComponent getGeometry() {
		return geometryRoot;
	}

	public SceneGraphComponent getPolyhedron(){
		return polyederRoot;
	}
	
}
