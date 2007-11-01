package koebe.frontend.content.jrealityviewer;

import static de.jreality.shader.CommonAttributes.AMBIENT_COEFFICIENT;
import static de.jreality.shader.CommonAttributes.AMBIENT_COLOR;
import static de.jreality.shader.CommonAttributes.ANTIALIASING_ENABLED;
import static de.jreality.shader.CommonAttributes.BACKGROUND_COLOR;
import static de.jreality.shader.CommonAttributes.BACK_FACE_CULLING_ENABLED;
import static de.jreality.shader.CommonAttributes.DIFFUSE_COLOR;
import static de.jreality.shader.CommonAttributes.EDGE_DRAW;
import static de.jreality.shader.CommonAttributes.FACE_DRAW;
import static de.jreality.shader.CommonAttributes.LINE_SHADER;
import static de.jreality.shader.CommonAttributes.POINT_RADIUS;
import static de.jreality.shader.CommonAttributes.POINT_SHADER;
import static de.jreality.shader.CommonAttributes.POLYGON_SHADER;
import static de.jreality.shader.CommonAttributes.SPECULAR_COEFFICIENT;
import static de.jreality.shader.CommonAttributes.SPECULAR_COLOR;
import static de.jreality.shader.CommonAttributes.SPECULAR_EXPONENT;
import static de.jreality.shader.CommonAttributes.SPHERES_DRAW;
import static de.jreality.shader.CommonAttributes.TRANSPARENCY;
import static de.jreality.shader.CommonAttributes.TRANSPARENCY_ENABLED;
import static de.jreality.shader.CommonAttributes.TUBES_DRAW;
import static de.jreality.shader.CommonAttributes.TUBE_RADIUS;
import static de.jreality.shader.CommonAttributes.VERTEX_DRAW;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.vecmath.Point4d;

import koebe.KoebePolyhedron.KoebePolyhedronContext;
import koebe.frontend.content.Viewer;
import koebe.frontend.controller.MainController;
import math.util.Circles;
import math.util.VecmathTools;
import circlepatterns.graph.CPEdge;
import circlepatterns.graph.CPFace;
import circlepatterns.graph.CPVertex;
import de.jreality.geometry.IndexedFaceSetFactory;
import de.jreality.geometry.SphereUtility;
import de.jreality.math.MatrixBuilder;
import de.jreality.scene.Appearance;
import de.jreality.scene.Camera;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.Light;
import de.jreality.scene.PointLight;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.StorageModel;
import de.jreality.tools.DraggingTool;
import de.jreality.tools.EncompassTool;
import de.jreality.tools.RotateTool;
import de.jreality.ui.viewerapp.ViewerApp;

/**
 * The jreality view for the koebe polyhedron application
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 */
@SuppressWarnings("serial")
public class KoebePolyhedronView extends JPanel implements Viewer{

	private MainController
		controller = null;
	
	private ViewerApp 
		va = null;
	private SceneGraphComponent 
		sceneRoot = new SceneGraphComponent(),
		avatarRoot = new SceneGraphComponent(),
		cameraRoot = new SceneGraphComponent(),
		polyhedronRoot = new SceneGraphComponent(),
		sphereRoot = new SceneGraphComponent(),
		meshRoot = new SceneGraphComponent(),
		circles1Root = new SceneGraphComponent(),
		circles2Root = new SceneGraphComponent(),
		light1Root = new SceneGraphComponent(),
		light2Root = new SceneGraphComponent();
	private IndexedFaceSetFactory 
		polyhedronFactory = new IndexedFaceSetFactory(),
		meshFactory = new IndexedFaceSetFactory(),
		circles1Factory = new IndexedFaceSetFactory(),
		circles2Factory = new IndexedFaceSetFactory();
	private IndexedFaceSet
		meshgeometry = meshFactory.getIndexedFaceSet(),
		polyhedronGeometry = polyhedronFactory.getIndexedFaceSet(),
		circles1Geometry = circles1Factory.getIndexedFaceSet(),
		circles2Geometry = circles2Factory.getIndexedFaceSet();
	
    private Light 
    	light1 = new PointLight(),
    	light2 = new PointLight(),
    	defaultLight = new PointLight();
	
    private Color
		faceColor = new Color(91, 122, 109),
		sphereColor = new Color(107, 85, 107),
		light1Color = Color.WHITE,
		light2Color = Color.WHITE,
		circles1Color = new Color(189, 93, 112),
		circles2Color = new Color(95, 95, 230),
		meshColor = new Color(66, 66, 66),
		backgoundColor = Color.WHITE;
	
