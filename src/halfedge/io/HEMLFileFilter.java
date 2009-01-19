package halfedge.io;

import java.io.File;

import javax.swing.filechooser.FileFilter;

public class HEMLFileFilter extends FileFilter {

	@Override
	public boolean accept(File f) {
		return f.getName().toLowerCase().endsWith(".heml") || f.isDirectory();
	}

	@Override
	public String getDescription() {
		return "HEML File";
	}

}
