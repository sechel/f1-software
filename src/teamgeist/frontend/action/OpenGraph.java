package teamgeist.frontend.action;

import halfedge.frontend.action.ExtensionFileFilter;
import halfedge.io.HESerializableReader;
import image.ImageHook;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import teamgeist.frontend.controller.MainController;
import alexandrov.graph.CPMEdge;
import alexandrov.graph.CPMFace;
import alexandrov.graph.CPMVertex;


/**
 * Opens HEDS binary files for the current editor
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 */
public class OpenGraph extends AbstractAction {

	private static final long 
		serialVersionUID = 1L;

	protected JFileChooser
		openChooser = new JFileChooser();
	protected Component 
		parent = null;
	protected MainController
		controller = null;
	
	public OpenGraph(Component parent, MainController controller) {
		this.parent  = parent;
		this.controller	= controller;
		
		putValue(Action.NAME, "Open");
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke('O', InputEvent.CTRL_MASK));
		putValue(Action.LONG_DESCRIPTION, "Open Teamgeist(TM)");
		putValue(Action.SHORT_DESCRIPTION, "Open");
		putValue(Action.SMALL_ICON, new ImageIcon(ImageHook.getImage("open.png")));
		putValue(Action.MNEMONIC_KEY, KeyEvent.VK_O);
		
		openChooser.removeChoosableFileFilter(openChooser.getAcceptAllFileFilter());
		openChooser.setCurrentDirectory(new File(System.getProperty("user.dir") + "/data/teamgeist"));
		openChooser.setDialogTitle("Open Teamgeist(TM)");
		openChooser.setDialogType(JFileChooser.OPEN_DIALOG);
		openChooser.addChoosableFileFilter(new ExtensionFileFilter("teamgeist", "Teamgeist in a file"));
		openChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
	}
	
	
	public void openFile(File file){
		FileInputStream fis;
		try {
			fis = new FileInputStream(file);
			HESerializableReader<CPMVertex, CPMEdge, CPMFace> reader = new HESerializableReader<CPMVertex, CPMEdge, CPMFace>(fis);
			controller.getViewer().viewTeamgeist(reader.readHalfEdgeDataStructure());
			controller.getViewer().encompass();
		} catch (Exception e1) {
			JOptionPane.showMessageDialog(parent, "Error while loding Teamgeist(TM): " + file.getName() + "\n" + e1.getMessage());
		}
	}
	
	
	public void actionPerformed(ActionEvent e) {
		int result = openChooser.showOpenDialog(parent);
		if (result == JFileChooser.APPROVE_OPTION){
			openFile(openChooser.getSelectedFile());
		}
	}

}
