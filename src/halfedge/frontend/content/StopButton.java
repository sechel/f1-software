package halfedge.frontend.content;

import halfedge.Edge;
import halfedge.Face;
import halfedge.Vertex;
import halfedge.decorations.HasXY;
import halfedge.frontend.controller.MainController;
import image.ImageHook;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;

public class StopButton <
	V extends Vertex<V, E, F> & HasXY,
	E extends Edge<V, E, F>, 
	F extends Face<V, E, F>
> extends JButton implements ActionListener{

	private static final long 
		serialVersionUID = 1L;
	private MainController<V, E, F>
		controller = null;
	
	public StopButton(MainController<V, E, F> controller){
		super(new ImageIcon(ImageHook.getImage("stop.png")));
		this.controller = controller;
		addActionListener(this);
		setPreferredSize(new Dimension(30, 20));
		setToolTipText("Stop Calculation");
	}

	public void actionPerformed(ActionEvent e) {
		controller.getCalculationRemote().setStopAsFastAsPossible(true);
	}
	
	@Override
	public void paint(Graphics g) {
		setEnabled(controller.getCalculationRemote() != null);
		super.paint(g);
	}
	
}
