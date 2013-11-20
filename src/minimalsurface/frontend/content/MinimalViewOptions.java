package minimalsurface.frontend.content;

import static java.awt.GridBagConstraints.REMAINDER;
import static minimalsurface.frontend.content.MinimalViewOptions.CircleType.EqualRadiusRing;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import koebe.frontend.content.ColorChooseJButton;
import koebe.frontend.content.ColorChooseJButton.ColorChangedEvent;
import koebe.frontend.content.ColorChooseJButton.ColorChangedListener;
import minimalsurface.controller.MainController;
import de.jreality.plugin.basic.View;
import de.jtem.jrworkspace.plugin.sidecontainer.SideContainerPerspective;
import de.jtem.jrworkspace.plugin.sidecontainer.template.ShrinkPanelPlugin;



/**
 * The option panel for the jReality polyhedron view
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 */
public class MinimalViewOptions extends ShrinkPanelPlugin implements ActionListener, ColorChangedListener, ChangeListener{

	private MinimalSurfaceContent
		view = null;
	private JPanel
		viewOptPanel = new JPanel(),
		geomOptPanel = new JPanel();
	private JCheckBox
		antialiasChecker = null,
		showPolyeder = null,
		showUnitSphereChecker = null,
		light1Checker = null,
		light2Checker = null,
		circlesChecker = null,
		spheresChecker = null,
		showMeshChecker = null,
		shadingChecker = null,
		helperLinesChecker = null,
		showVerticesChecker = null;
	private ColorChooseJButton
		polyederColorBtn = null,
		light1ColorBtn = null,
		light2ColorBtn = null,
		meshColorBtn = null,
		circlesColorBtn = null,
		spheresColorBtn = null,
		helperLinesColorBtn = null,
		backgroundColorBtn = null;
	private JSlider
		polyederTransSlider = null,
		light1IntesSlider = null,
		light2IntesSlider = null,
		meshWidthSlider = null,
		helperLinesWidthSlider = null,
		vertexWidthSlider = null,
		diskThicknessSlider = null;
	private JComboBox
		circleTypeCombo = null;
	private JButton
		updateRingsBtn = new JButton("Update Rings");
	
	
	public static enum CircleType {
		Disk,
		Ring,
		EqualRadiusRing
	}
	
	
	public MinimalViewOptions(MainController controller, MinimalSurfaceContent view){
		shrinkPanel.setTitle("View Options");
		shrinkPanel.setShrinked(true);
		setInitialPosition(SHRINKER_BOTTOM);
		antialiasChecker = new JCheckBox("Antialiasing", view.isAntialias());
		showPolyeder = new JCheckBox("Faces", view.isShowSurface());
		showUnitSphereChecker = new JCheckBox("Unit Sphere", false);
		light1Checker = new JCheckBox("Light 1", view.isLight1On());
		light2Checker = new JCheckBox("Light 2", view.isLight2On());
		circlesChecker = new JCheckBox("Circles", view.isShowCircles());
		spheresChecker = new JCheckBox("Spheres", view.isShowSpheres());
		showMeshChecker = new JCheckBox("Outline", view.isShowMesh());
		shadingChecker = new JCheckBox("Shading", view.isSmoothShading());
		showVerticesChecker = new JCheckBox("Vertices", view.isShowVertices());
		helperLinesChecker = new JCheckBox("Helpers", view.isShowHelperLines());
		
		polyederColorBtn = new ColorChooseJButton(shrinkPanel, view.getFaceColor());
		light1ColorBtn = new ColorChooseJButton(shrinkPanel, view.getLight1Color());
		light2ColorBtn = new ColorChooseJButton(shrinkPanel, view.getLight2Color());
		circlesColorBtn = new ColorChooseJButton(shrinkPanel, view.getCirclesColor());
		spheresColorBtn = new ColorChooseJButton(shrinkPanel, view.getSpheresColor());
		meshColorBtn = new ColorChooseJButton(shrinkPanel, view.getMeshColor());
		backgroundColorBtn = new ColorChooseJButton(shrinkPanel, view.getBackgroundColor());
		helperLinesColorBtn = new ColorChooseJButton(shrinkPanel, view.getHelperLinesColor());
		
		polyederTransSlider = new JSlider(0, 99, (int)(99 * view.getTransparencySurfaceValue()));
		light1IntesSlider = new JSlider(0, 100, (int)(100.0 * view.getLight1intensity() / 8.0));
		light2IntesSlider = new JSlider(0, 100, (int)(100.0 * view.getLight2intensity() / 8.0));
		meshWidthSlider = new JSlider(0, 100, (int)(100.0 * view.getMeshWidth() / 0.1));
		vertexWidthSlider = new JSlider(0, 100, (int)(100.0*view.getVertexSize() / 0.1));
		helperLinesWidthSlider = new JSlider(0, 100, (int)(100.0*view.getHelperLineWidth() / 0.1));
		diskThicknessSlider = new JSlider(0, 100, (int)(100.0*view.getDiskThickness() / 0.3));
		circleTypeCombo = new JComboBox(CircleType.values());
		circleTypeCombo.setSelectedItem(view.getCircleType());
		updateRingsBtn.setEnabled(view.getCircleType() == EqualRadiusRing);
		
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
		viewOptPanel.add(new JLabel("Background Color"), c1);
		c1.gridwidth = REMAINDER;
		c1.weightx = 0;
		viewOptPanel.add(backgroundColorBtn, c1);
		
		c1.weightx = 0;
		c1.gridwidth = 1;
		viewOptPanel.add(light1Checker, c1);
		c1.weightx = 1;
		viewOptPanel.add(light1IntesSlider, c1);
		c1.gridwidth = REMAINDER;
		c1.weightx = 0;
		viewOptPanel.add(light1ColorBtn, c1);
		
		c1.gridwidth = 1;
		c1.weightx = 0;
		viewOptPanel.add(light2Checker, c1);
		c1.weightx = 1;
		viewOptPanel.add(light2IntesSlider, c1);
		c1.gridwidth = GridBagConstraints.REMAINDER;
		c1.weightx = 0;
		viewOptPanel.add(light2ColorBtn, c1);

		c1.gridwidth = 1;
		c1.weightx = 0;
		viewOptPanel.add(new JLabel("Faces"), c1);
		c1.weightx = 1;
		c1.gridwidth = GridBagConstraints.REMAINDER;
		viewOptPanel.add(polyederTransSlider, c1);
		
		viewOptPanel.add(shadingChecker, c1);
		
		// geometry options
//		geomOptPanel.setBorder(BorderFactory.createTitledBorder("Geometry Options"));
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
		
		c2.weightx = 0;
		c2.gridwidth = 1;
		geomOptPanel.add(showMeshChecker, c2);
		c2.weightx = 1;
		geomOptPanel.add(meshWidthSlider, c2);
		c2.gridwidth = GridBagConstraints.REMAINDER;
		c2.weightx = 0;
		geomOptPanel.add(meshColorBtn, c2);
		
		c2.gridwidth = 1;
		c2.weightx = 0;
		geomOptPanel.add(showVerticesChecker, c2);
		c2.weightx = 1;
		c2.gridwidth = GridBagConstraints.REMAINDER;
		geomOptPanel.add(vertexWidthSlider, c2);
		
		c2.weightx = 1;
		c2.gridwidth = 2;
		geomOptPanel.add(spheresChecker, c2);
		c2.gridwidth = GridBagConstraints.REMAINDER;
		c2.weightx = 0;
		geomOptPanel.add(spheresColorBtn, c2);
		
		c2.gridwidth = 1;
		c2.weightx = 0;
		geomOptPanel.add(circlesChecker, c2);
		c2.weightx = 1;
		geomOptPanel.add(diskThicknessSlider, c2);		
		c2.gridwidth = GridBagConstraints.REMAINDER;
		c2.weightx = 0;
		geomOptPanel.add(circlesColorBtn, c2);
		
		JPanel circleTypePanel = new JPanel();
		circleTypePanel.setLayout(new GridLayout(1,3));
		circleTypePanel.add(new JLabel("Circle Style:"));
		circleTypePanel.add(circleTypeCombo);
		circleTypePanel.add(updateRingsBtn);
		c2.weightx = 1.0;
		geomOptPanel.add(circleTypePanel, c2);
		
		c2.weightx = 0;
		c2.gridwidth = 1;
		geomOptPanel.add(helperLinesChecker, c2);
		c2.weightx = 1;
		geomOptPanel.add(helperLinesWidthSlider, c2);
		c2.gridwidth = GridBagConstraints.REMAINDER;
		c2.weightx = 0;
		geomOptPanel.add(helperLinesColorBtn, c2);
		geomOptPanel.add(showUnitSphereChecker, c2);
		
		//main layout
		shrinkPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1;
		c.weighty = 1;
//		c.gridwidth = GridBagConstraints.RELATIVE;
//		shrinkPanel.add(viewOptPanel, c);
		c.gridwidth = GridBagConstraints.REMAINDER;
		shrinkPanel.add(geomOptPanel, c);
		
		antialiasChecker.addActionListener(this);
		showPolyeder.addActionListener(this);
		showUnitSphereChecker.addActionListener(this);
		showMeshChecker.addActionListener(this);
		circlesChecker.addActionListener(this);
		spheresChecker.addActionListener(this);
		light1Checker.addActionListener(this);
		light2Checker.addActionListener(this);
		shadingChecker.addActionListener(this);
		showVerticesChecker.addActionListener(this);
		helperLinesChecker.addActionListener(this);
		updateRingsBtn.addActionListener(this);
		
		polyederColorBtn.addColorChangedListener(this);
		light1ColorBtn.addColorChangedListener(this);
		light2ColorBtn.addColorChangedListener(this);
		circlesColorBtn.addColorChangedListener(this);
		spheresColorBtn.addColorChangedListener(this);
		meshColorBtn.addColorChangedListener(this);
		backgroundColorBtn.addColorChangedListener(this);
		helperLinesColorBtn.addColorChangedListener(this);
		
		polyederTransSlider.addChangeListener(this);
		light1IntesSlider.addChangeListener(this);
		light2IntesSlider.addChangeListener(this);
		meshWidthSlider.addChangeListener(this);
		vertexWidthSlider.addChangeListener(this);
		helperLinesWidthSlider.addChangeListener(this);
		diskThicknessSlider.addChangeListener(this);
		
		circleTypeCombo.addActionListener(this);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object s = e.getSource();
		if (antialiasChecker == s)
			view.setAntialias(antialiasChecker.isSelected());
		if (showPolyeder == s)
			view.setShowSurface(showPolyeder.isSelected());
		if (showUnitSphereChecker == s) 
			view.setShowUnitSphere(showUnitSphereChecker.isSelected());
		if (showMeshChecker == s)
			view.setShowMesh(showMeshChecker.isSelected());
		if (light1Checker == s)
			view.setLight1On(light1Checker.isSelected());
		if (light2Checker == s)
			view.setLight2On(light2Checker.isSelected());
		if (shadingChecker == s)
			view.setSmoothShading(shadingChecker.isSelected());
		if (circlesChecker == s)
			view.setShowCircles(circlesChecker.isSelected());
		if (spheresChecker == s)
			view.setShowSpheres(spheresChecker.isSelected());
		if (showVerticesChecker == s)
			view.setShowVertices(showVerticesChecker.isSelected());
		if (helperLinesChecker == s)
			view.setShowHelperLines(helperLinesChecker.isSelected());
		if (circleTypeCombo == s || updateRingsBtn == s) {
			CircleType type = CircleType.values()[circleTypeCombo.getSelectedIndex()];
			view.setCircleType(type);
			updateRingsBtn.setEnabled(type == EqualRadiusRing);
		}
//		view.updateProperties();
	}

	
	@Override
	public void colorChanged(ColorChangedEvent cce) {
		Object s = cce.getSource();
		if (polyederColorBtn == s)
			view.setFaceColor(cce.getColor());
		if (light1ColorBtn == s)
			view.setLight1Color(cce.getColor());
		if (light2ColorBtn == s)
			view.setLight2Color(cce.getColor());
		if (meshColorBtn == s)
			view.setMeshColor(cce.getColor());
		if (backgroundColorBtn == s)
			view.setBackgroundColor(cce.getColor());
		if (circlesColorBtn == s)
			view.setCirclesColor(cce.getColor());
		if (spheresColorBtn == s)
			view.setSpheresColor(cce.getColor());
		if (helperLinesColorBtn == s)
			view.setHelperLineColor(cce.getColor());
//		view.updateProperties();
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		if (e.getSource() == polyederTransSlider)
			view.setTransparencySpheresValue(polyederTransSlider.getValue() / 99.0);
		if (e.getSource() == light1IntesSlider)
			view.setLight1intensity(light1IntesSlider.getValue() * 8.0 / 100.0);
		if (e.getSource() == light2IntesSlider)
			view.setLight2intensity(light2IntesSlider.getValue() * 8.0 / 100.0);
		if (e.getSource() == meshWidthSlider)
			view.setMeshWidth(meshWidthSlider.getValue() * 0.1 / 100.0);
		if (e.getSource() == vertexWidthSlider)
			view.setVertexSize(vertexWidthSlider.getValue() * 0.1 / 100.0);
		if (e.getSource() == helperLinesWidthSlider)
			view.setHelperLineWidth(helperLinesWidthSlider.getValue() * 0.1 / 100.0);
		if (e.getSource() == diskThicknessSlider)
			view.setDiskThickness(diskThicknessSlider.getValue() * 0.3 / 100.0);
//		view.updateProperties();
	}
	
	@Override
	public Class<? extends SideContainerPerspective> getPerspectivePluginClass() {
		return View.class;
	}
	
}
