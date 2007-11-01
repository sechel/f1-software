package alexandrov.frontend.viewer;

import static de.jreality.shader.CommonAttributes.AMBIENT_COLOR;
import static de.jreality.shader.CommonAttributes.DIFFUSE_COLOR;
import static de.jreality.shader.CommonAttributes.FACE_DRAW;
import static de.jreality.shader.CommonAttributes.LINE_SHADER;
import static de.jreality.shader.CommonAttributes.SPECULAR_COLOR;
import static de.jreality.shader.CommonAttributes.SPECULAR_EXPONENT;
import static de.jreality.shader.CommonAttributes.SPHERES_DRAW;
import static de.jreality.shader.CommonAttributes.TUBES_DRAW;
import static de.jreality.shader.CommonAttributes.TUBE_RADIUS;
import static de.jreality.shader.CommonAttributes.VERTEX_DRAW;
import halfedge.Edge;
import halfedge.Face;
import halfedge.HalfEdgeDataStructure;
import halfedge.Vertex;
import halfedge.decorations.HasAngle;
import halfedge.decorations.HasCurvature;
import halfedge.decorations.HasLength;
import halfedge.decorations.HasRadius;
import halfedge.decorations.HasXY;
import halfedge.decorations.HasXYZW;
import halfedge.decorations.IsBoundary;
import halfedge.decorations.IsFlippable;
import halfedge.decorations.IsHidable;
import halfedge.surfaceutilities.EmbeddedEdge;
import halfedge.surfaceutilities.SurfaceUtility.EdgeAngleComparator;
import halfedge.triangulationutilities.TriangulationException;
import halfedge.unfoldutilities.Unfolder;

import java.awt.Color;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import math.util.VecmathTools;

import org.apache.commons.collections15.BidiMap;

import alexandrov.frontend.viewer.decorations.UsesGeodesics;
import alexandrov.math.CPMCurvatureFunctional;
import de.jreality.geometry.IndexedLineSetUtility;
import de.jreality.geometry.IndexedLineSetUtility2;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.SceneGraphComponent;

