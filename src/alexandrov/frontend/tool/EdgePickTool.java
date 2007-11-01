package alexandrov.frontend.tool;

import java.util.HashMap;

import alexandrov.graph.CPMEdge;
import de.jreality.scene.data.DoubleArray;
import de.jreality.scene.pick.PickResult;
import de.jreality.scene.tool.AbstractTool;
import de.jreality.scene.tool.InputSlot;
import de.jreality.scene.tool.ToolContext;


/**
 * The jReality tool for 3D edge length modifying
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 */
public class EdgePickTool extends AbstractTool {

	private HashMap<Integer, CPMEdge>
		edgeIDMap = null;

	private EdgeLengthEditor3D
		edgeLengthEditor = null;
	private CPMEdge
		editedEdge = null;
	private Double 
		pickZ = 0.0;
	
	public EdgePickTool(EdgeLengthEditor3D edgeLengthEditor){
		super(InputSlot.getDevice("PrimaryAction"));
		this.edgeLengthEditor = edgeLengthEditor;
		addCurrentSlot(InputSlot.getDevice("PointerTransformation"), "changed edge lengths");
	}
	
	public void activate(ToolContext tc) {
		if (edgeIDMap == null)
			return;
		if (tc.getCurrentPick().getPickType()==PickResult.PICK_TYPE_LINE){
			DoubleArray pointerMatrix = tc.getTransformationMatrix(InputSlot.getDevice("PointerTransformation"));
			pickZ = pointerMatrix.getValueAt(9);
			int edgeID = tc.getCurrentPick().getIndex();
			editedEdge = edgeIDMap.get(edgeID);
			edgeLengthEditor.setEditedEdge(editedEdge);
		} else {
			edgeLengthEditor.setEditedEdge(null);
			tc.reject();
		}
	}

	public void perform(ToolContext tc) {
		DoubleArray pointerMatrix = tc.getTransformationMatrix(InputSlot.getDevice("PointerTransformation"));
		if (editedEdge != null){
			Double deltaZ = pointerMatrix.getValueAt(9) - pickZ;
			Double newLength = editedEdge.getLength() + deltaZ;
			edgeLengthEditor.setEdgeLength(newLength);
			pickZ = pointerMatrix.getValueAt(9);
		}
	}

	public void deactivate(ToolContext tc) {
		editedEdge = null;
	}


	public void setEdgeIDMap(HashMap<Integer, CPMEdge> edgeIDMap) {
		this.edgeIDMap = edgeIDMap;
	}

}
