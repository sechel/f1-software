package koebe.frontend.tool;

import halfedge.HalfEdgeDataStructure;
import halfedge.frontend.content.GraphGraphics;
import halfedge.frontend.controller.ColorController;
import halfedge.frontend.graphtool.EditOperation;
import halfedge.frontend.graphtool.EditOperationException;
import halfedge.frontend.graphtool.GraphTool;
import halfedge.surfaceutilities.Subdivision;
import halfedge.surfaceutilities.SurfaceException;
import image.ImageHook;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.vecmath.Point2d;

import koebe.frontend.controller.MainController;
import circlepatterns.graph.CPEdge;
import circlepatterns.graph.CPFace;
import circlepatterns.graph.CPVertex;



/**
 * Generates a medial graph from the active graph
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 */
public class GenerateMedialGraph implements GraphTool<CPVertex, CPEdge, CPFace>  {

	protected Icon 
		icon = new ImageIcon(ImageHook.getImage("medial.png"));
	protected MainController
		medianController = new MainController(),
		controller = null;
	protected HalfEdgeDataStructure<CPVertex, CPEdge, CPFace>
		medial = null;
	protected boolean
		needsRepaint = false;
	protected HashMap<CPVertex, CPFace>
		vertexFaceMap = new HashMap<CPVertex, CPFace>();
	protected HashMap<CPFace, CPFace>
		faceFaceMap = new HashMap<CPFace, CPFace>();
	protected HashMap<CPEdge, CPVertex>
		edgeVertexMap = new HashMap<CPEdge, CPVertex>();
	protected HashMap<CPEdge, CPEdge>
		edgeEdgeMap = new HashMap<CPEdge, CPEdge>();
	protected boolean
		makeSurface = true;
	
	protected void calculateMedial(){
		HalfEdgeDataStructure<CPVertex, CPEdge, CPFace> g = controller.getEditedGraph();
		try {
			medial = Subdivision.createMedialGraph(g, vertexFaceMap, edgeVertexMap, faceFaceMap, edgeEdgeMap);
		} catch (SurfaceException e1) {
			medial = null;
			JOptionPane.showMessageDialog(controller.getMainPanel(), e1.getMessage());
		}
		for (CPEdge edge : edgeVertexMap.keySet()){ 			
			CPVertex vertex = edgeVertexMap.get(edge);
			Point2d left = edge.getTargetVertex().getXY();
			Point2d right = edge.getStartVertex().getXY();
			vertex.getXY().set((left.x + right.x) / 2, (left.y + right.y) / 2);
		}
//		if (makeSurface){
//			try {
//				SurfaceUtility.fillHoles(medial);
//			} catch (SurfaceException e) {
//				JOptionPane.showMessageDialog(controller.getMainPanel(), e.getMessage());
//			}
//		}
		needsRepaint = true;
	}
	
	
	@Override
	public Boolean initTool() {
		ColorController cc = medianController.getColorController();
		medianController.setUseFaces(false);
		cc.setFaceColor(new Color(0x30EEEE00, true));
		cc.setEdgeColor(new Color(0xDD050500, true));
		cc.setVertexColor(new Color(0xDD202000, true));
		calculateMedial();
		return false;
	}

	@Override
	public void leaveTool() {

	}

	@Override
	public void setController(halfedge.frontend.controller.MainController<CPVertex, CPEdge, CPFace> controller) {
		this.controller = (MainController)controller;
	}

	@Override
	public boolean processEditOperation(EditOperation operation)
			throws EditOperationException {
		switch (operation){
			default:
				calculateMedial();
				break;
			case MOUSE_POS:
				break;
		}
		return false;
	}

	@Override
	public void commitEdit(HalfEdgeDataStructure<CPVertex, CPEdge, CPFace> graph) {

	}

	@Override
	public void resetTool() {

	}

	@Override
	public String getName() {
		return "Medial";
	}

	@Override
	public Icon getIcon() {
		return icon;
	}

	@Override
	public String getDescription() {
		return "Construct Medial Graph";
	}

	@Override
	public String getShortDescription() {
		return "Medial Graph";
	}

	@Override
	public void paint(GraphGraphics g) {
		if (medial != null)
			g.drawGraph(medial, medianController);
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
	protected class OptionPanel extends JPanel implements ActionListener{
		
		private JCheckBox
			fillFaces = new JCheckBox("Make Surface", makeSurface);
		private JButton
			editMedialGraph = new JButton("Edit Medial Graph");
		
		public OptionPanel(){
			setLayout(new GridBagLayout());
			GridBagConstraints c = new GridBagConstraints();
			c.fill = GridBagConstraints.HORIZONTAL;
			c.weightx = 1;
			c.anchor = GridBagConstraints.WEST;
			c.insets = new Insets(2,2,2,2);
			
			c.gridwidth = GridBagConstraints.REMAINDER;
			add(fillFaces, c);
			add(editMedialGraph, c);
			
			fillFaces.addActionListener(this);
			editMedialGraph.addActionListener(this);
		}


		@Override
		public void actionPerformed(ActionEvent e) {
			if (fillFaces == e.getSource()){
				makeSurface = fillFaces.isSelected();
				calculateMedial();
			}
			if (editMedialGraph == e.getSource()){
				controller.setEditedGraph(medial);
			}
			needsRepaint = true;
			controller.refreshEditor();
		}
		
	}

}
