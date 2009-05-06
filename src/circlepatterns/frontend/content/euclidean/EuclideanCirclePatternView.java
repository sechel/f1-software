package circlepatterns.frontend.content.euclidean;

import halfedge.HalfEdgeDataStructure;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Ellipse2D;
import java.text.DecimalFormat;
import java.util.List;

import javax.swing.JPanel;
import javax.vecmath.Point2d;

import circlepatterns.graph.CPEdge;
import circlepatterns.graph.CPFace;
import circlepatterns.graph.CPVertex;


/**
 * The circle pattern view
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 */
public class EuclideanCirclePatternView extends JPanel{

	private static final long 
		serialVersionUID = 1L;

	private MouseColtroller
		mouseController = new MouseColtroller();
	protected Color
		backgroundColor = Color.WHITE,
		circles1Color = new Color(189, 93, 112),
		circles2Color = new Color(95, 95, 230),
		vertexColor = Color.RED,
		graphColor = Color.BLACK,
		dualGraphColor = Color.GREEN;
	protected Double 
		scale = 100.0;
	protected Point
		center = new Point();
	protected boolean
		antialiasing = true,
		showVertices = false,
		showGraph = false,
		showDualGraph = false,
		showCircles = true,
		showIndices = false,
		showRadii = false;
	protected Integer
		vertexSize = 10;
	private HalfEdgeDataStructure<CPVertex, CPEdge, CPFace>
		pattern = null;

	public EuclideanCirclePatternView() {
		super(true);
		addMouseListener(mouseController);
		addMouseMotionListener(mouseController);
	}
	
	
	public void update(){
		repaint();
	}
	
	
	@Override
	public void paint(Graphics gNorm) {
		Dimension dim = getSize();
		Graphics2D g = (Graphics2D)gNorm;
		if (antialiasing)
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		else 
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
		g.setColor(backgroundColor);
		g.fillRect(0, 0, dim.width, dim.height);
		Stroke s = new BasicStroke(0.5f);
		g.setStroke(s);
		
		if (pattern == null)
			return;
		
		if (showVertices){
			for (CPVertex vertex : pattern.getVertices()){
				double x =  vertex.getXY().x * scale + dim.width / 2.0 - center.x;
				double y = -vertex.getXY().y * scale + dim.height / 2.0 - center.y;
				g.setColor(vertexColor);
				Ellipse2D circle = new Ellipse2D.Double(x - vertexSize / 2, y - vertexSize / 2, vertexSize, vertexSize);
				g.draw(circle);
				if (showIndices)
					g.drawString(String.valueOf(vertex.getIndex()), (int)x, (int)y);
			}
		}
		for (CPFace face : pattern.getFaces()){
			if (showCircles){
				List<CPEdge> b = face.getBoundary();
				if (b.size() < 3)
					continue;
//				Point2d p1 = b.get(0).getTargetVertex().getXY(); 
//				Point2d p2 = b.get(1).getTargetVertex().getXY(); 
//				Point2d p3 = b.get(2).getTargetVertex().getXY();
//				Point2d c = new Point2d();
//				double r = 0.0;
//				try{
//					r = Circles.getCircleCenterAndRadius(p1, p2, p3, c) * scale;
//				} catch (Exception e) {
//					continue;
//				}
				Point2d c = face.getXY();
				double r = face.getRadius() * scale;
				double x = (c.x * scale) + dim.width / 2.0 - r - center.x;
				double y = -(c.y * scale) + dim.height / 2.0 - r - center.y;
				if (face.getLabel())
					g.setColor(circles2Color);
				else
					g.setColor(circles1Color);
				Shape circle = new Ellipse2D.Double(x, y, r * 2.0, r * 2.0);
				if (showIndices)
					g.drawString(String.valueOf(face.getIndex()), (int) (x + r), (int) (y + r));
				if (showRadii) {
					DecimalFormat df = new DecimalFormat("0.0000");
					g.drawString(df.format(face.getRadius()), (int) (x + r), (int) (y + r));
				}
				g.draw(circle);
			}
			if (showGraph){
				CPEdge firstEdge = face.getBoundaryEdge();
				Point2d lastPoint = firstEdge.getTargetVertex().getXY();
				CPEdge edge = firstEdge.getNextEdge();
				do {
					int x1 = (int)(lastPoint.x * scale) + dim.width / 2 - center.x;
					int y1 = -(int)(lastPoint.y * scale) + dim.height / 2 - center.y;
					int x2 = (int)(edge.getTargetVertex().getXY().x * scale) + dim.width / 2 - center.x;
					int y2 = -(int)(edge.getTargetVertex().getXY().y * scale) + dim.height / 2 - center.y;
					g.setColor(graphColor);
					g.drawLine(x1, y1, x2, y2);
					if (showIndices)
						g.drawString(String.valueOf(edge.getTargetVertex().getIndex()), x2, y2);
					lastPoint = edge.getTargetVertex().getXY();
					edge = edge.getNextEdge();
				} while (edge != firstEdge.getNextEdge());
			}
			if (showDualGraph){
				for (CPEdge edge : face.getBoundary()){
					CPEdge oppositeEdge = edge.getOppositeEdge();
					if (oppositeEdge.getLeftFace() == null)
						continue;
					CPFace adjFace = oppositeEdge.getLeftFace();
					int x1 = (int)(face.getXY().x * scale) + dim.width / 2 - center.x;
					int y1 = -(int)(face.getXY().y * scale) + dim.height / 2 - center.y;
					int x2 = (int)(adjFace.getXY().x * scale) + dim.width / 2 - center.x;
					int y2 = -(int)(adjFace.getXY().y * scale) + dim.height / 2 - center.y;
					g.setColor(dualGraphColor);
					g.drawLine(x1, y1, x2, y2);
				}
			}
		}
	}


	
	private class MouseColtroller extends MouseAdapter implements MouseMotionListener{
		
		private Point
			lastMouseDrag = new Point();
		private boolean
			antialiasingState = antialiasing;
		
		public void mouseDragged(MouseEvent e) {
			center = new Point(center.x + lastMouseDrag.x - e.getX(), center.y + lastMouseDrag.y - e.getY());
			lastMouseDrag = e.getPoint();
			update();
		}


		public void mouseMoved(MouseEvent e) {}


		@Override
		public void mousePressed(MouseEvent e) {
			antialiasingState = antialiasing;
			antialiasing = false;
			lastMouseDrag = e.getPoint();	
		}
		
		@Override
		public void mouseReleased(MouseEvent e) {
			antialiasing = antialiasingState;
			update();
		}
		
	}



	public void setPattern(HalfEdgeDataStructure<CPVertex, CPEdge, CPFace> pattern) {
		this.pattern = pattern;
		repaint();
	}
	
	
	
}
