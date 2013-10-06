package halfedge.frontend;

import halfedge.Edge;
import halfedge.Face;
import halfedge.HalfEdgeDataStructure;
import halfedge.Vertex;
import halfedge.decorations.HasXY;
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
public class FullFeaturedEditor	
	<
		V extends Vertex<V, E, F> & HasXY,
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>
	>  extends JPanel implements StatusChangedListener{

	private ShrinkPanelContainer
		optionsContainer = new ShrinkPanelContainer(200);
	private MainController<V, E, F>
		controller = null;
	
	private JToolBar
		actionsToolbar = new JToolBar(),
		fileToolbar = new JToolBar();
	private JPanel
		toolbarPanel = new JPanel();
	private EditPanel<V, E, F> 
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
	
	public FullFeaturedEditor(HalfEdgeDataStructure<V, E, F> halfedge, MainController<V, E, F> controller){
		this.controller = controller; 
		halfEdgePanel = new EditPanel<V, E, F>(controller);
		controller.setEditedGraph(halfedge);
		controller.setMainPanel(this);
		openAction = new OpenGraph<V, E, F>(halfEdgePanel, this, controller, filter);
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


	protected void addGraphAction(GraphTool<V, E, F> action){
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
		controller.getToolController().registerTool(action);
	}
	
	private void makeOptionsPanel(){
		optionsContainer.addShrinkPanel(halfEdgePanel.getOptionPanel());
		optionsContainer.addShrinkPanel(controller.getConstraintController().getConstraintOptionsPanel());
		optionsContainer.addShrinkPanel(controller.getToolController().getToolOptionsPanel());
		optionsContainer.addShrinkPanel(controller.getNodeController().getNodeOptionsPanel());
		add(optionsContainer, BorderLayout.WEST);
	}
	
	private void addGraphActions(){
		for (GraphTool<V, E, F> action : GraphToolPluginLoader.loadGraphActions())
			addGraphAction(action);
		toolbarPanel.add(actionsToolbar, BorderLayout.WEST);
	}
	
	
	@Override
	public void statusChanged(String msg) {
		
	}
	
	
	private class GraphActionSelectListener implements ActionListener{

		private GraphTool<V, E, F>	
		tool = null;
		
		public GraphActionSelectListener(GraphTool<V, E, F> tool) {
			this.tool = tool;
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			controller.getToolController().setActiveTool(tool);
			controller.getToolController().showToolOptions(tool);
			halfEdgePanel.repaint();
		}
		
	}


	public MainController<V, E, F> getController() {
		return controller;
	}


	public EditPanel<V, E, F> getEditPanel() {
		return halfEdgePanel;
	}


	
}
