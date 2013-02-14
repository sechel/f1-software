package halfedge.frontend.action;

import halfedge.Edge;
import halfedge.Face;
import halfedge.Vertex;
import halfedge.decorations.HasXY;
import halfedge.frontend.content.EditPanel;
import halfedge.frontend.controller.MainController;
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




/**
 * Opens HEDS binary files for the current editor
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 */
public class OpenGraph 
<
	V extends Vertex<V, E, F> & HasXY,
	E extends Edge<V, E, F>, 
	F extends Face<V, E, F>
> extends AbstractAction {

	private static final long 
		serialVersionUID = 1L;

	protected JFileChooser
		openChooser = new JFileChooser();
	protected Component 
		parent = null;
	protected MainController<V, E, F> 
		controller = null;
	protected EditPanel<V, E, F>
		editor = null;
	
	public OpenGraph(EditPanel<V, E, F> editor, Component parent, MainController<V, E, F> controller, ExtensionFileFilter filter) {
		this.parent  = parent;
		this.controller	= controller;
		this.editor = editor;
		
		putValue(Action.NAME, "Open");
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke('O', InputEvent.CTRL_MASK));
		putValue(Action.LONG_DESCRIPTION, "Open graph");
		putValue(Action.SHORT_DESCRIPTION, "Open");
		putValue(Action.SMALL_ICON, new ImageIcon(ImageHook.getImage("open.png")));
		putValue(Action.MNEMONIC_KEY, KeyEvent.VK_O);
		
		openChooser.removeChoosableFileFilter(openChooser.getAcceptAllFileFilter());
		openChooser.setCurrentDirectory(new File(System.getProperty("user.dir") + "/data"));
		openChooser.setDialogTitle("Open Graph");
		openChooser.setDialogType(JFileChooser.OPEN_DIALOG);
		openChooser.addChoosableFileFilter(filter);
		openChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
	}
	
	public void setFileFilter(ExtensionFileFilter filter){
		openChooser.resetChoosableFileFilters();
		openChooser.addChoosableFileFilter(filter);
	}
	
	
	public void openFile(File file){
		FileInputStream fis;
		try {
			fis = new FileInputStream(file);
			HESerializableReader<V, E, F> reader = new HESerializableReader<V, E, F>(fis);
			controller.setEditedGraph(reader.readHalfEdgeDataStructure());
			controller.fireGraphChanged();
			reader.close();
		} catch (Exception e1) {
			JOptionPane.showMessageDialog(parent, "Error while loding graph: " + file.getName() + "\n" + e1.getMessage());
		}
		controller.refreshEditor();
		if (editor != null)
			editor.encompass();
	}
	
	
	public void actionPerformed(ActionEvent e) {
		int result = openChooser.showOpenDialog(parent);
		if (result == JFileChooser.APPROVE_OPTION){
			openFile(openChooser.getSelectedFile());
		}
	}

}
