package image;
import java.awt.Image;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

/**
 * An image loader for jar files
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 */
public class ImageHook {


	public static Image getImage(String filename){
		InputStream in = ImageHook.class.getResourceAsStream(filename);
		if (in == null)
			return null;
		Image result = null;
		try {
			result = ImageIO.read(in);
		} catch (IOException e) {}
		return result;
	}
	
	
}
