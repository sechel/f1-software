package circlepatterns.frontend.content;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Rectangle2D;

import javax.swing.JDialog;
import javax.swing.JPanel;

import circlepatterns.frontend.CPTestSuite;



/**
 * A swing container class 
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 */
public class ShrinkPanel extends JPanel implements ComponentListener{
	
	private static final long 
		serialVersionUID = 1L;
	private int 
		minHeight = 0,
		width = 170,
		inset = 3,
		inner_inset = 5,
		name_space = 6,
		name_box_height = 15,
		name_box_middle = inset + name_box_height / 2;
	private Font
		font = new Font("Arial", Font.PLAIN, 12); 
	private boolean
		shrink = false,
		floated = false;
//	private ShrinkPanel
//		this_ref = this;
	private boolean 
		debug = false;
	private Color
		header_color = new Color(0.7f, 0.7f, 0.7f);
	private JDialog 
		floated_panel = new JDialog();
	private boolean
		fillSpace = false;
	protected ShrinkPanelContainer
		parentContainer = null;
	private Dimension
		floatingSize = null;
	private Point
		floatingLocation = null;
	
	private JPanel 
		content_panel = new JPanel(),
		upper_panel = new JPanel(),
		right_panel = new JPanel(),
		left_panel = new JPanel(),
		lower_panel = new JPanel();
	
	
	private String name = "A Shrink Panel";
	
	
	public ShrinkPanel(String name, int min_height, boolean fill) {
	    this(name);
	    this.minHeight = min_height;
	    this.fillSpace = fill;
	}
	
	public ShrinkPanel(String name) {
		this.name = name;
		setBackground(Color.GRAY);
		super.setLayout(new BorderLayout());
		
		
		if (debug){
			upper_panel.setBackground(Color.ORANGE);	
			right_panel.setBackground(Color.ORANGE);
			left_panel.setBackground(Color.ORANGE);
			lower_panel.setBackground(Color.ORANGE);
			content_panel.setBackground(Color.YELLOW);
		}

		Dimension upper_dim = new Dimension(width, name_box_height + inset + inner_inset);
		upper_panel.setPreferredSize(upper_dim);
		super.add(upper_panel, BorderLayout.NORTH);
		Dimension side_dim = new Dimension(inset + inner_inset, 10);
		right_panel.setPreferredSize(side_dim);
		left_panel.setPreferredSize(side_dim);
		Dimension low_dim = new Dimension(width, inset + inner_inset);
		lower_panel.setPreferredSize(low_dim);
		super.add(content_panel, BorderLayout.CENTER);
		super.add(right_panel, BorderLayout.EAST);
		super.add(left_panel, BorderLayout.WEST);
		super.add(lower_panel, BorderLayout.SOUTH);
		addComponentListener(this);
	
		upper_panel.addMouseListener(new MouseAdapter(){
			@Override
			public void mouseClicked(MouseEvent m) {
				Dimension dim = getSize();
				if (!floated && dim.width - name_space - name_box_height <= m.getX() && 
				        m.getX() <= dim.width - name_space - 8 &&
						inset + 4 <= m.getY() && m.getY() <= inset + name_box_height - 4){
				    setFloating(true);
				} else
				if (name_space <= m.getX() && m.getX() <= dim.width - name_space &&
					inset <= m.getY() && m.getY() <= inset + name_box_height)
					shrink();
			}
		});
	}
	

	@Override
	public void setLayout(LayoutManager arg0) {
		if (content_panel == null)
			return;
		content_panel.setLayout(arg0);
	}
	

	@Override
	public Component add(Component arg0) {
		content_panel.add(arg0);
		return arg0;
	}
	

	@Override
	public void add(Component arg0, Object arg1) {
		content_panel.add(arg0, arg1);
	}
	

	@Override
	public Component add(Component arg0, int arg1) {
		return content_panel.add(arg0, arg1);
	}
	

	@Override
	public void add(Component arg0, Object arg1, int arg2) {
		content_panel.add(arg0, arg1, arg2);
	}
	
	@Override
	public Component add(String arg0, Component arg1) {
		return content_panel.add(arg0, arg1);
	}


    @Override
	public void remove(Component arg0) {
        content_panel.remove(arg0);
    }


