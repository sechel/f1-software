package circlepatterns.frontend.content.euclidean;

import static java.awt.GridBagConstraints.HORIZONTAL;
import static java.awt.GridBagConstraints.RELATIVE;
import static java.awt.GridBagConstraints.REMAINDER;
import static java.awt.GridBagConstraints.WEST;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import koebe.frontend.content.ColorChooseJButton;
import koebe.frontend.content.ColorChooseJButton.ColorChangedEvent;
import koebe.frontend.content.ColorChooseJButton.ColorChangedListener;
import circlepatterns.frontend.action.ExportPDFAction;
import circlepatterns.frontend.content.ShrinkPanel;


/**
 * The options panel for the circle pattern view
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 */
@SuppressWarnings("serial")
public class EuclideanViewOptions extends ShrinkPanel implements ChangeListener, ActionListener, ColorChangedListener{

	private EuclideanCirclePatternView
		view = null;
	private JSlider
		scaleSlider = new JSlider(1, 200);
	private JCheckBox
		antialiasChecker = new JCheckBox("Antialiasing"),
		showVerticesChecker = new JCheckBox("Vertices"),
		showGraphChecker  = new JCheckBox("Graph"),
		showDualGraphChecker  = new JCheckBox("Dual Graph"),
		showCircles = new JCheckBox("Circles"),
		showIndicesChecker = new JCheckBox("Indices"),
		showRadiiChecker = new JCheckBox("Radii");
	private ColorChooseJButton
		circles1ColorBtn = null,
		circles2ColorBtn = null,
		graphColorBtn = null,
		dualColorBtn = null;
	private JButton
		exportPDFButton = null;
	private JPanel
		visibilityPanel = new JPanel();
	
	public EuclideanViewOptions(EuclideanCirclePatternView view) {
		super("Circle Pattern View Options");
		this.view = view;
		exportPDFButton = new JButton(new ExportPDFAction(view, view));
		circles1ColorBtn = new ColorChooseJButton(view, view.circles1Color);
		circles2ColorBtn = new ColorChooseJButton(view, view.circles2Color);
		graphColorBtn = new ColorChooseJButton(view, view.graphColor);
		dualColorBtn = new ColorChooseJButton(view, view.dualGraphColor);
		
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = HORIZONTAL;
		c.anchor = WEST;
		c.weightx = 1;

		
		visibilityPanel.setLayout(new GridBagLayout());
		visibilityPanel.setBorder(BorderFactory.createTitledBorder("Visibility"));
		c.gridwidth = 3;
		visibilityPanel.add(showVerticesChecker, c);
		c.gridy = 1;
		c.gridwidth = 2;
		visibilityPanel.add(showGraphChecker, c);
		c.gridwidth = 1;
		visibilityPanel.add(graphColorBtn, c);
		c.gridy = 2;
		c.gridwidth = 1;
		visibilityPanel.add(showCircles, c);
		visibilityPanel.add(circles1ColorBtn, c);
		visibilityPanel.add(circles2ColorBtn, c);
		c.gridy = 3;
		c.gridwidth = 2;
		visibilityPanel.add(showDualGraphChecker, c);
		c.gridwidth = 1;
		visibilityPanel.add(dualColorBtn, c);
		c.gridwidth = 2;
		c.gridy = 4;
		visibilityPanel.add(showIndicesChecker, c);
		c.gridwidth = 1;
		visibilityPanel.add(showRadiiChecker, c);		
		
		circles1ColorBtn.addColorChangedListener(this);
		circles2ColorBtn.addColorChangedListener(this);
		graphColorBtn.addColorChangedListener(this);
		dualColorBtn.addColorChangedListener(this);
		showVerticesChecker.addActionListener(this);
		showVerticesChecker.setSelected(view.showVertices);
		showCircles.addActionListener(this);
		showCircles.setSelected(view.showCircles);
		showDualGraphChecker.addActionListener(this);
		showDualGraphChecker.setSelected(view.showDualGraph);
		showGraphChecker.addActionListener(this);
		showGraphChecker.setSelected(view.showGraph);
		showIndicesChecker.addActionListener(this);
		showIndicesChecker.setSelected(view.showIndices);
		showRadiiChecker.addActionListener(this);
		c.gridwidth = 2;
		add(visibilityPanel, c);
		
		c.gridy = RELATIVE;
		c.gridwidth = RELATIVE;
		c.weightx = 0;
		add(new JLabel("Scale:"), c);
		c.gridwidth = REMAINDER;
		c.weightx = 1;
		scaleSlider.setValue(view.scale.intValue());
		scaleSlider.addChangeListener(this);
		add(scaleSlider, c);
		
		c.gridwidth = RELATIVE;
		add(antialiasChecker, c);
		antialiasChecker.setSelected(view.antialiasing);
		antialiasChecker.addActionListener(this);
		c.gridwidth = REMAINDER;
		add(exportPDFButton, c);
	}

	public void stateChanged(ChangeEvent e) {
		if (e.getSource() == scaleSlider){
			view.scale = Double.valueOf(scaleSlider.getValue());
		}
		view.update();
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == antialiasChecker)
			view.antialiasing = antialiasChecker.isSelected();
		if (e.getSource() == showCircles)
			view.showCircles = showCircles.isSelected();
		if (e.getSource() == showDualGraphChecker)
			view.showDualGraph = showDualGraphChecker.isSelected();
		if (e.getSource() == showGraphChecker)
			view.showGraph = showGraphChecker.isSelected();
		if (e.getSource() == showVerticesChecker)
			view.showVertices = showVerticesChecker.isSelected();
		if (e.getSource() == showIndicesChecker)
			view.showIndices = showIndicesChecker.isSelected();
		if (e.getSource() == showRadiiChecker) {
			view.showRadii = showRadiiChecker.isSelected();
		}
		view.update();
	}

	public void colorChanged(ColorChangedEvent cce) {
		Object s = cce.getSource();
		if (circles1ColorBtn == s) {
			view.circles1Color = cce.getColor();
		} else if (circles2ColorBtn == s) {
			view.circles2Color = cce.getColor();
		} else if (graphColorBtn == s) {
			view.graphColor = cce.getColor();
		} else if (dualColorBtn == s) {
			view.dualGraphColor = cce.getColor();
		}
		view.update();
	}
		
		
	
}
