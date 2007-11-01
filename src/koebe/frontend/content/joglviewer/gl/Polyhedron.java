package koebe.frontend.content.joglviewer.gl;

import static javax.media.opengl.GL.GL_COMPILE_AND_EXECUTE;
import static javax.media.opengl.GL.GL_DEPTH_TEST;
import static javax.media.opengl.GL.GL_FILL;
import static javax.media.opengl.GL.GL_FRONT;
import static javax.media.opengl.GL.GL_FRONT_AND_BACK;
import static javax.media.opengl.GL.GL_LIGHTING;
import static javax.media.opengl.GL.GL_LINE;
import static javax.media.opengl.GL.GL_POLYGON;
import halfedge.HalfEdgeDataStructure;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.vecmath.Point3d;
import javax.vecmath.Point4d;

import koebe.KoebePolyhedron.KoebePolyhedronContext;
import koebe.frontend.content.joglviewer.Renderer;
import circlepatterns.graph.CPEdge;
import circlepatterns.graph.CPFace;
import circlepatterns.graph.CPVertex;


/**
 * Renders a polyhedron
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 * @see halfedge.HalfEdgeDataStructure
 */
public class Polyhedron implements Renderer{

	private int
		patternList = -1;
	private boolean
		compiled = false;
	private KoebePolyhedronContext<CPVertex, CPEdge, CPFace>
		polyederContext = null;
	
	
	
	public void render(GLAutoDrawable draw){
		if (polyederContext == null)
			return;
		GL gl = draw.getGL();
		if (!compiled){
			if (patternList != -1)
				gl.glDeleteLists(patternList, 1);
			patternList = gl.glGenLists(1);
			gl.glNewList(patternList, GL_COMPILE_AND_EXECUTE);
			gl.glEnable(GL_DEPTH_TEST);
			gl.glPolygonMode(GL_FRONT, GL_FILL);
			renderPolyeder(draw);
			gl.glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
			gl.glDisable(GL_LIGHTING);
			gl.glDisable(GL_DEPTH_TEST);
			gl.glColor3d(0,0,0);
			gl.glLineWidth(2);
			renderPolyeder(draw);
			gl.glEndList();
			compiled = true;
		} else {
			gl.glPolygonMode(GL.GL_FRONT, GL.GL_FILL);
			gl.glEnable(GL.GL_DEPTH_TEST);
			gl.glCallList(patternList);
			gl.glPolygonMode(GL.GL_FRONT, GL.GL_LINE);
			gl.glDisable(GL.GL_DEPTH_TEST);
			gl.glColor3d(0,0,0);			
			gl.glCallList(patternList);
		}
	}

	
	
	private void renderPolyeder(GLAutoDrawable draw){
		if (polyederContext == null)
			return;
		GL gl = draw.getGL();
		HalfEdgeDataStructure<CPVertex, CPEdge, CPFace> poly = polyederContext.getPolyeder();
		for (CPFace f : poly.getFaces()){
			CPEdge firstEdge = f.getBoundaryEdge();
			CPEdge actEdge = firstEdge;
			Point4d p1 = firstEdge.getTargetVertex().getXYZW();
			Point4d p2 = firstEdge.getNextEdge().getTargetVertex().getXYZW();
			Point4d p3 = firstEdge.getNextEdge().getNextEdge().getTargetVertex().getXYZW();
			Point4d n = getNormal(p1, p2, p3);
			gl.glBegin(GL_POLYGON);
			gl.glNormal3d(n.x, n.y, n.z);
			do {
				Point4d p = actEdge.getTargetVertex().getXYZW();
				gl.glVertex4d(p.x, p.y, p.z, p.w);
				actEdge = actEdge.getNextEdge();
			} while (actEdge != firstEdge);
			gl.glEnd();
		}
		
	}

	
	private static Point4d
		ZERO = new Point4d();

	private Point4d getNormal(Point4d p1, Point4d p2, Point4d p3){
		Point3d v1 = new Point3d(p1.x / p1.w - p2.x / p2.w, p1.y / p1.w - p2.y / p2.w, p1.z / p1.w - p2.z / p2.w);
		Point3d v2 = new Point3d(p1.x / p1.w - p3.x / p3.w, p1.y / p1.w - p3.y / p3.w, p1.z / p1.w - p3.z / p3.w);
		double x = v1.y * v2.z  - v1.z * v2.y;
		double y = v1.z * v2.x  - v1.x * v2.z;
		double z = v1.x * v2.y  - v1.y * v2.x;
		Point4d result = new Point4d(x, y, z, 0);
		result.scale(result.distance(ZERO));
		result.w = 1;
		return result;
	}
	
	
	

	public void setPolyederContext(KoebePolyhedronContext<CPVertex, CPEdge, CPFace> polyederContext) {
		this.polyederContext = polyederContext;
		compiled = false;
	}
	

}
