package koebe.frontend.content.jrealityviewer;

import halfedge.frontend.controller.MainController;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
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
import circlepatterns.frontend.content.ShrinkPanel;
import circlepatterns.graph.CPEdge;
import circlepatterns.graph.CPFace;
import circlepatterns.graph.CPVertex;



/**
 * The option panel for the jReality polyhedron view
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 */
@SuppressWarnings("serial")
public class KoebeOptionPanel extends ShrinkPanel implements ActionListener, ColorChangedListener, ChangeListener{

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
		showCircles2 = null,
		showPolyeder = null,
		showSphere = null,
		showGrid = null,
		normalize = null,
		light1Checker = null,
		light2Checker = null,
		showMeshChecker = null;
	private ColorChooseJButton
		polyederColorBtn = null,
		sphereColorBtn = null,
		light1ColorBtn = null,
		light2ColorBtn = null,
		cicles1ColorBtn = null,
		cicles2ColorBtn = null,
		meshColorBtn = null,
		backgroundColorBtn = null;
	private JSlider
		polyederTransSlider = null,
		sphereTransSlider = null,
		light1IntesSlider = null,
		light2IntesSlider = null,
		meshWidthSlider = null,
		circles1WidthSlider = null,
		circles2WidthSlider = null;
	
	public KoebeOptionPanel(MainController<CPVertex, CPEdge, CPFace> controller,  KoebePolyhedronView view){
		super("View Options");
		antialiasChecker = new JCheckBox("Antialiasing", view.isAntialias());
		showGraph = new JCheckBox("Graph", view.isShowGraph());
		showMedial = new JCheckBox("Medial", view.isShowMedial());
		showCircles = new JCheckBox("Circles 1", view.isShowCircles());
		showCircles2 = new JCheckBox("Circles 2", view.isShowCircles2());
		showPolyeder = new JCheckBox("Faces", view.isShowPolyeder());
		showSphere = new JCheckBox("Sphere", view.isShowSphere());
		showGrid = new JCheckBox("Grid", view.isShowGrid());
		normalize = new JCheckBox("Normalize Polyhedron", view.getController().isNormalize());
		light1Checker = new JCheckBox("Light 1", view.isLight1On());
		light2Checker = new JCheckBox("Light 2", view.isLight2On());
		showMeshChecker = new JCheckBox("Outline", view.isShowMesh());
		
		polyederColorBtn = new ColorChooseJButton(controller.getMainPanel(), view.getFaceColor());
		sphereColorBtn = new ColorChooseJButton(controller.getMainPanel(), view.getSphereColor());
		light1ColorBtn = new ColorChooseJButton(controller.getMainPanel(), view.getLight1Color());
		light2ColorBtn = new ColorChooseJButton(controller.getMainPanel(), view.getLight2Color());
		cicles1ColorBtn = new ColorChooseJButton(controller.getMainPanel(), view.getCircles1Color());
		cicles2ColorBtn = new ColorChooseJButton(controller.getMainPanel(), view.getCircles2Color());
		meshColorBtn = new ColorChooseJButton(controller.getMainPanel(), view.getMeshColor());
		backgroundColorBtn = new ColorChooseJButton(controller.getMainPanel(), view.getBackgoundColor());
		
		polyederTransSlider = new JSlider(0, 99, (int)(99 * view.getTransparencyPolyederValue()));
		sphereTransSlider = new JSlider(0, 99, (int)(99 * view.getTransparencySphereValue()));
		light1IntesSlider = new JSlider(0, 100, (int)(100.0 * view.getLight1intensity() / 8.0));
		light2IntesSlider = new JSlider(0, 100, (int)(100.0 * view.getLight2intensity() / 8.0));
		meshWidthSlider = new JSlider(0, 100, (int)(100.0 * view.getMeshWidth() / 0.1));
		circles1WidthSlider = new JSlider(0, 100, (int)(100.0 * view.getCircles1Width() / 0.1));
		circles2WidthSlider = new JSlider(0, 100, (int)(100.0 * view.getCircles2Width() / 0.1));
		
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
		viewOptPanel.add(new JLabel("Backgound Color"), c1);
		c1.weightx = 0;
		c1.gridwidth = GridBagConstraints.REMAINDER;
		viewOptPanel.add(backgroundColorBtn, c1);
		
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
		c1.weightx = 0;
		viewOptPanel.add(new JLabel("Faces"), c1);
		c1.weightx = 1;
		c1.gridwidth = GridBagConstraints.REMAINDER;
		viewOptPanel.add(polyederTransSlider, c1);
		
		c1.gridwidth = 1;
		c1.weightx = 0;
		viewOptPanel.add(new JLabel("Sphere"), c1);
		c1.weightx = 1;
		c1.gridwidth = GridBagConstraints.REMAINDER;
		viewOptPanel.add(sphereTransSlider, c1);
		
		viewOptPanel.add(normalize, c1);
		
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
		
		c2.weightx = 1;
		c2.gridwidth = 2;
		geomOptPanel.add(showSphere, c2);
		c2.gridwidth = GridBagConstraints.REMAINDER;
		c2.weightx = 0;
		geomOptPanel.add(sphereColorBtn, c2);
		
		c2.gridwidth = 2;
		c2.weightx = 1;
		geomOptPanel.add(showCircles, c2);
		c2.gridwidth = GridBagConstraints.REMAINDER;
		c2.weightx = 0;
		geomOptPanel.add(cicles1ColorBtn, c2);
		
		c2.gridwidth = 1;
		c2.weightx = 0;
		geomOptPanel.add(new JLabel("Tubes"), c2);
		c2.weightx = 1;
		c2.gridwidth = GridBagConstraints.REMAINDER;
		geomOptPanel.add(circles1WidthSlider, c2);
		
		c2.gridwidth = 2;
		c2.weightx = 1;
		geomOptPanel.add(showCircles2, c2);
		c2.gridwidth = GridBagConstraints.REMAINDER;
		c2.weightx = 0;
		geomOptPanel.add(cicles2ColorBtn, c2);

		c2.gridwidth = 1;
		c2.weightx = 0;
		geomOptPanel.add(new JLabel("Tubes"), c2);
		c2.weightx = 1;
		c2.gridwidth = GridBagConstraints.REMAINDER;
		geomOptPanel.add(circles2WidthSlider, c2);
		
		//main layout
		setLayout(new GridLayout(1, 2));
		add(viewOptPanel);
		add(geomOptPanel);
		
		antialiasChecker.addActionListener(this);
		showCircles.addActionListener(this);
		showCircles2.addActionListener(this);
		showGraph.addActionListener(this);
		showMedial.addActionListener(this);
		showPolyeder.addActionListener(this);
		showSphere.addActionListener(this);
		showGrid.addActionListener(this);
		normalize.addActionListener(this);
		showMeshChecker.addActionListener(this);
		light1Checker.addActionListener(this);
		light2Checker.addActionListener(this);
		
		polyederColorBtn.addColorChangedListener(this);
		sphereColorBtn.addColorChangedListener(this);
		light1ColorBtn.addColorChangedListener(this);
		light2ColorBtn.addColorChangedListener(this);
		cicles1ColorBtn.addColorChangedListener(this);
		cicles2ColorBtn.addColorChangedListener(this);
		meshColorBtn.addColorChangedListener(this);
		backgroundColorBtn.addColorChangedListener(this);
		
		polyederTransSlider.addChangeListener(this);
		sphereTransSlider.addChangeListener(this);
		light1IntesSlider.addChangeListener(this);
		light2IntesSlider.addChangeListener(this);
		meshWidthSlider.addChangeListener(this);
		circles1WidthSlider.addChangeListener(this);
		circles2WidthSlider.addChangeListener(this);
	}

