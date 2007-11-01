package halfedge.frontend.graphconstraint;

import java.util.Collection;
import java.util.LinkedList;


/**
 * Loads all the known contraint plugins (BETA)
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 */
public class GraphConstraintsPluginLoader {

	private static LinkedList<GraphConstraint<?, ?, ?>>
		graphConstraints = null;
	
	
	static{
		graphConstraints = new LinkedList<GraphConstraint<?, ?, ?>>();
		
	}
	
	
	public static Collection<GraphConstraint<?, ?, ?>> loadGraphConstraints(){
		return graphConstraints;
	}
	
}
