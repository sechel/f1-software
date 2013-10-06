package minimalsurface.frontend.tool;

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
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.List;

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
public class EditCapitalPhi implements GraphTool<CPVertex, CPEdge, CPFace> {

	private Icon 
		icon = new ImageIcon(ImageHook.getImage("phi.png"));
	private MainController
		controller = null;
	private Color
		labelColor = Color.RED;
	
	@Override
	public Boolean initTool() {
		return true;
	}

	@Override
	public void leaveTool() {

	}

	@Override
	public void setController(halfedge.frontend.controller.MainController<CPVertex, CPEdge, CPFace> controller) {
		this.controller = (MainController)controller;
	}

	@Override
	public boolean processEditOperation(EditOperation operation) throws EditOperationException {
		CPFace f = CPFace.class.cast(operation.face);	
		switch (operation){
		case SELECT_FACE:
			double newPhi = PhiEditorDialog.showEdgeLengthDialog(controller.getGraphEditor(), f.getCapitalPhi());
			if (newPhi == -1)
				break;
			else {
				f.setCapitalPhi(newPhi);
				controller.fireGraphChanged();
			}
			break;
		default:
			break;
		}
		controller.refreshEditor();
		return false;
	}

	
	public static class PhiEditorDialog extends JDialog implements ActionListener{

		private static final long 
			serialVersionUID = 1L;
		private SpinnerNumberModel
			phiModel = new SpinnerNumberModel(0.0, 0.0, 4 * PI, 0.001);
		private JSpinner
			spinner = new JSpinner(phiModel);
		private JButton
			okButton = new JButton("OK"),
			cancelButton = new JButton("Cancel");
		private int
			result = JOptionPane.CANCEL_OPTION;
		
		private PhiEditorDialog(Component owner, double init){
			super((JFrame)null, true);
			setSize(200, 100);
			if (owner != null){
				setLocation((owner.getWidth() - getWidth()) / 2, (owner.getHeight() - getHeight()) / 2);
				setLocationRelativeTo(owner);
			}
			setTitle("New Phi");
			setResizable(false);
			
			getRootPane().setDefaultButton(okButton);
			phiModel.setValue(init);
			makeLayout();
		}

		private void makeLayout() {
			setLayout(new GridBagLayout());
			GridBagConstraints c = new GridBagConstraints();
			c.fill = HORIZONTAL;
			c.gridwidth = RELATIVE;
			c.insets = new Insets(3,3,3,3);
			c.weightx = 0;
			
			add(new JLabel("Capital Phi"), c);
			c.gridwidth = REMAINDER;
			add(spinner, c);
			c.gridwidth = RELATIVE;
			add(okButton, c);
			add(cancelButton, c);
			
			okButton.addActionListener(this);
			cancelButton.addActionListener(this);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == okButton)
				result = JOptionPane.OK_OPTION;
			setVisible(false);
		}

		
		public static double showEdgeLengthDialog(Component owner, double init){
			PhiEditorDialog dialog = new PhiEditorDialog(owner, init);
			dialog.setVisible(true);
			if (dialog.result == JOptionPane.OK_OPTION)
				return dialog.phiModel.getNumber().doubleValue();
			else
				return -1;
		}

	}
	
	
	@Override
	public void commitEdit(HalfEdgeDataStructure<CPVertex, CPEdge, CPFace> graph) {

	}

	@Override
	public void resetTool() {

	}

	@Override
	public String getName() {
		return "Phi Edge";
	}

	@Override
	public Icon getIcon() {
		return icon;
	}

	@Override
	public String getDescription() {
		return "Edit Capital Phi";
	}

	@Override
	public String getShortDescription() {
		return "Phi";
	}

	
	@Override
	public void paint(GraphGraphics g) {
		HalfEdgeDataStructure<CPVertex, CPEdge, CPFace> graph = controller.getEditedGraph();
		for (CPFace f : graph.getFaces()){
			Point2d mean = new Point2d();
			List<CPEdge> boundary = f.getBoundary();
			for (CPEdge b : boundary)
				mean.add(b.getTargetVertex().getXY());
			mean.scale(1.0 / boundary.size());
			Point drawPos = g.toViewCoord(mean);
			g.getGraphics().setColor(labelColor);
			g.getGraphics().setFont(controller.getFontController().getIndexFont());
			BigDecimal value = new BigDecimal(f.getCapitalPhi());
			value = value.round(new MathContext(3));
			g.getGraphics().drawString(value + "", drawPos.x, drawPos.y);
		}
	}

	@Override
	public boolean needsRepaint() {
		return true;
	}

	@Override
	public JPanel getOptionPanel() {
		return null;
	}

	public Color getLabelColor() {
		return labelColor;
	}

	public void setLabelColor(Color labelColor) {
		this.labelColor = labelColor;
	}

}
