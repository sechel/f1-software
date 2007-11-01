package halfedge.frontend.controller;

import halfedge.Edge;
import halfedge.Face;
import halfedge.Vertex;
import halfedge.decorations.HasXY;
import halfedge.frontend.graphconstraint.GraphConstraint;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JLabel;

import circlepatterns.frontend.content.ShrinkPanel;


/**
 * Defines edit constraints (BETA)
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 */
public class ConstraintController 
<
	V extends Vertex<V, E, F> & HasXY,
	E extends Edge<V, E, F>, 
	F extends Face<V, E, F>
>{

	private LinkedList<GraphConstraint<V, E, F>>
		graphConstraints = new LinkedList<GraphConstraint<V, E, F>>();
	
	
	public void addGraphConstraint(GraphConstraint<V, E, F> c){
		graphConstraints.add(c);
	}

	
	public void removeGraphConstraint(GraphConstraint<V, E, F> c){
		graphConstraints.remove(c);
	}
	
	public void removeAllGraphConstraints(){
		graphConstraints.clear();
	}

	
	public List<GraphConstraint<V, E, F>> getGraphConstraints(){
		return graphConstraints;
	}
	
	
	public ShrinkPanel getConstraintOptionsPanel(){
		return new Options();
	}
	
	
	@SuppressWarnings("serial")
	private class Options extends ShrinkPanel{
		
		public Options() {
			super("Constraints");
			if (graphConstraints.isEmpty()){
				add(new JLabel("No Constraints"));
			} else {
				setLayout(new GridBagLayout());
				GridBagConstraints c = new GridBagConstraints();
				c.fill = GridBagConstraints.HORIZONTAL;
				c.weightx = 1;
				c.anchor = GridBagConstraints.WEST;
				c.gridwidth = GridBagConstraints.REMAINDER;
				for (GraphConstraint<V, E, F> gc : graphConstraints){
					JCheckBox constraintChecker = new JCheckBox(gc.getName());
					add(constraintChecker, c);
				}
			}
		}
		
		
	}
	
	
}
