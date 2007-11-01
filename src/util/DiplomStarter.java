package util;

import static java.awt.GridBagConstraints.BOTH;
import static java.awt.GridBagConstraints.REMAINDER;
import static java.awt.GridBagConstraints.WEST;
import image.ImageHook;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JSeparator;
import javax.swing.UIManager;

import alexandrov.frontend.AlexandrovsPolyhedron;

import minimalsurface.frontend.MinimalSurfaces;

import koebe.frontend.KoebesPolyhedron;

import de.javasoft.plaf.synthetica.SyntheticaStandardLookAndFeel;

public class DiplomStarter extends JFrame implements ActionListener{

	private static final long 
		serialVersionUID = 1L;
	
	private ImageIcon 
		thesisIcon = new ImageIcon(ImageHook.getImage("thesis01.png")),
		koebeIcon = new ImageIcon(ImageHook.getImage("koebeIcosahedron.png")),
		minimalIcon = new ImageIcon(ImageHook.getImage("quadMinimal01.png")),
		alexandrovIcon = new ImageIcon(ImageHook.getImage("reuleauxTrianglePolyhedron01.png")),
		titleImage = new ImageIcon(ImageHook.getImage("title01.png"));
		
	private JLabel
		titleLabel = new JLabel(titleImage);
	
	private JButton	
		viewPDFButton = new JButton("View Diploma Thesis as PDF Document", thesisIcon),
		startKoebeButton = new JButton("Start the Koebe Polyhedron Editor", koebeIcon),
		startMinimalButton = new JButton("Start the Minimal Surface Designer", minimalIcon),
		startAlexandrovButton = new JButton("Start the Alexandrov Polytope Editor", alexandrovIcon);
	
	public DiplomStarter() {
		setTitle("Stefan Sechelmann: Diploma Thesis");
		setSize(500, 700);
		
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = BOTH;
		c.anchor = WEST;
		c.insets = new Insets(5,5,5,5);
		c.weightx = 0;
		c.gridwidth = REMAINDER;
		
		add(titleLabel, c);
		add(new JSeparator(JSeparator.HORIZONTAL), c);
		add(viewPDFButton, c);
		add(startKoebeButton, c);
		add(startMinimalButton, c);
		add(startAlexandrovButton, c);
		
		viewPDFButton.addActionListener(this);
		startKoebeButton.addActionListener(this);
		startMinimalButton.addActionListener(this);
		startAlexandrovButton.addActionListener(this);
		
		setResizable(false);
		
		KoebesPolyhedron.isStandAlone = false;
		MinimalSurfaces.isStandAlone = false;
		AlexandrovsPolyhedron.isStandAlone = false;
	}
	
	public void actionPerformed(ActionEvent e) {
		Object s = e.getSource();
		if (s == viewPDFButton)
			viewPDF();
		if (s == startKoebeButton)
			KoebesPolyhedron.main(null);
		if (s == startMinimalButton)
			MinimalSurfaces.main(null);
		if (s == startAlexandrovButton)
			AlexandrovsPolyhedron.main(null);
	}
	
	
	private static void viewPDF(){
		new Thread("Acroread Starter Thread"){
			public void run() {
				try {
					File tmp = File.createTempFile("DiplomaThesis", ".pdf");
					FileOutputStream out = new FileOutputStream(tmp);
					File thesisFile = new File("DiplomaThesis.pdf");
					InputStream in = new FileInputStream(thesisFile);
					byte[] buffer = new byte[1024];
					int len = 0;
					while ((len = in.read(buffer)) != -1){
						out.write(buffer, 0, len);
					}
					out.close();
					String line;
					Process p = null;
					String os = System.getProperty("os.name");
					if (os.toLowerCase().contains("win")){
						p = Runtime.getRuntime().exec("cmd /c " + tmp.getAbsolutePath());						
					} else {
						p = Runtime.getRuntime().exec("acroread " + tmp.getAbsolutePath());
					}
					InputStreamReader isr = new InputStreamReader(p.getInputStream());
					BufferedReader input = new BufferedReader(isr);
					while ((line = input.readLine()) != null) {
						System.out.println(line);
					}
					input.close();
				} catch (Exception err) {
					err.printStackTrace();
				}
			}
		}.start();
	}
	
	static{
		try {
			SyntheticaStandardLookAndFeel.setAntiAliasEnabled(true);
			SyntheticaStandardLookAndFeel.setWindowsDecorated(false);
			SyntheticaStandardLookAndFeel.setExtendedFileChooserEnabled(true);
			SyntheticaStandardLookAndFeel.setUseSystemFileIcons(true);
			UIManager.setLookAndFeel("de.javasoft.plaf.synthetica.SyntheticaBlueSteelLookAndFeel");
		} catch (Exception e) {}
	}
	
	
	public static void main(String[] args) {
		DiplomStarter starter = new DiplomStarter();
		starter.setVisible(true);	
		starter.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

}
