package minimalsurface.frontend.content;

import static image.ImageHook.getImage;
import static java.awt.BorderLayout.CENTER;
import static java.awt.BorderLayout.SOUTH;
import static java.awt.GridBagConstraints.HORIZONTAL;
import static java.awt.GridBagConstraints.NONE;
import static java.awt.GridBagConstraints.REMAINDER;
import static java.awt.GridBagConstraints.WEST;
import halfedge.HalfEdgeDataStructure;
import image.ImageHook;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.LinkedList;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.TransferHandler;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

import minimalsurface.controller.MainController;
import minimalsurface.frontend.macro.AutoCut;
import minimalsurface.frontend.macro.BoundaryConditions;
import minimalsurface.frontend.macro.CentralExtensionSubdivide;
import minimalsurface.frontend.macro.ConsistentStripSubdivide;
import minimalsurface.frontend.macro.CutEars;
import minimalsurface.frontend.macro.DualGraphSubdivision;
import minimalsurface.frontend.macro.DualizeKiteQuads;
import minimalsurface.frontend.macro.DualizeConicalQuads;
import minimalsurface.frontend.macro.EdgeQuadSubdivide;
import minimalsurface.frontend.macro.EnneperCirclesGenerator;
import minimalsurface.frontend.macro.KobePolyhedron;
import minimalsurface.frontend.macro.LoadCombinatorics;
import minimalsurface.frontend.macro.LoadToGraphEditor;
import minimalsurface.frontend.macro.MacroAction;
import minimalsurface.frontend.macro.MedialPolyhedron;
import minimalsurface.frontend.macro.MedialSubdivide;
import minimalsurface.frontend.macro.SphericalCirclePattern;
import minimalsurface.frontend.macro.SurfaceEditor;
import minimalsurface.frontend.macro.VertexQuadSubdivide;
import circlepatterns.graph.CPEdge;
import circlepatterns.graph.CPFace;
import circlepatterns.graph.CPVertex;
import de.varylab.feedback.swing.FeedbackAction;


public class MainWindow extends JFrame implements ListSelectionListener, ActionListener, MouseListener{

	private static final long 
		serialVersionUID = 1L;
	private final String 
		appName = "Minimal Surface Construction"; 
	private MainController
		controller = new MainController();
	private MinimalSurfaceViewer
		viewer = null;
	private LinkedList<MacroAction>
		creationPlan = new LinkedList<MacroAction>();

	
	private MacroAction[]
	  	actions = 
	  	{
			new LoadCombinatorics(), 
			new EnneperCirclesGenerator(),
			new KobePolyhedron(),
			new MedialPolyhedron(),
			new SphericalCirclePattern(),
			new BoundaryConditions(),
			new MedialSubdivide(),
			new VertexQuadSubdivide(),
			new ConsistentStripSubdivide(),
			new EdgeQuadSubdivide(),
			new DualGraphSubdivision(),
			new CentralExtensionSubdivide(),
			new CutEars(),
			new AutoCut(), 
			new DualizeConicalQuads(),
			new DualizeKiteQuads(),
			new LoadToGraphEditor(),
			new SurfaceEditor()
		};
	
	private JPanel
		definitionPanel = new JPanel(),
		calculationPanel = new JPanel(),
		actionOptionsPanel = new JPanel(),
		planPanel = new JPanel(),
		descriptionPanel = new JPanel(),
		libraryPanel = new JPanel();
	private JToolBar
		planToolBar = new JToolBar();
	private JLabel
		descriptionLabel = new JLabel("No Description");
	private JTable
		libraryTable = new JTable(new LibraryTableModel()),
		planTable = new JTable(new PlanTableModel());
	private JButton
		constructButton = new JButton(new ConstructSurfaceAction()),
		viewSurfaceButton = new JButton("View Minimal Surface", new ImageIcon(getImage("surface.png"))),
		removeActionButton = new JButton(new ImageIcon(getImage("delete.png"))),
		upButton = new JButton(new ImageIcon(getImage("up.png"))),
		downButton = new JButton(new ImageIcon(getImage("down.png"))),
		templateEuclideanBtn = new JButton(new ImageIcon(getImage("templateEuc.png"))),
		templateSphericalBtn = new JButton(new ImageIcon(getImage("templateSph.png")));
	private JProgressBar
		progressBar = new JProgressBar(0, 100);
	private JLabel
		statusLabel = new JLabel("Progress Status:");

	