	private Appearance 
		rootApp = new Appearance(),
		polyhedronApp = new Appearance(),
		sphereApp = new Appearance(),
		meshApp = new Appearance(),
		circles1App = new Appearance(),
		circles2App = new Appearance();
	
	private boolean
		antialias = true,
		showGraph = false,
		showMedial = false,
		showCircles = false,
		showCircles2 = false,
		showPolyeder = true,
		showMesh = true,
		showSphere = true,
		showGrid = false,
		transparencyPolyeder = true,
		transparencySphere = true,
		light1On = true,
		light2On = true;
	
	private double
		transparencyPolyederValue = 0.0,
		transparencySphereValue = 0.6,
		light1intensity = 4,
		light2intensity = 4,
		defaultLightIntensity = 1,
		meshWidth = 0.03,
		circles1Width = 0.03,
		circles2Width = 0.03;
	
	private KoebeOptionPanel
		viewOptPanel = null;
	
	public KoebePolyhedronView(MainController controller){
		this.controller = controller;
		backgoundColor = controller.getColorController().getBackgroundColor();
		viewOptPanel = new KoebeOptionPanel(controller, this);
		initScene();
		
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1;
		c.weighty = 1;
		
		add((Component) va.getContent(), c);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weighty = 0;
		JPanel wrap = new JPanel();
		wrap.setLayout(new BorderLayout());
		wrap.add(viewOptPanel, BorderLayout.CENTER);
		add(wrap, c);
		viewOptPanel.setShrinked(true);
		
		updateProperties();
	}
	
	
	private void initScene(){
		//root appearance
		rootApp.setAttribute(BACKGROUND_COLOR, backgoundColor);
		sceneRoot.setAppearance(rootApp);
		
		//avatar
		sceneRoot.addChild(avatarRoot);
		
		//camera
		Camera cam = new Camera();
		cam.setFieldOfView(45);
		
		cameraRoot.setCamera(cam);
		MatrixBuilder.euclidean().translate(0, 0, 4).assignTo(cameraRoot);
        SceneGraphPath cameraPath=new SceneGraphPath();
        cameraPath.push(sceneRoot);
        cameraPath.push(avatarRoot);
        cameraPath.push(cameraRoot);
        cameraPath.push(cam);
        avatarRoot.addChild(cameraRoot);
		
        //light 1
        light1.setColor(light1Color);
        light1.setIntensity(light1intensity);
        light1Root.setLight(light1);
		MatrixBuilder.euclidean().translate(6, 4, 0).assignTo(light1Root);
        cameraRoot.addChild(light1Root);
  
        //light 2
        light2.setColor(light2Color);
        light2.setIntensity(light2intensity);
        light2Root.setLight(light2);
		MatrixBuilder.euclidean().translate(-6, 4, 0).assignTo(light2Root);
        cameraRoot.addChild(light2Root);
        
        //default light
        SceneGraphComponent defaultLightRoot = new SceneGraphComponent();
        defaultLight.setColor(Color.WHITE);
        defaultLight.setIntensity(0);
        defaultLightRoot.setLight(defaultLight);
        cameraRoot.addChild(defaultLightRoot);     
        
        //geometry node
        SceneGraphComponent geometryRoot = new SceneGraphComponent();
		sceneRoot.addChild(geometryRoot);
        
        //mesh
        meshApp.setAttribute(LINE_SHADER + "." + DIFFUSE_COLOR, meshColor);
        meshApp.setAttribute(LINE_SHADER + "." + SPECULAR_COLOR, Color.WHITE);
        meshApp.setAttribute(LINE_SHADER + "." + SPECULAR_EXPONENT, 30);
        meshApp.setAttribute(FACE_DRAW, false);
        meshApp.setAttribute(TUBES_DRAW, true);
        meshApp.setAttribute(TUBE_RADIUS, meshWidth);
//        meshApp.setAttribute(TUBE_STYLE, TubeUtility.FRENET);
        meshApp.setAttribute(VERTEX_DRAW, true);
        meshApp.setAttribute(SPHERES_DRAW, true);
        meshApp.setAttribute(POINT_RADIUS, meshWidth);
        meshApp.setAttribute(POINT_SHADER + "." + DIFFUSE_COLOR, meshColor);
        meshApp.setAttribute(POINT_SHADER + "." + SPECULAR_COLOR, Color.WHITE);
        meshApp.setAttribute(POINT_SHADER + "." + SPECULAR_EXPONENT, 30);
        meshRoot.setAppearance(meshApp);
        meshRoot.setGeometry(meshgeometry);
        meshRoot.setVisible(showMesh);
        geometryRoot.addChild(meshRoot);
        
        //circles1
        circles1App.setAttribute(LINE_SHADER + "." + DIFFUSE_COLOR, circles1Color);
        circles1App.setAttribute(LINE_SHADER + "." + SPECULAR_COLOR, Color.WHITE);
        circles1App.setAttribute(LINE_SHADER + "." + SPECULAR_EXPONENT, 30);
        circles1App.setAttribute(FACE_DRAW, false);
        circles1App.setAttribute(TUBES_DRAW, true);
        circles1App.setAttribute(TUBE_RADIUS, circles1Width);
        circles1App.setAttribute(VERTEX_DRAW, false);
        circles1App.setAttribute(SPHERES_DRAW, false);
        circles1App.setAttribute(POINT_RADIUS, circles1Width);
        circles1App.setAttribute(BACK_FACE_CULLING_ENABLED, false);       
        circles1Root.setAppearance(circles1App);
        circles1Root.setGeometry(circles1Geometry);
        circles1Root.setVisible(showCircles);
        geometryRoot.addChild(circles1Root);
        
        //circles2
        circles2App.setAttribute(LINE_SHADER + "." + DIFFUSE_COLOR, circles2Color);
        circles2App.setAttribute(LINE_SHADER + "." + SPECULAR_COLOR, Color.WHITE);
        circles2App.setAttribute(LINE_SHADER + "." + SPECULAR_EXPONENT, 30);
        circles2App.setAttribute(FACE_DRAW, false);
        circles2App.setAttribute(TUBES_DRAW, true);
        circles2App.setAttribute(TUBE_RADIUS, circles2Width);
        circles2App.setAttribute(VERTEX_DRAW, false);
        circles2App.setAttribute(SPHERES_DRAW, false);
        circles2App.setAttribute(POINT_RADIUS, circles2Width);
        circles2App.setAttribute(BACK_FACE_CULLING_ENABLED, false);  
        circles2Root.setAppearance(circles2App);
        circles2Root.setGeometry(circles2Geometry);
        circles2Root.setVisible(showCircles2);
        geometryRoot.addChild(circles2Root);
        
        //polyeder
        polyhedronApp.setAttribute(POLYGON_SHADER, "flat");
        polyhedronApp.setAttribute(POLYGON_SHADER + "." + DIFFUSE_COLOR, faceColor);
        polyhedronApp.setAttribute(POLYGON_SHADER + "." + SPECULAR_COLOR, Color.WHITE);
        polyhedronApp.setAttribute(POLYGON_SHADER + "." + SPECULAR_EXPONENT, 30);
        polyhedronApp.setAttribute(POLYGON_SHADER + "." + SPECULAR_COEFFICIENT, 0.7);
        polyhedronApp.setAttribute(POLYGON_SHADER + "." + AMBIENT_COLOR, faceColor);
        polyhedronApp.setAttribute(POLYGON_SHADER + "." + AMBIENT_COEFFICIENT, 0.4);
        polyhedronApp.setAttribute(TRANSPARENCY, transparencyPolyederValue);
        polyhedronApp.setAttribute(TRANSPARENCY_ENABLED, transparencyPolyeder);
        polyhedronApp.setAttribute(BACK_FACE_CULLING_ENABLED, true);
        polyhedronApp.setAttribute(TUBES_DRAW, false);
        polyhedronApp.setAttribute(VERTEX_DRAW, false);
        polyhedronApp.setAttribute(SPHERES_DRAW, false);
        polyhedronRoot.setAppearance(polyhedronApp);
        polyhedronRoot.setGeometry(polyhedronGeometry);
        polyhedronRoot.setVisible(showPolyeder);
        geometryRoot.addChild(polyhedronRoot);
        
        //sphere
        sphereApp.setAttribute(POLYGON_SHADER, "smooth");
        sphereApp.setAttribute(POLYGON_SHADER + "." + DIFFUSE_COLOR, sphereColor);
        sphereApp.setAttribute(POLYGON_SHADER + "." + AMBIENT_COLOR, sphereColor);
        sphereApp.setAttribute(POLYGON_SHADER + "." + AMBIENT_COEFFICIENT, 0.4);
        sphereApp.setAttribute(POLYGON_SHADER + "." + SPECULAR_COLOR, Color.WHITE);
        sphereApp.setAttribute(POLYGON_SHADER + "." + SPECULAR_EXPONENT, 30);
        sphereApp.setAttribute(POLYGON_SHADER + "." + SPECULAR_COEFFICIENT, 0.7);
        sphereApp.setAttribute(VERTEX_DRAW, false);
        sphereApp.setAttribute(TRANSPARENCY, transparencySphereValue);
        sphereApp.setAttribute(EDGE_DRAW, false);
        sphereApp.setAttribute(TRANSPARENCY_ENABLED, transparencySphere);
        sphereApp.setAttribute(BACK_FACE_CULLING_ENABLED, true);
//        sphereApp.setAttribute(CommonAttributes.DEPTH_FUDGE_FACTOR, 10);
        sphereRoot = SphereUtility.tessellatedCubeSphere(30);
        sphereRoot.setAppearance(sphereApp);
        sphereRoot.setVisible(showSphere);
        geometryRoot.addChild(sphereRoot);
        
        //rotate tool
        geometryRoot.addTool(new RotateTool());
        geometryRoot.addTool(new DraggingTool());
        geometryRoot.addTool(new EncompassTool());
        
        //pick path
	    SceneGraphPath pickPath = new SceneGraphPath();
	    pickPath.push(sceneRoot);
	    pickPath.push(geometryRoot);
	    
	    va = new ViewerApp(sceneRoot, cameraPath, pickPath, cameraPath);
	    va.update();
	}
	

