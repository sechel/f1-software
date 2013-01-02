package alexandrov.frontend.content;

import static java.awt.GridBagConstraints.HORIZONTAL;
import static java.awt.GridBagConstraints.NONE;
import static java.awt.GridBagConstraints.RELATIVE;
import static java.awt.GridBagConstraints.REMAINDER;
import static java.awt.GridBagConstraints.WEST;

import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;

@SuppressWarnings("serial")
public class CalculationDialog extends JDialog implements ActionListener {

	private JButton 
		okBtn = new JButton("Calculate"),
		cancelBtn = new JButton("Cancel");
	private SpinnerNumberModel
		initRadiusFactorModel = new SpinnerNumberModel(2.0, 2.0, 100.0, 1.0),
		maxIterModel = new SpinnerNumberModel(100, 1, 10000, 1);
	private JSpinner 
		initRadiusSpinner = new JSpinner(initRadiusFactorModel),
		maxIterSpinner = new JSpinner(maxIterModel);
	private JTextField
		errorField = new JTextField("" + getError());
	private JLabel 
		initRadiusFactorLabel = new JLabel("Initial Radius Factor"),
		erroLabel = new JLabel("Curvature Residual"),
		maxIterLabel = new JLabel("Maximum Iterations");
	
	public enum CalculationMethod{
		SlowAndSafe,
		FastButDangerous,
		FastMultiCPU,
		Simple
	}
	
	private JComboBox
		methodCombo = new JComboBox(CalculationMethod.values());
		
	
	private int 
		result = JOptionPane.CANCEL_OPTION;
	private Double
		error = 1E-2;
	
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
		if (s == errorField){
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
			error = Double.parseDouble(errorField.getText());
			if (error <= 0)
				error = Double.MIN_VALUE;
			if (error > 1)
				error = 1.0;
		} catch (NumberFormatException nfe){}
		errorField.setText("" + getError());
	}
	
	
	public CalculationDialog(Frame parent) {
		super(parent);
		initialize();
		if (parent != null){
			setLocation((parent.getWidth() - getWidth()) / 2, (parent.getHeight() - getHeight()) / 2);
			setLocationRelativeTo(parent);
		}
	}

	private void initialize() {
		this.setSize(255, 138);
		this.makeLayout();
		this.setResizable(false);
		this.setModal(true);
		this.setTitle("Calculation");
	}

	private void makeLayout() {
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = WEST;
		c.fill = HORIZONTAL;
		c.insets = new Insets(3,3,3,3);
		
		c.gridwidth = RELATIVE;
		c.fill = NONE;
		add(initRadiusFactorLabel, c);
		c.gridwidth = REMAINDER;
		c.fill = HORIZONTAL;
		add(initRadiusSpinner, c);
		
		c.gridwidth = RELATIVE;
		add(erroLabel, c);
		c.gridwidth = REMAINDER;
		errorField.setHorizontalAlignment(SwingConstants.RIGHT);
		errorField.addFocusListener(new FieldFocusListener());
		add(errorField, c);

		c.gridwidth = RELATIVE;
		c.fill = NONE;
		add(maxIterLabel, c);
		c.gridwidth = REMAINDER;
		c.fill = HORIZONTAL;
		add(maxIterSpinner, c);
		
		c.gridwidth = RELATIVE;
		c.fill = NONE;
		add(new JLabel("Method"), c);
		c.gridwidth = REMAINDER;
		c.fill = HORIZONTAL;
		add(methodCombo, c);
		
		c.gridwidth = RELATIVE;
		c.fill = NONE;
		c.ipadx = 0;
		add(okBtn, c);
		
		c.ipadx = 20;
		add(cancelBtn, c);
		
		okBtn.addActionListener(this);
		cancelBtn.addActionListener(this);
		errorField.addActionListener(this);
		getRootPane().setDefaultButton(okBtn);
		pack();
	}

	
	public int getResult() {
		return result;
	}
	
	public Double getError(){
		if (error == null)
			error = 1E-10;
		return error;
	}
	
	public Integer getMaxIterations(){
		return maxIterModel.getNumber().intValue();
	}
	
	public CalculationMethod getMethod(){
		return (CalculationMethod)methodCombo.getSelectedItem();
	}
	
	public void setMethod(CalculationMethod method){
		methodCombo.setSelectedItem(method);
	}
	
	public double getInitialRadiusFactor() {
		return initRadiusFactorModel.getNumber().doubleValue();
	}
	
	public static void main(String[] args) {
		CalculationDialog dialog = new CalculationDialog(null);
		dialog.setVisible(true);
		System.err.println("Result: " + dialog.getResult());
		if (dialog.getResult() == JOptionPane.OK_OPTION){
			System.err.println("OK Hit");
			System.err.println("Error: " + dialog.getError());
			System.err.println("Max Iterations: " + dialog.getMaxIterations());
		}
		System.exit(0);
	}
	
} 
