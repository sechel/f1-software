package teamgeist;

import static javax.swing.JFrame.EXIT_ON_CLOSE;
import halfedge.io.HESerializableReader;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;

import javax.swing.JApplet;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import teamgeist.frontend.TeamgeistView;
import teamgeist.frontend.controller.MainController;
import util.debug.DBGTracer;
import alexandrov.graph.CPMEdge;
import alexandrov.graph.CPMFace;
import alexandrov.graph.CPMVertex;
import de.jreality.backends.label.LabelUtility;
import de.jreality.util.LoggingSystem;


public class TeamgeistApplet extends JApplet implements ActionListener{

	private static final long 
		serialVersionUID = 1L;

	private MainController
		controller = new MainController();
	private TeamgeistView
		viewer = null;
	private JCheckBox
		smoothChecker = new JCheckBox("Smooth Shading", false),
		wireFrameChecker = new JCheckBox("Wire Frame", true);
	
	private JPanel
		optionsPanel = new JPanel();
	
	public TeamgeistApplet(){
		this(true);
	}
	
	
	public TeamgeistApplet(boolean software) {
		viewer = new TeamgeistView(controller, software);
	}
	
	
	@Override
	public void init() {
		try {
//			JFrame.setDefaultLookAndFeelDecorated(true);
//			JDialog.setDefaultLookAndFeelDecorated(true);
//			UIManager.setLookAndFeel("org.jvnet.substance.skin.SubstanceOfficeSilver2007LookAndFeel");
//			SubstanceLookAndFeel.setCurrentTheme(new SubstanceSteelBlueTheme());
//			SubstanceLookAndFeel.setCurrentButtonShaper(new ClassicButtonShaper());
//			SubstanceLookAndFeel.setCurrentDecorationPainter(new Glass3DDecorationPainter());
//			SubstanceLookAndFeel.setCurrentGradientPainter(new GlassGradientPainter());
//			SubstanceLookAndFeel.setCurrentHighlightPainter(new GlassHighlightPainter());
//			UIManager.setLookAndFeel("de.muntjak.tinylookandfeel.TinyLookAndFeel");
//			SyntheticaStandardLookAndFeel.setAntiAliasEnabled(true);
//			SyntheticaStandardLookAndFeel.setWindowsDecorated(false);
//			UIManager.setLookAndFeel("de.javasoft.plaf.synthetica.SyntheticaStandardLookAndFeel");
//			LoggingSystem.getLogger(LabelUtility.class).setLevel(Level.OFF);
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {}
		
		super.init();
		setLayout(new BorderLayout());
		add(viewer, BorderLayout.CENTER);
		add(optionsPanel, BorderLayout.SOUTH);
		
		optionsPanel.setLayout(new GridLayout(1, 2));
		optionsPanel.add(smoothChecker);
		optionsPanel.add(wireFrameChecker);
		smoothChecker.addActionListener(this);
		wireFrameChecker.addActionListener(this);
		smoothChecker.setBackground(Color.WHITE);
		wireFrameChecker.setBackground(Color.WHITE);
		
		controller.setViewer(viewer);
		viewer.setWireFrameRender(wireFrameChecker.isSelected());
		viewer.setSmoothShading(smoothChecker.isSelected());
		viewer.getViewerApp().setBackgroundColor(Color.WHITE);
		String filename = "teamgeist06.teamgeist";
		try {
			filename = getParameter("file");
			if (filename == null)
				filename = "teamgeist06.teamgeist";
		} catch (Exception e) {} // No Applet
		InputStream fis = TeamgeistApplet.class.getResourceAsStream("data/" + filename);
		HESerializableReader<CPMVertex, CPMEdge, CPMFace> reader;
		try {
			reader = new HESerializableReader<CPMVertex, CPMEdge, CPMFace>(fis);
			viewer.viewTeamgeist(reader.readHalfEdgeDataStructure());
			viewer.encompass();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		SwingUtilities.updateComponentTreeUI(this);
	}

	public void actionPerformed(ActionEvent e) {
		Object s = e.getSource();
		if (wireFrameChecker == s)
			viewer.setWireFrameRender(wireFrameChecker.isSelected());
		if (smoothChecker == s)
			viewer.setSmoothShading(smoothChecker.isSelected());
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
//			UIManager.setLookAndFeel("de.muntjak.tinylookandfeel.TinyLookAndFeel");
//			SyntheticaLookAndFeel.setAntiAliasEnabled(true);
//			SyntheticaLookAndFeel.setWindowsDecorated(false);
//			UIManager.setLookAndFeel("de.javasoft.plaf.synthetica.SyntheticaStandardLookAndFeel");
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			LoggingSystem.getLogger(LabelUtility.class).setLevel(Level.OFF);
			DBGTracer.setActive(true);
		} catch (Exception e) {}
	}
	
	
	public static void main(String[] args) {
		TeamgeistApplet app = new TeamgeistApplet(false);
		app.init();
		JFrame frame = new JFrame();
		frame.add(app);
		frame.setTitle("Teamgeist Polyhedron");
		frame.setVisible(true);
		frame.setSize(400, 400);
		frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
	}
	
	
	
}
