package halfedge.frontend.content;

import halfedge.Edge;
import halfedge.Node;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


/**
 * An options panel for entity properties
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 */
@SuppressWarnings("serial")
public class NodeOptionsPanel extends JPanel {

	private Node<?, ?, ?> node = null;
	
	public NodeOptionsPanel(Node<?, ?, ?> node){
		this.node = node;
		makeLayout();
	}
	
	@SuppressWarnings("unchecked")
	private void makeLayout(){
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(2,2,2,2);
		c.weightx = 1.0;
		
		c.gridwidth = GridBagConstraints.RELATIVE;
		add(new JLabel("Index:"), c);
		c.gridwidth = GridBagConstraints.REMAINDER;
		add(new JLabel("" + node.getIndex()), c);
		if (Edge.class.isAssignableFrom(node.getClass())){
			Edge nextEdge = ((Edge)node).getNextEdge();
			Edge prevEdge = ((Edge)node).getPreviousEdge();
			Node target = ((Edge)node).getTargetVertex();
			Node leftFace = ((Edge)node).getLeftFace();
			Edge opp = ((Edge)node).getOppositeEdge();
			c.gridwidth = GridBagConstraints.RELATIVE;
			add(new JLabel("Sign:"), c);
			c.gridwidth = GridBagConstraints.REMAINDER;
			add(new JLabel(((Edge)node).isPositive() ? "+" : "-"), c);	
			c.gridwidth = GridBagConstraints.RELATIVE;
			add(new JLabel("Next:"), c);
			c.gridwidth = GridBagConstraints.REMAINDER;
			add(new JLabel(nextEdge == null ? "null" : "" + nextEdge.getIndex()), c);		
			c.gridwidth = GridBagConstraints.RELATIVE;
			add(new JLabel("Previous:"), c);
			c.gridwidth = GridBagConstraints.REMAINDER;
			add(new JLabel(prevEdge == null ? "null" : "" + prevEdge.getIndex()), c);	
			c.gridwidth = GridBagConstraints.RELATIVE;
			add(new JLabel("Target:"), c);
			c.gridwidth = GridBagConstraints.REMAINDER;
			add(new JLabel(target == null ? "null" : "" + target.getIndex()), c);	
			c.gridwidth = GridBagConstraints.RELATIVE;
			add(new JLabel("Left:"), c);
			c.gridwidth = GridBagConstraints.REMAINDER;
			add(new JLabel(leftFace == null ? "null" : "" + leftFace.getIndex()), c);
			c.gridwidth = GridBagConstraints.RELATIVE;
			add(new JLabel("Opposite:"), c);
			c.gridwidth = GridBagConstraints.REMAINDER;
			add(new JLabel(opp == null ? "null" : "" + opp.getIndex()), c);
		}
		
		for (Method m : node.getClass().getDeclaredMethods()){
			if (!m.getName().startsWith("set"))
				continue;
			if (m.getParameterTypes().length != 1)
				continue;
	
			Class p = m.getParameterTypes()[0];
			SetterType type = null;
			if (p.equals(Integer.class) || p.equals(int.class))
				type = SetterType.Int;
			else
			if (p.equals(Double.class) || p.equals(double.class))
				type = SetterType.Double;
			else
			if (p.equals(Short.class) || p.equals(short.class))
				type = SetterType.Short;
			else
			if (p.equals(Float.class) || p.equals(float.class))
				type = SetterType.Float;
			else
			if (p.equals(Long.class) || p.equals(long.class))
				type = SetterType.Long;
			else
			if (p.equals(String.class))
				type = SetterType.String;
			else
			if (p.equals(Boolean.class))
				type = SetterType.Boolean;
			else
			if (p.equals(boolean.class))
				type = SetterType.b0olean;
			
			if (type == null)
				continue;
			
			Method getter = null;
			try {
				switch (type){
				case b0olean:
					getter = node.getClass().getDeclaredMethod("is" + m.getName().substring(3), (Class[])null);
				default:
					getter = node.getClass().getDeclaredMethod("get" + m.getName().substring(3), (Class[])null);
				}
			} catch (Exception e) {}
			if (getter == null)
				continue;
			
			
			try {
				switch (type){
					case Double:
					case Float:
					case Int:
					case Long:
						SpinnerNumberModel model = null;
						JSpinner.NumberEditor editor = null;
						JSpinner spinner = null;
						switch (type){
							case Float:
							case Double:
								model = new SpinnerNumberModel((double)(Double)getter.invoke(node, (Object[])null), -1000.0, 1000.0, 0.01);
								spinner = new JSpinner(model);
								editor = new JSpinner.NumberEditor(spinner, "0.000000");
								spinner.setEditor(editor);
								break;
							case Int:
							case Long:
							case Short:
								model = new SpinnerNumberModel((int)(Integer)getter.invoke(node, (Object[])null), -1000, 1000, 1);
								spinner = new JSpinner(model);
								editor = new JSpinner.NumberEditor(spinner, "00000000");
								spinner.setEditor(editor);
								break;
						}
						if (model == null)
							continue;
						c.gridwidth = GridBagConstraints.RELATIVE;
						add(new JLabel(m.getName().substring(3)), c);
						c.gridwidth = GridBagConstraints.REMAINDER;
						spinner.addChangeListener(new SpinnerModelInvoker(model, m, type));
						add(spinner, c);
						break;
					case Boolean:
						Boolean isSelected = (Boolean)getter.invoke(node, (Object[])null);
						c.gridwidth = GridBagConstraints.REMAINDER;
						JCheckBox checker = new JCheckBox(m.getName().substring(3), isSelected);
						checker.addActionListener(new BooleanModelInvoker(checker, m));
						add(checker, c);
						break;
					case String:
						String text = (String)getter.invoke(node, (Object[])null);
						c.gridwidth = GridBagConstraints.RELATIVE;
						add(new JLabel(m.getName().substring(3)), c);
						c.gridwidth = GridBagConstraints.REMAINDER;
						JTextField field = new JTextField(text);
						field.addActionListener(new StringModelInvoker(field, m));
						add(field, c);
						break;
				}
			} catch (Exception e){
				e.printStackTrace();
				continue;
			}
		}
	}
	

	
	private enum SetterType{
		Float,
		Int,
		Long,
		Double,
		Short,
		String,
		Boolean,
		b0olean,
		Char;
	}
	


