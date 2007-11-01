package circlepatterns.frontend.content;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import circlepatterns.frontend.action.GenerateSquareGridTopology;



/**
 * The panel for generator tools
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 */
@SuppressWarnings("serial")
public class GeneratorShrinker extends ShrinkPanel implements ChangeListener{

	private JPanel
		squareGridPanel = new JPanel();
	
	private GenerateSquareGridTopology
		squareGridAction = new GenerateSquareGridTopology();
	
	private double
		maxEps = Math.PI / 4;
	private SpinnerNumberModel
		widthModel = new SpinnerNumberModel(squareGridAction.getWidth().intValue(), 2, 100000, 1),
		heightModel = new SpinnerNumberModel(squareGridAction.getHeight().intValue(), 2, 100000, 1),
		eps1Model = new SpinnerNumberModel(squareGridAction.getEps1().doubleValue(), -maxEps, maxEps, 0.01),
		eps2Model = new SpinnerNumberModel(squareGridAction.getEps2().doubleValue(), -maxEps, maxEps, 0.01);
	private JSpinner
		widthSpinner = new JSpinner(widthModel),
		heightSpinner = new JSpinner(heightModel),
		eps1Spinner = new JSpinner(eps1Model),
		eps2Spinner = new JSpinner(eps2Model);
	
	public GeneratorShrinker() {
		super("Topology Generator");
		
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1;
		c.insets = new Insets(2, 0, 2, 0);
		c.gridwidth = GridBagConstraints.REMAINDER;
		add(squareGridPanel, c);
		
		squareGridPanel.setBorder(BorderFactory.createTitledBorder("Square Grid"));
		squareGridPanel.setLayout(new GridBagLayout());
		c.gridwidth = GridBagConstraints.RELATIVE;
		squareGridPanel.add(new JLabel("Grid Width"), c);
		c.gridwidth = GridBagConstraints.REMAINDER;
		squareGridPanel.add(widthSpinner, c);
		c.gridwidth = GridBagConstraints.RELATIVE;
		squareGridPanel.add(new JLabel("Grid Height"), c);
		c.gridwidth = GridBagConstraints.REMAINDER;
		squareGridPanel.add(heightSpinner, c);		
		
		c.gridwidth = GridBagConstraints.RELATIVE;
		squareGridPanel.add(new JLabel("Eps Vertical"), c);
		c.gridwidth = GridBagConstraints.REMAINDER;
		squareGridPanel.add(eps1Spinner, c);		
		c.gridwidth = GridBagConstraints.RELATIVE;
		squareGridPanel.add(new JLabel("Eps Horizontal"), c);
		c.gridwidth = GridBagConstraints.REMAINDER;
		squareGridPanel.add(eps2Spinner, c);		
		squareGridPanel.add(new JButton(squareGridAction), c);
		
		widthSpinner.addChangeListener(this);
		heightSpinner.addChangeListener(this);
		eps1Spinner.addChangeListener(this);
		eps2Spinner.addChangeListener(this);
	}

	public void stateChanged(ChangeEvent e) {
		if (e.getSource() == widthSpinner)
			squareGridAction.setWidth(widthModel.getNumber().intValue());
		if (e.getSource() == heightSpinner)
			squareGridAction.setHeight(heightModel.getNumber().intValue());
		if (e.getSource() == eps1Spinner)
			squareGridAction.setEps1(eps1Model.getNumber().doubleValue());
		if (e.getSource() == eps2Spinner)
			squareGridAction.setEps2(eps2Model.getNumber().doubleValue());
	}
	
	
}
