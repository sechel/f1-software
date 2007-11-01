package halfedge.frontend.controller;

import static java.awt.Font.BOLD;

import java.awt.Font;


/**
 * Defines the fonts used for edge and vertex indices
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 */
public class FontController {

	private Font
		indexFont = new Font("Arial", BOLD, 10);

	public Font getIndexFont() {
		return indexFont;
	}

	public void setIndexFont(Font indexFont) {
		this.indexFont = indexFont;
	}
	
}
