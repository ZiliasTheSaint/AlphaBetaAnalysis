package alphaBetaAnalysis;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Connection;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import danfulea.db.DatabaseAgent;
import danfulea.db.DatabaseAgentSupport;
//import jdf.db.AdvancedSelectPanel;
//import jdf.db.DBConnection;
//import jdf.db.DBOperation;
import danfulea.math.Convertor;
import danfulea.math.StatsUtil;
import danfulea.math.numerical.Stats;
import danfulea.phys.PhysUtilities;
import danfulea.utils.FrameUtilities;
import danfulea.utils.ListUtilities;
import danfulea.utils.TimeUtilities;


/**
 * Class designed to perform background analysis and save the quantities of interest along with their uncertainties. Also, a long time background analysis (e.g. having records of background pulses every month 
 * during a specific time-period -1, 2 years or more) can be performed here.<br>
 * At start, if there are records in database the program automatically selects the last entry, meaning the controls are populated 
 * accordingly. TO PERFORM A NEW CALCULATION, SIMPLY 
 * PRESS CLEAR BUTTON! In this "new calculation mode" ignore the record count value (near the list) since it is related to database which is updated only after pressing save.  
 * 
 * @author Dan Fulea, 06 Jul. 2011
 *
 */
@SuppressWarnings("serial")
public class BackgroundFrame extends JFrame implements ActionListener, ItemListener, ListSelectionListener {

	private AlphaBetaAnalysis mf;
	private static final String BASE_RESOURCE_CLASS = "alphaBetaAnalysis.resources.AlphaBetaAnalysisResources";
	private ResourceBundle resources;

	private final Dimension PREFERRED_SIZE = new Dimension(800, 700);
	private final Dimension sizeCb = new Dimension(60, 21);
	private static final Dimension sizeLst = new Dimension(450, 200);
	private final Dimension tableDimension = new Dimension(700, 200);

	private static final String COMPUTE_COMMAND = "COMPUTE";
	private static final String SAVE_COMMAND = "SAVE";
	private static final String TODAY_COMMAND = "TODAY";
	private static final String ADD_COMMAND = "ADD";
	private static final String REMOVE_COMMAND = "REMOVE";
	private static final String CLEAR_COMMAND = "CLEAR";
	private static final String DELETE_COMMAND = "DELETE";
	private static final String REPORT_COMMAND = "REPORT";
	private static final String LONGTIMEANALYSIS_COMMAND = "LONGTIMEANALYSIS";
	private String command = null;

	private String alphaBetaDB = "";
	private String bkgAlphaTable = "";
	private String bkgBetaTable = "";
	private String bkgTable = "";
	private String bkgAlphaDetailsTable = "";
	private String bkgBetaDetailsTable = "";
	private String bkgDetailsTable = "";

	//private AdvancedSelectPanel asp = null;
	private JPanel suportSp = new JPanel(new BorderLayout());

	private JTabbedPane tabs;
	private JTextArea simTa = new JTextArea();
	@SuppressWarnings("rawtypes")
	private JList dataList;
	private JScrollPane listSp;
	@SuppressWarnings("rawtypes")
	private DefaultListModel dlm = new DefaultListModel();

	@SuppressWarnings("rawtypes")
	private JComboBox dayCb, monthCb = null;
	private JTextField yearTf = new JTextField(5);
	private JTextField descriptionTf = new JTextField(25);
	private JTextField timeTf = new JTextField(5);
	private JTextField countsTf = new JTextField(5);

	private String measurementDate="";
	private String descriptionS="";
	private int nDataList = 0;
	private int IDBKG = 0;
	//private int NRCRTnested = 0;
	private double deadTime = 0.0;
	private Vector<Double> cpsV, countsV, timeV;
	private double CPSmean = -1.0;
	private double CPS_unc_gauss = -1.0;
	private double CPS_unc_poisson = -1.0;

	/**
	 * The connection
	 */
	private Connection alphaBetadbcon = null;
	
	/**
	 * Main table primary key column name
	 */
	private String mainTablePrimaryKey = "ID";//bkgTable
	
	/**
	 * Nested table primary key column name
	 */
	private String nestedTablePrimaryKey = "NRCRT";//bkgDetailTable
	
	/**
	 * Shared column name for main table and nested table
	 */
	private String IDlink = "ID";
	
	/**
	 * The JTable component associated to main table
	 */
	private JTable mainTable;
	
	/**
	 * The column used for sorting data in main table (ORDER BY SQL syntax)
	 */
	private String orderbyS = "ID";
	
	/**
	 * The JTable component associated to nested table
	 */
	private JTable nestedTable;
	
	/**
	 * The column used for sorting data in nested table (ORDER BY SQL syntax)
	 */
	private String nestedorderbyS = "NRCRT";
	
	/**
	 * The database agent associated to main table
	 */
	private DatabaseAgentSupport dbagent;
	
	/**
	 * The database agent associated to nested table
	 */
	private DatabaseAgentSupport nesteddbagent;
	
	private JComboBox<String> orderbyCb;
	private final Dimension sizeOrderCb = new Dimension(200, 21);
	
	/**
	 * The constructor.
	 * @param mf, mf the AlphaBetaAnalysis object
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public BackgroundFrame(AlphaBetaAnalysis mf) {
		resources = ResourceBundle.getBundle(BASE_RESOURCE_CLASS);
		//DBConnection.startDerby();

		alphaBetaDB = resources.getString("main.db");
		bkgAlphaTable = resources.getString("main.db.bkg.alphaTable");
		bkgBetaTable = resources.getString("main.db.bkg.betaTable");
		bkgAlphaDetailsTable = resources
				.getString("main.db.bkg.alphaDetailsTable");
		bkgBetaDetailsTable = resources
				.getString("main.db.bkg.betaDetailsTable");

		String titles = "";
		if (AlphaBetaAnalysis.IMODE == AlphaBetaAnalysis.IALPHA) {
			titles = resources.getString("mode.ALPHA")
					+ resources.getString("Background.NAME");
			bkgTable = bkgAlphaTable;
			bkgDetailsTable = bkgAlphaDetailsTable;
		} else if (AlphaBetaAnalysis.IMODE == AlphaBetaAnalysis.IBETA) {
			titles = resources.getString("mode.BETA")
					+ resources.getString("Background.NAME");
			bkgTable = bkgBetaTable;
			bkgDetailsTable = bkgBetaDetailsTable;
		}
		
		//==================
		mainTablePrimaryKey = "ID";
		nestedTablePrimaryKey = "NRCRT";
		IDlink = "ID";
		DatabaseAgent.ID_CONNECTION = DatabaseAgent.DERBY_CONNECTION;
		startDerbyConnection();// with stabilityDB
		dbagent.setHasValidAIColumn(false);
		nesteddbagent.setHasValidAIColumn(false);		
		//===================
		this.setTitle(titles);
		this.mf = mf;
		deadTime = mf.deadTime;
		
		// ==========================
		// createAlphaBetaDB();
		// ======================
		
		//===================
		//======initialization
		dataList = new JList(dlm);
		String[] sarray = new String[31];
		for (int i = 1; i <= 31; i++) {
			if (i < 10)
				sarray[i - 1] = "0" + i;
			else
				sarray[i - 1] = Convertor.intToString(i);
		}
		dayCb = new JComboBox(sarray);
		dayCb.setMaximumRowCount(5);
		dayCb.setPreferredSize(sizeCb);

		sarray = new String[12];
		for (int i = 1; i <= 12; i++) {
			if (i < 10)
				sarray[i - 1] = "0" + i;
			else
				sarray[i - 1] = Convertor.intToString(i);
		}
		monthCb = new JComboBox(sarray);
		monthCb.setMaximumRowCount(5);
		monthCb.setPreferredSize(sizeCb);
		// ...
		today();
		//==================================
		cpsV = new Vector<Double>();
		countsV = new Vector<Double>();
		timeV = new Vector<Double>();
		//========================================
		//============
		
		performQueryDb();
		createGUI();

		setDefaultLookAndFeelDecorated(true);
		FrameUtilities.createImageIcon(
				this.resources.getString("form.icon.url"), this);

		FrameUtilities.centerFrameOnScreen(this);

		setVisible(true);
		mf.setEnabled(false);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		// force attemptExit to be called always!
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				attemptExit();

			}
		});

	}

	/**
	 * Starts derby connection and initializes the database agents.
	 */
	private void startDerbyConnection() {

		String datas = this.resources.getString("data.load");// "Data";
		String currentDir = System.getProperty("user.dir");
		String file_sep = System.getProperty("file.separator");
		String opens = currentDir + file_sep + datas;

		opens = opens + file_sep + alphaBetaDB;
		//-------------------------------------------------------------
		alphaBetadbcon = DatabaseAgent.getConnection(opens, "", "");		
		dbagent = new DatabaseAgentSupport(alphaBetadbcon, 
				mainTablePrimaryKey, bkgTable);
		nesteddbagent = new DatabaseAgentSupport(alphaBetadbcon, nestedTablePrimaryKey,
				bkgDetailsTable);			
	}
	
