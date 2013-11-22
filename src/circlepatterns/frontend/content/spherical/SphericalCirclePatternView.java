package circlepatterns.frontend.content.spherical;

import halfedge.HalfEdgeDataStructure;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.vecmath.Point4d;
import javax.vecmath.Vector3d;

import math.util.Circles;
import math.util.VecmathTools;
import circlepatterns.frontend.CPTestSuite;
import circlepatterns.graph.CPEdge;
import circlepatterns.graph.CPFace;
import circlepatterns.graph.CPVertex;
import de.jreality.geometry.PointSetFactory;
import de.jreality.geometry.Primitives;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.DefaultGeometryShader;
import de.jreality.shader.DefaultPointShader;
import de.jreality.shader.DefaultTextShader;
import de.jreality.shader.ShaderUtility;
import de.jreality.ui.viewerapp.ViewerApp;
import de.jreality.util.CameraUtility;

public class SphericalCirclePatternView{

	private SceneGraphComponent 
		root = new SceneGraphComponent(),
		sphereRoot = new SceneGraphComponent(),
		vertexRoot = new SceneGraphComponent(),
		circleCenterRoot = new SceneGraphComponent(),
		circlesRoot = new SceneGraphComponent(),
		geometryRoot = new SceneGraphComponent();
	private Appearance
		rootAppearance = new Appearance(),
		sphereAppearance = new Appearance(),
		circleCenterAppearance = new Appearance(),
		circleAppearance = new Appearance();
	private ViewerApp
		viewerApp = null;
	
	private PointSetFactory
		vertexFactory = new PointSetFactory(),
		circleCenterFactory = new PointSetFactory();
	
	
	public SphericalCirclePatternView() {
		viewerApp = new ViewerApp(root);
		makeAppearance();
		makeScene();
		viewerApp.setAttachNavigator(false);
		viewerApp.setShowMenu(false);
		viewerApp.update();
		encompass();
	}

	
	private void makeScene() {
		// root
		
		// circles
		root.addChild(geometryRoot);
		vertexRoot.setGeometry(vertexFactory.getGeometry());
		circleCenterRoot.setGeometry(circleCenterFactory.getGeometry());
		geometryRoot.addChild(circleCenterRoot);
		geometryRoot.addChild(vertexRoot);
		geometryRoot.addChild(circlesRoot);
		
		// sphere
		sphereRoot.setGeometry(Primitives.sphere(100));
		root.addChild(sphereRoot);
	}
	
	
	private void makeAppearance() {
		viewerApp.getJrScene().getSceneRoot().getAppearance().setAttribute(CommonAttributes.BACKGROUND_COLOR, Color.WHITE);
		root.setAppearance(rootAppearance);
		
		circleAppearance.setAttribute(CommonAttributes.FACE_DRAW, false);
		circleAppearance.setAttribute(CommonAttributes.VERTEX_DRAW, false);
		circleAppearance.setAttribute(CommonAttributes.TUBES_DRAW, false);
		circlesRoot.setAppearance(circleAppearance);
		
		sphereAppearance.setAttribute(CommonAttributes.VERTEX_DRAW, false);
		sphereAppearance.setAttribute(CommonAttributes.TRANSPARENCY_ENABLED, true);
		sphereAppearance.setAttribute(CommonAttributes.TRANSPARENCY, 0.7);
		sphereRoot.setAppearance(sphereAppearance);
		
		circleCenterAppearance.setAttribute(CommonAttributes.DIFFUSE_COLOR, Color.RED);
        Font labelFont = new Font("Arial", Font.BOLD, 40);
        DefaultGeometryShader geomShader = ShaderUtility.createDefaultGeometryShader(circleCenterAppearance, true);
        DefaultPointShader dps = (DefaultPointShader)geomShader.getPointShader();
        DefaultTextShader vertexTextShader = (DefaultTextShader)dps.getTextShader();
        vertexTextShader.setFont(labelFont);
        vertexTextShader.setDiffuseColor(Color.BLACK);
        vertexTextShader.setScale(0.0017);
		circleCenterRoot.setAppearance(circleCenterAppearance);
	}
	
	
	
	public Component getComponent() {
		return viewerApp.getContent();
	}
	

	private void encompass() {
		CameraUtility.encompass(viewerApp.getCurrentViewer());
	}
	
	
	public void updateSpherical() {
		HalfEdgeDataStructure<CPVertex, CPEdge, CPFace> graph = CPTestSuite.getTopology();
		
		// centers
		double[][] circleCenters = new double[graph.getNumFaces()][];
		String[] labels = new String[graph.getNumFaces()];
		for (CPFace f : graph.getFaces()) {
			VecmathTools.dehomogenize(f.getXYZW());
			circleCenters[f.getIndex()] = new double[] {f.getXYZW().x, f.getXYZW().y, f.getXYZW().z};
			labels[f.getIndex()] = f.getIndex() + "";
		}
//		circleCenterFactory.setVertexCount(circleCenters.length);
//		circleCenterFactory.setVertexCoordinates(circleCenters);
//		circleCenterFactory.setVertexLabels(labels);
//		circleCenterFactory.update();
		
		
		// vertices
		double[][] vertices = new double[graph.getNumVertices()][];
		for (CPVertex v : graph.getVertices()) {
			VecmathTools.dehomogenize(v.getXYZW());
			Point4d p = v.getXYZW();
			vertices[v.getIndex()] = new double[]{p.x, p.y, p.z};
		}
		
		vertexFactory.setVertexCount(vertices.length);
		vertexFactory.setVertexCoordinates(vertices);
		vertexFactory.update();
		
		
		// circles
		while (circlesRoot.getChildComponentCount() > 0)
			circlesRoot.removeChild(circlesRoot.getChildComponent(0));
		for (CPFace f : graph.getFaces()) {
			VecmathTools.dehomogenize(f.getXYZW());
			Vector3d N = new Vector3d(f.getXYZW().x, f.getXYZW().y, f.getXYZW().z);
			Double r = Math.sin(f.getRadius());
			Matrix mat = Circles.getTransform(f.getXYZW(), N, r);
			SceneGraphComponent circle = new SceneGraphComponent();
			circle.setGeometry(Primitives.regularPolygon(100));
			MatrixBuilder.euclidean().times(mat).assignTo(circle);
			circlesRoot.addChild(circle);
		}
		
		
		encompass();
	}
	
	
	
	
	
	
	
	
}
