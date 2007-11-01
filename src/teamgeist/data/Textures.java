package teamgeist.data;

import static de.jreality.shader.CommonAttributes.POLYGON_SHADER;

import java.awt.Image;
import java.io.IOException;

import javax.imageio.ImageIO;

import de.jreality.scene.Appearance;
import de.jreality.shader.ImageData;
import de.jreality.shader.TextureUtility;

public class Textures {


	public static void setTexture(Appearance app, String texFile){
		try {
			Image image = ImageIO.read(Textures.class.getResourceAsStream(texFile));
			TextureUtility.createTexture(app, POLYGON_SHADER, new ImageData(image)); 
		} catch (IOException e) {
			System.err.println("Could not load texture: " + texFile);
		}
	}
	
}
