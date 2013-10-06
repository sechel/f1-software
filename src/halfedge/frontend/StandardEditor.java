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
import halfedge.frontend.graphtool.standard.AddEdgeAction;
import halfedge.frontend.graphtool.standard.DeleteNode;
import halfedge.frontend.graphtool.standard.SelectNodeAction;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

import circlepatterns.frontend.content.ShrinkPanel;


/**
 * A editor implementation which is customizable
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 */
@SuppressWarnings("serial")
public class StandardEditor	
	<
		V extends Vertex<V, E, F> & HasXY,
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>
	>  extends JPanel implements StatusChangedListener{

	private JPanel
		optionsContainer = new JPanel();
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
	private OpenGraph<V, E, F>
		openAction = null;
	private SaveGraph
		saveAction = null;
	
	public StandardEditor(HalfEdgeDataStructure<V, E, F> halfedge, MainController<V, E, F> controller){
		this.controller = controller;
//		this.controller.setMainPanel(this);
		this.controller.setEditedGraph(halfedge);
		
		halfEdgePanel = new EditPanel<V, E, F>(controller);
		openAction = new OpenGraph<V, E, F>(halfEdgePanel, this.getParent(), controller, filter);
		saveAction = new SaveGraph(this.getParent(), controller, filter);
		
		makeLayout();
	}
	
	public void setFileType(ExtensionFileFilter fileFilter){
		openAction.setFileFilter(fileFilter);
		saveAction.setFileFilter(fileFilter);
	}
	

	private void makeLayout(){	
		setLayout(new BorderLayout());
		toolbarPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));

		setFileActions(openAction, saveAction);
		toolbarPanel.add(fileToolbar);
		
		editPanel.setLayout(new BorderLayout());
		editPanel.add(halfEdgePanel, BorderLayout.CENTER);
		editPanel.add(toolbarPanel, BorderLayout.NORTH);
		add(editPanel, BorderLayout.CENTER);
		
		addGraphActions();
		makeOptionsPanel();
		
		actionsToolbar.setFloatable(true);
	}

	public void setFileActions(Action openAction, Action saveAction){
		fileToolbar.removeAll();
		if (openAction != null)
			fileToolbar.add(openAction);
		if (saveAction != null)
			fileToolbar.add(saveAction);
	}
	

	public void addTool(GraphTool<V, E, F> action){
		action.setController(controller);
		JToggleButton actionButton = new JToggleButton();
		if (action.getIcon() == null)
			actionButton.setText(action.getName());
		else
			actionButton.setIcon(action.getIcon());
		actionButton.setToolTipText(action.getShortDescription());
		actionButton.addActionListener(new ToolSelectListener(action));
		actionsToolbar.add(actionButton);
		actionModeGroup.add(actionButton);
		controller.getToolController().registerTool(action);
	}
	
	public void addToolSepataror(){
		actionsToolbar.add(new JToolBar.Separator());
	}
	
	private void makeOptionsPanel(){
		optionsContainer.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.anchor = GridBagConstraints.WEST;
		c.gridwidth = GridBagConstraints.RELATIVE;
		c.weightx = 1;
		c.weighty = 1;
		ShrinkPanel optionPanel = halfEdgePanel.getOptionPanel();
		optionsContainer.add(optionPanel, c);
		add(optionsContainer, BorderLayout.SOUTH);
		optionPanel.setShrinked(true);
	}
	
	private void addGraphActions(){
		addTool(new SelectNodeAction<V, E, F>());
		addTool(new AddEdgeAction<V, E, F>());
		addTool(new DeleteNode<V, E, F>());
		actionsToolbar.add(new JToolBar.Separator());
		toolbarPanel.add(actionsToolbar, BorderLayout.WEST);
	}
	
	
	
	@Override
	public void statusChanged(String msg) {
		
	}
	
	
	private class ToolSelectListener implements ActionListener{

		private GraphTool<V, E, F>	
			tool = null;
		
		public ToolSelectListener(GraphTool<V, E, F> tool) {
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

	public EditPanel<V, E, F> getEditPanel(){
		return halfEdgePanel;
	}

	public void setController(MainController<V, E, F> controller) {
		this.controller = controller;
	}
	
}
