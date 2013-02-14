package alexandrov.frontend.action;

import halfedge.frontend.action.ExtensionFileFilter;
import halfedge.frontend.action.OpenGraph;
import halfedge.frontend.content.EditPanel;

import java.awt.Component;
import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;

import javax.swing.JOptionPane;

import alexandrov.frontend.controller.EditorMode;
import alexandrov.frontend.controller.MainController;
import alexandrov.graph.CPMEdge;
import alexandrov.graph.CPMFace;
import alexandrov.graph.CPMVertex;


/**
 * Opens a file chooser and loads a HalfEdgeDataStructure eather from
 * a CPM binary or from a CPML XML file
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 */
@SuppressWarnings("serial")
public class OpenCPMLAction extends OpenGraph<CPMVertex, CPMEdge, CPMFace> {

	private MainController 
		controller = null;
	
	public OpenCPMLAction(EditPanel<CPMVertex, CPMEdge, CPMFace> editor, Component parent, MainController controller, ExtensionFileFilter filter) {
		super(editor, parent, controller, filter);
		this.controller = controller;
		ExtensionFileFilter cpmlFilter = new ExtensionFileFilter("cpml", "Convex Metric File (xml)");
		openChooser.addChoosableFileFilter(cpmlFilter);
	}

	
	@Override
	public void openFile(File file) {
		if (file.getName().endsWith(".cpml")){
			try {
				LineNumberReader reader = new LineNumberReader(new FileReader(file));
				StringBuffer buf = new StringBuffer();
				String line = "";
				while (null != (line = reader.readLine())){
					buf.append(line + "\n");
				}
				controller.setCPMLGraph(buf.toString());
				reader.close();
			} catch (Exception e1) {
				JOptionPane.showMessageDialog(parent, "Error while loding graph: " + file.getName() + "\n" + e1.getMessage());
			}
			controller.refreshEditor();
			if (editor != null)
				editor.encompass();
		} else {
			super.openFile(file);
			controller.setEditorMode(EditorMode.GraphEditMode);
		}
	}
	
}
