package halfedge.frontend.graphtool;

import halfedge.frontend.graphtool.standard.AddEdgeAction;
import halfedge.frontend.graphtool.standard.AddFaceAction;
import halfedge.frontend.graphtool.standard.AddVertexAction;
import halfedge.frontend.graphtool.standard.DeleteNode;
import halfedge.frontend.graphtool.standard.FillHoles;
import halfedge.frontend.graphtool.standard.SelectNodeAction;

import java.util.Collection;
import java.util.LinkedList;

import koebe.frontend.tool.CutEars;


/**
 * Loads all known graph tools
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 */
@SuppressWarnings("unchecked")
public class GraphToolPluginLoader 
{

	private static LinkedList<GraphTool>
		graphActions = null;
	
	
	static{
		graphActions = new LinkedList<GraphTool>();
		
		graphActions.add(new SelectNodeAction());
		graphActions.add(new AddEdgeAction());
		graphActions.add(new AddVertexAction());
		graphActions.add(new AddFaceAction());
		graphActions.add(new FillHoles());
		graphActions.add(new DeleteNode());
//		graphActions.add(new GenerateMedialGraph());
//		graphActions.add(new EdgeQuadSubdivide());
//		graphActions.add(new VertexQuadSubdivide());
		graphActions.add(new CutEars());
//		graphActions.add(new CutAtEdge());
	}
	
	
	public static Collection<GraphTool> loadGraphActions(){
		return graphActions;
	}
	
}
