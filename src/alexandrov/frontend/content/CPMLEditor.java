package alexandrov.frontend.content;

import halfedge.HalfEdgeDataStructure;
import halfedge.frontend.graphtool.GraphTool;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

import sigma.util.jsyntax.InputHandler;
import sigma.util.jsyntax.JEditTextArea;
import alexandrov.frontend.controller.MainController;
import alexandrov.graph.CPMEdge;
import alexandrov.graph.CPMFace;
import alexandrov.graph.CPMVertex;
import alexandrov.io.CPMLReader;


/**
 * An XML Editor for CPML code based on jEdit
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 */
@SuppressWarnings("serial")
public class CPMLEditor extends JPanel{

	private JToolBar
		fileToolbar = new JToolBar(),
		actionsToolbar = new JToolBar();
	private JPanel
		toolbarPanel = new JPanel();
	private JEditTextArea
		editor = new JEditTextArea();
	private MainController 
		controller = null;
	private CPMLReader<CPMVertex, CPMEdge, CPMFace>
		reader = CPMLReader.createCPMLReader(CPMVertex.class, CPMEdge.class, CPMFace.class);
	private ButtonGroup 
		actionModeGroup = new ButtonGroup();
	private Font
		font = new Font("System", Font.PLAIN, 11);
	
	private static String 
		defaultGraph = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n" + 
			"<!DOCTYPE convexPolyhedralMetric>\n" + 
			"<cpml description=\"default tetrahedron\">\n" + 
			"	<edgelist>\n" + 
			"		<edge length=\"1.0\"/>\n" + 
			"		<edge length=\"1.0\"/>\n" + 
			"		<edge length=\"1.0\"/>\n" + 
			"		<edge length=\"1.0\"/>\n" + 
			"		<edge length=\"1.0\"/>\n" + 
			"		<edge length=\"1.0\"/>\n" + 
			"	</edgelist>\n" + 
			"	<trianglelist>\n" + 
			"		<triangle a=\"0\" b=\"1\" c=\"4\"/>\n" + 
			"		<triangle a=\"2\" b=\"5\" c=\"1\"/>\n" + 
			"		<triangle a=\"3\" b=\"2\" c=\"0\"/>\n" + 
			"		<triangle a=\"5\" b=\"3\" c=\"4\"/>\n" + 
			"	</trianglelist>\n" + 
			"</cpml>\n";
	
	public static String getDefaultCPML(){
		return defaultGraph;
	}
	
	public CPMLEditor(MainController controller){
		this.controller = controller;
		editor.setTokenMarker(new XMLTokenMarker());
		makeLayout();
		setCPML(defaultGraph);
		
		InputHandler inpHandler = editor.getInputHandler();
		inpHandler.addDefaultKeyBindings();
		inpHandler.addKeyBinding("C+C", new CtrlCListener());
		inpHandler.addKeyBinding("C+X", new CtrlXListener());
		inpHandler.addKeyBinding("C+V", new CtrlVListener());
		inpHandler.addKeyBinding("C+A", new CtrlAListener());
	}
	
	
	private void makeLayout() {
		setLayout(new BorderLayout());
		toolbarPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		toolbarPanel.add(fileToolbar);
		toolbarPanel.add(actionsToolbar);
		
		add(toolbarPanel, BorderLayout.NORTH);
		add(editor, BorderLayout.CENTER);
		
		editor.setFont(font);
	}


	public void setFileActions(Action openAction, Action saveAction){
		fileToolbar.removeAll();
		if (openAction != null)
			fileToolbar.add(openAction);
		if (saveAction != null)
			fileToolbar.add(saveAction);
	}


	@SuppressWarnings("deprecation")
	public HalfEdgeDataStructure<CPMVertex, CPMEdge, CPMFace> getGraph() {
		java.io.StringBufferInputStream in = new java.io.StringBufferInputStream(editor.getText());
		HalfEdgeDataStructure<CPMVertex, CPMEdge, CPMFace> newGraph = null;
		try {
			newGraph = reader.readCPML(in);
		} catch (Exception e1) {
			JOptionPane.showMessageDialog(controller.getMainPanel(), "Error parsing XML code.\n" + e1.getMessage());
			return null;
		}
		return newGraph;
	}
	
	public void addTool(GraphTool<CPMVertex, CPMEdge, CPMFace> action){
		action.setController(controller);
		JToggleButton actionButton = new JToggleButton();
		if (action.getIcon() == null)
			actionButton.setText(action.getName());
		else
			actionButton.setIcon(action.getIcon());
		actionButton.setToolTipText(action.getShortDescription());
		actionButton.addActionListener(new GraphActionSelectListener(action));
		actionsToolbar.add(actionButton);
		actionModeGroup.add(actionButton);
	}

	public void addToolSepataror(){
		actionsToolbar.add(new JToolBar.Separator());
	}
	
	private class GraphActionSelectListener implements ActionListener{

		private GraphTool<CPMVertex, CPMEdge, CPMFace>	
		tool = null;
		
		public GraphActionSelectListener(GraphTool<CPMVertex, CPMEdge, CPMFace> tool) {
			this.tool = tool;
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			controller.getToolController().setActiveTool(tool);
			controller.getToolController().showToolOptions(tool);
		}
		
	}

	public void setCPML(String code){
		editor.setText(code);
		editor.setCaretPosition(0);
	}
	
	public String getCPML(){
		return editor.getText();
	}
	

	private class CtrlCListener implements ActionListener{
        @Override
		public void actionPerformed(ActionEvent e) {
			editor.copy();
        }    
	}
	private class CtrlXListener implements ActionListener{
        @Override
		public void actionPerformed(ActionEvent e) {
			editor.cut();
        }    
	}	
	private class CtrlVListener implements ActionListener{
        @Override
		public void actionPerformed(ActionEvent e) {
			editor.paste();
        }
	}	
	private class CtrlAListener implements ActionListener{
        @Override
		public void actionPerformed(ActionEvent e) {
            editor.setSelectionStart(0);
            editor.setSelectionEnd(editor.getText().length());
        }
	}
	
	
}
