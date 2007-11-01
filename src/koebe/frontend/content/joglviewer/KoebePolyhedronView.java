package koebe.frontend.content.joglviewer;

import static javax.media.opengl.GL.GL_ALL_ATTRIB_BITS;
import static javax.media.opengl.GL.GL_AMBIENT;
import static javax.media.opengl.GL.GL_AMBIENT_AND_DIFFUSE;
import static javax.media.opengl.GL.GL_BLEND;
import static javax.media.opengl.GL.GL_DIFFUSE;
import static javax.media.opengl.GL.GL_FASTEST;
import static javax.media.opengl.GL.GL_FRONT;
import static javax.media.opengl.GL.GL_LIGHT0;
import static javax.media.opengl.GL.GL_LIGHT1;
import static javax.media.opengl.GL.GL_LIGHT2;
import static javax.media.opengl.GL.GL_LIGHTING;
import static javax.media.opengl.GL.GL_LINE_SMOOTH;
import static javax.media.opengl.GL.GL_LINE_SMOOTH_HINT;
import static javax.media.opengl.GL.GL_NORMALIZE;
import static javax.media.opengl.GL.GL_ONE_MINUS_SRC_ALPHA;
import static javax.media.opengl.GL.GL_POSITION;
import static javax.media.opengl.GL.GL_SHININESS;
import static javax.media.opengl.GL.GL_SMOOTH;
import static javax.media.opengl.GL.GL_SPECULAR;
import static javax.media.opengl.GL.GL_SRC_ALPHA;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.swing.JComponent;

import koebe.KoebePolyhedron.KoebePolyhedronContext;
import koebe.frontend.content.Viewer;
import koebe.frontend.content.joglviewer.gl.GraphRenderer;
import koebe.frontend.content.joglviewer.gl.Grid;
import koebe.frontend.content.joglviewer.gl.Polyhedron;
import koebe.frontend.content.joglviewer.gl.ProjectedCirclePattern;
import koebe.frontend.controller.MainController;
import circlepatterns.graph.CPEdge;
import circlepatterns.graph.CPFace;
import circlepatterns.graph.CPVertex;

import com.sun.opengl.util.GLUT;


/**
 * The OpenGL view for the koebe polyhedron application
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 */
@SuppressWarnings("serial")
public class KoebePolyhedronView extends Mouse3DView implements Viewer, MouseWheelListener{

	private GLUT 
		glut = new GLUT();
	
	// renderer presets
	private float[]
	    lightPos1 = {20f, 20f, -20f, 1},
	    lightPos2 = {-20f, 20f, 0, 1},
	    lightPos3 = {20, -20, 10, 1};
	private MainController
		controller = null;
	private float
		graphScale = 400,
		mainScale = 10;
	protected boolean
		antialias = true,
		showGraph = false,
		showMedial = false,
		showCircles = false,
		showPolyeder = true,
		showSphere = false,
		showGrid = false;
	
	private Color
		faceColor = new Color(0.5f, 0.5f, 0.8f, 0.8f),
		sphereColor = new Color(1f, 0.6f, 0.6f, 0.4f);
	
	// content renderer
	private ProjectedCirclePattern
		circelPattern = new ProjectedCirclePattern();
	private Grid
		grid = new Grid(20, 20, 10, -0.0005f);
	private GraphRenderer
		graph = new GraphRenderer(graphScale);
	private GraphRenderer
		medial = new GraphRenderer(1);
	private Polyhedron
		polyeder = new Polyhedron();
	
	// options GUI
	private OptionPanel
		viewOptPanel = null;


	public KoebePolyhedronView(MainController controller){
		this.controller = controller;
		viewOptPanel = new OptionPanel(this);
		add(viewOptPanel, BorderLayout.SOUTH);
		viewOptPanel.setShrinked(true);
		addMouseWheelListener(this);
	}
	
	
	
	public void initRender(GLAutoDrawable draw){
		GL gl = draw.getGL();
		gl.glShadeModel(GL_SMOOTH);
		if (antialias){
			gl.glEnable(GL_LINE_SMOOTH);
			gl.glEnable(GL.GL_BLEND);
			gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
			gl.glHint(GL.GL_LINE_SMOOTH_HINT, GL.GL_NICEST);
		} else {
			gl.glDisable(GL_LINE_SMOOTH);
			gl.glDisable(GL.GL_BLEND);
		}
		gl.glHint(GL_LINE_SMOOTH_HINT, GL_FASTEST);
	}
	
	
	
