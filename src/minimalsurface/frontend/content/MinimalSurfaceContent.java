package minimalsurface.frontend.content;

import static de.jreality.shader.CommonAttributes.AMBIENT_COEFFICIENT;
import static de.jreality.shader.CommonAttributes.AMBIENT_COLOR;
import static de.jreality.shader.CommonAttributes.ANTIALIASING_ENABLED;
import static de.jreality.shader.CommonAttributes.BACKGROUND_COLOR;
import static de.jreality.shader.CommonAttributes.BACK_FACE_CULLING_ENABLED;
import static de.jreality.shader.CommonAttributes.DEPTH_FUDGE_FACTOR;
import static de.jreality.shader.CommonAttributes.DIFFUSE_COLOR;
import static de.jreality.shader.CommonAttributes.EDGE_DRAW;
import static de.jreality.shader.CommonAttributes.FACE_DRAW;
import static de.jreality.shader.CommonAttributes.LIGHTING_ENABLED;
import static de.jreality.shader.CommonAttributes.LINE_SHADER;
import static de.jreality.shader.CommonAttributes.LINE_WIDTH;
import static de.jreality.shader.CommonAttributes.PICKABLE;
import static de.jreality.shader.CommonAttributes.POINT_RADIUS;
import static de.jreality.shader.CommonAttributes.POINT_SHADER;
import static de.jreality.shader.CommonAttributes.POINT_SIZE;
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
import static de.jreality.writer.u3d.U3DAttribute.U3D_FLAG;
import static de.jreality.writer.u3d.U3DAttribute.U3D_NONORMALS;
import static halfedge.decorations.HasQuadGraphLabeling.QuadGraphLabel.INTERSECTION;
import static halfedge.decorations.HasQuadGraphLabeling.QuadGraphLabel.SPHERE;
import static minimalsurface.frontend.content.MinimalViewOptions.CircleType.Disk;
import static minimalsurface.frontend.content.MinimalViewOptions.CircleType.Ring;
import halfedge.Edge;
import halfedge.Face;
import halfedge.HalfEdgeDataStructure;
import halfedge.Vertex;
import halfedge.decorations.HasQuadGraphLabeling;
import halfedge.decorations.HasRadius;
import halfedge.decorations.HasXYZW;

import java.awt.Color;
import java.util.List;

import javax.vecmath.Point4d;
import javax.vecmath.Vector3d;

import math.util.Circles;
import math.util.VecmathTools;
import minimalsurface.controller.MainController;
import minimalsurface.frontend.content.MinimalViewOptions.CircleType;
import util.debug.DBGTracer;
import circlepatterns.graph.CPEdge;
import circlepatterns.graph.CPFace;
import circlepatterns.graph.CPVertex;
import de.jreality.geometry.IndexedFaceSetFactory;
import de.jreality.geometry.IndexedLineSetFactory;
import de.jreality.geometry.Primitives;
import de.jreality.math.FactoredMatrix;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.plugin.JRViewerUtility;
import de.jreality.plugin.basic.Scene;
import de.jreality.plugin.content.DirectContent;
import de.jreality.scene.Appearance;
import de.jreality.scene.Geometry;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.Light;
import de.jreality.scene.PointLight;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphVisitor;
import de.jreality.scene.Sphere;
import de.jreality.scene.Transformation;
import de.jreality.scene.tool.Tool;
import de.jtem.jrworkspace.plugin.Controller;


/**
 * An jReality viewer for polyhedra
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 * 
 * @param <V> Vertex class this view can display
 * @param <E> Edge class this view can display
 * @param <F> Face class this view can display
 */
public class MinimalSurfaceContent extends DirectContent {

	@SuppressWarnings("unused")
	private MainController
		controller = null;
	private Scene
		scene = null;
	
	//geometry
	private SceneGraphComponent 
		sceneRoot = new SceneGraphComponent(),
		polyhedronRoot = new SceneGraphComponent(),
		geometryRoot = new SceneGraphComponent(),
		linesRoot = new SceneGraphComponent(),
		unitSphereRoot = new SceneGraphComponent(),
		activeDisksRoot = new SceneGraphComponent(),
		activeSpheresRoot = new SceneGraphComponent();
	private Transformation
		diskThicknessTransform = new Transformation();
	private IndexedFaceSet
		activeFaceSet = null;
	private Object
		surfaceMaster = new Object();
	
