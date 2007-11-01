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
import halfedge.surfaceutilities.EmbeddedVertex;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.vecmath.AxisAngle4d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import math.util.VecmathTools;

import org.apache.commons.collections15.BidiMap;
import org.apache.commons.collections15.keyvalue.MultiKey;
import org.apache.commons.collections15.map.MultiKeyMap;

import alexandrov.frontend.viewer.decorations.UsesGeodesics;
import de.jreality.geometry.IndexedLineSetFactory;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.SceneGraphComponent;

public class GeodesicsViewer  <
		V extends Vertex<V, E, F> & HasRadius & HasXYZW & HasXY & HasCurvature,
		E extends Edge<V, E, F> & IsBoundary & HasLength & IsFlippable & IsHidable & HasAngle,
		F extends Face<V, E, F>
	
	> extends ViewAddon<V,E,F> implements UsesGeodesics<V,E,F>{
	
	private BidiMap<V, EmbeddedEdge<V,E,F>> geodesics = null;
	
	private int helpPosition = 0;
	
	double[][] geodesicData = null;
	int[][] geodesicIndices = null;
	
	public BidiMap<V, EmbeddedEdge<V,E,F>> getGeodesics(){
		return geodesics;
	}
	
	public void setGeodesics(BidiMap<V, EmbeddedEdge<V,E,F>> e) {
		geodesics = e;
	}
	
	public void update() {
		geodesicData = new double[graph.getNumFaces()*graph.getNumVertices()*30][3];//FIXME
		geodesicIndices = new int[graph.getNumFaces()*graph.getNumVertices()*30][2];
		
		drawGeodesics();
		
		double[][] tvd = new double[helpPosition*2][3];
		int[][] tid = new int[helpPosition][3];
		System.arraycopy(geodesicData, 0, tvd, 0, helpPosition*2);
		System.arraycopy(geodesicIndices, 0, tid, 0, helpPosition);
		geodesicData = tvd;
		geodesicIndices = tid;

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
		geodesicsFactory.setVertexCoordinates(geodesicData);
		geodesicsFactory.setEdgeIndices(geodesicIndices);
		geodesicsFactory.update();
		geodesicsLines = geodesicsFactory.getIndexedLineSet();
		
		geodesicsStyle.setAttribute(LINE_SHADER + "." + DIFFUSE_COLOR, new Color(200,130,70));
		geodesicsStyle.setAttribute(LINE_SHADER + "." + AMBIENT_COLOR, new Color(155,70,40));
		geodesicsStyle.setAttribute(LINE_SHADER + "." + SPECULAR_COLOR, Color.WHITE);
		geodesicsStyle.setAttribute(LINE_SHADER + "." + SPECULAR_EXPONENT, 30);
		geodesicsStyle.setAttribute(FACE_DRAW, false);
		geodesicsStyle.setAttribute(TUBES_DRAW, true);
		geodesicsStyle.setAttribute(TUBE_RADIUS, 0.002);
		geodesicsStyle.setAttribute(VERTEX_DRAW, false);
		geodesicsStyle.setAttribute(SPHERES_DRAW, false);
        
		sgc.setGeometry(geodesicsLines);
		sgc.setAppearance(geodesicsStyle);
		sgc.setVisible(true);
		

	}
	
	private void drawHelpAbs(Point3d b, Point3d v) {

		geodesicData[2*helpPosition + 0][0] =  b.x;
		geodesicData[2*helpPosition + 0][1] =  b.y;
		geodesicData[2*helpPosition + 0][2] =  b.z;
		geodesicData[2*helpPosition + 1][0] =  v.x;
		geodesicData[2*helpPosition + 1][1] =  v.y;
		geodesicData[2*helpPosition + 1][2] =  v.z;
		
		geodesicIndices[helpPosition][0] = 2 * helpPosition + 0;
		geodesicIndices[helpPosition][1] = 2 * helpPosition + 1;
		
		helpPosition ++;
	}
	

	private void drawGeodesics() {


		for(EmbeddedEdge<V,E,F> straightEdge : geodesics.values()) {
			for(int i = 0; i < straightEdge.getEmbeddedVertices().size() - 1; i+=2) {
				
				EmbeddedVertex p = straightEdge.getEmbeddedVertex(i);
				EmbeddedVertex q = straightEdge.getEmbeddedVertex(i+1);
				
				drawHelpAbs(VecmathTools.p4top3(p.getXYZW()), VecmathTools.p4top3(q.getXYZW()));
		
			}
		}
		
	}

}
