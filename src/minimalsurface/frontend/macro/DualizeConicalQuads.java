package minimalsurface.frontend.macro;

import halfedge.HalfEdgeDataStructure;
import image.ImageHook;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.vecmath.Point4d;
import javax.vecmath.Vector4d;

import minimalsurface.util.GraphUtility;
import minimalsurface.util.MinimalSurfaceUtility;
import circlepatterns.graph.CPEdge;
import circlepatterns.graph.CPFace;
import circlepatterns.graph.CPVertex;
import de.jreality.geometry.BallAndStickFactory;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.writer.WriterOBJ;

public class DualizeConicalQuads extends MacroAction implements ActionListener, ChangeListener {

	protected Icon 
		icon = new ImageIcon(ImageHook.getImage("schwarz.png"));
	private SpinnerNumberModel
		associatedFamilyAngle = new SpinnerNumberModel(0.0, 0.0, 360.0, 1.0),
		alphaMinModel = new SpinnerNumberModel(0.0, 0.0, 360.0, 1.0),
		alphaMaxModel = new SpinnerNumberModel(90.0, 0.0, 360.0, 1.0),
		numStepsModel = new SpinnerNumberModel(90, 1, 360, 1),
		stickRadiusModel = new SpinnerNumberModel(0.005, 0.001, 1.0, 0.001);
	private JSpinner
		associatedFamilySpinner = new JSpinner(associatedFamilyAngle),
		alphaMinSpinner = new JSpinner(alphaMinModel),
		alphaMaxSpinner = new JSpinner(alphaMaxModel),
		numStepsSpinner = new JSpinner(numStepsModel),
		stickRadiusSpinner = new JSpinner(stickRadiusModel);
	private JCheckBox
		writeOBJAnimChecker = new JCheckBox("Write OBJ Series", false);
	private JPanel
		animationPanel = new JPanel();
	private JTextField
		directoryTextField = new JTextField();
	private JFileChooser
		directoryChooser = new JFileChooser(".");
	private JButton
		directoryChooseButton = new JButton("Select...");
	private JCheckBox
		sticksChecker = new JCheckBox("Tube Files");
	private JLabel
		minAlphaLabel = new JLabel("Min Psi"),
		maxAlphaLabel = new JLabel("Max Psi"),
		stepsLabel = new JLabel("Steps");
	
	private File
		animationDirectory = null;
	
	public DualizeConicalQuads() {
		optionPanel = new JPanel();
		optionPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(2, 2, 2, 2);
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1.0;
		c.gridwidth = GridBagConstraints.RELATIVE;
		optionPanel.add(new JLabel("Associated Family"));
		c.gridwidth = GridBagConstraints.REMAINDER;
		optionPanel.add(associatedFamilySpinner, c);
		
		animationPanel.setLayout(new GridBagLayout());
		animationPanel.add(writeOBJAnimChecker, c);
		c.gridwidth = GridBagConstraints.RELATIVE;
		animationPanel.add(minAlphaLabel, c);
		c.gridwidth = GridBagConstraints.REMAINDER;
		animationPanel.add(alphaMinSpinner, c);
		c.gridwidth = GridBagConstraints.RELATIVE;
		animationPanel.add(maxAlphaLabel, c);
		c.gridwidth = GridBagConstraints.REMAINDER;
		animationPanel.add(alphaMaxSpinner, c);
		c.gridwidth = GridBagConstraints.RELATIVE;
		animationPanel.add(stepsLabel, c);
		c.gridwidth = GridBagConstraints.REMAINDER;
		animationPanel.add(numStepsSpinner, c);
		c.gridwidth = GridBagConstraints.RELATIVE;
		animationPanel.add(sticksChecker, c);
		c.gridwidth = GridBagConstraints.REMAINDER;
		animationPanel.add(stickRadiusSpinner, c);		
		c.gridwidth = GridBagConstraints.RELATIVE;
		directoryTextField.setEditable(false);
		animationPanel.add(directoryTextField, c);
		c.gridwidth = GridBagConstraints.REMAINDER;
		animationPanel.add(directoryChooseButton, c);
		c.gridwidth = GridBagConstraints.REMAINDER;
		animationPanel.setBorder(BorderFactory.createTitledBorder("Animation Series"));
		optionPanel.add(animationPanel, c);
		
		c.weighty = 1.0;
		optionPanel.add(new JPanel(), c);
		
		writeOBJAnimChecker.addActionListener(this);
		directoryChooseButton.addActionListener(this);
		alphaMaxSpinner.addChangeListener(this);
		alphaMinModel.addChangeListener(this);
		
		directoryChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		directoryChooser.setDialogTitle("Select Series Directory");
		updateStates();
	}
	
