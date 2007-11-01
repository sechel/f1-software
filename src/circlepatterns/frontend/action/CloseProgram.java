package circlepatterns.frontend.action;

import image.ImageHook;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;




/**
 * Quits the application
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 */
public class CloseProgram extends AbstractAction {

	private static final long 
		serialVersionUID = 1L;

	public CloseProgram() {
		putValue(Action.NAME, "Exit");
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke('X', InputEvent.ALT_MASK));
		putValue(Action.LONG_DESCRIPTION, "Exit the Program");
		putValue(Action.SMALL_ICON, new ImageIcon(ImageHook.getImage("close.png")));
		putValue(Action.MNEMONIC_KEY, KeyEvent.VK_X);
	}
	
	public void actionPerformed(ActionEvent e) {
		System.exit(0);
	}

}