	public void actionPerformed(ActionEvent e) {
		if (antialiasChecker == e.getSource())
			view.setAntialias(antialiasChecker.isSelected());
		if (showCircles == e.getSource())
			view.setShowCircles(showCircles.isSelected());
		if (showCircles2 == e.getSource())
			view.setShowCircles2(showCircles2.isSelected());
		if (showGraph == e.getSource())
			view.setShowGraph(showGraph.isSelected());
		if (showMedial == e.getSource())
			view.setShowMedial(showMedial.isSelected());
		if (showPolyeder == e.getSource())
			view.setShowPolyeder(showPolyeder.isSelected());
		if (showSphere == e.getSource())
			view.setShowSphere(showSphere.isSelected());
		if (showGrid == e.getSource())
			view.setShowGrid(showGrid.isSelected());
		if (normalize == e.getSource()){
			view.getController().setNormalize(normalize.isSelected());
			view.getController().fireGraphChanged();
		}
		if (showMeshChecker == e.getSource())
			view.setShowMesh(showMeshChecker.isSelected());
		if (light1Checker == e.getSource())
			view.setLight1On(light1Checker.isSelected());
		if (light2Checker == e.getSource())
			view.setLight2On(light2Checker.isSelected());
		view.updateProperties();
	}

	
	public void colorChanged(ColorChangedEvent cce) {
		if (polyederColorBtn == cce.getSource())
			view.setFaceColor(cce.getColor());
		if (sphereColorBtn == cce.getSource())
			view.setSphereColor(cce.getColor());
		if (light1ColorBtn == cce.getSource())
			view.setLight1Color(cce.getColor());
		if (light2ColorBtn == cce.getSource())
			view.setLight2Color(cce.getColor());
		if (cicles1ColorBtn == cce.getSource())
			view.setCircles1Color(cce.getColor());
		if (cicles2ColorBtn == cce.getSource())
			view.setCircles2Color(cce.getColor());
		if (meshColorBtn == cce.getSource())
			view.setMeshColor(cce.getColor());
		if (backgroundColorBtn == cce.getSource())
			view.setBackgoundColor(cce.getColor());
		view.updateProperties();
	}

	public void stateChanged(ChangeEvent e) {
		if (e.getSource() == polyederTransSlider)
			view.setTransparencyPolyederValue(polyederTransSlider.getValue() / 99.0);
		if (e.getSource() == sphereTransSlider)
			view.setTransparencySphereValue(sphereTransSlider.getValue() / 99.0);
		if (e.getSource() == light1IntesSlider)
			view.setLight1intensity(light1IntesSlider.getValue() * 8.0 / 100.0);
		if (e.getSource() == light2IntesSlider)
			view.setLight2intensity(light2IntesSlider.getValue() * 8.0 / 100.0);
		if (e.getSource() == meshWidthSlider)
			view.setMeshWidth(meshWidthSlider.getValue() * 0.1 / 100.0);
		if (e.getSource() == circles1WidthSlider)
			view.setCircles1Width(circles1WidthSlider.getValue() * 0.1 / 100.0);
		if (e.getSource() == circles2WidthSlider)
			view.setCircles2Width(circles2WidthSlider.getValue() * 0.1 / 100.0);
		view.updateProperties();
	}
	
	
	
}
