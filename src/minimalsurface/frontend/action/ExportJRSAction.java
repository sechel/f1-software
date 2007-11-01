package minimalsurface.frontend.action;

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

import minimalsurface.frontend.content.MinimalSurfacePanel;
import de.jreality.writer.WriterJRS;


/**
 * Opens a file chooser and saves active polyhedron.
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 */
public class ExportJRSAction extends AbstractAction {

	private static final long 
		serialVersionUID = 1L;
	private JFileChooser 
		saveChooser = new JFileChooser();
	private Component 
		parent = null;
	private MinimalSurfacePanel
		viewer = null;


	public ExportJRSAction(Component parent, MinimalSurfacePanel view) {
		this.viewer = view;
		this.parent = parent;
		putValue(Action.NAME, "JRS...");
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke('J',
				InputEvent.CTRL_MASK));
		putValue(Action.LONG_DESCRIPTION, "Export JRS File");
		putValue(Action.SHORT_DESCRIPTION, "Export JRS...");
		putValue(Action.SMALL_ICON, new ImageIcon(ImageHook
				.getImage("save.png")));
		putValue(Action.MNEMONIC_KEY, KeyEvent.VK_J);

		saveChooser.setCurrentDirectory(new File(System.getProperty("user.dir") + "/data"));
		saveChooser.setDialogTitle("Export JRS File");
		saveChooser.setDialogType(JFileChooser.SAVE_DIALOG);
		saveChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		ExtensionFileFilter objFilter = new ExtensionFileFilter("jrs",
				"JRS Files");
		saveChooser.addChoosableFileFilter(objFilter);
	}

	public void actionPerformed(ActionEvent e) {
		int result = saveChooser.showSaveDialog(parent);
		if (result == JFileChooser.APPROVE_OPTION) {
			File file = saveChooser.getSelectedFile();
			if (!file.getName().toLowerCase().endsWith(".jrs"))
				file = new File(file.getAbsolutePath() + ".jrs");
			if (file.exists()) {
				int owr = JOptionPane.showConfirmDialog(parent,
						"Do you want to overwrite the file: " + file + "?");
				if (owr != JOptionPane.OK_OPTION)
					return;
			}
			try {
				FileOutputStream fos = new FileOutputStream(file);
				WriterJRS writer = new WriterJRS();
				writer.write(viewer.getSceneRoot(), fos);
				fos.close();
			} catch (Exception e1) {
				JOptionPane.showMessageDialog(parent, e1.getMessage());
			}
		}
	}

}
