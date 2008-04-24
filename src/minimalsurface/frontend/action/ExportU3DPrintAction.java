package minimalsurface.frontend.action;

import halfedge.frontend.action.ExtensionFileFilter;
import image.ImageHook;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileOutputStream;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;

import minimalsurface.frontend.content.MinimalSurfacePanel;
import de.jreality.geometry.ThickenedSurfaceFactory;
import de.jreality.io.JrScene;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.writer.u3d.WriterU3D;

/**
 * Opens a file chooser and saves active polyhedron.
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 */
public class ExportU3DPrintAction extends AbstractAction {

	private static final long 
		serialVersionUID = 1L;
	private JFileChooser 
		saveChooser = new JFileChooser();
	private Component 
		parent = null;
	private MinimalSurfacePanel
		viewer = null;
	private SpinnerNumberModel
		thicknessModel = new SpinnerNumberModel(0.05, 0.0, 10000.0, 0.01);
	private JSpinner
		thicknessSpinner = new JSpinner(thicknessModel);
	private JPanel
		optionPanel = new JPanel();


	public ExportU3DPrintAction(Component parent, MinimalSurfacePanel view) {
		this.viewer = view;
		this.parent = parent;
		putValue(Action.NAME, "U3D Print...");
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke('U',
				InputEvent.CTRL_MASK));
		putValue(Action.LONG_DESCRIPTION, "Export U3D Print File");
		putValue(Action.SHORT_DESCRIPTION, "Export U3D Print...");
		putValue(Action.SMALL_ICON, new ImageIcon(ImageHook
				.getImage("save.png")));
		putValue(Action.MNEMONIC_KEY, KeyEvent.VK_B);

		saveChooser.setCurrentDirectory(new File(System.getProperty("user.dir") + "/data"));
		saveChooser.setDialogTitle("Export U3D File");
		saveChooser.setDialogType(JFileChooser.SAVE_DIALOG);
		saveChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		ExtensionFileFilter objFilter = new ExtensionFileFilter("u3d",
				"U3D Files");
		saveChooser.addChoosableFileFilter(objFilter);
		saveChooser.setAccessory(optionPanel);
		makeLayout();
	}

	private void makeLayout(){
		optionPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridwidth = GridBagConstraints.RELATIVE; 
		optionPanel.add(new JLabel("Thickness"), c);
		optionPanel.add(thicknessSpinner, c);
	}
	
	
	private SceneGraphComponent getSurfaceComponent_R(SceneGraphComponent root) {
		if (root.getOwner() == viewer.getSurfaceOwner())
			return root;
		for (SceneGraphComponent c : root.getChildComponents())
			return getSurfaceComponent_R(c);
		return null;
	}
	
	private SceneGraphComponent getSurfaceComponent(JrScene scene) {
		return getSurfaceComponent_R(scene.getSceneRoot());
	}
	
	
	public void actionPerformed(ActionEvent e) {
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
				JrScene scene = viewer.getViewerApp().getJrScene();
				SceneGraphComponent surface = getSurfaceComponent(scene);
				IndexedFaceSet surfaceGeom = (IndexedFaceSet) surface.getGeometry();
				ThickenedSurfaceFactory tsf = new ThickenedSurfaceFactory(surfaceGeom);
				tsf.setThickenAlongFaceNormals(false);
				tsf.setThickness(thicknessModel.getNumber().doubleValue());
				tsf.update();
				surface.setGeometry(tsf.getThickenedSurface());
				writer.writeScene(scene, fos);
				surface.setGeometry(surfaceGeom);
				fos.close();
			} catch (Exception e1) {
				JOptionPane.showMessageDialog(parent, e1.getMessage());
			}
		}
	}

}
