package alexandrov.frontend.tool.deform;

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
import javax.swing.SwingConstants;

@SuppressWarnings("serial")
public class CircleTriangleDeformOptions extends JDialog implements ActionListener {

	private JButton 
		okBtn = new JButton("Generate"),
		cancelBtn = new JButton("Cancel");
	private SpinnerNumberModel
		segmentsModel = new SpinnerNumberModel(4, 2, 10000, 2);
	private JSpinner 
		segmentsSpinner = new JSpinner(segmentsModel);
	private JTextField
		scaleField = new JTextField("" + getScale());
	private JLabel 
		erroLabel = new JLabel("Scale"),
		maxIterLabel = new JLabel("Side Segments");
	
	private int 
		result = JOptionPane.CANCEL_OPTION;
	private Double
		scale = 1.0;
	
	@Override
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
		if (s == scaleField){
			verifyError();
		}
	}

	private class FieldFocusListener extends FocusAdapter{
		@Override
		public void focusLost(FocusEvent e) {
			verifyError();
		}
	}
	
	
	private void verifyError(){
		try {
			scale = Double.parseDouble(scaleField.getText());
			if (scale <= 0)
				scale = Double.MIN_VALUE;
			if (scale > 1000.0)
				scale = 1.0;
		} catch (NumberFormatException nfe){}
		scaleField.setText("" + getScale());
	}
	
	
	public CircleTriangleDeformOptions(JFrame owner, String title) {
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
		add(erroLabel, c);
		
		c.gridwidth = REMAINDER;
		c.fill = HORIZONTAL;
		scaleField.setHorizontalAlignment(SwingConstants.RIGHT);
		scaleField.addFocusListener(new FieldFocusListener());
		add(scaleField, c);
		
		c.gridwidth = RELATIVE;
		c.fill = NONE;
		add(maxIterLabel, c);	
		c.gridwidth = REMAINDER;
		c.fill = HORIZONTAL;
		add(segmentsSpinner, c);
		
	
		c.gridwidth = RELATIVE;
		c.fill = NONE;
		c.ipadx = 0;
		add(okBtn, c);
		
		c.ipadx = 20;
		add(cancelBtn, c);
		
		okBtn.addActionListener(this);
		cancelBtn.addActionListener(this);
		scaleField.addActionListener(this);
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

} 
