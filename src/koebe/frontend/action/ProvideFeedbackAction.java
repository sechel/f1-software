package koebe.frontend.action;

import static java.awt.GridBagConstraints.BOTH;
import static java.awt.GridBagConstraints.EAST;
import static java.awt.GridBagConstraints.HORIZONTAL;
import static java.awt.GridBagConstraints.NONE;
import static java.awt.GridBagConstraints.NORTHEAST;
import static java.awt.GridBagConstraints.NORTHWEST;
import static java.awt.GridBagConstraints.RELATIVE;
import static java.awt.GridBagConstraints.REMAINDER;
import static java.awt.GridBagConstraints.WEST;
import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.INFORMATION_MESSAGE;
import image.ImageHook;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
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
	private String
		product = null,
		productName = null;
	private Frame
		parent = null;
	private static PreparedStatement
		insertFeedbackStmnt = null;
	
	public ProvideFeedbackAction(Frame parent, String product, String productName) {
		putValue(Action.NAME, "Provide Feedback");
		putValue(Action.LONG_DESCRIPTION, "Provide feedback for the developers");
		putValue(Action.SHORT_DESCRIPTION, "Provide Feedback");
		putValue(Action.SMALL_ICON, new ImageIcon(ImageHook.getImage("feedback.png")));
		this.product = product;
		this.productName = productName;
		this.parent = parent;
		this.dialog = new FeedbackDialog(parent);
	}
	
	public static enum FeedbackType {
		General,
		Suggestion,
		Bug
	}
	
	
	public void actionPerformed(ActionEvent e) {
		dialog.setVisible(true);
	}

	
	protected void commitFeedback(final FeedbackDialog d) {
		Runnable committer = new Runnable() {
			public void run() {
				d.setStatus("Connectiong to feedback server theta.math.tu-berlin.de...");
				try {
					Class.forName("com.mysql.jdbc.Driver");
				} catch (ClassNotFoundException e) {
					JOptionPane.showMessageDialog(parent, "JDBC Driver could not be loaded!", "Error", ERROR_MESSAGE);
					return;
				}
				String url = "jdbc:mysql://theta.math.tu-berlin.de:3306/feedback";
				Connection con = null;
				try {
					con = DriverManager.getConnection(url, "feedback", "feedback123");
				} catch (SQLException e) {
					JOptionPane.showMessageDialog(parent, "Can't connect to feedback server!", "Error", ERROR_MESSAGE);
					return;
				}
				d.setStatus("Committing feedback entry...");
				try {
					String name = d.nameField.getText();
					String eMail = d.emailField.getText();
					String comment = d.commentArea.getText();
					FeedbackType type = FeedbackType.values()[ d.typeComboBox.getSelectedIndex()];
					insertFeedbackEntry(con, name, eMail, comment, type, product);
				} catch (SQLException e) {
					JOptionPane.showMessageDialog(parent, "Can't commit feedback!", "Error", ERROR_MESSAGE);
					return;
				}
				d.setStatus("Done.");
				JOptionPane.showMessageDialog(parent, "Thank you for your feedback", "Feedback", INFORMATION_MESSAGE);
				dialog.setVisible(false);

			}
		};
		Thread t = new Thread(committer, "Feedback commit thread");
		t.start();
	}
	
	
	protected void insertFeedbackEntry(Connection c, String name, String eMail, String comment, FeedbackType type, String product) throws SQLException {
		if (insertFeedbackStmnt == null || insertFeedbackStmnt.getConnection() != c) {
			String com = "INSERT INTO main (`name`, `email`, `comment`, `type`, `product`) values (?, ?, ?, ?, ?)";
			insertFeedbackStmnt = c.prepareStatement(com);
		}
		insertFeedbackStmnt.setString(1, name);
		insertFeedbackStmnt.setString(2, eMail);
		insertFeedbackStmnt.setString(3, comment);
		insertFeedbackStmnt.setInt(4, type.ordinal());
		insertFeedbackStmnt.setString(5, product);
		insertFeedbackStmnt.executeUpdate();
	}
	
	
	
	protected class FeedbackDialog extends JDialog implements ActionListener{

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
		private JTextField
			statusField = new JTextField("Thank you for your feedback");
		
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
			hc.anchor = NORTHWEST;
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
			JLabel label2 = new JLabel("  " + productName);
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
			
			statusField.setEditable(false);
			statusField.setFont(defaultFont);
			statusField.setPreferredSize(new Dimension(100, 16));
			c.fill = HORIZONTAL;
			c.insets = new Insets(2, 2, 1, 2);
			add(statusField, c);
		}

		public void actionPerformed(ActionEvent e) {
			if (okButton == e.getSource()) {
				commitFeedback(this);
			}
			if (cancelButton == e.getSource()) {
				setVisible(false);
			}
		}
		
		public void setStatus(String status) {
			statusField.setText(status);
			statusField.repaint();
		}
		
	}

	
	
	public static void main(String[] args) {
		ProvideFeedbackAction pfa = new ProvideFeedbackAction(null, "koebe", "Koebe Polyhedron Editor");
		pfa.actionPerformed(null);
		System.exit(0);
	}
	

}
