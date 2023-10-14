/*
 * USE - UML based specification environment
 * Copyright (C) 1999-2004 Mark Richters, University of Bremen
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundat	ion; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

package org.tzi.use.gui.views;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import org.tzi.use.gui.main.MainWindow;
import org.tzi.use.gui.main.ModelBrowserSorting;
import org.tzi.use.gui.main.ModelBrowserSorting.SortChangeEvent;
import org.tzi.use.gui.main.ModelBrowserSorting.SortChangeListener;
import org.tzi.use.gui.util.ExtendedJTable;

import org.tzi.use.main.Session;
import org.tzi.use.parser.ocl.OCLCompiler;
import org.tzi.use.uml.mm.MAttribute;
import org.tzi.use.uml.mm.MClass;
import org.tzi.use.uml.ocl.expr.Expression;
import org.tzi.use.uml.ocl.value.Value;
import org.tzi.use.uml.sys.MObject;
import org.tzi.use.uml.sys.MObjectState;
import org.tzi.use.uml.sys.MSystem;
import org.tzi.use.uml.sys.MSystemException;
import org.tzi.use.uml.sys.MSystemState;
import org.tzi.use.uml.sys.events.tags.SystemStateChangedEvent;
import org.tzi.use.uml.sys.soil.MAttributeAssignmentStatement;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.eventbus.Subscribe;

/** 
 * A view for showing and changing object properties (attributes).
 *  
 * @author  Mark Richters
 */
@SuppressWarnings("serial")
public class WizardMVMView extends JPanel implements View {
	private static final String NO_OBJECTS_AVAILABLE = "(No objects available.)";

	private MainWindow fMainWindow;
	private Session fSession;
	private MSystem fSystem;
	private MObject fObject;

	//---
	private JFrame frame;
	private JPanel panel;

	private DefaultListModel<MClass> modelClass;
	private DefaultListModel<String> modelObjects;
	//	private DefaultListModel<String> modelAttrs;
	private DefaultTableModel modelTabAttrs;
	private DefaultListModel<String> modelAssocs;
	private JTableHeader header;

	private JLabel lbClass;
	private JLabel lbObjects;
	private JLabel lbAttrs;
	private JLabel lbAssoc;
	private JLabel lbObj;
	//	private JLabel lbValue;	
	private JLabel lbFrom;
	private JLabel lbTo;
	private JLabel lbAclass;	
	private JLabel lbAobject;	
	private JLabel lbAmultiplicity;	
	private JLabel lbAresMultiplicity;

	private JList<MClass> lClass;
	private JList<String> lObjects;
	private JList<String> lAssocs;
	private JList<String> lAttrs;

	private JTable tabAttr;
	private JScrollPane paneTabAttrs;

	private JTextField txNewObject;
	private String nomClass;
	private MClass oClass;
	private String nomObj;	
	private JComboBox<String> cmbClassOri;
	private JComboBox<String> cmbClassDes;
	private JComboBox<String> cmbObjectOri;
	private JComboBox<String> cmbObjectDes;
	private JComboBox<String> cmbMultiOri;
	private JComboBox<String> cmbMultiDes;

	private JButton btnCreateObject;
	private JButton btnDeleteObject;
	private JButton btnSaveObject;
	private JButton btnCancelObject;
	private JButton btnCreateAssoc;
	private JButton btnSaveAssoc;	

	private boolean bNewObj;

	//	private NewObjectDiagramView fOdv;
	//---

	//	private JComboBox<String> fObjectComboBox;
	private JTable fTable;
	private JScrollPane fTablePane;

	private TableModel fTableModel;

	private List<MAttribute> fAttributes;
	private String[] fValues;
	private Map<MAttribute, Value> fAttributeValueMap;

	/**
	 * The table model.
	 */
	class TableModel extends AbstractTableModel implements SortChangeListener {
		final String[] columnNames = { "Attribute", "Value" };

		TableModel() {
			ModelBrowserSorting.getInstance().addSortChangeListener( this );
			update();
		}

		public String getColumnName(int col) {
			return columnNames[col];
		}

		public int getColumnCount() { 
			return 2; 
		}
		public int getRowCount() { 
			return fAttributes.size();
		}
		public Object getValueAt(int row, int col) { 
			if (col == 0 )
				return fAttributes.get(row);
			else
				return fValues[row];
		}
		public boolean isCellEditable(int row, int col) {
			return col == 1 && !fAttributes.get(row).isDerived();
		}

