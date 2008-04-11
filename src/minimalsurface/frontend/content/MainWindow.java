package minimalsurface.frontend.content;

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
import minimalsurface.frontend.macro.ConsistentStripSubdivide;
import minimalsurface.frontend.macro.CutEars;
import minimalsurface.frontend.macro.Dualize;
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


public class MainWindow extends JFrame implements ListSelectionListener, ActionListener, MouseListener{

	private static final long 
		serialVersionUID = 1L;
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
			new CutEars(),
			new AutoCut(), 
			new Dualize(),
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
		viewSurfaceButton = new JButton("View Minimal Surface", new ImageIcon(ImageHook.getImage("surface.png"))),
		removeActionButton = new JButton(new ImageIcon(ImageHook.getImage("delete.png"))),
		upButton = new JButton(new ImageIcon(ImageHook.getImage("up.png"))),
		downButton = new JButton(new ImageIcon(ImageHook.getImage("down.png")));
	private JProgressBar
		progressBar = new JProgressBar(0, 100);
	private JLabel
		statusLabel = new JLabel("Progress Status:");

	

	public MainWindow() {
		setSize(800, 400);
		setResizable(false);
		setTitle("Minimal Surface Construction");
		setLocationRelativeTo(controller.getMainFrame());
		setLocationByPlatform(false);
	
		makeLayout();
		
		controller.setMainFrame(this);
		controller.setMainPanel((JPanel)getContentPane());
		viewer = new MinimalSurfaceViewer(this, controller);
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
	}
	
	
	private class ConstructSurfaceAction extends AbstractAction{

		private static final long 
			serialVersionUID = 1L;

		public ConstructSurfaceAction(){
			putValue(Action.NAME, "Construct Surface");
			putValue(Action.SMALL_ICON, new ImageIcon(ImageHook.getImage("process.png")));
		}
		
		
		public void actionPerformed(ActionEvent e) {
			if (creationPlan.size() == 0) {
				JOptionPane.showMessageDialog(MainWindow.this, "No Construction Plan!");
				return;
			}
			new Thread("Minimal Surface Construction"){
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

		public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
			return null;
		}

		public DataFlavor[] getTransferDataFlavors() {
			return null;
		}

		public boolean isDataFlavorSupported(DataFlavor flavor) {
			return false;
		}
		
		
	}
	
	
	private class ActionTransferHandlerExport extends TransferHandler{
		
		private static final long serialVersionUID = 1L;

		public Icon getVisualRepresentation(Transferable t) {
			return actions[libraryTable.getSelectedRow()].getIcon();
		}

		protected Transferable createTransferable(JComponent c) {
			return new ActionTranferable();
		}
		
		public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
			return false;
		}
		
		public void exportAsDrag(JComponent comp, InputEvent e, int action) {
			super.exportAsDrag(comp, e, action);
		}
		
		public void exportToClipboard(JComponent comp, Clipboard clip, int action) throws IllegalStateException {
			super.exportToClipboard(comp, clip, action);
		}
		
		protected void exportDone(JComponent source, Transferable data, int action) {
			super.exportDone(source, data, action);
		}
		
		public int getSourceActions(JComponent c) {
			return COPY;
		}
		
	}
	
	
	private class DropActionListener implements DropTargetListener{

		public void dragEnter(DropTargetDragEvent dtde) {}
		public void dragExit(DropTargetEvent dte) {}
		public void dragOver(DropTargetDragEvent dtde) {}

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

		public void dropActionChanged(DropTargetDragEvent dtde) {}
		
	}
	
	
	private class PlanTableModel extends AbstractTableModel{
		
		private static final long 
			serialVersionUID = 1L;
	
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return false;
		}
		
		public int getColumnCount() {
			return 3;
		}

		public int getRowCount() {
			return creationPlan.size();
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			if (columnIndex == 0)
				return rowIndex + 1;
			if (columnIndex == 1)
				return creationPlan.get(rowIndex).getIcon();
			if (columnIndex == 2)
				return creationPlan.get(rowIndex).getName();
			return null;
		}
		
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
		
		
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return false;
		}
		
		
		public int getColumnCount() {
			return 2;
		}

		public int getRowCount() {
			return actions.length;
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			if (columnIndex == 0)
				return actions[rowIndex].getIcon();
			if (columnIndex == 1)
				return actions[rowIndex].getName();
			return null;
		}
		
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
	}


	public void valueChanged(ListSelectionEvent e) {
		updateInfos();
	}


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


	public void mouseEntered(MouseEvent e) {
		
	}


	public void mouseExited(MouseEvent e) {
		
	}


	public void mousePressed(MouseEvent e) {
		
	}


	public void mouseReleased(MouseEvent e) {
		
	}
	
}
