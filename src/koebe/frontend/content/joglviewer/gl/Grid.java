package koebe.frontend.content.joglviewer.gl;

import static javax.media.opengl.GL.GL_COMPILE_AND_EXECUTE;
import static javax.media.opengl.GL.GL_LINES;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;

import koebe.frontend.content.joglviewer.Renderer;

/**
 * Renders a grid in the xz-plane.
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 */
public class Grid implements Renderer{

	private int
		gridList = -1;
	private int
		width = 10,
		height = 10;
	private float
		size = 1,
		offset = 0.01f;
	
	
	public Grid(int width, int height){
		this.width = width;
		this.height = height;
	}
	
	
	public Grid(int width, int height, float size, float offset){
		this(width, height);
		this.size = size;
		this.offset = offset;
	}

	
	
	public void render(GLAutoDrawable draw){
		GL gl = draw.getGL();
		
		if (gridList != -1){
			gl.glCallList(gridList);
		} else {
			gridList = gl.glGenLists(1);
			gl.glNewList(gridList, GL_COMPILE_AND_EXECUTE);
			internalRender(draw);
			gl.glEndList();
		}
		
	}
	
	
	private void internalRender(GLAutoDrawable draw){
		GL gl = draw.getGL();
		
		gl.glPushMatrix();
		gl.glScalef(size, size, size);
		gl.glTranslatef(0, offset, 0);
		gl.glBegin(GL_LINES);
		float x = -0.5f;
		for (int i = 0; i <= height; i++){
			gl.glVertex3f(x, 0, -0.5f);
			gl.glVertex3f(x, 0, 0.5f);
			x += 1.0 / width;
		}
		float z = -0.5f;
		for (int i = 0; i <= height; i++){
			gl.glVertex3f(-0.5f, 0, z);
			gl.glVertex3f(0.5f, 0, z);
			z += 1.0 / height;
		}
		gl.glEnd();
		gl.glPopMatrix();
	}
	
	
	
}
