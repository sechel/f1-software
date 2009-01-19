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
import de.jreality.util.SceneGraphUtility;


/**
 * Mirrors the clicked component about the clicked vertex
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 */
public class PointReflectionTool extends AbstractTool implements GeometryTool {

	private MinimalSurfacePanel 
		view = null;
	private static InputSlot
		leftButton = InputSlot.getDevice("PrimaryAction");
	private ToolAction
		action = new ToolAction();
	
	public PointReflectionTool(MinimalSurfacePanel view){
		super(leftButton);
		this.view = view;
	}
	
	@Override
	public void activate(ToolContext tc) {
		if (tc.getCurrentPick() == null || tc.getCurrentPick().getPickType() != PICK_TYPE_POINT){
			tc.reject();
			return;
		}
		SceneGraphComponent generator = tc.getRootToLocal().getLastComponent();
		SceneGraphComponent cMesh = new SceneGraphComponent();
		
		cMesh.setGeometry(generator.getGeometry());
		
		int index = tc.getCurrentPick().getIndex();
		PointSet ifs = (PointSet)generator.getGeometry();
		DataList vList = ifs.getVertexAttributes(Attribute.COORDINATES);
		DoubleArray mirrorArray = (DoubleArray)vList.get(index);
		double[] mirror = mirrorArray.toDoubleArray(null);
		
		Matrix R = MatrixBuilder.euclidean(generator.getTransformation()).getMatrix();
		Matrix T = MatrixBuilder.euclidean().translate(mirror).getMatrix();
		Matrix Tinv = T.getInverse();
		Matrix S = MatrixBuilder.euclidean().scale(-1.0).getMatrix();
		S.multiplyOnLeft(T);
		S.multiplyOnRight(Tinv);
		S.multiplyOnLeft(R);
		
		S.assignTo(cMesh);

		for (int i = 0; i < generator.getChildComponentCount(); i++)
			cMesh.addChild(copy(generator.getChildComponent(i)));				
		
		cMesh.setName("Reflected Surface");
		view.addGeometry(cMesh);
		view.update();
	}


	private SceneGraphComponent copy(SceneGraphComponent c){
		SceneGraphComponent copy = SceneGraphUtility.copy(c);
		copy.setGeometry(c.getGeometry());
		copy.setTransformation(c.getTransformation());
		copy.setAppearance(c.getAppearance());
		for (int i = 0; i < c.getChildComponentCount(); i++)
			copy.addChild(c.getChildComponent(i));
		return copy;
	}
	
	
	private class ToolAction extends AbstractAction{

		public ToolAction(){
			putValue(Action.NAME, "Point Reflection");
		}
		
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			view.setGeometryTool(PointReflectionTool.this);
		}
		
	}
	
	
	@Override
	public void perform(ToolContext tc) {
		System.out.println("DomainMirrorTool.perform()");
	}

	public Action getAction() {
		return action;
	}

	public Tool getTool() {
		return this;
	}

}
