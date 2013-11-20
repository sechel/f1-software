package minimalsurface.frontend.content;

import javax.swing.ButtonGroup;
import javax.swing.JToggleButton;

import minimalsurface.frontend.surfacetool.AddLineTool;
import minimalsurface.frontend.surfacetool.PointReflectionTool;
import minimalsurface.frontend.surfacetool.ReflectAtPlaneTool;
import minimalsurface.frontend.surfacetool.RemoveEarTool;
import minimalsurface.frontend.surfacetool.RemoveSurfaceNodeTool;
import minimalsurface.frontend.surfacetool.RemoveTool;
import minimalsurface.frontend.surfacetool.RotateAroundLineTool;
import de.jreality.plugin.basic.View;
import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.aggregators.ToolBarAggregator;
import de.jtem.jrworkspace.plugin.flavor.PerspectiveFlavor;

public class MinimalSurfaceToolBar extends ToolBarAggregator {

	private MinimalSurfaceContent
		minimalSurfaceContent = null;
	
	public MinimalSurfaceToolBar(MinimalSurfaceContent content) {
		this.minimalSurfaceContent = content;
		setFloatable(false);
	}
	
	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
	    JToggleButton actionToggle1 = new JToggleButton(new PointReflectionTool(minimalSurfaceContent).getAction());
	    JToggleButton actionToggle2 = new JToggleButton(new ReflectAtPlaneTool(minimalSurfaceContent).getAction());
	    JToggleButton actionToggle3 = new JToggleButton(new RotateAroundLineTool(minimalSurfaceContent).getAction());
	    JToggleButton actionToggle4 = new JToggleButton(new RemoveTool(minimalSurfaceContent).getAction());
	    JToggleButton actionToggle5 = new JToggleButton(new AddLineTool(minimalSurfaceContent).getAction());
	    JToggleButton actionToggle6 = new JToggleButton(new RemoveSurfaceNodeTool(minimalSurfaceContent).getAction());
	    JToggleButton actionToggle7 = new JToggleButton(new RemoveEarTool(minimalSurfaceContent).getAction());
	    ButtonGroup actionGroup = new ButtonGroup();
	    actionGroup.add(actionToggle1);
	    actionGroup.add(actionToggle2);
	    actionGroup.add(actionToggle3);
	    actionGroup.add(actionToggle4);
	    actionGroup.add(actionToggle5);
	    actionGroup.add(actionToggle6);
	    actionGroup.add(actionToggle7);
	    
	    addTool(getClass(), 1, actionToggle1);
	    addTool(getClass(), 1, actionToggle2);
	    addTool(getClass(), 1, actionToggle3);
	    addTool(getClass(), 1, actionToggle4);
	    addTool(getClass(), 1, actionToggle5);
	    addTool(getClass(), 1, actionToggle6);
	    addTool(getClass(), 1, actionToggle7);
	}
	
	@Override
	public Class<? extends PerspectiveFlavor> getPerspective() {
		return View.class;
	}

}
