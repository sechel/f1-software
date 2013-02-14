package alexandrov.frontend;

import static alexandrov.frontend.content.ViewerMode.VIEWER_MODE_POLYHEDRON;
import halfedge.HalfEdgeDataStructure;
import halfedge.frontend.StandardEditor;
import halfedge.frontend.action.ExtensionFileFilter;
import halfedge.frontend.action.SaveGraph;
import halfedge.frontend.content.StopButton;
import halfedge.frontend.controller.MainController.StatusChangedListener;
import halfedge.frontend.graphtool.GraphTool;

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
import koebe.frontend.action.ExportSVGAction;
import util.debug.DBGTracer;
import alexandrov.frontend.action.CloseProgram;
import alexandrov.frontend.action.ExportCPMLAction;
import alexandrov.frontend.action.ExportOBJAction;
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
import alexandrov.frontend.tool.CalculateFromXMLTool;
import alexandrov.frontend.tool.CalculateTool;
import alexandrov.frontend.tool.EdgeFlipTool;
import alexandrov.frontend.tool.EditEdgeLength;
import alexandrov.frontend.tool.MakeCPMLEditableTool;
import alexandrov.frontend.tool.RandomCheckTool;
import alexandrov.frontend.tool.deform.CircleTriangleDeformTool;
import alexandrov.frontend.tool.deform.TetrahedronWankelDeformTool;
import alexandrov.frontend.tool.deform.WankelDeformTool;
import alexandrov.graph.CPMEdge;
import alexandrov.graph.CPMFace;
import alexandrov.graph.CPMVertex;
import de.jreality.backends.label.LabelUtility;
import de.jreality.ui.viewerapp.SunflowMenu;
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
public class AlexandrovsPolyhedron extends JFrame implements StatusChangedListener, MainProgramCapabilities{

	public static boolean
		isStandAlone = true;
	private static AlexandrovsPolyhedron
		mainApp = null;
	private MainController
		controller = new MainController(this);
	private static final String
		appName = "Alexandrov Polyhedron Editor";
	
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
	private StopButton<CPMVertex, CPMEdge, CPMFace>
		stopButton = new StopButton<CPMVertex, CPMEdge, CPMFace>(controller);
	private JPanel
		statusPanel = new JPanel();
	private JRadioButtonMenuItem 
		cpmlBtn = new JRadioButtonMenuItem(new SetCPMLMode(controller)),
		defaultBtn = new JRadioButtonMenuItem(new SetDefaultMode(controller));
	private JSplitPane 
		splitter = null;
	
	private ExtensionFileFilter
		filter = new ExtensionFileFilter("cpm", "Convex Metric File (binary)");
	private Action
		closeAction = new CloseProgram(),
		feedbackAction = null,
		exportRIB = null,
		exportPS = null,
		exportCPMS = null,
		exportSVG = null,
		exportOBJ = null,
		resetAction = null,
		aboutAction = null,
		saveCPML = null;
	private OpenCPMLAction
		openFile = null;
	private SaveGraph
		saveFile = null;
	
