package minimalsurface.frontend.content;

import static java.util.logging.Level.CONFIG;
import halfedge.HalfEdgeDataStructure;

import java.awt.EventQueue;

import javax.swing.Action;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenu;

import koebe.frontend.action.ExportSVGAction;
import minimalsurface.controller.MainController;
import minimalsurface.frontend.action.ExportJRSAction;
import minimalsurface.frontend.action.ExportOBJAction;
import minimalsurface.frontend.action.ExportSTLAction;
import minimalsurface.frontend.action.ExportSurfaceOBJAction;
import minimalsurface.frontend.action.ExportU3DAction;
import minimalsurface.frontend.action.ExportU3DPrintAction;
import minimalsurface.frontend.action.ExportVRMLAction;
import minimalsurface.frontend.action.ImportOBJAction;
import circlepatterns.graph.CPEdge;
import circlepatterns.graph.CPFace;
import circlepatterns.graph.CPVertex;
import de.jreality.plugin.JRViewer;
import de.jreality.plugin.basic.Inspector;
import de.jreality.plugin.basic.ViewMenuBar;
import de.jreality.plugin.basic.ViewToolBar;
import de.jreality.plugin.content.ContentTools;
import de.jreality.plugin.menu.CameraMenu;
import de.jreality.plugin.menu.ExportMenu;
import de.jreality.ui.viewerapp.SunflowMenu;
import de.jreality.util.LoggingSystem;
import de.jreality.util.Secure;

public class MinimalSurfaceViewer extends JDialog {

	private static final long 
		serialVersionUID = 1L;
	
	private JRViewer
		viewer = new JRViewer(true);
	private MinimalSurfaceContent
		minimalSurfaceContent = null;
	private MinimalViewOptions
		optionsPanel = null;
	private MinimalSurfaceToolBar
		toolBar = null;
	private JMenu
		exportMenu = new JMenu("Export", false),
		importMenu = new JMenu("Import", false);
	
	public MinimalSurfaceViewer(JFrame parent, MainController controller) {
		super(parent);
		setSize(900, 700);
		setTitle("Minimal Surface Viewer");
		setLocationRelativeTo(parent);
		
		minimalSurfaceContent = new MinimalSurfaceContent(controller);
		toolBar = new MinimalSurfaceToolBar(minimalSurfaceContent);
		optionsPanel = new MinimalViewOptions(controller, minimalSurfaceContent);
		viewer.registerPlugin(toolBar);
		viewer.registerPlugin(minimalSurfaceContent);
		viewer.registerPlugin(optionsPanel);
		viewer.registerPlugin(CameraMenu.class);
		viewer.registerPlugin(ContentTools.class);
		viewer.registerPlugin(ViewToolBar.class);
		viewer.registerPlugin(ViewMenuBar.class); 
		viewer.registerPlugin(Inspector.class);
		viewer.registerPlugin(ExportMenu.class);
		viewer.setShowPanelSlots(false, false, false, true);
		viewer.setShowMenuBar(true);
		viewer.setShowToolBar(true);
		viewer.getController().setPropertyEngineEnabled(false);
		Secure.setProperty("apple.laf.useScreenMenuBar", "false");
		setRootPane(viewer.startupLocal());
		viewer.getPlugin(CameraMenu.class).setZoomEnabled(true);
		viewer.getPlugin(ViewToolBar.class).getToolBarComponent().setVisible(false);
		
		Action exportSVG = new ExportSVGAction(this, viewer.getViewer());
		Action exportU3D = new ExportU3DAction(this, minimalSurfaceContent);
		Action exportU3DPrint = new ExportU3DPrintAction(this, minimalSurfaceContent);
		Action exportSTL = new ExportSTLAction(this, minimalSurfaceContent);
		Action exportOBJ = new ExportOBJAction(this, minimalSurfaceContent);
		Action exportSurfaceOBJ = new ExportSurfaceOBJAction(this, minimalSurfaceContent);
		Action exportJRS = new ExportJRSAction(this, minimalSurfaceContent);
		Action exportVRML = new ExportVRMLAction(this, minimalSurfaceContent);
		Action importOBJ = new ImportOBJAction(this, minimalSurfaceContent);
		
		exportMenu.add(exportU3D);
		exportMenu.add(exportU3DPrint);
		exportMenu.add(exportOBJ);
		exportMenu.add(exportSurfaceOBJ);
		exportMenu.add(exportSVG);
		exportMenu.add(exportSTL);
		exportMenu.add(exportVRML);
		exportMenu.add(exportJRS);
		importMenu.add(importOBJ);
		
	    try {
	    	exportMenu.add(new SunflowMenu(viewer.getViewer()));
	    } catch (Exception e) {
	    	LoggingSystem.getLogger(this).log(CONFIG, "no sunflow", e);
	    }
	    
	    ViewMenuBar menuBar = viewer.getPlugin(ViewMenuBar.class);
	    menuBar.addMenu(getClass(), 1, exportMenu);
	    menuBar.addMenu(getClass(), 1, importMenu);
	}
	
	
	public void view(final HalfEdgeDataStructure<CPVertex, CPEdge, CPFace> surface){
		Runnable r = new Runnable() {
			@Override
			public void run() {
				minimalSurfaceContent.resetGeometry();
				minimalSurfaceContent.addSurface(surface);
				minimalSurfaceContent.encompass();
			}
		};
		EventQueue.invokeLater(r);
	}
	
	
}
