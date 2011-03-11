package minimalsurface.frontend;

import javax.swing.JFrame;
import javax.swing.UIManager;

import minimalsurface.frontend.content.MainWindow;
import util.debug.DBGTracer;
import de.javasoft.plaf.synthetica.SyntheticaLookAndFeel;
import de.jreality.plugin.JRViewer;


/**
 * The main class for the minimal surfaces editor.
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 */
public class MinimalSurfaces{
	
	public static boolean
		isStandAlone = true;
	public static MainWindow
		mainWindow = null;
	
	
	static{
		try {
//			JFrame.setDefaultLookAndFeelDecorated(true);
//			JDialog.setDefaultLookAndFeelDecorated(true);
//			UIManager.setLookAndFeel("org.jvnet.substance.skin.SubstanceOfficeSilver2007LookAndFeel");
//			SubstanceLookAndFeel.setCurrentTheme(new SubstanceSteelBlueTheme());
//			SubstanceLookAndFeel.setCurrentButtonShaper(new ClassicButtonShaper());
//			SubstanceLookAndFeel.setCurrentDecorationPainter(new Glass3DDecorationPainter());
//			SubstanceLookAndFeel.setCurrentGradientPainter(new GlassGradientPainter());
//			SubstanceLookAndFeel.setCurrentHighlightPainter(new GlassHighlightPainter());
//			UIManager.setLookAndFeel("com.jgoodies.looks.plastic.PlasticLookAndFeel");
//			UIManager.setLookAndFeel("de.muntjak.tinylookandfeel.TinyLookAndFeel");
			SyntheticaLookAndFeel.setAntiAliasEnabled(true);
			SyntheticaLookAndFeel.setWindowsDecorated(false);
			SyntheticaLookAndFeel.setExtendedFileChooserEnabled(true);
			SyntheticaLookAndFeel.setUseSystemFileIcons(true);
			UIManager.setLookAndFeel("de.javasoft.plaf.synthetica.SyntheticaGreenDreamLookAndFeel");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public static void main(String[] args) {
		DBGTracer.setActive(false);
		JRViewer.getLastJRViewer();
		mainWindow = new MainWindow();
		if (isStandAlone)
			mainWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainWindow.setVisible(true);
	}

	public static MainWindow getMainWindow() {
		return mainWindow;
	}
}