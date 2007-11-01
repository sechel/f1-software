package halfedge.frontend.controller;

import halfedge.Edge;
import halfedge.Face;
import halfedge.Vertex;
import halfedge.decorations.HasXY;
import halfedge.frontend.graphtool.GraphTool;
import halfedge.frontend.graphtool.standard.NoGraphAction;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.LinkedList;

import javax.swing.JLabel;

import util.debug.DBGTracer;
import circlepatterns.frontend.content.ShrinkPanel;


/**
 * This controller manages the tools of the editor
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 */
public class ToolController 
<
	V extends Vertex<V, E, F> & HasXY,
	E extends Edge<V, E, F>, 
	F extends Face<V, E, F>
>  {

	private ShrinkPanel
		toolOptionsShrinker = new ShrinkPanel("Tool options");
	private GraphTool<V, E, F>
		graphTool = new NoGraphAction<V, E, F>();
	private MainController<V, E, F> 
		controller = null;
	private LinkedList<GraphTool<V, E, F>>
		registeredTools = new LinkedList<GraphTool<V,E,F>>();
	
	
	public ToolController(MainController<V, E, F> controller){
		this.controller = controller;
		showToolOptions(graphTool);
	}
	
	public GraphTool<V, E, F> getActiveTool() {
		return graphTool;
	}


	public void setActiveTool(GraphTool<V, E, F> newTool) {
		GraphTool<V, E, F> oldTool = this.graphTool;
		try {
			if (oldTool != null)
				oldTool.leaveTool();
			if (newTool != null){
				if (newTool.initTool())
					this.graphTool = newTool;
				else
					newTool.leaveTool();
			} 
		} catch (Exception e){
			DBGTracer.stackTrace(e);
			controller.setStatus(e.getMessage());
		}
	}

	
	public ShrinkPanel getToolOptionsPanel(){
		return toolOptionsShrinker;
	}
	
	
	public void showToolOptions(GraphTool<V, E, F> tool){
		if (tool.getOptionPanel() != null){
			toolOptionsShrinker.removeAll();
			toolOptionsShrinker.setLayout(new BorderLayout());
			toolOptionsShrinker.add(tool.getOptionPanel(), BorderLayout.CENTER);
			toolOptionsShrinker.updateShrinkPanel();
		} else {
			toolOptionsShrinker.removeAll();
			toolOptionsShrinker.setLayout(new FlowLayout());
			toolOptionsShrinker.add(new JLabel("No Options"));
			toolOptionsShrinker.updateShrinkPanel();
		}
	}

	public void registerTool(GraphTool<V, E, F> tool){
		registeredTools.add(tool);
	}
	
	public boolean unregisterTool(GraphTool<V, E, F> tool){
		return registeredTools.remove(tool);
	}
	
	public LinkedList<GraphTool<V, E, F>> getRegisteredTools() {
		return registeredTools;
	}
	
}
