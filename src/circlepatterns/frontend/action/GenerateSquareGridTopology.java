package circlepatterns.frontend.action;

import halfedge.HalfEdgeDataStructure;
import halfedge.generator.SquareGridGenerator;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

import circlepatterns.frontend.CPTestSuite;
import circlepatterns.graph.CPEdge;
import circlepatterns.graph.CPFace;
import circlepatterns.graph.CPVertex;


/**
 * Generates a square grid topology
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 * @see halfedge.generator.SquareGridGenerator
 */
@SuppressWarnings("serial")
public class GenerateSquareGridTopology extends AbstractAction {

	private Integer 
		width = 10,
		height = 10;
	private Double
		eps1 = 0.2,
		eps2 = 0.5;


	public GenerateSquareGridTopology() {
		putValue(Action.NAME, "Generate Square Grid");
	}
	
	
	public void actionPerformed(ActionEvent e) {
		HalfEdgeDataStructure<CPVertex, CPEdge, CPFace> ds = SquareGridGenerator.generate(width, height, CPVertex.class, CPEdge.class, CPFace.class);

		SquareGridGenerator.setSquareGridThetas(ds, eps1, eps2);
		
		CPTestSuite.setActiveFile(null);
		CPTestSuite.setTopology(ds);
		CPTestSuite.updateEuclidean();
	}

	public Integer getHeight() {
		return height;
	}


	public void setHeight(Integer height) {
		this.height = height;
	}


	public Integer getWidth() {
		return width;
	}


	public void setWidth(Integer width) {
		this.width = width;
	}
	
	
	public Double getEps1() {
		return eps1;
	}


	public void setEps1(Double eps1) {
		this.eps1 = eps1;
	}


	public Double getEps2() {
		return eps2;
	}


	public void setEps2(Double eps2) {
		this.eps2 = eps2;
	}
}
