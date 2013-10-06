package alexandrov.frontend;

import static alexandrov.frontend.content.ViewerMode.VIEWER_MODE_CAP;
import halfedge.HalfEdgeDataStructure;
import halfedge.frontend.StandardEditor;
import halfedge.frontend.action.ExtensionFileFilter;
import halfedge.frontend.action.SaveGraph;
import halfedge.frontend.controller.MainController.StatusChangedListener;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.logging.Level;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.UIManager;

import koebe.frontend.action.ExportPSAction;
import koebe.frontend.action.ExportRIBAction;
import util.debug.DBGTracer;
import alexandrov.frontend.action.CloseProgram;
import alexandrov.frontend.action.ExportCPMLAction;
import alexandrov.frontend.action.MainWindowClosing;
import alexandrov.frontend.action.OpenCPMLAction;
import alexandrov.frontend.action.ResetAction;
import alexandrov.frontend.action.SaveCPMLAction;
import alexandrov.frontend.action.SetCPMLMode;
import alexandrov.frontend.action.SetDebugModeAction;
import alexandrov.frontend.action.SetDefaultMode;
import alexandrov.frontend.action.ShowAboutAction;
import alexandrov.frontend.content.AlexandrovPolytopView;
import alexandrov.frontend.content.CPMLEditor;
import alexandrov.frontend.controller.MainController;
import alexandrov.frontend.controller.MainProgramCapabilities;
import alexandrov.frontend.tool.CalculateCapTool;
import alexandrov.frontend.tool.CalculateFromXMLTool;
import alexandrov.frontend.tool.EditEdgeLength;
import alexandrov.frontend.tool.SetPlanarLengthsTool;
import alexandrov.graph.CPMEdge;
import alexandrov.graph.CPMFace;
import alexandrov.graph.CPMVertex;
import de.jreality.backends.label.LabelUtility;
import de.jreality.ui.viewerapp.SunflowMenu;
import de.jreality.ui.viewerapp.actions.file.ExportSVG;
import de.jreality.util.LoggingSystem;
import de.varylab.feedback.swing.FeedbackAction;


/**
 * The main class for the alexandrov polyhedron application
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 */
@SuppressWarnings("serial")
public class AlexandrovsCap extends JFrame implements StatusChangedListener, MainProgramCapabilities{

	private static AlexandrovsCap
		mainApp = null;
	private MainController
		controller = new MainController(this);
	private static final String
		appName = "Alexandrov Cap Editor";
	
	private JMenuBar
		menuBar = new JMenuBar();
	private JMenu
		fileMenu = new JMenu("File"),
		modeMenu = new JMenu("Mode"),
		exportMenu = new JMenu("Export"),
		helpMenu = new JMenu("Help");
	private JCheckBoxMenuItem
		debugChecker = new JCheckBoxMenuItem(new SetDebugModeAction());
	private HalfEdgeDataStructure<CPMVertex, CPMEdge, CPMFace>
		combinatorics = null;
	private StandardEditor<CPMVertex, CPMEdge, CPMFace>
		defaultEditPanel = null;
	private CPMLEditor
		cpmlEditPanel = null;
	private AlexandrovPolytopView
		polytopView = null;
	private JLabel
		statusLabel = new JLabel("Ready");
	private JRadioButtonMenuItem 
		cpmlBtn = new JRadioButtonMenuItem(new SetCPMLMode(controller)),
		defaultBtn = new JRadioButtonMenuItem(new SetDefaultMode(controller));
	private JSplitPane 
		splitter = null;
	
	private ExtensionFileFilter
		filter = new ExtensionFileFilter("cpm", "Convex Metric File (binary)");
	private Action
		closeAction = new CloseProgram(),
		exportRIB = null,
		exportPS = null,
		exportSVG = null,
		exportCPMS = null,
		resetAction = null,
		aboutAction = null,
		saveCPML = null;
	private OpenCPMLAction
		openFile = null;
	private SaveGraph
		saveFile = null;
	
	public AlexandrovsCap() {
		setTitle(appName);
		controller.setMainPanel((JPanel)getContentPane());
		controller.setMainFrame(this);
		combinatorics = HalfEdgeDataStructure.createHEDS(CPMVertex.class, CPMEdge.class, CPMFace.class);
		
		defaultEditPanel = new StandardEditor<CPMVertex, CPMEdge, CPMFace>(combinatorics, controller);
		defaultEditPanel.getController().getColorController().setHoverColor(Color.YELLOW);
		defaultEditPanel.getController().getColorController().setIndexColor(Color.YELLOW);
		defaultEditPanel.getController().getAppearanceController().setShowVertexIndices(true);
		defaultEditPanel.getEditPanel().setDrawGrid(false);
		
		polytopView = new AlexandrovPolytopView(controller, VIEWER_MODE_CAP);
		controller.setPolytopView(polytopView);
		
		defaultEditPanel.getController().addStatusChangedListener(this);
		defaultEditPanel.getController().setUseFaces(false);
		defaultEditPanel.setFileType(filter);
		defaultEditPanel.addTool(new EditEdgeLength());
		defaultEditPanel.addTool(new SetPlanarLengthsTool());
		defaultEditPanel.addTool(new CalculateCapTool(polytopView));
		
		cpmlEditPanel = new CPMLEditor(controller);
		cpmlEditPanel.addTool(new CalculateFromXMLTool(polytopView));
		
		splitter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, defaultEditPanel, polytopView.getViewerComponent());
		
