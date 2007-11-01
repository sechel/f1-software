package koebe.frontend.content.joglviewer;

import static javax.media.opengl.GL.GL_BACK;
import static javax.media.opengl.GL.GL_COLOR_BUFFER_BIT;
import static javax.media.opengl.GL.GL_CULL_FACE;
import static javax.media.opengl.GL.GL_DEPTH_BUFFER_BIT;
import static javax.media.opengl.GL.GL_DEPTH_TEST;
import static javax.media.opengl.GL.GL_FILL;
import static javax.media.opengl.GL.GL_FRONT;
import static javax.media.opengl.GL.GL_NICEST;
import static javax.media.opengl.GL.GL_PERSPECTIVE_CORRECTION_HINT;
import static javax.media.opengl.GL.GL_SMOOTH;

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCanvas;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLEventListener;
import javax.swing.JPanel;


/**
 * A base OpenGL viewer which can rotate and translate by mouse dragging
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 */
public abstract class Mouse3DView extends JPanel implements GLEventListener, Runnable, MouseMotionListener{
	
	private static final long 
		serialVersionUID = 1L;
	private static GLCapabilities
		glCapabilities = new GLCapabilities();
	private static GLCanvas 
		glCanvas = null;
	private static boolean 
		animatorRunning = true;
	private Camera 
		cam = new Camera("Camera", 0f,0f,30f,  1f,0f,0f, 0f, 45f);
	
	public Mouse3DView(){
		glCapabilities.setDoubleBuffered(true);
		glCapabilities.setDepthBits(32);
		glCapabilities.setAlphaBits(8);
		glCanvas = new GLCanvas(glCapabilities);
		glCanvas.setAutoSwapBufferMode(false);
		glCanvas.addGLEventListener(this);
		glCanvas.addMouseMotionListener(this);
		glCanvas.addMouseListener(new MouseAdapter(){
			public void mousePressed(MouseEvent e) {
				last_drag_x = e.getX();
				last_drag_y = e.getY();
			}
		});
		
		setLayout(new BorderLayout());		
		add(glCanvas, BorderLayout.CENTER);	
		startAnimation();
	}

	
	public void startAnimation(){
		animatorRunning = true;
		new Thread(this, "Koebe AlexandrovPolyhedron Renderer").start();
	}
	
	public void stopAnimation(){
		animatorRunning = false;
	}
	

	public void run(){
		while(animatorRunning){
			glCanvas.display();
			glCanvas.swapBuffers();
			try {
				synchronized (this) {
					wait();
				}
			} catch (InterruptedException e){}
		}
	}

	
	public void paint(Graphics g) {
		super.paint(g);
		synchronized (this){
			notify();
		}
	}
	
	public void moveView(){
		synchronized (this){
			notify();
		}
	}
	

	public void init(GLAutoDrawable draw) {
		GL gl = draw.getGL();
		gl.glShadeModel(GL_SMOOTH);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST);
		gl.glLineWidth(1.0f);
		gl.glPolygonMode(GL_FRONT, GL_FILL);
		gl.glEnable(GL_CULL_FACE);
		gl.glCullFace(GL_BACK);
		gl.glClearColor(0.81f, 0.81f, 0.81f, 0.0f);
//		gl.glClearColor(1f, 1f, 1f, 0.0f);
	}


	
	public void display(GLAutoDrawable draw) {
		GL gl = draw.getGL();
		init(draw);
		gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		cam.apply(gl, glCanvas.getSize().width, glCanvas.getSize().height);
		render(draw);
	}


	public abstract void render(GLAutoDrawable draw);
	
	

	public void reshape(GLAutoDrawable draw, int x, int y, int w, int h) {
		GL gl = draw.getGL();
		gl.glViewport(0, 0, w, h);
		repaint();
	}


	private int 
		last_drag_x 	= 0,
		last_drag_y 	= 0;
	

	public void mouseDragged(MouseEvent e) {
		cam.rotate_around_local(0.0f, 0.0f, 0.0f,
			(-last_drag_y + e.getY()) / 2.0f,
			(-last_drag_x + e.getX()) / 2.0f,
			0.0f);
		last_drag_x = e.getX();
		last_drag_y = e.getY();
		moveView();
	}



	public void mouseMoved(MouseEvent arg0) {
	
	}


}