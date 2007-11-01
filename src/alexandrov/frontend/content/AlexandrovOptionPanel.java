package alexandrov.frontend.content;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import koebe.frontend.content.ColorChooseJButton;
import koebe.frontend.content.ColorChooseJButton.ColorChangedEvent;
import koebe.frontend.content.ColorChooseJButton.ColorChangedListener;
import alexandrov.frontend.controller.MainController;
import circlepatterns.frontend.content.ShrinkPanel;



/**
 * The option panel for the polyhedron view
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 */
@SuppressWarnings("serial")
public class AlexandrovOptionPanel extends ShrinkPanel implements ActionListener, ColorChangedListener, ChangeListener{

	private AlexandrovPolytopView
		view = null;
	private JPanel
		viewOptPanel = new JPanel(),
		geomOptPanel = new JPanel();
	private JCheckBox
		antialiasChecker = null,
		showGraph = null,
		showMedial = null,
		showPolyeder = null,
		showGrid = null,
		light1Checker = null,
		light2Checker = null,
		showMeshChecker = null,
		showFlippedChecker = null,
		showVertexIndexChecker = null,
		showEdgeLengthChecker = null,
		smoothShadingChecker = null,
		hideHiddenEdgesChecker = null;
	private ColorChooseJButton
		polyederColorBtn = null,
		light1ColorBtn = null,
		light2ColorBtn = null,
		meshColorBtn = null,
		backgroundColorBtn = null;
	private JSlider
		polyederTransSlider = null,
		light1IntesSlider = null,
		light2IntesSlider = null,
		meshWidthSlider = null;
	
