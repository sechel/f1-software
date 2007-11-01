package koebe.frontend.content.joglviewer.gl;

import static javax.media.opengl.GL.GL_COMPILE_AND_EXECUTE;
import static javax.media.opengl.GL.GL_LINES;
import static javax.media.opengl.GL.GL_POINTS;
import halfedge.HalfEdgeDataStructure;
import halfedge.decorations.HasXY;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;

import koebe.frontend.content.joglviewer.Renderer;
import circlepatterns.graph.CPEdge;
import circlepatterns.graph.CPFace;
import circlepatterns.graph.CPVertex;


/**
 * Renders a 2D graph in the xz-plane
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 */
public class GraphRenderer implements Renderer {

	private HalfEdgeDataStructure<CPVertex, CPEdge, CPFace>
		graph = null;
	private boolean 
		compiled = false;
	private int
		graphList = -1;
	private float
		scale = 1; 
	
	
	public GraphRenderer(float scale){
		this.scale = scale;
	}
	
	
	public void render(GLAutoDrawable draw) {
		if (graph == null)
			return;
		GL gl = draw.getGL();
		if (!compiled){
			if (graphList != -1)
				gl.glDeleteLists(graphList, 1);
			graphList = gl.glGenLists(1);
			gl.glNewList(graphList, GL_COMPILE_AND_EXECUTE);
			drawGraph(graph, draw, scale);
			gl.glEndList();
			compiled = true;
		} else {
			gl.glCallList(graphList);
		}
	}

	
	private void drawGraph(HalfEdgeDataStructure<CPVertex, CPEdge, CPFace> graph, GLAutoDrawable draw, float scale){
		GL gl = draw.getGL();
		
		// vertices
		gl.glPointSize(5);
		gl.glColor3f(1,0,0);
		gl.glBegin(GL_POINTS);
		for (HasXY v : graph.getVertices()){
			gl.glVertex3d(v.getXY().x / scale, 0, v.getXY().y / scale);
		}
		gl.glEnd();
		
		// edges
		gl.glColor3f(0,0,0);
		gl.glBegin(GL_LINES);
		for (CPEdge e : graph.getEdges()){
			CPVertex t = e.getTargetVertex();
			CPVertex s = e.getOppositeEdge().getTargetVertex();
			gl.glVertex3d(s.getXY().x / scale, 0, s.getXY().y / scale);
			gl.glVertex3d(t.getXY().x / scale, 0, t.getXY().y / scale);
		}
		gl.glEnd();
	}



	public void setGraph(HalfEdgeDataStructure<CPVertex, CPEdge, CPFace> graph) {
		this.graph = graph;
		compiled = false;
	}



	public void setScale(float scale) {
		this.scale = scale;
		compiled = false;
	}
	

	
	
	
	
	
}
