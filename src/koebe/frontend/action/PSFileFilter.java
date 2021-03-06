package koebe.frontend.action;

import java.io.File;

import javax.swing.filechooser.FileFilter;


/**
 * A ps file FileFilter
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 */
public class PSFileFilter extends FileFilter {

	@Override
	public boolean accept(File f) {
		return f.getName().toLowerCase().endsWith(".ps") || f.isDirectory();
	}

	@Override
	public String getDescription() {
		return "PS File *.ps";
	}

}
