package alexandrov.frontend.content;

import static java.awt.GridBagConstraints.HORIZONTAL;
import static java.awt.GridBagConstraints.RELATIVE;
import static java.awt.GridBagConstraints.REMAINDER;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

@SuppressWarnings("serial")
public class LengthEditDialog extends JDialog implements ActionListener{

	private SpinnerNumberModel
		lengthModel = new SpinnerNumberModel(0.0, 0.0, 1000.0, 0.01);
	private JSpinner
		spinner = new JSpinner(lengthModel);
	private JButton
		okButton = new JButton("OK"),
		cancelButton = new JButton("Cancel");
	private int
		result = JOptionPane.CANCEL_OPTION;
	
	private LengthEditDialog(Component owner, double init){
		super((JFrame)null, true);
		setSize(200, 100);
		if (owner != null){
			setLocation((owner.getWidth() - getWidth()) / 2, (owner.getHeight() - getHeight()) / 2);
			setLocationRelativeTo(owner);
		}
		setTitle("New Length");
		setResizable(false);
		
		getRootPane().setDefaultButton(okButton);
		lengthModel.setValue(init);
		makeLayout();
	}

	private void makeLayout() {
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = HORIZONTAL;
		c.gridwidth = RELATIVE;
		c.insets = new Insets(3,3,3,3);
		c.weightx = 0;
		
		add(new JLabel("Edge Length"), c);
		c.gridwidth = REMAINDER;
		add(spinner, c);
		c.gridwidth = RELATIVE;
		add(okButton, c);
		add(cancelButton, c);
		
		okButton.addActionListener(this);
		cancelButton.addActionListener(this);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == okButton)
			result = JOptionPane.OK_OPTION;
		setVisible(false);
	}

	
	public static double showEdgeLengthDialog(Component owner, double init){
		LengthEditDialog dialog = new LengthEditDialog(owner, init);
		dialog.setVisible(true);
		if (dialog.result == JOptionPane.OK_OPTION)
			return dialog.lengthModel.getNumber().doubleValue();
		else
			return -1;
	}

}