	//tools
//	private RotateTool 
//		rotateTool = new RotateTool();
//	private DraggingTool
//		draggingTool = new DraggingTool();
//	private EncompassTool
//		encompassTool = new EncompassTool();
//	private ClickWheelCameraZoomTool
//		zoomTool = new ClickWheelCameraZoomTool();
	private Tool
		activeGeometryTool = null;
	
	//properties
    private Light 
    	light1 = new PointLight(),
    	light2 = new PointLight(),
    	defaultLight = new PointLight();
	
    private Color
    	faceColor = new Color(102, 102, 102),
		rootColor = new Color(66, 66, 33),
		light1Color = Color.WHITE,
		light2Color = Color.WHITE,
		meshColor = Color.BLACK,
		circlesColor = new Color(0, 51, 102),
		spheresColor = new Color(102, 102, 0),
		backgroundColor = new Color(0xffffff);
	
	private Appearance 
		surfaceAppearance = new Appearance(),
		unitSphereApp = new Appearance(),
		diskApp = new Appearance(),
		spheresApp = new Appearance(),
		rootApp = new Appearance(),
		linesApp = new Appearance();
	
	private boolean
		antialias = true,
		showSurface = true,
		showMesh = true,
		showCircles = true,
		showSpheres = false,
		transparencySurface = false,
		light1On = true,
		light2On = true,
		smoothShading = false;
	
	private double
		transparencySurfaceValue = 0.0,
		light1intensity = 8,
		light2intensity = 8,
		defaultLightIntensity = 1,
		meshWidth = 0.015,
		diskThickness = 0.01;
	
	private CircleType
		circleType = CircleType.Disk;

	private Disk
		diskGeometry = new Disk(40, 1.0);
	private Ring
		ringGeometry = new Ring();
	private IndexedFaceSet
		circleGeometry = new IndexedFaceSet();
	private Geometry
		sphereGeometry = new Sphere();

