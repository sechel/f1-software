package koebe.frontend.action;

import static java.awt.GridBagConstraints.BOTH;
import static java.awt.GridBagConstraints.EAST;
import static java.awt.GridBagConstraints.HORIZONTAL;
import static java.awt.GridBagConstraints.NONE;
import static java.awt.GridBagConstraints.NORTHEAST;
import static java.awt.GridBagConstraints.NORTHWEST;
import static java.awt.GridBagConstraints.RELATIVE;
import static java.awt.GridBagConstraints.REMAINDER;
import static java.awt.GridBagConstraints.SOUTHWEST;
import static java.awt.GridBagConstraints.WEST;
import image.ImageHook;

import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class ProvideFeedbackAction extends AbstractAction {

	private static final long 
		serialVersionUID = 1L;
	private FeedbackDialog
		dialog = null;
	
	public ProvideFeedbackAction(Frame parent) {
		putValue(Action.NAME, "Provide Feedback");
		putValue(Action.LONG_DESCRIPTION, "Provide feedback for the developers");
		putValue(Action.SHORT_DESCRIPTION, "Provide Feedback");
		putValue(Action.SMALL_ICON, new ImageIcon(ImageHook.getImage("feedback.png")));
		dialog = new FeedbackDialog(parent);
	}
	
	public static enum FeedbackType {
		General,
		Suggestion,
		Bug
	}
	
	
	public void actionPerformed(ActionEvent e) {
		dialog.setVisible(true);
	}

	
	protected static void commitFeedback(FeedbackDialog dialog) {
		System.out.println("ProvideFeedbackAction.commitFeedback()");
	}
	
	
	protected static class FeedbackDialog extends JDialog implements ActionListener{

		private static final long 
			serialVersionUID = 1L;
		private JTextField
			nameField = new JTextField(),
			emailField = new JTextField();
		private JComboBox
			typeComboBox = new JComboBox(FeedbackType.values());
		private JTextArea
			commentArea = new JTextArea();
		private JButton
			okButton = new JButton("Send"),
			cancelButton = new JButton("Cancel");
		
		public FeedbackDialog(Frame parent) {
			super(parent);
			setModal(true);
			setResizable(false);
			setSize(500, 300);
			setTitle("Feedback");
			setIconImage(ImageHook.getImage("feedback.png"));
			setLocationByPlatform(true);
			setLocationRelativeTo(parent);
			makeLayout();
			
			okButton.addActionListener(this);
			cancelButton.addActionListener(this);
		}
		
		private void makeLayout() {
			Font defaultFont = new Font(Font.DIALOG_INPUT, Font.PLAIN, 12);
			setFont(defaultFont);
			
			setLayout(new GridBagLayout());
			GridBagConstraints c = new GridBagConstraints();
			
			
			JPanel headerPanel = new JPanel();
			headerPanel.setBorder(BorderFactory.createRaisedBevelBorder());
			headerPanel.setLayout(new GridBagLayout());
			GridBagConstraints hc = new GridBagConstraints();
			hc.gridx = 0;
			hc.gridy = 0;
			hc.gridwidth = 1;
			hc.gridheight =1;
			hc.weightx = 1.0;
			hc.anchor = SOUTHWEST;
			hc.fill = HORIZONTAL;
			JLabel label1 = new JLabel("Feedback");
			Font font1 = new Font(Font.DIALOG_INPUT, Font.PLAIN, 25);
			label1.setFont(font1);
			headerPanel.add(label1, hc);
			
			hc.gridx = 1;
			hc.gridy = 0;
			hc.gridwidth = 1;
			hc.gridheight =2;
			hc.weightx = 0.0;
			hc.anchor = NORTHEAST;
			headerPanel.add(new JLabel(new ImageIcon(ImageHook.getImage("feedback_big.png"))), hc);
			hc.gridx = 0;
			hc.gridy = 1;
			hc.gridwidth = 1;
			hc.gridheight =1;
			hc.weightx = 1.0;
			hc.anchor = NORTHWEST;
			Font font2 = new Font(Font.DIALOG_INPUT, Font.PLAIN, 10);
			JLabel label2 = new JLabel("  Koebe Polyhedron");
			label2.setFont(font2);
			headerPanel.add(label2, hc);
			
			c.fill = HORIZONTAL;
			c.insets = new Insets(2,4,2,4);
			c.weightx = 1.0;
			c.weighty = 0.0;
			c.gridwidth = REMAINDER;
			c.anchor = WEST;
			add(headerPanel, c);

			add(new JSeparator());
			
			c.gridwidth = RELATIVE;
			c.weighty = 0.0;
			c.weightx = 0.0;
			JLabel nameLabel = new JLabel("Your Name:");
			nameLabel.setFont(defaultFont);
			add(nameLabel, c);
			c.gridwidth = REMAINDER;
			c.weightx = 1.0;
			add(nameField, c);
			
			c.gridwidth = RELATIVE;
			c.weightx = 0.0;
			JLabel eMailLabel = new JLabel("eMail:");
			eMailLabel.setFont(defaultFont);
			add(eMailLabel, c);
			c.gridwidth = REMAINDER;
			c.weightx = 1.0;
			add(emailField, c);
			
			c.gridwidth = RELATIVE;
			c.weightx = 0.0;
			JLabel typeLabel = new JLabel("Feedback type:");
			typeLabel.setFont(defaultFont);
			add(typeLabel, c);
			c.gridwidth = REMAINDER;
			c.weightx = 1.0;
			typeComboBox.setFont(defaultFont);
			add(typeComboBox, c);
	
			JLabel commentLabel = new JLabel("Comment:");
			commentLabel.setFont(defaultFont);
			add(commentLabel, c);
			c.weightx = 1.0;
			c.weighty = 1.0;
			c.fill = BOTH;
			add(new JScrollPane(commentArea), c);
			
			JPanel okCancelPanel = new JPanel();
			okCancelPanel.setLayout(new GridLayout(1, 2, 8, 0));
			okButton.setFont(defaultFont);
			cancelButton.setFont(defaultFont);
			okCancelPanel.add(okButton);
			okCancelPanel.add(cancelButton);
			c.fill = NONE;
			c.anchor = EAST;
			c.weightx = 1.0;
			c.weighty = 0.0;
			add(okCancelPanel, c);
		}

		public void actionPerformed(ActionEvent e) {
			if (okButton == e.getSource()) {
				commitFeedback(this);
			}
			if (cancelButton == e.getSource()) {
				setVisible(false);
			}
		}
		
	}

	
	public static void main(String[] args) {
		FeedbackDialog dialog = new FeedbackDialog(null);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialog.setVisible(true);
	}
	
	
}
