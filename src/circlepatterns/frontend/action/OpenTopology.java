package circlepatterns.frontend.action;

import halfedge.HalfEdgeDataStructure;
import halfedge.frontend.action.ExtensionFileFilter;
import halfedge.io.HEMLReader;
import halfedge.io.HESerializableReader;
import image.ImageHook;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.KeyStroke;

import circlepatterns.frontend.CPTestSuite;
import circlepatterns.graph.CPEdge;
import circlepatterns.graph.CPFace;
import circlepatterns.graph.CPVertex;


/**
 * Opens a file chooser and loads HEML files
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 */
@SuppressWarnings("serial")
public class OpenTopology extends AbstractAction {

	private JFileChooser
		fileChooser = new JFileChooser();
	
	public OpenTopology() {
		putValue(Action.NAME, "Open File...");
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke('O', InputEvent.ALT_MASK));
		putValue(Action.LONG_DESCRIPTION, "Open a topoloy file");
		putValue(Action.SMALL_ICON, new ImageIcon(ImageHook.getImage("open.gif")));
		putValue(Action.MNEMONIC_KEY, KeyEvent.VK_O);
		fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir") + "/data"));
		fileChooser.setDialogTitle("Open Topology File");
		fileChooser.addChoosableFileFilter(new ExtensionFileFilter("heml", "HalfEdge Markup Language Files"));
		fileChooser.addChoosableFileFilter(new ExtensionFileFilter("heds", "HalfEdgeDataStructure Files"));
		fileChooser.addChoosableFileFilter(new ExtensionFileFilter("", "All Files"));
		fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		fileChooser.setMultiSelectionEnabled(false);
		fileChooser.setAcceptAllFileFilterUsed(false);
	}
	
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if (fileChooser.showOpenDialog(CPTestSuite.getMainFrame()) == JFileChooser.CANCEL_OPTION)
			return;
		File input = fileChooser.getSelectedFile();
		HalfEdgeDataStructure<CPVertex, CPEdge, CPFace> topology = null;
		
		if (input.getName().toLowerCase().endsWith("heml")) {
			HEMLReader<CPVertex, CPEdge, CPFace> hemlReader = HEMLReader.createHEMLReader(CPVertex.class, CPEdge.class, CPFace.class);
			try {
				topology = hemlReader.readHEML(input);
			} catch (Exception ex) {
				ex.printStackTrace();
				CPTestSuite.showError(ex.toString());
				return;
			}
		} else if (input.getName().toLowerCase().endsWith("heds")) {
			InputStream in;
			try {
				in = new FileInputStream(input);
				HESerializableReader<CPVertex, CPEdge, CPFace> hedsReader = new HESerializableReader<CPVertex, CPEdge, CPFace>(in);
				topology = hedsReader.readHalfEdgeDataStructure();
				hedsReader.close();
			} catch (Exception e1) {
				CPTestSuite.showError(e1.toString());
				return;
			}
		}
		CPTestSuite.setActiveFile(input);
		CPTestSuite.setTopology(topology);
		CPTestSuite.updateEuclidean();
	}

}
