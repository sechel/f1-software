package minimalsurface.frontend.content;

import halfedge.HalfEdgeDataStructure;
import halfedge.frontend.StandardEditor;
import halfedge.frontend.action.CloseProgram;
import halfedge.frontend.action.ExtensionFileFilter;
import halfedge.frontend.action.OpenGraph;
import halfedge.frontend.action.SaveGraph;
import halfedge.frontend.controller.MainController.StatusChangedListener;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JSeparator;

import minimalsurface.controller.MainController;
import minimalsurface.frontend.action.ExportOBJAction;
import minimalsurface.frontend.action.ExportSTLAction;
import minimalsurface.frontend.action.ResetAction;
import minimalsurface.frontend.tool.ClearEditor;
import minimalsurface.frontend.tool.EditCapitalPhi;
import minimalsurface.frontend.tool.EditTheta;
import minimalsurface.frontend.tool.FillHoles;
import minimalsurface.frontend.tool.ShowFaceIndex;
import alexandrov.frontend.action.SetDebugModeAction;
import circlepatterns.graph.CPEdge;
import circlepatterns.graph.CPFace;
import circlepatterns.graph.CPVertex;


/**
 * The main class for the Koebe polyhedron editor.
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 */
@SuppressWarnings("serial")
public class GraphEditor extends JDialog implements StatusChangedListener{

	private MainController
		controller = null;
	private static final String
		appName = "Graph Editor";
	
	private JMenuBar
		menuBar = new JMenuBar();
	private JMenu
		fileMenu = new JMenu("File"),
		exportMenu = new JMenu("Export"),
		helpMenu = new JMenu("Help");
	private HalfEdgeDataStructure<CPVertex, CPEdge, CPFace>
		combinatorics = null;
	private StandardEditor<CPVertex, CPEdge, CPFace>
		editPanel = null;
	private MinimalSurfacePanel
		viewer = null;
	private JLabel
		statusLabel = new JLabel("Ready");
	private JCheckBoxMenuItem
		debugChecker = new JCheckBoxMenuItem(new SetDebugModeAction());
	
	private ExtensionFileFilter
	 	filter = new ExtensionFileFilter("heds", "HalfEdgeDataStructure File");
	private Action
		closeAction = new CloseProgram(),
		openFile = null,
		saveFile = null,
		resetAction = null,
		exportOBJAction = null,
		exportSTLAction = null;
	
	
	public GraphEditor(MainController controller) {
		super(controller.getMainFrame(), false);
		controller.setGraphEditor(this);
		setSize(550, 600);
		setLocationRelativeTo(controller.getMainFrame());
		
		this.controller = controller;
		setTitle(appName);
		
		controller.setMainPanel((JPanel)getContentPane());
		combinatorics = HalfEdgeDataStructure.createHEDS(CPVertex.class, CPEdge.class, CPFace.class);
		editPanel = new StandardEditor<CPVertex, CPEdge, CPFace>(combinatorics, controller);
		viewer = new MinimalSurfacePanel(controller);
		controller.setViewer(viewer);
		
		createMenuBar();
		createContent();
		
		editPanel.addTool(new ClearEditor());
		editPanel.addTool(new FillHoles());
		editPanel.addTool(new EditTheta());
		editPanel.addTool(new EditCapitalPhi());
		editPanel.addTool(new ShowFaceIndex());
		
		editPanel.getController().addStatusChangedListener(this);
		editPanel.getController().setUseFaces(true);
		editPanel.getController().getColorController().setBackgroundColor(Color.WHITE);
		editPanel.getEditPanel().setDrawGrid(false);
	}
	
	
	private void createMenuBar(){
		openFile = new OpenGraph<CPVertex, CPEdge, CPFace>(editPanel.getEditPanel(), this, controller, filter);
		saveFile = new SaveGraph(this, editPanel.getController(), filter);
		resetAction = new ResetAction(controller);
		exportOBJAction = new ExportOBJAction(this, viewer);
		exportSTLAction = new ExportSTLAction(this, viewer);
		
		fileMenu.add(openFile);
		fileMenu.add(saveFile);
		fileMenu.add(new JSeparator());
		fileMenu.add(resetAction);
		fileMenu.add(new JSeparator());
		fileMenu.add(closeAction);
		menuBar.add(fileMenu);

		exportMenu.add(exportOBJAction);
		exportMenu.add(exportSTLAction);
		menuBar.add(exportMenu);
		
//		aboutAction = new ShowAboutAction(this);
		helpMenu.add(debugChecker);
//		helpMenu.add(aboutAction);
		menuBar.add(helpMenu);
		
//		setJMenuBar(menuBar);
	}
	
	private void createContent() {
		setLayout(new BorderLayout());
		
		editPanel.setBorder(BorderFactory.createTitledBorder("Combinatorics"));
		viewer.getViewerComponent().setBorder(BorderFactory.createTitledBorder("Minimal Surface"));
		
		Dimension minimumSize = new Dimension(150, 50);
		editPanel.setMinimumSize(minimumSize);
		viewer.getViewerComponent().setMinimumSize(minimumSize);
		
//		JSplitPane splitter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, editPanel, viewer.getViewerComponent());
//		splitter.setOneTouchExpandable(true);
//		splitter.setResizeWeight(0.5);
//		splitter.setDividerLocation(550);
//		splitter.setContinuousLayout(true);
		
		add(editPanel, BorderLayout.CENTER);
		add(statusLabel, BorderLayout.SOUTH);
	}

	
	@Override
	public void statusChanged(String msg) {
		if (msg == null){
			statusLabel.setText("Error: No message");
			return;	
		}
		if (msg.trim().equals(""))
			msg = "Ready";
		statusLabel.setText(msg);
		statusLabel.repaint();
	}
	

	
}