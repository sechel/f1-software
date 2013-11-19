package minimalsurface.frontend.action;

import static minimalsurface.frontend.content.MinimalViewOptions.CircleType.Ring;
import halfedge.Edge;
import halfedge.Face;
import halfedge.HalfEdgeDataStructure;
import halfedge.Vertex;
import halfedge.decorations.HasQuadGraphLabeling;
import halfedge.decorations.HasQuadGraphLabeling.QuadGraphLabel;
import halfedge.decorations.HasXYZW;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.List;

import javax.vecmath.Point4d;
import javax.vecmath.Vector3d;

import math.util.Circles;
import math.util.VecmathTools;
import minimalsurface.frontend.content.MinimalSurfaceContent;
import util.debug.DBGTracer;
import de.jreality.math.FactoredMatrix;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;

public class MinimalSurfaceVRMLWriter {

	public static String
	    surfaceDiffuse = "0.4 0.4 0.4",
	    surfaceAmbient = "0.4 0.4 0.4",
	    surfaceSpecular = "1.0 1.0 1.0",	
	    circlesDiffuse = "0.0 0.2 0.258823529",
	    circlesAmbient = "0.258823529 0.258823529 0.129411764",
	    circlesSpecular = "1.0 1.0 1.0",
	    spheresDiffuse = "0.258823529 0.258823529 0.0",
	    spheresAmbient = "0.258823529 0.258823529 0.129411764",
	    spheresSpecular = "1.0 1.0 1.0";
	public static double
		surfaceAmbientIntensity = 0.2,
		circlesAmbientIntensity = 0.2,
		spheresAmbientIntensity = 0.2,
		spheresTransparency = 0.5;
	
	
	public static	
	<
		V extends Vertex<V, E, F> & HasXYZW & HasQuadGraphLabeling,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F> & HasXYZW
	> void writeSurface(HalfEdgeDataStructure<V, E, F> surface, MinimalSurfaceContent panel, OutputStream out){
		PrintWriter o = new PrintWriter(out);
		o.println("#VRML V2.0 utf8");
		o.println("DEF MinimalSurface Transform {");
			o.println("\tchildren [");
				o.println("\t\tDEF Surface Transform {");
					makeSurface(surface, panel, o);
				o.println("\t\t}");
				o.println("\t\tDEF Circles Transform {");
				o.println("\t\tchildren [");
					makeDiskSurface(surface, panel, o, QuadGraphLabel.CIRCLE);
				o.println("\t\t]}");
				o.println("\t\tDEF Spheres Transform {");
				o.println("\t\tchildren [");
					makeDiskSurface(surface, panel, o, QuadGraphLabel.SPHERE);
				o.println("\t\t]}");	
			o.println("\t]");
		o.println("}");
		o.flush();
	}
	
	
	
