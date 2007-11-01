package teamgeist.frontend;

import static java.awt.GridBagConstraints.HORIZONTAL;
import static java.awt.GridBagConstraints.RELATIVE;
import static java.awt.GridBagConstraints.REMAINDER;
import halfedge.HalfEdgeDataStructure;
import halfedge.HalfEdgeUtility;
import halfedge.surfaceutilities.SurfaceException;
import halfedge.surfaceutilities.SurfaceUtility;
import halfedge.triangulationutilities.HaussdorfDistance;
import halfedge.triangulationutilities.TriangulationException;
import image.ImageHook;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import teamgeist.calculation.CalculateTeamgeistThread;
import teamgeist.calculation.CalculateTeamgeistThread.ResultListener;
import teamgeist.combinatorics.EdgeLengthMap;
import teamgeist.combinatorics.TeamgeistCombinatorics;
import teamgeist.combinatorics.TeamgeistLengthMap2;
import teamgeist.frontend.controller.MainController;
import util.debug.DBGTracer;
import alexandrov.graph.CPMEdge;
import alexandrov.graph.CPMFace;
import alexandrov.graph.CPMVertex;
import circlepatterns.frontend.content.ShrinkPanel;

public class TeamgeistCreationPanel extends ShrinkPanel implements ActionListener, ResultListener{

	private static final long 
		serialVersionUID = 1L;
	
	private MainController 
		controller = null;
	
	private Icon 
		icon = new ImageIcon(ImageHook.getImage("teamgeist.png"));
	private JButton
		makeRandomTeamgeist = new JButton("Randomize", icon),
		makeMinimalTeamgeist = new JButton("Minimize To Sphere", icon),
		stopMinimizing = new JButton("Stop");
	private SpinnerNumberModel
		seedModel = new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1);
	private JSpinner
		seedSpinner = new JSpinner(seedModel);
	private CalculateTeamgeistThread
		calcThread = null;
	private EdgeLengthMap
		lengthMap = new TeamgeistLengthMap2();
	private JPanel
		minimizePanel = new JPanel();
	private JTextField
		actErrorField = new JTextField("");
	
	private boolean 
		minimizeRunning = false;
	private Double
		actDistance = Double.MAX_VALUE;
	
	
	public TeamgeistCreationPanel(MainController controller) {
		super("Teamgeist");
		this.controller = controller;
		calcThread = new CalculateTeamgeistThread(controller, lengthMap);
		makeLayout();
	}


	private void makeLayout() {
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = HORIZONTAL;
		c.insets = new Insets(2,2,2,2);
		c.gridwidth = REMAINDER;
		c.weightx = 1.0;
		
		add(makeRandomTeamgeist, c);
		c.gridwidth = RELATIVE;
		add(new JLabel("Seed"), c);
		c.gridwidth = REMAINDER;
		add(seedSpinner, c);
		add(minimizePanel, c);
		
		minimizePanel.setLayout(new GridBagLayout());
		minimizePanel.add(makeMinimalTeamgeist, c);
		minimizePanel.add(actErrorField, c);
		minimizePanel.setBorder(BorderFactory.createTitledBorder("Minimizer"));
		minimizePanel.add(stopMinimizing, c);
		
		makeRandomTeamgeist.addActionListener(this);
		makeMinimalTeamgeist.addActionListener(this);
		stopMinimizing.addActionListener(this);
	}


	public void actionPerformed(ActionEvent e) {
		Object s = e.getSource();
		if (makeRandomTeamgeist == s){
			calcThread.removeAllResultListeners();
			randomTeamgeist();
		}
		if (makeMinimalTeamgeist == s){
			calcThread.addResultListener(this);
			minimalTeamgeist();
		}
		if (stopMinimizing == s)
			stopMinimizing();
	}
	
	
	private void minimalTeamgeist(){
		minimizeRunning = true;
		actDistance = Double.MAX_VALUE;
		randomTeamgeist();
	}
	
	
	private void stopMinimizing(){
		minimizeRunning = false;
	}
	
	public void error(String message) {
//		actErrorField.setText(message);
	}


	public void success(HalfEdgeDataStructure<CPMVertex, CPMEdge, CPMFace> result) {
		Double distance = Double.MAX_VALUE;
		try {
			distance = HaussdorfDistance.getDistanceToSphere(result);
		} catch (TriangulationException e) {
			e.printStackTrace();
		}
		if (actDistance > distance){
			controller.getViewer().viewTeamgeist(result);
			controller.getViewer().encompass();
			actErrorField.setText(distance + "");
			actDistance = distance;
		}
		if (minimizeRunning)
			randomTeamgeist();
	}
	
	private void randomTeamgeist() {
		DBGTracer.msg("loading graph: teamgeist_kombinatorik2_version2.cpm...");
		HalfEdgeDataStructure<CPMVertex, CPMEdge, CPMFace> graph = TeamgeistCombinatorics.getTeamgeistGraph();
		DBGTracer.msg("done");
		DBGTracer.msg("graph has " + graph.getNumEdges() + " edges");
		try {
			HalfEdgeUtility.removeAllFaces(graph);
			SurfaceUtility.linkAllEdges(graph);
			SurfaceUtility.fillHoles(graph);
		} catch (SurfaceException e) {
			controller.setStatus(e.getMessage());
			return;
		}
		calcThread.setRandomSeed(seedModel.getNumber().longValue());
		calcThread.setError(1E-10);
		calcThread.setMaxIterations(1000);
		calcThread.calculatePolyhedron(graph);
	}


}