	public void updateProperties(){
		getViewer().render();
	}
	
	
	public void resetGeometry(){
		polyhedronRoot.setGeometry(null);
		meshRoot.setGeometry(null);
		circles1Root.setGeometry(null);
		circles2Root.setGeometry(null);
	}
	
	
	public void updateGeometry(KoebePolyhedronContext<CPVertex, CPEdge, CPFace> context){
		updatePolyhedron(context);
		updateCircles(context);
		getViewer().render();
	}
	
	
	private void updatePolyhedron(KoebePolyhedronContext<CPVertex, CPEdge, CPFace> context){
		if (context == null){
			return;
		}

		double[][] vertexData = new double[context.getPolyeder().getNumVertices()][];
		for (CPVertex v : context.getPolyeder().getVertices()){
			vertexData[v.getIndex()] = new double[]{v.getXYZW().x / v.getXYZW().w, v.getXYZW().y / v.getXYZW().w, v.getXYZW().z / v.getXYZW().w};
		}
		int[][] indexData = new int[context.getPolyeder().getNumFaces()][];
		for (CPFace face : context.getPolyeder().getFaces()){
			LinkedList<Integer> indexList = new LinkedList<Integer>();
			CPEdge firstEdge = face.getBoundaryEdge();
			CPEdge actEdge = firstEdge;
			do {
				indexList.add(actEdge.getTargetVertex().getIndex());
				actEdge = actEdge.getNextEdge();
			} while (actEdge != firstEdge);
			
			int[] faceIndices = new int[indexList.size()];
			for (int i = 0; i < indexList.size(); i++) {
				faceIndices[i] = indexList.get(i);	
			}
			indexData[face.getIndex()] = faceIndices;
		}
		polyhedronFactory = new IndexedFaceSetFactory();
		polyhedronFactory.setVertexCount(vertexData.length);
		polyhedronFactory.setFaceCount(indexData.length);
		polyhedronFactory.setGenerateEdgesFromFaces(false);
		polyhedronFactory.setGenerateFaceNormals(true);
		polyhedronFactory.setVertexCoordinates(vertexData);
		polyhedronFactory.setFaceIndices(indexData);
		polyhedronFactory.update();
		polyhedronRoot.setGeometry(polyhedronFactory.getIndexedFaceSet());
		polyhedronGeometry = polyhedronFactory.getIndexedFaceSet();
		polyhedronGeometry.setGeometryAttributes("pickable", false);
		
		meshFactory = new IndexedFaceSetFactory();
		meshFactory.setVertexCount(vertexData.length);
		meshFactory.setFaceCount(indexData.length);
		meshFactory.setGenerateEdgesFromFaces(true);
		meshFactory.setGenerateFaceNormals(true);
		meshFactory.setVertexCoordinates(vertexData);
		meshFactory.setFaceIndices(indexData);
		meshFactory.update();	
		meshRoot.setGeometry(meshFactory.getIndexedFaceSet());
		meshgeometry = meshFactory.getIndexedFaceSet();
		meshgeometry.setGeometryAttributes("pickable", false);
	}

	
	
