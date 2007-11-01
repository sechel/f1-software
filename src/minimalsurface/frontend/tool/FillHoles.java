package minimalsurface.frontend.tool;

import halfedge.HalfEdgeDataStructure;
import halfedge.HalfEdgeUtility;
import halfedge.frontend.content.GraphGraphics;
import halfedge.frontend.graphtool.EditOperation;
import halfedge.frontend.graphtool.EditOperationException;
import halfedge.frontend.graphtool.GraphTool;
import halfedge.surfaceutilities.SurfaceException;
import halfedge.surfaceutilities.SurfaceUtility;
import image.ImageHook;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import minimalsurface.controller.MainController;
import circlepatterns.graph.CPEdge;
import circlepatterns.graph.CPFace;
import circlepatterns.graph.CPVertex;



/**
 * Creates all faces if the active graph is embedded
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 */
public class FillHoles implements GraphTool<CPVertex, CPEdge, CPFace> {

	private Icon 
		icon = new ImageIcon(ImageHook.getImage("fill.png"));
	private MainController
		controller = null;
	
	
	public Boolean initTool() {
		fillHoles();
		return false;
	}

	public void leaveTool() {
		

	}

	public void setController(halfedge.frontend.controller.MainController<CPVertex, CPEdge, CPFace> controller) {
		this.controller = (MainController)controller;
	}

	public boolean processEditOperation(EditOperation operation)
			throws EditOperationException {
		
		return false;
	}

	public void commitEdit(HalfEdgeDataStructure<CPVertex, CPEdge, CPFace> graph) {
		

	}

	public void resetTool() {
		

	}

	public String getName() {
		return "Fill Holes";
	}

	public Icon getIcon() {
		return icon;
	}

	public String getDescription() {
		return "Fills the holes in this surface";
	}

	public String getShortDescription() {
		return "Fills holes";
	}

	public void paint(GraphGraphics g) {
		

	}

	public boolean needsRepaint() {
		
		return false;
	}

	public JPanel getOptionPanel() {
		return new OptionPanel();
	}

	
	private void fillHoles(){
		HalfEdgeDataStructure<CPVertex, CPEdge, CPFace> graph = controller.getEditedGraph();
		try {
			HalfEdgeUtility.removeAllFaces(graph);
			SurfaceUtility.linkAllEdges(graph);
			SurfaceUtility.fillHoles(graph);
		} catch (SurfaceException e1) {
			JOptionPane.showMessageDialog(controller.getMainPanel(), e1.getMessage());
		}
		controller.refreshEditor();
		controller.fireGraphChanged();
	}
	
	
	@SuppressWarnings("serial")
	private class OptionPanel extends JPanel implements ActionListener{
		
		private JButton
			fillHolesButton = new JButton("Fill Holes");
		
		public OptionPanel(){
			setLayout(new GridBagLayout());
			GridBagConstraints c = new GridBagConstraints();
			c.fill = GridBagConstraints.HORIZONTAL;
			c.weightx = 1;
			c.anchor = GridBagConstraints.WEST;
			c.insets = new Insets(2,2,2,2);
			
			c.gridwidth = GridBagConstraints.REMAINDER;
			add(fillHolesButton, c);
			
			fillHolesButton.addActionListener(this);
		}


		public void actionPerformed(ActionEvent e) {
			fillHoles();
		}
		
	}
	
}
