package minimalsurface.frontend.tool;

import static java.awt.GridBagConstraints.BOTH;
import static java.awt.GridBagConstraints.HORIZONTAL;
import static java.awt.GridBagConstraints.RELATIVE;
import static java.awt.GridBagConstraints.REMAINDER;
import static java.lang.Math.PI;
import halfedge.HalfEdgeDataStructure;
import halfedge.frontend.content.GraphGraphics;
import halfedge.frontend.graphtool.EditOperation;
import halfedge.frontend.graphtool.EditOperationException;
import halfedge.frontend.graphtool.GraphTool;
import image.ImageHook;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.math.MathContext;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.vecmath.Point2d;

import minimalsurface.controller.MainController;
import circlepatterns.graph.CPEdge;
import circlepatterns.graph.CPFace;
import circlepatterns.graph.CPVertex;



/**
 * Edge length edit tool for the graph editor
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 */
public class EditTheta implements GraphTool<CPVertex, CPEdge, CPFace> {

	private Icon 
		icon = new ImageIcon(ImageHook.getImage("theta.png"));
	private MainController
		controller = null;
	private Color
		labelColor = Color.RED;
	private Options
		optionsPanel = null;
	
	public Boolean initTool() {
		// TODO here debuging only!
		for (CPEdge e : controller.getEditedGraph().getEdges())
			e.setTheta(Math.PI / 2);
		return true;
	}

	public void leaveTool() {

	}

	public void setController(halfedge.frontend.controller.MainController<CPVertex, CPEdge, CPFace> controller) {
		this.controller = (MainController)controller;
	}

	public boolean processEditOperation(EditOperation operation) throws EditOperationException {
		switch (operation){
		case SELECT_EDGE:
			double newTheta = ThetaEditorDialog.showEdgeThetaDialog(controller.getGraphEditor(), ((CPEdge)operation.edge).getTheta());
			if (newTheta == -1)
				break;
			else {
				((CPEdge)operation.edge).setTheta(newTheta);
				((CPEdge)operation.edge).getOppositeEdge().setTheta(newTheta);
				controller.fireGraphChanged();
			}
			break;
		}
		controller.refreshEditor();
		return false;
	}

	
	public static class ThetaEditorDialog extends JDialog implements ActionListener{

		private static final long 
			serialVersionUID = 1L;
		private SpinnerNumberModel
			thetaModel = new SpinnerNumberModel(0.0, 0.0, 1000.0, 0.01);
		private JSpinner
			spinner = new JSpinner(thetaModel);
		private JButton
			okButton = new JButton("OK"),
			cancelButton = new JButton("Cancel");
		private int
			result = JOptionPane.CANCEL_OPTION;
		
		private ThetaEditorDialog(Component owner, double init){
			super((JFrame)null, true);
			setSize(200, 100);
			if (owner != null){
				setLocation((owner.getWidth() - getWidth()) / 2, (owner.getHeight() - getHeight()) / 2);
				setLocationRelativeTo(owner);
			}
			setTitle("New Theta");
			setResizable(false);
			
			getRootPane().setDefaultButton(okButton);
			thetaModel.setValue(init);
			makeLayout();
		}

		private void makeLayout() {
			setLayout(new GridBagLayout());
			GridBagConstraints c = new GridBagConstraints();
			c.fill = HORIZONTAL;
			c.gridwidth = RELATIVE;
			c.insets = new Insets(3,3,3,3);
			c.weightx = 0;
			
			add(new JLabel("Edge Theta"), c);
			c.gridwidth = REMAINDER;
			add(spinner, c);
			c.gridwidth = RELATIVE;
			add(okButton, c);
			add(cancelButton, c);
			
			okButton.addActionListener(this);
			cancelButton.addActionListener(this);
		}

		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == okButton)
				result = JOptionPane.OK_OPTION;
			setVisible(false);
		}

		
		public static double showEdgeThetaDialog(Component owner, double init){
			ThetaEditorDialog dialog = new ThetaEditorDialog(owner, init);
			dialog.setVisible(true);
			if (dialog.result == JOptionPane.OK_OPTION)
				return dialog.thetaModel.getNumber().doubleValue();
			else
				return -1;
		}

	}
	
	
	public void commitEdit(HalfEdgeDataStructure<CPVertex, CPEdge, CPFace> graph) {

	}

	public void resetTool() {

	}

	public String getName() {
		return "Theta Edit";
	}

	public Icon getIcon() {
		return icon;
	}

	public String getDescription() {
		return "Edit Edge Theta";
	}

	public String getShortDescription() {
		return "Theta";
	}

	
	public void paint(GraphGraphics g) {
		HalfEdgeDataStructure<CPVertex, CPEdge, CPFace> graph = controller.getEditedGraph();
		for (CPEdge edge : graph.getPositiveEdges()){
			Point2d t = edge.getTargetVertex().getXY();
			Point2d s = edge.getStartVertex().getXY();
			Point2d mid = new Point2d((s.x + t.x) / 2, (s.y + t.y) / 2);
			Point drawPos = g.toViewCoord(mid);
			g.getGraphics().setColor(labelColor);
			g.getGraphics().setFont(controller.getFontController().getIndexFont());
			BigDecimal value = new BigDecimal(edge.getTheta());
			value = value.round(new MathContext(3));
			g.getGraphics().drawString(value + "", drawPos.x, drawPos.y);
		}
	}

	public boolean needsRepaint() {
		return true;
	}

	public JPanel getOptionPanel() {
		if (optionsPanel == null) {
			optionsPanel = new Options();
		}
		return optionsPanel;
	}

	public Color getLabelColor() {
		return labelColor;
	}

	public void setLabelColor(Color labelColor) {
		this.labelColor = labelColor;
	}

	
	private class Options extends JPanel implements ActionListener{

		private static final long 
			serialVersionUID = 1L;
		private GridBagConstraints
			c = new GridBagConstraints();
		private JPanel
			boundarySetPanel = new JPanel();
		private JButton
			setPi2Button = new JButton(new ImageIcon(ImageHook.getImage("buttonPlusPI2.png"))),
			setPi3Button = new JButton(new ImageIcon(ImageHook.getImage("buttonPlusPI3.png"))),
			setPi4Button = new JButton(new ImageIcon(ImageHook.getImage("buttonPlusPI4.png"))),
			setPi6Button = new JButton(new ImageIcon(ImageHook.getImage("buttonPlusPI6.png")));
			
		
		public Options() {
			setLayout(new GridBagLayout());
			c.fill = BOTH;
			c.weightx = 1.0;
			c.gridwidth = REMAINDER;
			boundarySetPanel.setBorder(BorderFactory.createTitledBorder("Boundary"));
			boundarySetPanel.setLayout(new GridLayout(1,4));
			boundarySetPanel.add(setPi2Button);
			boundarySetPanel.add(setPi3Button);
			boundarySetPanel.add(setPi4Button);
			boundarySetPanel.add(setPi6Button);
			add(boundarySetPanel, c);
		}


		public void actionPerformed(ActionEvent ae) {
			for (CPEdge e : controller.getEditedGraph().getEdges()) {
				if (ae.getSource() == setPi2Button) {
					e.setTheta(PI / 2);
				} else if (ae.getSource() == setPi3Button) {
					e.setTheta(PI / 3);
				} else if (ae.getSource() == setPi4Button) {
					e.setTheta(PI / 4);
				} else if (ae.getSource() == setPi6Button) {
					e.setTheta(PI / 6);
				}
			}
		}
		
	}
	
	
	
}
