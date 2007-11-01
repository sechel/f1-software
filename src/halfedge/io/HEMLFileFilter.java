package halfedge.io;

import java.io.File;

import javax.swing.filechooser.FileFilter;

public class HEMLFileFilter extends FileFilter {

	public boolean accept(File f) {
		return f.getName().toLowerCase().endsWith(".heml") || f.isDirectory();
	}

	public String getDescription() {
		return "HEML File";
	}

}
