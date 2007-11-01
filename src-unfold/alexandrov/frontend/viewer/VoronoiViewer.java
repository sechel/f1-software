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
import halfedge.triangulationutilities.TriangulationException;
import halfedge.unfoldutilities.Unfolder;

import java.awt.Color;
import java.util.Collection;
import java.util.HashMap;

import javax.vecmath.Point3d;

import math.util.VecmathTools;
import de.jreality.geometry.IndexedLineSetFactory;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.SceneGraphComponent;

public class VoronoiViewer  <
		V extends Vertex<V, E, F> & HasRadius & HasXYZW & HasXY & HasCurvature,
		E extends Edge<V, E, F> & IsBoundary & HasLength & IsFlippable & IsHidable & HasAngle,
		F extends Face<V, E, F>
	
	> extends ViewAddon<V,E,F>{
	
	private Collection<EmbeddedEdge<V,E,F>> paths = null;
//	private HalfEdgeDataStructure<V, E, F> joinTree = null;
	
	private int helpPosition = 0;
	
	double[][] voronoiData = null;
	int[][] voronoiIndices = null;
	
	
	public void update() {
		
		voronoiData = new double[graph.getNumFaces()*300][3];//FIXME
		voronoiIndices = new int[graph.getNumFaces()*300][2];
		
		drawVoronoi();
		
		double[][] tvd = new double[helpPosition*2][3];
		int[][] tid = new int[helpPosition][3];
		System.arraycopy(voronoiData, 0, tvd, 0, helpPosition*2);
		System.arraycopy(voronoiIndices, 0, tid, 0, helpPosition);
		voronoiData = tvd;
		voronoiIndices = tid;

	}
	
	public void generateSceneGraphComponent(){
		
		IndexedLineSetFactory geodesicsFactory = null;
		IndexedLineSet geodesicsLines = null;
		Appearance geodesicsStyle = null;
		
		update();

		sgc  = new SceneGraphComponent();
		geodesicsFactory = new IndexedLineSetFactory();
		geodesicsStyle = new Appearance();

		geodesicsFactory.setVertexCount(helpPosition*2);
		geodesicsFactory.setLineCount(helpPosition);
		geodesicsFactory.setVertexCoordinates(voronoiData);
		geodesicsFactory.setEdgeIndices(voronoiIndices);
		geodesicsFactory.update();
		geodesicsLines = geodesicsFactory.getIndexedLineSet();
		
		geodesicsStyle.setAttribute(LINE_SHADER + "." + DIFFUSE_COLOR, new Color(20,230,170));
		geodesicsStyle.setAttribute(LINE_SHADER + "." + AMBIENT_COLOR, new Color(155,70,40));
		geodesicsStyle.setAttribute(LINE_SHADER + "." + SPECULAR_COLOR, Color.WHITE);
		geodesicsStyle.setAttribute(LINE_SHADER + "." + SPECULAR_EXPONENT, 30);
		geodesicsStyle.setAttribute(FACE_DRAW, false);
		geodesicsStyle.setAttribute(TUBES_DRAW, true);
		geodesicsStyle.setAttribute(TUBE_RADIUS, 0.0015);
		geodesicsStyle.setAttribute(VERTEX_DRAW, false);
		geodesicsStyle.setAttribute(SPHERES_DRAW, false);
        
		sgc.setGeometry(geodesicsLines);
		sgc.setAppearance(geodesicsStyle);
		sgc.setVisible(true);
		

	}
	
	private void drawHelpAbs(Point3d b, Point3d v) {

		voronoiData[2*helpPosition + 0][0] =  b.x;
		voronoiData[2*helpPosition + 0][1] =  b.y;
		voronoiData[2*helpPosition + 0][2] =  b.z;
		voronoiData[2*helpPosition + 1][0] =  v.x;
		voronoiData[2*helpPosition + 1][1] =  v.y;
		voronoiData[2*helpPosition + 1][2] =  v.z;
		
		voronoiIndices[helpPosition][0] = 2 * helpPosition + 0;
		voronoiIndices[helpPosition][1] = 2 * helpPosition + 1;
		
		helpPosition ++;
	}
	

	private void drawVoronoi() {


		HashMap<F, V> dualMap = new HashMap<F,V>();
		HashMap<E, E> edgeToEdgeMap = new HashMap<E,E>();
		HalfEdgeDataStructure<V,E,F> voronoi = null;
		try {
			voronoi = Unfolder.constructVoronoi(graph, dualMap, edgeToEdgeMap);
			
			for(V v : graph.getVertices()) {
				for(E e : v.getEdgeStar()) {
					Point3d end = new Point3d(VecmathTools.p4top3(e.getTargetVertex().getXYZW()));
					Point3d start = new Point3d(VecmathTools.p4top3(e.getStartVertex().getXYZW()));
					end.scale(0.5);
					start.scale(0.5);
					end.add(start);	// middle now
					drawHelpAbs(VecmathTools.p4top3(edgeToEdgeMap.get(e).getTargetVertex().getXYZW()), end);
					drawHelpAbs(end, VecmathTools.p4top3(edgeToEdgeMap.get(e).getStartVertex().getXYZW()));
				}
			}
			
//			for(V v : voronoi.getVertices()) {
//				drawHelpAbs(new Point3d(0,0,0), VecmathTools.p4top3(v.getXYZW()));
//			}
//			
		} catch (TriangulationException e) {
			System.err.println(e.getMessage());
		}
	}

}
