package minimalsurface.frontend.macro;

import static java.awt.GridBagConstraints.RELATIVE;
import static java.awt.GridBagConstraints.REMAINDER;
import static java.awt.GridBagConstraints.WEST;
import halfedge.HalfEdgeDataStructure;
import halfedge.frontend.action.ExtensionFileFilter;
import halfedge.generator.SquareGridGenerator;
import halfedge.io.HESerializableReader;
import image.ImageHook;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import minimalsurface.frontend.content.GraphEditor;
import circlepatterns.graph.CPEdge;
import circlepatterns.graph.CPFace;
import circlepatterns.graph.CPVertex;

public class LoadCombinatorics extends MacroAction {

	private enum CreateMode {
		Predefined,
		FromFile,
		FromEditor
	}
	
	private enum Predefined{
		CubeLattice,
		QuadMesh
	}
	
	private int 
		quadULines = 10,
		quadVLines = 10;
	
	protected Icon 
		icon = new ImageIcon(ImageHook.getImage("combinatorics.png"));
	private CreateMode
		createMode = CreateMode.Predefined;
	private Predefined
		predefined = Predefined.CubeLattice;
	private File
		graphFile = null;
	private GraphEditor
		graphEditor = null;
	
	
	@Override
	public String getName() {
		return "Load Combinatorics";
	}

	@Override
	public HalfEdgeDataStructure<CPVertex, CPEdge, CPFace> process(
			HalfEdgeDataStructure<CPVertex, CPEdge, CPFace> graph) throws Exception {
		switch (createMode){
			case Predefined:
				HESerializableReader<CPVertex, CPEdge, CPFace> reader = null;
				InputStream in = null;
				switch (predefined){
					case CubeLattice:
						in = getClass().getResourceAsStream("predefined/wuerfel.heds");
						reader = new HESerializableReader<CPVertex, CPEdge, CPFace>(in);
						graph = reader.readHalfEdgeDataStructure();
						break;
					case QuadMesh:
//						in = getClass().getResourceAsStream("predefined/quadMesh.heds");
						graph = SquareGridGenerator.generate(quadULines, quadVLines, CPVertex.class, CPEdge.class, CPFace.class);
						break;
				}
				break;
			case FromFile:
				if (graphFile == null)
					return null;
				in = new FileInputStream(graphFile);
				reader = new HESerializableReader<CPVertex, CPEdge, CPFace>(in);
				graph = reader.readHalfEdgeDataStructure();
				break;
			case FromEditor:	
				graph = new HalfEdgeDataStructure<CPVertex, CPEdge, CPFace>(getController().getEditedGraph());
				break;
		}
		return graph;
	}

	@Override
	public Icon getIcon() {
		return icon;
	}
	
	
	public GraphEditor getGraphEditor() {
		if (graphEditor == null) {
			graphEditor = new GraphEditor(getController());
		}
		return graphEditor;
	}
	
	@Override
	public JPanel getOptionPanel() {
		if (optionPanel == null) {
			optionPanel = new OptionPanel();
		}
		return optionPanel;
	}
	
	
	private class OptionPanel extends JPanel implements ActionListener, ChangeListener{

		private static final long 
			serialVersionUID = 1L;
		private ImageIcon
			openIcon = new ImageIcon(ImageHook.getImage("open.gif"));
		private JButton
			loadFromFileButton = new JButton("Load From File", openIcon),
			showEditorButton = new JButton("Show Graph Editor");
		private JPanel
			predefinedPanel = new JPanel(),
			fromFilePanel = new JPanel(),
			fromEditorPanel = new JPanel();
		private JRadioButton
			modePredefinedRadio = new JRadioButton("Load Predefined", true),
			modeFromFileRadio = new JRadioButton("Load From File"),
			modeFromEdtor = new JRadioButton("Get From Editor"),
			predefinedCubeButton = new JRadioButton("Cube", true),
			predefinedQuadMeshButton = new JRadioButton("Quad");
		private SpinnerNumberModel
			quadULinesModel = new SpinnerNumberModel(quadULines, 2, 100, 1),
			quadVLinesModel = new SpinnerNumberModel(quadVLines, 2, 100, 1);
		private JSpinner
			quadULinesSpinner = new JSpinner(quadULinesModel),
			quadVLinesSpinner = new JSpinner(quadVLinesModel);
		private JTextField
			activeFileField = new JTextField();
		private JFileChooser
			fileChooser = new JFileChooser("data");
		private JLabel
			xLabel = new JLabel("X");
		
