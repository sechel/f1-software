package koebe.frontend.content.joglviewer.gl;

import static javax.media.opengl.GL.GL_COMPILE_AND_EXECUTE;
import static javax.media.opengl.GL.GL_LINE_STRIP;

import java.util.List;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.vecmath.Point4d;

import koebe.KoebePolyhedron.KoebePolyhedronContext;
import koebe.frontend.content.joglviewer.Renderer;
import math.util.Circles;
import circlepatterns.graph.CPEdge;
import circlepatterns.graph.CPFace;
import circlepatterns.graph.CPVertex;


/**
 * Renders a circle pattern in the xz-Plane
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 */
public class ProjectedCirclePattern implements Renderer{

	
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
			renderCirclePattern(draw);
			gl.glEndList();
			compiled = true;
		} else {
			gl.glCallList(patternList);
		}
	}
	
	
	private void renderCirclePattern(GLAutoDrawable draw){
		GL gl = draw.getGL();
		//gl.glDisable(GL_DEPTH_TEST);
		gl.glEnable(GL.GL_BLEND);
		if (polyederContext == null)
			return;
		
		gl.glColor3f(1,0,0);
		for (CPVertex vertex : polyederContext.getPolyeder().getVertices()){
			List<CPEdge> targetList = vertex.getEdgeStar();
			CPVertex v1 = polyederContext.getEdgeVertexMap().get(targetList.get(0));
			if (!v1.isValid())
				v1 = polyederContext.getNorthPole();
			CPVertex v2 = polyederContext.getEdgeVertexMap().get(targetList.get(1));
			if (!v2.isValid())
				v2 = polyederContext.getNorthPole();
			CPVertex v3 = polyederContext.getEdgeVertexMap().get(targetList.get(2));
			if (!v3.isValid())
				v3 = polyederContext.getNorthPole();
			Point4d p1 = v1.getXYZW();
			Point4d p2 = v2.getXYZW();
			Point4d p3 = v3.getXYZW();
			Point4d center = new Point4d();
			List<Point4d> pList = Circles.getCircle(p1, p2, p3, 30, center);
			gl.glBegin(GL_LINE_STRIP);
				for (Point4d p : pList){
					gl.glVertex4d(p.x, p.y, p.z, p.w);
				}
				Point4d p = pList.get(0);
				gl.glVertex4d(p.x, p.y, p.z, p.w);
			gl.glEnd();
		}
		
		
		gl.glColor3f(0,1,0);
		for (CPFace face : polyederContext.getPolyeder().getFaces()){
			List<CPEdge> b = face.getBoundary();
			CPVertex v1 = polyederContext.getEdgeVertexMap().get(b.get(0));
			if (!v1.isValid())
				v1 = polyederContext.getNorthPole();
			CPVertex v2 = polyederContext.getEdgeVertexMap().get(b.get(1));
			if (!v2.isValid())
				v2 = polyederContext.getNorthPole();
			CPVertex v3 = polyederContext.getEdgeVertexMap().get(b.get(2));
			if (!v3.isValid())
				v3 = polyederContext.getNorthPole();
			Point4d p1 = v1.getXYZW();
			Point4d p2 = v2.getXYZW();
			Point4d p3 = v3.getXYZW();
			Point4d center = new Point4d();
			List<Point4d> pList = Circles.getCircle(p1, p2, p3, 30, center);
			gl.glBegin(GL_LINE_STRIP);
				for (Point4d p : pList){
					gl.glVertex4d(p.x, p.y, p.z, p.w);
				}
				Point4d p = pList.get(0);
				gl.glVertex4d(p.x, p.y, p.z, p.w);
			gl.glEnd();
		}
		
	}
	
	
	public void setPolyederContext(KoebePolyhedronContext<CPVertex, CPEdge, CPFace> polyederContext){
		this.polyederContext = polyederContext;
		compiled = false;
	}
	
	
}
