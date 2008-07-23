package circlepatterns.frontend;

import halfedge.HalfEdgeDataStructure;
import halfedge.frontend.action.ExtensionFileFilter;
import image.ImageHook;

import java.awt.BorderLayout;
import java.io.File;

import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;
import javax.swing.UIManager;

import org.jvnet.substance.SubstanceLookAndFeel;
import org.jvnet.substance.button.ClassicButtonShaper;
import org.jvnet.substance.painter.GlassGradientPainter;
import org.jvnet.substance.painter.decoration.Glass3DDecorationPainter;
import org.jvnet.substance.painter.highlight.GlassHighlightPainter;
import org.jvnet.substance.theme.SubstanceSteelBlueTheme;

import circlepatterns.frontend.action.CloseProgram;
import circlepatterns.frontend.action.ExportGraph;
import circlepatterns.frontend.action.MainWindowClosing;
import circlepatterns.frontend.action.OpenTopology;
import circlepatterns.frontend.content.GeneratorShrinker;
import circlepatterns.frontend.content.MainTabPanel;
import circlepatterns.frontend.content.ShrinkPanelContainer;
import circlepatterns.frontend.content.TopologyInfoShrinker;
import circlepatterns.frontend.content.euclidean.EuclideanComputationShrinker;
import circlepatterns.frontend.content.spherical.SphericalComputationShrinker;
import circlepatterns.graph.CPEdge;
import circlepatterns.graph.CPFace;
import circlepatterns.graph.CPVertex;


/**
 * The main class for the circle pattern test suite
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 */
public class CPTestSuite extends JFrame {

	private static final long 	
		serialVersionUID = 1L;

	private static final String
		applicationName = "Circle Pattern Test Suite 0.1b";
	private static CPTestSuite
		mainApp = null;
	private static HalfEdgeDataStructure<CPVertex, CPEdge, CPFace>
		topology = null;
	private static File
		activeFile = null;
	
	private ImageIcon
		mainIcon = new ImageIcon(ImageHook.getImage("CPtestSuite.png"));
	
	private Action
		closeAction = new CloseProgram(),
		openAction = new OpenTopology(),
		saveAction = new ExportGraph(this, new ExtensionFileFilter("heds", "HEDS File"));
	
	private JMenuBar
		menuBar = new JMenuBar();
	private JMenu
		fileMenu = new JMenu("File");
	private JMenuItem
		closeItem = new JMenuItem(closeAction),
		openItem = new JMenuItem(openAction);
	
	private JToolBar
		mainToolbar = new JToolBar();
	
	private ShrinkPanelContainer
		leftPanel = new ShrinkPanelContainer(200);
	private TopologyInfoShrinker
		topologyInfo = new TopologyInfoShrinker();
	private EuclideanComputationShrinker
		euclideanComputationShrinker = new EuclideanComputationShrinker();
	private SphericalComputationShrinker
		shpComputationShrinker = new SphericalComputationShrinker();
	private MainTabPanel
		mainTabPanel = new MainTabPanel();
	private GeneratorShrinker
		generatorShrinker = new GeneratorShrinker();
	private JLabel
		statusLine = new JLabel();
	
	public CPTestSuite() {
		setTitle(applicationName);
		setIconImage(mainIcon.getImage());
		setSize(800, 600);
		buildMenu();
		buildContent();
	}
	
	private void buildMenu(){
		fileMenu.add(openItem);
		fileMenu.add(saveAction);
		fileMenu.add(closeItem);
		fileMenu.setMnemonic('f');
		menuBar.add(fileMenu);
		setJMenuBar(menuBar);
	}
	
	private void buildContent(){
		setLayout(new BorderLayout());
		leftPanel.add(topologyInfo);
		leftPanel.add(euclideanComputationShrinker);
		euclideanComputationShrinker.setShrinked(true);
		leftPanel.add(shpComputationShrinker);
		leftPanel.add(generatorShrinker);
		add(leftPanel, BorderLayout.WEST);
		add(mainTabPanel, BorderLayout.CENTER);
		add(statusLine, BorderLayout.SOUTH);
		add(mainToolbar, BorderLayout.NORTH);
		
		mainToolbar.setFloatable(false);
		mainToolbar.add(openAction);
	}
	
	
	static{
		try {
			JFrame.setDefaultLookAndFeelDecorated(true);
			JDialog.setDefaultLookAndFeelDecorated(true);
			UIManager.setLookAndFeel("org.jvnet.substance.skin.SubstanceOfficeSilver2007LookAndFeel");
			SubstanceLookAndFeel.setCurrentTheme(new SubstanceSteelBlueTheme());
			SubstanceLookAndFeel.setCurrentButtonShaper(new ClassicButtonShaper());
			SubstanceLookAndFeel.setCurrentDecorationPainter(new Glass3DDecorationPainter());
			SubstanceLookAndFeel.setCurrentGradientPainter(new GlassGradientPainter());
			SubstanceLookAndFeel.setCurrentHighlightPainter(new GlassHighlightPainter());
//			UIManager.setLookAndFeel("com.jgoodies.looks.plastic.PlasticLookAndFeel");
//			SyntheticaStandardLookAndFeel.setAntiAliasEnabled(true);
//			SyntheticaStandardLookAndFeel.setWindowsDecorated(false);
//			UIManager.setLookAndFeel("de.javasoft.plaf.synthetica.SyntheticaStandardLookAndFeel");
		} catch (Exception e) {}
	}
	
	
	public static void main(String[] args) {
		mainApp = new CPTestSuite();
		mainApp.setVisible(true);
		mainApp.setExtendedState(JFrame.MAXIMIZED_BOTH);
		mainApp.addWindowListener(new MainWindowClosing());
		setStatus("Welcome");
	}

	
	public static void setStatus(String message){
		mainApp.statusLine.setText(message);
		mainApp.statusLine.repaint();
	}
	
	public static void showError(String error){
		JOptionPane.showMessageDialog(mainApp, error, "Error", JOptionPane.ERROR_MESSAGE);
	}
	
	
	public static HalfEdgeDataStructure<CPVertex, CPEdge, CPFace> getTopology() {
		return topology;
	}

	public static void setTopology(HalfEdgeDataStructure<CPVertex, CPEdge, CPFace> topology) {
		CPTestSuite.topology = topology;
		mainApp.mainTabPanel.getEuclideanCirclePatternView().setPattern(topology);
	}
	
	public static void updateEuclidean(){
		mainApp.topologyInfo.updateLayout();
		mainApp.mainTabPanel.updateEuclidean();
		if (activeFile != null)
			getMainFrame().setTitle(applicationName + " - " + activeFile.getAbsolutePath());
		else
			getMainFrame().setTitle(applicationName);
		mainApp.mainTabPanel.setSelectedIndex(0);
	}
	
	public static void updateSpherical(){
		mainApp.topologyInfo.updateLayout();
		mainApp.mainTabPanel.updateSpherical();
		if (activeFile != null)
			getMainFrame().setTitle(applicationName + " - " + activeFile.getAbsolutePath());
		else
			getMainFrame().setTitle(applicationName);
		mainApp.mainTabPanel.setSelectedIndex(1);
	}
	
	public static JFrame getMainFrame(){
		return mainApp;
	}

	public static File getActiveFile() {
		return activeFile;
	}

	public static void setActiveFile(File activeFile) {
		CPTestSuite.activeFile = activeFile;
	}
	
}
