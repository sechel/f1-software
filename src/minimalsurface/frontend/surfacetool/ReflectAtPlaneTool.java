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
public class ReflectAtPlaneTool extends AbstractTool implements GeometryTool {

	private MinimalSurfacePanel 
		view = null;
	private static InputSlot
		leftButton = InputSlot.getDevice("PrimaryAction");
	private double[]
	    firstPoint = null,
	    secondPoint = null;
	private ToolAction
		action = new ToolAction();
	
	
	public ReflectAtPlaneTool(MinimalSurfacePanel view){
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
		DoubleArray pointArray = null;
		try {
			pointArray = (DoubleArray)vList.get(index);
		} catch (ArrayIndexOutOfBoundsException e){
			tc.reject();
			return;
		}
		double[] pickPoint = pointArray.toDoubleArray(null);
		
		if (firstPoint == null){
			firstPoint = pickPoint;
			return;
		}
		if (secondPoint == null){
			secondPoint = pickPoint;
			return;
		}

		SceneGraphComponent cMesh = new SceneGraphComponent();
		cMesh.setGeometry(generator.getGeometry());
		
		Matrix A = MatrixBuilder.euclidean(generator.getTransformation()).getMatrix();
		
		Matrix R = MatrixBuilder.euclidean().reflect(firstPoint, secondPoint, pickPoint).getMatrix();
		
		R.multiplyOnLeft(A);
		R.assignTo(cMesh);

		for (int i = 0; i < generator.getChildComponentCount(); i++)
			cMesh.addChild(copy(generator.getChildComponent(i)));				
		
		view.addGeometry(cMesh);
		view.update();
		firstPoint = null;
		secondPoint = null;
	}


	private SceneGraphComponent copy(SceneGraphComponent c){
		SceneGraphComponent copy = (SceneGraphComponent)SceneGraphUtility.copy(c);
		copy.setGeometry(c.getGeometry());
		copy.setTransformation(c.getTransformation());
		copy.setAppearance(c.getAppearance());
		for (int i = 0; i < c.getChildComponentCount(); i++)
			copy.addChild(c.getChildComponent(i));
		return copy;
	}
	
	
	
	public void perform(ToolContext tc) {
	}

	private class ToolAction extends AbstractAction{
		
		public ToolAction(){
			putValue(Action.NAME, "Reflection At Plane");
		}
		
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			view.setGeometryTool(ReflectAtPlaneTool.this);
		}
		
	}
	
	public Action getAction() {
		return action;
	}


	public Tool getTool() {
		return this;
	}

}
