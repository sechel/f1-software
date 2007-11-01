package koebe.frontend.action;

import image.ImageHook;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import de.jreality.scene.Viewer;
import de.jreality.soft.SVGViewer;


/**
 * Exports the current polyhedron to a postscript file
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 */
public class ExportSVGAction extends AbstractAction {

	private static final long 
		serialVersionUID = 1L;
	private Viewer
		sceneViewer = null;
	private JFileChooser
		saveChooser = new JFileChooser();
	private Component 
		parent = null;

	public ExportSVGAction(Component parent, Viewer view){
		this.sceneViewer = view;
		this.parent = parent;
		putValue(Action.NAME, "SVG...");
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke('V', InputEvent.CTRL_MASK));
		putValue(Action.LONG_DESCRIPTION, "Export SVG File");
		putValue(Action.SHORT_DESCRIPTION, "Export SVG...");
		putValue(Action.SMALL_ICON, new ImageIcon(ImageHook.getImage("save.png")));
		putValue(Action.MNEMONIC_KEY, KeyEvent.VK_V);
		
		saveChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
		saveChooser.setDialogTitle("Export SVG File");
		saveChooser.setDialogType(JFileChooser.SAVE_DIALOG);
		saveChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		saveChooser.addChoosableFileFilter(new SVGFileFilter());

	}
	
	
	public void actionPerformed(ActionEvent e) {
		int result = saveChooser.showSaveDialog(parent);
		if (result == JFileChooser.APPROVE_OPTION){
			File file = saveChooser.getSelectedFile();
			if (!file.getName().toLowerCase().endsWith(".svg"))
				file = new File(file.getAbsolutePath() + ".svg");
			if (file.exists()){
				int owr = JOptionPane.showConfirmDialog(parent, "Do you want to overwrite the file: " + file + "?");
				if (owr != JOptionPane.OK_OPTION) return; 
			}
			try {
				SVGViewer svgExporter = new SVGViewer(file.getAbsolutePath());
				svgExporter.setCameraPath(sceneViewer.getCameraPath());
				svgExporter.setSceneRoot(sceneViewer.getSceneRoot());
				svgExporter.render();
			} catch (Exception e1) {
				JOptionPane.showMessageDialog(parent, e1.getMessage());
			}
		}
	}

}