public class StarViewer  <
		V extends Vertex<V, E, F> & HasRadius & HasXYZW & HasXY & HasCurvature,
		E extends Edge<V, E, F> & IsBoundary & HasLength & IsFlippable & IsHidable & HasAngle,
		F extends Face<V, E, F>
	
	> extends ViewAddon<V,E,F> implements UsesGeodesics<V,E,F>{
	
	private BidiMap<V, EmbeddedEdge<V,E,F>> geodesics = null;
	
	private int helpPosition = 0;
	
	double[][] geodesicData = null;
	int[][] geodesicIndices = null;
	
	double[] normal = null;
	double[] dv0v1 = null;
	double[] dv0v2 = null;
	double[] dw0w1 = null;
	
	Point3d pv0, pv1, pv2 = null;
	Point3d pw0, pw1, pw2 = null;
	
	double[] dv0, dv1, dv2 = null;
	double[] dw0, dw1, dw2 = null;
	
	Vector3d v0v1, v0v2 = null;
	Vector3d w0w1, w0w2 = null;
	
	int index_w0, index_w1, index_w2 = 0;
	Vector3d tN = null;

	double[] curvature = null;
	double[] arclengths = null;
	
	V sourceVertex = null;
	
	public void setSource(V s) {
		sourceVertex = s;
	}

	public BidiMap<V, EmbeddedEdge<V,E,F>> getGeodesics(){
		return geodesics;
	}
	
	
	public void setGeodesics(BidiMap<V, EmbeddedEdge<V,E,F>> e) {
		geodesics = e;
	}
	
	public void update() {
		geodesicData = new double[graph.getNumFaces()*graph.getNumVertices()*3][3];//FIXME
		geodesicIndices = new int[graph.getNumFaces()*graph.getNumVertices()*3][2];
		
		drawGeodesics();
		
		double[][] tvd = new double[helpPosition*2][3];
		int[][] tid = new int[helpPosition][3];
		System.arraycopy(geodesicData, 0, tvd, 0, helpPosition*2);
		System.arraycopy(geodesicIndices, 0, tid, 0, helpPosition);
		geodesicData = tvd;
		geodesicIndices = tid;

	}
	
	public void setGraph(HalfEdgeDataStructure<V,E,F> graph) {
		this.graph = graph;
	}
	
	public void generateSceneGraphComponent(){

		Appearance geodesicsStyle = null;
		
		update();

		sgc  = new SceneGraphComponent();

		geodesicsStyle = new Appearance();
		
		IndexedLineSet curve = IndexedLineSetUtility2.createPlaneCurveFromCruvature(null, curvature, arclengths, dv0, dv0v1, normal, false);

		double data[][] = IndexedLineSetUtility.extractCurve(null, curve, 0);
		
//		System.err.println(index_w1);
		
		pw0 = new Point3d(data[index_w0]);
		pw1 = new Point3d(data[index_w1]);
		pw2 = new Point3d(data[index_w2]);
		
		w0w1 = new Vector3d(pw1);
		w0w1.sub(pw0);
		w0w1.normalize();
		
		double[] dw0w1 = new double[3];
		w0w1.get(dw0w1);
		
		double[] dw0 = new double[3];
		pw0.get(dw0);
		double[] dw1 = new double[3];
		pw1.get(dw1);
		double[] dw2 = new double[3];
		pw2.get(dw2);
		
		Matrix rot = MatrixBuilder
						.euclidean()
						.rotateFromTo(dw0w1, dv0v1)
						.getMatrix();
		
		Matrix mirr = MatrixBuilder.euclidean().rotate(Math.PI, dv0v1).getMatrix();
		
		dv0v1 = rot.multiplyVector(dv0v1);
		dv0v1 = mirr.multiplyVector(dv0v1);
		
		for(int i = 0; i < curvature.length; i++)
			curvature[i] = -curvature[i];

						
		Matrix V = new Matrix(new double[] {dv0[0], dv0[1], dv0[2], 1, 
											dv1[0], dv1[1], dv1[2], 1, 
											dv2[0], dv2[1], dv2[2], 1, 
												0 ,		0 ,		0 , 1});
		
		Matrix W = new Matrix(new double[] {dw0[0], dw0[1], dw0[2], 1, 
											dw1[0], dw1[1], dw1[2], 1, 
											dw2[0], dw2[1], dw2[2], 1, 
												0 ,		0 ,		0 , 1});
		
		Matrix U = new Matrix(V);
		U.multiplyOnLeft(W.getInverse());
				
		curve = IndexedLineSetUtility2.createPlaneCurveFromCruvature(null, curvature, arclengths, dv0, dv0v1, normal, false);

		geodesicsStyle.setAttribute(LINE_SHADER + "." + DIFFUSE_COLOR, new Color(100,130,250));
		geodesicsStyle.setAttribute(LINE_SHADER + "." + AMBIENT_COLOR, new Color(55,70,240));
		geodesicsStyle.setAttribute(LINE_SHADER + "." + SPECULAR_COLOR, Color.WHITE);
		geodesicsStyle.setAttribute(LINE_SHADER + "." + SPECULAR_EXPONENT, 30);
		geodesicsStyle.setAttribute(FACE_DRAW, false);
		geodesicsStyle.setAttribute(TUBES_DRAW, false);
		geodesicsStyle.setAttribute(TUBE_RADIUS, 0.01);
		geodesicsStyle.setAttribute(VERTEX_DRAW, false);
		geodesicsStyle.setAttribute(SPHERES_DRAW, false);
        
		sgc.setGeometry(curve);
		sgc.setAppearance(geodesicsStyle);
		sgc.setVisible(true);
		
//		Appearance zeroApp = new Appearance();
//		SceneGraphComponent zeroRoot = new SceneGraphComponent();
//		zeroApp.setAttribute(VERTEX_DRAW, true);
//		zeroApp.setAttribute(POINT_SHADER + "." + DIFFUSE_COLOR, Color.RED);
//		zeroApp.setAttribute("pointSize", 8.0);
//		zeroApp.setAttribute(SPHERES_DRAW, false);
//		PointSetFactory psf = new PointSetFactory();
//		double[][] pts = new double[][] {dv0, dv1, dv2,  dw0, dw1, dw2, dmirror};
//		psf.setVertexCount(pts.length);
//		psf.setVertexCoordinates(pts);
//		String[] labels = new String[]{"v0", "v1", "v2", "w0", "w1", "w2", "mirror"};
//		psf.setVertexLabels(labels);
//		psf.update();
//		PointSet point = psf.getPointSet();
//		zeroRoot.setGeometry(point);
//		zeroRoot.setAppearance(zeroApp);
//		zeroRoot.setName("Zero Indicator");
//		zeroRoot.setVisible(true);
//		sgc.addChild(zeroRoot);


	}
	
	private void drawGeodesics() {
		
		// pick root vertex
		F bottom = Unfolder.getBottom(graph, sourceVertex);
		V v0 = bottom.getBoundaryEdge().getStartVertex();
		V v1 = bottom.getBoundaryEdge().getTargetVertex();
		V v2 = bottom.getBoundaryEdge().getNextEdge().getTargetVertex();
		EmbeddedEdge<V,E,F> ee_v0, ee_v1 = null;
		
//		System.err.println("Nr of geodesics: " + geodesics.size());
				

		List<EmbeddedEdge<V,E,F>> sortedList = new LinkedList<EmbeddedEdge<V,E,F>>();
		sortedList.addAll(geodesics.values());
		Collections.sort(sortedList, new EdgeAngleComparator<EmbeddedEdge<V,E,F>>());
		ee_v0 = geodesics.get(v0);
		ee_v1 = geodesics.get(v1);
		if(ee_v0 == null)
			System.err.println("Couldn't find geodesic to first vertex");
		if(ee_v1 == null)
			System.err.println("Couldn't find geodesic to last vertex");
		
		
		List<EmbeddedEdge<V,E,F>> subList = sortedList.subList(sortedList.indexOf(ee_v0), sortedList.size()-1);
		subList.add(sortedList.get(sortedList.size()-1));
		subList.addAll(sortedList.subList(0, sortedList.indexOf(ee_v0)));
		
		sortedList = subList;

		curvature = new double[sortedList.size()*2];
		arclengths = new double[sortedList.size()*2];
		
		try {
			
			double vertexGamma = CPMCurvatureFunctional.getGammaAt(ee_v0.getSourceVertex());
			double vertexCurvature = 2*Math.PI - vertexGamma;
			
			double b = sortedList.get(0).getAngle();
			
//			System.err.println("Before:");
			for(EmbeddedEdge<V,E,F> ee : sortedList) {
				System.err.println(ee.getSourceEdge() + "Ang: " + ee.getAngle());
			}
			
//			System.err.println("After:");
			for(EmbeddedEdge<V,E,F> ee : sortedList) {
				double a = (ee.getAngle() - b + 2*(2*Math.PI - vertexCurvature)); 
				ee.setAngle(a % (2*Math.PI - vertexCurvature));
				System.err.println(ee.getSourceEdge() + "Ang: " + ee.getAngle());
			}
			
			// get normal to plane in which we live (rotation axis)
			pv0 = new Point3d(VecmathTools.p4top3(v0.getXYZW()));
			pv1 = new Point3d(VecmathTools.p4top3(v1.getXYZW()));
			pv2 = new Point3d(VecmathTools.p4top3(v2.getXYZW()));

			v0v1 = new Vector3d(pv1);
			v0v1.sub(pv0);
			v0v1.normalize();
			
			v0v2 = new Vector3d(pv2);
			v0v2.sub(pv0);
			v0v2.normalize();
			
			tN = new Vector3d();
			tN.cross(v0v1, v0v2);
			tN.normalize();
			
			normal = new double[3];
			tN.get(normal);
			
			dv0 = new double[3];
			pv0.get(dv0);
			dv1 = new double[3];
			pv1.get(dv1);
			dv2 = new double[3];
			pv2.get(dv2);
			
			dv0v1 = new double[3];
			v0v1.get(dv0v1);
			dv0v2 = new double[3];
			v0v2.get(dv0v2);
		
			Iterator<EmbeddedEdge<V,E,F>> iterator = sortedList.iterator();
			EmbeddedEdge<V,E,F> start = iterator.next();
			EmbeddedEdge<V,E,F> current = start;
			EmbeddedEdge<V,E,F> previous = null;
			
//			System.err.println("Base geodesic is: " + current);

			
			int i = 1;
			
			while(iterator.hasNext()) {
				previous = current;
				current = iterator.next();	// 2nd, angle > 0
				

				if(current.getEndVertex() == v1) {
					index_w1 = i+1;
				}
				
				if(current.getEndVertex() == v2) {
					index_w2 = i+1;
				}
				
				System.err.println("Current geodesic is: " + current);
				
				// source turn
				arclengths[i] = current.getLength2();
				curvature[i] =  -(Math.PI - (current.getAngle() - previous.getAngle()));
//				curvature[i] =  -(Math.PI - (EmbeddedHalfEdgeUtility.getAngle(current, previous)));
				
//				System.err.println("Sourceturn: " + curvature[i] + "( " + (current.getAngle() - previous.getAngle()) + ")");
				
				// curvature turn
				arclengths[i+1] = current.getLength2();
				curvature[i+1] = Math.PI - (2*Math.PI - CPMCurvatureFunctional.getGammaAt(current.getEndVertex()));
//				System.err.println("Endturn: " + curvature[i+1] + "( " + (2*Math.PI - CPMCurvatureFunctional.getGammaAt(current.getEndVertex())) + ")");
				
				i += 2;
			}

			arclengths[i] = start.getLength2();
			curvature[i] = -(Math.PI - (vertexGamma - current.getAngle()));
//			System.err.println("Sourceturn: " + curvature[i] + "( " + (vertexGamma - current.getAngle()) + ")");
			
			arclengths[0] = start.getLength2();
			curvature[0] = Math.PI - (2*Math.PI - CPMCurvatureFunctional.getGammaAt(start.getEndVertex()));
//			System.err.println("Endturn: " + curvature[0] + "( " + (2*Math.PI - CPMCurvatureFunctional.getGammaAt(start.getEndVertex())) + ")");

			index_w0 = 0;
			
		} catch(TriangulationException e) {
			System.err.println(e.getMessage());
		}
		
		

	}

}
