package koebe.frontend.content.joglviewer;

import javax.media.opengl.GLAutoDrawable;

/**
 * A renderer interface
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 */
public interface Renderer {

	
	public void render(GLAutoDrawable draw);
	
	
}
