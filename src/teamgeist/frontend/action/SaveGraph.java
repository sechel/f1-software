package teamgeist.frontend.action;

import halfedge.frontend.action.ExtensionFileFilter;
import halfedge.io.HESerializableWriter;
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

import teamgeist.frontend.controller.MainController;

/**
 * Saves HEDS binary files from the current editor
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 */
public class SaveGraph extends AbstractAction {

	private static final long 
		serialVersionUID = 1L;

	private JFileChooser
		saveChooser = new JFileChooser();
	private Component 
		parent = null;
	private MainController
		controller = null;
	private ExtensionFileFilter 
		filter = null;
	
	public SaveGraph(Component parent, MainController controller) {
		this.parent  = parent;
		this.controller	= controller;
		this.filter  = new ExtensionFileFilter("teamgeist", "Teamgeist in a file");
		
		putValue(Action.NAME, "Save...");
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke('S', InputEvent.CTRL_MASK));
		putValue(Action.LONG_DESCRIPTION, "Save the active Teamgeist(TM)");
		putValue(Action.SHORT_DESCRIPTION, "Save...");
		putValue(Action.SMALL_ICON, new ImageIcon(ImageHook.getImage("save.png")));
		putValue(Action.MNEMONIC_KEY, KeyEvent.VK_S);
		
		saveChooser.setCurrentDirectory(new File(System.getProperty("user.dir") + "/data/teamgeist"));
		saveChooser.setDialogTitle("Save Teamgeist(TM)");
		saveChooser.setDialogType(JFileChooser.SAVE_DIALOG);
		saveChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		saveChooser.addChoosableFileFilter(filter);
	}
	
	public void setFileFilter(ExtensionFileFilter filter){
		saveChooser.resetChoosableFileFilters();
		saveChooser.addChoosableFileFilter(filter);
		this.filter = filter;
	}
	
	public void actionPerformed(ActionEvent e) {
		int result = saveChooser.showSaveDialog(parent);
		if (result == JFileChooser.APPROVE_OPTION){
			File file = saveChooser.getSelectedFile();
			if (file.exists()){
				int owr = JOptionPane.showConfirmDialog(parent, "Do you want to overwrite the file: " + file + "?");
				if (owr != JOptionPane.OK_OPTION) return; 
			}
			if (!file.getName().toLowerCase().endsWith("." + filter.getExtension()))
				file = new File(file.getAbsolutePath() + "." + filter.getExtension());
			FileOutputStream fos;
			try {
				if (controller.getViewer().getViewedTeamgeist() == null){
					JOptionPane.showMessageDialog(parent, "No active Teamgeist");
					return;
				}
				fos = new FileOutputStream(file);
				HESerializableWriter writer = new HESerializableWriter(fos);
				writer.writeHalfEdgeDataStructure(controller.getViewer().getViewedTeamgeist());
			} catch (Exception e1) {
				JOptionPane.showMessageDialog(parent, e1.getMessage());
			}
		}
	}

}