	/**
	 * Exit method
	 */
	private void attemptExit() {
		
		try{
			if (alphaBetadbcon != null)
				alphaBetadbcon.close();
		}catch (Exception e){
			e.printStackTrace();
		}
		
		mf.setEnabled(true);
		dispose();
	}

	/**
	 * Setting up the frame size.
	 */
	public Dimension getPreferredSize() {
		return PREFERRED_SIZE;
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 */
	private void createGUI() {
		JPanel content = new JPanel(new BorderLayout());

		tabs = createTabs();
		content.add(tabs, BorderLayout.CENTER);

		setContentPane(new JScrollPane(content));
		content.setOpaque(true); // content panes must be opaque
		pack();
	}

	/**
	 * Create tabs
	 * @return the result
	 */
	private JTabbedPane createTabs() {

		JTabbedPane tabs = new JTabbedPane();
		JPanel inputPan = createInputPanel();
		JPanel outputPan = createOutputPanel();
		String s = resources.getString("tabs.input");
		tabs.add(s, inputPan);
		s = resources.getString("tabs.output");
		tabs.add(s, outputPan);
		return tabs;
	}

	/**
	 * Create input data panel
	 * @return the result
	 */
	private JPanel createInputPanel() {
		Character mnemonic = null;
		JButton button = null;
		JLabel label = null;
		String buttonName = "";
		String buttonToolTip = "";
		String buttonIconName = "";

		//-------------------------
		orderbyCb = dbagent.getOrderByComboBox();
		orderbyCb.setMaximumRowCount(5);
		orderbyCb.setPreferredSize(sizeOrderCb);
		orderbyCb.addItemListener(this);
		JPanel orderP = new JPanel();
		orderP.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 2));
		label = new JLabel(resources.getString("sort.by"));//"Sort by: ");
		label.setForeground(AlphaBetaAnalysis.foreColor);
		orderP.add(label);
		orderP.add(orderbyCb);
		orderP.setBackground(AlphaBetaAnalysis.bkgColor);
		label = new JLabel(resources.getString("records.count"));//"Records count: ");
		label.setForeground(AlphaBetaAnalysis.foreColor);
		orderP.add(label);
		orderP.add(dbagent.getRecordsLabel());// recordsCount);
		//-----------------------------------------		
		// ==========
		//dataList = new JList(dlm);
		listSp = new JScrollPane(dataList);
		listSp.setPreferredSize(sizeLst);// --!!!--for resising well
		// ==========
		buttonName = resources.getString("today");
		buttonToolTip = resources.getString("today.toolTip");
		buttonIconName = resources.getString("img.today");
		button = FrameUtilities.makeButton(buttonIconName, TODAY_COMMAND,
				buttonToolTip, buttonName, this, this);
		mnemonic = (Character) resources.getObject("today.mnemonic");
		button.setMnemonic(mnemonic.charValue());

