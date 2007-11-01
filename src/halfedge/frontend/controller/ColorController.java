package halfedge.frontend.controller;

import java.awt.Color;


/**
 * Defines the colors used in the editor
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 */
public class ColorController {

	
	private Color
		faceColor = new Color(0x506994af, true),
		edgeColor = new Color(0x000000),
		vertexColor = new Color(0x3c7c1f),
		hoverColor = new Color(0xCCc64141, true),
		selectColor = new Color(0xc64141),
		backgroundColor = new Color(0xcecece),//UIManager.getColor("Panel.background"),
		faceActionColor = new Color(0x306994af, true),
		edgeActionColor = new Color(0x5b5b5b),
		vertexActionColor = new Color(0x7ea06d),
		indexColor = new Color(0xFF0000),
		gridColor = new Color(0xbbbbbb);
	
	
	public Color getIndexColor() {
		return indexColor;
	}

	public void setIndexColor(Color indexColor) {
		this.indexColor = indexColor;
	}

	public Color getEdgeColor() {
		return edgeColor;
	}

	public void setEdgeColor(Color edgeColor) {
		this.edgeColor = edgeColor;
	}

	public Color getFaceColor() {
		return faceColor;
	}

	public void setFaceColor(Color faceColor) {
		this.faceColor = faceColor;
	}

	public Color getHoverColor() {
		return hoverColor;
	}

	public void setHoverColor(Color hoverColor) {
		this.hoverColor = hoverColor;
	}

	public Color getSelectColor() {
		return selectColor;
	}

	public void setSelectColor(Color selectColor) {
		this.selectColor = selectColor;
	}

	public Color getVertexColor() {
		return vertexColor;
	}

	public void setVertexColor(Color vertexColor) {
		this.vertexColor = vertexColor;
	}

	public Color getBackgroundColor() {
		return backgroundColor;
	}

	public void setBackgroundColor(Color backgroundColor) {
		this.backgroundColor = backgroundColor;
	}

	public Color getEdgeActionColor() {
		return edgeActionColor;
	}

	public void setEdgeActionColor(Color edgeActionColor) {
		this.edgeActionColor = edgeActionColor;
	}

	public Color getFaceActionColor() {
		return faceActionColor;
	}

	public void setFaceActionColor(Color faceActionColor) {
		this.faceActionColor = faceActionColor;
	}

	public Color getVertexActionColor() {
		return vertexActionColor;
	}

	public void setVertexActionColor(Color vertexActionColor) {
		this.vertexActionColor = vertexActionColor;
	}

	public Color getGridColor() {
		return gridColor;
	}

	public void setGridColor(Color gridColor) {
		this.gridColor = gridColor;
	}


	
}
