package minimalsurface.frontend.macro;

import static java.awt.BorderLayout.CENTER;
import static java.awt.BorderLayout.SOUTH;
import static java.lang.Math.PI;
import halfedge.HalfEdgeDataStructure;
import halfedge.surfaceutilities.Ears;
import image.ImageHook;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import minimalsurface.MinimalSurfaces;
import circlepatterns.graph.CPEdge;
import circlepatterns.graph.CPFace;
import circlepatterns.graph.CPVertex;

public class BoundaryConditions extends MacroAction {

	protected Icon 
		icon = new ImageIcon(ImageHook.getImage("phi.png"));
	private EditBoundaryDialog
		editDialog = null;
	
	public BoundaryConditions() {
		editDialog = new EditBoundaryDialog(MinimalSurfaces.getMainWindow());
	}
	
	@Override
	public String getName() {
		return "Edit Boundary Conditions";
	}

	@Override
	public HalfEdgeDataStructure<CPVertex, CPEdge, CPFace> process(
			HalfEdgeDataStructure<CPVertex, CPEdge, CPFace> graph) throws Exception {
		editDialog.setData(graph);
		editDialog.setVisible(true);
		return graph;
	}
	
	@Override
	public Icon getIcon() {
		return icon;
	}

	private class EditBoundaryDialog extends JDialog implements ListSelectionListener, ActionListener, ChangeListener{

		private static final long serialVersionUID = 1L;
		
		private JList 
			interestList = new JList();
		private JLabel
			phiLabel = new JLabel(new ImageIcon(ImageHook.getImage("phi.png")));
		private JPanel
			viewPanel = new JPanel(),
			enterPanel = new JPanel(),
			anglePanel = new JPanel(),
			viewModePanel = new JPanel(),
			presetButtonsPanel = new JPanel();
		private JRadioButton
			ears = new JRadioButton("Ears"),
			dualAngles = new JRadioButton("Solitary Dualangles?"),
			triangles = new JRadioButton("Solitary Triangles"),
			quads = new JRadioButton("Solitary Quads"),
			all = new JRadioButton("All Faces"),
			allBoundary = new JRadioButton("Boundary");
		private JButton
			guessPhi = new JButton(new ImageIcon(ImageHook.getImage("wildcard.png"))),
			piButton = new JButton(new ImageIcon(ImageHook.getImage("buttonPI.png"))),
			twoPiButton = new JButton(new ImageIcon(ImageHook.getImage("button2PI.png"))),
			threePiButton = new JButton(new ImageIcon(ImageHook.getImage("button3PI.png"))),
			addPi2Button = new JButton(new ImageIcon(ImageHook.getImage("buttonPlusPI2.png"))),
			addPi3Button = new JButton(new ImageIcon(ImageHook.getImage("buttonPlusPI3.png"))),
			addPi4Button = new JButton(new ImageIcon(ImageHook.getImage("buttonPlusPI4.png"))),
			addPi6Button = new JButton(new ImageIcon(ImageHook.getImage("buttonPlusPI6.png"))),
			minusPi2Button = new JButton(new ImageIcon(ImageHook.getImage("buttonMinusPI2.png"))),
			minusPi3Button = new JButton(new ImageIcon(ImageHook.getImage("buttonMinusPI3.png"))),
			minusPi4Button = new JButton(new ImageIcon(ImageHook.getImage("buttonMinusPI4.png"))),
			minusPi6Button = new JButton(new ImageIcon(ImageHook.getImage("buttonMinusPI6.png")));
		private SpinnerNumberModel
			angleModel = new SpinnerNumberModel(0.0, 0.0, 8*PI, 0.001);
		private JSpinner
			angleSpinner = new JSpinner(angleModel);
		private JButton
			okButton = new JButton("Continue Construction", new ImageIcon(ImageHook.getImage("process.png")));
		private HalfEdgeDataStructure<CPVertex, CPEdge, CPFace> 
			graph = null;
		
