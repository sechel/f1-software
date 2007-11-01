package halfedge.frontend.controller;

import halfedge.Edge;
import halfedge.Face;
import halfedge.HalfEdgeDataStructure;
import halfedge.Vertex;
import halfedge.decorations.HasXY;

import java.util.LinkedList;

import javax.swing.JFrame;
import javax.swing.JPanel;


/**
 * The main application controller. It has several subcontrollers
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 */
public class MainController <
		V extends Vertex<V, E, F> & HasXY,
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>
	>  {

	private boolean
		useFaces = true;
	private RemoteControl
		calculationRemote = null;
	
	private JPanel
		mainPanel = null;
	private JFrame
		mainFrame = null;
	private HalfEdgeDataStructure<V, E, F>
		editedGraph = null;
	private NodeController
		nodeController = new NodeController();
	private ColorController
		colorController = new ColorController();
	private ToolController<V, E, F>
		toolController = new ToolController<V, E, F>(this);
	private ConstraintController<V, E, F>
		constraintController = new ConstraintController<V, E, F>();
	private AppearanceController
		appearanceController = new AppearanceController();
	private FontController
		fontController = new FontController();
	private LinkedList<GraphChangedListener>
		changeListeners = new LinkedList<GraphChangedListener>();
	private LinkedList<StatusChangedListener>
		statusListeners = new LinkedList<StatusChangedListener>();
	
	
	public interface GraphChangedListener{
		public void graphChanged();
	}
	
	public interface StatusChangedListener{
		public void statusChanged(String msg);
	}
	
	public boolean addGraphChangedListener(GraphChangedListener listener){
		return changeListeners.add(listener);
	}
	
	public boolean removeGraphChangedListener(GraphChangedListener listener){
		return changeListeners.remove(listener);
	}
	
	public void removeAllGraphChangedListener(){
		changeListeners.clear();
	}
	
	
	public boolean addStatusChangedListener(StatusChangedListener listener){
		return statusListeners.add(listener);
	}
	
	public boolean removeStatusChangedListener(StatusChangedListener listener){
		return statusListeners.remove(listener);
	}
	
	public void removeAllStatusChangedListener(){
		statusListeners.clear();
	}
	
	
	public void fireGraphChanged(){
		for (GraphChangedListener l : changeListeners)
			l.graphChanged();
	}
	
	
	public MainController(){
	}
	
	
	public void refreshEditor(){
		mainPanel.repaint();
	}
	
	
	public ConstraintController<V, E, F> getConstraintController() {
		return constraintController;
	}


	public NodeController getNodeController(){
		return nodeController;
	}
	
	
	public ToolController<V, E, F> getToolController() {
		return toolController;
	}
	

	
	public HalfEdgeDataStructure<V, E, F> getEditedGraph() {
		return editedGraph;
	}



	public void setEditedGraph(HalfEdgeDataStructure<V, E, F> editedGraph) {
		this.editedGraph = editedGraph;
	}


	public ColorController getColorController() {
		return colorController;
	}


	public JPanel getMainPanel() {
		return mainPanel;
	}


	public AppearanceController getAppearanceController() {
		return appearanceController;
	}

	
	public void setStatus(String msg){
		for (StatusChangedListener l : statusListeners)
			l.statusChanged(msg);
	}

	public boolean usesFaces() {
		return useFaces;
	}

	public void setUseFaces(boolean useFaces) {
		this.useFaces = useFaces;
	}

	public FontController getFontController() {
		return fontController;
	}

	public void setMainPanel(JPanel mainPanel) {
		this.mainPanel = mainPanel;
	}

	public RemoteControl getCalculationRemote() {
		return calculationRemote;
	}

	public void setCalculationRemote(RemoteControl calculationRemote) {
		this.calculationRemote = calculationRemote;
		refreshEditor();
	}

	public JFrame getMainFrame() {
		return mainFrame;
	}

	public void setMainFrame(JFrame mainFrame) {
		this.mainFrame = mainFrame;
	}
	
}
