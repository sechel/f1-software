package koebe.frontend.content;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.util.LinkedList;

import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JFrame;


/**
 * A color chooser button
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 */
@SuppressWarnings("serial")
public class ColorChooseJButton extends JButton implements ActionListener{

	private JComponent 
		parent = null;
	private Color
		color = Color.WHITE;
	private LinkedList<ColorChangedListener>
		changeListeners = new LinkedList<ColorChangedListener>();
	
	
	public static interface ColorChangedListener{
		public void colorChanged(ColorChangedEvent cce);
	}
	
	public static class ColorChangedEvent extends ActionEvent{
		private Color color = null;
		public ColorChangedEvent(Object source, Color color){
			super(source, 0, "color changed");
			this.color = color;
		}
		public Color getColor() {
			return color;
		}
	}
	
	
	public void addColorChangedListener(ColorChangedListener l){
		changeListeners.add(l);
	}
	
	public void removeColorChangedListener(ColorChangedListener l){
		changeListeners.remove(l);
	}
	
	public void removeAllColorChangedListeners(){
		changeListeners.clear();
	}
	
	protected void fireColorChanged(Color color){
		for (ColorChangedListener c : changeListeners)
			c.colorChanged(new ColorChangedEvent(this, color));
	}
	
	public ColorChooseJButton(JComponent parent, Color color){
		super("_");
		this.parent = parent;
		this.color = color;
		super.addActionListener(this);
//		setPreferredSize(new Dimension(30, 30));
	}
	

	public void paint(Graphics g) {
		super.paint(g);
		g.setColor(color);
		g.fill3DRect(5, 3, getWidth() - 10, getHeight() - 6, true);
	}


	public void actionPerformed(ActionEvent e) {
		Color newColor = JColorChooser.showDialog(parent, "Choose Color", color);
		if (newColor != null){
			color = newColor;
			fireColorChanged(newColor);
		}
		repaint();
	}
	
	
	public static void main(String[] args) {
		JFrame frame = new JFrame();
		frame.addWindowListener(new WindowAdapter(){
			public void windowClosing(java.awt.event.WindowEvent e) {
				System.exit(0);
			};
		});
		frame.add(new ColorChooseJButton(null, Color.WHITE));
		frame.setVisible(true);
	}
	
	
	
}