		public EditBoundaryDialog(Frame parent) {
			super(parent, true);
			setResizable(false);
			setSize(400, 300);
			setTitle("Boundary Conditions");
			setLocationRelativeTo(parent);
			
			setLayout(new GridLayout(1,2));
			add(viewPanel);
			add(anglePanel);
			
			viewPanel.setLayout(new BorderLayout());
			JScrollPane scroller = new JScrollPane(interestList);
			JPanel scrollWrapper = new JPanel();
			scrollWrapper.setLayout(new BorderLayout());
			scrollWrapper.add(scroller, CENTER);
			scrollWrapper.setBorder(BorderFactory.createTitledBorder("Boundary Faces"));
			viewPanel.add(scrollWrapper, CENTER);
			viewPanel.add(viewModePanel, SOUTH);
			
			viewModePanel.setLayout(new GridLayout(6,1));
			viewModePanel.setBorder(BorderFactory.createTitledBorder("Filter"));
			viewModePanel.add(all);
			viewModePanel.add(allBoundary);
			viewModePanel.add(dualAngles);
			viewModePanel.add(triangles);
			viewModePanel.add(quads);
			viewModePanel.add(ears);
			
			ButtonGroup group = new ButtonGroup();
			group.add(all);
			group.add(allBoundary);
			group.add(dualAngles);
			group.add(triangles);
			group.add(quads);
			group.add(ears);
			
			GridBagConstraints c = new GridBagConstraints();
			c.fill = GridBagConstraints.HORIZONTAL;
			
			enterPanel.setLayout(new GridBagLayout());
			c.weightx = 0.0;
			c.gridwidth = GridBagConstraints.RELATIVE;
			enterPanel.add(phiLabel, c);
			c.weightx = 1.0;
			c.gridwidth = GridBagConstraints.REMAINDER;
			enterPanel.add(angleSpinner, c);
			
			presetButtonsPanel.setBorder(BorderFactory.createTitledBorder("Angle Presets"));
			presetButtonsPanel.setLayout(new GridLayout(3, 4, 2, 2));
			presetButtonsPanel.add(guessPhi);
			presetButtonsPanel.add(piButton);
			presetButtonsPanel.add(twoPiButton);
			presetButtonsPanel.add(threePiButton);
			presetButtonsPanel.add(addPi2Button);
			presetButtonsPanel.add(addPi3Button);
			presetButtonsPanel.add(addPi4Button);
			presetButtonsPanel.add(addPi6Button);
			presetButtonsPanel.add(minusPi2Button);
			presetButtonsPanel.add(minusPi3Button);
			presetButtonsPanel.add(minusPi4Button);
			presetButtonsPanel.add(minusPi6Button);
			
			anglePanel.setLayout(new GridBagLayout());
			c.weightx = 1.0;
			c.gridwidth = GridBagConstraints.REMAINDER;
			anglePanel.add(enterPanel, c);
			anglePanel.add(presetButtonsPanel, c);
			anglePanel.add(okButton, c);
			
			okButton.addActionListener(this);
			angleSpinner.addChangeListener(this);
			interestList.getSelectionModel().addListSelectionListener(this);
			all.addActionListener(this);
			allBoundary.addActionListener(this);
			dualAngles.addActionListener(this);
			triangles.addActionListener(this);
			quads.addActionListener(this);
			ears.addActionListener(this);
			guessPhi.addActionListener(this);
			piButton.addActionListener(this);
			twoPiButton.addActionListener(this);
			threePiButton.addActionListener(this);
			addPi2Button.addActionListener(this);
			addPi3Button.addActionListener(this);
			addPi4Button.addActionListener(this);
			addPi6Button.addActionListener(this);
			minusPi2Button.addActionListener(this);
			minusPi3Button.addActionListener(this);
			minusPi4Button.addActionListener(this);
			minusPi6Button.addActionListener(this);
		}
		
		public void setData(HalfEdgeDataStructure<CPVertex, CPEdge, CPFace> graph){
			this.graph = graph;
			all.setSelected(true);
			interestList.setListData(getAllFaces().toArray());
			updateStates();
		}

		@Override
		public void stateChanged(ChangeEvent e) {
			Object[] faces = interestList.getSelectedValues();
			for (Object of : faces){
				CPFace f = (CPFace)of;
				if (e.getSource() == angleSpinner)
					f.setCapitalPhi(angleModel.getNumber().doubleValue());
			}
		}


