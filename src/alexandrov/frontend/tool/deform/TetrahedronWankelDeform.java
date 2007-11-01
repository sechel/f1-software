package alexandrov.frontend.tool.deform;

import halfedge.generator.SphericalTriangleGenerator;
import alexandrov.graph.CPMEdge;
import alexandrov.graph.CPMFace;
import alexandrov.graph.CPMVertex;
import alexandrov.io.CPMLWriter;

public class TetrahedronWankelDeform {
	

	private SphericalTriangleGenerator<CPMVertex, CPMEdge, CPMFace>
		gen = null;
	
	/**
	 * Creates a tetrahedron wankel polyhedron
	 * @param edgesPerSide needs to be 2*n
	 * @param scale the overall scale
	 */
	public TetrahedronWankelDeform(int edgesPerSide, double scale) {
		gen = new SphericalTriangleGenerator<CPMVertex, CPMEdge, CPMFace>(edgesPerSide, scale, CPMVertex.class, CPMEdge.class, CPMFace.class);
	}
	

	public String getCPML(){
		try {
			return CPMLWriter.convertToCPML(gen.getGraph());
		} catch (Exception e) {
			return null;
		}
	}
	
	
}
