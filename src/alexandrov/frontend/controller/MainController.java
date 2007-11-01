package alexandrov.frontend.controller;

import halfedge.HalfEdgeDataStructure;
import alexandrov.frontend.content.AlexandrovPolytopView;
import alexandrov.graph.CPMEdge;
import alexandrov.graph.CPMFace;
import alexandrov.graph.CPMVertex;


/**
 * The main controller for the alexandrov polyhedron editor.
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 * @see halfedge.frontend.controller.MainController
 */
public class MainController extends
		halfedge.frontend.controller.MainController<CPMVertex, CPMEdge, CPMFace> {

	private EditorMode
		mode = EditorMode.GraphEditMode;
	private AlexandrovPolytopView
		polytopView = null;
	private MainProgramCapabilities 
		mainProgram = null;
	

	public MainController(MainProgramCapabilities mainProgram) {
		this.mainProgram = mainProgram;
	}

	
	public EditorMode getEditorMode() {
		return mode;
	}


	public void setEditorMode(EditorMode mode) {
		switch (mode){
			case GraphEditMode:
				mainProgram.switchToGraphMode();
				break;
			case XMLEditMode:
				mainProgram.switchToXMLMode();
				break;
		}
		this.mode = mode;
	}

	
	public void setCPMLGraph(String cpml){
		mainProgram.getCpmlEditPanel().setCPML(cpml);
		setEditorMode(EditorMode.XMLEditMode);
	}
	
	
	public void setEditedGraph(HalfEdgeDataStructure<CPMVertex, CPMEdge, CPMFace> editedGraph) {
		super.setEditedGraph(editedGraph);
		setEditorMode(EditorMode.GraphEditMode);
	}
	
	
	@Override
	public HalfEdgeDataStructure<CPMVertex, CPMEdge, CPMFace> getEditedGraph() {
		switch (mode){
			case GraphEditMode:
				return super.getEditedGraph();
			case XMLEditMode:
				return mainProgram.getCpmlEditPanel().getGraph();
		}
		return null;
	}


	public AlexandrovPolytopView getPolytopView() {
		return polytopView;
	}


	public void setPolytopView(
			AlexandrovPolytopView polytopView) {
		this.polytopView = polytopView;
	}


}
