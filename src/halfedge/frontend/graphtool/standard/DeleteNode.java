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
	
	@Override
	public Boolean initTool() {
		return true;
	}

	@Override
	public void leaveTool() {

	}

	@Override
	public void setController(MainController<V, E, F>  controller) {
		this.controller = controller;
	}

	@Override
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

	@Override
	public void commitEdit(HalfEdgeDataStructure<V, E, F> graph) {

	}

	@Override
	public void resetTool() {

	}

	@Override
	public String getName() {
		return "Delete";
	}

	@Override
	public Icon getIcon() {
		return icon;
	}

	@Override
	public String getDescription() {
		return "Delete a node";
	}

	@Override
	public String getShortDescription() {
		return "Delete node";
	}

	@Override
	public void paint(GraphGraphics g) {

	}

	@Override
	public boolean needsRepaint() {
		return false;
	}

	@Override
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


		@Override
		public void actionPerformed(ActionEvent e) {
			controller.getEditedGraph().clear();
			controller.refreshEditor();
		}
		
	}
	
	
}
