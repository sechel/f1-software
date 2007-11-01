package circlepatterns.frontend.action;

import halfedge.frontend.action.ExtensionFileFilter;
import image.ImageHook;

import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileOutputStream;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import circlepatterns.frontend.content.euclidean.EuclideanCirclePatternView;

import com.lowagie.text.Document;
import com.lowagie.text.PageSize;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfWriter;

/**
 * Opens a file chooser and saves active polyhedron.
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 */
public class ExportPDFAction extends AbstractAction {

	private static final long 
		serialVersionUID = 1L;
	private JFileChooser 
		saveChooser = new JFileChooser();
	private Component 
		parent = null;
	private EuclideanCirclePatternView
		viewer = null;


	public ExportPDFAction(Component parent, EuclideanCirclePatternView view) {
		this.viewer = view;
		this.parent = parent;
		putValue(Action.NAME, "PDF...");
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke('E',
				InputEvent.CTRL_MASK));
		putValue(Action.LONG_DESCRIPTION, "Export PDF File");
		putValue(Action.SHORT_DESCRIPTION, "Export PDF...");
		putValue(Action.SMALL_ICON, new ImageIcon(ImageHook
				.getImage("save.png")));
		putValue(Action.MNEMONIC_KEY, KeyEvent.VK_E);

		saveChooser.setCurrentDirectory(new File(System.getProperty("user.dir") + "/data"));
		saveChooser.setDialogTitle("Export PDF File");
		saveChooser.setDialogType(JFileChooser.SAVE_DIALOG);
		saveChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		ExtensionFileFilter objFilter = new ExtensionFileFilter("pdf",
				"PDF Files");
		saveChooser.addChoosableFileFilter(objFilter);
	}

	public void actionPerformed(ActionEvent e) {
		int result = saveChooser.showSaveDialog(parent);
		if (result == JFileChooser.APPROVE_OPTION) {
			File file = saveChooser.getSelectedFile();
			if (!file.getName().toLowerCase().endsWith(".pdf"))
				file = new File(file.getAbsolutePath() + ".pdf");
			if (file.exists()) {
				int owr = JOptionPane.showConfirmDialog(parent,
						"Do you want to overwrite the file: " + file + "?");
				if (owr != JOptionPane.OK_OPTION)
					return;
			}
			try {
				FileOutputStream out = new FileOutputStream(file);
				Document doc = new Document(PageSize.A4, 50, 50, 50, 50);
				PdfWriter writer = PdfWriter.getInstance(doc, out);
				doc.open();
				PdfContentByte cb = writer.getDirectContent();
				Graphics2D g2 = cb.createGraphics(PageSize.A4.width(), PageSize.A4.height());
				viewer.paint(g2);
				g2.dispose();
				doc.close();
			} catch (Exception e1) {
				JOptionPane.showMessageDialog(parent, e1.getMessage());
			}
		}
	}

}
