package halfedge.frontend.controller;

import halfedge.Node;
import halfedge.frontend.content.MultiNodeOptionsPanel;
import halfedge.frontend.content.NodeOptionsPanel;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;

import circlepatterns.frontend.content.ShrinkPanel;


/**
 * The node controller holds the selected node or a list of
 * selected nodes.
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 */
public class NodeController {

	private ShrinkPanel
		nodeOptionsPanel = new ShrinkPanel("Node Options");
	private ArrayList<Node<?, ?, ?>>
		selectedNodes = new ArrayList<Node<?, ?, ?>>();

	
	public NodeController(){
		showOptions((Node<?, ?, ?>)null);
	}
	
	
	public ArrayList<Node<?, ?, ?>> getSelectedNodes() {
		return selectedNodes;
	}


	public boolean isNodeSelected(Node<?, ?, ?> node){
		return selectedNodes.contains(node);
	}
	
	
	public boolean selectNodeExclusiv(Node<?, ?, ?> node){
		selectedNodes.clear();
		return selectedNodes.add(node);
	}
	
	public boolean selectNode(Node<?, ?, ?> node){
		return selectedNodes.add(node);
	}
	
	public boolean selectNodes(Collection<Node<?, ?, ?>> nodes){
		return selectedNodes.addAll(nodes);
	}
	

	public boolean unSelectNode(Node<?, ?, ?> node){
		return selectedNodes.remove(node);
	}

	public boolean unSelectNodes(Collection<Node<?, ?, ?>> nodes){
		return selectedNodes.remove(nodes);
	}	
	
	public void unselectAll(){
		selectedNodes.clear();
	}
	
	public ShrinkPanel getNodeOptionsPanel() {
		return nodeOptionsPanel;
	}


	public void showOptions(Node<?, ?, ?> node){
		nodeOptionsPanel.removeAll();
		if (node != null){
			JPanel panel = new NodeOptionsPanel(node);
			nodeOptionsPanel.setLayout(new BorderLayout());
			nodeOptionsPanel.add(panel, BorderLayout.CENTER);
		} else {
			nodeOptionsPanel.setLayout(new FlowLayout());
			nodeOptionsPanel.add(new JLabel("No Node Selected"));
		}
		nodeOptionsPanel.updateShrinkPanel();
	}
	
	
	public void showOptions(List<Node<?, ?, ?>> nodeList){
		JPanel panel = new MultiNodeOptionsPanel(nodeList);
		nodeOptionsPanel.removeAll();
		nodeOptionsPanel.setLayout(new BorderLayout());
		nodeOptionsPanel.add(panel, BorderLayout.CENTER);
		nodeOptionsPanel.updateShrinkPanel();
	}

	
}
