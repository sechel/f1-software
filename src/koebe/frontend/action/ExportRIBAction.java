package koebe.frontend.action;

import image.ImageHook;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;

import de.jreality.renderman.RIBViewer;
import de.jreality.scene.Viewer;

/**
 * Exports the current polyhedron to a renderman file
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 */
public class ExportRIBAction extends AbstractAction{

	private static final long 
		serialVersionUID = 1L;
	private Viewer
		sceneViewer = null;
	private RIBViewer
		ribExporter = new RIBViewer();
	private JFileChooser
		saveChooser = new JFileChooser();
	private Component 
		parent = null;
	private JPanel
		accessoryPanel = new JPanel();
	
	private SpinnerNumberModel
		widthModel = new SpinnerNumberModel(800, 10, 1000000, 1),
		heightModel = new SpinnerNumberModel(600, 10, 1000000, 1);
	private JSpinner
		widthSpinner = new JSpinner(widthModel),
		heightSpinner = new JSpinner(heightModel);
	
	public ExportRIBAction(Component parent, Viewer view){
		this.sceneViewer = view;
		this.parent = parent;
		putValue(Action.NAME, "RIB...");
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke('E', InputEvent.CTRL_MASK));
		putValue(Action.LONG_DESCRIPTION, "Export Renderman File");
		putValue(Action.SHORT_DESCRIPTION, "Export RIB...");
		putValue(Action.SMALL_ICON, new ImageIcon(ImageHook.getImage("save.png")));
		putValue(Action.MNEMONIC_KEY, KeyEvent.VK_E);
		
		saveChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
		saveChooser.setDialogTitle("Export Renderman File");
		saveChooser.setDialogType(JFileChooser.SAVE_DIALOG);
		saveChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		saveChooser.addChoosableFileFilter(new RIBFileFilter());
		saveChooser.setAccessory(accessoryPanel);
		
		accessoryPanel.setLayout(new GridBagLayout());
		accessoryPanel.setBorder(BorderFactory.createTitledBorder("Options"));
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(5, 10, 5, 10);
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.NORTHWEST;
		accessoryPanel.add(widthSpinner, c);
		c.anchor = GridBagConstraints.CENTER;
		accessoryPanel.add(new JLabel("X"), c);
		accessoryPanel.add(heightSpinner, c);
		c.fill = GridBagConstraints.BOTH;
		accessoryPanel.add(new JPanel(), c);
	}
	
	
	@Override
	public void actionPerformed(ActionEvent e) {
		int result = saveChooser.showSaveDialog(parent);
		if (result == JFileChooser.APPROVE_OPTION){
			File file = saveChooser.getSelectedFile();
			if (file.exists()){
				int owr = JOptionPane.showConfirmDialog(parent, "Do you want to overwrite the file: " + file + "?");
				if (owr != JOptionPane.OK_OPTION) return; 
			}
			if (file.getName().toLowerCase().endsWith(".rib"))
				file = new File(file.getAbsolutePath().substring(0, file.getAbsolutePath().length() - 4));
			try {
				ribExporter.setCameraPath(sceneViewer.getCameraPath());
				ribExporter.setSceneRoot(sceneViewer.getSceneRoot());
				ribExporter.setFileName(file.getAbsolutePath());
				ribExporter.setWidth(widthModel.getNumber().intValue());
				ribExporter.setHeight(heightModel.getNumber().intValue());
				ribExporter.render();
			} catch (Exception e1) {
				JOptionPane.showMessageDialog(parent, e1.getMessage());
			}
		}
	}

}
