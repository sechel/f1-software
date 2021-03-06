package teamgeist;

import java.awt.BorderLayout;

import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JSeparator;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;

import teamgeist.frontend.GraphicsOptions;
import teamgeist.frontend.TeamgeistCreationPanel;
import teamgeist.frontend.TeamgeistView;
import teamgeist.frontend.action.ExportU3DAction;
import teamgeist.frontend.action.OpenGraph;
import teamgeist.frontend.action.SaveGraph;
import teamgeist.frontend.controller.MainController;
import teamgeist.frontend.controller.MainController.StatusChangedListener;
import util.debug.DBGTracer;
import alexandrov.frontend.action.CloseProgram;
import alexandrov.frontend.action.MainWindowClosing;
import alexandrov.frontend.action.SetDebugModeAction;
import circlepatterns.frontend.content.ShrinkPanelContainer;

public class TeamgeistDesigner extends JFrame implements StatusChangedListener{

	private static final long 
		serialVersionUID = 1L;
	private String 
		title = "Teamgeist Designer 0.1 Beta"; 

	private MainController
		controller = new MainController();
	private TeamgeistView
		viewer = new TeamgeistView(controller, false);
	private ShrinkPanelContainer
		sidePanel = new ShrinkPanelContainer(210);
	private JCheckBoxMenuItem
		debugChecker = new JCheckBoxMenuItem(new SetDebugModeAction());

	private Action
		exportU3DAction = new ExportU3DAction(this, controller);
	
	private JMenuBar
		menuBar = new JMenuBar();
	private JMenu
		fileMenu = new JMenu("File"),
		helpMenu = new JMenu("Help");
	
	private JLabel
		statusLabel = new JLabel("Welcome");
	
	public TeamgeistDesigner(){
		setTitle(title);
		makeLayout();
	}
	
	private void makeLayout() {
		setLayout(new BorderLayout(3,3));
		
		controller.setViewer(viewer);
		
		sidePanel.add(new TeamgeistCreationPanel(controller));
		sidePanel.add(new GraphicsOptions(controller));
		add(viewer, BorderLayout.CENTER);
		add(sidePanel, BorderLayout.WEST);
		add(statusLabel, BorderLayout.SOUTH);
		
		viewer.setBorder(new BevelBorder(BevelBorder.LOWERED));
		makeMenuBar();
		controller.addStatusChangedListener(this);
	}
	
	private void makeMenuBar() {
		fileMenu.add(new OpenGraph(this, controller));
		fileMenu.add(new SaveGraph(this, controller));
		fileMenu.add(new JSeparator());
		fileMenu.add(exportU3DAction);
		fileMenu.add(new JSeparator());
		fileMenu.add(new CloseProgram());
		helpMenu.add(debugChecker);
		menuBar.add(fileMenu);
		menuBar.add(helpMenu);
		setJMenuBar(menuBar);
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
//			JFrame.setDefaultLookAndFeelDecorated(true);
//			JDialog.setDefaultLookAndFeelDecorated(true);
//			UIManager.setLookAndFeel("org.jvnet.substance.skin.SubstanceOfficeSilver2007LookAndFeel");
//			SubstanceLookAndFeel.setCurrentTheme(new SubstanceSteelBlueTheme());
//			SubstanceLookAndFeel.setCurrentButtonShaper(new ClassicButtonShaper());
//			SubstanceLookAndFeel.setCurrentDecorationPainter(new Glass3DDecorationPainter());
//			SubstanceLookAndFeel.setCurrentGradientPainter(new GlassGradientPainter());
//			SubstanceLookAndFeel.setCurrentHighlightPainter(new GlassHighlightPainter());
//			UIManager.setLookAndFeel("com.jgoodies.looks.plastic.PlasticLookAndFeel");
//			UIManager.setLookAndFeel("de.muntjak.tinylookandfeel.TinyLookAndFeel");
//			SyntheticaLookAndFeel.setAntiAliasEnabled(true);
//			SyntheticaLookAndFeel.setWindowsDecorated(false);
//			UIManager.setLookAndFeel("de.javasoft.plaf.synthetica.SyntheticaStandardLookAndFeel");
//			LoggingSystem.getLogger(LabelUtility.class).setLevel(Level.OFF);
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			DBGTracer.setActive(false);
		} catch (Exception e) {}
	}
	
	
	public static void main(String[] args) {
		TeamgeistDesigner app = new TeamgeistDesigner();
		app.addWindowListener(new MainWindowClosing());
		app.setVisible(true);
		app.setSize(800, 600);
	}
	
}
