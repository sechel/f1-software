package halfedge.frontend.graphtool.standard;

import halfedge.Edge;
import halfedge.Face;
import halfedge.HalfEdgeDataStructure;
import halfedge.HalfEdgeUtility;
import halfedge.Vertex;
import halfedge.decorations.HasXY;
import halfedge.frontend.content.GraphGraphics;
import halfedge.frontend.controller.MainController;
import halfedge.frontend.graphtool.EditOperation;
import halfedge.frontend.graphtool.EditOperationException;
import halfedge.frontend.graphtool.GraphTool;
import image.ImageHook;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;



/**
 * Deletes the selected node
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 */
public class DeleteNode 
<
	V extends Vertex<V, E, F> & HasXY,
	E extends Edge<V, E, F>, 
	F extends Face<V, E, F>
>  implements GraphTool<V, E, F>  {

	private Icon 
		icon = new ImageIcon(ImageHook.getImage("delete.png"));
	private MainController<V, E, F> 
		controller = null;
	
	public Boolean initTool() {
		return true;
	}

	public void leaveTool() {

	}

	public void setController(MainController<V, E, F>  controller) {
		this.controller = controller;
	}

	@SuppressWarnings("unchecked")
	public boolean processEditOperation(EditOperation operation)
			throws EditOperationException {
		switch (operation){
			case SELECT_VERTEX:
				HalfEdgeUtility.removeVertex((V)operation.vertex);
				controller.fireGraphChanged();
				break;
			case SELECT_EDGE:
				HalfEdgeUtility.removeEdge((E)operation.edge);
				controller.fireGraphChanged();
				break;
			case SELECT_FACE:
				HalfEdgeUtility.removeFace((F)operation.face);
				controller.fireGraphChanged();
				break;
		}
		return false;
	}

	public void commitEdit(HalfEdgeDataStructure<V, E, F> graph) {

	}

	public void resetTool() {

	}

	public String getName() {
		return "Delete";
	}

	public Icon getIcon() {
		return icon;
	}

	public String getDescription() {
		return "Delete a node";
	}

	public String getShortDescription() {
		return "Delete node";
	}

	public void paint(GraphGraphics g) {

	}

	public boolean needsRepaint() {
		return false;
	}

	public JPanel getOptionPanel() {
		return new OptionPanel();
	}

	
	@SuppressWarnings("serial")
	private class OptionPanel extends JPanel implements ActionListener{
		
		private JButton
			delAllButton = new JButton("Clear Graph");
		
		public OptionPanel(){
			setLayout(new GridBagLayout());
			GridBagConstraints c = new GridBagConstraints();
			c.fill = GridBagConstraints.HORIZONTAL;
			c.weightx = 1;
			c.anchor = GridBagConstraints.WEST;
			c.insets = new Insets(2,2,2,2);
			
			c.gridwidth = GridBagConstraints.REMAINDER;
			add(delAllButton, c);
			
			delAllButton.addActionListener(this);
		}


		public void actionPerformed(ActionEvent e) {
			controller.getEditedGraph().clear();
			controller.refreshEditor();
		}
		
	}
	
	
}