		public void setValueAt(Object value, int row, int col) {
			fValues[row] = value.toString();
			fireTableCellUpdated(row, col);
		}

		private void update() {
			// initialize table model
			if ( haveObject() ) {
				MObjectState objState = fObject.state(fSystem.state());
				fAttributeValueMap = objState.attributeValueMap();

				Collection<MAttribute> attributes = ModelBrowserSorting.getInstance().sortAttributes( fAttributeValueMap.keySet() );

				attributes = Collections2.filter(attributes, new Predicate<MAttribute>() {
					@Override
					public boolean apply(MAttribute input) {
						return !input.isDerived();
					}
				});

				fAttributes = Lists.newArrayList(attributes);
				fValues = new String[fAttributes.size()];
				for (int i = 0; i < fValues.length; i++) {
					fValues[i] = fAttributeValueMap.get(fAttributes.get(i)).toString();
				}
			} else {
				fAttributes = Collections.emptyList();
				fValues = new String[0];
			}
			fireTableDataChanged();
		}

		/**
		 * After the occurrence of an event the attribute list is updated.
		 */
		public void stateChanged( SortChangeEvent e ) {
			fAttributes = ModelBrowserSorting.getInstance()
					.sortAttributes( fAttributes );
			update();
		}
	}

	public WizardMVMView(MainWindow parent, Session session) {
		super(new BorderLayout());
		fMainWindow = parent;
		fSession = session;
		fSystem = session.system();
		fSystem.registerRequiresAllDerivedValues();
		fSystem.getEventBus().register(this);
		bNewObj=false;

		frame = new JFrame("Prototipo MVM Wizard");
		frame.setSize(630, 660);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocationRelativeTo(null);

		panel = new JPanel();

		lAttrs = new JList<String>();
		lObjects = new JList<String>();

		frame.add(panel);

		panel.setLayout(null);

		modelClass = new DefaultListModel<MClass>();
		modelObjects = new DefaultListModel<>();
		modelAssocs = new DefaultListModel<>();

		modelTabAttrs = new DefaultTableModel();
		tabAttr = new JTable(modelTabAttrs);

		lbClass = new JLabel("Class");
		lbClass.setBounds(10, 15, 60, 25);
		panel.add(lbClass);

		lbObjects = new JLabel("Objects");
		lbObjects.setBounds(110, 15, 60, 25);
		panel.add(lbObjects);	   

		lbAttrs = new JLabel("Attributes");
		lbAttrs.setBounds(245, 15, 60, 25);
		panel.add(lbAttrs);			

		lClass = new JList<MClass>(loadListMClass());
		lClass.setBounds(10, 40, 90, 140);
		lClass.setBorder(BorderFactory.createLineBorder(Color.black));
		lClass.setSelectedIndex(0);
		oClass = lClass.getSelectedValue();
		nomClass = oClass.name();

		lClass.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent evt) {
				oClass = lClass.getSelectedValue();
				nomClass = oClass.name();
				lObjects.setModel(loadListObjects(nomClass));
				lObjects.setSelectedIndex(0);
				nomObj = (String) lObjects.getSelectedValue();
				selectObject( nomObj);
				txNewObject.setText(nomObj);
				txNewObject.setEnabled(false);
			}
		});

		modelObjects=loadListObjects(nomClass);

		lObjects = new JList<String>( modelObjects );
		lObjects.setBounds(110, 40, 90, 140);
		lObjects.setBorder(BorderFactory.createLineBorder(Color.black));
		lObjects.setSelectedIndex(0);
		nomObj = (String) lObjects.getSelectedValue();

		lObjects.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent evt) {
				oClass = lClass.getSelectedValue();
				nomClass = oClass.name();
				nomObj = (String) lObjects.getSelectedValue();
				selectObject( nomObj);
				txNewObject.setText(nomObj);
				txNewObject.setEnabled(false);
			}
		});

		// create table of attribute/value pairs
		fTableModel = new TableModel();
		fTable = new ExtendedJTable(fTableModel);
		fTable.setPreferredScrollableViewportSize(new Dimension(250, 70));

		fTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		fTablePane = new JScrollPane(fTable);
		fTablePane.setBounds(210, 40, 180, 140);

		selectObject(nomObj);

		lbObj = new JLabel("Object");
		lbObj.setBounds(400, 15, 160, 25);
		panel.add(lbObj);	

		txNewObject = new JTextField(20);
		txNewObject.setBounds(400, 40, 100, 25);
		txNewObject.setEnabled(false);
		txNewObject.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) { //watch for key strokes
				if(txNewObject.getText().length() == 0 )
					btnSaveObject.setEnabled(false);
				else
				{
					btnSaveObject.setEnabled(true);
				}
			}
		});
		panel.add(txNewObject);

		nomObj = (String) lObjects.getSelectedValue();
		txNewObject.setText(nomObj);

		btnCreateObject = new JButton("New Object");
		btnCreateObject.setBounds(400, 70, 100, 25);
		btnCreateObject.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				initNewObject();
			}
		});
		panel.add(btnCreateObject);

		btnSaveObject = new JButton("Save Object");
		btnSaveObject.setBounds(400, 100, 100, 25);

		btnSaveObject.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				nomObj = txNewObject.getText();
				oClass = lClass.getSelectedValue();
				saveObject(oClass, nomObj);
				bNewObj=false;
				txNewObject.setEnabled(false);
				selectObject(nomObj);
			}
		});

		panel.add(btnSaveObject);

		btnCancelObject = new JButton("Cancel Object");
		btnCancelObject.setBounds(400, 130, 100, 25);

		btnCancelObject.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				cancelObject(nomObj);
			}
		});
		panel.add(btnCancelObject);

		btnDeleteObject = new JButton("Delete Object");
		btnDeleteObject.setBounds(400, 160, 100, 25);
		btnDeleteObject.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				deleteObject(nomObj);
			}
		});
		panel.add(btnDeleteObject);

		//deleteObject(String nomObj) {

		lbAssoc = new JLabel("Assoc");
		lbAssoc.setBounds(10, 190, 160, 25);
		panel.add(lbAssoc);	

		lbFrom = new JLabel("From");
		lbFrom.setBounds(220, 190, 160, 25);
		panel.add(lbFrom);

		lbTo = new JLabel("To");
		lbTo.setBounds(350, 190, 160, 25);
		panel.add(lbTo);

		lAssocs = new JList<String>( modelAssocs );
		lAssocs.setBounds(10, 215, 110, 85);
		lAssocs.setBorder(BorderFactory.createLineBorder(Color.black));
		panel.add(lAssocs);

		lbAclass = new JLabel("Class");
		lbAclass.setBounds(150, 215, 100, 25);
		panel.add(lbAclass);

		lbAobject = new JLabel("Object");
		lbAobject.setBounds(150, 245, 100, 25);
		panel.add(lbAobject);	

		lbAmultiplicity = new JLabel("Multiplicity");
		lbAmultiplicity.setBounds(150, 275, 100, 25);
		panel.add(lbAmultiplicity);

		cmbClassOri = new JComboBox<String>();
		cmbClassOri.setBounds(220, 215, 120, 25);
		panel.add(cmbClassOri);

		cmbClassDes = new JComboBox<String>();
		cmbClassDes.setBounds(350, 215, 120, 25);
		panel.add(cmbClassDes);

		cmbObjectOri = new JComboBox<String>();
		cmbObjectOri.setBounds(220, 245, 120, 25);
		panel.add(cmbObjectOri);

		cmbObjectDes = new JComboBox<String>();
		cmbObjectDes.setBounds(350, 245, 120, 25);
		panel.add(cmbObjectDes);

		cmbMultiOri = new JComboBox<String>();
		cmbMultiOri.setBounds(220, 275, 120, 25);
		panel.add(cmbMultiOri);		

		cmbMultiDes = new JComboBox<String>();
		cmbMultiDes.setBounds(350, 275, 120, 25);
		panel.add(cmbMultiDes);		

		btnCreateAssoc = new JButton("Create Assoc");
		btnCreateAssoc.setBounds(15, 310, 110, 25);
		panel.add(btnCreateAssoc);

		btnSaveAssoc = new JButton("Save Assoc");
		btnSaveAssoc.setBounds(220, 310, 120, 25);
		panel.add(btnSaveAssoc);

		panel.add(lClass);
		panel.add(lObjects);
		panel.add(lAttrs);		
		panel.add(fTablePane);	

		lClass.setSelectedIndex(0);
		nomClass = ((MClass) lClass.getSelectedValue()).name();
		lObjects.setModel(loadListObjects(nomClass));
		lObjects.setSelectedIndex(0);

		add(panel);

		setSize(new Dimension(400, 300));

	}

	public void setFrameName(String name) {
		frame.setName(name);
	}
	private DefaultListModel<MClass> loadListMClass() {
		DefaultListModel<MClass> ldefLModel = new DefaultListModel<MClass>();
		for (MClass oClass : fSystem.model().classes()) {
			ldefLModel.addElement(oClass);
		}
		return ldefLModel;
	}

	private DefaultListModel<String> loadListObjects(String nomClass) {
		DefaultListModel<String> ldefLModel = new DefaultListModel<String>();
		MSystemState state = fSystem.state();
		Set<MObject> allObjects = state.allObjects();
		ArrayList<String> livingObjects = new ArrayList<String>();

		for (MObject obj : allObjects) {
			if (obj.cls().name().equals(nomClass)) {
				ldefLModel.addElement(obj.name());
			}
		}
		return ldefLModel;
	}
	
	private void initNewObject() {
		txNewObject.setText("");
		txNewObject.setEnabled(true);
		bNewObj=true;
	}

	private void cancelObject(String nomObj) {
		selectObject(nomObj);
	}
	private void createObject(MClass oClass, String nomObj) {
		fMainWindow.createObject(oClass, nomObj);
		lObjects.setModel(loadListObjects(nomClass));
	}
	private void saveObject(MClass oClass, String nomObj) {
		if (bNewObj) {
			// Hacer copia de fValues
			String[] fValuesAnt = new String[fAttributes.size()];
			for (int i = 0; i < fAttributes.size(); i++) {
				fValuesAnt[i] = fValues[i];
			}
			createObject(oClass, nomObj);
			selectObject( nomObj);
			for (int i = 0; i < fAttributes.size(); i++) {
				fValues[i] = fValuesAnt[i];
			}
		}
		applyChanges();
	}
	private void deleteObject(String nomObj) {
		selectObject(nomObj);
		MSystemState state = fSystem.state();
		state.deleteObject(fObject);
		lObjects.setModel(loadListObjects(nomClass));
	}

	/**
	 * Applies changes by setting new attribute values. Entries may be
	 * arbitrary OCL expressions. 
	 */
	private void applyChanges() {

		if (fTable.getCellEditor() != null) { 
			fTable.getCellEditor().stopCellEditing();
		}

		if (!haveObject()) { 
			return;
		}

		// Don't refresh after first change...
		fSystem.getEventBus().unregister(this);
		boolean error = false;

		for (int i = 0; i < fValues.length; ++i) {
			MAttribute attribute = fAttributes.get(i);
			String newValue = fValues[i];
			String oldValue = fAttributeValueMap.get(attribute).toString();

			if (!newValue.equals(oldValue)) {

				StringWriter errorOutput = new StringWriter();
				Expression valueAsExpression = 
						OCLCompiler.compileExpression(
								fSystem.model(),
								fSystem.state(),
								newValue, 
								"<input>", 
								new PrintWriter(errorOutput, true), 
								fSystem.varBindings());

				if (valueAsExpression == null) {
					JOptionPane.showMessageDialog(
							fMainWindow, 
							errorOutput, 
							"Error", 
							JOptionPane.ERROR_MESSAGE);
					error = true;
					continue;
				}

				try {
					fSystem.execute(
							new MAttributeAssignmentStatement(
									fObject, 
									attribute, 
									valueAsExpression));

				} catch (MSystemException e) {
					JOptionPane.showMessageDialog(
							fMainWindow, 
							e.getMessage(), 
							"Error", 
							JOptionPane.ERROR_MESSAGE);
					error = true;
				}
			}
		}

		if (!error) update();

		fSystem.getEventBus().register(this);
	}

	private boolean haveObject() {
		return fObject != null && fObject.exists(fSystem.state());
	}

	/**
	 * An object has been selected from the list. Update the table of
	 * properties.
	 */
	public void selectObject(String objName) {
		MSystemState state = fSystem.state();
		fObject = state.objectByName(objName);
		fTableModel.update();

		// Buscar en lista
		lObjects.setSelectedIndex(0);
		int nObjs= lObjects.getModel().getSize();
		for (int nObj=0;nObj<nObjs;nObj++) {
			if (lObjects.getModel().getElementAt(nObj).equals(objName)) {
				lObjects.setSelectedIndex(nObj);
			}
		}
		return;
	}


	private void update() {
		fTableModel.update();
	}

	@Subscribe
	public void onStateChanged(SystemStateChangedEvent e) {
		update();
	}

	/**
	 * Detaches the view from its model.
	 */
	public void detachModel() {
		fSystem.getEventBus().unregister(this);
		fSystem.unregisterRequiresAllDerivedValues();
	}
}