	public MainWindow() {
		setSize(820, 460);
		setResizable(false);
		setTitle(appName);
		setLocationRelativeTo(controller.getMainFrame());
		setLocationByPlatform(false);
	
		makeLayout();
		
		controller.setMainFrame(this);
		controller.setMainPanel((JPanel)getContentPane());
		viewer = new MinimalSurfaceViewer(this, controller);
		updateInfos();
	}
	
	
	private void makeLayout() {
		setLayout(new BorderLayout());
		add(definitionPanel, CENTER);
		add(calculationPanel, SOUTH);
		
		libraryPanel.setBorder(BorderFactory.createTitledBorder("Action Library"));
		planPanel.setBorder(BorderFactory.createTitledBorder("Creation Plan"));
		actionOptionsPanel.setBorder(BorderFactory.createTitledBorder("Action Options"));			
		
		libraryTable.setTableHeader(null);
		JScrollPane libraryScroller = new JScrollPane(libraryTable);
		libraryPanel.setLayout(new BorderLayout());
		libraryPanel.add(libraryScroller, CENTER);
		libraryPanel.add(descriptionPanel, SOUTH);
		
		descriptionPanel.setLayout(new BorderLayout());
		descriptionPanel.setBorder(BorderFactory.createTitledBorder("Action Description"));
		descriptionPanel.add(descriptionLabel, CENTER);
		
		planTable.setTableHeader(null);
		planTable.setPreferredSize(new Dimension(0, 223));
		JScrollPane planScroller = new JScrollPane(planTable);
		planPanel.setLayout(new BorderLayout());
		planPanel.add(planScroller, CENTER);
		planPanel.add(planToolBar, BorderLayout.SOUTH);
		
		planToolBar.add(removeActionButton);
		planToolBar.add(upButton);
		planToolBar.add(downButton);
		planToolBar.add(templateEuclideanBtn);
		planToolBar.add(templateSphericalBtn);
		planToolBar.add(new FeedbackAction(this, "minimal", appName));
		
		actionOptionsPanel.setLayout(new BorderLayout());
		
		definitionPanel.setLayout(new GridLayout(1, 3));
		definitionPanel.add(libraryPanel);
		definitionPanel.add(planPanel);
		definitionPanel.add(actionOptionsPanel);
		
		calculationPanel.setLayout(new GridBagLayout());
		calculationPanel.setBorder(BorderFactory.createTitledBorder("Calculation"));
		GridBagConstraints c = new GridBagConstraints();
		c.gridwidth = GridBagConstraints.RELATIVE;
		c.weightx = 1.0;
		c.anchor = WEST;
		c.fill = NONE;
		calculationPanel.add(constructButton, c);
		c.gridwidth = REMAINDER;
		c.anchor = GridBagConstraints.EAST;
		calculationPanel.add(viewSurfaceButton, c);
		c.fill = HORIZONTAL;
		c.anchor = WEST;
		calculationPanel.add(statusLabel, c);
		calculationPanel.add(progressBar, c);
	
		libraryTable.setRowSelectionAllowed(true);
		libraryTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		libraryTable.getColumnModel().setColumnSelectionAllowed(false);
		libraryTable.getColumnModel().getColumn(0).setMaxWidth(20);
		libraryTable.setDragEnabled(true);
		libraryTable.setTransferHandler(new ActionTransferHandlerExport());
		libraryTable.getSelectionModel().addListSelectionListener(this);
		libraryTable.addMouseListener(this);
		
		planTable.setDragEnabled(false);
		planTable.setDropTarget(new DropTarget(planTable, new DropActionListener()));
		planTable.setRowSelectionAllowed(true);
		planTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		planTable.getColumnModel().setColumnSelectionAllowed(false);
		planTable.getColumnModel().getColumn(0).setMaxWidth(30);
		planTable.getColumnModel().getColumn(1).setMaxWidth(20);
		planTable.getSelectionModel().addListSelectionListener(this);
		
		viewSurfaceButton.addActionListener(this);
		removeActionButton.addActionListener(this);
		upButton.addActionListener(this);
		downButton.addActionListener(this);
		templateEuclideanBtn.addActionListener(this);
		templateSphericalBtn.addActionListener(this);
	}
	
	
	private class ConstructSurfaceAction extends AbstractAction{

		private static final long 
			serialVersionUID = 1L;

