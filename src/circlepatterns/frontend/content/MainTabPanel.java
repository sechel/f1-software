package circlepatterns.frontend.content;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import circlepatterns.frontend.content.euclidean.EuclideanCirclePatternView;
import circlepatterns.frontend.content.euclidean.EuclideanViewOptions;
import circlepatterns.frontend.content.spherical.SphericalCirclePatternView;

/**
 * The center panel of the circle pattern test suite application
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 */
public class MainTabPanel extends JTabbedPane{
	
	private static final long 
		serialVersionUID = 1L;

	private EuclideanCirclePatternView
		euclideanCircleView = new EuclideanCirclePatternView();
	private SphericalCirclePatternView
		sphericalCirclePatternView = new SphericalCirclePatternView();
	private EuclideanViewOptions
		circleViewOptionShrinker = new EuclideanViewOptions(euclideanCircleView);
	private JPanel
		euclideanPanel = new JPanel(),
		sphericalPanel = new JPanel();
	
	public MainTabPanel() {
		euclideanPanel.setLayout(new BorderLayout());
		euclideanPanel.add(euclideanCircleView, BorderLayout.CENTER);
		euclideanPanel.add(circleViewOptionShrinker, BorderLayout.SOUTH);
		circleViewOptionShrinker.setShrinked(true);
		addTab("Euclidean", euclideanPanel);
		
		sphericalPanel.setLayout(new BorderLayout());
		sphericalPanel.add(sphericalCirclePatternView.getComponent(), BorderLayout.CENTER);	
		addTab("Spherical", sphericalPanel);
	}
	
	
	public void updateEuclidean() {
		euclideanCircleView.update();
	}
	
	public void updateSpherical() {
		sphericalCirclePatternView.updateSpherical();
	}
	
	public EuclideanCirclePatternView getEuclideanCirclePatternView(){
		return euclideanCircleView;
	}
	
	public SphericalCirclePatternView getSphericalCirclePatternView() {
		return sphericalCirclePatternView;
	}

	
}
