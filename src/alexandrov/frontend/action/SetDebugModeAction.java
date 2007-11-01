package alexandrov.frontend.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;

import util.debug.DBGTracer;



/**
 * JCheckbox action switches the debug mode on or off
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 */
public class SetDebugModeAction extends AbstractAction {

	private static final long 
		serialVersionUID = 1L;
	
	public SetDebugModeAction() {
		putValue(Action.NAME, "Debug Output");
		putValue(Action.LONG_DESCRIPTION, "Shows debug output on the console.");
	}
	
	public void actionPerformed(ActionEvent e) {
		DBGTracer.setActive(((JCheckBoxMenuItem)e.getSource()).isSelected());
	}

	
}
