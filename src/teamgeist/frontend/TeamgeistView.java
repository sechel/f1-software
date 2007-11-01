package teamgeist.frontend;

import static de.jreality.shader.CommonAttributes.AMBIENT_COEFFICIENT;
import static de.jreality.shader.CommonAttributes.AMBIENT_COLOR;
import static de.jreality.shader.CommonAttributes.ANTIALIASING_ENABLED;
import static de.jreality.shader.CommonAttributes.BACKGROUND_COLOR;
import static de.jreality.shader.CommonAttributes.BACK_FACE_CULLING_ENABLED;
import static de.jreality.shader.CommonAttributes.DIFFUSE_COLOR;
import static de.jreality.shader.CommonAttributes.EDGE_DRAW;
import static de.jreality.shader.CommonAttributes.LINE_SHADER;
import static de.jreality.shader.CommonAttributes.POLYGON_SHADER;
import static de.jreality.shader.CommonAttributes.SMOOTH_SHADING;
import static de.jreality.shader.CommonAttributes.SPECULAR_COEFFICIENT;
import static de.jreality.shader.CommonAttributes.SPECULAR_COLOR;
import static de.jreality.shader.CommonAttributes.SPECULAR_EXPONENT;
import static de.jreality.shader.CommonAttributes.SPHERES_DRAW;
import static de.jreality.shader.CommonAttributes.TRANSPARENCY_ENABLED;
import static de.jreality.shader.CommonAttributes.TUBES_DRAW;
import static de.jreality.shader.CommonAttributes.TUBE_RADIUS;
import static de.jreality.shader.CommonAttributes.VERTEX_DRAW;
import halfedge.HalfEdgeDataStructure;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.util.HashSet;

import javax.swing.JPanel;

import teamgeist.combinatorics.TextureCoordinateMap;
import teamgeist.data.Textures;
import teamgeist.frontend.controller.MainController;
import util.debug.DBGTracer;
import alexandrov.graph.CPMEdge;
import alexandrov.graph.CPMFace;
import alexandrov.graph.CPMVertex;
import de.jreality.geometry.IndexedFaceSetFactory;
import de.jreality.math.MatrixBuilder;
import de.jreality.scene.Appearance;
import de.jreality.scene.Camera;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.shader.DefaultGeometryShader;
import de.jreality.shader.DefaultPointShader;
import de.jreality.shader.DefaultTextShader;
import de.jreality.shader.ShaderUtility;
import de.jreality.ui.viewerapp.ViewerApp;
import de.jreality.util.CameraUtility;

public class TeamgeistView extends JPanel {

	private static final long 
		serialVersionUID = 1L;
	private HalfEdgeDataStructure<CPMVertex, CPMEdge, CPMFace> 
		teamgeist = null;
	private MainController 
		controller = null;
	
	private IndexedFaceSetFactory
		patchFactory = new IndexedFaceSetFactory(),
		rotorFactory = new IndexedFaceSetFactory();
	private IndexedFaceSet
		patchGeometry = patchFactory.getIndexedFaceSet(),
		rotorGeometry = rotorFactory.getIndexedFaceSet();
	private SceneGraphComponent
		root = new SceneGraphComponent(),
		geometryRoot = new SceneGraphComponent(),
		patchRoot = new SceneGraphComponent(),
		rotorRoot = new SceneGraphComponent(),
		cameraRoot = new SceneGraphComponent();
//		light1Root = new SceneGraphComponent(),
//		light2Root = new SceneGraphComponent(),
//		light3Root = new SceneGraphComponent();
	private ViewerApp
		va = new ViewerApp(root);
	private Color
		faceColor = new Color(0.4f, 0.4f, 0.4f);
//		light1Color = Color.WHITE,
//		light2Color = Color.WHITE;
//	private double
//		light1intensity = 4,
//		light2intensity = 4,
//		light3intensity = 2;
	private Appearance
		rootAppearance = new Appearance(),
		teamgeistAppearance = new Appearance();
//	private Tool
//		rotateTool = new RotateTool(),
//		encompassTool = new EncompassTool();
//	private PickSystem
//		pickSystem = new AABBPickSystem();
//	
//	private Light
//		light1 = new PointLight(),
//		light2 = new PointLight();
	private TextureCoordinateMap
		teamgeistCoords = new TextureCoordinateMap();
	
	private boolean
		softwareRendering = false;
//	
	private SceneGraphPath
		cameraPath = new SceneGraphPath();
	