		JPanel dateP = new JPanel();
		dateP.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 2));
		dateP.setBorder(FrameUtilities.getGroupBoxBorder(
				resources.getString("date.border"), AlphaBetaAnalysis.foreColor));
		label = new JLabel(resources.getString("day"));
		label.setForeground(AlphaBetaAnalysis.foreColor);
		dateP.add(label);
		dateP.add(dayCb);
		label = new JLabel(resources.getString("month"));
		label.setForeground(AlphaBetaAnalysis.foreColor);
		dateP.add(label);
		dateP.add(monthCb);
		label = new JLabel(resources.getString("year"));
		label.setForeground(AlphaBetaAnalysis.foreColor);
		dateP.add(label);
		dateP.add(yearTf);
		dateP.add(button);
		dateP.setBackground(AlphaBetaAnalysis.bkgColor);
		label.setForeground(AlphaBetaAnalysis.foreColor);
		// ======================================
		JPanel p1P = new JPanel();
		p1P.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 2));
		label = new JLabel(resources.getString("bkg.descriptionTf"));
		label.setForeground(AlphaBetaAnalysis.foreColor);
		p1P.add(label);
		p1P.add(descriptionTf);
		p1P.setBackground(AlphaBetaAnalysis.bkgColor);

		JPanel p2P = new JPanel();
		p2P.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 2));
		label = new JLabel(resources.getString("bkg.measurementTime"));
		label.setForeground(AlphaBetaAnalysis.foreColor);
		p2P.add(label);
		p2P.add(timeTf);
		label = new JLabel(resources.getString("bkg.counts"));
		label.setForeground(AlphaBetaAnalysis.foreColor);
		p2P.add(label);
		p2P.add(countsTf);countsTf.addActionListener(this);
		buttonName = resources.getString("bkg.add");
		buttonToolTip = resources.getString("bkg.add.toolTip");
		buttonIconName = resources.getString("img.insert");
		button = FrameUtilities.makeButton(buttonIconName, ADD_COMMAND,
				buttonToolTip, buttonName, this, this);
		mnemonic = (Character) resources.getObject("bkg.add.mnemonic");
		button.setMnemonic(mnemonic.charValue());
		p2P.add(button);
		p2P.setBackground(AlphaBetaAnalysis.bkgColor);
		// ==========================================
		JPanel listP = new JPanel(new BorderLayout());
		listP.add(listSp, BorderLayout.CENTER);
		listP.setBackground(AlphaBetaAnalysis.bkgColor);

		JPanel listButP = new JPanel();
		BoxLayout bllistButP = new BoxLayout(listButP, BoxLayout.Y_AXIS);
		listButP.setLayout(bllistButP);
		
		JPanel nestedLabel =  new JPanel();
		nestedLabel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 20));
		label = new JLabel(resources.getString("records.count"));//"Records count: ");
		label.setForeground(AlphaBetaAnalysis.foreColor);
		nestedLabel.add(label);
		nestedLabel.add(nesteddbagent.getRecordsLabel());
		nestedLabel.setBackground(AlphaBetaAnalysis.bkgColor);
		
		listButP.add(nestedLabel);
		
		buttonName = resources.getString("bkg.remove");
		buttonToolTip = resources.getString("bkg.remove.toolTip");
		buttonIconName = resources.getString("img.delete");
		button = FrameUtilities.makeButton(buttonIconName, REMOVE_COMMAND,
				buttonToolTip, buttonName, this, this);
		mnemonic = (Character) resources.getObject("bkg.remove.mnemonic");
		button.setMnemonic(mnemonic.charValue());
		listButP.add(button);
		listButP.add(Box.createRigidArea(new Dimension(0, 10)));// some space
		buttonName = resources.getString("bkg.clear");
		buttonToolTip = resources.getString("bkg.clear.toolTip");
		buttonIconName = resources.getString("img.delete.all");
		button = FrameUtilities.makeButton(buttonIconName, CLEAR_COMMAND,
				buttonToolTip, buttonName, this, this);
		mnemonic = (Character) resources.getObject("bkg.clear.mnemonic");
		button.setMnemonic(mnemonic.charValue());
		listButP.add(button);
		listButP.add(Box.createRigidArea(new Dimension(0, 10)));// some space
		buttonName = resources.getString("bkg.compute");
		buttonToolTip = resources.getString("bkg.compute.toolTip");
		buttonIconName = resources.getString("img.set");
		button = FrameUtilities.makeButton(buttonIconName, COMPUTE_COMMAND,
				buttonToolTip, buttonName, this, this);
		mnemonic = (Character) resources.getObject("bkg.compute.mnemonic");
		button.setMnemonic(mnemonic.charValue());
		listButP.add(button);
		listButP.setBackground(AlphaBetaAnalysis.bkgColor);

		JPanel p3P = new JPanel();
		p3P.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 2));
		p3P.add(listP);
		p3P.add(listButP);
		p3P.setBackground(AlphaBetaAnalysis.bkgColor);
		// ==============================================
		JPanel p4P = new JPanel();
		p4P.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 2));
		buttonName = resources.getString("bkg.delete");
		buttonToolTip = resources.getString("bkg.delete.toolTip");
		buttonIconName = resources.getString("img.close");
		button = FrameUtilities.makeButton(buttonIconName, DELETE_COMMAND,
				buttonToolTip, buttonName, this, this);
		mnemonic = (Character) resources.getObject("bkg.delete.mnemonic");
		button.setMnemonic(mnemonic.charValue());
		p4P.add(button);
		buttonName = resources.getString("bkg.report");
		buttonToolTip = resources.getString("bkg.report.toolTip");
		buttonIconName = resources.getString("img.report");
		button = FrameUtilities.makeButton(buttonIconName, REPORT_COMMAND,
				buttonToolTip, buttonName, this, this);
		mnemonic = (Character) resources.getObject("bkg.report.mnemonic");
		button.setMnemonic(mnemonic.charValue());
		//p4P.add(button);
		buttonName = resources.getString("bkg.longTimeAnalysis");
		buttonToolTip = resources.getString("bkg.longTimeAnalysis.toolTip");
		buttonIconName = resources.getString("img.set");
		button = FrameUtilities
				.makeButton(buttonIconName, LONGTIMEANALYSIS_COMMAND,
						buttonToolTip, buttonName, this, this);
		mnemonic = (Character) resources
				.getObject("bkg.longTimeAnalysis.mnemonic");
		button.setMnemonic(mnemonic.charValue());
		p4P.add(button);
		p4P.setBackground(AlphaBetaAnalysis.bkgColor);
		// ===========
		suportSp.setPreferredSize(tableDimension);
		JScrollPane scrollPane = new JScrollPane(mainTable);
		mainTable.setFillsViewportHeight(true);
		suportSp.add(scrollPane);
		
		JPanel p5P = new JPanel();
		BoxLayout blp5P = new BoxLayout(p5P, BoxLayout.Y_AXIS);
		p5P.setLayout(blp5P);
		p5P.setBorder(FrameUtilities.getGroupBoxBorder(
				resources.getString("records.border"),
				AlphaBetaAnalysis.foreColor));
		p5P.add(orderP);//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
		p5P.add(suportSp);
		p5P.add(p4P);
		p5P.setBackground(AlphaBetaAnalysis.bkgColor);
		// ==============
		JPanel infoBoxP = new JPanel();
		BoxLayout bl03 = new BoxLayout(infoBoxP, BoxLayout.Y_AXIS);
		infoBoxP.setLayout(bl03);
		infoBoxP.add(p1P);
		infoBoxP.add(dateP);
		infoBoxP.add(p2P);
		infoBoxP.add(p3P);
		// infoBoxP.add(suportSp);
		infoBoxP.add(p5P);
		infoBoxP.setBackground(AlphaBetaAnalysis.bkgColor);

		JPanel mainP = new JPanel(new BorderLayout());
		mainP.add(infoBoxP, BorderLayout.CENTER);// main dimension !!
		mainP.setBackground(AlphaBetaAnalysis.bkgColor);
		return mainP;
	}

	/**
	 * Create the result panel
	 * @return the result
	 */
	private JPanel createOutputPanel() {
		simTa.setCaretPosition(0);
		simTa.setEditable(false);
		simTa.setLineWrap(true);
		simTa.setWrapStyleWord(true);
		// simTa.setFocusable(false);// THE KEY TO AVOID THREAD BUG WHEN SELECT
		// TEXTAREA!!!!
		simTa.setBackground(AlphaBetaAnalysis.textAreaBkgColor);// setForeground
		simTa.setForeground(AlphaBetaAnalysis.textAreaForeColor);// setForeground

		Character mnemonic = null;
		JButton button = null;
		// JLabel label = null;
		String buttonName = "";
		String buttonToolTip = "";
		String buttonIconName = "";

		JPanel p1P = new JPanel();
		p1P.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 6));
		buttonName = resources.getString("bkg.save");
		buttonToolTip = resources.getString("bkg.save.toolTip");
		buttonIconName = resources.getString("img.save.database");
		button = FrameUtilities.makeButton(buttonIconName, SAVE_COMMAND,
				buttonToolTip, buttonName, this, this);
		mnemonic = (Character) resources.getObject("bkg.save.mnemonic");
		button.setMnemonic(mnemonic.charValue());
		p1P.add(button);
		p1P.setBackground(AlphaBetaAnalysis.bkgColor);

		JPanel resultP = new JPanel(new BorderLayout());
		resultP.add(new JScrollPane(simTa), BorderLayout.CENTER);
		// resultP.setPreferredSize(textAreaDimension);
		resultP.setBackground(AlphaBetaAnalysis.bkgColor);

		JPanel mainP = new JPanel(new BorderLayout());
		mainP.add(resultP, BorderLayout.CENTER);// main dimension !!
		mainP.add(p1P, BorderLayout.SOUTH);
		mainP.setBackground(AlphaBetaAnalysis.bkgColor);
		return mainP;
	}

	/**
	 * Most of actions are defined here.
	 */
	public void actionPerformed(ActionEvent arg0) {
		// TODO Auto-generated method stub
		command = arg0.getActionCommand();
		if (command.equals(COMPUTE_COMMAND)) {
			performBackgroundAnalysis();
		} else if (command.equals(SAVE_COMMAND)) {
			save();
		} else if (command.equals(TODAY_COMMAND)) {
			today();
		} else if (command.equals(ADD_COMMAND)) {
			add();
		} else if (command.equals(REMOVE_COMMAND)) {
			remove();
		} else if (command.equals(CLEAR_COMMAND)) {
			clear();
		} else if (command.equals(DELETE_COMMAND)) {
			delete();
		} else if (command.equals(REPORT_COMMAND)) {
			report();
		} else if (command.equals(LONGTIMEANALYSIS_COMMAND)) {
			longTimeAnalysis();
		} else if (arg0.getSource() == countsTf) {// enter!
			add();
		}
	}

	/**
	 * JCombobox actions are set here
	 */
	public void itemStateChanged(ItemEvent e) {
		// TODO Auto-generated method stub
		if (e.getSource() == orderbyCb) {
			sort();
		} //else if (e.getSource() == nestedorderbyCb) {
		//	sort2();
		//}
	}
	
	/**
	 * JTable related actions are set here
	 */
	public void valueChanged(ListSelectionEvent e) {

		if (e.getSource() == mainTable.getSelectionModel()) {
			updateDetailTable();
			/*// firts reset fields
			userNameTf.setText("");
			passwordTf.setText("");
			emailTf.setText("");
			addressTf.setText("");

			int selRow = mainTable.getSelectedRow();
			if (selRow != -1) {
				int selID = (Integer) mainTable.getValueAt(selRow, 
						dbagent.getPrimaryKeyColumnIndex());																			
																									
				userNameTf.setText((String) mainTable.getValueAt(selRow, 1));
				passwordTf.setText((String) mainTable.getValueAt(selRow, 2));

				// ===update nested===
				nesteddbagent.setLinks(IDlink, Convertor.intToString(selID));
				nesteddbagent.performSelection(nestedorderbyS);
			} else {
				return;
			}*/
		} else if (e.getSource() == nestedTable.getSelectionModel()) {
			//emailTf.setText("");
			//addressTf.setText("");
			//int selRow = nestedTable.getSelectedRow();
			//if (selRow != -1) {
			//	emailTf.setText((String) nestedTable.getValueAt(selRow, 2));
			//	addressTf.setText((String) nestedTable.getValueAt(selRow, 3));
			//} else {
			//	return;
			//}
		}
		// ---------------------------------------------------
	}
	
	/**
	 * Sorts data from main table
	 */
	private void sort() {
		orderbyS = (String) orderbyCb.getSelectedItem();
		// performSelection();
		dbagent.performSelection(orderbyS);
	}
	
	/**
	 * Set date as today.
	 */
	private void today() {

		String s = null;
		//TimeUtilities.today();
		TimeUtilities todayTu = new TimeUtilities();
		s = Convertor.intToString(todayTu.getDay());//TimeUtilities.iday);
		//if (TimeUtilities.iday < 10)
		if (todayTu.getDay() < 10)//
			s = "0" + s;
		dayCb.setSelectedItem((Object) s);
		s = Convertor.intToString(todayTu.getMonth());//TimeUtilities.imonth);
		if (todayTu.getMonth() < 10)//(TimeUtilities.imonth < 10)
			s = "0" + s;
		monthCb.setSelectedItem((Object) s);
		s = Convertor.intToString(todayTu.getYear());//TimeUtilities.iyear);
		yearTf.setText(s);
	}

	/**
	 * Remove entry from list
	 */
	private void remove() {
		if (nDataList != 0) {
			nDataList--;//NRCRTnested--;

			int index = ListUtilities.getSelectedIndex(dataList);
			// ListUtilities.remove(index,dlm);
			// ListUtilities.select(nDataList-1,dataList);

			cpsV.removeElementAt(index);
			countsV.removeElementAt(index);
			timeV.removeElementAt(index);
			// now reconstruct list:
			ListUtilities.removeAll(dlm);
			for (int i = 0; i < nDataList; i++) {

				String s = resources.getString("bkg.list.nrcrt") + (i + 1)
						+ "; " + resources.getString("bkg.list.id") + IDBKG
						+ "; " + resources.getString("bkg.list.counts")
						+ countsV.elementAt(i) + "; "
						+ resources.getString("bkg.list.time")
						+ timeV.elementAt(i) + "; "
						+ resources.getString("bkg.list.cpm")
						+ Convertor.formatNumber(60.0 * cpsV.elementAt(i), 3)
						+ "; " + resources.getString("bkg.list.cps")
						+ Convertor.formatNumber(cpsV.elementAt(i), 3);

				ListUtilities.add(s, dlm);
			}
			ListUtilities.select(nDataList - 1, dataList);

			if (nDataList == 0) {
				timeTf.setEnabled(true);
			}

		} else {
			timeTf.setEnabled(true);// redundant
		}
	}

	/**
	 * Clear all control fields and corresponding variables. The program is now ready for a new calculation.
	 */
	private void clear() {
		cpsV.removeAllElements();
		countsV.removeAllElements();
		timeV.removeAllElements();
		ListUtilities.removeAll(dlm);
		nDataList = 0;
		timeTf.setEnabled(true);
		descriptionTf.setText("");
		today();
	}

	/**
	 * Delete a record from main table.
	 */
	private void delete() {
		
		int selID = 0;
		int selRow = mainTable.getSelectedRow();
		if (selRow != -1) {
			selID = (Integer) mainTable.getValueAt(selRow, 
					dbagent.getPrimaryKeyColumnIndex());													
																	
		} else {
			return;
		}
		// deleteRecord
		dbagent.delete(Convertor.intToString(selID));//, orderbyS);
		//dbagent.performSelection(orderbyS);
		// now delete from nested
		nesteddbagent.setLinks(IDlink, Convertor.intToString(selID));
		nesteddbagent.delete(bkgDetailsTable, IDlink, Convertor.intToString(selID));
				
		//first perform some cleanings:
		cpsV.removeAllElements();
		countsV.removeAllElements();
		timeV.removeAllElements();
		ListUtilities.removeAll(dlm);
		nDataList = 0;
		timeTf.setEnabled(true);
		descriptionTf.setText("");
		today();
		
		//selection is done at the end
		dbagent.performSelection(orderbyS);
		/*try {
			// prepare db query data
			String datas = resources.getString("data.load");
			String currentDir = System.getProperty("user.dir");
			String file_sep = System.getProperty("file.separator");
			String opens = currentDir + file_sep + datas;
			String dbName = alphaBetaDB;
			opens = opens + file_sep + dbName;
			
			Connection con1 = DBConnection.getDerbyConnection(opens, "", "");

			JTable aspTable = asp.getTab();
			int rowTableCount = aspTable.getRowCount();// =MAX ID!!

			int selID = 0;// NO ZERO ID
			int selRow = aspTable.getSelectedRow();
			if (selRow != -1) {
				selID = (Integer) aspTable.getValueAt(selRow, 0);
			} else {
				if (con1 != null)
					con1.close();
				
				return;// nothing to delete
			}
			
			Statement s = con1.createStatement(ResultSet.TYPE_FORWARD_ONLY,
					ResultSet.CONCUR_UPDATABLE);
			ResultSet res = s.executeQuery("SELECT * FROM " + bkgTable);
			PreparedStatement psUpdate = null;
			while (res.next()) {
				int id = res.getInt("ID");
				if (id == selID) {
					res.deleteRow();
				} else if (id > selID) {
					// since in this table ID is UNIQUE and ASCENDING, we can
					// make
					// on-the fly update
					psUpdate = con1.prepareStatement("update " + bkgTable
							+ " set ID=? where ID=?");

					psUpdate.setInt(1, id - 1);
					psUpdate.setInt(2, id);

					psUpdate.executeUpdate();
					psUpdate.close();
				}
			}
			// now detail table
			s = con1.createStatement(ResultSet.TYPE_FORWARD_ONLY,
					ResultSet.CONCUR_UPDATABLE);
			res = s.executeQuery("SELECT * FROM " + bkgDetailsTable);
			while (res.next()) {
				int id = res.getInt("ID");
				if ((id == selID)) {
					res.deleteRow();
				}
			}
			if (selID + 1 <= rowTableCount)
				for (int i = selID + 1; i <= rowTableCount; i++) {
					psUpdate = con1.prepareStatement("update " + bkgDetailsTable
							+ " set ID=? where ID=?");

					psUpdate.setInt(1, i - 1);
					psUpdate.setInt(2, i);

					psUpdate.executeUpdate();
					psUpdate.close();
				}
			
			//first perform some cleanings:
			cpsV.removeAllElements();
			countsV.removeAllElements();
			timeV.removeAllElements();
			ListUtilities.removeAll(dlm);
			nDataList = 0;
			timeTf.setEnabled(true);
			descriptionTf.setText("");
			today();
			//then selection:..if any!!!
			performCurrentSelection();
			// do not shutdown derby..it will be closed at frame exit!

			if (res != null)
				res.close();
			if (s != null)
				s.close();
			if (psUpdate != null)
				psUpdate.close();
			if (con1 != null)
				con1.close();
		} catch (Exception e) {
			e.printStackTrace();
		}*/
	}

	/**
	 * Dummy method.
	 */
	private void report() {
		//no report here!!!
	}

	/**
	 * Perform long time analysis for background to see if significant (in statistic way) changes are occurred.
	 */
	private void longTimeAnalysis() {
		try {
			String datas = resources.getString("data.load");
			String currentDir = System.getProperty("user.dir");
			String file_sep = System.getProperty("file.separator");
			String opens = currentDir + file_sep + datas;
			String dbName = alphaBetaDB;
			opens = opens + file_sep + dbName;
			//========================
			String s = "select * from " + bkgTable;//+ " where ID = "
			//+ selID;

			//Connection con1 = DBConnection.getDerbyConnection(opens, "", "");
			//DBOperation.select(s, con1);
			DatabaseAgent.select(alphaBetadbcon, s);
			
			int J=DatabaseAgent.getRowCount();//DBOperation.getRowCount();//total bkg measurement set!
			int[] j=new int[J];
			double[] Rj=new double[J];
			for (int i=0;i<J;i++){
				j[i]=(Integer) DatabaseAgent.getValueAt(i, 0);//DBOperation.getValueAt(i, 0);//id
				Rj[i]=(Double) DatabaseAgent.getValueAt(i, 4);//DBOperation.getValueAt(i, 4);//cps
			}
			double[] fj=new double[J];//degrees of freedom
			//double[] tj=new double[J];//time
			double[] s2b=new double[J];//stdev per set!
			double km=0.0;//mean number of each measurements set!
			//===============
			double s2a=0.0;
			double t=0.0;
			double R=0.0;
			int nr=0;
			//now a new select:
			for (int i=0;i<J;i++){
				s = "select * from " + bkgDetailsTable + " where ID = "
				+ j[i] + " ORDER BY NRCRT";
				
				//con1 = DBConnection.getDerbyConnection(opens, "", "");
				//DBOperation.select(s, con1);
				DatabaseAgent.select(alphaBetadbcon, s);
				
				int k=DatabaseAgent.getRowCount();//DBOperation.getRowCount();
				km=km+k;//(Integer) DBOperation.getValueAt(i, 0);//nrcrt
				
				if (k<2){
					fj[i]=10000.0;
				} else{
					fj[i]=k-1.0;
				}
				//tj[i]=(Double)DBOperation.getValueAt(0, 3);//time always the same
				t=(Double)DatabaseAgent.getValueAt(0, 3);//DBOperation.getValueAt(0, 3);//
				//---------------
				for (int l=0;l<k;l++){
					nr++;
					double rr=(Double)DatabaseAgent.getValueAt(l, 4);//DBOperation.getValueAt(l, 4);//individual cps!!
					R=R+rr;

					s2b[i]=s2b[i]+(rr-Rj[i])*(rr-Rj[i]);					
				}
				if (k<2){
					s2b[i]=Rj[i]/t;
				} else {
					s2b[i]=s2b[i]/(k-1.0);
				}
				//------------
			}//i=0->J
			km=km/J;
			R=R/nr;
			//==========now variances;
			double s2bb=0.0;
			double fb=0.0;
			double fa=0.0;
			for (int i=0;i<J;i++){
				s2a=s2a+(Rj[i]-R)*(Rj[i]-R);
				s2bb=s2bb+s2b[i]*fj[i];
				fb=fb+fj[i];
			}
			//s2bb=s2bb/fb;//with fb degrees of freedom!~J*(km-1)!!!
			if (km>1){
				fb=J*(km-1.0);
			}
			s2bb=s2bb/fb;//with fb degrees of freedom!~J*(km-1)!!!
			
			if (nr<2){
				s2a=R/t;//km is also 1!!
				fa=10000.0;
			} else {
				if (J<2){
					s2a=km*R/t;	fa=10000.0;//formal!!!
				} else {
					s2a=km*s2a/(J-1);fa=J-1.0;
				}
			}
			//==========now:
			boolean differenceB=StatsUtil.ftest_default(s2bb, s2a, fb, fa);
			if (!StatsUtil.failB) {
				s = resources.getString("report.err.diff.longTime");
				
				if (differenceB) {
					s = s + resources.getString("diff.yes") + "\n";
					simTa.append(s);

					
				} else {
					s = s + resources.getString("diff.no") + "\n";
					simTa.append(s);
				}
			} else {
				s = resources.getString("report.err.fail1") + "\n";
				simTa.append(s);
			}
			tabs.setSelectedIndex(1);
			//======================
			//if (con1 != null)
				//con1.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Add entry in list
	 */
	private void add() {
		double time = 0.0;
		double counts = 0.0;
		double cps = 0.0;
		double cpm = 0.0;
		boolean nulneg = false;
		try {

			time = Convertor.stringToDouble(timeTf.getText());
			if (time <= 0)
				nulneg = true;
			counts = Convertor.stringToDouble(countsTf.getText());
			if (counts <= 0)
				nulneg = true;

		} catch (Exception e) {
			String title = resources.getString("number.error.title");
			String message = resources.getString("number.error");
			JOptionPane.showMessageDialog(null, message, title,
					JOptionPane.ERROR_MESSAGE);

			e.printStackTrace();
			return;
		}
		if (nulneg) {
			String title = resources.getString("number.error.title");
			String message = resources.getString("number.error");
			JOptionPane.showMessageDialog(null, message, title,
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		// ==================
		// if here we have time, counts and valid deadTime via constructor!!
		cps = PhysUtilities.tmortCorOfImp(counts, time, deadTime);
		cps = cps / time;// counts=>counts rate!
		cpm = cps * 60.0;// ...S[cps]=N/sec=60 x N/min=>solution=S[cpm]=60xN

		cpsV.addElement(cps);
		countsV.addElement(counts);
		timeV.addElement(time);// constant!!!!

		String s = resources.getString("bkg.list.nrcrt") + (nDataList + 1)//(NRCRTnested+1)//nDataList + 1)//
				+ "; " + resources.getString("bkg.list.id") + IDBKG + "; "
				+ resources.getString("bkg.list.counts") + counts + "; "
				+ resources.getString("bkg.list.time") + time + "; "
				+ resources.getString("bkg.list.cpm")
				+ Convertor.formatNumber(cpm, 3) + "; "
				+ resources.getString("bkg.list.cps")
				+ Convertor.formatNumber(cps, 3);

		ListUtilities.add(s, dlm);
		ListUtilities.select(nDataList, dataList);

		nDataList++;//NRCRTnested++;

		// now block the timeTf!!! do not change it!
		timeTf.setEnabled(false);
		countsTf.setText("");
		countsTf.requestFocusInWindow();
	}

	/**
	 * Perform background analysis.
	 */
	private void performBackgroundAnalysis() {
		boolean nulneg = false;
		int d, m, y;
		try {
			d = Convertor.stringToInt((String) dayCb.getSelectedItem());
			m = Convertor.stringToInt((String) monthCb.getSelectedItem());
			y = Convertor.stringToInt((String) yearTf.getText());
			if (d <= 0)
				nulneg = true;
			if (m <= 0)
				nulneg = true;
			if (y <= 0)
				nulneg = true;

		} catch (Exception e) {
			String title = resources.getString("number.error.title");
			String message = resources.getString("number.error");
			JOptionPane.showMessageDialog(null, message, title,
					JOptionPane.ERROR_MESSAGE);

			e.printStackTrace();
			return;
		}

		if (nulneg) {
			String title = resources.getString("number.error.title");
			String message = resources.getString("number.error");
			JOptionPane.showMessageDialog(null, message, title,
					JOptionPane.ERROR_MESSAGE);

			return;
		}

		if (cpsV.size() < 1) {
			String title = resources.getString("data.error.title");
			String message = resources.getString("data.error");
			JOptionPane.showMessageDialog(null, message, title,
					JOptionPane.ERROR_MESSAGE);

			return;
		}
		
		//TimeUtilities.setDate(d, m, y);
		//measurementDate = TimeUtilities.formatDate();
		TimeUtilities tu = new TimeUtilities(d, m, y);
		measurementDate = tu.formatDate();
		// ========================
		// =======================
		double time = timeV.elementAt(0);// constant!!
		double CPS_std_gauss = 0.0;
		double CPS_std_poisson = 0.0;
		
		double[] cps = new double[cpsV.size()];
		for (int i = 0; i < cps.length; i++) {
			cps[i] = cpsV.elementAt(i);
		}

		Stats.avevar(cps, cps.length);
		CPSmean = Stats.ave_avevar;
		CPS_unc_gauss = Stats.var_avevar;
		CPS_std_gauss = Math.sqrt(CPS_unc_gauss);
		CPS_unc_gauss = Math.sqrt(CPS_unc_gauss) / Math.sqrt(cps.length);

		CPS_unc_poisson = Math.sqrt(CPSmean) / Math.sqrt(cps.length * time);
		CPS_std_poisson = Math.sqrt(CPSmean) / Math.sqrt(time);

		if (cps.length < 2) {
			CPS_unc_gauss = CPS_unc_poisson;// 1 measurement!
		}
		
		double f_gauss = cpsV.size() - 1.0;// minim 0!!!
		if (f_gauss == 0.0) {
			//f_gauss = 10000.0;// formal, 1 measurements=>inf degrees of freedom!!
			//better estimate because we have mean here:
			f_gauss=StatsUtil.evaluateDegreesOfFreedom(CPS_unc_gauss, CPSmean);
		}
		double f_poisson = f_gauss;
		
		StatsUtil.confidenceLevel = 0.95;
		boolean differentB_ttest = StatsUtil.ttest_deviation(CPS_std_gauss,
				CPS_std_poisson, f_gauss, f_poisson);
		// -----------------------------
		simTa.selectAll();
		simTa.replaceSelection("");
		String s = resources.getString("report.cps");
		s = s + Convertor.formatNumber(CPSmean, 5) + "\n";
		simTa.append(s);
		s = resources.getString("report.err.gauss");
		s = s + Convertor.formatNumber(CPS_unc_gauss, 5) + "\n";
		simTa.append(s);
		s = resources.getString("report.err.poisson");
		s = s + Convertor.formatNumber(CPS_unc_poisson, 5) + "\n";
		simTa.append(s);

		if (!StatsUtil.failB) {
			s = resources.getString("report.err.diff1");
			if (differentB_ttest) {
				s = s + resources.getString("diff.yes") + "\n";
				simTa.append(s);

				if (CPS_std_poisson > CPS_std_gauss) {
					s = resources.getString("report.err.warning") + "\n";
					simTa.append(s);
				}
			} else {
				s = s + resources.getString("diff.no") + "\n";
				simTa.append(s);
			}
		} else {
			s = resources.getString("report.err.fail1") + "\n";
			simTa.append(s);
		}
		// ---------------------------------------
		boolean differentB_ftest = StatsUtil.ftest_deviation(CPS_unc_gauss,
				CPS_unc_poisson, f_gauss, f_poisson);

		if (!StatsUtil.failB) {
			s = resources.getString("report.err.diff2");
			if (differentB_ftest) {
				s = s + resources.getString("diff.yes") + "\n";
				simTa.append(s);

				if (CPS_unc_poisson > CPS_unc_gauss) {
					s = resources.getString("report.err.warning") + "\n";
					simTa.append(s);
				}
			} else {
				s = s + resources.getString("diff.no") + "\n";
				simTa.append(s);
			}
		} else {
			s = resources.getString("report.err.fail2") + "\n";
			simTa.append(s);
		}
		// ============
		tabs.setSelectedIndex(1);

		// the following are not required here!!!
		// double t_coverageFactor=StatsUtil.getStudentFactor(f_gauss);
		// CPS_unc_gauss=t_coverageFactor*CPS_unc_gauss;
	}

	/**
	 * Save background analysis results in database.
	 */
	private void save() {
		// always insert a new record...no UPDATE!!!!
		if (CPSmean == -1) {
			String title = resources.getString("calculation.error.title");
			String message = resources.getString("calculation.error");
			JOptionPane.showMessageDialog(null, message, title,
					JOptionPane.ERROR_MESSAGE);

			tabs.setSelectedIndex(0);
			return;
		}
		// ==================
		String[] data = new String[dbagent.getUsefullColumnCount()];
		int kCol = 0;
		data[kCol] = descriptionTf.getText();
		kCol++;
		data[kCol] = measurementDate;
		kCol++;
		data[kCol] = Convertor.doubleToString(CPSmean * 60.0);
		kCol++;
		data[kCol] = Convertor.doubleToString(CPSmean);
		kCol++;
		data[kCol] = Convertor.doubleToString(CPS_unc_gauss);
		kCol++;
		data[kCol] = Convertor.doubleToString(CPS_unc_poisson);
		kCol++;
		dbagent.insert(data);//, orderbyS);

		//now the detail table. It is nested nrcrt+id=UniqueID: No need for single column PriKey
		int id = dbagent.getAIPrimaryKeyValue();
		int n = cpsV.size();
		for (int i = 0; i < n; i++) {
			int nrcrt = i + 1;
			data = new String[nesteddbagent.getAllColumnCount()];//new String[nesteddbagent.getUsefullColumnCount()];
			kCol = 0;
			data[kCol] = Convertor.intToString(nrcrt);//Convertor.intToString(id);
			kCol++;
			data[kCol] = Convertor.intToString(id);
			kCol++;
			data[kCol] = Convertor.doubleToString(countsV.elementAt(i));
			kCol++;
			data[kCol] = Convertor.doubleToString(timeV.elementAt(i));
			kCol++;
			data[kCol] = Convertor.doubleToString(cpsV.elementAt(i));
			kCol++;
			
			nesteddbagent.insertAll(data);//insert(data);//, nestedorderbyS);
		}
		dbagent.performSelection(orderbyS);
		
		//==========================================
		/*try {
			String datas = resources.getString("data.load");
			String currentDir = System.getProperty("user.dir");
			String file_sep = System.getProperty("file.separator");
			String opens = currentDir + file_sep + datas;
			String dbName = alphaBetaDB;
			opens = opens + file_sep + dbName;

			String s = "select * from " + bkgTable;

			Connection con1 = DBConnection.getDerbyConnection(opens, "", "");
			DBOperation.select(s, con1);

			int id = DBOperation.getRowCount();

			// insert always!!

			id = id + 1;// id where we make the insertion
			PreparedStatement psInsert = null;

			psInsert = con1.prepareStatement("insert into " + bkgTable
					+ " values " + "(?, ?, ?, ?, ?, ?, ?)");
			psInsert.setString(1, Convertor.intToString(id));
			psInsert.setString(2, descriptionTf.getText());
			psInsert.setString(3, measurementDate);
			psInsert.setString(4, Convertor.doubleToString(CPSmean * 60.0));
			psInsert.setString(5, Convertor.doubleToString(CPSmean));
			psInsert.setString(6, Convertor.doubleToString(CPS_unc_gauss));
			psInsert.setString(7, Convertor.doubleToString(CPS_unc_poisson));

			psInsert.executeUpdate();

			int n = cpsV.size();
			psInsert = con1.prepareStatement("insert into " + bkgDetailsTable
					+ " values " + "(?, ?, ?, ?, ?)");
			for (int i = 0; i < n; i++) {
				int nrcrt = i + 1;
				psInsert.setString(1, Convertor.intToString(nrcrt));
				psInsert.setString(2, Convertor.intToString(id));
				psInsert.setString(3,
						Convertor.doubleToString(countsV.elementAt(i)));
				psInsert.setString(4,
						Convertor.doubleToString(timeV.elementAt(i)));
				psInsert.setString(5,
						Convertor.doubleToString(cpsV.elementAt(i)));

				psInsert.executeUpdate();
			}

			psInsert.close();

			performCurrentSelection();

			if (con1 != null)
				con1.close();

		} catch (Exception e) {
			e.printStackTrace();
		}*/
		// ============
		tabs.setSelectedIndex(0);
	}

	
	/*private void performCurrentSelection() {
		// of course, this solution is BARBARIC!!
		// we remove a panel, perform a SELECT statement
		// and rebuild the panel containing the select result.
		// An adequate solution must skip the time-consuming
		// SELECT statement (on huge database)
		// and handle INSERT/delete or update statements at
		// database level!!! Anyway, only INSERT statement
		// could be improved since a DELETE statement often
		// requires an ID update therefore a whole database
		// table scan is required so this solution is quite
		// good enough!
		// However, we don't make art here, we make science!:P
		suportSp.remove(asp);
		performQueryDb();
		validate();
	}*/

	/**
	 * Perform database initialization.
	 */
	private void performQueryDb() {
		dbagent.init();
		orderbyS = mainTablePrimaryKey;// when start-up...ID is default!!
		
		nesteddbagent.init();
		nestedorderbyS = nestedTablePrimaryKey;
		
		mainTable = dbagent.getMainTable();
		// allow single selection of rows...not multiple rows!
		ListSelectionModel rowSM = mainTable.getSelectionModel();
		rowSM.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		rowSM.addListSelectionListener(this);// listener!	
		
		nestedTable = nesteddbagent.getMainTable();
		ListSelectionModel rowSM2 = nestedTable.getSelectionModel();
		rowSM2.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);//ListSelectionModel.SINGLE_SELECTION);
		rowSM2.addListSelectionListener(this);
		
		IDBKG = dbagent.getAIPrimaryKeyValue();//mainTable.getRowCount();
		//NRCRTnested = nesteddbagent.getAIPrimaryKeyValue();//max value
		
		if (mainTable.getRowCount() > 0){
			//select last row!
			mainTable.setRowSelectionInterval(mainTable.getRowCount() - 1,
					mainTable.getRowCount() - 1); // last ID
		} else{
			IDBKG = 1;// initialization!
			nDataList = 0;
		}
		/*try {
			String datas = resources.getString("data.load");
			String currentDir = System.getProperty("user.dir");
			String file_sep = System.getProperty("file.separator");
			String opens = currentDir + file_sep + datas;
			String dbName = alphaBetaDB;
			opens = opens + file_sep + dbName;

			String s = "select * from " + bkgTable;

			Connection con1 = DBConnection.getDerbyConnection(opens, "", "");
			DBOperation.select(s, con1);

			asp = new AdvancedSelectPanel();
			suportSp.add(asp, BorderLayout.CENTER);

			JTable mainTable = asp.getTab();

			ListSelectionModel rowSM = mainTable.getSelectionModel();
			rowSM.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			rowSM.addListSelectionListener(new ListSelectionListener() {
				public void valueChanged(ListSelectionEvent e) {
					if (e.getValueIsAdjusting())
						return; // Don't want to handle intermediate selections

					updateDetailTable();
				}
			});

			IDBKG = mainTable.getRowCount();// last ID

			if (mainTable.getRowCount() > 0) {
				// always display last row!
				mainTable.setRowSelectionInterval(mainTable.getRowCount() - 1,
						mainTable.getRowCount() - 1); // last ID
				//populate some fields:
				//descriptionS=(String)mainTable.getValueAt(mainTable.getRowCount() - 1, 1);
				//descriptionTf.setText(descriptionS);
				//measurementDate=(String)mainTable.getValueAt(mainTable.getRowCount() - 1, 2);
				//TimeUtilities.unformatDate(measurementDate);
				//dayCb.setSelectedItem((Object) TimeUtilities.idayS);
				//monthCb.setSelectedItem((Object) TimeUtilities.imonthS);
				//yearTf.setText(TimeUtilities.iyearS);
			} else {
				IDBKG = 1;// initialization!
				nDataList = 0;
			}

			if (con1 != null)
				con1.close();

		} catch (Exception e) {
			e.printStackTrace();
		}*/
	}

	/**
	 * Update nested table based on selection in main table. Here, it updates controls (including the count list) based on selected background 
	 * record.
	 */
	private void updateDetailTable() {
		//JTable aspTable = asp.getTab();
		int selID = 0;// NO ZERO ID
		int selRow = mainTable.getSelectedRow();//aspTable.getSelectedRow();
		if (selRow != -1) {
			selID = (Integer) mainTable.getValueAt(selRow, 0);//aspTable.getValueAt(selRow, 0);//
		} else {
			return;
		}

		IDBKG = selID;
		
		descriptionS=(String)mainTable.getValueAt(selRow, 1);
		descriptionTf.setText(descriptionS);
		measurementDate=(String)mainTable.getValueAt(selRow, 2);
		TimeUtilities tu = new TimeUtilities(measurementDate);	
		dayCb.setSelectedItem((Object) tu.getDayS());//TimeUtilities.idayS);
		monthCb.setSelectedItem((Object) tu.getMonthS());//TimeUtilities.imonthS);
		yearTf.setText(tu.getYearS());//TimeUtilities.iyearS);
		
		// ===update nested===
		nesteddbagent.setLinks(IDlink, Convertor.intToString(selID));
		nesteddbagent.performSelection(nestedorderbyS);//DatabasaAgent is updated!
		
		cpsV.removeAllElements();
		countsV.removeAllElements();
		timeV.removeAllElements();
		ListUtilities.removeAll(dlm);
		nDataList = 0;
		timeTf.setEnabled(true);
		
		cpsV = new Vector<Double>();
		countsV = new Vector<Double>();
		timeV = new Vector<Double>();

		// populate list:
		nDataList = DatabaseAgent.getRowCount();//DBOperation.getRowCount();
		String s = "";
		for (int i = 0; i < nDataList; i++) {
			cpsV.addElement((Double) DatabaseAgent.getValueAt(i, 4));//DBOperation.getValueAt(i, 4));//
			countsV.addElement((Double) DatabaseAgent.getValueAt(i, 2));//DBOperation.getValueAt(i, 2));//
			timeV.addElement((Double) DatabaseAgent.getValueAt(i, 3));//DBOperation.getValueAt(i, 3));//

			s = resources.getString("bkg.list.nrcrt")
					+ (Integer) DatabaseAgent.getValueAt(i, 0)//DBOperation.getValueAt(i, 0)//
					+ "; "
					+ resources.getString("bkg.list.id")
					+ (Integer) DatabaseAgent.getValueAt(i, 1)//DBOperation.getValueAt(i, 1)//
					+ "; "
					+ resources.getString("bkg.list.counts")
					+ (Double) DatabaseAgent.getValueAt(i, 2)//DBOperation.getValueAt(i, 2)//
					+ "; "
					+ resources.getString("bkg.list.time")
					+ (Double) DatabaseAgent.getValueAt(i, 3)//DBOperation.getValueAt(i, 3)//
					+ "; "
					+ resources.getString("bkg.list.cpm")
					+ Convertor
							.formatNumber(60.0 * (Double) DatabaseAgent.getValueAt(i, 4), 3)//DBOperation.getValueAt(i, 4), 3)//
					+ "; "
					+ resources.getString("bkg.list.cps")
					+ Convertor.formatNumber(
							(Double) DatabaseAgent.getValueAt(i, 4), 3);//DBOperation.getValueAt(i, 4), 3);//

			ListUtilities.add(s, dlm);
		}
		//====================
		if (timeV.size()!=0){//we have data!!!
			timeTf.setText(Convertor.doubleToString(timeV.elementAt(0)));
			timeTf.setEnabled(false);
		}
		//============================
					
		ListUtilities.select(nDataList - 1, dataList);
		/*try {
			String datas = resources.getString("data.load");
			String currentDir = System.getProperty("user.dir");
			String file_sep = System.getProperty("file.separator");
			String opens = currentDir + file_sep + datas;
			String dbName = alphaBetaDB;
			opens = opens + file_sep + dbName;
			
			IDBKG = selID;
			
			//=============main table
			String s = "select * from " + bkgTable+ " where ID = "
			+ selID;

			Connection con1 = DBConnection.getDerbyConnection(opens, "", "");
			DBOperation.select(s, con1);
			
			descriptionS=(String)DBOperation.getValueAt(0, 1);
			descriptionTf.setText(descriptionS);
			measurementDate=(String)DBOperation.getValueAt(0, 2);
			TimeUtilities.unformatDate(measurementDate);
			dayCb.setSelectedItem((Object) TimeUtilities.idayS);
			monthCb.setSelectedItem((Object) TimeUtilities.imonthS);
			yearTf.setText(TimeUtilities.iyearS);

			//=====================
			// now the job:
			s = "select * from " + bkgDetailsTable + " where ID = "
					+ selID + " ORDER BY NRCRT";
			// IF press header=>selRow=-1=>ID=0=>NO ZERO ID DATA=>
			// so display an empty table!
			con1 = DBConnection.getDerbyConnection(opens, "", "");
			DBOperation.select(s, con1);

			// if (aspDate != null)
			// suportSpDate.remove(aspDate);
			cpsV.removeAllElements();
			countsV.removeAllElements();
			timeV.removeAllElements();
			ListUtilities.removeAll(dlm);
			nDataList = 0;
			timeTf.setEnabled(true);
			//=============================

			// aspDate = new AdvancedSelectPanel();
			// suportSpDate.add(aspDate, BorderLayout.CENTER);

			cpsV = new Vector<Double>();
			countsV = new Vector<Double>();
			timeV = new Vector<Double>();

			// populate list:
			
			nDataList = DBOperation.getRowCount();
			s = "";
			for (int i = 0; i < nDataList; i++) {
				cpsV.addElement((Double) DBOperation.getValueAt(i, 4));
				countsV.addElement((Double) DBOperation.getValueAt(i, 2));
				timeV.addElement((Double) DBOperation.getValueAt(i, 3));

				s = resources.getString("bkg.list.nrcrt")
						+ (Integer) DBOperation.getValueAt(i, 0)
						+ "; "
						+ resources.getString("bkg.list.id")
						+ (Integer) DBOperation.getValueAt(i, 1)
						+ "; "
						+ resources.getString("bkg.list.counts")
						+ (Double) DBOperation.getValueAt(i, 2)
						+ "; "
						+ resources.getString("bkg.list.time")
						+ (Double) DBOperation.getValueAt(i, 3)
						+ "; "
						+ resources.getString("bkg.list.cpm")
						+ Convertor
								.formatNumber(60.0 * (Double) DBOperation
										.getValueAt(i, 4), 3)
						+ "; "
						+ resources.getString("bkg.list.cps")
						+ Convertor.formatNumber(
								(Double) DBOperation.getValueAt(i, 4), 3);

				ListUtilities.add(s, dlm);
			}
			//====================
			if (timeV.size()!=0){//we have data!!!
				timeTf.setText(Convertor.doubleToString(timeV.elementAt(0)));
				timeTf.setEnabled(false);
			}
			//============================
						
			ListUtilities.select(nDataList - 1, dataList);

			if (con1 != null)
				con1.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}*/
	}

	// ===========
	//@SuppressWarnings("unused")
	/*private void createAlphaBetaDB() {
		Connection conng = null;

		String datas = resources.getString("data.load");
		String currentDir = System.getProperty("user.dir");
		String file_sep = System.getProperty("file.separator");
		String opens = currentDir + file_sep + datas;
		String dbName = resources.getString("main.db");// "AlphaBeta";
		opens = opens + file_sep + dbName;
		String protocol = "jdbc:derby:";

		Statement s = null;

		try {
			String driver = "org.apache.derby.jdbc.EmbeddedDriver";
			// disable log file!
			System.setProperty("derby.stream.error.method",
					"jdf.db.DBConnection.disableDerbyLogFile");

			Class.forName(driver).newInstance();

			conng = DriverManager.getConnection(protocol + opens
					+ ";create=true", "", "");

			// conng = DBConnection.getDerbyConnection(opens, "", "");
			String str = "";
			// ------------------
			conng.setAutoCommit(false);
			s = conng.createStatement();

			// delete the table
			// s.execute("drop table " +
			// "GammaEnergyCalibration");

			str = "create table "
					+ resources.getString("main.db.bkg.alphaTable")
					+ " ( ID integer, "
					+ "description VARCHAR(50), date VARCHAR(50), "
					+ "CPM DOUBLE PRECISION, CPS DOUBLE PRECISION, "
					+ "CPS_err_GAUSS DOUBLE PRECISION, CPS_err_POISSON DOUBLE PRECISION)";
			s.execute(str);

			str = "create table "
					+ resources.getString("main.db.bkg.betaTable")
					+ " ( ID integer, "
					+ "description VARCHAR(50), date VARCHAR(50), "
					+ "CPM DOUBLE PRECISION, CPS DOUBLE PRECISION, "
					+ "CPS_err_GAUSS DOUBLE PRECISION, CPS_err_POISSON DOUBLE PRECISION)";
			s.execute(str);

			str = "create table "
					+ resources.getString("main.db.bkg.alphaDetailsTable")
					+ " ( NRCRT integer, ID integer, "
					+ "Counts DOUBLE PRECISION, time_sec DOUBLE PRECISION, "
					+ "CPS DOUBLE PRECISION)";
			s.execute(str);

			str = "create table "
					+ resources.getString("main.db.bkg.betaDetailsTable")
					+ " ( NRCRT integer, ID integer, "
					+ "Counts DOUBLE PRECISION, time_sec DOUBLE PRECISION, "
					+ "CPS DOUBLE PRECISION)";
			s.execute(str);

			conng.commit();

			// /////
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {

			// Connection
			try {
				if (conng != null) {
					conng.close();
					conng = null;
				}
			} catch (Exception sqle) {
				sqle.printStackTrace();
			}
		}
	}*/
}
