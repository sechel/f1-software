package minimalsurface.frontend.action;

import halfedge.HalfEdgeDataStructure;
import halfedge.frontend.action.ExtensionFileFilter;
import halfedge.surfaceutilities.Converter;
import halfedge.surfaceutilities.Converter.PositionConverter;
import image.ImageHook;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import minimalsurface.frontend.content.MinimalSurfacePanel;
import circlepatterns.graph.CPEdge;
import circlepatterns.graph.CPFace;
import circlepatterns.graph.CPVertex;
import de.jreality.reader.ReaderOBJ;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;

/**
 * Opens a file chooser and saves active polyhedron.
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 */
public class ImportOBJAction extends AbstractAction {

	private static final long 
		serialVersionUID = 1L;
	private JFileChooser 
		openChooser = new JFileChooser();
	private Component 
		parent = null;
	private MinimalSurfacePanel
		viewer = null;


	public ImportOBJAction(Component parent, MinimalSurfacePanel view) {
		this.viewer = view;
		this.parent = parent;
		putValue(Action.NAME, "OBJ...");
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke('O',
				InputEvent.CTRL_MASK));
		putValue(Action.LONG_DESCRIPTION, "Import OBJ File");
		putValue(Action.SHORT_DESCRIPTION, "Import OBJ...");
		putValue(Action.SMALL_ICON, new ImageIcon(ImageHook
				.getImage("open.png")));
		putValue(Action.MNEMONIC_KEY, KeyEvent.VK_O);

		openChooser.setCurrentDirectory(new File(System.getProperty("user.dir") + "/data"));
		openChooser.setDialogTitle("Import OBJ File");
		openChooser.setDialogType(JFileChooser.OPEN_DIALOG);
		openChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		ExtensionFileFilter objFilter = new ExtensionFileFilter("obj",
				"OBJ Files");
		openChooser.addChoosableFileFilter(objFilter);
	}

	
	private class PosConverter implements PositionConverter<CPVertex>{

		@Override
		public double[] getPosition(CPVertex v) {
			return new double[]{v.getXYZW().x, v.getXYZW().y, v.getXYZW().z, v.getXYZW().w};
		}

		@Override
		public void setPosition(CPVertex v, double[] pos) {
			v.getXYZW().x = pos[0];
			v.getXYZW().y = pos[1];
			v.getXYZW().z = pos[2];
			v.getXYZW().w = pos[3];
		}
		
	}
	
	
	@Override
	public void actionPerformed(ActionEvent e) {
		int result = openChooser.showOpenDialog(parent);
		if (result == JFileChooser.APPROVE_OPTION) {
			File file = openChooser.getSelectedFile();
			try {
				ReaderOBJ reader = new ReaderOBJ();
				SceneGraphComponent c = reader.read(file);
				IndexedFaceSet ifs = (IndexedFaceSet)c.getChildComponent(0).getGeometry();
				PosConverter converter = new PosConverter();
				HalfEdgeDataStructure<CPVertex, CPEdge, CPFace> heds = Converter.ifs2heds(ifs, CPVertex.class, CPEdge.class, CPFace.class, converter);
				viewer.resetGeometry();
				viewer.addSurface(heds);
			} catch (Exception e1) {
				JOptionPane.showMessageDialog(parent, e1.getMessage());
			}
		}
	}

}
