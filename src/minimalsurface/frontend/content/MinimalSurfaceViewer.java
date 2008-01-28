package minimalsurface.frontend.content;

import static java.awt.BorderLayout.CENTER;
import static java.awt.BorderLayout.NORTH;
import static java.util.logging.Level.CONFIG;
import halfedge.HalfEdgeDataStructure;

import java.awt.BorderLayout;

import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

import minimalsurface.controller.MainController;
import minimalsurface.frontend.action.ExportJRSAction;
import minimalsurface.frontend.action.ExportOBJAction;
import minimalsurface.frontend.action.ExportSTLAction;
import minimalsurface.frontend.action.ExportSurfaceOBJAction;
import minimalsurface.frontend.action.ExportU3DAction;
import minimalsurface.frontend.action.ExportU3DPrintAction;
import minimalsurface.frontend.action.ExportVRMLAction;
import minimalsurface.frontend.action.ImportOBJAction;
import minimalsurface.frontend.surfacetool.AddLineTool;
import minimalsurface.frontend.surfacetool.PointReflectionTool;
import minimalsurface.frontend.surfacetool.ReflectAtPlaneTool;
import minimalsurface.frontend.surfacetool.RemoveTool;
import minimalsurface.frontend.surfacetool.RotateAroundLineTool;
import circlepatterns.graph.CPEdge;
import circlepatterns.graph.CPFace;
import circlepatterns.graph.CPVertex;
import de.jreality.ui.viewerapp.SunflowMenu;
import de.jreality.util.LoggingSystem;

public class MinimalSurfaceViewer extends JDialog {

	private static final long 
		serialVersionUID = 1L;
	
	private MinimalSurfacePanel
		minimalSurfacePanel = null;
	private JMenuBar	
		menuBar = new JMenuBar();
	private JMenu
		exportMenu = new JMenu("Export", false),
		importMenu = new JMenu("Import", false);
	private JToolBar
		toolBar = new JToolBar();

	
	public MinimalSurfaceViewer(JFrame parent, MainController controller) {
		super(parent);
		setSize(600, 600);
		setTitle("Minimal Surface Viewer");
		setLocationRelativeTo(controller.getMainFrame());
		
		minimalSurfacePanel = new MinimalSurfacePanel(controller);
//		Action exportSVG = new ExportSVGAction(this, minimalSurfacePanel.getViewerApp().getViewer());
		Action exportU3D = new ExportU3DAction(this, minimalSurfacePanel);
		Action exportU3DPrint = new ExportU3DPrintAction(this, minimalSurfacePanel);
		Action exportSTL = new ExportSTLAction(this, minimalSurfacePanel);
		Action exportOBJ = new ExportOBJAction(this, minimalSurfacePanel);
		Action exportSurfaceOBJ = new ExportSurfaceOBJAction(this, minimalSurfacePanel);
		Action exportJRS = new ExportJRSAction(this, minimalSurfacePanel);
		Action exportVRML = new ExportVRMLAction(this, minimalSurfacePanel);
		
		Action importOBJ = new ImportOBJAction(this, minimalSurfacePanel);
		
		setLayout(new BorderLayout());
		add(minimalSurfacePanel, CENTER);
		
		exportMenu.add(exportU3D);
		exportMenu.add(exportU3DPrint);
		exportMenu.add(exportOBJ);
		exportMenu.add(exportSurfaceOBJ);
//		exportMenu.add(exportSVG);
		exportMenu.add(exportSTL);
		exportMenu.add(exportVRML);
		exportMenu.add(exportJRS);
		
		importMenu.add(importOBJ);
		
	    try {
	    	exportMenu.add(new SunflowMenu(minimalSurfacePanel.getViewerApp()));
	    } catch (Exception e) {
	    	LoggingSystem.getLogger(this).log(CONFIG, "no sunflow", e);
	    }
	    
	    menuBar.add(exportMenu);
	    menuBar.add(importMenu);
	    setJMenuBar(menuBar);
	    
	    JToggleButton actionToggle1 = new JToggleButton(new PointReflectionTool(minimalSurfacePanel).getAction());
	    JToggleButton actionToggle2 = new JToggleButton(new ReflectAtPlaneTool(minimalSurfacePanel).getAction());
	    JToggleButton actionToggle3 = new JToggleButton(new RotateAroundLineTool(minimalSurfacePanel).getAction());
	    JToggleButton actionToggle4 = new JToggleButton(new RemoveTool(minimalSurfacePanel).getAction());
	    JToggleButton actionToggle5 = new JToggleButton(new AddLineTool(minimalSurfacePanel).getAction());
	    ButtonGroup actionGroup = new ButtonGroup();
	    actionGroup.add(actionToggle1);
	    actionGroup.add(actionToggle2);
	    actionGroup.add(actionToggle3);
	    actionGroup.add(actionToggle4);
	    actionGroup.add(actionToggle5);
	    
	    toolBar.add(actionToggle1);
	    toolBar.add(actionToggle2);
	    toolBar.add(actionToggle3);
	    toolBar.add(actionToggle4);
	    toolBar.add(actionToggle5);
	    add(toolBar, NORTH);
	}
	
	
	public void view(HalfEdgeDataStructure<CPVertex, CPEdge, CPFace> surface){
		minimalSurfacePanel.resetGeometry();
		minimalSurfacePanel.addSurface(surface);
		minimalSurfacePanel.repaint();
	}
	
	
}
