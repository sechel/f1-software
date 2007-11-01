package alexandrov.frontend.tool;

import static java.awt.GridBagConstraints.BOTH;
import static java.awt.GridBagConstraints.REMAINDER;

import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;


/**
 * The report which is shown after randomization 
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 */
@SuppressWarnings("serial")
public class ReportDialog extends JDialog {

	private JTextArea
		reportArea = new JTextArea();
	private JScrollPane
		textScroller = new JScrollPane(reportArea);
	
	public ReportDialog(Frame parent){
		super(parent);
		makeLayout();
	}

	public void setReport(StringBuffer report){
		reportArea.setText(report.toString());
	}
	
	
	private void makeLayout() {
		setTitle("Random Check Report");
		setSize(500, 400);
		setModal(true);
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = BOTH;
		c.gridwidth = REMAINDER;
		c.weightx = 1;
		c.weighty = 1;
		add(textScroller, c);
	}
	
	
}
