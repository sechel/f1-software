package alexandrov.frontend.tool;

import halfedge.HalfEdgeDataStructure;
import halfedge.frontend.content.GraphGraphics;
import halfedge.frontend.graphtool.EditOperation;
import halfedge.frontend.graphtool.EditOperationException;
import halfedge.frontend.graphtool.GraphTool;
import halfedge.surfaceutilities.SurfaceException;
import halfedge.surfaceutilities.SurfaceUtility;
import halfedge.triangulationutilities.ConsistencyCheck;
import halfedge.triangulationutilities.Delaunay;
import halfedge.triangulationutilities.TriangulationException;
import image.ImageHook;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.Random;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import util.debug.DBGTracer;
import alexandrov.Alexandrov;
import alexandrov.Alexandrov2;
import alexandrov.frontend.content.AlexandrovPolytopView;
import alexandrov.frontend.controller.MainController;
import alexandrov.graph.CPMEdge;
import alexandrov.graph.CPMFace;
import alexandrov.graph.CPMVertex;
import alexandrov.math.CPMCurvatureFunctional;


/**
 * Loads a ikosahedron topology and radomizes the edge lengths
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 */
public class RandomCheckTool implements GraphTool<CPMVertex, CPMEdge, CPMFace>, Runnable {

	private Icon 
		icon = new ImageIcon(ImageHook.getImage("refresh.png"));
	private boolean
		checksRunning = false;
	private MainController
		controller = null;
	private AlexandrovPolytopView
		view = null;
	private JFrame
		mainFrame = null;
	private Double
		smallRadiusThreashold = 1E-5; 
	
	
	public RandomCheckTool(AlexandrovPolytopView view, JFrame mainFrame){
		this.view = view;
		this.mainFrame = mainFrame;
	}
	
	
	@SuppressWarnings("unchecked")
	public HalfEdgeDataStructure<CPMVertex, CPMEdge, CPMFace> getTestGraph(){
		InputStream in = getClass().getResourceAsStream("ikosaeder.cpm");
		try {
			ObjectInputStream ois = new ObjectInputStream(in);
			HalfEdgeDataStructure<CPMVertex, CPMEdge, CPMFace> result = null;
			result = (HalfEdgeDataStructure<CPMVertex, CPMEdge, CPMFace>)ois.readObject();
			return result;
		} catch (FileNotFoundException fnfe){
			fnfe.printStackTrace();
		} catch (IOException ioe){
			ioe.printStackTrace();
		} catch (ClassNotFoundException cnfe){
			cnfe.printStackTrace();
		}
		return null;
	}
	
	
	
	public Boolean initTool() {
		Thread runner = new Thread(this, "Random Check");
		runner.start();
		return true;
	}

	
	public void run() {
		ReportDialog reportDialog = new ReportDialog(mainFrame);
		StringBuffer report = new StringBuffer();
		checksRunning = true;
		Random rnd = new Random();
		rnd.setSeed(1);
		report.append("Loading Test Graph ikosaeder.cpm...");
		HalfEdgeDataStructure<CPMVertex, CPMEdge, CPMFace> graph = getTestGraph();
		controller.setEditedGraph(graph);
		report.append("done.\n");
		try {
			SurfaceUtility.linkAllEdges(graph);
			SurfaceUtility.fillHoles(graph);
		} catch (SurfaceException e1) {
			DBGTracer.stackTrace(e1);
			controller.setStatus(e1.getMessage());
			return;
		}
		report.append("Beginning Checks\n");
		int checknum = 0;
		while (checksRunning){
			controller.setStatus("randomizing...");
			checknum++;
			report.append("Check " + checknum + " -----------------\n");
			report.append("Finding valid edge lengths...\n");
			
			//find lengths where the triangle inequation holds
			try {
				do{
					for (CPMEdge e : graph.getPositiveEdges()){
						Double length = rnd.nextDouble()*(2.0/3.0) + 1.0/3.0;
						e.setLength(length);
						e.getOppositeEdge().setLength(length);
					}
				} while (!ConsistencyCheck.checkEdgeLengths(graph) || !CPMCurvatureFunctional.isMetricConvex(graph));
			} catch (TriangulationException e) {
				e.printStackTrace();
			}
			report.append("calculation... ");
			controller.setStatus("calculation... ");
			HalfEdgeDataStructure<CPMVertex, CPMEdge, CPMFace> runGraph = new HalfEdgeDataStructure<CPMVertex, CPMEdge, CPMFace>(graph);
			try {
				Alexandrov2.constructPolyhedron(runGraph, 2.0, 1E-5, 50, null, null);
			} catch (Exception e1) {
				report.append(e1.getMessage() + "\n");
				int result = JOptionPane.showConfirmDialog(controller.getMainPanel(), e1.getMessage() + "\nTry the safe algorithm?");
				if (result == JOptionPane.OK_OPTION){
					runGraph = new HalfEdgeDataStructure<CPMVertex, CPMEdge, CPMFace>(graph);
					try {
						Alexandrov.constructPolyhedron(runGraph, 2.0, 1E-5, 50, null);
						view.updateGeometry(runGraph);
						view.encompass();
						result = JOptionPane.showConfirmDialog(controller.getMainPanel(), "success in 2nd try: continue?");
						if (result != JOptionPane.OK_OPTION)
							return;
					} catch (Exception e) {
						JOptionPane.showMessageDialog(controller.getMainPanel(), "2nd try given up: \n" + e.getMessage());
					}
				} else {
					return;
				}
			}
			report.append("Polytop sucessfully calculated.\n");
			controller.setStatus("polytop sucessfully calculated.\n");
			report.append("Performed flips: " + Delaunay.getNumFlips(runGraph) + ", Effective flips: " + Delaunay.getNumEffectiveFlips(runGraph) + "\n");
			view.updateGeometry(runGraph);
			view.encompass();
			for (CPMVertex v : runGraph.getVertices()){
				if (v.getRadius() < smallRadiusThreashold){
					JOptionPane.showMessageDialog(controller.getMainPanel(), "Radius was small - iterrupting!" + "\n" + runGraph);
					report.append("!!!!!!!!! RADIUS AT VERTEX " + v.getIndex() + " = " + v.getRadius() + " !!!!!!!!!!!!");
					return;
				}
			}
		}
		if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(controller.getMainPanel(), "Random Checks Stopped: Do you want a report?")){
			reportDialog.setReport(report);
			reportDialog.setVisible(true);
		}
	}
	
	public void leaveTool() {
		checksRunning = false;
	}

	public void setController(halfedge.frontend.controller.MainController<CPMVertex, CPMEdge, CPMFace> controller) {
		this.controller = (MainController)controller;
	}

	public boolean processEditOperation(EditOperation operation)
			throws EditOperationException {
		return false;
	}

	public void commitEdit(HalfEdgeDataStructure<CPMVertex, CPMEdge, CPMFace> graph) {

	}

	public void resetTool() {

	}

	public String getName() {
		return "Random Check Tool";
	}

	public Icon getIcon() {
		return icon;
	}

	public String getDescription() {
		return "This tool generates random edge lengths and runs the algorithm.";
	}

	public String getShortDescription() {
		return "Random Checks";
	}

	public void paint(GraphGraphics g) {

	}

	public boolean needsRepaint() {
		return false;
	}

	public JPanel getOptionPanel() {
		return null;
	}

}
