package minimalsurface.frontend.surfacetool;

import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;

import minimalsurface.frontend.content.MinimalSurfacePanel;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphNode;
import de.jreality.scene.pick.PickResult;
import de.jreality.scene.tool.AbstractTool;
import de.jreality.scene.tool.InputSlot;
import de.jreality.scene.tool.Tool;
import de.jreality.scene.tool.ToolContext;


/**
 * Mirrors the clicked component about the clicked vertex
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 */
public class RemoveTool extends AbstractTool implements GeometryTool {

	private static InputSlot
		leftButton = InputSlot.getDevice("PrimaryAction");
	private MinimalSurfacePanel
		panel = null;
	private ToolAction
		action = new ToolAction();
	
	public RemoveTool(MinimalSurfacePanel panel){
		super(leftButton);
		this.panel = panel;
	}
	
	public void activate(ToolContext tc) {
		if (tc.getCurrentPick() == null){
			tc.reject();
			return;
		}
		assert tc.getCurrentPick() != null;
		int pickType = tc.getCurrentPick().getPickType();
	if ((pickType == PickResult.PICK_TYPE_LINE || pickType == PickResult.PICK_TYPE_POINT || pickType == PickResult.PICK_TYPE_FACE)){
			List<SceneGraphNode> nodeList = tc.getRootToLocal().toList();
			SceneGraphComponent parentNode = (SceneGraphComponent)nodeList.get(nodeList.size() - 3);
			SceneGraphComponent c = (SceneGraphComponent)nodeList.get(nodeList.size() - 2);
			parentNode.removeChild(c);
		} else
			tc.reject();
	}

	
	public void perform(ToolContext tc) {
		System.out.println("RemoveCircleTool.perform()");
	}

	private class ToolAction extends AbstractAction{

		public ToolAction(){
			putValue(Action.NAME, "Remove");
		}
		
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			panel.setGeometryTool(RemoveTool.this);
		}
		
	}
	
	public Action getAction() {
		return action;
	}


	public Tool getTool() {
		return this;
	}
	
}
