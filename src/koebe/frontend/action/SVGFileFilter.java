package koebe.frontend.action;

import java.io.File;

import javax.swing.filechooser.FileFilter;


/**
 * A ps file FileFilter
 * @author Kristoffer Josefsson
 */
public class SVGFileFilter extends FileFilter {

	@Override
	public boolean accept(File f) {
		return f.getName().toLowerCase().endsWith(".svg") || f.isDirectory();
	}

	@Override
	public String getDescription() {
		return "SVG File *.svg";
	}

}
