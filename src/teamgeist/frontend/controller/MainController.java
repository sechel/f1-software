package teamgeist.frontend.controller;

import java.util.LinkedList;

import javax.swing.JPanel;

import teamgeist.frontend.TeamgeistView;


/**
 * The main application controller. It has several subcontrollers
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 */
public class MainController{

	private JPanel
		mainPanel = null;
	private TeamgeistView
		viewer = null;
	private ColorController
		colorController = new ColorController();
	private AppearanceController
		appearanceController = new AppearanceController();
	private LinkedList<GraphChangedListener>
		changeListeners = new LinkedList<GraphChangedListener>();
	private LinkedList<StatusChangedListener>
		statusListeners = new LinkedList<StatusChangedListener>();
	
	
	public interface GraphChangedListener{
		public void graphChanged();
	}
	
	public interface StatusChangedListener{
		public void statusChanged(String msg);
	}
	
	public boolean addGraphChangedListener(GraphChangedListener listener){
		return changeListeners.add(listener);
	}
	
	public boolean removeGraphChangedListener(GraphChangedListener listener){
		return changeListeners.remove(listener);
	}
	
	public void removeAllGraphChangedListener(){
		changeListeners.clear();
	}
	
	
	public boolean addStatusChangedListener(StatusChangedListener listener){
		return statusListeners.add(listener);
	}
	
	public boolean removeStatusChangedListener(StatusChangedListener listener){
		return statusListeners.remove(listener);
	}
	
	public void removeAllStatusChangedListener(){
		statusListeners.clear();
	}
	
	
	public void fireGraphChanged(){
		for (GraphChangedListener l : changeListeners)
			l.graphChanged();
	}
	
	
	public MainController(){
	}
	
	
	public ColorController getColorController() {
		return colorController;
	}


	public JPanel getMainPanel() {
		return mainPanel;
	}


	public AppearanceController getAppearanceController() {
		return appearanceController;
	}

	
	public void setStatus(String msg){
		for (StatusChangedListener l : statusListeners)
			l.statusChanged(msg);
	}

	public void setMainPanel(JPanel mainPanel) {
		this.mainPanel = mainPanel;
	}

	public TeamgeistView getViewer() {
		return viewer;
	}

	public void setViewer(TeamgeistView viewer) {
		this.viewer = viewer;
	}
	
}
