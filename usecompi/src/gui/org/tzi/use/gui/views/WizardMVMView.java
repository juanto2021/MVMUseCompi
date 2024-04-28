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
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyVetoException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;

import org.tzi.use.config.Options;
import org.tzi.use.gui.main.MainWindow;
import org.tzi.use.gui.main.ModelBrowserSorting;
import org.tzi.use.gui.main.ModelBrowserSorting.SortChangeEvent;
import org.tzi.use.gui.main.ModelBrowserSorting.SortChangeListener;
import org.tzi.use.gui.main.ViewFrame;
import org.tzi.use.gui.mvm.AssocWizard;
import org.tzi.use.gui.mvm.LinkWizard;
import org.tzi.use.gui.mvm.MVMWizardAssoc;
import org.tzi.use.gui.util.ExtendedJTable;
import org.tzi.use.gui.views.diagrams.objectdiagram.NewObjectDiagram;
import org.tzi.use.gui.views.diagrams.objectdiagram.NewObjectDiagramView;
import org.tzi.use.gui.views.diagrams.objectdiagram.QualifierInputView;
import org.tzi.use.main.Session;
import org.tzi.use.parser.ocl.OCLCompiler;
import org.tzi.use.uml.mm.MAssociation;
import org.tzi.use.uml.mm.MAssociationEnd;
import org.tzi.use.uml.mm.MAttribute;
import org.tzi.use.uml.mm.MClass;
import org.tzi.use.uml.mm.MClassInvariant;
import org.tzi.use.uml.mm.MModel;
import org.tzi.use.uml.mm.MMultiplicity;
import org.tzi.use.uml.ocl.expr.Evaluator;
import org.tzi.use.uml.ocl.expr.Expression;
import org.tzi.use.uml.ocl.expr.MultiplicityViolationException;
import org.tzi.use.uml.ocl.value.BooleanValue;
import org.tzi.use.uml.ocl.value.Value;
import org.tzi.use.uml.sys.MLink;
import org.tzi.use.uml.sys.MLinkEnd;
import org.tzi.use.uml.sys.MObject;
import org.tzi.use.uml.sys.MObjectState;
import org.tzi.use.uml.sys.MSystem;
import org.tzi.use.uml.sys.MSystemException;
import org.tzi.use.uml.sys.MSystemState;
import org.tzi.use.uml.sys.events.tags.SystemStateChangedEvent;
import org.tzi.use.uml.sys.soil.MAttributeAssignmentStatement;
import org.tzi.use.uml.sys.soil.MLinkDeletionStatement;
import org.tzi.use.uml.sys.soil.MLinkInsertionStatement;

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

	private static final String NAMEFRAMEMVMDIAGRAM = "MVM";
	private static final String NAMEFRAMEMVMWIZARD = "MVMWizard";
	private MainWindow fMainWindow;
	//	private WizardMVMView thisWizard;
	private PrintWriter fLogWriter;
	private Session fSession;
	private MSystem fSystem;
	private MObject fObject;

	private JFrame frame;
	private JPanel panel;

	private DefaultListModel<String> modelObjects;
	private DefaultTableModel modelTabAttrs;

	private JLabel lbClass;
	private JLabel lbObjects;
	private JLabel lbAttrs;
	private JLabel lbAssoc;
	private JLabel lbObj;
	private JLabel lbFrom;
	private JLabel lbTo;
	private JLabel lbFromClass;
	private JLabel lbToClass;
	private JLabel lbAclass;	
	private JLabel lbAobject;	
	private JLabel lbAmultiplicity;	
	private JLabel lbArole;	
	private JLabel lbClassInvariants;
	private JLabel lbResClassInvariants;
	private JLabel lbCheckStructure;

	private JScrollPane scrollPaneClass;
	private JScrollPane scrollPaneObj;

	private JList<MClass> lClass;
	private JList<String> lObjects;
	private JList<MAssociation> lAssocs;
	private JList<String> lAttrs;

	private JTextField txNewObject;
	private JTextField txMultiOri;
	private JTextField txMultiDes;
	private JTextField txOriAssocRole;
	private JTextField txDesAssocRole;
	private String nomClass;
	private MClass oClass;
	private String nomObj;	
	private JComboBox<MClass> cmbClassOri;
	private JComboBox<MClass> cmbClassDes;
	private JComboBox<MObject> cmbObjectOri;
	private JComboBox<MObject> cmbObjectDes;
	private JComboBox<String> cmbMultiOri;
	private JComboBox<String> cmbMultiDes;
	private JCheckBox chkAutoLayout;	

	private JButton btnCreateObject;
	private JButton btnNewObjectAuto;
	private JButton btnDeleteObject;
	private JButton btnSaveObject;
	private JButton btnCancelObject;
	private JButton btnInsertLinkAssoc;
	private JButton btnDeleteLink;	
	private JButton btnShowClassInvariants;	
	private JButton btnShowCheckStructure;
	private JButton btnRefreshElements;

	private boolean bNewObj;
	private JTable fTable;
	private JScrollPane fTablePane;

	private TableModel fTableModel;

	private List<MAttribute> fAttributes;
	private String[] fValues;
	private Map<MAttribute, Value> fAttributeValueMap;
	private NewObjectDiagram odvAssoc;
	private String aMulti[] = new String[] { 
			"0", "1", "*" };
	private List<Future<EvalResult>> futures;
	private List<AssocWizard> lAssocsWizard;
	private Color colorSoftGray;

	private JInternalFrame[] allframes;

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

	public WizardMVMView(MainWindow parent, Session session, PrintWriter logWriter) {
		super(new BorderLayout());
		//		thisWizard=this;
		fMainWindow = parent;
		fSession = session;
		fSystem = session.system();
		fSystem.registerRequiresAllDerivedValues();
		fSystem.getEventBus().register(this);
		fLogWriter=logWriter;
		colorSoftGray=new Color(218,224,224);

		searchObjDiagramAssociated();

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

		modelObjects = new DefaultListModel<>();
		modelTabAttrs = new DefaultTableModel();

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
		lClass.setBounds(10, 40, 90, 145);
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

		scrollPaneClass = new JScrollPane();
		scrollPaneClass.setViewportView(lClass);
		scrollPaneClass.setBounds(10, 40, 90, 145);

		modelObjects=loadListObjects(nomClass);


		lObjects = new JList<String>( modelObjects );
		lObjects.setBounds(110, 40, 90, 110);
		lObjects.setBorder(BorderFactory.createLineBorder(Color.black));
		lObjects.setLayoutOrientation(JList.VERTICAL);
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
		scrollPaneObj = new JScrollPane();
		scrollPaneObj.setViewportView(lObjects);
		scrollPaneObj.setBounds(110, 40, 90, 110);
		// mas
		btnNewObjectAuto = new JButton("+");
		btnNewObjectAuto.setBounds(110, 160, 90, 25);

		btnNewObjectAuto.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				newObjectAuto();
			}
		});
		panel.add(btnNewObjectAuto);

		fTableModel = new TableModel();
		fTable = new ExtendedJTable(fTableModel);
		fTable.setPreferredScrollableViewportSize(new Dimension(250, 70));

		fTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		fTablePane = new JScrollPane(fTable);
		fTablePane.setBounds(210, 40, 180, 100);

		chkAutoLayout=new JCheckBox("Auto Layout");
		chkAutoLayout.setBounds(210, 160, 160, 25);
		chkAutoLayout.setSelected(true);
		chkAutoLayout.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (odvAssoc!=null) {
					if (e.getStateChange()==1) {
						odvAssoc.forceStartLayoutThread();
					}else {
						odvAssoc.forceStopLayoutThread();
					}
				}
			}
		});
		panel.add(chkAutoLayout);	

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
				setResClassInvariants();
				setResCheckStructure();
				bNewObj=false;
				txNewObject.setEnabled(false);
				selectObject(nomObj);
				cmbObjectOri.setModel(loadComboObjectMObject(cmbClassOri));
				cmbObjectDes.setModel(loadComboObjectMObject(cmbClassDes));
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

				int resp =JOptionPane.showConfirmDialog(null, "Are you sure to delete object ["+nomObj+"]?",
						"Delete objects", JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE);
				if (resp==0) {
					deleteObject(nomObj);
					cmbObjectOri.setModel(loadComboObjectMObject(cmbClassOri));
					cmbObjectDes.setModel(loadComboObjectMObject(cmbClassDes));
				}
			}
		});
		panel.add(btnDeleteObject);

		lbAssoc = new JLabel("Association");
		lbAssoc.setBounds(10, 190, 160, 25);
		panel.add(lbAssoc);	

		lbFrom = new JLabel("From");
		lbFrom.setBounds(220, 190, 160, 25);
		panel.add(lbFrom);

		lbTo = new JLabel("To");
		lbTo.setBounds(350, 190, 160, 25);
		panel.add(lbTo);

		lAssocs = new JList<MAssociation>(loadListAssoc());

		lAssocs.setBounds(10, 215, 110, 85);
		lAssocs.setBorder(BorderFactory.createLineBorder(Color.black));
		lAssocs.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent evt) {
				MAssociation oAssoc = lAssocs.getSelectedValue();
				setComposAssoc(oAssoc);
			}
		});

		panel.add(lAssocs);

		btnRefreshElements = new JButton("Refresh");
		btnRefreshElements.setBounds(10, 310, 110, 25);
		btnRefreshElements.setVerticalAlignment(SwingConstants.CENTER);
		btnRefreshElements.setHorizontalAlignment(SwingConstants.CENTER);
		btnRefreshElements.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				refreshComponents();
			}
		});
		panel.add(btnRefreshElements);

		lbAclass = new JLabel("Class");
		lbAclass.setBounds(150, 215, 100, 25);
		panel.add(lbAclass);

		lbAobject = new JLabel("Object");
		lbAobject.setBounds(150, 245, 100, 25);
		panel.add(lbAobject);	

		lbAmultiplicity = new JLabel("Multiplicity");
		lbAmultiplicity.setBounds(150, 275, 100, 25);
		panel.add(lbAmultiplicity);

		lbArole = new JLabel("Role");
		lbArole.setBounds(150, 305, 100, 25);
		panel.add(lbArole);

		cmbClassOri = new JComboBox<MClass>();

		cmbClassOri.setModel(loadComboClass());
		cmbClassOri.setBounds(220, 215, 120, 25);
		cmbClassOri.addActionListener (new ActionListener () {
			public void actionPerformed(ActionEvent e) {
				cmbObjectOri.setModel(loadComboObjectMObject(cmbClassOri));
			}
		});
		cmbClassOri.setEnabled(false);
		cmbClassOri.setVisible(false);
		panel.add(cmbClassOri);

		Border blackline = BorderFactory.createLineBorder(Color.black);

		lbFromClass = new JLabel("");
		lbFromClass.setBounds(220, 215, 120, 25);
		lbFromClass.setBorder(blackline);
		lbFromClass.setBackground(colorSoftGray);
		lbFromClass.setHorizontalAlignment(SwingConstants.CENTER);
		lbFromClass.setOpaque(true);

		panel.add(lbFromClass);

		cmbClassDes = new JComboBox<MClass>();
		cmbClassDes.setModel(loadComboClass());
		cmbClassDes.setBounds(350, 215, 120, 25);
		cmbClassDes.addActionListener (new ActionListener () {
			public void actionPerformed(ActionEvent e) {
				cmbObjectDes.setModel(loadComboObjectMObject(cmbClassDes));
			}
		});
		cmbClassDes.setEnabled(false);
		cmbClassDes.setVisible(false);
		panel.add(cmbClassDes);

		lbToClass = new JLabel("");
		lbToClass.setBounds(350, 215, 120, 25);
		lbToClass.setBorder(blackline);
		lbToClass.setBackground(colorSoftGray);
		lbToClass.setHorizontalAlignment(SwingConstants.CENTER);
		lbToClass.setOpaque(true);
		panel.add(lbToClass);

		cmbObjectOri = new JComboBox<MObject>();
		cmbObjectOri.setModel(loadComboObjectMObject(cmbClassOri));
		cmbObjectOri.setBounds(220, 245, 120, 25);

		cmbObjectOri.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				MObject oSel = (MObject) cmbObjectOri.getSelectedItem();
				System.out.println("Busca extremo para ["+oSel.name()+"]");
				MObject oRel = findAssocEnd(oSel);
				if (oRel!=null) {
					System.out.println("Le corresponde ["+oRel.name()+"]");
					// Buscar y seleccionar oRel en cmbObjectDes
					cmbObjectDes.setSelectedItem(oRel);
				}else {
					System.out.println("No tiene extremo");
				}
			}
		});
		panel.add(cmbObjectOri);

		cmbObjectDes = new JComboBox<MObject>();
		cmbObjectDes.setModel(loadComboObjectMObject(cmbClassDes));
		cmbObjectDes.setBounds(350, 245, 120, 25);

		cmbObjectDes.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				MObject oSel = (MObject) cmbObjectDes.getSelectedItem();
				System.out.println("Busca extremo para ["+oSel.name()+"]");
				MObject oRel = findAssocEnd(oSel);
				if (oRel!=null) {
					System.out.println("Le corresponde ["+oRel.name()+"]");
					// Buscar y seleccionar oRel en cmbObjectDes
					cmbObjectDes.setSelectedItem(oRel);
				}else {
					System.out.println("No tiene extremo");
				}
			}
		});
		panel.add(cmbObjectDes);

		cmbMultiOri = new JComboBox<String>(aMulti);
		cmbMultiOri.setBounds(220, 275, 120, 25);
		cmbMultiOri.setEditable(false);
		cmbMultiOri.setVisible(false);

		panel.add(cmbMultiOri);		

		txMultiOri = new JTextField(20);
		txMultiOri.setBounds(220, 275, 120, 25);
		txMultiOri.setBorder(blackline);
		txMultiOri.setEditable(false);
		txMultiOri.setBackground(colorSoftGray);
		txMultiOri.setHorizontalAlignment(SwingConstants.CENTER);
		panel.add(txMultiOri);	

		cmbMultiDes = new JComboBox<String>(aMulti);
		cmbMultiDes.setBounds(350, 275, 120, 25);
		cmbMultiDes.setEditable(false);
		cmbMultiDes.setVisible(false);
		panel.add(cmbMultiDes);		

		txMultiDes = new JTextField(20);
		txMultiDes.setBounds(350, 275, 120, 25);
		txMultiDes.setBorder(blackline);
		txMultiDes.setEditable(false);
		txMultiDes.setBackground(colorSoftGray);
		txMultiDes.setHorizontalAlignment(SwingConstants.CENTER);
		panel.add(txMultiDes);	

		txOriAssocRole = new JTextField(20);
		txOriAssocRole.setBounds(220, 305, 120, 25);
		txOriAssocRole.setBorder(blackline);
		txOriAssocRole.setEditable(false);
		txOriAssocRole.setBackground(colorSoftGray);
		txOriAssocRole.setHorizontalAlignment(SwingConstants.CENTER);
		panel.add(txOriAssocRole);	

		txDesAssocRole = new JTextField(20);
		txDesAssocRole.setBounds(350, 305, 120, 25);
		txDesAssocRole.setBorder(blackline);
		txDesAssocRole.setEditable(false);
		txDesAssocRole.setBackground(colorSoftGray);
		txDesAssocRole.setHorizontalAlignment(SwingConstants.CENTER);
		panel.add(txDesAssocRole);	

		btnInsertLinkAssoc = new JButton("Insert link");
		btnInsertLinkAssoc.setBounds(220, 340, 110, 25);
		btnInsertLinkAssoc.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MAssociation oAssoc = lAssocs.getSelectedValue();
				insertLink(oAssoc) ;
			}
		});
		panel.add(btnInsertLinkAssoc);

		btnDeleteLink = new JButton("Delete link");
		btnDeleteLink.setBounds(350, 340, 120, 25);
		btnDeleteLink.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MAssociation oAssoc = lAssocs.getSelectedValue();
				int resp =JOptionPane.showConfirmDialog(null, "Are you sure to delete link ["+oAssoc.name()+"]?",
						"Delete Links", JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE);					

				deleteLink(oAssoc) ;
			}
		});

		panel.add(btnDeleteLink);

		lbClassInvariants = new JLabel("State invariants");
		lbClassInvariants.setBounds(220, 375, 120, 25);
		panel.add(lbClassInvariants);

		lbResClassInvariants = new JLabel("");
		lbResClassInvariants.setBounds(350, 375, 120, 25);
		lbResClassInvariants.setBorder(blackline);
		lbResClassInvariants.setVerticalAlignment(SwingConstants.CENTER);
		lbResClassInvariants.setHorizontalAlignment(SwingConstants.CENTER);
		lbResClassInvariants.setFont(new Font("Serif", Font.BOLD, 18));
		lbResClassInvariants.setVisible(false);
		panel.add(lbResClassInvariants);

		btnShowClassInvariants = new JButton("OK");
		btnShowClassInvariants.setBounds(350, 375, 120, 25);
		btnShowClassInvariants.setVerticalAlignment(SwingConstants.CENTER);
		btnShowClassInvariants.setHorizontalAlignment(SwingConstants.CENTER);
		btnShowClassInvariants.setFont(new Font("Serif", Font.BOLD, 18));
		btnShowClassInvariants.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showClassInvariantsState();
			}
		});
		panel.add(btnShowClassInvariants);

		lbCheckStructure = new JLabel("Check Structure");
		lbCheckStructure.setBounds(220, 410, 120, 25);
		panel.add(lbCheckStructure);

		btnShowCheckStructure = new JButton("OK");
		btnShowCheckStructure.setBounds(350, 410, 120, 25);
		btnShowCheckStructure.setVerticalAlignment(SwingConstants.CENTER);
		btnShowCheckStructure.setHorizontalAlignment(SwingConstants.CENTER);
		btnShowCheckStructure.setFont(new Font("Serif", Font.BOLD, 18));
		btnShowCheckStructure.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				boolean ok=checkStructure();
				if (!ok) {
					MVMWizardAssoc dW= new MVMWizardAssoc(frame,lAssocsWizard);
					dW.setSize(910,490);
					dW.setLocationRelativeTo(null);
					dW.setVisible(true);
					String commandWizard = dW.getCommandWizard();
					MAssociation oAssocPralWizard = dW.getAssocWizard();
					if (commandWizard!="") {
						doActionsWizardAssoc(oAssocPralWizard,commandWizard);
					}
				}
			}
		});
		panel.add(btnShowCheckStructure);
		panel.add(scrollPaneClass);
		panel.add(scrollPaneObj);

		panel.add(lAttrs);		
		panel.add(fTablePane);	

		lClass.setSelectedIndex(0);
		nomClass = ((MClass) lClass.getSelectedValue()).name();
		lObjects.setModel(loadListObjects(nomClass));
		lObjects.setSelectedIndex(0);
		lAssocs.setSelectedIndex(0);
		MAssociation oAssoc = lAssocs.getSelectedValue();
		if (oAssoc!=null) {
			setComposAssoc(oAssoc);
		}

		setResClassInvariants();
		setResCheckStructure();
		add(panel);

		setSize(new Dimension(400, 300));
		
		// Provis
		refreshComponents();

	}
	/**
	 * Refresh elements
	 */
	public void refreshComponents() {
		oClass = lClass.getSelectedValue();
		lClass.setModel(loadListMClass());
		lClass.setSelectedValue(oClass, true);
		nomObj = (String) lObjects.getSelectedValue();
		lObjects.setModel(loadListObjects(oClass.name()));
		lObjects.setSelectedValue(nomObj, true);
	}
	private MObject findAssocEnd(MObject oFindAssoc) {
		MObject objEnd=null;
		MSystemState state = fSystem.state();
		Set<MLink> oLinkSets=state.allLinks();
		for (MLink oLink: oLinkSets) {
			System.out.println("oLink ["+oLink.linkedObjects()+"]");
			MLinkEnd oL0 = oLink.getLinkEnd(0);
			MLinkEnd oL1 = oLink.getLinkEnd(1);
			if(oL0.object().name().equals(oFindAssoc.name())&&
					oL0.object().cls().name().equals(oFindAssoc.cls().name())) {
				return oL1.object();
			}
			if(oL1.object().name().equals(oFindAssoc.name())&&
					oL1.object().cls().name().equals(oFindAssoc.cls().name())) {
				return oL0.object();
			}				

		}
		return objEnd;
	}

	/**
	 * Realiza las acciones propuestas en wizard
	 * @param oAssocPralWizard
	 * @param commandWizard
	 */
	private void doActionsWizardAssoc(MAssociation oAssocPralWizard, String commandWizard) {
		System.out.println("getActionCommand " + commandWizard);
		ArrayList<String> lNewObjects = new ArrayList<String>();
		String[] partes = commandWizard.split("-");
		int nPartes = partes.length;
		for (int nParte=0;nParte<nPartes;nParte++) {
			String parte = partes[nParte];
			String[] subPartes = parte.split(" ");
			String action=subPartes[0];
			String paramAction=subPartes[1];
			switch(action) {
			case "C": // Create objects
				String[] subParamC = paramAction.split(":");
				int canCrear=Integer.valueOf(subParamC[0]);
				String classCrear=subParamC[1];
				for (int n=0;n<canCrear;n++) {
					String nomObjNew=findNomProposed(classCrear);
					bNewObj=true;
					MClass oClassCreate = findMClassByName(classCrear);
					// Inicializar objeto
					saveObject(oClassCreate, nomObjNew);
					cmbObjectOri.setModel(loadComboObjectMObject(cmbClassOri));
					cmbObjectDes.setModel(loadComboObjectMObject(cmbClassDes));
					System.out.println("Creo objeto ["+nomObjNew+"] [" + classCrear+"]");
					fLogWriter.println("Create object ["+nomObjNew+"] [" + classCrear+"]");
					lNewObjects.add(nomObjNew);
					cmbObjectOri.setModel(loadComboObjectMObject(cmbClassOri));
					cmbObjectDes.setModel(loadComboObjectMObject(cmbClassDes));
				}

				break;
			case "I": // Insert links
				String[] subParamI = paramAction.split(":");
				String objPral=subParamI[0];
				String restoParam=subParamI[1];
				if (restoParam.contains("(NEWS)")) {
					// Se han de recuperar los objetos creados y sustituir (NEWS) por dichos objetos
					// Los objetos estan en lNewObjects
					String agrupador="";
					for(String newObject: lNewObjects) {
						if (agrupador!="") {
							agrupador+=",";
						}
						agrupador+=newObject;
					}
					String strPaso=restoParam.replace("(NEWS)", agrupador);
					restoParam=strPaso;
				}
				String[] objsLinkar = restoParam.split(",");
				int nObjs = objsLinkar.length;
				for(int nObj=0;nObj<nObjs;nObj++) {
					String objLinkar = objsLinkar[nObj];

					System.out.println("Insert link entre ["+objPral+"] [" + objLinkar+"]");

					MObject oOri=null;
					MObject oDes=null;
					MObject o1 = findObjectByName(objPral);
					MObject o2 = findObjectByName(objLinkar);
					// Averiguar el orden correcto para oOri i oDes segun oAssocPralWizard
					List<MAssociationEnd> oAsocEnds = oAssocPralWizard.associationEnds();

					int na=0;
					MAssociationEnd oAssocEnd = oAsocEnds.get(na);
					MClass oClassAssocEnd = oAssocEnd.cls();
					if (oClassAssocEnd.name().equals(o1.cls().name())) {
						oOri = o1;
						oDes =	o2;	
					}else {
						oOri = o2;
						oDes =	o1;	
					}

					MObject[] fParticipants = new MObject[] {oOri,oDes};
					insertLink(oAssocPralWizard, fParticipants);
					fLogWriter.println("Inserto link entre ["+oOri.name()+"] y [" + oDes.name()+"]");
				}
				break;
			case "D": // Delete object
				// Hay que borrar objeto indicado en paramAction 

				int idxActual = lObjects.getSelectedIndex();
				nomObj = paramAction;
				deleteObject(nomObj);
				int nObjects = lObjects.getModel().getSize();
				if (nObjects>0) {
					if (idxActual>nObjects) {
						idxActual=nObjects;
					}
					lObjects.setSelectedIndex(idxActual);
					nomObj = (String) lObjects.getSelectedValue();
					selectObject( nomObj);
				}else {
					fTableModel.update();
				}

				break;
			default:

			}
			setComposAssoc(oAssocPralWizard);
		}
		setResClassInvariants();
		setResCheckStructure();
	}
	public String findNomProposed(String className) {
		int numObj = 1;
		String nomProposed = className.toLowerCase() + numObj; 

		// Averiguar si existe objeto o no en base a una iteracion

		while(existObject(nomProposed)) {
			numObj+=1;
			nomProposed = className.toLowerCase() + numObj; 
		}
		System.out.println("nomProposed [" + nomProposed+"]");
		return nomProposed;
	}
	public void newObjectAuto() {
		oClass = lClass.getSelectedValue();
		nomClass = oClass.name();
		String nomProposed = findNomProposed(nomClass);

		bNewObj=true;
		saveObject(oClass, nomProposed);
		setResClassInvariants();
		setResCheckStructure();
		bNewObj=false;
		txNewObject.setEnabled(false);
		selectObject(nomProposed);
		nomObj = nomProposed;
		cmbObjectOri.setModel(loadComboObjectMObject(cmbClassOri));
		cmbObjectDes.setModel(loadComboObjectMObject(cmbClassDes));
		selectObject( nomObj);
	}
	public boolean existObject(String name) {
		boolean bRes=false;
		MSystemState state = fSystem.state();
		Set<MObject> allObjects = state.allObjects();

		for (MObject obj : allObjects) {
			if (obj.name().equals(name)) {
				bRes=true;
				return bRes;
			}
		}		

		return bRes;
	}

	public void setResCheckStructure() {
		boolean bRes = checkStructure();
		String sRes="OK";
		if (!bRes) sRes="KO";
		btnShowCheckStructure.setText(sRes);
		if(!bRes) {
			btnShowCheckStructure.setForeground(Color.white);
			btnShowCheckStructure.setBackground(Color.red);
		}else {
			btnShowCheckStructure.setForeground(Color.black);
			btnShowCheckStructure.setBackground(Color.green);
		}
	}

	public void setResClassInvariants() {
		boolean bRes = check_inv_state();
		String sRes="OK";
		if (!bRes) sRes="KO";
		lbResClassInvariants.setText(sRes);
		btnShowClassInvariants.setText(sRes);
		if(!bRes) {
			lbResClassInvariants.setForeground(Color.white);
			lbResClassInvariants.setBackground(Color.red);
			btnShowClassInvariants.setForeground(Color.white);
			btnShowClassInvariants.setBackground(Color.red);
		}else {
			lbResClassInvariants.setForeground(Color.black);
			lbResClassInvariants.setBackground(Color.green);
			btnShowClassInvariants.setForeground(Color.black);
			btnShowClassInvariants.setBackground(Color.green);
		}
		lbResClassInvariants.setOpaque(true);

	}
	public void showClassInvariantsState() {
		ClassInvariantView civ = new ClassInvariantView(fMainWindow,
				fSession.system());
		ViewFrame f = new ViewFrame("Class invariants", civ,
				"InvariantView.gif");
		civ.setViewFrame(f);
		JComponent c = (JComponent) f.getContentPane();
		c.setLayout(new BorderLayout());
		c.add(civ, BorderLayout.CENTER);
		fMainWindow.addNewViewFrame(f);
	}
	public boolean check_inv_state() {
		boolean bRes = false;

		MModel fModel = fSystem.model();
		int n = fModel.classInvariants().size();
		MClassInvariant[] fClassInvariants = new MClassInvariant[0];
		fClassInvariants = new MClassInvariant[n];
		System.arraycopy(fModel.classInvariants().toArray(), 0,
				fClassInvariants, 0, n);
		Arrays.sort(fClassInvariants);
		EvalResult[] fValues;
		fValues = new EvalResult[n];
		for (int i = 0; i < fValues.length; i++) {
			fValues[i] = null;
		}
		ExecutorService executor = Executors.newFixedThreadPool(Options.EVAL_NUMTHREADS);
		futures = new ArrayList<Future<EvalResult>>();
		ExecutorCompletionService<EvalResult> ecs = new ExecutorCompletionService<EvalResult>(executor);
		boolean violationLabel = false; 
		int numFailures = 0;
		boolean structureOK = true;
		for (int i = 0; i < fClassInvariants.length; i++) {
			if(!fClassInvariants[i].isActive()){
				continue;
			}
			MyEvaluatorCallable cb = new MyEvaluatorCallable(fSystem.state(), i, fClassInvariants[i]);
			futures.add(ecs.submit(cb));
		}

		for (int i = 0; i < fClassInvariants.length && !isCancelled(); i++) {
			if(!fClassInvariants[i].isActive()){
				continue;
			}
			try {
				EvalResult res;
				res = ecs.take().get();
				fValues[res.index] = res;

				boolean ok = false;
				// if v == null it is not considered as a failure, rather it is
				// a MultiplicityViolation and it is skipped as failure count
				boolean skip = false;
				if (res.result != null) {
					ok = res.result.isDefined() && ((BooleanValue)res.result).isTrue();
				} else {
					violationLabel = true;
					skip = true;
				}

				if (!skip && !ok)
					numFailures++;

			} catch (InterruptedException ex) {
				break;
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		}

		for (Future<EvalResult> f : futures) {
			f.cancel(true);
		}
		boolean todosOk=true;
		for (EvalResult res : fValues) {
			Boolean boolRes=  ((BooleanValue)res.result).value();

			if (boolRes.equals(false)) todosOk=false;
			System.out.println("res.index ["+res.index+"]");
			System.out.println("Para invs res ["+fClassInvariants[res.index].name()+"] result ["+res.result+"]");
		}
		System.out.println("todosOk ["+todosOk+"]");
		executor.shutdown();

		return todosOk;
	}

	private void getErrorsEstructure() {
		lAssocsWizard = new ArrayList<AssocWizard>();
		StringWriter buffer = new StringWriter();
		PrintWriter out = new PrintWriter(buffer);
		boolean reportAllErrors=true;
		lAssocsWizard = fSession.system().state().checkStructureWithErrorsInfo( out, 
				reportAllErrors);
		return;
	}
	private boolean checkStructure() {
		Map<String, List<String>> mapObjects = new HashMap<String, List<String>>();
		getErrorsEstructure(); //Provis
		// En una pasada se han de ver los objetos que admiten una multiplicidad de * para que esten disponibles
		// Se han de ver todas las asociaciones finales que hay y ver que objetos siempre estan disponibles 

		for(AssocWizard oAssoc: lAssocsWizard) {
			MAssociation oAssocModel = oAssoc.getassocModel();
			List<MAssociationEnd> oAsoccEnds = oAssocModel.associationEnds();

			MAssociationEnd oAssocEnd1 = oAsoccEnds.get(0);
			MAssociationEnd oAssocEnd2 = oAsoccEnds.get(1);
			// Si la primera tiene multi *, los objetos de la segunda debe ser disponible			
			if (oAssocEnd1.multiplicity().equals("*")) {
				// Buscamos objetos de la segunda
				MClass oClassBuscar = oAssocEnd2.cls();
				mapObjects=addAndFindObjectsIntoMap(mapObjects, oClassBuscar);
			}
			// Si la segunda tiene multi *, los objetos de la primera debe ser disponible			
			if (oAssocEnd2.multiplicity().getRanges().get(0).toString().equals("*")) {
				// Buscamos objetos de la primera
				MClass oClassBuscar = oAssocEnd1.cls();
				mapObjects=addAndFindObjectsIntoMap(mapObjects, oClassBuscar);
			}
		}
 
		System.out.println("Ya");
		List<AssocWizard> lAssocsWizardPaso = new ArrayList<AssocWizard>();
		for(AssocWizard oAssoc: lAssocsWizard) {
			lAssocsWizardPaso.add(oAssoc);
		}
		lAssocsWizard.clear();
		for (AssocWizard aw: lAssocsWizardPaso) {
			System.out.println("aw ["+aw.getName()+"]");	
			for (LinkWizard lw: aw.getlLinks()) {
				int needed = lw.getNeeded();
				String objectName = lw.getObject();
				String nomClass = lw.getNomClass(); //Clase del objeto principal
				String classOfName = lw.getOfClass(); // Clase del objeto que necesita
				System.out.println("lw ["+objectName+"] cause ["+lw.getCause()+"] necesita ["+needed+"] de la clase ["+classOfName+"]");
				// Pasada para ver los objetos disponibles por clase
				if (needed>0) {
					List lObjDisponibles = new ArrayList<String>();
					// Si tiene alguna necesidad es que puede linkarse con otros objetos segun relacion
					if (mapObjects.containsKey(nomClass)) {
						lObjDisponibles=mapObjects.get(nomClass);
						if (!lObjDisponibles.contains(objectName)) {
							lObjDisponibles.add(objectName);
						}
					}else{
						lObjDisponibles.add(objectName);
						mapObjects.put(nomClass, lObjDisponibles);
					}
				}
			}
			// Revision de clases disponibles y objetos disponibles de cada una de las mismas
			for (Map.Entry<String, List<String>> entry : mapObjects.entrySet()) {
				String className = (String) entry.getKey();
				List<String> lObjDisponibles = new ArrayList<String>();
				lObjDisponibles = entry.getValue();
				System.out.println("La clase ["+className+"] tiene disponibles:");
				for(String nameObject: lObjDisponibles) {
					System.out.println("   Object ["+nameObject+"] ");
				}
			}
			// Analisis de problemas a solucionar
			analyzeProposals(aw, mapObjects);
		}
		boolean ok = true;
		if (mapObjects.size()>0) ok=false;
		return ok;
	}

	private boolean checkStructure_old() {

		boolean ok = checkStructure();
		//		return false;
		// Provis ----------------------
		//		List<AssocWizard> lAssocsWizard2=getErrorsEstructure(); //Provis
		//		
		//		Map<String, List<String>> mapObjects = new HashMap<String, List<String>>();
		//
		//		StringWriter buffer = new StringWriter();
		//		PrintWriter out = new PrintWriter(buffer);
		//		boolean ok = fSession.system().state().checkStructure(out);
		//
		//		System.out.println("Total ["+ok+"]");
		//		System.out.println("Totalout ["+buffer+"]");
		//
		//		lAssocsWizard = new ArrayList<AssocWizard>();
		//
		//		String msgRef = buffer.toString();
		//		if (msgRef.contains("Multiplicity")) {
		//
		//			String msg = msgRef.replace("\r\n", "");
		//			String[] lineas = msg.split("\\.");
		//			int nLineas = lineas.length;
		//			System.out.println("nLineas"+nLineas+"]");
		//			for (int nLinea=0;nLinea<nLineas;nLinea++) {
		//				String linea = lineas[nLinea];
		//				System.out.println("Linea ["+linea+"]");
		//				if (linea.contains("Multiplicity constraint violation")) {
		//					String cause="";
		//					String fullMessage="";
		//					String nomAssociation="";
		//					String nomAssocObject="";
		//					String nomAssocClass="";
		//					String connectedNum="";
		//					String connectedClass="";
		//					String assoccEnd="";
		//					String multiplicity="";	
		//					MObject oObjectPral=null;
		//					//Aqui6
		//
		//					fullMessage=linea;
		//					String linea2 = linea.replaceFirst("of class","of class1");
		//					linea=linea2;
		//					String[] partes = linea.split("'");
		//					int nPartes = partes.length;
		//					for (int nParte=0;nParte<nPartes;nParte++) {
		//						String parte = partes[nParte];
		//						System.out.println("Parte ["+parte+"]");
		//						//Multiplicity constraint violation
		//						if (parte.contains("Multiplicity constraint violation")) {
		//							String[] subPartes = parte.split(":");
		//							cause=subPartes[0]+"'";
		//							String[] subPartes2 = parte.split("`");
		//							nomAssociation=subPartes2[1];
		//						}
		//						if (parte.contains(":  Object")) {
		//							String[] subPartes = parte.split("`");
		//							nomAssocObject=subPartes[1];
		//							oObjectPral = findObjectByName(nomAssocObject);
		//						}else if (parte.contains("of class1")) {
		//							String[] subPartes = parte.split("`");
		//							nomAssocClass=subPartes[1];
		//						}else if (parte.contains("objects of class")||parte.contains("object of class")) {
		//							String[] subPartesSPC = parte.split(" ");
		//							connectedNum = subPartesSPC[4];
		//							String[] subPartes = parte.split("`");
		//							connectedClass=subPartes[1];
		//						}else if (parte.contains("at association end")) {
		//							String[] subPartes = parte.split("`");
		//							assoccEnd=subPartes[1];
		//						}else if (parte.contains("but the multiplicity")) {
		//							String[] subPartes = parte.split("`");
		//							multiplicity=subPartes[1];
		//						}
		//					}
		//
		//					// Busca assoc en lista
		//
		//					List<LinkWizard> lLinksWizard = new ArrayList<LinkWizard>();
		//
		//					AssocWizard aw = new AssocWizard();
		//					boolean existAssocWizard=false;
		//					int indexAssoc=-1;
		//					for(indexAssoc=0;indexAssoc<lAssocsWizard.size();indexAssoc++) {
		//						AssocWizard awl = lAssocsWizard.get(indexAssoc);
		//						if (awl.getName().equals(nomAssociation)) {
		//							aw = awl;
		//							existAssocWizard=true;
		//							break;
		//						}
		//					}
		//					MAssociation oAassocModel = findAssocByName(nomAssociation);
		//					if (existAssocWizard) {
		//						lLinksWizard=aw.getlLinks();
		//					}else {
		//						aw.setName(nomAssociation);
		//						aw.setState("ko");
		//						aw.setassocModel(oAassocModel);
		//					}
		//
		//					// Si no esta, crea nueva assoc
		//					// Si esta, la usa
		//					System.out.println("cause ["+cause+"]");
		//					System.out.println("nomAssociation  ["+ nomAssociation +"]");
		//					System.out.println("nomAssocObject ["+nomAssocObject+"]");
		//					System.out.println("nomAssocClass ["+nomAssocClass+"]");
		//					System.out.println("connectedNum ["+connectedNum+"]");
		//					System.out.println("connectedClass ["+connectedClass+"]");
		//					System.out.println("assoccEnd ["+assoccEnd+"]");
		//					System.out.println("multiplicity ["+multiplicity+"]");	
		//					//				
		//					LinkWizard lw = new LinkWizard();
		//					lw.setObject(nomAssocObject);
		//					lw.setNomClass(nomAssocClass);
		//					lw.setConnectedTo(connectedNum);
		//					lw.setOfClass(connectedClass);
		//					lw.setAssocEnd(assoccEnd);
		//					lw.setMultiSpecified(multiplicity);
		//					lw.setCause(cause);
		//					lw.setFullMessage(fullMessage);
		//					lw.setoMObject(oObjectPral);
		//					//					oObjectPral
		//
		//					int multi = 0; 
		//					int connectedTo=0; 
		//					try {
		//						multi = Integer.parseInt(lw.getMultiSpecified()); 
		//					}catch(Exception e) {}
		//					try {
		//						connectedTo=Integer.parseInt(lw.getConnectedTo()); 
		//					}catch(Exception e) {}
		//					int needed = multi-connectedTo;
		//					if (needed < 0) 
		//						needed=0;
		//					lw.setNeeded(needed);
		//
		//					lLinksWizard.add(lw);
		//
		//					aw.setlLinks(lLinksWizard);
		//
		//					if (existAssocWizard) {
		//						lAssocsWizard.remove(indexAssoc);
		//						lAssocsWizard.add(aw);
		//					}else {
		//						lAssocsWizard.add(aw);
		//					}
		//				}
		//
		//			}
		//
		//		}
		//
		//		// En una pasada se han de ver los objetos que admiten una multiplicidad de * para que esten disponibles
		//		// Se han de ver todas las asociaciones finales que hay y ver que objetos siempre estan disponibles 
		//
		//		for(AssocWizard oAssoc: lAssocsWizard) {
		//			MAssociation oAssocModel = oAssoc.getassocModel();
		//			List<MAssociationEnd> oAsoccEnds = oAssocModel.associationEnds();
		//
		//			MAssociationEnd oAssocEnd1 = oAsoccEnds.get(0);
		//			MAssociationEnd oAssocEnd2 = oAsoccEnds.get(1);
		//			// Si la primera tiene multi *, los objetos de la segunda debe ser disponible			
		//			if (oAssocEnd1.multiplicity().equals("*")) {
		//				// Buscamos objetos de la segunda
		//				MClass oClassBuscar = oAssocEnd2.cls();
		//				mapObjects=addAndFindObjectsIntoMap(mapObjects, oClassBuscar);
		//			}
		//			// Si la segunda tiene multi *, los objetos de la primera debe ser disponible			
		//			if (oAssocEnd2.multiplicity().getRanges().get(0).toString().equals("*")) {
		//				// Buscamos objetos de la primera
		//				MClass oClassBuscar = oAssocEnd1.cls();
		//				mapObjects=addAndFindObjectsIntoMap(mapObjects, oClassBuscar);
		//			}
		//		}
		//
		//		System.out.println("Ya");
		//		List<AssocWizard> lAssocsWizardPaso = new ArrayList<AssocWizard>();
		//		for(AssocWizard oAssoc: lAssocsWizard) {
		//			lAssocsWizardPaso.add(oAssoc);
		//		}
		//		lAssocsWizard.clear();
		//		for (AssocWizard aw: lAssocsWizardPaso) {
		//			System.out.println("aw ["+aw.getName()+"]");	
		//			for (LinkWizard lw: aw.getlLinks()) {
		//				int needed = lw.getNeeded();
		//				String objectName = lw.getObject();
		//				String nomClass = lw.getNomClass(); //Clase del objeto principal
		//				String classOfName = lw.getOfClass(); // Clase del objeto que necesita
		//				System.out.println("lw ["+objectName+"] cause ["+lw.getCause()+"] necesita ["+needed+"] de la clase ["+classOfName+"]");
		//				// Pasada para ver los objetos disponibles por clase
		//				if (needed>0) {
		//					List lObjDisponibles = new ArrayList<String>();
		//					// Si tiene alguna necesidad es que puede linkarse con otros objetos segun relacion
		//					if (mapObjects.containsKey(nomClass)) {
		//						lObjDisponibles=mapObjects.get(nomClass);
		//						if (!lObjDisponibles.contains(objectName)) {
		//							lObjDisponibles.add(objectName);
		//						}
		//					}else{
		//						lObjDisponibles.add(objectName);
		//						mapObjects.put(nomClass, lObjDisponibles);
		//					}
		//				}
		//			}
		//			// Revision de clases disponibles y objetos disponibles de cada una de las mismas
		//			for (Map.Entry<String, List<String>> entry : mapObjects.entrySet()) {
		//				String className = (String) entry.getKey();
		//				List<String> lObjDisponibles = new ArrayList<String>();
		//				lObjDisponibles = entry.getValue();
		//				System.out.println("La clase ["+className+"] tiene disponibles:");
		//				for(String nameObject: lObjDisponibles) {
		//					System.out.println("   Object ["+nameObject+"] ");
		//				}
		//			}
		//			// Analisis de problemas a solucionar
		//			analyzeProposals(aw, mapObjects);
		//		}
		// Provis ----------------------
		return ok;
	}
	private Map<String, List<String>> addAndFindObjectsIntoMap(Map<String, List<String>> mapObjects, 
			MClass oClassBuscar){
		MSystemState state = fSystem.state();
		Set<MObject> allObjects = state.allObjects();
		for (MObject obj : allObjects) {
			String objectName = obj.name();
			String strClassName = oClassBuscar.name();
			if (obj.cls().name().equals(strClassName)) {
				List<String> lObjDisponibles = new ArrayList<String>();

				if (mapObjects.containsKey(strClassName)) {
					lObjDisponibles=mapObjects.get(strClassName);
					if (!lObjDisponibles.contains(objectName)) {
						lObjDisponibles.add(objectName);
						mapObjects.replace(strClassName, lObjDisponibles);
					}
				}else{
					lObjDisponibles.add(objectName);
					mapObjects.put(strClassName, lObjDisponibles);
				}
			}
		}
		return mapObjects;
	}
	/** En base a la estructura de un objecto de la associacion, propone crear y/o linkar
	 * dicho objeto a otros
	 * 
	 */
	public void analyzeProposals(AssocWizard aw,Map<String, List<String>> mapObjects) {

		AssocWizard awNew = aw;
		System.out.println("aw ["+aw.getName()+"]");	
		List<LinkWizard> oLinks = aw.getlLinks();
		List<LinkWizard> oNewLinks = new ArrayList<LinkWizard>();

		for (LinkWizard lw: oLinks) {
			List<String> lObjAsignar = new ArrayList<String>();
			int needed = lw.getNeeded();
			int cover = 0;
			int pending=0;
			int canAssig = 0;
			int mustCreate = 0;
			String objectNameToSolve = lw.getObject();// Object a solucionar
			String nomClass = lw.getNomClass(); //Clase del objeto principal
			String classNeeded = lw.getOfClass(); // Clase del objeto que se necesita
			System.out.println("lw ["+objectNameToSolve+"] cause ["+lw.getCause()+"] necesita ["+needed+"] de la clase ["+classNeeded+"]");
			// Buscar cuantos objetos necesarios estan disponibles

			String strAssig="";
			for (Map.Entry<String, List<String>> entry : mapObjects.entrySet()) {
				String className = (String) entry.getKey();
				if (className.equals(classNeeded)){
					List<String> lObjDisponibles = new ArrayList<String>();
					lObjDisponibles = entry.getValue();

					for(String nameObject: lObjDisponibles) {
						pending = needed - cover;
						if (pending > 0 ) {
							System.out.println("   Object ["+nameObject+"] ");
							lObjAsignar.add(nameObject);
							cover+=1;
							canAssig+=1;
							if (strAssig!="") {
								strAssig+=",";
							}
							strAssig+=nameObject;
						}
					}
				}
			}
			mustCreate = needed - cover;
			Map<String, String> mapActions = new HashMap<String, String>();
			String linkAction="";
			if (canAssig > 0) {
				System.out.println("Para ["+objectNameToSolve+"] asignamos ["+canAssig+"] ["+strAssig+"] y queda pendiente ["+mustCreate+"]");
				mapActions.put("A", strAssig);
				linkAction=objectNameToSolve+":"+strAssig;
			}

			// Si queda algun objeto pendiente de asignar hemos de crearlo
			if (mustCreate > 0 ) {
				System.out.println("Para ["+objectNameToSolve+"] hemos de crear ["+mustCreate+"] objects de tipo ["+classNeeded+"]");
				mapActions.put("C-"+mustCreate, classNeeded);
				// Considerar calcular los nombres de los nuevos objectos para cambiar NEWS por dichos nombres
				if (linkAction!="") {
					linkAction+=",(NEWS)";
				}else {
					linkAction=objectNameToSolve+":(NEWS)";
				}
			}
			if (linkAction!="") {
				mapActions.put("I", linkAction);
			}
			// Insertamos acciones en lw
			lw.setMapActions(mapActions);
			System.out.println();
			for (Map.Entry<String, String> entry : mapActions.entrySet()) {
				String actionW =  entry.getKey();
				String infoW = entry.getValue();
				System.out.println("ActionW ["+actionW+"]");
				System.out.println("InfoW   ["+infoW+"]");
			}
			oNewLinks.add(lw);
		}
		awNew.setlLinks(oNewLinks);
		lAssocsWizard.add(awNew);
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

		for (MObject obj : allObjects) {
			if (obj.cls().name().equals(nomClass)) {
				ldefLModel.addElement(obj.name());
			}
		}
		return ldefLModel;
	}
	private  DefaultComboBoxModel<MClass> loadComboClass() {
		DefaultComboBoxModel<MClass> cbm = new DefaultComboBoxModel<MClass>();

		for (MClass oClass : fSystem.model().classes()) {
			cbm.addElement(oClass);
		}
		return cbm;

	}

	private  DefaultComboBoxModel<MObject> loadComboObjectMObject(JComboBox<MClass> cmbClass) {
		DefaultComboBoxModel<MObject> cbm = new DefaultComboBoxModel<MObject>();
		MClass oClass = (MClass) cmbClass.getItemAt(cmbClass.getSelectedIndex());
		MSystemState state = fSystem.state();
		Set<MObject> allObjects = state.allObjects();

		for (MObject obj : allObjects) {
			if (obj.cls().name().equals(oClass.name())) {
				cbm.addElement(obj);
			}
		}

		return cbm;
	}

	private DefaultListModel<MAssociation> loadListAssoc() {
		DefaultListModel<MAssociation> ldefLModel = new DefaultListModel<MAssociation>();
		for (MAssociation oAssoc : fSystem.model().associations()) {
			ldefLModel.addElement(oAssoc);
		}
		return ldefLModel;
	}
	private void setComposAssoc(MAssociation oAssoc) {

		Set<MLink> links = fSystem.state().linksOfAssociation(oAssoc).links();
		// Si no hay links, se han de inicializar los combos en base a las AssociationEnds
		int nLink=0;
		if (links.size()==0) {
			for (MAssociationEnd ma:oAssoc.associationEnds()) {
				String className=ma.cls().name();
				switch(nLink){
				case 0:
					selectClasInCombo(cmbClassOri,className);
					lbFromClass.setText(className);
				case 1:
					selectClasInCombo(cmbClassDes,className);
					lbToClass.setText(className);
				default:
					// De momento no hacemos nada
					break;
				}
				nLink+=1;
			}
			return;
		}

		// Si hay links, se analizan los links y se muestra el primero
		// Inicialmente supondremos que solo hay un link

		for (MLink oLink:links) {
			System.out.println(oLink.linkedObjects());
			if (nLink==0) {
				MObject oOri=null;
				MObject oDes=null;

				int nLinkEnd=0;
				for(MLinkEnd oMlinkEnd: oLink.linkEnds()) {
					System.out.println(oMlinkEnd.object().name());
					MAssociationEnd oMAssociationEnd=oMlinkEnd.associationEnd();
					System.out.println(oMAssociationEnd.name());

					MMultiplicity oMMultiplicity=oMAssociationEnd.multiplicity();

					System.out.println(oMMultiplicity.toString());
					switch(nLinkEnd){
					case 0:
						oOri=oMlinkEnd.object();
						txOriAssocRole.setText(oMAssociationEnd.name());
						cmbClassOri.setSelectedItem(oOri.cls());
						cmbObjectOri.setSelectedItem(oOri);
						cmbMultiOri.setSelectedItem(oMMultiplicity.toString());
						txMultiOri.setText(oMMultiplicity.toString());
						lbFromClass.setText(oOri.cls().name());
						break;
					case 1:
						oDes=oMlinkEnd.object();
						txDesAssocRole.setText(oMAssociationEnd.name());
						cmbClassDes.setSelectedItem(oDes.cls());
						cmbObjectDes.setSelectedItem(oDes);
						cmbMultiDes.setSelectedItem(oMMultiplicity.toString());
						txMultiDes.setText(oMMultiplicity.toString());
						lbToClass.setText(oDes.cls().name());
						break;
					default:
						// De momento no hacemos nada
						break;
					}
					nLinkEnd+=1;
				}

			}
		}
		System.out.println(oAssoc.name());

	}
	private void insertLink(MAssociation oAssoc) {
		MObject oOri=null;
		MObject oDes=null;
		MObject o1 = cmbObjectOri.getItemAt(cmbObjectOri.getSelectedIndex());
		MObject o2 = cmbObjectDes.getItemAt(cmbObjectDes.getSelectedIndex());

		// Determinar orden de oOri y oDes
		List<MAssociationEnd> oAsocEnds = oAssoc.associationEnds();

		int na=0;
		MAssociationEnd oAssocEnd = oAsocEnds.get(na);
		MClass oClassAssocEnd = oAssocEnd.cls();
		if (oClassAssocEnd.name().equals(o1.cls().name())) {
			oOri = o1;
			oDes =	o2;	
		}else {
			oOri = o2;
			oDes =	o1;	
		}

		MObject[] fParticipants = new MObject[] {oOri,oDes};
		insertLink(oAssoc, fParticipants);
	}

	private void insertLink(MAssociation association, MObject[] objects) {
		if (association.hasQualifiedEnds()) {
			QualifierInputView input = new QualifierInputView(fMainWindow, fSystem, association, objects);
			input.setLocationRelativeTo(this);
			input.setVisible(true);

		} else {
			try {
				fSystem.execute(new MLinkInsertionStatement(association, objects, Collections.<List<Value>>emptyList()));
			} catch (MSystemException e) {
				JOptionPane.showMessageDialog(
						fMainWindow, 
						e.getMessage(), 
						"Error", 
						JOptionPane.ERROR_MESSAGE);
			}
		}
		setResCheckStructure();
	}
	private void deleteLink(MAssociation oAssoc) {
		// Averiguar el link del que se trata
		MObject oOri = cmbObjectOri.getItemAt(cmbObjectOri.getSelectedIndex());
		MObject oDes = cmbObjectDes.getItemAt(cmbObjectDes.getSelectedIndex());

		MLink oLinkTOdel=null;
		Set<MLink> links = fSystem.state().linksOfAssociation(oAssoc).links();
		// Inicialmente suponderemos que solo hay un link

		for (MLink oLink:links) {
			System.out.println(oLink.linkedObjects());
			//Si se trata de los objetos seleccionados en la UI es el link buscado

			int nLinksObj = oLink.linkedObjects().size();
			MObject o1=null;
			MObject o2=null;
			for(int nLinkObj = 0; nLinkObj<nLinksObj;nLinkObj++) {
				switch(nLinkObj){
				case 0:
					o1=oLink.linkedObjects().get(nLinkObj);
					break;
				case 1:
					o2=oLink.linkedObjects().get(nLinkObj);
					break;
				default:
					break;
				}
			}
			if (o1!=null && o2!=null) {
				if (oOri.equals(o1) && oDes.equals(o2) || oOri.equals(o2) && oDes.equals(o1))
					oLinkTOdel = oLink;
				continue;
			}
		}

		if(oLinkTOdel!=null) {
			System.out.println("Borra ["+oLinkTOdel.toString()+"]");
			deleteLink(oLinkTOdel);
		}

	}
	private void deleteLink(MLink link) {
		try {
			fSystem.execute(
					new MLinkDeletionStatement(link));
		} catch (MSystemException e) {
			JOptionPane.showMessageDialog(
					fMainWindow, 
					e.getMessage(), 
					"Error", 
					JOptionPane.ERROR_MESSAGE);
		}
		// Localizar diagrama y ver si se puede actualizar
		for (NewObjectDiagramView odv: fMainWindow.getObjectDiagrams()) {
			if (odv.getName().equals(NAMEFRAMEMVMDIAGRAM)) {
				odv.repaint();
			}
		}
		setResCheckStructure();
	}

	private void searchObjDiagramAssociated() {
		odvAssoc = null; 
		for (NewObjectDiagramView odv: fMainWindow.getObjectDiagrams()) {
			if (odv.getName()!=null) {
				if (odv.getName().equals(NAMEFRAMEMVMDIAGRAM)) {
					odvAssoc = odv.getDiagram();
					return;
				}
			}
		}
	}

	private void createObjDiagram() {
		NewObjectDiagramView odv = new NewObjectDiagramView(fMainWindow, fSession.system());
		ViewFrame f = new ViewFrame("Object diagram", odv, "ObjectDiagram.gif");
		f.setName(NAMEFRAMEMVMDIAGRAM);
		odv.setName(NAMEFRAMEMVMDIAGRAM);

		int OBJECTS_LARGE_SYSTEM = 100;

		// Many objects. Ask user if all objects should be hidden
		if (fSession.system().state().allObjects().size() > OBJECTS_LARGE_SYSTEM) {

			int option = JOptionPane.showConfirmDialog(new JPanel(),
					"The current system state contains more then " + OBJECTS_LARGE_SYSTEM + " instances." +
							"This can slow down the object diagram.\r\nDo you want to start with an empty object diagram?",
							"Large system state", JOptionPane.YES_NO_OPTION);

			if (option == JOptionPane.YES_OPTION) {
				odv.getDiagram().hideAll();
			}
		}

		JComponent c = (JComponent) f.getContentPane();
		c.setLayout(new BorderLayout());
		c.add(odv, BorderLayout.CENTER);
		fMainWindow.addNewViewFrame(f);
		fMainWindow.getObjectDiagrams().add(odv);
		odvAssoc = odv.getDiagram();

		tile();
	}

	private void initNewObject() {
		txNewObject.setText("");
		txNewObject.setEnabled(true);
		bNewObj=true;
	}

	private void cancelObject(String nomObj) {
		selectObject(nomObj);
	}
	private boolean checkExistObjDiagram() {
		boolean existDiagram=false;
		// Ver frames
		JDesktopPane fDesk = fMainWindow.getFdesk();
		allframes = fDesk.getAllFrames();

		for (JInternalFrame ifr: allframes) {
			if (ifr.getName()!=null ) {
				if (ifr.getName().equals(NAMEFRAMEMVMDIAGRAM)) {
					existDiagram=true;
					continue;
				}	
			}
		}

		if (!existDiagram) {
			createObjDiagram();
		}
		return existDiagram;
	}
	private void createObject(MClass oClass, String nomObj) {

		checkExistObjDiagram();
		fMainWindow.createObject(oClass, nomObj);
		lObjects.setModel(loadListObjects(nomClass));

		if (chkAutoLayout.isSelected()) {
			odvAssoc.forceStartLayoutThread();
		}
	}
	private void saveObject(MClass oClass, String nomObj) {
		// Verificamos existencia de ObjectDiagram MVM
		checkExistObjDiagram();

		if (bNewObj) {
			// Hacer copia de fValues siempre que la clase sea la misma
			//			String nameClassAttributes = ((MAttribute) fAttributes[0]).

			boolean sameClass=false;
			if (fAttributes.size()>0) {
				MAttribute attr1 = fAttributes.get(0);
				if (attr1.owner().name().equals(oClass.name())) {
					sameClass=true;
				}			
			}

			String[] fValuesAnt = new String[fAttributes.size()];
			if (sameClass) {
				for (int i = 0; i < fAttributes.size(); i++) {
					fValuesAnt[i] = fValues[i];
				}
			}
			if (!existObject(nomObj)) {
				createObject(oClass, nomObj);
			}

			selectObject( nomObj);
			if (fValues.length == fValuesAnt.length && sameClass) {
				for (int i = 0; i < fAttributes.size(); i++) {
					fValues[i] = fValuesAnt[i];
				}
			}else {
				for (int i = 0; i < fAttributes.size(); i++) {
					MAttribute attr = (MAttribute) fAttributes.get(i);
					attr.type();
					if (attr.type().isTypeOfInteger()) {
						fValues[i] = "1";
					}else if (attr.type().isTypeOfString()) {
						fValues[i] = "'x'";
					}
				}
			}
		}
		applyChanges();
		if (!checkExistObjDiagram()) {
			odvAssoc.forceStartLayoutThread();
		}
		//		setResClassInvariants();
		//		setResCheckStructure();

	}
	private void deleteObject(String nomObjDel) {
		int idx = selectObject(nomObjDel);
		MSystemState state = fSystem.state();
		// Localizar diagrama y ver si se puede actualizar
		for (NewObjectDiagramView odv: fMainWindow.getObjectDiagrams()) {
			if (odv.getName().equals(NAMEFRAMEMVMDIAGRAM)) {
				odv.getDiagram().deleteObject(fObject);
				odv.repaint();
				//				idx=idx-1;

			}
		}
		state.deleteObject(fObject);
		lObjects.setModel(loadListObjects(nomClass));
		//		if (idx<0) idx=0;
		//		if (idx>-1) {
		//			lObjects.setSelectedIndex(idx);
		//			nomObj = (String) lObjects.getSelectedValue();
		//		}else {
		//			fTableModel.update();
		//		}
		int nObjects = lObjects.getModel().getSize();
		if (nObjects>0) {
			if (idx>nObjects-1) {
				idx=nObjects-1;
			}
			lObjects.setSelectedIndex(idx);
			nomObj = (String) lObjects.getSelectedValue();
			selectObject( nomObj);
		}else {
			fTableModel.update();
		}
		setResClassInvariants();
		setResCheckStructure();
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
	public int selectObject(String objName) {
		int idx = -1;
		MSystemState state = fSystem.state();
		fObject = state.objectByName(objName);
		fTableModel.update();

		// Buscar en lista
		lObjects.setSelectedIndex(0);
		int nObjs= lObjects.getModel().getSize();
		for (int nObj=0;nObj<nObjs;nObj++) {
			if (lObjects.getModel().getElementAt(nObj).equals(objName)) {
				lObjects.setSelectedIndex(nObj);
				idx=nObj;
				return idx;
			}
		}
		return idx;
	}
	private MObject findObjectByName(String nameObject) {
		MObject oRes=null;
		MSystemState state = fSystem.state();
		oRes=state.objectByName(nameObject);
		return oRes;
	}

	private MClass findMClassByName(String nameClass) {
		MClass oRes=null;
		for (MClass oClass : fSystem.model().classes()) {
			if (oClass.name().equals(nameClass)) {
				oRes=oClass;
				return oRes;
			}
		}
		return oRes;
	}


	private MAssociation findAssocByName(String nameAssoc) {
		MAssociation oRes=null;
		for (MAssociation oAssoc : fSystem.model().associations()) {
			if (oAssoc.name().equals(nameAssoc)) {
				oRes=oAssoc;
				return oRes;
			}
		}		
		return oRes;
	}

	public void selectClasInCombo(JComboBox<MClass> cmb, String className) {

		// Buscar en lista
		int nObjs= cmb.getModel().getSize();
		for (int nObj=0;nObj<nObjs;nObj++) {
			MClass cl = (MClass) cmb.getModel().getElementAt(nObj);
			if (cl.name().equals(className)) {
				cmb.setSelectedIndex(nObj);
				return;
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
	private void tile() {
		JDesktopPane fDesk = fMainWindow.getFdesk();
		//		allframes = fDesk.getAllFrames();
		allframes = fMainWindow.sortInternalFrames(fDesk.getAllFrames());
		int count = allframes.length;
		if (count == 0)
			return;

		// Determine the necessary grid size
		int sqrt = (int) Math.sqrt(count);
		int rows = sqrt;
		int cols = sqrt;
		if (rows * cols < count) {
			cols++;
			if (rows * cols < count) {
				rows++;
			}
		}

		// Define some initial values for size & location
		Dimension size = fDesk.getSize();

		int w = size.width / cols;
		int h = size.height / rows;
		int x = 0;
		int y = 0;

		// Iterate over the frames, deiconifying any iconified frames and
		// then relocating & resizing each
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols && ((i * cols) + j < count); j++) {
				JInternalFrame f = allframes[(i * cols) + j];

				if (f.isIcon() && !f.isClosed()) {
					try {
						f.setIcon(false);
					} catch (PropertyVetoException ex) {
						// ignored
					}
				}
				fDesk.getDesktopManager().resizeFrame(f, x, y, w, h);
				x += w;
			}
			y += h; // start the next row
			x = 0;
		}

	}
	public final boolean isCancelled() {
		//        return future.isCancelled();
		return false;
	}
	private class EvalResult {
		public final int index;
		public final Value result;
		public final String message;
		public final long duration;

		public EvalResult(int index, Value result, String message, long duration) {
			this.index = index;
			this.result = result;
			this.message = message;
			this.duration = duration;
		}
	}
	private class MyEvaluatorCallable implements Callable<EvalResult> {
		final int index;
		final MSystemState state;
		final MClassInvariant inv;

		public MyEvaluatorCallable(MSystemState state, int index, MClassInvariant inv) {
			this.state = state;
			this.index = index;
			this.inv = inv;
		}

		@Override
		public EvalResult call() throws Exception {
			if (isCancelled()) return null;

			Evaluator eval = new Evaluator();
			Value v = null;
			String message = null;
			long start = System.currentTimeMillis();

			try {
				v = eval.eval(inv.flaggedExpression(), state);
			} catch (MultiplicityViolationException e) {
				message = e.getMessage();
			}

			return new EvalResult(index, v, message, System.currentTimeMillis() - start);
		}
	}
}

