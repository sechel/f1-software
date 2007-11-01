package alexandrov.frontend.content;

import static java.awt.Font.BOLD;
import static java.awt.Font.PLAIN;
import static java.awt.GridBagConstraints.NONE;
import static java.awt.GridBagConstraints.NORTHWEST;
import static java.awt.GridBagConstraints.RELATIVE;
import static java.awt.GridBagConstraints.REMAINDER;
import image.ImageHook;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;




/**
 * The about box
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 */
@SuppressWarnings("serial")
public class AboutBox extends JDialog {

	private Icon 
		f1LogoIcon = new ImageIcon(ImageHook.getImage("pslogo.png"));
	private JLabel
		f1Logo = new JLabel(f1LogoIcon),
		title = new JLabel("Polyhedral Surfaces"),
		label1 = new JLabel("Project Leader: Prof. Alexander I. Bobenko"),
		label2 = new JLabel("Researcher: Ivan Izmestiev"),
		label3 = new JLabel("Programmer: Stefan Sechelmann");
	
	private Font
		titleFont = new Font("Arial", BOLD, 25),
		infoFont = new Font("Arial", PLAIN, 12);
	
	public AboutBox(JFrame owner){
		super(owner);
		setResizable(false);
		setModal(true);
		setTitle("About Alexandrovs Polytop Editor");
		makeLayout();
		
		if (owner != null){
			setLocation((owner.getWidth() - getWidth()) / 2, (owner.getHeight() - getHeight()) / 2);
			setLocationRelativeTo(owner);
		}
	}

	private void makeLayout() {
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = NONE;
		c.insets = new Insets(5,5,5,5);
		c.gridwidth = RELATIVE;
		c.anchor = NORTHWEST;
		c.weightx = 0;
		c.weighty = 0;
		c.gridheight = 4;
		
		add(f1Logo, c);
		c.gridheight = 1;
		title.setFont(titleFont);
		add(title, c);
		
		c.gridx = 1;
		c.gridwidth = REMAINDER;
		label1.setFont(infoFont);
		add(label1, c);
		label2.setFont(infoFont);
		add(label2, c);
		label3.setFont(infoFont);
		add(label3, c);
		
		pack();
	}
	
	public static void main(String[] args) {
		AboutBox b = new AboutBox(null);
		b.addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		b.setVisible(true);
	}
}
