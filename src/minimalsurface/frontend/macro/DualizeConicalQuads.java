package minimalsurface.frontend.macro;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.HashMap;
import java.util.Map;

import halfedge.HalfEdgeDataStructure;
import image.ImageHook;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.vecmath.Vector4d;

import minimalsurface.util.MinimalSurfaceUtility;
import circlepatterns.graph.CPEdge;
import circlepatterns.graph.CPFace;
import circlepatterns.graph.CPVertex;

public class DualizeConicalQuads extends MacroAction {

	protected Icon 
		icon = new ImageIcon(ImageHook.getImage("schwarz.png"));
	private SpinnerNumberModel
		associatedFamilyAngle = new SpinnerNumberModel(0.0, 0.0, 180.0, 1.0);
	private JSpinner
		associatedFamilySpinner = new JSpinner(associatedFamilyAngle);
	
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
		c.weighty = 1.0;
		optionPanel.add(new JPanel(), c);
	}
	
	@Override
	public String getName() {
		return "Dualize Conical Quads";
	}

	@Override
	public HalfEdgeDataStructure<CPVertex, CPEdge, CPFace> process(
		HalfEdgeDataStructure<CPVertex, CPEdge, CPFace> gaussMap
	) throws Exception {
		double alpha = Math.toRadians(associatedFamilyAngle.getNumber().doubleValue());
		Map<CPEdge, Vector4d> edgeNormals = new HashMap<CPEdge, Vector4d>();
		for (CPEdge e : gaussMap.getPositiveEdges()) {
			Vector4d n = MinimalSurfaceUtility.getEdgeNormal(e);
			edgeNormals.put(e, n);
			edgeNormals.put(e.getOppositeEdge(), n);
		}
		MinimalSurfaceUtility.dualizeSurfaceKoenigs(gaussMap, edgeNormals, alpha);
		return gaussMap;
	}

	@Override
	public Icon getIcon() {
		return icon;
	}
	
}