    @Override
	public void removeAll() {
        content_panel.removeAll();
    }

    
	public void updateShrinkPanel(){
		if (!(getParent() instanceof ShrinkPanelContainer) && getParent() != null)
			width = getParent().getSize().width;
		Dimension new_size = getSize();
		if (!shrink){
			new_size = content_panel.getMinimumSize();
			new_size.width = width;
			new_size.height = new_size.height + inset * 3 + inner_inset + name_box_height;
			if (new_size.height < minHeight)
			    new_size.height = minHeight + inset * 3 + inner_inset + name_box_height;
		} else {
			new_size.width = width;
			new_size.height = inset * 2 + name_box_height;
		}
		
		if (!fillSpace){
			setMaximumSize(new_size);
			setMinimumSize(new_size);
		} else {
		    if (!shrink){
		        setMaximumSize(null);
		        setMinimumSize(null);
		    } else {
			    setMaximumSize(new_size);
			    setMinimumSize(new_size);	        
		    }
		}
		setPreferredSize(new_size);	
		revalidate();	
		repaint();
	}
	
	
	
	
	private void setFloating(boolean floating){
	    floated = floating;
	    if (floating){
	        if (!shrink)
	            shrink();
			super.remove(content_panel);
			floated_panel = new JDialog(CPTestSuite.getMainFrame(), false);
			floated_panel.setTitle(name);
			floated_panel.addWindowListener(new WindowAdapter(){
		        @Override
				public void windowClosing(WindowEvent arg0) {
		            super.windowClosed(arg0);
		            shrink();
		        } 
			});
			floated_panel.getContentPane().add(content_panel);
			if (floatingSize == null)
			    floatingSize = getUnshrinkedSize();
			floated_panel.setSize(floatingSize);
			floated_panel.setResizable(true);
			if (floatingLocation == null)
			    floatingLocation = this.getLocationOnScreen();
			floated_panel.setLocation(floatingLocation);
	        floated_panel.setVisible(true);
	    } else {
	        if (floated_panel.isVisible()){
		        floatingSize = floated_panel.getSize();
		        floatingLocation = floated_panel.getLocationOnScreen();
		        floated_panel.remove(content_panel);
		        floated_panel.setVisible(false);
	        }
			super.add(content_panel, BorderLayout.CENTER);
	    }
	}
	
	
	
	private Dimension getUnshrinkedSize(){
		Dimension result = content_panel.getMinimumSize();
		result.width = width;
		result.height = result.height + inset * 3 + inner_inset + name_box_height;
		if (result.height < minHeight)
		    result.height = minHeight + inset * 3 + inner_inset + name_box_height;	 
		return result;
	}
	
	
	private void shrink(){
		Dimension new_size = getSize();
		if (shrink){
		    new_size = getUnshrinkedSize();
			setFloating(false);
		} else {
			new_size.width = width;
			new_size.height = inset * 2 + name_box_height;
		}
		shrink = !shrink;
		if (!fillSpace){
		    setMaximumSize(new_size);
		    setMinimumSize(new_size);
		} else {
		    if (!shrink){
		        setMaximumSize(null);
		        setMinimumSize(null);
		    } else {
			    setMaximumSize(new_size);
			    setMinimumSize(new_size);	        
		    }
		}
		setPreferredSize(new_size);
		revalidate();
	}
	
	
	public void setShrinked(boolean shrink){
	    if (this.shrink == shrink) 
	        return;
	    else
	        shrink();
	}
	
	

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		Graphics2D g2D = (Graphics2D)g;
		g2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		Dimension dim = getSize();
		// the outline and name
		g.setColor(Color.BLACK);
		g.draw3DRect(inset, name_box_middle, dim.width - inset * 2, dim.height - name_box_middle - inset, true);
		g.setColor(header_color);
		g.fillRect(name_space, inset, dim.width - name_space * 2, name_box_height);
		g.setColor(Color.BLACK);
		g.draw3DRect(name_space, inset, dim.width - name_space * 2, name_box_height, true);
		g.setFont(font);
		Rectangle2D bounds = font.getStringBounds(name, g2D.getFontRenderContext());
		g.drawString(name, name_space + (dim.width - name_space * 2) / 2 - (int)bounds.getWidth() / 2, inset + name_box_height - 3);
		if (shrink){
			g.drawString("+", name_space + inset, inset + name_box_height - 3);
		} else {
			g.drawString("-", name_space + inset, inset + name_box_height - 3);			
		}
		//floate aera
		g.setColor(Color.RED);
		if (floated)
			g.fillRect(dim.width - name_space - name_box_height, inset + 4, name_box_height - 8, name_box_height - 8);	
		g.setColor(Color.BLACK);
		g.draw3DRect(dim.width - name_space - name_box_height, inset + 4, name_box_height - 8, name_box_height - 8, true);
	}




	@Override
	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
		updateShrinkPanel();
	}




	public void componentResized(ComponentEvent arg0) {
		updateShrinkPanel();	
	}


	public void componentMoved(ComponentEvent arg0) {

	}


	public void componentShown(ComponentEvent arg0) {
	
	}


	public void componentHidden(ComponentEvent arg0) {

	}
    /**
     * @return Returns the header_color.
     */
    public Color getHeaderColor() {
        return header_color;
    }
    /**
     * @param header_color The header_color to set.
     */
    public void setHeaderColor(Color header_color) {
        this.header_color = header_color;
    }
    /**
     * @return Returns the minHeight.
     */
    public int getMinHeight() {
        return minHeight;
    }
    /**
     * @param minHeight The minHeight to set.
     */
    public void setMinHeight(int minHeight) {
        this.minHeight = minHeight;
    }

    public ShrinkPanelContainer getParentContainer() {
        return parentContainer;
    }

}