	public static 
	<
		V extends Vertex<V, E, F> & HasXYZW & HasQuadGraphLabeling,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> void makeSurface(HalfEdgeDataStructure<V, E, F> surface, MinimalSurfaceContent panel, PrintWriter o){
		double[][] vertexData = new double[surface.getNumVertices()][];
		for (V v : surface.getVertices()){
			VecmathTools.dehomogenize(v.getXYZW());
			if (VecmathTools.isNAN(v.getXYZW())){
				v.getXYZW().set(0, 0, 0, 1);
				DBGTracer.msg("NaN in viewSurface() changed to 0.0!");
			}
			double[] p = new double[]{v.getXYZW().x, v.getXYZW().y, v.getXYZW().z};
			vertexData[v.getIndex()] = p;
		}
		int[][] faceData = new int[surface.getNumFaces()][];
		for (F f : surface.getFaces()){
			List<E> b = f.getBoundary();
			faceData[f.getIndex()] = new int[b.size()];
			int counter = 0;
			for (E e : b){
				faceData[f.getIndex()][counter] = e.getTargetVertex().getIndex();
				counter++;
			}
		}
		o.println("\t\tchildren [");
			o.println("\t\t\tShape {");
				o.println("\t\t\t\tappearance Appearance {");
				o.println("\t\t\t\tmaterial Material {");
					o.println("\t\t\t\t\tdiffuseColor " + surfaceDiffuse);
					//o.println("\t\t\t\t\tambientColor " + surfaceAmbient);
					o.println("\t\t\t\t\tspecularColor " + surfaceSpecular);
					o.println("\t\t\t\t\tambientIntensity " + surfaceAmbientIntensity); 
				o.println("\t\t\t\t}");
				o.println("\t\t\t\t}");
				o.println("\t\t\t\tgeometry DEF SurfaceGeometry IndexedFaceSet {");
					o.println("\t\t\t\t\tcoord DEF SurfaceVertices Coordinate { point [");
					for (int i = 0; i < vertexData.length; i++){
						o.print("\t\t\t\t\t\t" + vertexData[i][0] + " " + vertexData[i][1] + " " + vertexData[i][2]);
						if (i < vertexData.length - 1)
							o.println(",");
						else
							o.println(" ");
					}
					o.println("\t\t\t\t\t]}");
					o.println("\t\t\t\t\tcoordIndex [");
					for (int i = 0; i < faceData.length; i++){
						o.print("\t\t\t\t\t\t");
						for (int j : faceData[i])
							o.print(j + ", ");
						if (i < faceData.length - 1)
							o.println("-1,");
						else
							o.println(" ");
					}
					o.println("\t\t\t\t\t]");
				o.println("\t\t\t\t}");
			o.println("\t\t\t}");
		o.println("\t\t]");
		
	}
	
	
	
	
	public static 
	<
		V extends Vertex<V, E, F> & HasXYZW & HasQuadGraphLabeling,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> void makeDiskSurface(HalfEdgeDataStructure<V, E, F> surface, MinimalSurfaceContent panel, PrintWriter o, QuadGraphLabel label){
		for (V v : surface.getVertices()){
			if (v.getVertexLabel() != label)
				continue;
			Matrix T = null;
			try {
				switch (v.getVertexLabel()){
					case CIRCLE:
						List<V> star = v.getVertexStar();
						Point4d C = v.getXYZW();
						Point4d P1 = star.get(0).getXYZW(); 
						Point4d P2 = star.get(1).getXYZW();
						Point4d v1 = new Point4d(P1);
						Point4d v2 = new Point4d(P2);
						v1.sub(C); v1.w = 1.0;
						v2.sub(C); v2.w = 1.0;
						
						Point4d n = VecmathTools.cross(v1, v2);
						if (VecmathTools.length(n) < 1E-4){
							// try a third point in the star 
							if (star.size() < 3)
								continue; // there is no third point, thus there is no circle
							Point4d P3 = star.get(2).getXYZW();
							Point4d v3 = new Point4d(P3);
							v3.sub(C); v3.w = 1.0;
							n = VecmathTools.cross(v1, v3);
						}
						VecmathTools.dehomogenize(n);
						Vector3d N = new Vector3d(n.x, n.y, n.z);
						Double r = C.distance(P1);
						
						Matrix S = MatrixBuilder.euclidean().rotate(Math.PI / 2, 1, 0, 0).scale(1, 0.01, 1).getMatrix();
						T = Circles.getTransform(C, N, r, panel.getCircleType() == Ring);
						T.multiplyOnRight(S);
						break;
					case SPHERE:
						Point4d c = v.getXYZW();
						VecmathTools.dehomogenize(c);
						Point4d p = v.getEdgeStar().get(0).getStartVertex().getXYZW();
						VecmathTools.dehomogenize(p);
						double radius = VecmathTools.distance(c, p);
						T = MatrixBuilder.euclidean().translate(c.x, c.y, c.z).scale(radius).getMatrix();
						break;
					case INTERSECTION:
						continue;
				}
			} catch (Exception e){
				System.err.println("Error");
			}
			String name = (v.getVertexLabel() == QuadGraphLabel.CIRCLE ? "Circle" : "Sphere");
			o.println("\t\tDEF " + name + " Transform {");
			FactoredMatrix M = new FactoredMatrix(T, 0);
			double[] t = M.getTranslation();
			double rang = M.getRotationAngle();
			double[] r = M.getRotationAxis();
			double[] s = M.getStretch();
				o.println("\t\t\ttranslation " + t[0] + " " + t[1] + " " + t[2]);
				o.println("\t\t\trotation " + r[0] + " " + r[1] + " " + r[2] + " " + rang);
				o.println("\t\t\tscale " + s[0] + " " + s[1] + " " + s[2]);
				o.println("\t\t\tchildren [");
					o.println("\t\t\t\tShape {");
					switch (v.getVertexLabel()){
					default:
					case CIRCLE:
						o.println("\t\t\t\t\tappearance Appearance {");
						o.println("\t\t\t\t\tmaterial Material {");
						o.println("\t\t\t\t\t\tdiffuseColor " + circlesDiffuse);
//						o.println("\t\t\t\t\t\tambientColor " + circlesAmbient);
						o.println("\t\t\t\t\t\tspecularColor " + circlesSpecular);
						o.println("\t\t\t\t\t\tambientIntensity " + circlesAmbientIntensity); 
						o.println("\t\t\t\t\t}");
						o.println("\t\t\t\t\t}");
						o.println("\t\t\t\t\tgeometry Cylinder {}");
						break;
					case SPHERE:
						o.println("\t\t\t\t\tappearance Appearance {");
						o.println("\t\t\t\t\tmaterial Material {");
						o.println("\t\t\t\t\t\tdiffuseColor " + spheresDiffuse);
//						o.println("\t\t\t\t\t\tambientColor " + spheresAmbient);
						o.println("\t\t\t\t\t\tspecularColor " + spheresSpecular);
						o.println("\t\t\t\t\t\tambientIntensity " + spheresAmbientIntensity); 
						o.println("\t\t\t\t\t\ttransparency " + spheresTransparency);
						o.println("\t\t\t\t\t}");
						o.println("\t\t\t\t\t}");						
						o.println("\t\t\t\t\tgeometry Sphere {}");
						break;
					}
					o.println("\t\t\t\t}");
				o.println("\t\t\t]");
			o.println("\t\t}");
		}
	}
	
	
}