	private class StringModelInvoker implements ActionListener{

		private JTextField 
			field = null;
		private Method
			method = null;
		
		public StringModelInvoker(JTextField field, Method method) {
			this.field = field;
			this.method = method;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				method.invoke(node, field.getText());
			} catch (IllegalArgumentException e1) {
			} catch (IllegalAccessException e1) {
			} catch (InvocationTargetException e1) {
			}
		}

		
	}
	
	private class BooleanModelInvoker implements ActionListener{

		private JCheckBox 
			box = null;
		private Method
			method = null;;
		
		public BooleanModelInvoker(JCheckBox box, Method method) {
			this.box = box;
			this.method = method;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				method.invoke(node, box.isSelected());
			} catch (IllegalArgumentException e1) {
			} catch (IllegalAccessException e1) {
			} catch (InvocationTargetException e1) {
			}
		}

		
	}
	
	
	private class SpinnerModelInvoker implements ChangeListener{

		private SpinnerNumberModel
			model = null;
		private Method
			method = null;
		private SetterType
			type = null;
		
		public SpinnerModelInvoker(SpinnerNumberModel model, Method method, SetterType type) {
			this.model = model;
			this.method = method;
			this.type = type;
		}
		
		@Override
		public void stateChanged(ChangeEvent e) {
			try {
				switch (type){
					case Int:
						method.invoke(node, model.getNumber().intValue());
						break;
					case Double:
						method.invoke(node, model.getNumber().doubleValue());
						break;
					case Short:
						method.invoke(node, model.getNumber().shortValue());
						break;
					case Float:
						method.invoke(node, model.getNumber().floatValue());
						break;
					case Long:
						method.invoke(node, model.getNumber().longValue());
						break;
				} 
			} catch (IllegalArgumentException e1) {
			} catch (IllegalAccessException e1) {
			} catch (InvocationTargetException e1) {
			}
		}
		
	}
	
	
}
