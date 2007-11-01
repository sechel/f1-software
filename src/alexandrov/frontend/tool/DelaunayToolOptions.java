package alexandrov.frontend.tool;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;


/**
 * The option panel for the Delaunay tool
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 */
public class DelaunayToolOptions extends JPanel implements ActionListener{

	private static final long 
		serialVersionUID = 1L;
	private DelaunayTool
		tool = null;
	
	private JButton makeDelaunayBtn = null;

	/**
	 * This is the default constructor
	 */
	public DelaunayToolOptions(DelaunayTool tool) {
		super();
		this.tool = tool;
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridwidth = 1;
		gridBagConstraints.gridy = 0;
		this.setLayout(new GridBagLayout());
		this.setSize(new java.awt.Dimension(220,108));
		this.add(getMakeDelaunayBtn(), gridBagConstraints);
		getMakeDelaunayBtn().addActionListener(this);
	}

	/**
	 * This method initializes makeDelaunayBtn	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getMakeDelaunayBtn() {
		if (makeDelaunayBtn == null) {
			makeDelaunayBtn = new JButton();
			makeDelaunayBtn.setText("Make Delaunay");
		}
		return makeDelaunayBtn;
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == getMakeDelaunayBtn())
			tool.makeDelaunay();
	}

}  //  @jve:decl-index=0:visual-constraint="258,79"
