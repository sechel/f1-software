package teamgeist.frontend.controller;


/**
 * Controls the appearance of vertices, edges and faces. It holds
 * line widths and view flags.
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 */
public class AppearanceController {

	private boolean
		antialiasing = true,
		showVertexIndices = false,
		showEdgeIndices = false;
	private double
		lineWidth = 2,
		hoverLineWidth = 2;

	public boolean isAntialiasing() {
		return antialiasing;
	}

	public void setAntialiasing(boolean antialiasing) {
		this.antialiasing = antialiasing;
	}

	public boolean isShowVertexIndices() {
		return showVertexIndices;
	}

	public void setShowVertexIndices(boolean showIndices) {
		this.showVertexIndices = showIndices;
	}

	public double getHoverLineWidth() {
		return hoverLineWidth;
	}

	public double getLineWidth() {
		return lineWidth;
	}

	public void setHoverLineWidth(double hoverLineWidth) {
		this.hoverLineWidth = hoverLineWidth;
	}

	public void setLineWidth(double lineWidth) {
		this.lineWidth = lineWidth;
	}

	public boolean isShowEdgeIndices() {
		return showEdgeIndices;
	}

	public void setShowEdgeIndices(boolean showEdgeIndices) {
		this.showEdgeIndices = showEdgeIndices;
	}
	
	
}
