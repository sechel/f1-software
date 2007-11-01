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

public class CircleTriangleDeform {
	
	private int
		numSegments = 30;
	private double
		scale = 1.0;
	private LinkedList<Point2d>
		trianglePoints = null,
		circlePoints = null;
	
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
	 * Creates a deform of a triangle and a circle
	 * @param edgesPerSide needs to be divisibly by 2
	 */
	public CircleTriangleDeform(int edgesPerSide, double scale) {
		if (edgesPerSide % 2 != 0)
			throw new RuntimeException("edgesPerSide needs to be divisibly by 2");
		numSegments = edgesPerSide * 3;
		this.scale = scale;
		init();
	}
	
	
	private void init(){
		trianglePoints = new LinkedList<Point2d>();
		double segmentLength = getSegmentLength();
		double sideLength = segmentLength * numSegments / 3;
		int sideSegments = numSegments / 3;
		for (int i = 0; i < sideSegments; i++){
			trianglePoints.add(new Point2d(i * segmentLength, 0));
		}
		Point2d triangleTip = new Point2d(sideLength / 2, sin(PI/3) * sideLength);
		Point2d rightCorner = new Point2d(sideLength, 0);
		for (int i = 0; i < sideSegments; i++){
			double lin = i / (double)sideSegments;
			double x = (1-lin) * rightCorner.x + lin * triangleTip.x;
			double y = (1-lin) * rightCorner.y + lin * triangleTip.y;
			trianglePoints.add(new Point2d(x, y));
		}
		for (int i = 0; i < sideSegments; i++){
			double lin = i / (double)sideSegments;
			double x = (1-lin) * triangleTip.x;
			double y = (1-lin) * triangleTip.y;
			trianglePoints.add(new Point2d(x, y));
		}
		
		circlePoints = new LinkedList<Point2d>();
		for (int i = 0; i < numSegments; i++){
			double frac = i * 2*PI / numSegments;
			double x = cos(frac) * scale;
			double y = sin(frac) * scale;
			circlePoints.add(new Point2d(x, y));
		}
	}
	
	
	private double getTriangleEdgeLength(int s, int t){
		Point2d start = trianglePoints.get(s); 
		Point2d target = trianglePoints.get(t); 
		return start.distance(target);
	}
	
	private double getCircleEdgeLength(int s, int t){
		Point2d start = circlePoints.get(s); 
		Point2d target = circlePoints.get(t); 
		return start.distance(target);	
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
	
	
	private void makeTriangulation(StringBuffer edgeBuf, StringBuffer triBuf){
		//rim edges
		int sideSegments = numSegments / 3;
		double segmentLength = getSegmentLength();
		for (int i = 0; i < numSegments; i++)
			edgeList.add(segmentLength);
		
		//on circle
		int innner1 = triangulate(numSegments / 6, sideSegments - 1, true); 
		int innner2 = triangulate(3 * numSegments / 6, sideSegments - 1, true); 
		int innner3 = triangulate(5 * numSegments / 6, sideSegments - 1, true);
		Triangle innerTri = new Triangle();
		innerTri.a = innner1;
		innerTri.b = innner3;
		innerTri.c = innner2;
		triangles.add(innerTri);
		
		//on triangle
		innner1 = triangulate(0, sideSegments - 1, false); 
		innner2 = triangulate(numSegments / 3, sideSegments - 1, false); 
		innner3 = triangulate(2 * numSegments / 3, sideSegments - 1, false);
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
	
	private int triangulate(int mid, int count, boolean circle){
		int A = mid;
		int B = getPreviousOnRim(mid);
		int C = getNextOnRim(mid);
		int counter = 0;
		int a = B;
		while (counter < count){
			int lasta = a;
			if (circle)
				a = addEdge(getCircleEdgeLength(B, C));
			else
				a = addEdge(getTriangleEdgeLength(B, C));
			Triangle triangle = new Triangle();
			triangle.a = a;
			if (counter % 2 == 0){
				if (circle){
					triangle.b = A;
					triangle.c = lasta;
				} else {
					triangle.b = lasta;
					triangle.c = A;	
				}
				A = B;
				B = getPreviousOnRim(A);
			} else {
				if (circle){
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
	
	
	private double getSegmentLength(){
		return  scale * sqrt(2) * sqrt( 1 - cos(2 * PI / numSegments) );
	}
	
		
	public static void main(String[] args) {
		CircleTriangleDeform deform = new CircleTriangleDeform(6, 100.0);
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
