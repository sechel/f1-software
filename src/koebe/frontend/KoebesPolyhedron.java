package koebe.frontend;

import halfedge.HalfEdgeDataStructure;
import halfedge.frontend.StandardEditor;
import halfedge.frontend.action.CloseProgram;
import halfedge.frontend.action.ExtensionFileFilter;
import halfedge.frontend.action.MainWindowClosing;
import halfedge.frontend.action.OpenGraph;
import halfedge.frontend.action.SaveGraph;
import halfedge.frontend.controller.MainController.StatusChangedListener;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.UIManager;

import koebe.frontend.action.ExportOBJAction;
import koebe.frontend.action.ExportPSAction;
import koebe.frontend.action.ExportRIBAction;
import koebe.frontend.action.ExportSVGAction;
import koebe.frontend.action.ExportU3DAction;
import koebe.frontend.action.ExportVRMLAction;
import koebe.frontend.action.ResetAction;
import koebe.frontend.action.ShowAboutAction;
import koebe.frontend.content.Viewer;
import koebe.frontend.content.jrealityviewer.KoebePolyhedronView;
import koebe.frontend.controller.MainController;
import koebe.frontend.tool.DualizeTool;
import koebe.frontend.tool.EdgeQuadSubdivide;
import koebe.frontend.tool.MedialSubdivide;
import koebe.frontend.tool.ProjectTool;
import koebe.frontend.tool.VertexQuadSubdivide;
import util.debug.DBGTracer;
import alexandrov.frontend.action.SetDebugModeAction;
import circlepatterns.graph.CPEdge;
import circlepatterns.graph.CPFace;
import circlepatterns.graph.CPVertex;
import de.javasoft.plaf.synthetica.SyntheticaLookAndFeel;
import de.varylab.feedback.swing.FeedbackAction;


/**
 * The main class for the Koebe polyhedron editor.
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 */
@SuppressWarnings("serial")
public class KoebesPolyhedron extends JFrame implements StatusChangedListener{

	public static boolean 
		isStandAlone = true;
	private static KoebesPolyhedron
		mainApp = null;
	private MainController
		controller = new MainController();
	private static final String
		appName = "Koebe Polyhedron Editor";
	
	private JMenuBar
		menuBar = new JMenuBar();
	private JMenu
		fileMenu = new JMenu("File"),
		exportMenu = new JMenu("Export"),
		helpMenu = new JMenu("Help");
	private JCheckBoxMenuItem
		debugChecker = new JCheckBoxMenuItem(new SetDebugModeAction());	
	private HalfEdgeDataStructure<CPVertex, CPEdge, CPFace>
		combinatorics = null;
	private StandardEditor<CPVertex, CPEdge, CPFace>
		editPanel = null;
	private Viewer
		koebesPolyederView = null;
	private JLabel
		statusLabel = new JLabel("Ready");
	