	public AlexandrovOptionPanel(MainController controller,  AlexandrovPolytopView view){
		super("View Options");
		antialiasChecker = new JCheckBox("Antialiasing", view.isAntialias());
		showGraph = new JCheckBox("Graph", view.isShowGraph());
		showMedial = new JCheckBox("Medial", view.isShowMedial());
		showPolyeder = new JCheckBox("Polytop", view.isShowPolyeder());
		showGrid = new JCheckBox("Grid", view.isShowGrid());
		light1Checker = new JCheckBox("Light 1", view.isLight1On());
		light2Checker = new JCheckBox("Light 2", view.isLight2On());
		showMeshChecker = new JCheckBox("Outline", view.isShowMesh());
		showFlippedChecker = new JCheckBox("Show Flipped Edges", view.isShowFlippedEdges());
		showVertexIndexChecker = new JCheckBox("Show Vertex Indices", view.isShowVertexIndices());
		showEdgeLengthChecker = new JCheckBox("Show Edge Lengths", view.isShowEdgeLengths());
		smoothShadingChecker = new JCheckBox("Smooth Shading", view.isSmoothShading());
		hideHiddenEdgesChecker = new JCheckBox("Hide Hidden Edges", view.isHideHiddenEdges());
		
		polyederColorBtn = new ColorChooseJButton(controller.getMainPanel(), view.getFaceColor());
		light1ColorBtn = new ColorChooseJButton(controller.getMainPanel(), view.getLight1Color());
		light2ColorBtn = new ColorChooseJButton(controller.getMainPanel(), view.getLight2Color());
		meshColorBtn = new ColorChooseJButton(controller.getMainPanel(), view.getMeshColor());
		backgroundColorBtn = new ColorChooseJButton(controller.getMainPanel(), view.getBackgroundColor());
		
		polyederTransSlider = new JSlider(0, 99, (int)(99 * view.getTransparencyPolyederValue()));
		light1IntesSlider = new JSlider(0, 100, (int)(100.0 * view.getLight1intensity() / 12.0));
		light2IntesSlider = new JSlider(0, 100, (int)(100.0 * view.getLight2intensity() / 12.0));
		meshWidthSlider = new JSlider(0, 100, (int)(100.0 * view.getMeshWidth() / 0.1));
		
		this.view = view;
		
		// view options
		viewOptPanel.setBorder(BorderFactory.createTitledBorder("View Options"));
		viewOptPanel.setLayout(new GridBagLayout());
		GridBagConstraints c1 = new GridBagConstraints();
		c1.fill = GridBagConstraints.HORIZONTAL;
		c1.anchor = GridBagConstraints.NORTHWEST;
		c1.weighty = 1;
		
		c1.gridwidth = 2;
		c1.weightx = 1;
		viewOptPanel.add(light1Checker, c1);
		c1.gridwidth = GridBagConstraints.REMAINDER;
		c1.weightx = 0;
		viewOptPanel.add(light1ColorBtn, c1);
		
		c1.gridwidth = 1;
		viewOptPanel.add(new JLabel("Intesity"), c1);
		c1.weightx = 1;
		c1.gridwidth = GridBagConstraints.REMAINDER;
		viewOptPanel.add(light1IntesSlider, c1);

		c1.gridwidth = 2;
		c1.weightx = 1;
		viewOptPanel.add(light2Checker, c1);
		c1.gridwidth = GridBagConstraints.REMAINDER;
		c1.weightx = 0;
		viewOptPanel.add(light2ColorBtn, c1);
		
		c1.gridwidth = 1;
		c1.weightx = 0;
		viewOptPanel.add(new JLabel("Intesity"), c1);
		c1.weightx = 1;
		c1.gridwidth = GridBagConstraints.REMAINDER;
		viewOptPanel.add(light2IntesSlider, c1);
		
		c1.gridwidth = 1;
		c1.weightx = 1;
		c1.gridwidth = GridBagConstraints.REMAINDER;
		viewOptPanel.add(smoothShadingChecker, c1);

		c1.gridwidth = 2;
		c1.weightx = 1;
		viewOptPanel.add(new JLabel("Background Color"), c1);
		c1.gridwidth = GridBagConstraints.REMAINDER;
		c1.weightx = 0;
		viewOptPanel.add(backgroundColorBtn, c1);
		
//		c1.weightx = 1;
//		c1.gridwidth = GridBagConstraints.REMAINDER;
//		viewOptPanel.add(antialiasChecker, c1);
		
		// geometry options
		geomOptPanel.setBorder(BorderFactory.createTitledBorder("Geometry Options"));
		geomOptPanel.setLayout(new GridBagLayout());
		GridBagConstraints c2 = new GridBagConstraints();
		c2.fill = GridBagConstraints.HORIZONTAL;
		c2.anchor = GridBagConstraints.NORTHWEST;
		c2.weighty = 1;
		
		c2.weightx = 1;
		c2.gridwidth = 2;
		geomOptPanel.add(showPolyeder, c2);
		c2.gridwidth = GridBagConstraints.REMAINDER;
		c2.weightx = 0;
		geomOptPanel.add(polyederColorBtn, c2);
		
		c2.gridwidth = 1;
		c2.weightx = 0;
		geomOptPanel.add(new JLabel("Alpha"), c2);
		c2.weightx = 1;
		c2.gridwidth = GridBagConstraints.REMAINDER;
		geomOptPanel.add(polyederTransSlider, c2);
		
		c2.weightx = 1;
		c2.gridwidth = 2;
		geomOptPanel.add(showMeshChecker, c2);
		c2.gridwidth = GridBagConstraints.REMAINDER;
		c2.weightx = 0;
		geomOptPanel.add(meshColorBtn, c2);
		
		c2.gridwidth = 1;
		c2.weightx = 0;
		geomOptPanel.add(new JLabel("Tubes"), c2);
		c2.weightx = 1;
		c2.gridwidth = GridBagConstraints.REMAINDER;
		geomOptPanel.add(meshWidthSlider, c2);
		
		geomOptPanel.add(showFlippedChecker, c2);
		geomOptPanel.add(hideHiddenEdgesChecker, c2);
		geomOptPanel.add(showVertexIndexChecker, c2);
		geomOptPanel.add(showEdgeLengthChecker, c2);
		
		
		//main layout
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1;
		c.weighty = 1;
		c.gridwidth = GridBagConstraints.RELATIVE;
		add(viewOptPanel, c);
		c.gridwidth = GridBagConstraints.REMAINDER;
		add(geomOptPanel, c);
		
		antialiasChecker.addActionListener(this);
		showGraph.addActionListener(this);
		showMedial.addActionListener(this);
		showPolyeder.addActionListener(this);
		showGrid.addActionListener(this);
		showMeshChecker.addActionListener(this);
		light1Checker.addActionListener(this);
		light2Checker.addActionListener(this);
		showFlippedChecker.addActionListener(this);
		showVertexIndexChecker.addActionListener(this);
		showEdgeLengthChecker.addActionListener(this);
		smoothShadingChecker.addActionListener(this);
		hideHiddenEdgesChecker.addActionListener(this);
		
		polyederColorBtn.addColorChangedListener(this);
		light1ColorBtn.addColorChangedListener(this);
		light2ColorBtn.addColorChangedListener(this);
		meshColorBtn.addColorChangedListener(this);
		backgroundColorBtn.addColorChangedListener(this);
		
		polyederTransSlider.addChangeListener(this);
		light1IntesSlider.addChangeListener(this);
		light2IntesSlider.addChangeListener(this);
		meshWidthSlider.addChangeListener(this);
	}