		@Override
		public void actionPerformed(ActionEvent e) {
			Object s = e.getSource();
			if (s == okButton)
				setVisible(false);
			if (s == all)
				interestList.setListData(getAllFaces().toArray());
			if (s == allBoundary)
				interestList.setListData(getAllBoundaryFaces().toArray());
			if (s == ears)
				interestList.setListData(getEars().toArray());
			if (s == dualAngles)
				interestList.setListData(getSolitaryNGon(2).toArray());
			if (s == triangles)
				interestList.setListData(getSolitaryNGon(3).toArray());
			if (s == quads)
				interestList.setListData(getSolitaryNGon(4).toArray());
			
			// preset buttons
			Object[] faces = interestList.getSelectedValues();
			for (Object of : faces){
				CPFace f = (CPFace)of;
//				if (s == guessPhi){
//					double phi = 0.0;
//					for (CPEdge b : f.getBoundary()){
//						if (b.isInteriorEdge())
//							phi += PI / 2;
//						else
//							phi += PI;
//					}
//				}
				if (s == piButton)
					f.setCapitalPhi(PI);
				if (s == twoPiButton)
					f.setCapitalPhi(2*PI);
				if (s == threePiButton)
					f.setCapitalPhi(3*PI);
				if (s == minusPi2Button)
					f.setCapitalPhi(f.getCapitalPhi() - PI / 2);
				if (s == minusPi3Button)
					f.setCapitalPhi(f.getCapitalPhi() - PI / 3);
				if (s == minusPi4Button)
					f.setCapitalPhi(f.getCapitalPhi() - PI / 4);
				if (s == minusPi6Button)
					f.setCapitalPhi(f.getCapitalPhi() - PI / 6);
				if (s == addPi2Button)
					f.setCapitalPhi(f.getCapitalPhi() + PI / 2);
				if (s == addPi3Button)
					f.setCapitalPhi(f.getCapitalPhi() + PI / 3);
				if (s == addPi4Button)
					f.setCapitalPhi(f.getCapitalPhi() + PI / 4);
				if (s == addPi6Button)
					f.setCapitalPhi(f.getCapitalPhi() + PI / 6);
			}
			updateStates();
		}
		
		private void updateStates(){
			CPFace f = (CPFace)interestList.getSelectedValue();
			if (f != null)
				angleModel.setValue(f.getCapitalPhi());
		}

		@Override
		public void valueChanged(ListSelectionEvent e) {
			updateStates();
		}
		
		private List<CPFace> getAllFaces() {
			if (graph == null)
				return new LinkedList<CPFace>();
			return graph.getFaces();
		}
		
		
		private List<CPFace> getAllBoundaryFaces(){
			if (graph == null)
				return Collections.emptyList();
			LinkedList<CPFace> result = new LinkedList<CPFace>();
			for (CPFace f : graph.getFaces())
				if (!f.isInteriorFace())
					result.add(f);
			return result;
		}
		
		private List<CPFace> getEars(){
			if (graph == null)
				return Collections.emptyList();
			return Ears.findEarsFaces(graph);
		}
		
		private List<CPFace> getSolitaryNGon(int n){
			if (graph == null)
				return Collections.emptyList();
			LinkedList<CPFace> result = new LinkedList<CPFace>();
			for (CPFace f : graph.getFaces()){
				if (f.isInteriorFace())
					continue;
				if (f.getBoundary().size() != n)
					continue;
				boolean nextEquals = false;
				boolean prevEquals = false;
				for (CPEdge e : f.getBoundary()){
					if (e.isBoundaryEdge() && e.getNextEdge().getRightFace() != null){
						CPFace prevFace = e.getOppositeEdge().getPreviousEdge().getRightFace();
						if (prevFace.getBoundary().size() == n)
							prevEquals = true;
					}
					if (e.isBoundaryEdge() && e.getPreviousEdge().getRightFace() != null){
						CPFace nextFace = e.getOppositeEdge().getNextEdge().getRightFace();
						if (nextFace.getBoundary().size() == n)
							nextEquals = true;
					}
				}
				if (!(nextEquals && prevEquals)) 
					result.add(f);
			}
			return result;
		}
	}
	
	
}