	private Point4d sphereMirror(Point4d p){
		double lengthSqr = (p.x*p.x + p.y*p.y + p.z*p.z ) / p.w*p.w;
		if (lengthSqr == 0)
			return new Point4d(0, 0, 0, 1);
		return new Point4d(p.x , p.y , p.z , p.w * lengthSqr);
	}
	
	
	
	
	private void updateCircles(KoebePolyhedronContext<CPVertex, CPEdge, CPFace> context){
		if (context == null){
			return;
		}
		int res = 50;
		int index = 0;
		double[][] vertexdata = new double[context.getPolyeder().getNumVertices() * res][];
		int[][] indexdata = new int[context.getPolyeder().getNumVertices()][res + 1];
		try{
			for (CPVertex vertex : context.getPolyeder().getVertices()){
				VecmathTools.dehomogenize(vertex.getXYZW());
				CPEdge edge = vertex.getConnectedEdge();
				CPVertex v = context.getEdgeVertexMap().get(edge);
				if (!v.isValid())
					v = context.getNorthPole();
				Point4d conePeak = vertex.getXYZW();
				Point4d center = sphereMirror(conePeak);
				List<Point4d> pList = Circles.getCircle(center, conePeak, v.getXYZW(), res);
				int index2 = 0;
				indexdata[index][res] = index * res + index2;
				for (Point4d p : pList){
					vertexdata[index * res + index2] = new double[]{p.x, p.y, p.z};
					indexdata[index][index2] = index * res + index2;
					index2++;
				}
				index++;
			}
	
			IndexedLineSet ils = circles1Factory.getIndexedFaceSet();
			ils.setVertexCountAndAttributes(Attribute.COORDINATES, StorageModel.DOUBLE3_ARRAY.createReadOnly(vertexdata));
			ils.setEdgeCountAndAttributes(Attribute.INDICES, StorageModel.INT_ARRAY_ARRAY.createReadOnly(indexdata));
			circles1Root.setGeometry(circles1Geometry);
		} catch (Exception e){
			e.printStackTrace();
		}
		
		try{
			vertexdata = new double[context.getPolyeder().getNumFaces() * res][];
			indexdata = new int[context.getPolyeder().getNumFaces()][res + 1];
			index = 0;
			for (CPFace face : context.getPolyeder().getFaces()){
				VecmathTools.dehomogenize(face.getXYZW());
				CPEdge edge = face.getBoundaryEdge();
				CPVertex v = context.getEdgeEdgeMap().get(edge).getTargetVertex();
				if (v == null)
					v = context.getNorthPole();
				Point4d conePeak = face.getXYZW();
				Point4d center = sphereMirror(conePeak);
				List<Point4d> pList = Circles.getCircle(center, conePeak, v.getXYZW(), res);
				int index2 = 0;
				indexdata[index][res] = index * res + index2;
				for (Point4d p : pList){
					vertexdata[index * res + index2] = new double[]{p.x, p.y, p.z};
					indexdata[index][index2] = index * res + index2;
					index2++;
				}
				index++;
			}
			IndexedLineSet ils = circles2Factory.getIndexedFaceSet();
			ils.setVertexCountAndAttributes(Attribute.COORDINATES, StorageModel.DOUBLE3_ARRAY.createReadOnly(vertexdata));
			ils.setEdgeCountAndAttributes(Attribute.INDICES, StorageModel.INT_ARRAY_ARRAY.createReadOnly(indexdata));
			circles2Root.setGeometry(circles2Geometry);
		} catch (Exception e){
			e.printStackTrace();
		}
		
	}
	
	
	
