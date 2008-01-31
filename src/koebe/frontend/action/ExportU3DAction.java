package koebe.frontend.action;

import halfedge.frontend.action.ExtensionFileFilter;
import image.ImageHook;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileOutputStream;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import koebe.frontend.content.jrealityviewer.KoebePolyhedronView;
import de.jreality.writer.u3d.WriterU3D;


/**
 * Opens a file chooser and saves active polyhedron.
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 */
public class ExportU3DAction extends AbstractAction {

	private static final long 
		serialVersionUID = 1L;
	private JFileChooser 
		saveChooser = new JFileChooser();
	private Component 
		parent = null;
	private KoebePolyhedronView
		viewer = null;


	public ExportU3DAction(Component parent, KoebePolyhedronView view) {
		this.viewer = view;
		this.parent = parent;
		putValue(Action.NAME, "U3D...");
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke('U',
				InputEvent.CTRL_MASK));
		putValue(Action.LONG_DESCRIPTION, "Export U3D File");
		putValue(Action.SHORT_DESCRIPTION, "Export U3D...");
		putValue(Action.SMALL_ICON, new ImageIcon(ImageHook
				.getImage("save.png")));
		putValue(Action.MNEMONIC_KEY, KeyEvent.VK_U);

		saveChooser.setCurrentDirectory(new File(System.getProperty("user.dir") + "/data"));
		saveChooser.setDialogTitle("Export U3D File");
		saveChooser.setDialogType(JFileChooser.SAVE_DIALOG);
		saveChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		ExtensionFileFilter objFilter = new ExtensionFileFilter("u3d",
				"U3D Files");
		saveChooser.addChoosableFileFilter(objFilter);
	}

	public void actionPerformed(ActionEvent e) {
		if (viewer.getPolyhedron() == null){
			JOptionPane.showMessageDialog(parent, "No active polytop!");
			return;
		}
		int result = saveChooser.showSaveDialog(parent);
		if (result == JFileChooser.APPROVE_OPTION) {
			File file = saveChooser.getSelectedFile();
			if (!file.getName().toLowerCase().endsWith(".u3d"))
				file = new File(file.getAbsolutePath() + ".u3d");
			if (file.exists()) {
				int owr = JOptionPane.showConfirmDialog(parent,
						"Do you want to overwrite the file: " + file + "?");
				if (owr != JOptionPane.OK_OPTION)
					return;
			}
			try {
				FileOutputStream fos = new FileOutputStream(file);
				WriterU3D writer = new WriterU3D();
				writer.write(viewer.getSceneRoot(), fos);
			} catch (Exception e1) {
				JOptionPane.showMessageDialog(parent, e1.getMessage());
			}
		}
	}

}