	private HalfEdgeDataStructure<CPVertex, CPEdge, CPFace>
		activeSurface = null;
	
	
	public MinimalSurfaceContent(MainController controller){
		this.controller = controller;
		initScene();
	}
	
	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		this.scene = c.getPlugin(Scene.class);
		setContent(sceneRoot);
	}

	
	private void initScene(){
		//root appearance
		rootApp.setAttribute(BACKGROUND_COLOR, backgroundColor);
//		rootApp.setAttribute(POLYGON_SHADER + "." + ANTIALIASING_ENABLED, antialias);
		sceneRoot.setAppearance(rootApp);
		sceneRoot.setName("Scene Root");
		
        //geometry node
        geometryRoot.setName("Geometry");
		sceneRoot.addChild(geometryRoot);
		
		//unit sphere
		unitSphereRoot.setName("Unit Sphere");
		unitSphereRoot.setAppearance(unitSphereApp);
		unitSphereRoot.setGeometry(Primitives.sphere(100));
		unitSphereRoot.setVisible(false);
		unitSphereApp.setAttribute(VERTEX_DRAW, false);
		unitSphereApp.setAttribute(EDGE_DRAW, false);
		unitSphereApp.setAttribute(FACE_DRAW, true);
		unitSphereApp.setAttribute(TRANSPARENCY_ENABLED, true);
		unitSphereApp.setAttribute(BACK_FACE_CULLING_ENABLED, true);
		unitSphereApp.setAttribute(POLYGON_SHADER + "." + TRANSPARENCY, 0.5);
		sceneRoot.addChild(unitSphereRoot);
        
        //polyhedron
		surfaceAppearance.setAttribute(FACE_DRAW, true);
		surfaceAppearance.setAttribute(POLYGON_SHADER, "smooth");
        surfaceAppearance.setAttribute(POLYGON_SHADER + "." + SMOOTH_SHADING, smoothShading);
        surfaceAppearance.setAttribute(POLYGON_SHADER + "." + DIFFUSE_COLOR, faceColor);
        surfaceAppearance.setAttribute(POLYGON_SHADER + "." + SPECULAR_COLOR, Color.WHITE);
        surfaceAppearance.setAttribute(POLYGON_SHADER + "." + SPECULAR_EXPONENT, 30);
        surfaceAppearance.setAttribute(POLYGON_SHADER + "." + SPECULAR_COEFFICIENT, 0.7);
        surfaceAppearance.setAttribute(POLYGON_SHADER + "." + AMBIENT_COLOR, faceColor);
        surfaceAppearance.setAttribute(POLYGON_SHADER + "." + AMBIENT_COEFFICIENT, 0.4);
        surfaceAppearance.setAttribute(TRANSPARENCY, transparencySurfaceValue);
        surfaceAppearance.setAttribute(TRANSPARENCY_ENABLED, transparencySurface);
        surfaceAppearance.setAttribute(POLYGON_SHADER + "." + BACK_FACE_CULLING_ENABLED, true);
        
        surfaceAppearance.setAttribute(VERTEX_DRAW, false);
        surfaceAppearance.setAttribute(POINT_SHADER + "." + SMOOTH_SHADING, true);
        surfaceAppearance.setAttribute(POINT_SHADER + "." + SPHERES_DRAW, true);
        surfaceAppearance.setAttribute(POINT_SHADER + "." + POINT_RADIUS, meshWidth * 3);
        surfaceAppearance.setAttribute(POINT_SHADER + "." + POINT_SIZE, 1.0);
        surfaceAppearance.setAttribute(POINT_SHADER + "." + DIFFUSE_COLOR, Color.WHITE);
        surfaceAppearance.setAttribute(POINT_SHADER + "." + SPECULAR_COLOR, Color.WHITE);
        surfaceAppearance.setAttribute(POINT_SHADER + "." + SPECULAR_EXPONENT, 30);
        surfaceAppearance.setAttribute(POINT_SHADER + "." + TRANSPARENCY_ENABLED, false);
        
        surfaceAppearance.setAttribute(EDGE_DRAW, true);
        surfaceAppearance.setAttribute(LINE_SHADER + "." + SMOOTH_SHADING, true);
        surfaceAppearance.setAttribute(LINE_SHADER + "." + TUBES_DRAW, true);
        surfaceAppearance.setAttribute(LINE_SHADER + "." + TUBE_RADIUS, meshWidth);
        surfaceAppearance.setAttribute(LINE_SHADER + "." + DEPTH_FUDGE_FACTOR, .99999);
        surfaceAppearance.setAttribute(LINE_SHADER + "." + DIFFUSE_COLOR, meshColor);
        surfaceAppearance.setAttribute(LINE_SHADER + "." + SPECULAR_COLOR, Color.WHITE);
        surfaceAppearance.setAttribute(LINE_SHADER + "." + SPECULAR_EXPONENT, 30);
        surfaceAppearance.setAttribute(LINE_SHADER + "." + TRANSPARENCY_ENABLED, false);

        polyhedronRoot.setAppearance(surfaceAppearance);
        polyhedronRoot.setVisible(showSurface);
        polyhedronRoot.setName("Surface Root");
        geometryRoot.addChild(polyhedronRoot);
        
        // disks
        diskApp.setAttribute(POLYGON_SHADER, "flat");
        diskApp.setAttribute(POLYGON_SHADER + "." + DIFFUSE_COLOR, rootColor);
        diskApp.setAttribute(POLYGON_SHADER + "." + SPECULAR_COLOR, Color.WHITE);
        diskApp.setAttribute(POLYGON_SHADER + "." + SPECULAR_EXPONENT, 30);
        diskApp.setAttribute(POLYGON_SHADER + "." + SPECULAR_COEFFICIENT, 0.7);
        diskApp.setAttribute(POLYGON_SHADER + "." + AMBIENT_COLOR, circlesColor);
        diskApp.setAttribute(POLYGON_SHADER + "." + AMBIENT_COEFFICIENT, 0.4);
        diskApp.setAttribute(POLYGON_SHADER + "." + SMOOTH_SHADING, true);
        diskApp.setAttribute(POLYGON_SHADER + "." + BACK_FACE_CULLING_ENABLED, true);
        diskApp.setAttribute(FACE_DRAW, showCircles);
        diskApp.setAttribute(VERTEX_DRAW, false);
        diskApp.setAttribute(EDGE_DRAW, false);
        diskApp.setAttribute(LIGHTING_ENABLED, true);
        diskApp.setAttribute(TRANSPARENCY_ENABLED, false);
        diskApp.setAttribute(PICKABLE, false);
        setCircleType(Disk);
        
        // spheres
        spheresApp.setAttribute(POLYGON_SHADER, "smooth");
        spheresApp.setAttribute(POLYGON_SHADER + "." + SMOOTH_SHADING, true);
        spheresApp.setAttribute(POLYGON_SHADER + "." + BACK_FACE_CULLING_ENABLED, true);
		spheresApp.setAttribute(POLYGON_SHADER + "." + DIFFUSE_COLOR, rootColor);
		spheresApp.setAttribute(POLYGON_SHADER + "." + SPECULAR_COLOR, Color.WHITE);
		spheresApp.setAttribute(POLYGON_SHADER + "." + SPECULAR_EXPONENT, 30);
		spheresApp.setAttribute(POLYGON_SHADER + "." + SPECULAR_COEFFICIENT, 0.7);
		spheresApp.setAttribute(POLYGON_SHADER + "." + AMBIENT_COLOR, spheresColor);
		spheresApp.setAttribute(POLYGON_SHADER + "." + AMBIENT_COEFFICIENT, 0.4);
		spheresApp.setAttribute(TRANSPARENCY, 0.5);
		spheresApp.setAttribute(TRANSPARENCY_ENABLED, true);
		spheresApp.setAttribute(FACE_DRAW, showSpheres);
		spheresApp.setAttribute(VERTEX_DRAW, false);
		spheresApp.setAttribute(EDGE_DRAW, false);
		spheresApp.setAttribute(LIGHTING_ENABLED, true);
		spheresApp.setAttribute(PICKABLE, false);
        
        // disk thickness
		Matrix S = MatrixBuilder.euclidean().scale(1, 1, diskThickness).getMatrix();
		S.assignTo(diskThicknessTransform);
		
        // helper lines
        linesRoot.setName("Helper Lines");
        linesRoot.setAppearance(linesApp);
        linesApp.setAttribute(VERTEX_DRAW, false);
        linesApp.setAttribute(EDGE_DRAW, true);
        linesApp.setAttribute(LINE_SHADER + "." + DIFFUSE_COLOR, Color.WHITE);
        linesApp.setAttribute(LINE_SHADER + "." + SPECULAR_COLOR, Color.WHITE);
        linesApp.setAttribute(LINE_SHADER + "." + TUBES_DRAW, true);
        linesApp.setAttribute(LINE_SHADER + "." + TUBE_RADIUS, meshWidth * 3);
        geometryRoot.addChild(linesRoot);
        
//        // tools
//        sceneRoot.addTool(encompassTool);
//        sceneRoot.addTool(rotateTool);
//        sceneRoot.addTool(draggingTool);
//        sceneRoot.addTool(zoomTool);
	}
	

	public void setGeometryTool(Tool geometryTool){
		if (activeGeometryTool != null) {
			geometryRoot.removeTool(activeGeometryTool);
		}
		geometryRoot.addTool(geometryTool);
		activeGeometryTool = geometryTool;
	}
	
	
	public void addLineGeometry(double[] start, double[] end) {
		IndexedLineSetFactory ilsf = new IndexedLineSetFactory();
		ilsf.setVertexCount(2);
		ilsf.setEdgeCount(1);
		ilsf.setVertexCoordinates(new double[][] {start, end});
		ilsf.setEdgeIndices(new int[] {0, 1});
		ilsf.update();
		SceneGraphComponent c = new SceneGraphComponent();
		c.setGeometry(ilsf.getGeometry());
		linesRoot.addChild(c);
	}
	
	public HalfEdgeDataStructure<CPVertex, CPEdge, CPFace> getActiveSurface(){
		return activeSurface;
	}
	
	public void addSurface(HalfEdgeDataStructure<CPVertex, CPEdge, CPFace> surface){
		double[][] vertexData = new double[surface.getNumVertices()][];
		for (CPVertex v : surface.getVertices()){
			VecmathTools.dehomogenize(v.getXYZW());
			if (VecmathTools.isNAN(v.getXYZW())){
				v.getXYZW().set(0, 0, 0, 1);
				DBGTracer.msg("NaN in viewSurface() changed to 0.0!");
			}
			double[] p = new double[]{v.getXYZW().x, v.getXYZW().y, v.getXYZW().z};
			vertexData[v.getIndex()] = p;
		}
		int[][] faceData = new int[surface.getNumFaces()][];
		double[][] faceVertexData = new double[surface.getNumFaces()][];
		for (CPFace f : surface.getFaces()){
			List<CPEdge> b = f.getBoundary();
			faceData[f.getIndex()] = new int[b.size()];
			int counter = 0;
			for (CPEdge e : b){
				faceData[f.getIndex()][counter] = e.getTargetVertex().getIndex();
				counter++;
			}
			//face vertex
			VecmathTools.dehomogenize(f.getXYZW());
			double[] p = new double[]{f.getXYZW().x, f.getXYZW().y, f.getXYZW().z};
			faceVertexData[f.getIndex()] = p;
		}
		
		IndexedFaceSetFactory surfaceFactory = new IndexedFaceSetFactory();
		surfaceFactory.setVertexCount(vertexData.length);
		surfaceFactory.setFaceCount(faceData.length);
		surfaceFactory.setVertexCoordinates(vertexData);
		surfaceFactory.setFaceIndices(faceData);
		surfaceFactory.setGenerateVertexNormals(true);
		surfaceFactory.setGenerateFaceNormals(true);
		surfaceFactory.setGenerateEdgesFromFaces(true);
		surfaceFactory.update();
	
		SceneGraphComponent surfaceRoot = new SceneGraphComponent();
		surfaceRoot.setName("Surface");
        surfaceRoot.setGeometry(surfaceFactory.getIndexedFaceSet());
        surfaceRoot.setOwner(surfaceMaster);
        activeFaceSet = surfaceFactory.getIndexedFaceSet();
        activeFaceSet.setVertexAttributes(U3D_NONORMALS, U3D_FLAG);
        
        polyhedronRoot.addChild(surfaceRoot);
        surfaceRoot.addChild(makeDiskSurface(surface));
		activeSurface = surface;
	}
	
	
	/**
	 * Constructs a surface made from disks. The centers of
	 * the disks are the vertices labeled <code>false</code>
	 * @param <V> The vertices with a labeling
	 * @param <E>
	 * @param <F>
	 * @param surface
	 */
	public <
		V extends Vertex<V, E, F> & HasXYZW & HasQuadGraphLabeling,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F> & HasXYZW & HasRadius
	> SceneGraphComponent makeDiskSurface(HalfEdgeDataStructure<V, E, F> surface){
		SceneGraphComponent disksSpheresRoot = new SceneGraphComponent();
		disksSpheresRoot.setName("Disks and Spheres");
		activeDisksRoot = new SceneGraphComponent();
		activeDisksRoot.setName("Disks");
		activeSpheresRoot = new SceneGraphComponent();
		activeSpheresRoot.setName("Spheres");
		activeDisksRoot.setAppearance(diskApp);
		activeSpheresRoot.setAppearance(spheresApp);
		disksSpheresRoot.addChild(activeDisksRoot);
		disksSpheresRoot.addChild(activeSpheresRoot);
		for (V v : surface.getVertices()){
			try {
				switch (v.getVertexLabel()){
					case CIRCLE:
						List<V> star = v.getVertexStar();
						Point4d C = v.getXYZW();
						Point4d P1 = null;
						Point4d P2 = null;
						if (star.size() < 2)
							continue;
						for (int i = 0; i < star.size(); i++) {
							if (star.get(i).getVertexLabel() == INTERSECTION) {
								P1 = star.get(i).getXYZW();
								break;
							}
						}
						F f = v.getFaceStar().get(0);
						for (E b : f.getBoundary()) {
							V bv = b.getTargetVertex();
							if (bv.getVertexLabel() == SPHERE) {
								P2 = bv.getXYZW();
							}
						}
						if (P1 == null || P2 == null) {
							continue;
						}
						Point4d v1 = new Point4d(P1);
						Point4d v2 = new Point4d(P2);
						v1.sub(C); v1.w = 1.0;
						v2.sub(C); v2.w = 1.0;
						
						Point4d n = VecmathTools.cross(v1, v2);
						VecmathTools.dehomogenize(n);
						Vector3d N = new Vector3d(n.x, n.y, n.z);
						Double r = C.distance(P1);
						addDisk(activeDisksRoot, C, N, r);
						break;
					case SPHERE:
						Point4d c = v.getXYZW();
						VecmathTools.dehomogenize(c);
						Point4d p = v.getEdgeStar().get(0).getStartVertex().getXYZW();
						VecmathTools.dehomogenize(p);
						double radius = VecmathTools.distance(c, p);
						SceneGraphComponent sphere = new SceneGraphComponent();
						sphere.setName("Sphere");
						sphere.setGeometry(sphereGeometry);
						MatrixBuilder.euclidean().translate(c.x, c.y, c.z).scale(radius).assignTo(sphere);
						activeSpheresRoot.addChild(sphere);
						break;
					case INTERSECTION:
						break;
				}
			} catch (Exception e){
				System.err.println("Error");
			}
		}
		for (F f : surface.getFaces()) {
			double r = f.getRadius();
			if (r == 0.0) continue;
			Point4d C = f.getXYZW();
			VecmathTools.dehomogenize(C);
			E be = f.getBoundary().get(0);
			Point4d v0 = be.getStartVertex().getXYZW();
			Point4d v1 = be.getTargetVertex().getXYZW();
			VecmathTools.dehomogenize(v0);
			VecmathTools.dehomogenize(v1);
			Vector3d vec1 = new Vector3d(v0.x - C.x, v0.y - C.y, v0.z - C.z);
			Vector3d vec2 = new Vector3d(v1.x - C.x, v1.y - C.y, v1.z - C.z);
			Vector3d N = new Vector3d();
			N.cross(vec1, vec2);
			N.normalize();
			addDisk(activeDisksRoot, C, N, r);
		}
		return disksSpheresRoot;
	}
	
	
	private void addDisk(SceneGraphComponent root, Point4d C, Vector3d N, double r) {
		Matrix T = Circles.getTransform(C, N, r, circleType == Ring);
		SceneGraphComponent transformC = new SceneGraphComponent();
		transformC.setName("Circle Transform");
		T.assignTo(transformC);
		SceneGraphComponent disk = new SceneGraphComponent();
		disk.setName("Circle");
		disk.setTransformation(diskThicknessTransform);
		disk.setGeometry(circleGeometry);
		transformC.addChild(disk);
		root.addChild(transformC);
	}
	
	
	public void addGeometry(SceneGraphComponent c){
		polyhedronRoot.addChild(c);
	}
	
	
	public void removeGeometry(SceneGraphComponent c){
		if (polyhedronRoot.getChildNodes().contains(c))
			polyhedronRoot.removeChild(c);
	}
	
	
	public void resetGeometry(){
		while (polyhedronRoot.getChildComponentCount() > 0)
			polyhedronRoot.removeChild(polyhedronRoot.getChildComponent(0));
		while (linesRoot.getChildComponentCount() > 0)
			linesRoot.removeChild(linesRoot.getChildComponent(0));
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


	public boolean isShowMesh() {
		return showMesh;
	}


	public boolean isShowSurface() {
		return showSurface;
	}




	public boolean isTransparencySurface() {
		return transparencySurface;
	}

	
	
	

	public double getTransparencySurfaceValue() {
		return transparencySurfaceValue;
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
		surfaceAppearance.setAttribute(POLYGON_SHADER + "." + DIFFUSE_COLOR, faceColor);
        surfaceAppearance.setAttribute(POLYGON_SHADER + "." + AMBIENT_COLOR, faceColor);
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
        surfaceAppearance.setAttribute(LINE_SHADER + "." + DIFFUSE_COLOR, meshColor);
        //surfaceAppearance.setAttribute(POINT_SHADER + "." + DIFFUSE_COLOR, meshColor);
	}

	public void setSmoothShading(boolean smooth) {
		this.smoothShading = smooth;
//		polyederApp.setAttribute(POLYGON_SHADER, smooth ? "smooth" : "flat");
        surfaceAppearance.setAttribute(POLYGON_SHADER + "." + SMOOTH_SHADING, smoothShading);
	}
	
	public boolean isSmoothShading(){
		return smoothShading;
	}
	

	public void setMeshWidth(double meshWidth) {
		this.meshWidth = meshWidth;
        surfaceAppearance.setAttribute(LINE_SHADER + "." + TUBE_RADIUS, meshWidth);
        surfaceAppearance.setAttribute(LINE_SHADER + "." + LINE_WIDTH, meshWidth);
	}

	public void setShowMesh(boolean showMesh) {
		this.showMesh = showMesh;
		surfaceAppearance.setAttribute(EDGE_DRAW, showMesh);
	}
	
	public void setVertexSize(double size){
		surfaceAppearance.setAttribute(POINT_SHADER + "." + POINT_RADIUS, size);
		surfaceAppearance.setAttribute(POINT_SHADER + "." + POINT_SIZE, 10.0);
	}
	
	public double getVertexSize(){
		return (Double)surfaceAppearance.getAttribute(POINT_SHADER + "." + POINT_RADIUS);
	}
	

	public boolean isShowVertices(){
		return (Boolean)surfaceAppearance.getAttribute(VERTEX_DRAW);
	}
	
	
	public void setShowVertices(boolean show){
		surfaceAppearance.setAttribute(VERTEX_DRAW, show);
	}
	
	
	public void setShowSurface(boolean showPolyeder) {
		this.showSurface = showPolyeder;
		surfaceAppearance.setAttribute(FACE_DRAW, showPolyeder);
	}

	public void setTransparencySpheres(boolean transparencyPolyeder) {
		this.transparencySurface = transparencyPolyeder;
		spheresApp.setAttribute(TRANSPARENCY_ENABLED, transparencyPolyeder);
	}


	public void setTransparencySpheresValue(double transparencyPolyederValue) {
		setTransparencySpheres(transparencyPolyederValue > 0.0);
		spheresApp.setAttribute(TRANSPARENCY, transparencyPolyederValue);
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

	
	public IndexedFaceSet getGeometrySurface() {
		return activeFaceSet;
	}
	
//	public IndexedFaceSet getInvertedSurface() {
//		return invertedFaceSet;
//	}
//	
	
	public SceneGraphComponent getSceneRoot() {
		return sceneRoot;
	}

	public Color getCirclesColor() {
		return circlesColor;
	}


	public void setCirclesColor(Color circlesColor) {
//		diskApp.setAttribute(POLYGON_SHADER + "." + DIFFUSE_COLOR, circlesColor);
		diskApp.setAttribute(POLYGON_SHADER + "." + AMBIENT_COLOR, circlesColor);		
		this.circlesColor = circlesColor;
	}


	public Color getSpheresColor() {
		return spheresColor;
	}


	public void setSpheresColor(Color spheresColor) {
//		spheresApp.setAttribute(POLYGON_SHADER + "." + DIFFUSE_COLOR, spheresColor);
		spheresApp.setAttribute(POLYGON_SHADER + "." + AMBIENT_COLOR, spheresColor);		
		this.spheresColor = spheresColor;
	}

	
	public void setShowCircles(boolean show){
		diskApp.setAttribute(FACE_DRAW, show);
		showCircles = show;
	}
	
	public boolean isShowCircles(){
		return showCircles;
	}
	
	public void setShowSpheres(boolean show){
		spheresApp.setAttribute(FACE_DRAW, show);
		showSpheres = show;
	}
	
	public boolean isShowSpheres(){
		return showSpheres;
	}
	
	
	public void setShowHelperLines(boolean show) {
		linesRoot.setVisible(show);
	}
	
	public boolean isShowHelperLines() {
		return linesRoot.isVisible();
	}
	
	public void setHelperLineColor(Color color) {
        linesApp.setAttribute(LINE_SHADER + "." + DIFFUSE_COLOR, color);
	}
	
	public Color getHelperLinesColor() {
		return (Color)linesApp.getAttribute(LINE_SHADER + "." + DIFFUSE_COLOR);
	}
	
	public void setHelperLineWidth(double width) {
		linesApp.setAttribute(LINE_SHADER + "." + TUBE_RADIUS, width);
	}
	
	public double getHelperLineWidth() {
		return (Double)linesApp.getAttribute(LINE_SHADER + "." + TUBE_RADIUS);
	}

	public void setShowUnitSphere(boolean show) {
		unitSphereRoot.setVisible(show);
	}

	public double getDiskThickness() {
		return diskThickness;
	}


	public void setDiskThickness(double diskThickness) {
		this.diskThickness = diskThickness;
		switch (circleType) {
		case Disk:
			MatrixBuilder S = MatrixBuilder.euclidean();
			S.scale(1, 1, diskThickness);
			S.assignTo(diskThicknessTransform);
			break;
		case Ring:
			ringGeometry = new Ring(diskThickness, 40, 20);
			S = MatrixBuilder.euclidean();
			S.assignTo(diskThicknessTransform);
			setCircleType(Ring);
			break;
		case EqualRadiusRing:
//			S = MatrixBuilder.euclidean();
//			S.assignTo(diskThicknessTransform);
//			setCircleType(circleType);
			break;
		}
	}
	
	
	public Object getSurfaceOwner() {
		return surfaceMaster;
	}


	public CircleType getCircleType() {
		return circleType;
	}


	public void setCircleType(CircleType circleType) {
		CircleType oldType = this.circleType;
		switch (circleType) {
		default:
		case Disk:
		case Ring:
			if (oldType != CircleType.EqualRadiusRing) 
				break;
			activeDisksRoot.childrenWriteAccept(new SceneGraphVisitor() {
				@Override
				public void visit(SceneGraphComponent c) {
					if (c.getName().equals("Circle Transform")) {
						SceneGraphComponent circle = c.getChildComponent(0);
						circle.setGeometry(circleGeometry);
					} else {
						c.childrenWriteAccept(this, false, false, false, false, false, true);
					}
				}
			}, false, false, false, false, false, true);
			break;
		}
		this.circleType = circleType;
		IndexedFaceSet newGeom = null;
		switch (circleType) {
		case Disk:
			newGeom = diskGeometry;
			MatrixBuilder S = MatrixBuilder.euclidean();
			S.scale(1, 1, diskThickness);
			S.assignTo(diskThicknessTransform);
			break;
		case Ring:
			ringGeometry = new Ring(diskThickness, 40, 20);
			newGeom = ringGeometry;
			S = MatrixBuilder.euclidean();
			S.assignTo(diskThicknessTransform);
			break;
		case EqualRadiusRing:
			newGeom = circleGeometry;
			S = MatrixBuilder.euclidean();
			S.assignTo(diskThicknessTransform);
			activeDisksRoot.childrenWriteAccept(new SceneGraphVisitor() {
				@Override
				public void visit(SceneGraphComponent c) {
					if (c.getName().equals("Circle Transform")) {
						Transformation T = c.getTransformation();
						SceneGraphComponent circle = c.getChildComponent(0);
						FactoredMatrix fm = new FactoredMatrix(T);
						double scale = fm.getStretch()[0];
						circle.setGeometry(new Ring(diskThickness / scale, 40 , 20));
					} else {
						c.childrenWriteAccept(this, false, false, false, false, false, true);
					}
				}
			}, false, false, false, false, false, true);
			break;
		}
		circleGeometry.setVertexCountAndAttributes(newGeom.getVertexAttributes());
		circleGeometry.setEdgeCountAndAttributes(newGeom.getEdgeAttributes());
		circleGeometry.setFaceCountAndAttributes(newGeom.getFaceAttributes());
		circleGeometry.setName(newGeom.getName());
	}
	
	
	public void update() {
		
	}
	
	public void encompass() {
		JRViewerUtility.encompassEuclidean(scene);
	}
	
}
