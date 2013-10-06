 package halfedge.frontend.graphtool.standard;

import halfedge.Edge;
import halfedge.Face;
import halfedge.HalfEdgeDataStructure;
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
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.vecmath.Point2d;



/**
 * Adds an edge to the active graph
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 */
public class AddEdgeAction 
<
	V extends Vertex<V, E, F> & HasXY,
	E extends Edge<V, E, F>, 
	F extends Face<V, E, F>
>  implements GraphTool<V, E, F> {

	private Icon 
		icon = new ImageIcon(ImageHook.getImage("addedge.png"));
	private MainController<V, E, F> 
		controller = null;
	private V
		startVertex = null,
		targetVertex = null;
	private Point2d
		startPos = null,
		targetPos = null;
	private Point2d
		lastMousePos = new Point2d();
	private boolean
		needsRepaint = false;
	private EditType
		editType = EditType.EdgeStrip;
	private boolean
		fillAfterCommit = true;
	
	public enum EditType {
		EdgeStrip,
		Single;
	}
	
	
	@Override
	public Boolean initTool() {
		return true;
	}

	@Override
	public void leaveTool() {
		
	}
	
	@Override
	public void setController(MainController<V, E, F> controller) {
		this.controller = controller;
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean processEditOperation(EditOperation operation)
			throws EditOperationException {
		switch (operation){
			case SELECT_VERTEX:
				if (startVertex == null && startPos == null){
					startVertex = (V)operation.vertex;
					startPos = startVertex.getXY();
					needsRepaint = true;
					return false;
				}
				if (targetVertex == null && targetPos == null) {
					targetVertex = (V)operation.vertex;
					targetPos = targetVertex.getXY();
					if (targetVertex == startVertex){
						resetTool();
						return false;
					}
					needsRepaint = true;
					return true;
				}
				break;
			case CANCEL:
				resetTool();
				needsRepaint = true;
				break;
			case MOUSE_POS:
				lastMousePos = operation.mousePosition;
				if (startPos != null || targetPos != null)
					needsRepaint = true;
				break;
			case SELECT_POSITION:
				if (startVertex == null && startPos == null){
					startPos = operation.mousePosition;
					needsRepaint = true;
					return false;
				} 
				if (targetVertex == null && targetPos == null){
					targetPos = operation.mousePosition;
					needsRepaint = true;
					return true;
				}
		}
		return false;
	}

	@Override
	public void commitEdit(HalfEdgeDataStructure<V, E, F> graph) {
		boolean stopEdgeStrip = true;
		if (startVertex == null){
			startVertex = graph.addNewVertex();
			startVertex.setXY(startPos);
		}
		if (targetVertex == null){
			stopEdgeStrip = false;
			targetVertex = graph.addNewVertex();
			targetVertex.setXY(targetPos);
		}
		if (startVertex == targetVertex)
			return;
		
		E edge = graph.addNewEdge();
		E oppEdge = graph.addNewEdge();
		edge.setTargetVertex(targetVertex);
		oppEdge.setTargetVertex(startVertex);
		edge.linkOppositeEdge(oppEdge);
		
		switch (editType){
			case EdgeStrip:
				if (!stopEdgeStrip){
					V newStart = targetVertex;
					resetTool();
					startVertex = newStart;
					startPos = startVertex.getXY();
				} else {
					resetTool();
				}
				break;
			case Single:
				resetTool();
				break;
		}
		
		if (fillAfterCommit){
			controller.refreshEditor();
			controller.fireGraphChanged();
		}
	}

	
	@Override
	public void resetTool() {
		startVertex = null;
		targetVertex = null;
		startPos = null;
		targetPos = null;	
	}
	
	
	
	@Override
	public String getName() {
		return "Add Edge";
	}

	@Override
	public Icon getIcon() {
		return icon;
	}

	@Override
	public String getDescription() {
		return "Adds an edge to the current graph";
	}

	@Override
	public String getShortDescription() {
		return "Add an edge";
	}

	@Override
	public void paint(GraphGraphics g) {
		if (startPos != null){
			g.getGraphics().setColor(controller.getColorController().getEdgeActionColor());
			g.drawEdge(startPos, lastMousePos);
			g.getGraphics().setColor(controller.getColorController().getVertexActionColor());
			if (startVertex == null){
				g.drawVertex(startPos);
			}
			g.drawVertex(lastMousePos);
		}
	}
	

	@Override
	public boolean needsRepaint() {
		boolean result = needsRepaint;
		needsRepaint = false;
		return result;
	}

	@Override
	public JPanel getOptionPanel() {
		return new OptionPanel();
	}

	
	@SuppressWarnings("serial")
	private class OptionPanel extends JPanel implements ActionListener{
		
		private JComboBox
			editTypeCombo = new JComboBox(EditType.values());
		private JCheckBox
			fillAfterCommitChecker = new JCheckBox("Automatic Faces", fillAfterCommit);
			
		
		public OptionPanel(){
			setLayout(new GridBagLayout());
			GridBagConstraints c = new GridBagConstraints();
			c.fill = GridBagConstraints.HORIZONTAL;
			c.weightx = 1;
			c.anchor = GridBagConstraints.WEST;
			c.insets = new Insets(2,2,2,2);

			c.gridwidth = GridBagConstraints.REMAINDER;
			add(fillAfterCommitChecker, c);
			c.gridwidth = GridBagConstraints.RELATIVE;
			add(new JLabel("Edit Mode"), c);
			c.gridwidth = GridBagConstraints.REMAINDER;
			editTypeCombo.setSelectedItem(editType);
			add(editTypeCombo);
			
			editTypeCombo.addActionListener(this);
			fillAfterCommitChecker.addActionListener(this);
		}


		@Override
		public void actionPerformed(ActionEvent e) {
			if (editTypeCombo == e.getSource()){
				editType = (EditType)editTypeCombo.getSelectedItem();
			} else
			if (fillAfterCommitChecker == e.getSource()){
				fillAfterCommit = fillAfterCommitChecker.isSelected();
			}
		}
		
	}
	
	
}