	public void render(GLAutoDrawable draw) {
		GL gl = draw.getGL();
		gl.glScalef(mainScale, mainScale, mainScale);
		
		initRender(draw);
		
		gl.glPushAttrib(GL_ALL_ATTRIB_BITS);
		setColor(controller.getColorController().getGridColor(), draw);
		if (showGrid)
			grid.render(draw);
		gl.glPopAttrib();
		
		gl.glPushAttrib(GL_ALL_ATTRIB_BITS);
		if (showGraph)
			graph.render(draw);
		gl.glPopAttrib();
		
		gl.glPushAttrib(GL_ALL_ATTRIB_BITS);
		if (showMedial)
			medial.render(draw);
		gl.glPopAttrib();
		
		gl.glPushAttrib(GL_ALL_ATTRIB_BITS);
		lightOn(draw);
		setColor(faceColor, draw);
		if (showPolyeder)	
			polyeder.render(draw);
		gl.glPopAttrib();
		
		gl.glPushAttrib(GL_ALL_ATTRIB_BITS);
		gl.glPushMatrix();
		lightOn(draw);
		setColor(sphereColor, draw);
		gl.glRotatef(90,1,0,0);
		if (showSphere)
			glut.glutSolidSphere(1.0, 50, 50);
		gl.glPopMatrix();
		gl.glPopAttrib();
		
		gl.glPushAttrib(GL_ALL_ATTRIB_BITS);
		if (showCircles)
			circelPattern.render(draw);
		gl.glPopAttrib();
	}


	/*
	 * put the lights on
	 */
	private void lightOn(GLAutoDrawable draw){
		GL gl = draw.getGL();
		gl.glLightfv(GL_LIGHT0, GL_POSITION, lightPos1, 0);
		gl.glLightfv(GL_LIGHT0, GL_DIFFUSE, new float[]{1, 1, 1, 1}, 0);
		gl.glLightfv(GL_LIGHT0, GL_SPECULAR, new float[]{1, 1, 1, 1}, 0);
		gl.glLightfv(GL_LIGHT0, GL_AMBIENT, new float[]{0.1f, 0.1f, 0.1f, 1}, 0);
		gl.glLightfv(GL_LIGHT1, GL_POSITION, lightPos2, 0);
		gl.glLightfv(GL_LIGHT1, GL_DIFFUSE, new float[]{0.8f, 0.8f, 0.8f, 1}, 0);
		gl.glLightfv(GL_LIGHT1, GL_SPECULAR, new float[]{0.7f, 0.7f, 0.7f, 1}, 0);
		gl.glLightfv(GL_LIGHT2, GL_POSITION, lightPos3, 0);
		gl.glLightfv(GL_LIGHT2, GL_DIFFUSE, new float[]{0.8f, 0.8f, 0.8f, 1}, 0);
		gl.glLightfv(GL_LIGHT2, GL_SPECULAR, new float[]{0.7f, 0.7f, 0.7f, 1}, 0);
		
		gl.glEnable(GL_LIGHT0);
		gl.glEnable(GL_LIGHT1);
		gl.glEnable(GL_LIGHT2);
		
		gl.glEnable(GL_LIGHTING);
	}
	
	
	
	private float[] 
	    c = new float[4];
	
	private void setColor(Color color, GLAutoDrawable draw){
		GL gl = draw.getGL();
		color.getColorComponents(c);
		if (color.getTransparency() == Color.TRANSLUCENT){
			gl.glEnable(GL_BLEND);
			gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
			c[3] = color.getAlpha() / 255.0f;
		}
		gl.glEnable(GL_NORMALIZE);
		gl.glMaterialfv(GL_FRONT, GL_AMBIENT_AND_DIFFUSE, c, 0);
		gl.glMaterialfv(GL_FRONT, GL_SPECULAR, c, 0);
		gl.glMaterialf(GL_FRONT, GL_SHININESS, 30);
		gl.glColor4fv(c, 0);
	}



	public void resetGeometry() {
		
	}
	


	public void updateGeometry(KoebePolyhedronContext<CPVertex, CPEdge, CPFace> context){
		circelPattern.setPolyederContext(context);
		medial.setGraph(context.getMedial());
		polyeder.setPolyederContext(context);
		graph.setGraph(controller.getEditedGraph());
		repaint();
	}
	

	public void mouseWheelMoved(MouseWheelEvent e) {
		if (e.getUnitsToScroll() < 0)
			mainScale /= 1.1;
		else
			mainScale *= 1.1;
		if (mainScale > 15)
			mainScale = 15;
		if (mainScale < 2)
			mainScale = 2;
		moveView();
	}



	public void update() {
		synchronized (this) {
			notify();
		}
	}



	public JComponent getViewerComponent() {
		return this;
	}



	public void displayChanged(GLAutoDrawable arg0, boolean arg1, boolean arg2) {
		
	}



	public MainController getController() {
		return controller;
	}

	
}