	public AlexandrovsPolyhedron() {
		setTitle(appName);
		controller.setMainPanel((JPanel)getContentPane());
		controller.setMainFrame(this);
		combinatorics = HalfEdgeDataStructure.createHEDS(CPMVertex.class, CPMEdge.class, CPMFace.class);
		
		defaultEditPanel = new StandardEditor<CPMVertex, CPMEdge, CPMFace>(combinatorics, controller);
		defaultEditPanel.getController().getColorController().setHoverColor(Color.YELLOW);
		defaultEditPanel.getController().getColorController().setIndexColor(Color.YELLOW);
		defaultEditPanel.getController().getAppearanceController().setShowVertexIndices(true);
		defaultEditPanel.getEditPanel().setDrawGrid(false);
		
		polytopView = new AlexandrovPolytopView(controller, VIEWER_MODE_POLYHEDRON);
		controller.setPolytopView(polytopView);
		
		defaultEditPanel.getController().addStatusChangedListener(this);
		defaultEditPanel.getController().setUseFaces(false);
		defaultEditPanel.setFileType(filter);
		defaultEditPanel.addTool(new EdgeFlipTool());
		defaultEditPanel.addTool(new EditEdgeLength());
		defaultEditPanel.addToolSepataror();
		defaultEditPanel.addToolSepataror();
		defaultEditPanel.addTool(new CalculateTool(polytopView));
		defaultEditPanel.addTool(new RandomCheckTool(polytopView, this));
		
		cpmlEditPanel = new CPMLEditor(controller);
		cpmlEditPanel.addTool(new MakeCPMLEditableTool());
		cpmlEditPanel.addTool(new CircleTriangleDeformTool());
		cpmlEditPanel.addTool(new WankelDeformTool());
		cpmlEditPanel.addTool(new TetrahedronWankelDeformTool());
		//cpmlEditPanel.addTool(new EggGeneratorTool());
		cpmlEditPanel.addToolSepataror();
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
		feedbackAction = new FeedbackAction(this, "alexandrov", appName);
		
		fileMenu.add(openFile);
//		fileMenu.add(saveFile);
		fileMenu.add(new JSeparator());
		fileMenu.add(resetAction);
		fileMenu.add(new JSeparator());
		fileMenu.add(feedbackAction);
		fileMenu.add(closeAction);
		menuBar.add(fileMenu);
		
		ButtonGroup modeGroup = new ButtonGroup();
		defaultBtn.setSelected(true);
		modeGroup.add(cpmlBtn);
		modeGroup.add(defaultBtn);
		modeMenu.add(cpmlBtn);
		modeMenu.add(defaultBtn);
		menuBar.add(modeMenu);
		
		exportRIB = new ExportRIBAction(this, polytopView.getViewer());
		exportPS = new ExportPSAction(this, polytopView.getViewer());
		exportCPMS = new ExportCPMLAction(this, polytopView);
		exportSVG = new ExportSVGAction(this, polytopView.getViewer());
		exportOBJ = new ExportOBJAction(this, polytopView);
		
		exportMenu.add(exportCPMS);
		exportMenu.add(exportOBJ);
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
		
		statusPanel.setLayout(new BorderLayout());
		statusPanel.add(statusLabel, BorderLayout.CENTER);
		statusPanel.add(stopButton, BorderLayout.EAST);
		add(statusPanel, BorderLayout.SOUTH);
	}
	
	
	public void switchToXMLMode(){
		if (cpmlEditPanel != null){
			cpmlEditPanel.setSize(splitter.getLeftComponent().getSize());
			splitter.setLeftComponent(cpmlEditPanel);
		}
	}
	
	public void switchToGraphMode(){
		if (defaultEditPanel != null){
			defaultEditPanel.setSize(splitter.getLeftComponent().getSize());
			splitter.setLeftComponent(defaultEditPanel);
		}
	}
	
	
	public void statusChanged(String msg) {
		if (msg == null)
			return;
		if (msg.trim().equals(""))
			msg = "Ready";
		statusLabel.setText(msg);
		statusLabel.repaint();
	}
	
	public void addTool(GraphTool<CPMVertex, CPMEdge, CPMFace> tool){
		defaultEditPanel.addTool(tool);
	}
	
	
	static{
		try {
//			JFrame.setDefaultLookAndFeelDecorated(true);
//			JDialog.setDefaultLookAndFeelDecorated(true);
//			UIManager.setLookAndFeel("org.jvnet.substance.skin.SubstanceModerateLookAndFeel");
//			SubstanceLookAndFeel.setCurrentTheme(new SubstanceSepiaTheme());
//			SubstanceLookAndFeel.setCurrentButtonShaper(new ClassicButtonShaper());
//			SubstanceLookAndFeel.setCurrentDecorationPainter(new Glass3DDecorationPainter());
//			SubstanceLookAndFeel.setCurrentGradientPainter(new GlassGradientPainter());
//			SubstanceLookAndFeel.setCurrentHighlightPainter(new GlassHighlightPainter());
//			UIManager.setLookAndFeel("com.jgoodies.looks.plastic.PlasticLookAndFeel");
//			UIManager.setLookAndFeel("de.muntjak.tinylookandfeel.TinyLookAndFeel");
//			SyntheticaLookAndFeel.setAntiAliasEnabled(true);
//			SyntheticaLookAndFeel.setWindowsDecorated(false);
//			SyntheticaLookAndFeel.setExtendedFileChooserEnabled(true);
//			SyntheticaLookAndFeel.setUseSystemFileIcons(true);
//			UIManager.setLookAndFeel("de.javasoft.plaf.synthetica.SyntheticaBlueSteelLookAndFeel");
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			LoggingSystem.getLogger(LabelUtility.class).setLevel(Level.OFF);
		} catch (Exception e) {}
	}
	
	public void updateView(){
		polytopView.update();
	}
	
	
	public static void main(String[] args) {
		DBGTracer.setActive(false);
		mainApp = new AlexandrovsPolyhedron();
		mainApp.setVisible(true);
		if (isStandAlone)
			mainApp.addWindowListener(new MainWindowClosing());
		mainApp.updateView();
		mainApp.validate();
		mainApp.setSize(800, 500);
	}


	public CPMLEditor getCpmlEditPanel() {
		return cpmlEditPanel;
	}
	
}
