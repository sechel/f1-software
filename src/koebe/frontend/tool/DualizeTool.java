package koebe.frontend.tool;

import static de.jreality.scene.data.Attribute.COORDINATES;
import static de.jreality.scene.data.Attribute.INDICES;
import halfedge.HalfEdgeDataStructure;
import halfedge.frontend.content.GraphGraphics;
import halfedge.frontend.graphtool.EditOperation;
import halfedge.frontend.graphtool.EditOperationException;
import halfedge.frontend.graphtool.GraphTool;
import halfedge.surfaceutilities.Converter;
import halfedge.surfaceutilities.Converter.PositionConverter;

import java.util.List;

import javax.swing.Icon;
import javax.swing.JPanel;
import javax.vecmath.Point4d;

import koebe.frontend.content.jrealityviewer.KoebePolyhedronView;
import koebe.frontend.controller.MainController;
import math.util.VecmathTools;
import circlepatterns.graph.CPEdge;
import circlepatterns.graph.CPFace;
import circlepatterns.graph.CPVertex;
import de.jreality.geometry.IndexedFaceSetUtility;
import de.jreality.math.Rn;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.data.DoubleArrayArray;
import de.jreality.scene.data.IntArrayArray;

public class DualizeTool implements GraphTool<CPVertex, CPEdge, CPFace> {

	private MainController
		controller = null;
	
	
	private KoebePolyhedronView
		jRViewer = null;
	
	public DualizeTool(KoebePolyhedronView viewer) {
		jRViewer = viewer;
	}
	
	
	public Boolean initTool() {
		if (jRViewer.getPolyhedron() == null) {
			controller.setStatus("No active polyhedron!");
			return false;
		}
		IndexedFaceSet ifs = jRViewer.getPolyhedron();
		double[][] vertices = ifs.getVertexAttributes(COORDINATES).toDoubleArrayArray(null);
		int[][] faces = ifs.getFaceAttributes(INDICES).toIntArrayArray(null);
		
		for (double[] v : vertices)
			Rn.normalize(v, v);
		
		double[][] newVertices = new double[faces.length][];
		int[][] newFaces = new int[vertices.length][];
		
		// calculate the dual vertex as the intersection of tangent planes
		for (int i = 0; i < faces.length; i++) {
			int[] f = faces[i];
			if (f.length != 3) {
				controller.setStatus("Polyhedron is no triangulation!");
				return false;
			}
			double[][] triangle = new double[][] {vertices[f[0]], vertices[f[1]], vertices[f[2]]};
			double[] c = Trigonometrie.getCircumCenter(triangle);
			Point4d p = new Point4d(c[0], c[1], c[2], 1.0);
			VecmathTools.sphereMirror(p);
			newVertices[i] = new double[] {p.x, p.y, p.z};
		}
		
		PositionConverter<CPVertex> pConv = new Converter.PositionConverter<CPVertex>() {

			public double[] getPosition(CPVertex v) {
				Point4d p = v.getXYZW();
				VecmathTools.dehomogenize(p);
				double[] ret = new double[] {p.x, p.y, p.z};
				return ret;
			}

			public void setPosition(CPVertex v, double[] pos) {
				v.getXYZW().x = pos[0];
				v.getXYZW().y = pos[1];
				v.getXYZW().z = pos[2];
				v.getXYZW().w = 1.0;
			}
			
		};
		
		HalfEdgeDataStructure<CPVertex, CPEdge, CPFace> heds = Converter.ifs2heds(vertices, faces, CPVertex.class, CPEdge.class, CPFace.class, pConv); 
		
		for (CPVertex v : heds.getVertices()) {
			List<CPFace> fStar = v.getFaceStar();
			int[] face = new int[fStar.size()];
			for (int i = 0; i < fStar.size(); i++)
				face[fStar.size() - i - 1] = fStar.get(i).getIndex();
			newFaces[v.getIndex()] = face;
		}
		
		ifs.setVertexCountAndAttributes(COORDINATES, new DoubleArrayArray.Array(newVertices));
		ifs.setFaceCountAndAttributes(INDICES, new IntArrayArray.Array(newFaces));
		IndexedFaceSetUtility.calculateAndSetFaceNormals(ifs);
		jRViewer.setPolyhedron(ifs);
		return true;
	}
	
	public void commitEdit(HalfEdgeDataStructure<CPVertex, CPEdge, CPFace> graph) {
		
	}

	public String getDescription() {
		return getShortDescription();
	}

	public Icon getIcon() {
		return null;
	}

	public String getName() {
		return "Dualize";
	}

	public JPanel getOptionPanel() {
		return null;
	}

	public String getShortDescription() {
		return getName();
	}


	public void leaveTool() {

	}

	public boolean needsRepaint() {
		return true;
	}

	public void paint(GraphGraphics g) {

	}

	public boolean processEditOperation(EditOperation operation)
			throws EditOperationException {
		return false;
	}

	public void resetTool() {

	}

	public void setController(halfedge.frontend.controller.MainController<CPVertex, CPEdge, CPFace> controller) {
		this.controller = (MainController)controller;
	}
	

}
