package minimalsurface.frontend.surfacetool;

import static de.jreality.scene.pick.PickResult.PICK_TYPE_POINT;
import halfedge.HalfEdgeDataStructure;
import halfedge.surfaceutilities.Ears;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

import minimalsurface.frontend.content.MinimalSurfacePanel;
import circlepatterns.graph.CPEdge;
import circlepatterns.graph.CPFace;
import circlepatterns.graph.CPVertex;
import de.jreality.scene.tool.AbstractTool;
import de.jreality.scene.tool.InputSlot;
import de.jreality.scene.tool.Tool;
import de.jreality.scene.tool.ToolContext;


/**
 * Removes the clicked component from the HEDS
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 */
public class RemoveEarTool extends AbstractTool implements GeometryTool {

	private MinimalSurfacePanel 
		view = null;
	private static InputSlot
		leftButton = InputSlot.getDevice("PrimaryAction");
	private ToolAction
		action = new ToolAction();
	
	public RemoveEarTool(MinimalSurfacePanel view){
		super(leftButton);
		this.view = view;
	}
	
	@Override
	public void activate(ToolContext tc) {
		if (tc.getCurrentPick() == null){
			tc.reject();
			return;
		}
		int index = tc.getCurrentPick().getIndex();
		HalfEdgeDataStructure<CPVertex, CPEdge, CPFace> heds = view.getActiveSurface();
		try {
			if (tc.getCurrentPick().getPickType() == PICK_TYPE_POINT) {
				CPVertex v = heds.getVertex(index);
				if (v.isOnBoundary()) {
					Ears.cutEar(v);
				}
			} else {
				return;
			}
			view.resetGeometry();
			view.addSurface(heds);
		} catch (Exception e) {
			e.printStackTrace();
		}
		view.update();
	}

	
	private class ToolAction extends AbstractAction{

		public ToolAction(){
			putValue(Action.NAME, "Remove Ear");
		}
		
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			view.setGeometryTool(RemoveEarTool.this);
		}
		
	}
	
	
	@Override
	public void perform(ToolContext tc) {
	}

	@Override
	public Action getAction() {
		return action;
	}

	@Override
	public Tool getTool() {
		return this;
	}

}
