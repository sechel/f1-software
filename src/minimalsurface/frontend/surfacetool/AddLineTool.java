package minimalsurface.frontend.surfacetool;

import static de.jreality.scene.pick.PickResult.PICK_TYPE_POINT;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

import minimalsurface.frontend.content.MinimalSurfacePanel;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.scene.PointSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.DataList;
import de.jreality.scene.data.DoubleArray;
import de.jreality.scene.tool.AbstractTool;
import de.jreality.scene.tool.InputSlot;
import de.jreality.scene.tool.Tool;
import de.jreality.scene.tool.ToolContext;

public class AddLineTool extends AbstractTool implements GeometryTool {

	private MinimalSurfacePanel 
		view = null;
	private static InputSlot
		leftButton = InputSlot.getDevice("PrimaryAction");
	private double[]
	    firstPoint = null;
	private ToolAction
		action = new ToolAction();
	
	
	public AddLineTool(MinimalSurfacePanel view){
		super(leftButton);
		this.view = view;
	}
	
	public void activate(ToolContext tc) {
		if (tc.getCurrentPick() == null || tc.getCurrentPick().getPickType() != PICK_TYPE_POINT){
			tc.reject();
			return;
		}
		SceneGraphComponent generator = tc.getRootToLocal().getLastComponent();
		int index = tc.getCurrentPick().getIndex();
		PointSet ifs = (PointSet)generator.getGeometry();
		DataList vList = ifs.getVertexAttributes(Attribute.COORDINATES);
		DoubleArray pointArray = (DoubleArray)vList.get(index);
		double[] pickPoint = pointArray.toDoubleArray(null);
		Matrix T = MatrixBuilder.euclidean(generator.getTransformation()).getMatrix();
		pickPoint = T.multiplyVector(pickPoint);
		
		if (firstPoint == null){
			firstPoint = pickPoint;
			return;
		}
		view.addLineGeometry(firstPoint, pickPoint);
		view.update();
		firstPoint = null;
	}
	
	
	
	private class ToolAction extends AbstractAction{
	
		public ToolAction(){
			putValue(Action.NAME, "Add Helper Line");
		}
		
		private static final long serialVersionUID = 1L;
	
		public void actionPerformed(ActionEvent e) {
			view.setGeometryTool(AddLineTool.this);
		}
		
	}
	
	public Action getAction() {
		return action;
	}
	
	
	public Tool getTool() {
		return this;
	}


}
