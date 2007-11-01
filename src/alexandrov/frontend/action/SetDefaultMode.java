package alexandrov.frontend.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

import alexandrov.frontend.controller.EditorMode;
import alexandrov.frontend.controller.MainController;



/**
 * Switches the alexandrov polyhedron editor to the default
 * graph edtor mode.
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 */
@SuppressWarnings("serial")
public class SetDefaultMode extends AbstractAction {

	private MainController 
	controller = null;
	
	public SetDefaultMode(MainController controller){
		this.controller = controller;
		putValue(Action.NAME, "Graph Editor");
		putValue(Action.LONG_DESCRIPTION, "Switch to contruction mode");
		putValue(Action.SHORT_DESCRIPTION, "Graph Editor");
	}
	
	public void actionPerformed(ActionEvent e) {
		controller.setEditorMode(EditorMode.GraphEditMode);
	}

}
