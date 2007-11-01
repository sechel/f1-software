package circlepatterns.frontend.content;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;

/**
 * A container class which can stack ShrinkPanels and scroll them
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 */
public class ShrinkPanelContainer extends JScrollPane implements MouseMotionListener, MouseListener, MouseWheelListener, ComponentListener{
	
	private static final long 
		serialVersionUID = 1L;
	private JPanel 
		content = new JPanel();
	private int last_drag = 0;
	private int width = 170;
	private boolean fill = false;
	
	
	public ShrinkPanelContainer(int width){
		super(VERTICAL_SCROLLBAR_NEVER, HORIZONTAL_SCROLLBAR_NEVER);
		content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
		setPreferredSize(new Dimension(width + 2, 10));
		setViewportView(content);
		setWheelScrollingEnabled(true);
		setBorder(null);
		
		addComponentListener(this);
		addMouseListener(this);
		addMouseMotionListener(this);
		content.addMouseMotionListener(this);
		content.addMouseListener(this);
		this.width = width;
	}
	
	
    public Component add(Component arg0) {
        if (arg0 instanceof ShrinkPanel){
            addShrinkPanel((ShrinkPanel) arg0);
            return arg0;
        } else {
            return content.add(arg0);
        }
    }

    public Component add(ShrinkPanel panel) {
        addShrinkPanel(panel);
        return panel;
    }    
    
	public void addShrinkPanel(ShrinkPanel panel){
		panel.setWidth(width);
		content.add(panel);
		panel.doLayout();
		panel.removeMouseWheelListener(this);
		panel.removeMouseMotionListener(this);
		panel.removeMouseListener(this);
		panel.addMouseWheelListener(this);
		panel.addMouseMotionListener(this);
		panel.addMouseListener(this);
		Component[] components = panel.getComponents();
		for (int i = 0; i < components.length; i++) {
            Component component = components[i];
            component.removeMouseListener(this);
            component.removeMouseMotionListener(this);
            component.removeMouseWheelListener(this);
            component.addMouseListener(this);
            component.addMouseMotionListener(this);
            component.addMouseWheelListener(this);
        }
		panel.parentContainer = this;
	}

	public void removeShrinkPanel(ShrinkPanel panel){
	    content.remove(panel);
	}

	public void removeAll(){
	    content.removeAll();
	}
	
	
	public void mouseDragged(MouseEvent e) {
		JScrollBar vert = getVerticalScrollBar();
		int schroll_pos = vert.getValue();
		vert.setValue(schroll_pos - (e.getY() - last_drag));
		last_drag = e.getY() - (e.getY() - last_drag);
	}


	public void mouseMoved(MouseEvent arg0) {
		
	}


	public void mouseClicked(MouseEvent arg0) {
	
	}


	public void mousePressed(MouseEvent arg0) {
		last_drag = arg0.getY();
	}

	public void mouseReleased(MouseEvent arg0) {
	}


	public void mouseEntered(MouseEvent arg0) {
	
	}


	public void mouseExited(MouseEvent arg0) {

	}



    public int getWidth() {
        return width;
    }

    
    public void setWidth(int width) {
        this.width = width;
		setPreferredSize(new Dimension(width + 2, 10));
        Component[] comps = getComponents();
        for (int i = 0; i < comps.length; i++) {
            Component component = comps[i];
            if (component instanceof ShrinkPanel){
                ShrinkPanel panel = (ShrinkPanel)component;
                panel.setWidth(width);
                panel.doLayout();
            }
        }
    }


    public void mouseWheelMoved(MouseWheelEvent m) {
        int dx = -m.getUnitsToScroll() * 5;
		JScrollBar vert = getVerticalScrollBar();
		int schroll_pos = vert.getValue();
		vert.setValue(schroll_pos - dx);
		last_drag = dx;
    }
    
    /**
     * @return Returns the fill.
     */
    public boolean isFilling() {
        return fill;
    }
    /**
     * @param fill The fill to set.
     */
    public void setFilling(boolean fill) {
        this.fill = fill;
    }


    public void componentResized(ComponentEvent arg0) {
        if (fill){
			Component[] components = content.getComponents();
			for (int i = 0; i < components.length; i++) {
	            Component component = components[i];
	            if (component instanceof ShrinkPanel){
	                Dimension size = component.getSize();
	                size.height = getHeight() / components.length;
	                ((ShrinkPanel)component).setPreferredSize(size);
	            }
	        }       
        }
    }


    public void componentMoved(ComponentEvent arg0) {
    }


    public void componentShown(ComponentEvent arg0) {
    }


    public void componentHidden(ComponentEvent arg0) {
    }


    public JPanel getContent() {
        return content;
    }


    public void setContent(JPanel content) {
        this.content = content;
    }
}
