package alexandrov.frontend.action;

import image.ImageHook;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import alexandrov.frontend.content.CPMLEditor;
import alexandrov.frontend.controller.MainController;


/**
 * Resets the alexandrov polyhedron editor to its defaults
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 */
public class ResetAction extends AbstractAction{

	private static final long 
		serialVersionUID = 1L;
	private MainController
		controller = null;
	
	public ResetAction(MainController controller){
		this.controller = controller;
		putValue(Action.NAME, "Reset");
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke('R', InputEvent.CTRL_MASK));
		putValue(Action.LONG_DESCRIPTION, "Reset the current graph");
		putValue(Action.SHORT_DESCRIPTION, "Reset the current graph");
		putValue(Action.SMALL_ICON, new ImageIcon(ImageHook.getImage("delete.png")));
		putValue(Action.MNEMONIC_KEY, KeyEvent.VK_R);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(controller.getMainPanel(), "Would you really like to reset?")){
			switch (controller.getEditorMode()){
			case GraphEditMode:
				controller.getEditedGraph().clear();
				break;
			case XMLEditMode:
				controller.setCPMLGraph(CPMLEditor.getDefaultCPML());
				break;
			}
			controller.fireGraphChanged();
			controller.refreshEditor();
		}
	}
	
}