	private ExtensionFileFilter
	 	filter = new ExtensionFileFilter("heds", "HalfEdgeDataStructure File");
	private Action
		closeAction = new CloseProgram(),
		feedbackAction = null,
		openFile = null,
		saveFile = null,
		exportVRML = null,
		exportU3D = null,
		exportRIB = null,
		exportPS = null,
		exportSVG = null,
		resetAction = null,
		aboutAction = null,
		exportOBJ = null;
	
	
	public KoebesPolyhedron() {
		setTitle(appName);
		
		controller.setMainPanel((JPanel)getContentPane());
		controller.setMainFrame(this);
		combinatorics = HalfEdgeDataStructure.createHEDS(CPVertex.class, CPEdge.class, CPFace.class);
		editPanel = new StandardEditor<CPVertex, CPEdge, CPFace>(combinatorics, controller);
		koebesPolyederView = new KoebePolyhedronView(controller);
		controller.setKoebeViewer(koebesPolyederView);
		
		createMenuBar();
		createContent();
		
		editPanel.addTool(new MedialSubdivide());
		editPanel.addTool(new EdgeQuadSubdivide());
		editPanel.addTool(new VertexQuadSubdivide());
		editPanel.getController().addStatusChangedListener(this);
		editPanel.getController().setUseFaces(false);
		editPanel.getEditPanel().setDrawGrid(false);
	}
	
	
	private void createMenuBar(){
		openFile = new OpenGraph<CPVertex, CPEdge, CPFace>(editPanel.getEditPanel(), this, controller, filter);
		saveFile = new SaveGraph(this, editPanel.getController(), filter);
		resetAction = new ResetAction(controller);
		feedbackAction = new FeedbackAction(this, "koebe", appName);
		
		fileMenu.add(openFile);
		fileMenu.add(saveFile);
		fileMenu.add(new JSeparator());
		fileMenu.add(resetAction);
		fileMenu.add(new JSeparator());
		fileMenu.add(feedbackAction);
		fileMenu.add(closeAction);
		menuBar.add(fileMenu);
		
		if (koebesPolyederView instanceof koebe.frontend.content.jrealityviewer.KoebePolyhedronView) {
			koebe.frontend.content.jrealityviewer.KoebePolyhedronView jRViewer = (koebe.frontend.content.jrealityviewer.KoebePolyhedronView) koebesPolyederView;
			exportVRML = new ExportVRMLAction(this, jRViewer);
			exportU3D = new ExportU3DAction(this, jRViewer);
			exportRIB = new ExportRIBAction(this, jRViewer.getViewer());
			exportPS = new ExportPSAction(this, jRViewer.getViewer());
			exportSVG = new ExportSVGAction(this, jRViewer.getViewer());
			exportOBJ = new ExportOBJAction(this, jRViewer);
			exportMenu.add(exportVRML);
			exportMenu.add(exportU3D);
			exportMenu.add(exportRIB);
			exportMenu.add(exportOBJ);
			exportMenu.add(new JSeparator());
			exportMenu.add(exportPS);
			exportMenu.add(exportSVG);
			menuBar.add(exportMenu);
			
			editPanel.addTool(new ProjectTool(jRViewer));
			editPanel.addTool(new DualizeTool(jRViewer));
		}
		
		aboutAction = new ShowAboutAction(this);
		helpMenu.add(debugChecker);
		helpMenu.add(aboutAction);
		menuBar.add(helpMenu);
		
		setJMenuBar(menuBar);
	}
	
	private void createContent() {
		setLayout(new BorderLayout());
		
		editPanel.setBorder(BorderFactory.createTitledBorder("Graph Editor"));
		koebesPolyederView.getViewerComponent().setBorder(BorderFactory.createTitledBorder("Koebe Polyhedron View"));
		
		Dimension minimumSize = new Dimension(150, 50);
		editPanel.setMinimumSize(minimumSize);
		koebesPolyederView.getViewerComponent().setMinimumSize(minimumSize);
		
		JSplitPane splitter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, editPanel, koebesPolyederView.getViewerComponent());
		splitter.setOneTouchExpandable(true);
		splitter.setResizeWeight(0.5);
		splitter.setDividerLocation(390);
		splitter.setContinuousLayout(true);
		
		add(splitter, BorderLayout.CENTER);
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
//			JFrame.setDefaultLookAndFeelDecorated(true);
//			JDialog.setDefaultLookAndFeelDecorated(true);
//			UIManager.setLookAndFeel("org.jvnet.substance.skin.SubstanceModerateLookAndFeel");
//			SubstanceLookAndFeel.setCurrentTheme(new SubstanceSteelBlueTheme());
//			SubstanceLookAndFeel.setCurrentButtonShaper(new ClassicButtonShaper());
//			SubstanceLookAndFeel.setCurrentDecorationPainter(new Glass3DDecorationPainter());
//			SubstanceLookAndFeel.setCurrentGradientPainter(new GlassGradientPainter());
//			SubstanceLookAndFeel.setCurrentHighlightPainter(new GlassHighlightPainter());
//			UIManager.setLookAndFeel("com.jgoodies.looks.plastic.PlasticLookAndFeel");
//			UIManager.setLookAndFeel("de.muntjak.tinylookandfeel.TinyLookAndFeel");
			SyntheticaLookAndFeel.setAntiAliasEnabled(true);
			SyntheticaLookAndFeel.setWindowsDecorated(false);
			SyntheticaLookAndFeel.setExtendedFileChooserEnabled(true);
			SyntheticaLookAndFeel.setUseSystemFileIcons(true);
			UIManager.setLookAndFeel("de.javasoft.plaf.synthetica.SyntheticaBlueSteelLookAndFeel");
		} catch (Exception e) {}
	}
	
	
	public static void main(String[] args) {
		DBGTracer.setActive(false);
		mainApp = new KoebesPolyhedron();
		mainApp.setVisible(true);
		if (isStandAlone)
			mainApp.addWindowListener(new MainWindowClosing());
		mainApp.koebesPolyederView.update();
		mainApp.validate();
		mainApp.setSize(800, 500);
	}

	
}