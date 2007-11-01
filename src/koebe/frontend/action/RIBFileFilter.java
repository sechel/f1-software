package koebe.frontend.action;

import java.io.File;

import javax.swing.filechooser.FileFilter;

/**
 * A rib file FileFilter
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 */
public class RIBFileFilter extends FileFilter {

	public boolean accept(File f) {
		return f.getName().toLowerCase().endsWith(".rib") || f.isDirectory();
	}

	public String getDescription() {
		return "Renderman File *.rib";
	}

}
