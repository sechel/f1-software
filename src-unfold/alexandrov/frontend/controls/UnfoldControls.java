package alexandrov.frontend.controls;

import static java.awt.GridBagConstraints.BOTH;
import halfedge.HalfEdgeDataStructure;
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
import java.util.Collection;
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
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.commons.collections15.BidiMap;

import alexandrov.frontend.controller.MainController;
import alexandrov.frontend.tool.CutEdgeRemoverPickTool;
import alexandrov.frontend.tool.SourcePickTool;
import alexandrov.frontend.viewer.GeodesicsViewer;
import alexandrov.frontend.viewer.StarViewer;
import alexandrov.frontend.viewer.UnfoldViewer;
import alexandrov.frontend.viewer.VoronoiViewer;
import alexandrov.graph.CPMEdge;
import alexandrov.graph.CPMFace;
import alexandrov.graph.CPMVertex;
import de.jreality.scene.SceneGraphComponent;

public class UnfoldControls extends JDialog implements ActionListener, ChangeListener{
	
	private boolean first = true;
	private boolean showGeodesics = true;
	private boolean forceDelaunay = false;
	private boolean showUnfold = true;
	private boolean showVoronoi = true;
	private boolean showStarNet = true;
	
	private static final long 
		serialVersionUID = 1L;
	private MainController 
		controller = null;
	private JButton
		newJoinTreeButton = new JButton("New unfolding"),
		newGeodesicsButton = new JButton("New geodesics"),
		resetManualEdgesButton = new JButton("Reset");
	private JSlider
		unfoldStrenghtSlider = new JSlider(0, 100, 0);
	private SpinnerNumberModel
		depthModel = new SpinnerNumberModel(1, 1, 20, 1);
	private JSpinner
		depthSpinner = new JSpinner(depthModel);
	private JCheckBox
		showGeodesicsBox = new JCheckBox("", showGeodesics),
		forceDelaunayBox = new JCheckBox("", forceDelaunay),
		showUnfoldBox = new JCheckBox("", showUnfold),
		showVoronoiBox = new JCheckBox("", showVoronoi),
		showStarNetBox = new JCheckBox("", showStarNet);
	
	private static String[]
	    graphStrings = {"Graph", "Voronoi", "Join tree"};
	private JComboBox
		graphDisplayComboBox = new JComboBox(graphStrings),
		algoComboBox = new JComboBox(Unfolder.getAlgorithmNames());
	
	private JPanel
		unfoldPanel = new JPanel(),
		graphPanel = new JPanel(),
		geodesicsPanel = new JPanel(),
		animationPanel = new JPanel(),
		metaPanel = new JPanel();
	
	private GridBagConstraints
		mainGBC = new GridBagConstraints(),
		unfoldGBC = new GridBagConstraints(),
		graphGBC = new GridBagConstraints(),
		geodesicsGBC = new GridBagConstraints(),
		animationGBC = new GridBagConstraints();
	
	private UnfoldViewer<CPMVertex, CPMEdge, CPMFace>
		unfoldViewer = new UnfoldViewer<CPMVertex, CPMEdge, CPMFace>();
	
	private GeodesicsViewer<CPMVertex, CPMEdge, CPMFace>
		geodesicsViewer = new GeodesicsViewer<CPMVertex, CPMEdge, CPMFace>();

	private VoronoiViewer<CPMVertex, CPMEdge, CPMFace> 
		voronoiViewer = new VoronoiViewer<CPMVertex, CPMEdge, CPMFace>();
	
	private StarViewer<CPMVertex, CPMEdge, CPMFace>
		starViewer = new StarViewer<CPMVertex, CPMEdge, CPMFace>();
	
	private HalfEdgeDataStructure<CPMVertex, CPMEdge, CPMFace> 
		joinTree = null,
		polyGraph = null;
	
	private SceneGraphComponent 
		mainScene = null,
		unfoldScene = null,
		geodesicsScene = null,
		voronoiScene = null,
		starNetScene = null;
	
	Collection<EmbeddedEdge<CPMVertex,CPMEdge,CPMFace>> paths = null;
	
	Collection<CPMEdge> manualEdges = null;
	
	private String algorithm = Unfolder.getDefaultAlgorithm();
	
	private int sourceVertex = 0;
	private SourcePickTool sourcePickTool = null;
	private CutEdgeRemoverPickTool cerpTool = null;
	
