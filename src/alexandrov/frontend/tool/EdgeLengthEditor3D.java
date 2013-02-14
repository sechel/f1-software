package alexandrov.frontend.tool;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import alexandrov.Alexandrov2;
import alexandrov.AlexandrovCap;
import alexandrov.frontend.content.AlexandrovPolytopView;
import alexandrov.frontend.controller.MainController;
import alexandrov.graph.CPMEdge;
import circlepatterns.frontend.content.ShrinkPanel;


/**
 * The edge length editor panel.
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 */
@SuppressWarnings("serial")
public class EdgeLengthEditor3D extends ShrinkPanel implements ChangeListener, ActionListener{

	private AlexandrovPolytopView
		view = null;
	private MainController
		controller = null;
	private CPMEdge
		editedEdge = null;
	
	private SpinnerNumberModel
		lengthModel = new SpinnerNumberModel(0, 0, 20, 0.01);
	private JSpinner
		lengthSpinner = new JSpinner(lengthModel);
	private JCheckBox
		dragEditChecker = new JCheckBox("Mouse Drag Edit");
	private JTextField
		errorField = new JTextField("1E-5");
	private SpinnerNumberModel
		maxIterModel = new SpinnerNumberModel(100, 1, 10000, 1);
	private JSpinner
		maxIterSpinner = new JSpinner(maxIterModel);
	private JPanel
		paramPanel = new JPanel();
	
	private Double
		error = 1E-2;
	
	
	public EdgeLengthEditor3D(AlexandrovPolytopView view, MainController controller) {
		super("Edge Length Editor");
		this.view = view;
		this.controller = controller;
		dragEditChecker.setSelected(view.isDragEdit());
		makeLayout();
		updateStates();
	}

	
	private class FieldFocusListener extends FocusAdapter{
		@Override
		public void focusLost(FocusEvent e) {
			verifyError();
		}
	}
	
	
	private void verifyError(){
		try {
			error = Double.parseDouble(errorField.getText());
			if (error <= 0)
				error = Double.MIN_VALUE;
			if (error > 1)
				error = 1.0;
		} catch (NumberFormatException nfe){}
		errorField.setText("" + getError());
	}
	
	
	
	private void makeLayout() {
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(3,3,3,3);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.WEST;
		
		c.gridwidth = GridBagConstraints.RELATIVE;
		add(new JLabel("Edge Length"), c);
		c.gridwidth = GridBagConstraints.REMAINDER;
		add(lengthSpinner, c);
		
		paramPanel.setLayout(new GridLayout(1, 4, 3, 3));
		paramPanel.add(new JLabel("Error"));
		paramPanel.add(errorField);
		errorField.setHorizontalAlignment(SwingConstants.RIGHT);
		errorField.addFocusListener(new FieldFocusListener());
		paramPanel.add(new JLabel("Max Iterations"));
		paramPanel.add(maxIterSpinner);
		
		c.gridwidth = GridBagConstraints.REMAINDER;
		add(paramPanel, c);
		c.gridwidth = GridBagConstraints.REMAINDER;
		add(dragEditChecker, c);
		
		maxIterSpinner.addChangeListener(this);
		dragEditChecker.addActionListener(this);
		lengthSpinner.addChangeListener(this);
		errorField.addActionListener(this);
	}
	

	private void updateStates() {
		if (editedEdge == null){
			lengthModel.setValue(0.0);
			lengthSpinner.setEnabled(false);
		} else {
			lengthSpinner.setEnabled(true);
			lengthSpinner.removeChangeListener(this);
			lengthModel.setValue(editedEdge.getLength());
			lengthSpinner.addChangeListener(this);
		}
	}
	
	
	public void setEditedEdge(CPMEdge editedEdge) {
		this.editedEdge = editedEdge;
		updateStates();
	}

	public void actionPerformed(ActionEvent e) {
		Object s = e.getSource();
		if (s == dragEditChecker)
			view.setDragEdit(dragEditChecker.isSelected());
		if (s == errorField)
			verifyError();
	}
	

	public void stateChanged(ChangeEvent e) {
		if (editedEdge == null)
			return;
		if (lengthSpinner == e.getSource()){
			Double newLength = lengthModel.getNumber().doubleValue();
			if (newLength <= 0){
				updateStates();
				return;
			}
			Double oldLength = editedEdge.getLength();
			editedEdge.setLength(newLength);
			editedEdge.getOppositeEdge().setLength(newLength);
			try {
				switch (view.getViewerMode()) {
				case VIEWER_MODE_CAP:
					AlexandrovCap.constructCap(view.getActiveGraph(), getError(), getMaxIterations());
					break;
				case VIEWER_MODE_POLYHEDRON:
					Alexandrov2.constructPolyhedron(view.getActiveGraph(), 2.0, getError(), getMaxIterations(), null, null);
					break;
				}
				view.updateGeometry(view.getActiveGraph());
				controller.setStatus("successfully constructed");
			} catch (Exception e1) {
				controller.setStatus(e1.getMessage());
				editedEdge.setLength(oldLength);
				editedEdge.getOppositeEdge().setLength(oldLength);
			}
		}
		verifyError();
		updateStates();
	}

	
	public void setEdgeLength(Double length){
		lengthModel.setValue(length);
	}



	public Double getError() {
		return error;
	}



	public void setError(Double error) {
		this.error = error;
	}
	
	
	public Integer getMaxIterations(){
		return maxIterModel.getNumber().intValue();
	}
	
}
