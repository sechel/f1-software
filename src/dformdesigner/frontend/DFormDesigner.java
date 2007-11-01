package dformdesigner.frontend;

import halfedge.frontend.controller.MainController;
import halfedge.frontend.controller.MainController.StatusChangedListener;

import java.awt.BorderLayout;
import java.util.logging.Level;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import util.debug.DBGTracer;
import alexandrov.frontend.action.MainWindowClosing;
import alexandrov.graph.CPMEdge;
import alexandrov.graph.CPMFace;
import alexandrov.graph.CPMVertex;
import de.javasoft.plaf.synthetica.SyntheticaStandardLookAndFeel;
import de.jreality.backends.label.LabelUtility;
import de.jreality.util.LoggingSystem;
import de.jtem.java2d.SceneComponent;
import de.jtem.java2dx.modelling.GraphicsModeller2DPanel;


/**
 * The main class for the alexandrov polyhedron application
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 */
@SuppressWarnings("serial")
public class DFormDesigner extends JFrame implements StatusChangedListener, ChangeListener{

	private static DFormDesigner
		mainApp = null;
	private MainController<CPMVertex, CPMEdge, CPMFace>
		controller = new MainController<CPMVertex, CPMEdge, CPMFace>();
	private static final String
		appName = "Alexandrov Polytop Editor";
	
	private JLabel
		statusLabel = new JLabel("Ready");
	private JPanel
		statusPanel = new JPanel();
	private JSplitPane 
		splitter = null;
	
	private GraphicsModeller2DPanel
		viewer2d = new GraphicsModeller2DPanel();
	private SceneComponent
		root = viewer2d.getGraphicsModeller2D().getViewer().getRoot();
	
	
	public DFormDesigner() {
		setTitle(appName);
		controller.setMainPanel((JPanel)getContentPane());
		controller.setMainFrame(this);
				
		viewer2d.getGraphicsModeller2D().addChangeListener(this);
		
		splitter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, viewer2d, new JPanel());
		
		createMenuBar();
		createContent();
	}
	
	// doesnt work
	public void stateChanged(ChangeEvent e) {
		System.out.println("DFormDesigner.stateChanged()");
		for (int i = 0; i < root.getChildCount(); i++)
			System.err.println("Child: " + root.getChild(i));
	}
	
	
	private void createMenuBar(){

	}
	
	private void createContent() {
		setLayout(new BorderLayout());
	
		splitter.setOneTouchExpandable(true);
		splitter.setResizeWeight(0.5);
		splitter.setDividerLocation(390);
		splitter.setContinuousLayout(true);
		
		add(splitter, BorderLayout.CENTER);
		
		statusPanel.setLayout(new BorderLayout());
		statusPanel.add(statusLabel, BorderLayout.CENTER);
		add(statusPanel, BorderLayout.SOUTH);
	}
	

	
	
	public void statusChanged(String msg) {
		if (msg == null)
			return;
		if (msg.trim().equals(""))
			msg = "Ready";
		statusLabel.setText(msg);
		statusLabel.repaint();
	}
	
	
	
	static{
		try {
//			UIManager.setLookAndFeel("com.jgoodies.looks.plastic.PlasticLookAndFeel");
//			UIManager.setLookAndFeel("de.muntjak.tinylookandfeel.TinyLookAndFeel");
			SyntheticaStandardLookAndFeel.setAntiAliasEnabled(true);
			SyntheticaStandardLookAndFeel.setWindowsDecorated(false);
			SyntheticaStandardLookAndFeel.setExtendedFileChooserEnabled(true);
			SyntheticaStandardLookAndFeel.setUseSystemFileIcons(true);
			UIManager.setLookAndFeel("de.javasoft.plaf.synthetica.SyntheticaBlueSteelLookAndFeel");
			LoggingSystem.getLogger(LabelUtility.class).setLevel(Level.OFF);
		} catch (Exception e) {}
	}
	
	public void updateView(){

	}
	
	
	public static void main(String[] args) {
		DBGTracer.setActive(false);
		mainApp = new DFormDesigner();
		mainApp.setVisible(true);
		mainApp.addWindowListener(new MainWindowClosing());
		mainApp.updateView();
		mainApp.validate();
		mainApp.setSize(800, 500);
	}

	
}