		createMenuBar();
		createContent();
	}
	
	
	private void createMenuBar(){
		openFile = new OpenCPMLAction(defaultEditPanel.getEditPanel(), this, controller, filter);
		saveFile = new SaveGraph(this, controller, filter);
		resetAction = new ResetAction(controller);
		saveCPML = new SaveCPMLAction(this, cpmlEditPanel);
		
		fileMenu.add(openFile);
//		fileMenu.add(saveFile);
		fileMenu.add(new JSeparator());
		fileMenu.add(resetAction);
		fileMenu.add(new JSeparator());
		fileMenu.add(new FeedbackAction(this, "alexandrovCap", appName));
		fileMenu.add(closeAction);
		menuBar.add(fileMenu);
		
		ButtonGroup modeGroup = new ButtonGroup();
		defaultBtn.setSelected(true);
		modeGroup.add(cpmlBtn);
		modeGroup.add(defaultBtn);
		modeMenu.add(cpmlBtn);
		modeMenu.add(defaultBtn);
//		menuBar.add(modeMenu);
		
		exportRIB = new ExportRIBAction(this, polytopView.getViewer());
		exportPS = new ExportPSAction(this, polytopView.getViewer());
		exportSVG = new ExportSVG("Export SVG", polytopView.getViewer(), null);
		exportCPMS = new ExportCPMLAction(this, polytopView);
		exportMenu.add(exportCPMS);
		exportMenu.add(new JPopupMenu.Separator());
		exportMenu.add(exportRIB);
		exportMenu.add(exportPS);
		exportMenu.add(exportSVG);
	    try {
	    	exportMenu.add(new SunflowMenu(polytopView.getViewerApp()));
	    } catch (Exception e) {
	    	e.printStackTrace();
	    	LoggingSystem.getLogger(this).log(Level.CONFIG, "no sunflow", e);
	    }
		menuBar.add(exportMenu);
		
		aboutAction = new ShowAboutAction(this);
		helpMenu.add(aboutAction);
		helpMenu.add(debugChecker);
		menuBar.add(helpMenu);
		
		setJMenuBar(menuBar);
		
		defaultEditPanel.setFileActions(openFile, saveFile);
		cpmlEditPanel.setFileActions(openFile, saveCPML);
	}
	
	private void createContent() {
		setLayout(new BorderLayout());
		
		Dimension minimumSize = new Dimension(150, 50);
		cpmlEditPanel.setMinimumSize(minimumSize);
		defaultEditPanel.setMinimumSize(minimumSize);
		polytopView.getViewerComponent().setMinimumSize(minimumSize);
		
		cpmlEditPanel.setBorder(BorderFactory.createTitledBorder("CPML Editor"));
		defaultEditPanel.setBorder(BorderFactory.createTitledBorder("Graph Editor"));
		polytopView.getViewerComponent().setBorder(BorderFactory.createTitledBorder("Polytop View"));
		splitter.setOneTouchExpandable(true);
		splitter.setResizeWeight(0.5);
		splitter.setDividerLocation(390);
		splitter.setContinuousLayout(true);
		
		add(splitter, BorderLayout.CENTER);
		add(statusLabel, BorderLayout.SOUTH);
	}
	
	
	@Override
	public void switchToXMLMode(){
		if (cpmlEditPanel != null){
			cpmlEditPanel.setSize(splitter.getLeftComponent().getSize());
			splitter.setLeftComponent(cpmlEditPanel);
		}
	}
	
	@Override
	public void switchToGraphMode(){
		if (defaultEditPanel != null){
			defaultEditPanel.setSize(splitter.getLeftComponent().getSize());
			splitter.setLeftComponent(defaultEditPanel);
		}
	}
	
	
	@Override
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
			LoggingSystem.getLogger(LabelUtility.class).setLevel(Level.OFF);
//			UIManager.setLookAndFeel("com.jgoodies.looks.plastic.PlasticLookAndFeel");
//			UIManager.setLookAndFeel("de.muntjak.tinylookandfeel.TinyLookAndFeel");
//			SyntheticaLookAndFeel.setAntiAliasEnabled(true);
//			SyntheticaLookAndFeel.setWindowsDecorated(false);
//			UIManager.setLookAndFeel("de.javasoft.plaf.synthetica.SyntheticaStandardLookAndFeel");
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {}
	}
	
	
	public static void main(String[] args) {
		DBGTracer.setActive(false);
		mainApp = new AlexandrovsCap();
		mainApp.setVisible(true);
		mainApp.addWindowListener(new MainWindowClosing());
		mainApp.polytopView.update();
		mainApp.validate();
		mainApp.setSize(800, 500);
	}


	@Override
	public CPMLEditor getCpmlEditPanel() {
		return cpmlEditPanel;
	}
	
}