	public TeamgeistView(MainController controller, boolean software){
		this.softwareRendering = software;
//		if (software)
//			viewer = new ToolSystemViewer(new DefaultViewer(false));
//		else
//			viewer = new ToolSystemViewer(new Viewer());
		this.controller = controller;
		setLayout(new BorderLayout());
		va.update();
		CameraUtility.getCamera(va.getCurrentViewer()).setFieldOfView(23);
		add((Component) va.getViewingComponent(), BorderLayout.CENTER);
		initScene();
	}
	
	public void encompass(){
//		SceneGraphPath scenePath = new SceneGraphPath();
//		scenePath.push(root);
//		MatrixBuilder.euclidean().assignTo(cameraRoot);
//		CameraUtility.encompass(cameraPath, scenePath, cameraPath, 1, 0);
		System.out.println("TeamgeistView.encompass()");
	}
	
	
	public void viewTeamgeist(HalfEdgeDataStructure<CPMVertex, CPMEdge, CPMFace> teamgeist){
		this.teamgeist = teamgeist;
		if (teamgeist == null || teamgeist.getNumEdges() == 0){
			resetGeometry();
			return;
		}
		DBGTracer.msg("updating polyeder geometry...");
		
		// vertices 
		double[][] patchVertexData = new double[teamgeist.getNumVertices()][3];
		String[] vertexLabels = new String[teamgeist.getNumVertices()];
		for (CPMVertex v : teamgeist.getVertices()){
			vertexLabels[v.getIndex()] = "" + v.getIndex();
			double[] p = patchVertexData[v.getIndex()];
			p[0] = v.getXYZW().x / v.getXYZW().w;
			p[1] = v.getXYZW().y / v.getXYZW().w;
			p[2] = v.getXYZW().z / v.getXYZW().w;
		}

		double[][] rotorVertexData = new double[teamgeist.getNumVertices()][3];
		for (CPMVertex v : teamgeist.getVertices()){
			double[] p = rotorVertexData[v.getIndex()];
			p[0] = v.getXYZW().x / v.getXYZW().w;
			p[1] = v.getXYZW().y / v.getXYZW().w;
			p[2] = v.getXYZW().z / v.getXYZW().w;
		}
		
		double[][] patchTexCoords = new double[teamgeist.getNumVertices()][];
		for (CPMVertex v : teamgeist.getVertices()){
			patchTexCoords[v.getIndex()] = teamgeistCoords.getPatchCoordinate(v.getIndex());
		}
		
		double[][] rotor1TexCoords = new double[teamgeist.getNumVertices()][];
		for (CPMVertex v : teamgeist.getVertices()){
			rotor1TexCoords[v.getIndex()] = teamgeistCoords.getRotorCoordinate(v.getIndex());
		}
		
		
		// faces
		int[][] patchIndexData = new int[teamgeist.getNumFaces()][3];
		HashSet<CPMFace> readyFaces = new HashSet<CPMFace>();
		for (CPMEdge e : teamgeist.getEdges()){
			int i = e.getStartVertex().getIndex();
			int j = e.getTargetVertex().getIndex();
			int k = e.getNextEdge().getTargetVertex().getIndex();
			if (!teamgeistCoords.isInPatch(i, j, k))
				continue;
			CPMFace f = e.getLeftFace();
			if (f == null)
				continue;
			if (readyFaces.contains(f))
				continue;
			CPMEdge e1 = e.getNextEdge();
			CPMEdge e2 = e.getPreviousEdge();
			
			patchIndexData[f.getIndex()][2] = e2.getTargetVertex().getIndex();
			patchIndexData[f.getIndex()][1] = e.getTargetVertex().getIndex();
			patchIndexData[f.getIndex()][0] = e1.getTargetVertex().getIndex();
			readyFaces.add(f);
		}
		
		int[][] rotor1IndexData = new int[teamgeist.getNumFaces()][3];
		readyFaces = new HashSet<CPMFace>();
		for (CPMEdge e : teamgeist.getEdges()){
			int i = e.getStartVertex().getIndex();
			int j = e.getTargetVertex().getIndex();
			int k = e.getNextEdge().getTargetVertex().getIndex();
			if (teamgeistCoords.isInPatch(i, j, k))
				continue;
			CPMFace f = e.getLeftFace();
			if (f == null)
				continue;
			if (readyFaces.contains(f))
				continue;
			CPMEdge e1 = e.getNextEdge();
			CPMEdge e2 = e.getPreviousEdge();
			
			rotor1IndexData[f.getIndex()][2] = e2.getTargetVertex().getIndex();
			rotor1IndexData[f.getIndex()][1] = e.getTargetVertex().getIndex();
			rotor1IndexData[f.getIndex()][0] = e1.getTargetVertex().getIndex();
			readyFaces.add(f);
		}
		
	
		// polyhedron
		patchFactory = new IndexedFaceSetFactory();
		patchFactory.setVertexCount(patchVertexData.length);
		patchFactory.setFaceCount(patchIndexData.length);
		patchFactory.setGenerateEdgesFromFaces(true);
		patchFactory.setGenerateVertexNormals(true);
		patchFactory.setGenerateFaceNormals(true);
		patchFactory.setVertexCoordinates(patchVertexData);
		patchFactory.setVertexTextureCoordinates(patchTexCoords);
		patchFactory.setFaceIndices(patchIndexData);
		patchFactory.setVertexLabels(vertexLabels);
		patchFactory.update();
		patchGeometry = patchFactory.getIndexedFaceSet();
		patchGeometry.setGeometryAttributes("pickable", false);
		patchRoot.setGeometry(patchGeometry);
		
		rotorFactory = new IndexedFaceSetFactory();
		rotorFactory.setVertexCount(rotorVertexData.length);
		rotorFactory.setFaceCount(rotor1IndexData.length);
		rotorFactory.setGenerateEdgesFromFaces(true);
		rotorFactory.setGenerateVertexNormals(true);
		rotorFactory.setGenerateFaceNormals(true);
		rotorFactory.setVertexCoordinates(rotorVertexData);
		rotorFactory.setVertexTextureCoordinates(rotor1TexCoords);
		rotorFactory.setFaceIndices(rotor1IndexData);
		rotorFactory.update();
		rotorGeometry = rotorFactory.getIndexedFaceSet();
		rotorGeometry.setGeometryAttributes("pickable", false);
		rotorRoot.setGeometry(rotorGeometry);
		
		CameraUtility.encompass(va.getCurrentViewer());
	}
	
