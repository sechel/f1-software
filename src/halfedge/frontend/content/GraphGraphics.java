package halfedge.frontend.content;

import halfedge.Edge;
import halfedge.Face;
import halfedge.HalfEdgeDataStructure;
import halfedge.Vertex;
import halfedge.decorations.HasXY;
import halfedge.frontend.controller.MainController;

import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.vecmath.Point2d;



/**
 * A wrapper for the Java2d Graphics object
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 */
public class GraphGraphics{

	private EditPanel<?, ?, ?>
		panel = null;
	Graphics2D 
		g = null;
	private Ellipse2D 
		vertexCircle = new Ellipse2D.Double(0, 0 , 10, 10);
	
	public GraphGraphics(EditPanel<?, ?, ?> hepanel, Graphics2D g){
		this.g = g;
		this.panel = hepanel;
	}
	
	
	public 		
	<
		InV extends Vertex<InV, InE, InF> & HasXY,
		InE extends Edge<InV, InE, InF>,
		InF extends Face<InV, InE, InF>
	> void drawGraph(HalfEdgeDataStructure<InV, InE, InF> graph, MainController<InV, InE, InF> controller){
		
		if (controller.usesFaces()){
			for (InF f : graph.getFaces()){
				InE e = f.getBoundaryEdge();
				if (e == null)
					continue;
				LinkedList<Point2d> pList = new LinkedList<Point2d>();
				boolean firstEdge = true;
				for (InE edge = e; firstEdge || e != edge; edge = edge.getNextEdge()){
					if (edge == null)
						break;
					pList.add(edge.getTargetVertex().getXY());
					firstEdge = false;
				}
				if (controller.getNodeController().isNodeSelected(f))
					g.setColor(controller.getColorController().getSelectColor());
				else
					g.setColor(controller.getColorController().getFaceColor());
				fillFace(pList);
			}
		}
		
		Stroke edgeStroke = new BasicStroke((float)controller.getAppearanceController().getLineWidth());
		g.setStroke(edgeStroke);
		for (InE e : graph.getPositiveEdges()){
			if (controller.getNodeController().isNodeSelected(e) || controller.getNodeController().isNodeSelected(e.getOppositeEdge()))
				g.setColor(controller.getColorController().getSelectColor());
			else
				g.setColor(controller.getColorController().getEdgeColor());
			Point2d s = e.getOppositeEdge().getTargetVertex().getXY();
			Point2d t = e.getTargetVertex().getXY();
			drawEdge(s, t);
			if (controller.getAppearanceController().isShowEdgeIndices()){
				g.setFont(controller.getFontController().getIndexFont());
				g.setColor(controller.getColorController().getIndexColor());
				Point p1 = toViewCoord(e.getTargetVertex().getXY());
				Point p2 = toViewCoord(e.getOppositeEdge().getTargetVertex().getXY());
				g.drawString(e.getIndex() + ", " + e.getOppositeEdge().getIndex(), (p1.x + p2.x) / 2, (p1.y + p2.y) / 2);
			}
		}
		
		
		for (InV v : graph.getVertices()){
			if (controller.getNodeController().isNodeSelected(v))
				g.setColor(controller.getColorController().getSelectColor());
			else
				g.setColor(controller.getColorController().getVertexColor());
			fillVertex(v.getXY());
			g.setColor(controller.getColorController().getEdgeColor());
			drawVertex(v.getXY());
			if (controller.getAppearanceController().isShowVertexIndices()){
				g.setFont(controller.getFontController().getIndexFont());
				g.setColor(controller.getColorController().getIndexColor());
				Point p = toViewCoord(v.getXY());
				g.drawString(v.getIndex() + "", p.x, p.y);
			}
		}
	}
	
	
	public void drawGrid(int width, int height, int scale){
		Dimension dim = g.getClipBounds().getSize();
		float y = -0.5f;
		for (int i = 0; i <= height; i++){
			int screenY = toViewCoord(new Point2d(0, y * scale)).y;
			g.drawLine(0, screenY, dim.width, screenY);
			y += 1.0 / height;
		}
		float x = -0.5f;
		for (int j = 0; j <= width; j++) {
			int screenX = toViewCoord(new Point2d(x * scale, 0)).x;
			g.drawLine(screenX, 0, screenX, dim.height);	
			x += 1.0 / width;
		}
	}
	
	
	
	public Point toViewCoord(Point2d point){
		int x = (int)Math.round(point.x * panel.scale - panel.center.x);
		int y = (int)Math.round(point.y * panel.scale - panel.center.y);
		return new Point(x, y);
	}
	
	
	public Point2d toGraphCoord(Point point){
		double x = (point.x + panel.center.x) / panel.scale;
		double y = (point.y + panel.center.y) / panel.scale;
		return new Point2d(x, y);
	}
	
	
	public void drawVertex(Point2d point){
		Point p = toViewCoord(point);
		vertexCircle.setFrame(p.x - 5, p.y - 5, 10, 10);
		g.draw(vertexCircle);
	}
	
	public void fillVertex(Point2d point){
		Point p = toViewCoord(point);
		vertexCircle.setFrame(p.x - 5, p.y - 5, 10, 10);
		g.fill(vertexCircle);
	}
	
	public void drawEdge(Point2d start, Point2d end){
		Point s = toViewCoord(start);
		Point e = toViewCoord(end);
		g.drawLine(s.x, s.y, e.x, e.y);
	}
	
	public void fillFace(List<Point2d> pList){
		GeneralPath path = makePath(pList);
		g.fill(path);
	}
	
	public void drawFace(List<Point2d> pList){
		GeneralPath path = makePath(pList);
		g.draw(path);
	}

	
	public GeneralPath makePath(List<Point2d> pList){
		GeneralPath poly = new GeneralPath();
		if (pList.isEmpty())
			return poly;
		Iterator<Point2d> it = pList.iterator();
		Point2d p1 = it.next();
		Point2d first = p1;
		while (it.hasNext()){
			Point2d p2 = it.next();
			Point vp1 = toViewCoord(p1);
			Point vp2 = toViewCoord(p2);
			Line2D line = new Line2D.Double(vp1.getX(), vp1.getY(), vp2.getX(), vp2.getY());
			poly.append(line, true);
			p1 = p2;
		}
		Point vp1 = toViewCoord(p1);
		Point vfirst = toViewCoord(first);
		Line2D line = new Line2D.Double(vp1.getX(), vp1.getY(), vfirst.getX(), vfirst.getY());
		poly.append(line, true);
		return poly;
	}
	
	
	public Graphics2D getGraphics(){
		return g;
	}
	
	
	
}
