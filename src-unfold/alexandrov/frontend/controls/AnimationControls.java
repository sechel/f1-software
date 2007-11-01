package alexandrov.frontend.controls;

import static java.awt.GridBagConstraints.BOTH;
import halfedge.HalfEdgeDataStructure;
import halfedge.frontend.graphtool.GraphTool;
import halfedge.surfaceutilities.EmbeddedEdge;
import halfedge.triangulationutilities.Delaunay;
import halfedge.triangulationutilities.TriangulationException;
import halfedge.unfoldutilities.StarTreeFinder;
import halfedge.unfoldutilities.Unfolder;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.Statement;
import java.io.File;
import java.util.Collection;
import java.util.Formatter;
import java.util.LinkedList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.commons.collections15.BidiMap;

import alexandrov.frontend.controller.MainController;
import alexandrov.frontend.tool.SourcePickTool;
import alexandrov.frontend.tool.UnfoldTool;
import alexandrov.frontend.viewer.GeodesicsViewer;
import alexandrov.frontend.viewer.StarViewer;
import alexandrov.frontend.viewer.UnfoldViewer;
import alexandrov.frontend.viewer.VoronoiViewer;
import alexandrov.graph.CPMEdge;
import alexandrov.graph.CPMFace;
import alexandrov.graph.CPMVertex;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.Rn;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Transformation;
import de.jreality.scene.Viewer;
import de.jreality.sunflow.RenderOptions;
import de.jreality.sunflow.Sunflow;
import de.jreality.sunflow.SunflowRenderer;
import de.jreality.ui.viewerapp.FileLoaderDialog;
import de.jtem.beans.DimensionPanel;

public class AnimationControls extends JDialog implements ActionListener, ChangeListener{
	

	private boolean rotate = false;
	private boolean unfold = true;
	
	
	private static final long 
		serialVersionUID = 1L;
	private MainController 
		controller = null;
	private JButton
		renderButton = new JButton("Render!"),
		browseButton = new JButton("Browse...");
	private SpinnerNumberModel
		frameModel = new SpinnerNumberModel(360, 1, 20000, 1);
	private JSpinner
		frameSpinner = new JSpinner(frameModel);
	private JCheckBox
		unfoldBox = new JCheckBox("", unfold),
		rotateBox = new JCheckBox("", rotate);
	private JTextField
		filnameTextField = new JTextField("aout"),
		directoryTextField = new JTextField("/homes/geometer/josefsso/store/anim");
	
	private static String[]
	    rendererStrings = {"ambocc", "viewIrradiance", "path"};
	private JComboBox
		rendererComboBox = new JComboBox(rendererStrings);
	
	private JPanel
		metaPanel = new JPanel();
	
	private GridBagConstraints
		metaGBC = new GridBagConstraints();
	
	public AnimationControls(MainController controller) {
		this.controller = controller;

		// main controller
		setTitle("Unfold Options");
		setModal(false);
		setAlwaysOnTop(true);
		setLayout(new GridBagLayout());
		

		metaPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Animation"),
                BorderFactory.createEmptyBorder(5,5,5,5)));
		
		metaPanel.setLayout(new GridBagLayout());
		metaGBC.fill = BOTH;
		metaGBC.insets = new Insets(1,1,1,1);
		
		metaGBC.gridx = 1;
		metaGBC.gridy = 0;
		metaPanel.add(browseButton, metaGBC);
		metaGBC.gridx = 0;
		metaGBC.gridy = 0;
		metaPanel.add(directoryTextField, metaGBC);

		metaGBC.gridx = 1;
		metaGBC.gridy = 1;
		metaPanel.add(new JLabel("Filename"), metaGBC);
		metaGBC.gridx = 0;
		metaGBC.gridy = 1;
		metaPanel.add(filnameTextField, metaGBC);
		
		metaGBC.gridx = 1;
		metaGBC.gridy = 2;
		metaPanel.add(new JLabel("#Frames"), metaGBC);
		metaGBC.gridx = 0;
		metaGBC.gridy = 2;
		metaPanel.add(frameSpinner, metaGBC);

		metaGBC.gridx = 1;
		metaGBC.gridy = 3;
		metaPanel.add(new JLabel("Rotate"), metaGBC);
		metaGBC.gridx = 0;
		metaGBC.gridy = 3;
		metaPanel.add(rotateBox, metaGBC);
		
		metaGBC.gridx = 1;
		metaGBC.gridy = 4;
		metaPanel.add(new JLabel("Unfold"), metaGBC);
		metaGBC.gridx = 0;
		metaGBC.gridy = 4;
		metaPanel.add(unfoldBox, metaGBC);
		
		metaGBC.gridx = 1;
		metaGBC.gridy = 5;
		metaPanel.add(renderButton, metaGBC);
		
		add(metaPanel, metaGBC);


		setPreferredSize(new Dimension(220, 220));
		pack();
		
		frameSpinner.addChangeListener(this);

		renderButton.addActionListener(this);
		browseButton.addActionListener(this);

		unfoldBox.addActionListener(this);
		rendererComboBox.addActionListener(this);
		rotateBox.addActionListener(this);

	}


	public void stateChanged(ChangeEvent event) {
		Object s = event.getSource();


		if (frameSpinner == s){
			int d = frameModel.getNumber().intValue();

		}

	}
	
	
	public void actionPerformed(ActionEvent event) {

		Object s = event.getSource();

		if (renderButton == s){
			// render action

			for(int i = 0; i < frameModel.getNumber().intValue(); i++) {
				if(rotate) {
					double deg = 2*Math.PI*i/frameModel.getNumber().intValue();
					Transformation t = new Transformation(MatrixBuilder.euclidean().rotateX(deg).getMatrix().getArray());
					controller.getPolytopView().getGeometry().setTransformation(t);
				}
				
				if(unfold) {
					LinkedList<GraphTool<CPMVertex, CPMEdge, CPMFace>> cc = controller.getToolController().getRegisteredTools();
					for(GraphTool<CPMVertex, CPMEdge, CPMFace> c : cc) {
						if(c.getClass() == UnfoldTool.class) {
							UnfoldTool b = (UnfoldTool)c;
							b.setFoldStrength((int)(100+95.*Math.cos(2*Math.PI*i/(double)frameModel.getNumber().intValue()))/2.0);
						}
					}
				}
				
				// I hate java
				String si = null;
				if(i < 10)
					si = "0000" + Integer.toString(i);
				else if(i < 100)
					si = "000" + Integer.toString(i);
				else if(i < 1000)
					si = "00" + Integer.toString(i);
				else if(i < 10000)
					si = "0" + Integer.toString(i);
				else if(i < 100000)
					si = Integer.toString(i);
				
				File f = new File(directoryTextField.getText() + "/" + filnameTextField.getText() + si + ".png");
				Dimension d = new Dimension(720, 540);
				RenderOptions ro = new RenderOptions();
				Viewer v = controller.getPolytopView().getViewer();
				
				
				try {
					new Statement(Class.forName("de.jreality.sunflow.Sunflow"), "renderAndSave", new Object[]{v, ro, d, f}).execute();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
		} 
		
		if (browseButton == s) {
			// select new folder
		}
		
		if(rendererComboBox == s) {
			String choice = (String)rendererComboBox.getSelectedItem();
			if(choice == "Graph") {
			} if(choice == "Voronoi") {
			} if(choice == "Join tree") {
			}
		}
	}
	
	
	public static void main(String[] args) {
		AnimationControls app = new AnimationControls(null);
		app.setVisible(true);
	}
	
}