		public OptionPanel() {
			setLayout(new GridBagLayout());
			GridBagConstraints c = new GridBagConstraints();
			c.gridwidth = GridBagConstraints.REMAINDER;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.weightx = 1.0;
			c.weighty = 1.0;
			c.insets = new Insets(0,2,2,2);
			
			add(modePredefinedRadio, c);
			add(predefinedPanel, c);
			add(new JSeparator(), c);
			add(modeFromFileRadio, c);
			add(fromFilePanel, c);
			add(new JSeparator(), c);
			add(modeFromEdtor, c);
			add(fromEditorPanel, c);
			
			c.insets = new Insets(0,10,2,2);
			
			predefinedPanel.setLayout(new GridBagLayout());
			c.gridwidth = 4;
			c.gridy = 0;
			c.anchor = WEST;
			predefinedPanel.add(predefinedCubeButton, c);
			c.gridwidth = 1;
			c.gridy = 1;
			predefinedPanel.add(predefinedQuadMeshButton, c);
			predefinedPanel.add(quadULinesSpinner, c);
			predefinedPanel.add(xLabel, c);
			predefinedPanel.add(quadVLinesSpinner, c);
			
			fromFilePanel.setLayout(new GridBagLayout());
			c.gridwidth = REMAINDER;
			c.gridy = RELATIVE;
			fromFilePanel.add(loadFromFileButton, c);
			fromFilePanel.add(activeFileField, c);
			activeFileField.setEditable(false);
			activeFileField.setText("- no active file -");
			
			fromEditorPanel.setLayout(new GridBagLayout());
			c.gridwidth = REMAINDER;
			fromEditorPanel.add(showEditorButton, c);
			
			
			ButtonGroup modeGroup = new ButtonGroup();
			modeGroup.add(modeFromEdtor);
			modeGroup.add(modeFromFileRadio);
			modeGroup.add(modePredefinedRadio);
			
			ButtonGroup predefineGroup = new ButtonGroup();
			predefineGroup.add(predefinedCubeButton);
			predefineGroup.add(predefinedQuadMeshButton);
			
			modeFromEdtor.addActionListener(this);
			modeFromFileRadio.addActionListener(this);
			modePredefinedRadio.addActionListener(this);
			loadFromFileButton.addActionListener(this);
			showEditorButton.addActionListener(this);
			predefinedCubeButton.addActionListener(this);
			predefinedQuadMeshButton.addActionListener(this);
			quadULinesSpinner.addChangeListener(this);
			quadVLinesSpinner.addChangeListener(this);
			fileChooser.addChoosableFileFilter(new ExtensionFileFilter("heds", "HalfEdgeDataStructure File"));
			
			updateStates();
		}

		private void updateStates(){
			boolean pre = modePredefinedRadio.isSelected();
			boolean edit = modeFromEdtor.isSelected();
			boolean file = modeFromFileRadio.isSelected();
			boolean quad = predefinedQuadMeshButton.isSelected();
			predefinedPanel.setEnabled(pre);
			fromEditorPanel.setEnabled(edit);
			fromFilePanel.setEnabled(file);
			predefinedCubeButton.setEnabled(pre);
			predefinedQuadMeshButton.setEnabled(pre);
			loadFromFileButton.setEnabled(file);
			activeFileField.setEnabled(file);
//			showEditorButton.setEnabled(edit);
			quadULinesSpinner.setEnabled(pre && quad);
			quadVLinesSpinner.setEnabled(pre && quad);
			xLabel.setEnabled(pre && quad);
		}
		
		
		@Override
		public void actionPerformed(ActionEvent e) {
			Object s = e.getSource();
			if (s == modePredefinedRadio)
				createMode = CreateMode.Predefined;
			if (s == modeFromEdtor)
				createMode = CreateMode.FromEditor;
			if (s == modeFromFileRadio)
				createMode = CreateMode.FromFile;
			if (s == predefinedCubeButton)
				predefined = Predefined.CubeLattice;
			if (s == predefinedQuadMeshButton)
				predefined = Predefined.QuadMesh;
			if (s == loadFromFileButton){
				int res = fileChooser.showOpenDialog(this.getParent());
				if (res != JFileChooser.CANCEL_OPTION){
					File selFile = fileChooser.getSelectedFile();
					if (selFile != null){
						graphFile = selFile;
						activeFileField.setText(graphFile.getName());
	
					}
				}
			}
			if (s == showEditorButton)
				getGraphEditor().setVisible(true);
			updateStates();
		}

		@Override
		public void stateChanged(ChangeEvent e) {
			if (e.getSource() == quadULinesSpinner)
				quadULines = quadULinesModel.getNumber().intValue();
			if (e.getSource() == quadVLinesSpinner)
				quadVLines = quadVLinesModel.getNumber().intValue();
			updateStates();
		}
		
		
	}
	
	
}
