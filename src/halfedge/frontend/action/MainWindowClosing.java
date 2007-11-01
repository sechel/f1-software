package halfedge.frontend.action;

import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.AbstractAction;

/**
 * Window adapter action wrapper
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 */
@SuppressWarnings("serial")
public class MainWindowClosing extends AbstractAction implements WindowListener {

	
	public void windowOpened(WindowEvent e) {

	}

	public void windowClosing(WindowEvent e) {
		actionPerformed(new ActionEvent(e.getSource(), e.getID(), "Window Closing"));
	}

	public void windowClosed(WindowEvent e) {

	}

	public void windowIconified(WindowEvent e) {

	}

	public void windowDeiconified(WindowEvent e) {

	}

	public void windowActivated(WindowEvent e) {

	}

	public void windowDeactivated(WindowEvent e) {

	}

	public void actionPerformed(ActionEvent e) {
		System.exit(0);
	}

}
