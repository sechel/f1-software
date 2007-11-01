package alexandrov.frontend.tool.deform;

import static java.lang.Math.PI;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.LinkedList;

import javax.vecmath.Point2d;

public class WankelDeform {
	
	private int
		numSegments = 30,
		twist = 5;
	private double
		scale = 1.0;
	private LinkedList<Point2d>
		wankelPoints = null;
	
	private LinkedList<Double>	
		edgeList = new LinkedList<Double>();
	private LinkedList<Triangle>
		triangles = new LinkedList<Triangle>();
	
	private String
		cpmlRepresentation = null;
	
	
	private class Triangle{
		public int a, b, c;
	}
	
	
	/**
	 * Creates a deform of a wankel triangle
	 * @param edgesPerSide needs to be divisibly by 2
	 */
	public WankelDeform(int edgesPerSide, int twist, double scale) {
		if (edgesPerSide % 2 != 0)
			throw new RuntimeException("edgesPerSide needs to be divisibly by 2");
		numSegments = edgesPerSide * 3;
		this.scale = scale;
		this.twist = twist;
		init();
	}
	
	
	private void init(){
		wankelPoints = new LinkedList<Point2d>();
		int sideSegments = numSegments / 3;
		// right side
		for (int i = 0; i < sideSegments; i++){
			double x = cos(PI/3 * i / sideSegments);
			double y = sin(PI/3 * i / sideSegments);
			wankelPoints.add(new Point2d(x, y));
		}
		//left side
		for (int i = 0; i < sideSegments; i++){
			double x = cos(PI * (i / (sideSegments * 3.0) + 2/3.0));
			double y = sin(PI * (i / (sideSegments * 3.0) + 2/3.0));
			wankelPoints.add(new Point2d(x + 1, y));
		}
		//bottom
		Point2d bottomMid = new Point2d(cos(PI/3), sin(PI/3));
		for (int i = 0; i < sideSegments; i++){
			double x = cos(PI * (i / (sideSegments * 3.0) + 4/3.0));
			double y = sin(PI * (i / (sideSegments * 3.0) + 4/3.0));
			wankelPoints.add(new Point2d(x + bottomMid.x, y + bottomMid.y));
		}	
	}
	

	private double getEdgeLength(int s, int t){
		if (s < 0) s = numSegments + s;
		if (t < 0) t = numSegments + t;
		Point2d start = wankelPoints.get(s % numSegments); 
		Point2d target = wankelPoints.get(t % numSegments); 
		return start.distance(target) * scale;	
	}

	
	public String getCPML(){
		if (cpmlRepresentation == null){
			StringBuffer result = new StringBuffer();
			result.append("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n" +
						  "<!DOCTYPE convexPolyhedralMetric>\n" +
						  "<cpml description=\"circle triangle deform\">\n");
			StringBuffer edgeListBuffer = new StringBuffer();
			StringBuffer triangleBuffer = new StringBuffer();
			makeTriangulation(edgeListBuffer, triangleBuffer);
			result.append("\t<edgelist>\n");
			result.append(edgeListBuffer.toString());
			result.append("\t</edgelist>\n");
			result.append("\t<trianglelist>\n");
			result.append(triangleBuffer.toString());
			result.append("\t</trianglelist>\n");
			result.append("</cpml>\n");
			cpmlRepresentation = result.toString();
		}
		return cpmlRepresentation;
	}
	
	
	private int addEdge(double length){
		edgeList.add(length);
		return edgeList.size() - 1;
	}
	
	
	private double getSegmentLength(){
		return  sqrt(2) * sqrt( 1 - cos(PI / numSegments) ) * scale;
	}
	
	private void makeTriangulation(StringBuffer edgeBuf, StringBuffer triBuf){
		//rim edges
		int sideSegments = numSegments / 3;
		double segmentLength = getSegmentLength();
		for (int i = 0; i < numSegments; i++)
			edgeList.add(segmentLength);
		
		//on upper wankel
		int innner1 = triangulate(twist, sideSegments - 1, true); 
		int innner2 = triangulate(sideSegments + twist, sideSegments - 1, true); 
		int innner3 = triangulate(2 * sideSegments + twist, sideSegments - 1, true);
		Triangle innerTri = new Triangle();
		innerTri.a = innner1;
		innerTri.b = innner3;
		innerTri.c = innner2;
		triangles.add(innerTri);
		
		//on lower wankel
		innner1 = triangulate(0, sideSegments - 1, false); 
		innner2 = triangulate(sideSegments, sideSegments - 1, false); 
		innner3 = triangulate(2 * sideSegments, sideSegments - 1, false);
		innerTri = new Triangle();
		innerTri.a = innner1;
		innerTri.b = innner2;
		innerTri.c = innner3;
		triangles.add(innerTri);	
		
		int index = 0;
		for (Double length : edgeList){
			BigDecimal round = new BigDecimal(length);
			round = round.round(new MathContext(10));
			if (index >= numSegments){
				edgeBuf.append("\t\t<edge length=\"" + round + "\" index=\"" + index + "\">\n");
				edgeBuf.append("\t\t\t<property name=\"hidden\" type=\"boolean\" value=\"true\"/>\n");
				edgeBuf.append("\t\t</edge>\n");
			} else
				edgeBuf.append("\t\t<edge length=\"" + round + "\" index=\"" + index + "\"/>\n");
			index++;
		}
		index = 0;
		for (Triangle tri : triangles){
			triBuf.append("\t\t<triangle a=\"" + tri.a + "\" b=\"" + tri.b + "\" c=\"" + tri.c + "\" index=\"" + index + "\"/>\n");
			index++;
		}
	}
	
	
	private int getNextOnRim(int vertex){
		return (vertex + 1) % numSegments;
	}
	
	private int getPreviousOnRim(int vertex){
		if (vertex - 1 < 0)
			return numSegments - 1;
		else
			return vertex - 1;
	}
	
	private int triangulate(int mid, int count, boolean upper){
		int A = mid;
		int B = getPreviousOnRim(mid);
		int C = getNextOnRim(mid);
		int counter = 0;
		int a = B;
		while (counter < count){
			int lasta = a;
			if (upper)
				a = addEdge(getEdgeLength(B - twist, C - twist)); 
			else
				a = addEdge(getEdgeLength(B, C));
			Triangle triangle = new Triangle();
			triangle.a = a;
			if (counter % 2 == 0){
				if (upper){
					triangle.b = A;
					triangle.c = lasta;
				} else {
					triangle.b = lasta;
					triangle.c = A;	
				}
				A = B;
				B = getPreviousOnRim(A);
			} else {
				if (upper){
					triangle.b = lasta;
					triangle.c = B;
				} else {
					triangle.b = B;
					triangle.c = lasta;
				}
				A = C;
				C = getNextOnRim(A);
			}
			triangles.add(triangle);
			counter++;
		}
		return a;
	}
	

		
	public static void main(String[] args) {
		WankelDeform deform = new WankelDeform(6, 3, 100.0);
		File deformTestFile = new File("data/cpml/circleTriangleTest.cpml");
		try {
			DataOutputStream das = new DataOutputStream(new FileOutputStream(deformTestFile));
			das.writeBytes(deform.getCPML());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println(deform.getCPML());
	}
	
}