	public UnfoldControls(MainController controller) {
		this.controller = controller;

		// main controller
		setTitle("Unfold Options");
		setModal(false);
		setAlwaysOnTop(true);
		setLayout(new GridBagLayout());
		
		// unfold panel
		unfoldPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Unfolding"),
                BorderFactory.createEmptyBorder(5,5,5,5)));
		
		unfoldPanel.setLayout(new GridBagLayout());
		unfoldGBC.fill = BOTH;
		unfoldGBC.insets = new Insets(1,1,1,1);
		
		unfoldGBC.gridx = 0;
		unfoldGBC.gridy = 0;
		unfoldPanel.add(new JLabel("Show unfolding"), unfoldGBC);
		unfoldGBC.gridx = 1;
		unfoldGBC.gridy = 0;
		unfoldPanel.add(showUnfoldBox, unfoldGBC);
		unfoldGBC.gridx = 0;
		unfoldGBC.gridy = 1;
		unfoldPanel.add(new JLabel("Algorithm"), unfoldGBC);
		unfoldGBC.gridx = 1;
		unfoldGBC.gridy = 1;
		unfoldPanel.add(algoComboBox, unfoldGBC);
		unfoldGBC.gridx = 0;
		unfoldGBC.gridy = 2;
		unfoldPanel.add(new JLabel("Unfold strength"), unfoldGBC);
		unfoldGBC.gridx = 1;
		unfoldGBC.gridy = 2;
		unfoldPanel.add(unfoldStrenghtSlider, unfoldGBC);
		unfoldGBC.gridx = 0;
		unfoldGBC.gridy = 3;
		unfoldPanel.add(new JLabel("Unfold depth"), unfoldGBC);
		unfoldGBC.gridx = 1;
		unfoldGBC.gridy = 3;
		unfoldPanel.add(depthSpinner, unfoldGBC);
		unfoldGBC.gridx = 0;
		unfoldGBC.gridy = 4;
		unfoldPanel.add(newJoinTreeButton, unfoldGBC);
		unfoldGBC.gridx = 1;
		unfoldGBC.gridy = 4;
		unfoldPanel.add(resetManualEdgesButton, unfoldGBC);
		
		animationPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Animation"),
                BorderFactory.createEmptyBorder(5,5,5,5)));
		
		animationPanel.setLayout(new GridBagLayout());
		animationGBC.fill = BOTH;
		animationGBC.insets = new Insets(1,1,1,1);
		
		animationGBC.gridx = 0;
		animationGBC.gridy = 0;
		animationPanel.add(new JLabel("Show unfolding"), animationGBC);
		animationGBC.gridx = 1;
		animationGBC.gridy = 0;
		animationPanel.add(showUnfoldBox, animationGBC);
		
		// graph panel
		graphPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Graph"),
                BorderFactory.createEmptyBorder(5,5,5,5)));
		
		graphPanel.setLayout(new GridBagLayout());
		graphGBC.fill = BOTH;
		graphGBC.insets = new Insets(1,1,1,1);
		
		graphGBC.gridx = 0;
		graphGBC.gridy = 0;
		graphPanel.add(new JLabel("Force Delaunay"), graphGBC);
		graphGBC.gridx = 1;
		graphGBC.gridy = 0;
		graphPanel.add(forceDelaunayBox, graphGBC);
		graphGBC.gridx = 0;
		graphGBC.gridy = 1;
		graphPanel.add(new JLabel("Edit"), graphGBC);
		graphGBC.gridx = 1;
		graphGBC.gridy = 1;
		graphPanel.add(graphDisplayComboBox, graphGBC);
		
		// geodesics panel
		geodesicsPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Geodesics"),
                BorderFactory.createEmptyBorder(5,5,5,5)));
		
		geodesicsPanel.setLayout(new GridBagLayout());
		geodesicsGBC.fill = BOTH;
		geodesicsGBC.insets = new Insets(1,1,1,1);

		geodesicsGBC.gridx = 0;
		geodesicsGBC.gridy = 0;
		geodesicsPanel.add(new JLabel("Show geodesics"), geodesicsGBC);
		geodesicsGBC.gridx = 1;
		geodesicsGBC.gridy = 0;
		geodesicsPanel.add(showGeodesicsBox, geodesicsGBC);
		geodesicsGBC.gridx = 0;
		geodesicsGBC.gridy = 1;
		geodesicsPanel.add(new JLabel("Show voronoi regions"), geodesicsGBC);
		geodesicsGBC.gridx = 1;
		geodesicsGBC.gridy = 1;
		geodesicsPanel.add(showVoronoiBox, geodesicsGBC);
		geodesicsGBC.gridx = 0;
		geodesicsGBC.gridy = 2;
		geodesicsPanel.add(new JLabel("Show star net"), geodesicsGBC);
		geodesicsGBC.gridx = 1;
		geodesicsGBC.gridy = 2;
		geodesicsPanel.add(showStarNetBox, geodesicsGBC);
		geodesicsGBC.gridx = 0;
		geodesicsGBC.gridy = 3;
		geodesicsPanel.add(newGeodesicsButton, geodesicsGBC);
		
		metaPanel.setLayout(new GridBagLayout());
		GridBagConstraints metaGBC = new GridBagConstraints();
		metaGBC.fill = BOTH;
		metaGBC.gridx = 0;
		metaGBC.gridy = 0;
		metaPanel.add(graphPanel, metaGBC);
		metaGBC.gridx = 0;
		metaGBC.gridy = 1;
		metaPanel.add(geodesicsPanel, metaGBC);
		
		mainGBC.fill = BOTH;
		mainGBC.gridx = 0;
		mainGBC.gridy = 0;
		add(unfoldPanel, mainGBC);
		mainGBC.gridx = 1;
		mainGBC.gridy = 0;
		add(metaPanel, mainGBC);


		setPreferredSize(new Dimension(500, 255));
		pack();
		
		unfoldStrenghtSlider.addChangeListener(this);
		depthSpinner.addChangeListener(this);
		
		showUnfoldBox.addActionListener(this);
		newJoinTreeButton.addActionListener(this);
		resetManualEdgesButton.addActionListener(this);
		newGeodesicsButton.addActionListener(this);
		showStarNetBox.addActionListener(this);
		showGeodesicsBox.addActionListener(this);
		showVoronoiBox.addActionListener(this);
		graphDisplayComboBox.addActionListener(this);
		forceDelaunayBox.addActionListener(this);
		algoComboBox.addActionListener(this);
		
		sourcePickTool = new SourcePickTool(this);
		cerpTool = new CutEdgeRemoverPickTool(this);
		manualEdges = new LinkedList<CPMEdge>();
		

		controller.getPolytopView().getGeometry().getChildComponent(1).addTool(sourcePickTool);
		controller.getPolytopView().getGeometry().getChildComponent(1).addTool(cerpTool);
	}
	
	private Double getUnfoldState(){
		return unfoldStrenghtSlider.getValue() / 100.0;
	}
	
	public void setUnfoldState(double s) {
		unfoldStrenghtSlider.setValue((int)s);
	}
	
	public void setSourceVertex(int v) {
		sourceVertex = v;
	}
	

	public void stateChanged(ChangeEvent event) {
		Object s = event.getSource();
		if (unfoldStrenghtSlider == s){
			updateFold();
			updateView();
		}

		if (depthSpinner == s){
			int d = depthModel.getNumber().intValue();
			unfoldViewer.setUnfoldDepth(d);
			depthModel.setMaximum(unfoldViewer.getMaximumUnfoldDepth());
			updateFold();
			updateView();
		}

	}
	
	
	public void actionPerformed(ActionEvent event) {

		Object s = event.getSource();

		if (newJoinTreeButton == s){
			makeNewUnfolding();
		} if (newGeodesicsButton == s){
			makeNewGeodesics();
		} if (resetManualEdgesButton == s) {
			resetManualEdges();
		} if(showGeodesicsBox == s) {
			showGeodesics = !showGeodesics;
			if(geodesicsScene != null)
				geodesicsScene.setVisible(showGeodesics);
		} if(showVoronoiBox == s) {
			showVoronoi = !showVoronoi;
			if(voronoiScene != null)
				voronoiScene.setVisible(showVoronoi);
		} if(showStarNetBox == s) {
			showStarNet = !showStarNet;
			if(starNetScene != null)
				starNetScene.setVisible(showStarNet);
		} if(showUnfoldBox == s) {
			showUnfold = !showUnfold;
			if(unfoldScene != null)
				unfoldScene.setVisible(showUnfold);
		} if(forceDelaunayBox == s) {
			forceDelaunay = !forceDelaunay;
		} if(algoComboBox == s) {
			algorithm = (String)algoComboBox.getSelectedItem();
		} if(graphDisplayComboBox == s) {
			String choice = (String)graphDisplayComboBox.getSelectedItem();
			if(choice == "Graph") {
				controller.setEditedGraph(polyGraph);
			} if(choice == "Voronoi") {
				try {
					controller.setEditedGraph(Unfolder.constructVoronoi(polyGraph));
				} catch (TriangulationException e) {
					System.err.println("No triangulation");
				}
			} if(choice == "Join tree") {
					controller.setEditedGraph(joinTree);
			}
		}
	}
	
	private void resetManualEdges() {
		for(CPMEdge e : manualEdges) {
			e.setHidden(false);
		}
		manualEdges = new LinkedList<CPMEdge>();
	}
	
	public void removeCutEdge(int edgeID) {
		CPMEdge e = polyGraph.getEdge(edgeID);
		manualEdges.add(e);
		manualEdges.add(e.getOppositeEdge());
		
		polyGraph.getEdge(edgeID).setHidden(true);
		
		if(joinTree != null) {
			CPMEdge je = joinTree.addNewEdge();
			CPMEdge jeo = joinTree.addNewEdge();
			
			je.setTargetVertex(joinTree.getVertex(e.getLeftFace().getIndex()));
			jeo.setTargetVertex(joinTree.getVertex(e.getRightFace().getIndex()));
			je.linkOppositeEdge(jeo);
			
			updateFold();
		}
	}

	
	private void makeNewGeodesics() {

		polyGraph = new HalfEdgeDataStructure<CPMVertex, CPMEdge, CPMFace>(controller.getPolytopView().getActiveGraph());

		BidiMap<CPMVertex, EmbeddedEdge<CPMVertex, CPMEdge, CPMFace>>
			geodesics = StarTreeFinder.getStarTree(polyGraph, polyGraph.getVertex(sourceVertex));
		
		geodesicsViewer.setGeodesics(geodesics);
		
		// geodesics
		geodesicsViewer.setGraph(polyGraph);
		geodesicsViewer.generateSceneGraphComponent();
		
		if(geodesicsScene != null) {
			// risky...
			mainScene.removeChild(mainScene.getChildComponent(mainScene.getChildComponentCount()-1));
		}
		
		updateView();
		
		mainScene = controller.getPolytopView().getGeometry();
		geodesicsScene = geodesicsViewer.getSceneGraphComponent();
		mainScene.addChild(geodesicsScene);
		
		controller.getPolytopView().updatePolyhedron(polyGraph);
		
		// voronoi
		voronoiViewer.setGraph(polyGraph);
		voronoiViewer.generateSceneGraphComponent();
		
		if(voronoiScene != null) {
			// risky...
			mainScene.removeChild(mainScene.getChildComponent(mainScene.getChildComponentCount()-1));
		}
		
		updateView();
		
		mainScene = controller.getPolytopView().getGeometry();
		voronoiScene = voronoiViewer.getSceneGraphComponent();
		mainScene.addChild(voronoiScene);
		
		controller.getPolytopView().updatePolyhedron(polyGraph);
		
		// star net
		starViewer.setGeodesics(geodesics);
		starViewer.setSource(polyGraph.getVertex(sourceVertex));
		starViewer.setGraph(polyGraph);
		starViewer.generateSceneGraphComponent();
		
		if(starNetScene != null) {
			// risky...
			mainScene.removeChild(mainScene.getChildComponent(mainScene.getChildComponentCount()-1));
		}
		
		updateView();
		
		mainScene = controller.getPolytopView().getGeometry();
		starNetScene = starViewer.getSceneGraphComponent();
		mainScene.addChild(starNetScene);
		
		controller.getPolytopView().updatePolyhedron(polyGraph);
		

	}
	
	private void makeNewUnfolding() {
		
		polyGraph = new HalfEdgeDataStructure<CPMVertex, CPMEdge, CPMFace>(controller.getPolytopView().getActiveGraph());
		
		if(forceDelaunay) {
			if(Delaunay.isDelaunay(polyGraph)) {
				System.err.println("Graph is already Delaunay");
			} else {
				try{
					Delaunay.constructDelaunay(polyGraph);
					System.err.println("Delauneysing the polytope");
				} catch (TriangulationException e) {
					controller.setStatus(e.getMessage());
					return;
				}
			}
		}
		try{
			joinTree = Unfolder.getUnfolding(polyGraph, polyGraph.getVertex(sourceVertex), algorithm);
		} catch (TriangulationException e) {
			controller.setStatus(e.getMessage());
			return;
		}
		unfoldViewer.setGraph(polyGraph);
		unfoldViewer.setJoinTree(joinTree);
		

		if(unfoldScene != null) {
			// risky...
			mainScene.removeChild(mainScene.getChildComponent(mainScene.getChildComponentCount()-1));
		}
		
		updateFold();
		
		mainScene = controller.getPolytopView().getGeometry();
		unfoldScene = unfoldViewer.getSceneGraphComponent();
		mainScene.addChild(unfoldScene);
		
		updateView();

	}


	public void updateFold() {
		
		unfoldViewer.setSource(polyGraph.getVertex(sourceVertex));
		unfoldViewer.setFoldStrength(getUnfoldState());
		unfoldViewer.generateSceneGraphComponent();
		

		controller.getPolytopView().updatePolyhedron(polyGraph);
		
	}
	
	public void updateView(){

		controller.getPolytopView().update();
		controller.getPolytopView().getViewer().render();

	}
	
	
	public static void main(String[] args) {
		UnfoldControls app = new UnfoldControls(null);
		app.setVisible(true);
	}
	
}