		public ConstructSurfaceAction(){
			putValue(Action.NAME, "Construct Surface");
			putValue(Action.SMALL_ICON, new ImageIcon(ImageHook.getImage("process.png")));
		}
		
		
		@Override
		public void actionPerformed(ActionEvent e) {
			if (creationPlan.size() == 0) {
				JOptionPane.showMessageDialog(MainWindow.this, "No Construction Plan!");
				return;
			}
			new Thread("Minimal Surface Construction"){
				@Override
				public void run() {
					HalfEdgeDataStructure<CPVertex, CPEdge, CPFace> graph = null;
					progressBar.setMaximum(creationPlan.size());
					int progress = 0;
					for (MacroAction tool : creationPlan) {
						statusLabel.setText("Current Action: " + (progress + 1) + ". " + tool.getName());
						try {
							tool.setController(controller);
							graph = tool.process(graph);
						} catch (Exception e1) {
							JOptionPane.showMessageDialog(MainWindow.this, "Exception in tool: " + tool.getName() + "\n" + e1);
							e1.printStackTrace();
							return;
						}
						if (graph == null){
							statusLabel.setText("The tool \"" + tool.getName() + "\" returned a null result.");
							return;
						}
						if (tool.getClass() == LoadToGraphEditor.class) {
							controller.setEditedGraph(graph);
							controller.fireGraphChanged();
						}
						progress++;
						progressBar.setValue(progress);
					}
					viewer.setVisible(true);
					viewer.view(graph);
					statusLabel.setText("Surface constructed");		
				}
			}.start();
		}
		
	}
	
	
	
	private class ActionTranferable implements Transferable{

		@Override
		public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
			return null;
		}

		@Override
		public DataFlavor[] getTransferDataFlavors() {
			return null;
		}

