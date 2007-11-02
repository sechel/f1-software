package halfedge.frontend;

import halfedge.HalfEdgeDataStructure;
import halfedge.frontend.action.ExtensionFileFilter;
import halfedge.frontend.action.MainWindowClosing;
import halfedge.frontend.action.OpenGraph;
import halfedge.frontend.action.SaveGraph;
import halfedge.frontend.controller.MainController.StatusChangedListener;
import halfedge.frontend.graphtool.standard.EditTheta;

import java.awt.BorderLayout;

import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.UIManager;

import koebe.frontend.controller.MainController;
import util.debug.DBGTracer;
import circlepatterns.graph.CPEdge;
import circlepatterns.graph.CPFace;
import circlepatterns.graph.CPVertex;
import de.javasoft.plaf.synthetica.SyntheticaStandardLookAndFeel;

/**
 * A test implemetation for an HalfEdge editor
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 */
@SuppressWarnings("serial")
public class EditorTest extends JFrame implements StatusChangedListener{

	private static EditorTest
		mainApp = null;
	private static final String
		appName = "HalfEdge Editor 0.1 Beta";
	
	private JMenuBar
		menuBar = new JMenuBar();
	private JMenu
		fileMenu = new JMenu("File");
	private MainController
		controller = new MainController();
	private FullFeaturedEditor<CPVertex, CPEdge, CPFace>
		editPanel = new FullFeaturedEditor<CPVertex, CPEdge, CPFace>(HalfEdgeDataStructure.createHEDS(CPVertex.class, CPEdge.class, CPFace.class), controller);
	private JLabel
		statusLabel = new JLabel("Ready");
	
	private ExtensionFileFilter
		filter = new ExtensionFileFilter("heds", "HalfEdgeDataStructure File");
	private Action
		closeAction = new halfedge.frontend.action.CloseProgram(),
		openAction = new OpenGraph<CPVertex, CPEdge, CPFace>(null, this, editPanel.getController(), filter),
		saveAction = new SaveGraph(this, editPanel.getController(), filter);
	
	public EditorTest() {
		setTitle(appName);
		setSize(800, 600);
		createMenuBar();
		createContent();
		editPanel.getController().setMainFrame(this);
		
		editPanel.addGraphAction(new EditTheta());
	}
	
	
	private void createMenuBar(){
		fileMenu.add(openAction);
		fileMenu.add(saveAction);
		fileMenu.add(closeAction);
		menuBar.add(fileMenu);
		setJMenuBar(menuBar);
	}
	
	private void createContent() {
		setLayout(new BorderLayout());
		add(editPanel, BorderLayout.CENTER);
		add(statusLabel, BorderLayout.SOUTH);
	}
	
	
	public void statusChanged(String msg) {
		if (msg == null){
			statusLabel.setText("No message");
			return;	
		}
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
			UIManager.setLookAndFeel("de.javasoft.plaf.synthetica.SyntheticaStandardLookAndFeel");
		} catch (Exception e) {}
	}
	
	
	public static void main(String[] args) {
		DBGTracer.setActive(true);
		mainApp = new EditorTest();
		mainApp.setVisible(true);
		mainApp.setExtendedState(JFrame.MAXIMIZED_BOTH);
		mainApp.addWindowListener(new MainWindowClosing());
	}

	
}