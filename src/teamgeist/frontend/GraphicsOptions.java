package teamgeist.frontend;

import static java.awt.GridBagConstraints.HORIZONTAL;
import static java.awt.GridBagConstraints.REMAINDER;
import static java.awt.GridBagConstraints.WEST;
import halfedge.HalfEdgeDataStructure;
import halfedge.triangulationutilities.HaussdorfDistance;
import halfedge.triangulationutilities.TriangulationException;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;

import teamgeist.frontend.controller.MainController;
import alexandrov.graph.CPMEdge;
import alexandrov.graph.CPMFace;
import alexandrov.graph.CPMVertex;
import circlepatterns.frontend.content.ShrinkPanel;

public class GraphicsOptions extends ShrinkPanel implements ActionListener{

	private static final long 
		serialVersionUID = 1L;

	private MainController
		controller = null;
	private JCheckBox
		shadingChecker = new JCheckBox("Smooth Shading", true),
		viewWireFrame = new JCheckBox("Wireframe", false);
	private JButton	
		calcHaussdorfDistanceBtn = new JButton("Haussdorf Distance");
	
	public GraphicsOptions(MainController controller) {
		super("Graphics");
		this.controller = controller;
		makeLayout();
		
		shadingChecker.addActionListener(this);
		viewWireFrame.addActionListener(this);
		calcHaussdorfDistanceBtn.addActionListener(this);
	}


	private void makeLayout() {
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = HORIZONTAL;
		c.insets = new Insets(2,2,2,2);
		c.anchor = WEST;
		c.weightx = 1;
		
		c.gridwidth = REMAINDER;
		add(shadingChecker, c);
		add(viewWireFrame, c);
		add(calcHaussdorfDistanceBtn, c);
	}


	@Override
	public void actionPerformed(ActionEvent e) {
		Object s = e.getSource();
		if (shadingChecker == s){
			controller.getViewer().setSmoothShading(shadingChecker.isSelected());
		}
		if (viewWireFrame == s){
			controller.getViewer().setWireFrameRender(viewWireFrame.isSelected());
		}
		if (calcHaussdorfDistanceBtn == s){
			HalfEdgeDataStructure<CPMVertex, CPMEdge, CPMFace> t = controller.getViewer().getViewedTeamgeist();
			if (t == null)
				controller.setStatus("No active teamgeist!");
			else{
				double d = -1.0;
//				double r = -1.0;
				try {
					d = HaussdorfDistance.getDistanceToSphere(t);
//					r = HaussdorfDistance.getMaxRadius(t);
				} catch (TriangulationException e1) {
					controller.setStatus(e1.getMessage());
					return;
				}
				controller.setStatus("Hausdorff Distance is: " + d);
			}
		}
	}
	
	
}