		@Override
		public boolean isDataFlavorSupported(DataFlavor flavor) {
			return false;
		}
		
		
	}
	
	
	private class ActionTransferHandlerExport extends TransferHandler{
		
		private static final long serialVersionUID = 1L;

		@Override
		public Icon getVisualRepresentation(Transferable t) {
			return actions[libraryTable.getSelectedRow()].getIcon();
		}

		@Override
		protected Transferable createTransferable(JComponent c) {
			return new ActionTranferable();
		}
		
		@Override
		public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
			return false;
		}
		
		@Override
		public void exportAsDrag(JComponent comp, InputEvent e, int action) {
			super.exportAsDrag(comp, e, action);
		}
		
		@Override
		public void exportToClipboard(JComponent comp, Clipboard clip, int action) throws IllegalStateException {
			super.exportToClipboard(comp, clip, action);
		}
		
		@Override
		protected void exportDone(JComponent source, Transferable data, int action) {
			super.exportDone(source, data, action);
		}
		
		@Override
		public int getSourceActions(JComponent c) {
			return COPY;
		}
		
	}
	
	
	private class DropActionListener implements DropTargetListener{

		@Override
		public void dragEnter(DropTargetDragEvent dtde) {}
		@Override
		public void dragExit(DropTargetEvent dte) {}
		@Override
		public void dragOver(DropTargetDragEvent dtde) {}

		@Override
		public void drop(DropTargetDropEvent dtde) {
			dtde.acceptDrop(dtde.getDropAction());
			Point p = dtde.getLocation();
			MacroAction tool = null;
			try {
				tool = actions[libraryTable.getSelectedRow()].getClass().newInstance();
			} catch (Exception e) {
				JOptionPane.showMessageDialog(MainWindow.this, e.toString());
				return;
			}
			tool.setController(controller);
			int pos = planTable.rowAtPoint(p);
			if (pos == -1)
				creationPlan.add(tool);
			else
				creationPlan.add(pos, tool);
			dtde.dropComplete(true);
			planTable.updateUI();
		}

		@Override
		public void dropActionChanged(DropTargetDragEvent dtde) {}
		
	}
	
	
	private class PlanTableModel extends AbstractTableModel{
		
		private static final long 
			serialVersionUID = 1L;
	
		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return false;
		}
		
		@Override
		public int getColumnCount() {
			return 3;
		}

		@Override
		public int getRowCount() {
			return creationPlan.size();
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			if (columnIndex == 0)
				return rowIndex + 1;
			if (columnIndex == 1)
				return creationPlan.get(rowIndex).getIcon();
			if (columnIndex == 2)
				return creationPlan.get(rowIndex).getName();
			return null;
		}
		
		@Override
		public Class<?> getColumnClass(int col) {
			if (col == 0)
				return Integer.class;
			if (col == 1)
				return Icon.class;
			if (col == 2)
				return String.class;
			return String.class;
        }
		
	}
	
	
	
	private class LibraryTableModel extends AbstractTableModel{

		private static final long 
			serialVersionUID = 1L;
		
		
		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return false;
		}
		
		
		@Override
		public int getColumnCount() {
			return 2;
		}

		@Override
		public int getRowCount() {
			return actions.length;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			if (columnIndex == 0)
				return actions[rowIndex].getIcon();
			if (columnIndex == 1)
				return actions[rowIndex].getName();
			return null;
		}
		
		@Override
		public Class<?> getColumnClass(int col) {
			if (col == 0)
				return Icon.class;
			else
				return String.class;
        }
		
	}

	
	private void updateInfos() {
		int libRow = libraryTable.getSelectedRow();
		int planRow = planTable.getSelectedRow();
		if (libRow >= 0) {
			MacroAction tool = actions[libRow];
			descriptionLabel.setText(tool.getDescription());
			descriptionLabel.repaint();
		}
		actionOptionsPanel.removeAll();
		if (planRow >= 0){
			JScrollPane scroller = new JScrollPane(creationPlan.get(planRow).getOptionPanel());
			scroller.setBorder(null);
			actionOptionsPanel.add(scroller, CENTER);
		} 
		actionOptionsPanel.updateUI();
	}


	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == viewSurfaceButton){
			viewer.setVisible(true);
		}
		if (e.getSource() == removeActionButton){
			int planRow = planTable.getSelectedRow();
			if (planRow >= 0){
				creationPlan.remove(planRow);
				actionOptionsPanel.removeAll();
				planTable.clearSelection();
				planTable.getSelectionModel().setSelectionInterval(planRow - 1, planRow - 1);
			}
			planTable.updateUI();
			actionOptionsPanel.updateUI();
		}
		if (e.getSource() == upButton){
			int planRow = planTable.getSelectedRow();
			if (planRow >= 1){
				MacroAction tool = creationPlan.remove(planRow);
				creationPlan.add(planRow - 1, tool);
				planTable.getSelectionModel().setSelectionInterval(planRow - 1, planRow - 1);		
				planTable.updateUI();
				actionOptionsPanel.updateUI();
			}
		}
		if (e.getSource() == downButton){
			int planRow = planTable.getSelectedRow();
			if (planRow < creationPlan.size() - 1){
				MacroAction tool = creationPlan.remove(planRow);
				creationPlan.add(planRow + 1, tool);
				planTable.getSelectionModel().setSelectionInterval(planRow + 1, planRow + 1);
				planTable.updateUI();
				actionOptionsPanel.updateUI();
			}
		}
		if (e.getSource() == templateEuclideanBtn) {
			creationPlan.clear();
			creationPlan.add(new LoadCombinatorics());
			creationPlan.add(new MedialPolyhedron());
			creationPlan.add(new VertexQuadSubdivide());
			creationPlan.add(new AutoCut());
			creationPlan.add(new DualizeKiteQuads());
			for (MacroAction m : creationPlan) {
				m.setController(controller);
			}
			planTable.getSelectionModel().setSelectionInterval(0, 0);
			planTable.updateUI();
			actionOptionsPanel.updateUI();
		}
		if (e.getSource() == templateSphericalBtn) {
			creationPlan.clear();
			creationPlan.add(new LoadCombinatorics());
			creationPlan.add(new BoundaryConditions());
			creationPlan.add(new SphericalCirclePattern());
			creationPlan.add(new VertexQuadSubdivide());
			creationPlan.add(new DualizeKiteQuads());
			for (MacroAction m : creationPlan) {
				m.setController(controller);
			}
			planTable.getSelectionModel().setSelectionInterval(0, 0);
			planTable.updateUI();
			actionOptionsPanel.updateUI();
		}
	}
 

	@Override
	public void valueChanged(ListSelectionEvent e) {
		updateInfos();
	}


	@Override
	public void mouseClicked(MouseEvent me) {
		if (me.getClickCount() != 2)
			return;
		MacroAction tool = null;
		try {
			tool = actions[libraryTable.getSelectedRow()].getClass().newInstance();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(MainWindow.this, e.toString());
			return;
		}
		tool.setController(controller);
		creationPlan.add(tool);
		planTable.updateUI();
	}


	@Override
	public void mouseEntered(MouseEvent e) {
		
	}


	@Override
	public void mouseExited(MouseEvent e) {
		
	}


	@Override
	public void mousePressed(MouseEvent e) {
		
	}


	@Override
	public void mouseReleased(MouseEvent e) {
		
	}
	
}