	public de.jreality.scene.Viewer getViewer(){
		return va.getCurrentViewer();
	}


	public void update() {
		getViewer().render();
	}
	
	public JComponent getViewerComponent() {
		return this;
	}

	
	public IndexedFaceSet getPolyhedron() {
		return polyhedronFactory.getIndexedFaceSet();
	}
	
	public void setPolyhedron(IndexedFaceSet ifs) {
		polyhedronGeometry = ifs;
		polyhedronGeometry.setGeometryAttributes("pickable", false);
		polyhedronRoot.setGeometry(polyhedronGeometry);
	}

	
	public boolean isAntialias() {
		return antialias;
	}


	public Color getCircles1Color() {
		return circles1Color;
	}


	public double getCircles1Width() {
		return circles1Width;
	}


	public Color getCircles2Color() {
		return circles2Color;
	}


	public double getCircles2Width() {
		return circles2Width;
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


	public boolean isShowCircles() {
		return showCircles;
	}


	public boolean isShowCircles2() {
		return showCircles2;
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


	public boolean isShowSphere() {
		return showSphere;
	}


	public Color getSphereColor() {
		return sphereColor;
	}


	public boolean isTransparencyPolyeder() {
		return transparencyPolyeder;
	}


	public double getTransparencyPolyederValue() {
		return transparencyPolyederValue;
	}


	public boolean isTransparencySphere() {
		return transparencySphere;
	}


	public double getTransparencySphereValue() {
		return transparencySphereValue;
	}


	public void setAntialias(boolean antialias) {
		this.antialias = antialias;
		sceneRoot.getAppearance().setAttribute(ANTIALIASING_ENABLED, antialias);
	}


	public void setCircles1Color(Color circles1Color) {
		this.circles1Color = circles1Color;
        circles1App.setAttribute(LINE_SHADER + "." + DIFFUSE_COLOR, circles1Color);
	}


	public void setCircles1Width(double circles1Width) {
		this.circles1Width = circles1Width;
	      circles1App.setAttribute(TUBE_RADIUS, circles1Width);
	}


	public void setCircles2Color(Color circles2Color) {
		this.circles2Color = circles2Color;
	      circles2App.setAttribute(LINE_SHADER + "." + DIFFUSE_COLOR, circles2Color);
	}


	public void setCircles2Width(double circles2Width) {
		this.circles2Width = circles2Width;
        circles2App.setAttribute(TUBE_RADIUS, circles2Width);
	}


	public void setDefaultLightIntensity(double defaultLightIntensity) {
		this.defaultLightIntensity = defaultLightIntensity;
	}


	public void setFaceColor(Color faceColor) {
		this.faceColor = faceColor;
		polyhedronApp.setAttribute(POLYGON_SHADER + "." + DIFFUSE_COLOR, faceColor);
        polyhedronApp.setAttribute(POLYGON_SHADER + "." + AMBIENT_COLOR, faceColor);
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


	public void setMeshWidth(double meshWidth) {
		this.meshWidth = meshWidth;
        meshApp.setAttribute(TUBE_RADIUS, meshWidth);
        meshApp.setAttribute(POINT_RADIUS, meshWidth);
	}


	public void setShowCircles(boolean showCircles) {
		this.showCircles = showCircles;
		circles1Root.setVisible(showCircles);
	}


	public void setShowCircles2(boolean showCircles2) {
		this.showCircles2 = showCircles2;
		circles2Root.setVisible(showCircles2);
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
		polyhedronRoot.setVisible(showPolyeder);
	}


	public void setShowSphere(boolean showSphere) {
		this.showSphere = showSphere;
		sphereRoot.setVisible(showSphere);
	}


	public void setSphereColor(Color sphereColor) {
		this.sphereColor = sphereColor;
        sphereApp.setAttribute(POLYGON_SHADER + "." + DIFFUSE_COLOR, sphereColor);
        sphereApp.setAttribute(POLYGON_SHADER + "." + AMBIENT_COLOR, sphereColor);
	}


	public void setTransparencyPolyeder(boolean transparencyPolyeder) {
		this.transparencyPolyeder = transparencyPolyeder;
	}


	public void setTransparencyPolyederValue(double transparencyPolyederValue) {
		this.transparencyPolyederValue = transparencyPolyederValue;
        polyhedronApp.setAttribute(TRANSPARENCY, transparencyPolyederValue);
        polyhedronApp.setAttribute(TRANSPARENCY_ENABLED, transparencyPolyederValue != 0);
	}


	public void setTransparencySphere(boolean transparencySphere) {
		this.transparencySphere = transparencySphere;
	}


	public void setTransparencySphereValue(double transparencySphereValue) {
		this.transparencySphereValue = transparencySphereValue;
	    sphereApp.setAttribute(TRANSPARENCY, transparencySphereValue);
	    sphereApp.setAttribute(TRANSPARENCY_ENABLED, transparencySphereValue != 0);
	}


	public MainController getController() {
		return controller;
	}


	public SceneGraphComponent getSceneRoot() {
		return sceneRoot;
	}


	public Color getBackgoundColor() {
		return backgoundColor;
	}


	public void setBackgoundColor(Color backgoundColor) {
		this.backgoundColor = backgoundColor;
		rootApp.setAttribute(BACKGROUND_COLOR, backgoundColor);
	}


	
}