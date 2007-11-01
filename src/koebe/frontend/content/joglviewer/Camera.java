package koebe.frontend.content.joglviewer;

import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;



/**
 * An OpenGL camera implementation
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 */
public class Camera{

	private GLU
		glu = new GLU();
	
	public final static int 
		CAMERA_FREE = 0,
		CAMERA_TARGET = 1;

	public int 
		timevalue = 0;
	public float 
		camera_near = 1.0f,
		camera_far = 1000.0f,
		camera_fov = 0.7854f,
		camera_tdist = 178.1420f;	
	public int 
		camera_type = 0;
	public Quaternion 
		orientation = new Quaternion();
	public float[]
		rotaxis = new float[4],
		position = new float[4];
	public float 
		rot_angle;
	

	public Camera(String name) {

	}

	public Camera(String name,float x,float y,float z,
						float axis_x,float axis_y,float axis_z,float angle,float fov) {
		setPosition(x, y, z);
		setRotation((float)Math.toRadians(angle), axis_x, axis_y, axis_z);
		camera_fov = (float)Math.toRadians(fov);
		orientation = Quaternion.getFromAngleAxis((float)Math.toRadians(angle),axis_x,axis_y,axis_z);

	}



	public void init() {
		orientation = Quaternion.getFromAngleAxis(getRotangle(), getRotaxis());		
	}



	public void setPosition(float x, float y, float z) {
		float pos[] = {x, y, z}; 
		setPosition(pos);
	}

	public void changePosition(float[] pos) {
		float new_pos[] = {getPosition()[0] + pos[0], getPosition()[1] + pos[1], getPosition()[2] + pos[2]};
		setPosition(new_pos);
	}


	public void setRotationAngleAxis(float angle, float axis[]){
		Quaternion rot = Quaternion.getFromAngleAxis(angle, axis);		
		Quaternion rot_90 = Quaternion.getFromEuler((float)Math.PI / 2, 0.0f, 0.0f);
		orientation = rot_90.multiply(rot);
		applyOrientation();
	}


	private void setRotation(float angle, float axis_x, float axis_y, float axis_z) {
		float[] axis = {axis_x, axis_y, axis_z};
		setRotaxis(axis);
		setRotangle(angle);
	} 
	
	private void setRotation(float[] rot) {
		setRotation(rot[0], rot[1], rot[2], rot[3]);
	}


	public void applyOrientation() {
		setRotation(orientation.toAngleAxis());
	}

	public void move_amount(float x_dif, float y_dif, float z_dif) {
		changePosition(orientation.affectPoint(x_dif, y_dif, z_dif));
	}

	public void rotate_camera_local(float x, float y, float z) {
		x = (float)Math.toRadians(x);
		y = (float)Math.toRadians(y);
		z = (float)Math.toRadians(z);
		orientation = orientation.multiply(Quaternion.getFromEuler(orientation.affectPoint(x, y, z)));
		applyOrientation();
	}

	public void rotate_camera_global(float x, float y, float z) {
		x = (float)Math.toRadians(x);
		y = (float)Math.toRadians(y);
		z = (float)Math.toRadians(z);
		orientation = orientation.multiply(Quaternion.getFromEuler(0.0f, y, 0.0f));
		float[] x_rot_affected = orientation.affectPoint(x, 0.0f, 0.0f);
		Quaternion x_rot = Quaternion.getFromEuler(x_rot_affected);
		orientation = orientation.multiply(x_rot);
		applyOrientation();
	}

	public void rotate_around_local(float x_pos,float y_pos,float z_pos,
									float x,float y,float z) {
		x = (float)Math.toRadians(x);
		y = (float)Math.toRadians(y);
		z = (float)Math.toRadians(z);
		float[] affected_rot = orientation.affectPoint(x, y, z);
		Quaternion rot = Quaternion.getFromEuler(affected_rot);
		float[] affected_point = rot.affectPoint(getPosition());
		setPosition(affected_point);
		orientation = orientation.multiply(rot);
		applyOrientation();
	}

	public void rotate_around_global(	float x_pos,float y_pos,float z_pos,
										float x,float y,float z) {
		y = (float)Math.toRadians(y);
		Quaternion y_rot = Quaternion.getFromEuler(0.0f, y, 0.0f);
		float[] affected_pos = y_rot.affectPoint(getPosition());
		setPosition(affected_pos);
		orientation = orientation.multiply(y_rot);
		x = (float)Math.toRadians(x);
		float[] x_rot_affected = orientation.affectPoint(x, 0.0f, 0.0f);
		Quaternion x_rot = Quaternion.getFromEuler(x_rot_affected);
		float[] affected_point = x_rot.affectPoint(getPosition());
		setPosition(affected_point);
		orientation = orientation.multiply(x_rot);
		applyOrientation();
	}

	//this is the transfor method of camera
	public void apply(GL gl, int width, int height) {
		gl.glMatrixMode(GL.GL_PROJECTION);
		gl.glLoadIdentity();
		float deg_fov = (float)Math.toDegrees(camera_fov);
		glu.gluPerspective(deg_fov, width / (float) height,
							camera_near <= 0.0 ? 1.0 : camera_near,
							camera_far * 100);
		gl.glMatrixMode(GL.GL_MODELVIEW);
		gl.glLoadIdentity();		
		gl.glRotatef((float)Math.toDegrees(getRotangle()), getRotaxis()[0], getRotaxis()[1], getRotaxis()[2]);
		gl.glTranslatef(-getPosition()[0], -getPosition()[1], -getPosition()[2]);
	}

	/**
	 * @return Returns the position.
	 */
	public float[] getPosition() {
		return position;
	}
	/**
	 * @param position The position to set.
	 */
	public void setPosition(float[] position) {
		this.position = position;
	}
	/**
	 * @return Returns the rot_angle.
	 */
	public float getRotangle() {
		return rot_angle;
	}
	/**
	 * @param rot_angle The rot_angle to set.
	 */
	public void setRotangle(float rot_angle) {
		this.rot_angle = rot_angle;
	}
	/**
	 * @return Returns the rotaxis.
	 */
	public float[] getRotaxis() {
		return rotaxis;
	}
	/**
	 * @param rotaxis The rotaxis to set.
	 */
	public void setRotaxis(float[] rotaxis) {
		this.rotaxis = rotaxis;
	}
}
