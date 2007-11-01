package alexandrov.frontend.tool;

import alexandrov.frontend.controls.UnfoldControls;
import de.jreality.scene.data.DoubleArray;
import de.jreality.scene.pick.PickResult;
import de.jreality.scene.tool.AbstractTool;
import de.jreality.scene.tool.InputSlot;
import de.jreality.scene.tool.ToolContext;



public class SourcePickTool extends AbstractTool {

	private UnfoldControls
		unfoldControls = null;
	private Double 
		pickZ = 0.0;
	
	public SourcePickTool(UnfoldControls unfoldControl){
		super(InputSlot.getDevice("PrimaryAction"));
		this.unfoldControls = unfoldControl;
		addCurrentSlot(InputSlot.getDevice("PointerTransformation"), "pick source of unfold");
	}
	
	public void activate(ToolContext tc) {

		if (tc.getCurrentPick().getPickType()==PickResult.PICK_TYPE_POINT){

			DoubleArray pointerMatrix = tc.getTransformationMatrix(InputSlot.getDevice("PointerTransformation"));
			pickZ = pointerMatrix.getValueAt(9);
			int vertexID = tc.getCurrentPick().getIndex();
			System.err.println("Picked a vertex:" + vertexID);

			unfoldControls.setSourceVertex(vertexID);
		} else {
			tc.reject();
		}
	}

	public void perform(ToolContext tc) {

	}


}

