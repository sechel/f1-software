package halfedge.frontend.content;

import static halfedge.frontend.graphtool.EditOperation.CANCEL;
import static halfedge.frontend.graphtool.EditOperation.DRAG_BEGIN;
import static halfedge.frontend.graphtool.EditOperation.DRAG_END;
import static halfedge.frontend.graphtool.EditOperation.DRAG_TO;
import static halfedge.frontend.graphtool.EditOperation.KEY_TYPED;
import static halfedge.frontend.graphtool.EditOperation.MOUSE_POS;
import static halfedge.frontend.graphtool.EditOperation.SELECT_POSITION;
import halfedge.Edge;
import halfedge.Face;
import halfedge.Vertex;
import halfedge.decorations.HasXY;
import halfedge.frontend.controller.MainController;
import halfedge.frontend.graphtool.EditOperation;
import halfedge.frontend.graphtool.EditOperationException;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import util.debug.DBGTracer;
import circlepatterns.frontend.content.ShrinkPanel;


/**
 * The editor panel
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 */
@SuppressWarnings("serial")
public class EditPanel 
<
	V extends Vertex<V, E, F> & HasXY,
	E extends Edge<V, E, F>, 
	F extends Face<V, E, F>
> extends JPanel implements MouseListener, MouseMotionListener, MouseWheelListener, KeyListener{

	protected MainController<V, E, F>
		controller = null;
	protected boolean
		drawGrid = true;
	protected double 
		scale = 1,
		gridwidth = 20;
	protected Point
		center = new Point();
	protected GraphGraphics
		graphics = null;
	private InternalTool<V, E, F>
		internalTool = new InternalTool<V, E, F>(this);
	
	public EditPanel(MainController<V, E, F> controller) {
		super(true);
		this.controller = controller;
		internalTool.setController(controller);
		
		addMouseListener(this);
		addMouseMotionListener(this);
		addMouseWheelListener(this);
		addKeyListener(this);
		graphics = new GraphGraphics(this, (Graphics2D)getGraphics());
	}
	
	
	
	@Override
	public void paint(Graphics g) {
		Graphics2D g2d = (Graphics2D)g;
		graphics.g = g2d;
		
		if (controller.getAppearanceController().isAntialiasing())
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		else 
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
		g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
		
		Dimension dim = getSize();
		
		g2d.setBackground(controller.getColorController().getBackgroundColor());
		g2d.clearRect(0, 0, dim.width, dim.height);
	
		if (drawGrid){
			g.setColor(controller.getColorController().getGridColor());
			graphics.drawGrid(20, 20, 5 * 400);
		}
		
		try {
			graphics.drawGraph(controller.getEditedGraph(), controller);
		} catch (Exception e) {
			DBGTracer.stackTrace(e);
			System.err.println("Error drawing graph");
		}
		
		paintActionHelpers(graphics);
	}

	
	public void encompass(){
		internalTool.encompass();
	}
	
	
	public void paintActionHelpers(GraphGraphics g){
		controller.getToolController().getActiveTool().paint(g);
		internalTool.paint(g);
	}
	

	private void commitOperation(boolean commit){
		if (commit){
			controller.getToolController().getActiveTool().commitEdit(controller.getEditedGraph());
			controller.fireGraphChanged();
		}
	}
	
	
	private void showErrorMessage(String message){
		JOptionPane.showMessageDialog(this, message, "Action Error", JOptionPane.ERROR_MESSAGE);
	}
	

	@Override
	public void mouseClicked(MouseEvent e) {
		EditOperation opInternal = null;
		EditOperation opAction = null;
		if (SwingUtilities.isLeftMouseButton(e)){
			opInternal = SELECT_POSITION;
			opAction = SELECT_POSITION;
		} else {
			opInternal = CANCEL;
			opAction = CANCEL;
		}
		opInternal.mouseEvent = e;
		opAction.mouseEvent = e;
		opInternal.mousePosition = graphics.toGraphCoord(e.getPoint());
		opAction.mousePosition.set(opInternal.mousePosition);
		opInternal.mouseModifiersEx = e.getModifiersEx();
		opAction.mouseModifiersEx = e.getModifiersEx();
		try {
			if (!internalTool.processEditOperation(opInternal))
				commitOperation(controller.getToolController().getActiveTool().processEditOperation(opAction));
		} catch (EditOperationException e1) {
			showErrorMessage(e1.getMessage());
		}
		if (controller.getToolController().getActiveTool().needsRepaint() || internalTool.needsRepaint())
			repaint();
	}



	@Override
	public void mousePressed(MouseEvent e) {
		EditOperation op = DRAG_BEGIN;
		op.mouseEvent = e;
		op.mousePosition = graphics.toGraphCoord(e.getPoint());
		op.mouseModifiersEx = e.getModifiersEx();
		try {
			if (!internalTool.processEditOperation(op))
				commitOperation(controller.getToolController().getActiveTool().processEditOperation(op));
		} catch (EditOperationException e1) {
			showErrorMessage(e1.getMessage());
		}
		if (controller.getToolController().getActiveTool().needsRepaint() || internalTool.needsRepaint())
			repaint();
	}



	@Override
	public void mouseReleased(MouseEvent e) {
		EditOperation op = DRAG_END;
		op.mouseEvent = e;
		op.mousePosition = graphics.toGraphCoord(e.getPoint());
		op.mouseModifiersEx = e.getModifiersEx();
		try {
			if (!internalTool.processEditOperation(op))
				commitOperation(controller.getToolController().getActiveTool().processEditOperation(op));
		} catch (EditOperationException e1) {
			showErrorMessage(e1.getMessage());
		}
		if (controller.getToolController().getActiveTool().needsRepaint() || internalTool.needsRepaint())
			repaint();
	}



	@Override
	public void mouseEntered(MouseEvent e) {

	}



	@Override
	public void mouseExited(MouseEvent e) {
		
	}



	@Override
	public void mouseDragged(MouseEvent e) {
		EditOperation op = DRAG_TO;
		op.mouseEvent = e;
		op.mousePosition = graphics.toGraphCoord(e.getPoint());
		op.mouseModifiersEx = e.getModifiersEx();
		try {
			if (!internalTool.processEditOperation(op))
				commitOperation(controller.getToolController().getActiveTool().processEditOperation(op));
		} catch (EditOperationException e1) {
			showErrorMessage(e1.getMessage());
		}
		if (controller.getToolController().getActiveTool().needsRepaint() || internalTool.needsRepaint())
			repaint();
	}



	@Override
	public void mouseMoved(MouseEvent e) {
		EditOperation op = MOUSE_POS;
		op.mouseEvent = e;
		op.mousePosition = graphics.toGraphCoord(e.getPoint());
		op.mouseModifiersEx = e.getModifiersEx();
		try {
			if (!internalTool.processEditOperation(op))
				commitOperation(controller.getToolController().getActiveTool().processEditOperation(op));
		} catch (EditOperationException e1) {
			showErrorMessage(e1.getMessage());
		}
		if (controller.getToolController().getActiveTool().needsRepaint() || internalTool.needsRepaint())
			repaint();
	}
	
	

	@Override
	public void keyTyped(KeyEvent e) {
		EditOperation op = KEY_TYPED;
		op.keyEvent = e;
		try {
			if (!internalTool.processEditOperation(op))
				commitOperation(controller.getToolController().getActiveTool().processEditOperation(op));
		} catch (EditOperationException e1) {
			showErrorMessage(e1.getMessage());
		}
		if (controller.getToolController().getActiveTool().needsRepaint() || internalTool.needsRepaint())
			repaint();
	}



	@Override
	public void keyPressed(KeyEvent e) {
		EditOperation op = KEY_TYPED;
		op.keyEvent = e;
		try {
			if (!internalTool.processEditOperation(op))
				commitOperation(controller.getToolController().getActiveTool().processEditOperation(op));
		} catch (EditOperationException e1) {
			showErrorMessage(e1.getMessage());
		}
		if (controller.getToolController().getActiveTool().needsRepaint() || internalTool.needsRepaint())
			repaint();
	}



	@Override
	public void keyReleased(KeyEvent e) {
		EditOperation op = KEY_TYPED;
		op.keyEvent = e;
		try {
			if (!internalTool.processEditOperation(op))
				commitOperation(controller.getToolController().getActiveTool().processEditOperation(op));
		} catch (EditOperationException e1) {
			showErrorMessage(e1.getMessage());
		}
		if (controller.getToolController().getActiveTool().needsRepaint() || internalTool.needsRepaint())
			repaint();
	}


	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		if (e.getWheelRotation() > 0)
			scale *= 1 + e.getWheelRotation() / 10.0;
		else
			scale /= 1 + -e.getWheelRotation() / 10.0;
		repaint();
	}
	
	
	
	
	public ShrinkPanel getOptionPanel(){
		return new OptionPanel();
	}
	
	
	private class OptionPanel extends ShrinkPanel implements ActionListener{

		private JCheckBox
			antialiasingChecker = new JCheckBox("Antialiasing", controller.getAppearanceController().isAntialiasing()),
			vertexIndicesChecker = new JCheckBox("Show Vertex Indices", controller.getAppearanceController().isShowVertexIndices()),
			edgeIndicesChecker = new JCheckBox("Show Edge Indides", controller.getAppearanceController().isShowEdgeIndices());
		
		
		public OptionPanel() {
			super("View Options");
			setLayout(new GridBagLayout());
			GridBagConstraints c = new GridBagConstraints();
			c.fill = GridBagConstraints.HORIZONTAL;
			c.weightx = 1;
			c.anchor = GridBagConstraints.WEST;
			
			c.gridwidth = GridBagConstraints.REMAINDER;
			add(antialiasingChecker, c);
			add(vertexIndicesChecker, c);
			add(edgeIndicesChecker, c);
			
			antialiasingChecker.addActionListener(this);
			vertexIndicesChecker.addActionListener(this);
			edgeIndicesChecker.addActionListener(this);
		}


		@Override
		public void actionPerformed(ActionEvent e) {
			Object s = e.getSource();
			if (antialiasingChecker == s)
				controller.getAppearanceController().setAntialiasing(antialiasingChecker.isSelected());
			if (vertexIndicesChecker == s)
				controller.getAppearanceController().setShowVertexIndices(vertexIndicesChecker.isSelected());
			if (edgeIndicesChecker == s)
				controller.getAppearanceController().setShowEdgeIndices(edgeIndicesChecker.isSelected());
			EditPanel.this.repaint();
		}
		
	}


	public boolean isDrawGrid() {
		return drawGrid;
	}



	public void setDrawGrid(boolean drawGrid) {
		this.drawGrid = drawGrid;
	}

	
}
