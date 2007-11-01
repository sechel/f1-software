package halfedge.frontend.action;

import java.io.File;

import javax.swing.filechooser.FileFilter;


/**
 * A custom file filter
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 */
public class ExtensionFileFilter extends FileFilter {

	private String 
		ext = "",
		descr = "";
	
	public ExtensionFileFilter(String ext, String description){
		this.ext = ext;
		this.descr = description;
	}
	
	
	public String getExtension() {
		return ext;
	}


	public void setExtension(String ext) {
		this.ext = ext;
	}


	public boolean accept(File f) {
		return f.getName().toLowerCase().endsWith(ext) || f.isDirectory();
	}

	public String getDescription() {
		return descr + " *." + ext;
	}

}
