package alexandrov.frontend.tool;

import halfedge.HalfEdgeDataStructure;
import halfedge.frontend.action.ExtensionFileFilter;
import halfedge.frontend.action.OpenGraph;
import halfedge.frontend.action.SaveGraph;
import halfedge.frontend.content.EditPanel;
import halfedge.frontend.controller.MainController;
import halfedge.frontend.controller.MainController.StatusChangedListener;
import halfedge.frontend.graphtool.GraphTool;
import halfedge.frontend.graphtool.GraphToolPluginLoader;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

import alexandrov.graph.CPMEdge;
import alexandrov.graph.CPMFace;
import alexandrov.graph.CPMVertex;
import circlepatterns.frontend.content.ShrinkPanelContainer;


/**
 * A full features editor implementation which has all tools and the 
 * options panel at the right.
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 */
@SuppressWarnings("serial")
public class FullFeaturedEditor	extends JPanel implements StatusChangedListener{

	private ShrinkPanelContainer
		optionsContainer = new ShrinkPanelContainer(200);
	private MainController<CPMVertex, CPMEdge, CPMFace>
		controller = null;
	
	private JToolBar
		actionsToolbar = new JToolBar(),
		fileToolbar = new JToolBar();
	private JPanel
		toolbarPanel = new JPanel();
	private EditPanel<CPMVertex, CPMEdge, CPMFace>
		halfEdgePanel = null;
	private ButtonGroup 
		actionModeGroup = new ButtonGroup();
	private JPanel
		editPanel = new JPanel();
	
	private ExtensionFileFilter
		filter = new ExtensionFileFilter("heds", "HalfEdgeDataStructure File");
	private Action
		openAction = null,
		saveAction = null;
	
	public FullFeaturedEditor(HalfEdgeDataStructure<CPMVertex, CPMEdge, CPMFace> halfedge, MainController<CPMVertex, CPMEdge, CPMFace> controller){
		this.controller = controller; 
		halfEdgePanel = new EditPanel<CPMVertex, CPMEdge, CPMFace>(controller);
		controller.setEditedGraph(halfedge);
		controller.setMainPanel(this);
		openAction = new OpenGraph<CPMVertex, CPMEdge, CPMFace>(halfEdgePanel, this, controller, filter);
		saveAction = new SaveGraph(this, controller, filter);
		makeLayout();
	}
	

	private void makeLayout(){	
		setLayout(new BorderLayout());
		toolbarPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));

		fileToolbar.add(openAction);
		fileToolbar.add(saveAction);
		toolbarPanel.add(fileToolbar);
		
		editPanel.setLayout(new BorderLayout());
		editPanel.add(halfEdgePanel, BorderLayout.CENTER);
		editPanel.add(toolbarPanel, BorderLayout.NORTH);
		add(editPanel, BorderLayout.CENTER);
		
		addGraphActions();
		makeOptionsPanel();
	}


	private void addGraphAction(GraphTool<CPMVertex, CPMEdge, CPMFace> action){
		action.setController(controller);
		JToggleButton actionButton = new JToggleButton();
		if (action.getIcon() == null)
			actionButton.setText(action.getName());
		else
			actionButton.setIcon(action.getIcon());
		actionButton.setToolTipText(action.getShortDescription());
		actionButton.addActionListener(new GraphActionSelectListener(action));
		actionsToolbar.add(actionButton);
		actionModeGroup.add(actionButton);
	}
	
	private void makeOptionsPanel(){
		optionsContainer.addShrinkPanel(halfEdgePanel.getOptionPanel());
		optionsContainer.addShrinkPanel(controller.getConstraintController().getConstraintOptionsPanel());
		optionsContainer.addShrinkPanel(controller.getToolController().getToolOptionsPanel());
		optionsContainer.addShrinkPanel(controller.getNodeController().getNodeOptionsPanel());
		add(optionsContainer, BorderLayout.WEST);
	}
	
	private void addGraphActions(){
		for (GraphTool<CPMVertex, CPMEdge, CPMFace> action : GraphToolPluginLoader.loadGraphActions())
			addGraphAction(action);
		toolbarPanel.add(actionsToolbar, BorderLayout.WEST);
	}
	
	
	public void statusChanged(String msg) {
		
	}
	
	
	private class GraphActionSelectListener implements ActionListener{

		private GraphTool<CPMVertex, CPMEdge, CPMFace>	
		tool = null;
		
		public GraphActionSelectListener(GraphTool<CPMVertex, CPMEdge, CPMFace> tool) {
			this.tool = tool;
		}
		
		public void actionPerformed(ActionEvent e) {
			controller.getToolController().setActiveTool(tool);
			controller.getToolController().showToolOptions(tool);
			halfEdgePanel.repaint();
		}
		
	}


	public MainController<CPMVertex, CPMEdge, CPMFace> getController() {
		return controller;
	}


	public EditPanel<CPMVertex, CPMEdge, CPMFace> getEditPanel() {
		return halfEdgePanel;
	}


	
}
