package halfedge.frontend.graphtool;

import halfedge.Edge;
import halfedge.Face;
import halfedge.HalfEdgeDataStructure;
import halfedge.Vertex;
import halfedge.decorations.HasXY;
import halfedge.frontend.content.GraphGraphics;
import halfedge.frontend.controller.MainController;

import javax.swing.Icon;
import javax.swing.JPanel;

/**
 * The graph tool interface.
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 */
public interface GraphTool 
	<
		V extends Vertex<V, E, F> & HasXY,
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>
	>{
	
	/**
	 * Called once when the action is selected from the toolbar
	 * @return true if the tool should remain activated or false is the 
	 * tool does its job in init
	 */
	public Boolean initTool();

	/**
	 * Called when the user selects another action
	 */
	public void leaveTool();
	
	/**
	 * Sets the main controller, the connection to the editor system
	 * @param controller The controller of the current editor
	 */
	public void setController(MainController<V, E, F> controller);
	
	/**
	 * Reacts on a user action
	 * @param operation the operation the user requests
	 * @return true if the action is complete and ready to commit, false if there are more steps to do
	 * @throws EditOperationException If the action could not be completed
	 */
	public boolean processEditOperation(EditOperation operation) throws EditOperationException;
	
	/**
	 * If processEditOperation results in true this method is called
	 * @param graph the graph this operation should be commited to
	 */
	public void commitEdit(HalfEdgeDataStructure<V, E, F> graph);

	/**
	 * Prepares this action for a new edit operation
	 */
	public void resetTool();
	
	/**
	 * The name of this action
	 * @return the name string
	 */
	public String getName();
	
	/**
	 * An user interface icon
	 * @return the icon object or null is there is no icon
	 */
	public Icon getIcon();
	
	/**
	 * A description of this tool
	 * @return the description string 
	 */
	public String getDescription();
	
	/**
	 * A shorter description
	 * @return the description string
	 */
	public String getShortDescription();
	
	/**
	 * This method is called during the edit process and an draw helper objects or other things
	 * @param g
	 */
	public void paint(GraphGraphics g);

	/**
	 * Returns if this GraphAction nedds a repaint for its helper objects or if it changed
	 * the graph so that a full repaint is needed
	 * @return true if a repaint is needed
	 */
	public boolean needsRepaint();
	
	/**
	 * The option panel of this action
	 * @return the option panel or null if this action has no options
	 */
	public JPanel getOptionPanel();
}
