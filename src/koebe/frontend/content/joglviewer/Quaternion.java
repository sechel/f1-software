package koebe.frontend.content.joglviewer;


/**
 * A quaternion implementation
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 */
public class Quaternion implements java.io.Serializable{

	private static final long 
		serialVersionUID = 1L;
	private double w = 0.0f;
	private double vec[] = {1.0f, 0.0f, 0.0f};

	public Quaternion(double w, double axis_x, double axis_y, double axis_z) {
		this.w = w;
		vec[0] = axis_x;
		vec[1] = axis_y;
		vec[2] = axis_z;
	}

	public Quaternion(double angle, double[] vec) {
		w = angle;
		this.vec = vec.clone();
	}

	public Quaternion() {
	}


	public double getAngle() {
		return w;
	}
	public double[] getVector() {
		return vec;
	}
	public void setAngle(double angle) {
		w = angle;
	}
	public void setVector(double[] vec) {
		this.vec = vec;
	}

	public Quaternion multiply(Quaternion m) {
		Quaternion result = new Quaternion();
		double tmp_w = w * m.getAngle()
				- vec[0] * m.getVector()[0]
				- vec[1] * m.getVector()[1]
				- vec[2] * m.getVector()[2];
		double tmp_x = w * m.getVector()[0]
				+ vec[0] * m.getAngle()
				+ vec[1] * m.getVector()[2]
				- vec[2] * m.getVector()[1];
		double tmp_y = w * m.getVector()[1]
				+ vec[1] * m.getAngle()
				+ vec[2] * m.getVector()[0]
				- vec[0] * m.getVector()[2];
		double tmp_z = w * m.getVector()[2]
				+ vec[2] * m.getAngle()
				+ vec[0] * m.getVector()[1]
				- vec[1] * m.getVector()[0];
		double[] new_vec = { tmp_x, tmp_y, tmp_z };
		result.setVector(new_vec);
		result.setAngle(tmp_w);
		return result;
	}

	public Quaternion add(Quaternion a) {
		w += a.getAngle();
		vec[0] = vec[0] + a.getVector()[0];
		vec[1] = vec[1] + a.getVector()[1];
		vec[2] = vec[2] + a.getVector()[2];
		return this;
	}

	public float[] affectPoint(float[] point) {
		double[] result = new double[3];
		double x = vec[0];
		double y = vec[1];
		double z = vec[2];
		result[0] = (w * w + x * x - y * y - z * z) * point[0]
				  + (2 * x * y + 2 * w * z) * point[1]
			      + (2 * x * z - 2 * w * y) * point[2];
		result[1] = (2 * x * y - 2 * w * z) * point[0]
				  + (w * w - x * x + y * y - z * z) * point[1]
				  + (2 * y * z + 2 * w * x) * point[2];
		result[2] = (2 * x * z + 2 * w * y) * point[0]
				  + (2 * y * z - 2 * w * x) * point[1]
				  + (w * w - x * x - y * y + z * z) * point[2];
		return doubleToFloat(result);
	}

	public float[] affectPoint(float x, float y, float z) {
		float[] tmp = new float[3];
		tmp[0] = x; tmp[1] = y; tmp[2] = z;
		return affectPoint(tmp);
	}

	public float[] toAngleAxis() {
		double[] result = new double[4];
		double sin_angle = sin(acos(w));
		result[0] = 2 * acos(w);
		result[1] = vec[0] / sin_angle;
		result[2] = vec[1] / sin_angle;
		result[3] = vec[2] / sin_angle;
		return doubleToFloat(result);
	}


	public float[] toEuler(){
 		double[] result = new double[3];	
 		double w = -this.w;
    	double sqw = w*w;    
    	double sqx = vec[0]*vec[0];   
    	double sqy = vec[1]*vec[1];   
    	double sqz = vec[2]*vec[2]; 
       	result[1] = asin(-2.0 * (vec[0]*vec[2] - vec[1]*w));   	
    	result[2] = atan2(2.0 * (vec[0]*vec[1] + vec[2]*w),(sqx - sqy - sqz + sqw));   
    	result[0] = atan2(2.0 * (vec[1]*vec[2] + vec[0]*w),(-sqx - sqy + sqz + sqw));    
    	return doubleToFloat(result);	
	}


	public static Quaternion getFromEuler(float[] e) {
		Quaternion qx = new Quaternion(cos(e[0] / 2), sin(e[0] / 2), 0.0f, 0.0f);
		Quaternion qy = new Quaternion(cos(e[1] / 2), 0.0f, sin(e[1] / 2), 0.0f);
		Quaternion qz = new Quaternion(cos(e[2] / 2), 0.0f, 0.0f, sin(e[2] / 2));
		return qx.multiply(qy).multiply(qz);
	}

	public static Quaternion getFromEuler(float x_rot, float y_rot, float z_rot) {
		float[] tmp = new float[3];
		tmp[0] = x_rot; tmp[1] = y_rot; tmp[2] = z_rot;
		return getFromEuler(tmp);
	}

	public static Quaternion getFromAngleAxis(float[] a) {
		Quaternion result = new Quaternion();
		double sin_a = sin(a[0] / 2);
		double cos_a = cos(a[0] / 2);
		result.setAngle(cos_a);
		double[] tmp_vec = new double[3];
		tmp_vec[0] = a[1] * sin_a;
		tmp_vec[1] = a[2] * sin_a;
		tmp_vec[2] = a[3] * sin_a;
		result.setVector(tmp_vec);
		return result;
	}

	public static Quaternion getFromAngleAxis(float angle, float axis_x, float axis_y, float axis_z) {
		float[] tmp = new float[4];
		tmp[0] = angle; tmp[1] = axis_x; tmp[2] = axis_y; tmp[3] = axis_z;
		return getFromAngleAxis(tmp);
	}

	public static Quaternion getFromAngleAxis(float angle, float[] axis) {
		float[] tmp = new float[4];
		tmp[0] = angle; tmp[1] = axis[0]; tmp[2] = axis[1]; tmp[3] = axis[2];
		return getFromAngleAxis(tmp);
	}



	private static float[] doubleToFloat(double[] vec){
		float[] result = new float[vec.length];
		for (int i = 0; i < vec.length; i++)
			result[i] = (float)vec[i];
		return result;		
	} 

	
	public static float sin(double value) {
		return (float) StrictMath.sin(value);
	}
	public static float cos(double value) {
		return (float) StrictMath.cos(value);
	}
	public static float asin(double value) {
		return (float) StrictMath.asin(value);
	}
	public static float acos(double value) {
		return (float) StrictMath.acos(value);
	}
	public static float tan(double value) {
		return (float) StrictMath.tan(value);
	}
	public static float atan2(double value, double value2) {
		return (float) StrictMath.atan2(value, value2);
	}	

	@Override
	public String toString() {
		float[] angax = toAngleAxis();
		return "Orientation: " + (float)Math.toDegrees(angax[0]) + "Grad um "
			+ angax[1] + ", "
			+ angax[2] + ", "
			+ angax[3];
	}

}
