package circlepatterns.frontend.content;

import halfedge.HalfEdgeDataStructure;

import java.awt.GridLayout;

import javax.swing.JLabel;

import circlepatterns.frontend.CPTestSuite;
import circlepatterns.graph.CPEdge;
import circlepatterns.graph.CPFace;
import circlepatterns.graph.CPVertex;


/**
 * Info panel
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 */
public class TopologyInfoShrinker extends ShrinkPanel {

	private static final long 
		serialVersionUID = 1L;

	public TopologyInfoShrinker() {
		super("Topology");
		updateLayout();
	}

	
	public void updateLayout(){
		removeAll();
		if (CPTestSuite.getTopology() != null){
			HalfEdgeDataStructure<CPVertex, CPEdge, CPFace> top = CPTestSuite.getTopology();
			setLayout(new GridLayout(3, 2));
			add(new JLabel("Vertices: "));
			add(new JLabel(top.getNumVertices() + ""));
			add(new JLabel("Edges: "));
			add(new JLabel(top.getNumEdges() + ""));
			add(new JLabel("Faces: "));
			add(new JLabel(top.getNumFaces() + ""));
		} else
			add(new JLabel("No topology loaded"));
		updateShrinkPanel();
	}
	
	
	
	
	
}
