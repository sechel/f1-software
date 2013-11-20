package minimalsurface.frontend.macro;

import halfedge.HalfEdgeDataStructure;
import image.ImageHook;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.vecmath.Point4d;

import circlepatterns.graph.CPEdge;
import circlepatterns.graph.CPFace;
import circlepatterns.graph.CPVertex;
import de.jreality.math.P3;
import de.jreality.math.Rn;

public class CalculateDiagonalIntersections extends MacroAction {

	protected static Icon 
		icon = new ImageIcon(ImageHook.getImage("vertexsubdivide.png"));
	
	@Override
	public String getName() {
		return "Calculate Diagonal Intersections";
	}

	@Override
	public HalfEdgeDataStructure<CPVertex, CPEdge, CPFace> process(
		HalfEdgeDataStructure<CPVertex, CPEdge, CPFace> graph
	) throws Exception {
		for (CPFace f : graph.getFaces()) {
			if (f.getBoundary().size() == 4) {
				calculateAndSetDiagonalIntersection(f);
			}
		}
		return graph;
	}
	
	
	static void calculateAndSetDiagonalIntersection(CPFace f) {
		Point4d a = f.getBoundary().get(0).getStartVertex().getXYZW();
		Point4d b = f.getBoundary().get(1).getStartVertex().getXYZW();
		Point4d c = f.getBoundary().get(2).getStartVertex().getXYZW();
		Point4d d = f.getBoundary().get(3).getStartVertex().getXYZW();
		Point4d r = calculateDiagonalIntersection(a, b, c, d);
		f.setXYZW(r);
	}
	
	static Point4d calculateDiagonalIntersection(Point4d pa, Point4d pb, Point4d pc, Point4d pd) {
		double[] a = {pa.x, pa.y, pa.z, pa.w};
		double[] b = {pb.x, pb.y, pb.z, pb.w};
		double[] c = {pc.x, pc.y, pc.z, pc.w};
		double[] d = {pd.x, pd.y, pd.z, pd.w};
		double[] pf = P3.planeFromPoints(null, a, b, c);
		double[] ab = Rn.subtract(null, b, a);
		double[] ad = Rn.subtract(null, d, a);
		double[] n = Rn.crossProduct(null, ab, ad);
		Rn.normalize(n, n);
		double[] an = Rn.add(null, a, n);
		double[] bn = Rn.add(null, b, n);
		double[] p1 = P3.planeFromPoints(null, a, an, c);
		double[] p2 = P3.planeFromPoints(null, b, bn, d);
		double[] r = P3.pointFromPlanes(null, p1, p2, pf);
		return new Point4d(r);
	}
	
	@Override
	public Icon getIcon() {
		return icon;
	}

}