	private void updateStates() {
		boolean animate = writeOBJAnimChecker.isSelected();
		alphaMaxSpinner.setEnabled(animate);
		alphaMinSpinner.setEnabled(animate);
		numStepsSpinner.setEnabled(animate);
		animationPanel.setEnabled(animate);
		directoryChooseButton.setEnabled(animate);
		directoryTextField.setEnabled(animate);
		sticksChecker.setEnabled(animate);
		stickRadiusSpinner.setEnabled(animate);
		minAlphaLabel.setEnabled(animate);
		maxAlphaLabel.setEnabled(animate);
		stepsLabel.setEnabled(animate);
	}
	
	@Override
	public void stateChanged(ChangeEvent e) {
		alphaMinModel.setMaximum(alphaMaxModel.getNumber().doubleValue());
		alphaMaxModel.setMinimum(alphaMinModel.getNumber().doubleValue());
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if (writeOBJAnimChecker == e.getSource()) {
			updateStates();
		}
		if (directoryChooseButton == e.getSource()) {
			Window parent = SwingUtilities.getWindowAncestor(optionPanel);
			int r = directoryChooser.showOpenDialog(parent);
			if (r != JFileChooser.APPROVE_OPTION) return;
			animationDirectory = directoryChooser.getSelectedFile();
			directoryTextField.setText(animationDirectory.getName());
		}
	}
	
	@Override
	public String getName() {
		return "Dualize Conical Quads";
	}

	@Override
	public HalfEdgeDataStructure<CPVertex, CPEdge, CPFace> process(
		HalfEdgeDataStructure<CPVertex, CPEdge, CPFace> gaussMap
	) throws Exception {
		// calculate edge normals
		Map<CPEdge, Vector4d> edgeNormals = new HashMap<CPEdge, Vector4d>();
		for (CPEdge e : gaussMap.getPositiveEdges()) {
			Vector4d n = MinimalSurfaceUtility.getEdgeNormal(e);
			edgeNormals.put(e, n);
			edgeNormals.put(e.getOppositeEdge(), n);
		}
		
		boolean writeAnimation = writeOBJAnimChecker.isSelected();
		if (writeAnimation) {
			// store gauss map geometry
			Map<CPVertex, Point4d> gaussPositions = new HashMap<CPVertex, Point4d>();
			for (CPVertex v : gaussMap.getVertices()) {
				gaussPositions.put(v, new Point4d(v.getXYZW()));
			}
			double minAlpha = alphaMinModel.getNumber().doubleValue();
			double maxAlpha = alphaMaxModel.getNumber().doubleValue();
			int steps = numStepsModel.getNumber().intValue();
			double stepsize = (maxAlpha - minAlpha) / steps;
			int counter = 0;
			for (double alpha = minAlpha; alpha < maxAlpha + stepsize / 2; alpha += stepsize) {
				double radAlpha = Math.toRadians(alpha);
				for (CPVertex v : gaussMap.getVertices()) v.setXYZW(gaussPositions.get(v));
				MinimalSurfaceUtility.dualizeSurfaceKoenigs(gaussMap, edgeNormals, radAlpha);
				
				IndexedFaceSet ifs = GraphUtility.toIndexedFaceSet(gaussMap);
				if (sticksChecker.isSelected()) {
					double radius = stickRadiusModel.getNumber().doubleValue();
					File file = new File(animationDirectory, makeOBJFileName("mesh", counter));
					BallAndStickFactory bsf = new BallAndStickFactory(ifs);
					bsf.setBallRadius(radius);
					bsf.setStickRadius(radius);
					bsf.setShowBalls(true);
					bsf.setShowSticks(true);
					bsf.update();
					WriterOBJ.write(bsf.getSceneGraphComponent(), new FileOutputStream(file));
				}
				File file = new File(animationDirectory, makeOBJFileName("surface", counter));
				WriterOBJ.write(ifs, new FileOutputStream(file));
				counter++;
			}
		} else {
			double alpha = Math.toRadians(associatedFamilyAngle.getNumber().doubleValue());
			MinimalSurfaceUtility.dualizeSurfaceKoenigs(gaussMap, edgeNormals, alpha);
		}
		return gaussMap;
	}
	
	protected String makeOBJFileName(String prefix, int count) {
		NumberFormat nf = new DecimalFormat("000");
		return prefix + nf.format(count) + ".obj";
	}
	

	@Override
	public Icon getIcon() {
		return icon;
	}
	
}
