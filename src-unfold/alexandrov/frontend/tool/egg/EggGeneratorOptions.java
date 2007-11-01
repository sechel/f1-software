package alexandrov.frontend.tool.egg;

import static java.awt.GridBagConstraints.HORIZONTAL;
import static java.awt.GridBagConstraints.NONE;
import static java.awt.GridBagConstraints.RELATIVE;
import static java.awt.GridBagConstraints.REMAINDER;
import static java.awt.GridBagConstraints.WEST;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

@SuppressWarnings("serial")
public class EggGeneratorOptions extends JDialog implements ActionListener {

	private JButton 
		okBtn = new JButton("Generate"),
		cancelBtn = new JButton("Cancel");
	private SpinnerNumberModel
		segmentsModel = new SpinnerNumberModel(3, 1, 10000, 1);
	private JSpinner 
		segmentsSpinner = new JSpinner(segmentsModel);
	private SpinnerNumberModel
		resModel = new SpinnerNumberModel(3, 3, 10000, 1);
	private JSpinner 
		resSpinner = new JSpinner(resModel);
	private SpinnerNumberModel
		eccModel = new SpinnerNumberModel(0.5, 0, 0.99, 0.01);
	private JSpinner 
		eccSpinner = new JSpinner(eccModel);
	private JLabel 
		resLabel = new JLabel("Divisions"),
		maxIterLabel = new JLabel("Height Segments"),
		eccLabel = new JLabel("Eccentricity");
	
	private int 
		result = JOptionPane.CANCEL_OPTION;
	private Double
		scale = 1.0;
	
	public void actionPerformed(ActionEvent e) {
		Object s = e.getSource();
		if (s == okBtn){
			result = JOptionPane.OK_OPTION;
			setVisible(false);
		}
		if (s == cancelBtn){
			result = JOptionPane.CANCEL_OPTION;
			setVisible(false);	
		}
	}

	public EggGeneratorOptions(JFrame owner, String title) {
		super(owner);
		setTitle(title);
		initialize();
		if (owner != null){
			setLocation((owner.getWidth() - getWidth()) / 2, (owner.getHeight() - getHeight()) / 2);
			setLocationRelativeTo(owner);
		}
	}

	private void initialize() {
		this.setSize(255, 138);
		this.makeLayout();
		this.setResizable(false);
		this.setModal(true);
	}

	private void makeLayout() {
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		
		c.fill = NONE; 
		c.anchor = WEST;
		c.gridwidth = RELATIVE;
		c.insets = new Insets(3,3,3,3);
		
		c.gridwidth = RELATIVE;
		c.fill = NONE;
		add(maxIterLabel, c);	
		c.gridwidth = REMAINDER;
		c.fill = HORIZONTAL;
		add(segmentsSpinner, c);
		
		c.gridwidth = RELATIVE;
		c.fill = NONE;
		add(resLabel, c);	
		c.gridwidth = REMAINDER;
		c.fill = HORIZONTAL;
		add(resSpinner, c);
		
		c.gridwidth = RELATIVE;
		c.fill = NONE;
		add(eccLabel, c);	
		c.gridwidth = REMAINDER;
		c.fill = HORIZONTAL;
		add(eccSpinner, c);
		
		c.gridwidth = RELATIVE;
		c.fill = NONE;
		c.ipadx = 0;
		add(okBtn, c);
		
		c.ipadx = 20;
		add(cancelBtn, c);
		
		okBtn.addActionListener(this);
		cancelBtn.addActionListener(this);
		getRootPane().setDefaultButton(okBtn);
		pack();
	}

	
	public int getResult() {
		return result;
	}
	
	public Double getScale(){
		if (scale == null)
			scale = 1.0;
		return scale;
	}
	
	public Integer getSegments(){
		return segmentsModel.getNumber().intValue();
	}
	
	public Integer getRes(){
		return resModel.getNumber().intValue();
	}
	
	public double getEccentricity() {
		return eccModel.getNumber().doubleValue();
	}
	
	public static void main(String[] args) {
		EggGeneratorOptions app = new EggGeneratorOptions(null, "egg");
		app.setVisible(true);
	}

} 