	public void actionPerformed(ActionEvent e) {
		if (antialiasChecker == e.getSource())
			view.setAntialias(antialiasChecker.isSelected());
		if (showGraph == e.getSource())
			view.setShowGraph(showGraph.isSelected());
		if (showMedial == e.getSource())
			view.setShowMedial(showMedial.isSelected());
		if (showPolyeder == e.getSource())
			view.setShowPolyeder(showPolyeder.isSelected());
		if (showGrid == e.getSource())
			view.setShowGrid(showGrid.isSelected());
		if (showMeshChecker == e.getSource())
			view.setShowMesh(showMeshChecker.isSelected());
		if (light1Checker == e.getSource())
			view.setLight1On(light1Checker.isSelected());
		if (light2Checker == e.getSource())
			view.setLight2On(light2Checker.isSelected());
		if (showFlippedChecker == e.getSource())
			view.setShowFlippedEdges(showFlippedChecker.isSelected());
		if (showVertexIndexChecker == e.getSource())
			view.setShowVertexIndices(showVertexIndexChecker.isSelected());
		if (showEdgeLengthChecker == e.getSource())
			view.setShowEdgeLengths(showEdgeLengthChecker.isSelected());
		if (smoothShadingChecker == e.getSource())
			view.setSmoothShading(smoothShadingChecker.isSelected());
		if (hideHiddenEdgesChecker == e.getSource())
			view.setHideHiddenEdges(hideHiddenEdgesChecker.isSelected());
		view.updateProperties();
	}

	
	public void colorChanged(ColorChangedEvent cce) {
		if (polyederColorBtn == cce.getSource())
			view.setFaceColor(cce.getColor());
		if (light1ColorBtn == cce.getSource())
			view.setLight1Color(cce.getColor());
		if (light2ColorBtn == cce.getSource())
			view.setLight2Color(cce.getColor());
		if (meshColorBtn == cce.getSource())
			view.setMeshColor(cce.getColor());
		if (backgroundColorBtn == cce.getSource())
			view.setBackgroundColor(cce.getColor());
		view.updateProperties();
	}

	public void stateChanged(ChangeEvent e) {
		if (e.getSource() == polyederTransSlider)
			view.setTransparencyPolyederValue(polyederTransSlider.getValue() / 99.0);
		if (e.getSource() == light1IntesSlider)
			view.setLight1intensity(light1IntesSlider.getValue() * 12.0 / 100.0);
		if (e.getSource() == light2IntesSlider)
			view.setLight2intensity(light2IntesSlider.getValue() * 12.0 / 100.0);
		if (e.getSource() == meshWidthSlider)
			view.setMeshWidth(meshWidthSlider.getValue() * 0.1 / 100.0);
		view.updateProperties();
	}
	
	
	
}