	public void resetGeometry(){
		geometryRoot.setGeometry(null);
		va.getCurrentViewer().render();
	}
	
	public HalfEdgeDataStructure<CPMVertex, CPMEdge, CPMFace> getViewedTeamgeist(){
		return teamgeist;
	}
	

	public void setSmoothShading(boolean smooth){
		teamgeistAppearance.setAttribute(POLYGON_SHADER + "." + SMOOTH_SHADING, smooth);
	}
	
	public void setWireFrameRender(boolean wireframe){
		teamgeistAppearance.setAttribute(EDGE_DRAW, wireframe);
//		teamgeistAppearance.setAttribute(FACE_DRAW, !wireframe);
	}
	
	
	private void initScene(){
//		viewer.setPickSystem(pickSystem);
//		pickSystem.setSceneRoot(root);
		
		//root appearance
		rootAppearance.setAttribute(BACKGROUND_COLOR, controller.getColorController().getBackgroundColor());
		rootAppearance.setAttribute(POLYGON_SHADER + "." + ANTIALIASING_ENABLED, controller.getAppearanceController().isAntialiasing());
		root.setAppearance(rootAppearance);
		root.setName("Scene Root");
		
		//camera
		Camera cam = new Camera();
		cam.setFieldOfView(40);
		cameraRoot.setCamera(cam);
		cameraRoot.setName("Camera");
		MatrixBuilder.euclidean().translate(0, 0, 4).assignTo(cameraRoot);
        cameraPath.push(root);
        cameraPath.push(cameraRoot);
        cameraPath.push(cam);
        root.addChild(cameraRoot);
		
//        //light 1
//        light1.setColor(light1Color);
//        light1.setIntensity(light1intensity);
//        light1Root.setLight(light1);
//        light1Root.setName("Light1");
//		MatrixBuilder.euclidean().translate(12, 4, -5).assignTo(light1Root);
//        cameraRoot.addChild(light1Root);
//  
//        //light 2
//        light2.setColor(light2Color);
//        light2.setIntensity(light2intensity);
//        light2Root.setLight(light2);
//        light2Root.setName("Light2");
//		MatrixBuilder.euclidean().translate(-12, 4, -5).assignTo(light2Root);
//        cameraRoot.addChild(light2Root);
	
//        //light 3
//        Light light3 = new PointLight();
//        light3.setColor(light1Color);
//        light3.setIntensity(light3intensity);
//        light3Root.setLight(light3);
//        light3Root.setName("Light3");
//		MatrixBuilder.euclidean().assignTo(light3Root);
//        cameraRoot.addChild(light3Root);
        
        geometryRoot.addChild(patchRoot);
        geometryRoot.addChild(rotorRoot);
        
        Appearance patchApp = new Appearance();
        Textures.setTexture(patchApp, "teamgeist01sw.jpg");
        patchRoot.setAppearance(patchApp);
        Appearance rotor1App = new Appearance();
        Textures.setTexture(rotor1App, "teamgeist01sw.jpg");
        rotorRoot.setAppearance(rotor1App);
        
        //all geomentry
        if (softwareRendering){
        	faceColor = Color.WHITE;
        }
        teamgeistAppearance.setAttribute(POLYGON_SHADER, "smooth");
        teamgeistAppearance.setAttribute(POLYGON_SHADER + "." + DIFFUSE_COLOR, faceColor);
        teamgeistAppearance.setAttribute(POLYGON_SHADER + "." + AMBIENT_COLOR, Color.GRAY);
        teamgeistAppearance.setAttribute(POLYGON_SHADER + "." + SPECULAR_COLOR, Color.WHITE);
        teamgeistAppearance.setAttribute(POLYGON_SHADER + "." + SPECULAR_EXPONENT, 30);
        teamgeistAppearance.setAttribute(POLYGON_SHADER + "." + SPECULAR_COEFFICIENT, 0.7);
        teamgeistAppearance.setAttribute(POLYGON_SHADER + "." + AMBIENT_COLOR, faceColor);
        teamgeistAppearance.setAttribute(POLYGON_SHADER + "." + AMBIENT_COEFFICIENT, 0.4);
        teamgeistAppearance.setAttribute(LINE_SHADER + "." + DIFFUSE_COLOR, Color.GREEN);
        teamgeistAppearance.setAttribute(LINE_SHADER + "." + AMBIENT_COLOR, Color.GRAY);
        teamgeistAppearance.setAttribute(LINE_SHADER + "." + SPECULAR_COLOR, Color.WHITE);
        teamgeistAppearance.setAttribute(LINE_SHADER + "." + SPECULAR_EXPONENT, 30);
        teamgeistAppearance.setAttribute(LINE_SHADER + "." + SPECULAR_COEFFICIENT, 0.7);
        teamgeistAppearance.setAttribute(LINE_SHADER + "." + AMBIENT_COLOR, faceColor);
        teamgeistAppearance.setAttribute(LINE_SHADER + "." + AMBIENT_COEFFICIENT, 0.4);
        teamgeistAppearance.setAttribute(TUBE_RADIUS, 0.01);
        teamgeistAppearance.setAttribute(TRANSPARENCY_ENABLED, false);
        teamgeistAppearance.setAttribute(BACK_FACE_CULLING_ENABLED, true);
        teamgeistAppearance.setAttribute(TUBES_DRAW, true);
        teamgeistAppearance.setAttribute(EDGE_DRAW, false);
        teamgeistAppearance.setAttribute(VERTEX_DRAW, false);
        teamgeistAppearance.setAttribute(SPHERES_DRAW, true);
        geometryRoot.setAppearance(teamgeistAppearance);
        geometryRoot.setName("Teamgeist");
        root.addChild(geometryRoot);
        Font labelFont = new Font("Arial", Font.BOLD, 40);
        DefaultGeometryShader geomShader = ShaderUtility.createDefaultGeometryShader(teamgeistAppearance, true);
        DefaultPointShader dps = (DefaultPointShader)geomShader.getPointShader();
        DefaultTextShader vertexTextShader = (DefaultTextShader)dps.getTextShader();
        vertexTextShader.setFont(labelFont);
        vertexTextShader.setDiffuseColor(controller.getColorController().getIndexColor());
        vertexTextShader.setScale(0.0017);
        
        MatrixBuilder.euclidean().rotate(-Math.PI / 6, 1,0,0).rotate(Math.PI / 2.1, 0,0,1).rotate(Math.PI / 3.5, 0,1,0).assignTo(geometryRoot);
        
        //render trigger
//        RenderTrigger rt = new RenderTrigger();
//        rt.addSceneGraphComponent(root);
//        rt.addViewer(va.getCurrentViewer());
        
        va.getCurrentViewer().setCameraPath(cameraPath);
//        va.getCurrentViewer().setAvatarPath(cameraPath);
        
        //pick path
//	    SceneGraphPath pickPath = new SceneGraphPath();
//	    pickPath.push(root);
//	    pickPath.push(geometryRoot);
//	    viewer.setEmptyPickPath(pickPath);
//	    viewer.initializeTools();
	    
//        geometryRoot.addTool(encompassTool);
//       	geometryRoot.addTool(rotateTool);
	}

	public ViewerApp getViewerApp() {
		return va;
	}

}
