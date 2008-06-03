package minimalsurface.frontend;

import javax.swing.JFrame;
import javax.swing.UIManager;

import minimalsurface.frontend.content.MainWindow;
import util.debug.DBGTracer;
import de.javasoft.plaf.synthetica.SyntheticaStandardLookAndFeel;


/**
 * The main class for the Koebe polyhedron editor.
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 */
@SuppressWarnings("serial")
public class MinimalSurfaces{
	
	public static boolean
		isStandAlone = true;
	public static MainWindow
		mainWindow = null;
	
	
	static{
		try {
//			UIManager.setLookAndFeel("com.jgoodies.looks.plastic.PlasticLookAndFeel");
//			UIManager.setLookAndFeel("de.muntjak.tinylookandfeel.TinyLookAndFeel");
			SyntheticaStandardLookAndFeel.setAntiAliasEnabled(true);
			SyntheticaStandardLookAndFeel.setWindowsDecorated(false);
			SyntheticaStandardLookAndFeel.setExtendedFileChooserEnabled(true);
			SyntheticaStandardLookAndFeel.setUseSystemFileIcons(true);
			UIManager.setLookAndFeel("de.javasoft.plaf.synthetica.SyntheticaGreenDreamLookAndFeel");
		} catch (Exception e) {}
	}
	
	
	public static void main(String[] args) {
		DBGTracer.setActive(false);
		mainWindow = new MainWindow();
		if (isStandAlone)
			mainWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainWindow.setVisible(true);
	}

	public static MainWindow getMainWindow() {
		return mainWindow;
	}
}