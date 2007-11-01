package koebe.frontend.content.joglviewer;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import circlepatterns.frontend.content.ShrinkPanel;



/**
 * The option panel for the OpenGL polyhedron view
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 */
@SuppressWarnings("serial")
public class OptionPanel extends ShrinkPanel implements ActionListener{

	private KoebePolyhedronView
		view = null;
	private JPanel
		viewOptPanel = new JPanel(),
		geomOptPanel = new JPanel();
	private JCheckBox
		antialiasChecker = null,
		showGraph = null,
		showMedial = null,
		showCircles = null,
		showPolyeder = null,
		showSphere = null,
		showGrid = null,
		normalize = null;
	
	public OptionPanel(KoebePolyhedronView view){
		super("View Options");
		antialiasChecker = new JCheckBox("Antialiasing", view.antialias);
		showGraph = new JCheckBox("Graph", view.showGraph);
		showMedial = new JCheckBox("Medial", view.showMedial);
		showCircles = new JCheckBox("Circles", view.showCircles);
		showPolyeder = new JCheckBox("Polyhedron", view.showPolyeder);
		showSphere = new JCheckBox("Sphere", view.showSphere);
		showGrid = new JCheckBox("Grid", view.showGrid);
		normalize = new JCheckBox("Normalize Polyhedron", view.getController().isNormalize());
		
		this.view = view;
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1;
		c.gridwidth = GridBagConstraints.RELATIVE;
		
		viewOptPanel.setBorder(BorderFactory.createTitledBorder("View Options"));
		viewOptPanel.setLayout(new GridBagLayout());
		c.gridwidth = GridBagConstraints.REMAINDER;
		viewOptPanel.add(antialiasChecker, c);
		viewOptPanel.add(normalize, c);
		c.gridwidth = GridBagConstraints.RELATIVE;
		add(viewOptPanel, c);
		
		geomOptPanel.setBorder(BorderFactory.createTitledBorder("Geometry Options"));
		geomOptPanel.setLayout(new GridLayout(2, 2));
		geomOptPanel.add(showCircles);
//		geomOptPanel.add(showGraph);
		geomOptPanel.add(showMedial);
		geomOptPanel.add(showPolyeder);
		geomOptPanel.add(showSphere);
//		geomOptPanel.add(showGrid);
		add(geomOptPanel, c);
		
		antialiasChecker.addActionListener(this);
		showCircles.addActionListener(this);
		showGraph.addActionListener(this);
		showMedial.addActionListener(this);
		showPolyeder.addActionListener(this);
		showSphere.addActionListener(this);
		showGrid.addActionListener(this);
		normalize.addActionListener(this);
	}

	public void actionPerformed(ActionEvent e) {
		if (antialiasChecker == e.getSource())
			view.antialias = antialiasChecker.isSelected();
		if (showCircles == e.getSource())
			view.showCircles = showCircles.isSelected();
		if (showGraph == e.getSource())
			view.showGraph = showGraph.isSelected();
		if (showMedial == e.getSource())
			view.showMedial = showMedial.isSelected();
		if (showPolyeder == e.getSource())
			view.showPolyeder = showPolyeder.isSelected();
		if (showSphere == e.getSource())
			view.showSphere = showSphere.isSelected();
		if (showGrid == e.getSource())
			view.showGrid = showGrid.isSelected();
		if (normalize == e.getSource()){
			view.getController().setNormalize(normalize.isSelected());
			view.getController().fireGraphChanged();
		}
		synchronized (view) {
			view.notify();
		}
	}
	
	
	
	
}
