package alexandrov.frontend.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFrame;

import alexandrov.frontend.content.AboutBox;



/**
 * Shows the about box for the editor
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 */
public class ShowAboutAction extends AbstractAction {

	private static final long 
		serialVersionUID = 1L;
	private AboutBox
		aboutBox = null;
	private JFrame
		parent = null;
	
	public ShowAboutAction(JFrame owner) {
		this.parent = owner;
		putValue(Action.NAME, "About...");
		putValue(Action.LONG_DESCRIPTION, "Shows information about this program.");
	}
	
	private AboutBox getAboutFrame(){
		if (aboutBox == null)
			aboutBox = new AboutBox(parent);
		return aboutBox;
	}
	
	
	public void actionPerformed(ActionEvent e) {
		getAboutFrame().setVisible(true);
	}
	
}
