package alexandrov.frontend.tool;

import alexandrov.frontend.controls.UnfoldControls;
import de.jreality.scene.data.DoubleArray;
import de.jreality.scene.pick.PickResult;
import de.jreality.scene.tool.AbstractTool;
import de.jreality.scene.tool.InputSlot;
import de.jreality.scene.tool.ToolContext;



public class CutEdgeRemoverPickTool extends AbstractTool {

	private UnfoldControls
		unfoldControls = null;
	private Double 
		pickZ = 0.0;
	
	public CutEdgeRemoverPickTool(UnfoldControls unfoldControl){
		super(InputSlot.getDevice("PrimaryAction"));
		this.unfoldControls = unfoldControl;
		addCurrentSlot(InputSlot.getDevice("PointerTransformation"), "pick edge to remove from cut tree");
	}
	
	public void activate(ToolContext tc) {

		if (tc.getCurrentPick().getPickType()==PickResult.PICK_TYPE_LINE){

			DoubleArray pointerMatrix = tc.getTransformationMatrix(InputSlot.getDevice("PointerTransformation"));
			pickZ = pointerMatrix.getValueAt(9);
			int edgeID = tc.getCurrentPick().getIndex();
			System.err.println("Picked an edge:" + edgeID);

			unfoldControls.removeCutEdge(edgeID);
		} else {
			tc.reject();
		}
	}

	public void perform(ToolContext tc) {

	}


}

