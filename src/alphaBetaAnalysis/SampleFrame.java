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
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.pdfbox.pdmodel.PDDocument;

import danfulea.utils.PDFRenderer;
import danfulea.utils.ExampleFileFilter;
import danfulea.utils.FileOperation;
import danfulea.utils.TimeUtilities;
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


/**
 * Class designed to perform sample analysis and save the quantities of interest along with their uncertainties. <br>
 * Auxiliary data are:<br>
 * Measured sample versus actual sample conversion. For instance, we measured 40 mg coming 
 * from a total residue of 100 mg when evaporates 2 liters of sample. Set 40 and 2 as denominator and 100 as numerator. The final activity 
 * will be given in Bq/l;<br>
 * the efficiency selection;<br> 
 * and finally the background selection for appropriate correction.<br> 
 * At start, if there are records in database the program automatically selects the last entry, meaning the controls are populated accordingly. TO PERFORM A NEW CALCULATION, SIMPLY 
 * PRESS CLEAR BUTTON! In this "new calculation mode" ignore the record count value (near the list) since it is related to database which is updated only after pressing save. 
 * 
 * @author Dan Fulea, 06 Jul. 2011
 *
 */
@SuppressWarnings({ "serial", "unused" })
public class SampleFrame extends JFrame implements ActionListener, ItemListener, ListSelectionListener{
	private AlphaBetaAnalysis mf;
	private static final String BASE_RESOURCE_CLASS = "alphaBetaAnalysis.resources.AlphaBetaAnalysisResources";
	protected ResourceBundle resources;

	private final Dimension PREFERRED_SIZE = new Dimension(850, 740);
	private final Dimension sizeCb = new Dimension(60, 21);
	private final Dimension sizeCb2 = new Dimension(90, 21);
	private static final Dimension sizeLst = new Dimension(450, 200);
	private static final Dimension sizeLst2 = new Dimension(450, 100);
	private final Dimension tableDimension = new Dimension(700, 170);
	private final Dimension ta_PREFERRED_SIZE = new Dimension(800, 540);

	private static final String COMPUTE_COMMAND = "COMPUTE";
	private static final String SAVE_COMMAND = "SAVE";
	private static final String TODAY_COMMAND = "TODAY";	
	private static final String ADD_COMMAND = "ADD";
	private static final String REMOVE_COMMAND = "REMOVE";
	private static final String CLEAR_COMMAND = "CLEAR";
	private static final String DELETE_COMMAND = "DELETE";
	private static final String REPORT_COMMAND = "REPORT";
	private static final String REMOVE_AUX_COMMAND = "REMOVE_AUX";
	private static final String ADD_AUX_COMMAND = "ADD_AUX";
		
	private String command = null;

	private String icrpDB = "";
	private String icrpTable = "";
	
	private String alphaBetaDB = "";
	private String effAlphaTable = "";
	private String effBetaTable = "";
	private String effTable = "";
	
	private String bkgAlphaTable = "";
	private String bkgBetaTable = "";
	private String bkgTable = "";
	private String bkgAlphaDetailsTable = "";
	private String bkgBetaDetailsTable = "";
	private String bkgDetailsTable = "";
	
	private String sampleAlphaTable="";
	private String sampleBetaTable="";
	private String sampleTable="";
	private String sampleAlphaDetailsTable="";
	private String sampleBetaDetailsTable="";
	private String sampleDetailsTable="";

	
	//private JComboBox nucCb;
	//private Vector<String> nucV;
	
	//private AdvancedSelectPanel asp = null;
	private JPanel suportSp = new JPanel(new BorderLayout());
	
	//private AdvancedSelectPanel aspNuc = null;
	private JPanel suportSpNuc = new JPanel(new BorderLayout());
	
	//private AdvancedSelectPanel aspBkg = null;
	private JPanel suportSpBkg = new JPanel(new BorderLayout());
	
	private JTabbedPane tabs;
	//protected JTextArea simTa = new JTextArea();
	//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
	JEditorPane textArea = new JEditorPane("text/html", "");
	private JScrollPane jScrollPane1 = new JScrollPane(textArea);
	private String htmlToPrint ="";
	private File macroFile =null;//store the temporary html file
	//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
	@SuppressWarnings("rawtypes")
	private JList dataList;
	private JScrollPane listSp;
	@SuppressWarnings("rawtypes")
	private DefaultListModel dlm = new DefaultListModel();

	@SuppressWarnings("rawtypes")
	private JList auxDataList;
	private JScrollPane auxListSp;
	@SuppressWarnings("rawtypes")
	private DefaultListModel auxDlm = new DefaultListModel();
	private JTextField valueTf = new JTextField(5);
	private JTextField uncTf = new JTextField(5);
	private JTextField umTf = new JTextField(5);
	private JRadioButton numRb, denRb = null;

	@SuppressWarnings("rawtypes")
	private JComboBox dayCb, monthCb;
	private JTextField yearTf  = new JTextField(5);
	private JTextField descriptionTf = new JTextField(25);
	private JTextField timeTf = new JTextField(5);
	private JTextField countsTf = new JTextField(5);
	
	private JTextField efficiencyTf = new JTextField(5);
	private JTextField efficiencyUncTf = new JTextField(5);
	
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
	
	private String nucName="";
	private double eff;
	private double eff_extUnc_Gauss;
	private double eff_extUnc_Poisson;
	private double eff_coverageFactorGauss;
	private double eff_coverageFactorPoisson;
	
	private double[] bkgCPS;
	private double bkgCPSmean=0.0;
	private double bkgCPS_GaussUnc=0.0;
	private double bkgCPS_PoissonUnc=0.0;
	private double tbkg=0.0;	
	
	private Vector<Double> valueV, uncV, fvalueV;
	private Vector<Boolean> positionV;
	
	private double activity;
	private double activity_extUnc_Gauss;
	private double activity_extUnc_Poisson;
	private double activity_coverageFactorGauss;
	private double activity_coverageFactorPoisson;
	private String activityUnits="";
	private String equivalent="";
	private double mda;
	private double mda_unc;
	private String significance="";
	
	protected String outFilename = null;
	
	/**
	 * The connection
	 */
	private Connection alphaBetadbcon = null;
	
	/**
	 * Main table primary key column name
	 */
	private String mainTablePrimaryKey = "ID";//sampleTable
	
	/**
	 * Nested table primary key column name
	 */
	private String nestedTablePrimaryKey = "NRCRT";//sampleDetailsTable
	
	/**
	 * Efficiency table primary key column name
	 */
	private String effmainTablePrimaryKey = "ID";//effTable
	
	/**
	 * Background main table primary key column name
	 */
	private String bkgmainTablePrimaryKey = "ID";//bkgTable
	
	/**
	 * Background nested table primary key column name
	 */
	private String bkgnestedTablePrimaryKey = "NRCRT";//bkgDetailsTable
	
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
	 * The JTable component associated to background main table
	 */
	private JTable bkgmainTable;
	
	/**
	 * The column used for sorting data in background main table (ORDER BY SQL syntax)
	 */
	private String bkgorderbyS = "ID";
	
	/**
	 * The JTable component associated to background nested table
	 */
	private JTable bkgnestedTable;
	
	/**
	 * The column used for sorting data in background nested table (ORDER BY SQL syntax)
	 */
	private String bkgnestedorderbyS = "NRCRT";
	
	/**
	 * The JTable component associated to efficiency main table
	 */
	private JTable effmainTable;
	
	/**
	 * The column used for sorting data in efficiency table (ORDER BY SQL syntax)
	 */
	private String efforderbyS = "ID";
	
	/**
	 * The database agent associated to main table
	 */
	private DatabaseAgentSupport dbagent;
	
	/**
	 * The database agent associated to nested table
	 */
	private DatabaseAgentSupport nesteddbagent;
	
	/**
	 * The database agent associated to eff table
	 */
	private DatabaseAgentSupport effdbagent;
	
	/**
	 * The database agent associated to background table
	 */
	private DatabaseAgentSupport bkgdbagent;
	
	/**
	 * The database agent associated to background nested table
	 */
	private DatabaseAgentSupport bkgnesteddbagent;
			
	private JComboBox<String> orderbyCb;
	private JComboBox<String> efforderbyCb;
	private JComboBox<String> bkgorderbyCb;
	
	private final Dimension sizeOrderCb = new Dimension(200, 21);
	
	private String bkgName="";
	private String effName="";
	private String bkgDate="";
	private String effDate="";
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	/**
	 * The constructor.
	 * @param mf, mf the AlphaBetaAnalysis object
	 */
	public SampleFrame(AlphaBetaAnalysis mf){
		resources = ResourceBundle.getBundle(BASE_RESOURCE_CLASS);
		//DBConnection.startDerby();

		icrpDB = resources.getString("library.master.jaeri.db");
		icrpTable = resources.getString("library.master.jaeri.db.indexTable");
		
		alphaBetaDB = resources.getString("main.db");
		effAlphaTable = resources.getString("main.db.eff.alphaTable");
		effBetaTable = resources.getString("main.db.eff.betaTable");
		
		sampleAlphaDetailsTable = resources
				.getString("main.db.sample.alphaDetailsTable");
		sampleBetaDetailsTable = resources
				.getString("main.db.sample.betaDetailsTable");		
		sampleAlphaTable = resources.getString("main.db.sample.alphaTable");
		sampleBetaTable = resources.getString("main.db.sample.betaTable");
		
		bkgAlphaTable = resources.getString("main.db.bkg.alphaTable");
		bkgBetaTable = resources.getString("main.db.bkg.betaTable");
		bkgAlphaDetailsTable = resources.getString("main.db.bkg.alphaDetailsTable");
		bkgBetaDetailsTable = resources.getString("main.db.bkg.betaDetailsTable");

		String titles = "";
		if (AlphaBetaAnalysis.IMODE == AlphaBetaAnalysis.IALPHA) {
			titles = resources.getString("mode.ALPHA")
					+ resources.getString("Sample.NAME");
			effTable = effAlphaTable;
			sampleDetailsTable = sampleAlphaDetailsTable;
			sampleTable=sampleAlphaTable;
			bkgTable = bkgAlphaTable;
			bkgDetailsTable = bkgAlphaDetailsTable;
		} else if (AlphaBetaAnalysis.IMODE == AlphaBetaAnalysis.IBETA) {
			titles = resources.getString("mode.BETA")
					+ resources.getString("Sample.NAME");
			effTable = effBetaTable;
			sampleDetailsTable = sampleBetaDetailsTable;
			sampleTable=sampleBetaTable;
			bkgTable = bkgBetaTable;
			bkgDetailsTable = bkgBetaDetailsTable;
		}
		
		//==================
		effmainTablePrimaryKey = "ID";
		bkgmainTablePrimaryKey = "ID";
		bkgnestedTablePrimaryKey = "NRCRT";
		mainTablePrimaryKey = "ID";
		nestedTablePrimaryKey = "NRCRT";
		IDlink = "ID";
		DatabaseAgent.ID_CONNECTION = DatabaseAgent.DERBY_CONNECTION;
		startDerbyConnection();// with stabilityDB
		effdbagent.setHasValidAIColumn(false);
		bkgdbagent.setHasValidAIColumn(false);
		bkgnesteddbagent.setHasValidAIColumn(false);
		dbagent.setHasValidAIColumn(false);
		nesteddbagent.setHasValidAIColumn(false);
		//===================
				
		this.setTitle(titles);
		this.mf = mf;
		deadTime = mf.deadTime;
		
		// ==========================
		//createAlphaBetaDB();
		// ======================
		
		//===================
		//======initialization
		dataList = new JList(dlm);
		auxDataList = new JList(auxDlm);
		
		numRb = new JRadioButton(resources.getString("sample.numRb"));
		numRb.setForeground(AlphaBetaAnalysis.foreColor);
		numRb.setBackground(AlphaBetaAnalysis.bkgColor);
		denRb = new JRadioButton(resources.getString("sample.denRb"));
		denRb.setBackground(AlphaBetaAnalysis.bkgColor);
		denRb.setForeground(AlphaBetaAnalysis.foreColor);
		ButtonGroup group = new ButtonGroup();
		group.add(numRb);
		group.add(denRb);
		numRb.setSelected(true);
		
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
		//nucDayCb = new JComboBox(sarray);
		//nucDayCb.setMaximumRowCount(5);
		//nucDayCb.setPreferredSize(sizeCb);

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
		//nucMonthCb = new JComboBox(sarray);
		//nucMonthCb.setMaximumRowCount(5);
		//nucMonthCb.setPreferredSize(sizeCb);
		// ...
		today();
		
		//==================================
		cpsV = new Vector<Double>();
		countsV = new Vector<Double>();
		timeV = new Vector<Double>();

		valueV = new Vector<Double>();
		positionV = new Vector<Boolean>();
		uncV = new Vector<Double>();
		fvalueV = new Vector<Double>();
		//========================================
		initDBComponents();
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
	 * Starts derby connection and initializes the agents (if any).
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
				mainTablePrimaryKey, sampleTable);
		nesteddbagent = new DatabaseAgentSupport(alphaBetadbcon, nestedTablePrimaryKey,
				sampleDetailsTable);
		effdbagent = new DatabaseAgentSupport(alphaBetadbcon, 
				effmainTablePrimaryKey, effTable);
		bkgdbagent = new DatabaseAgentSupport(alphaBetadbcon, 
				bkgmainTablePrimaryKey, bkgTable);
		bkgnesteddbagent = new DatabaseAgentSupport(alphaBetadbcon, bkgnestedTablePrimaryKey,
				bkgDetailsTable);
	}
	
	/**
	 * Exit method
	 */
	private void attemptExit() {
		if (macroFile!=null){
		try{
			macroFile.delete();
		} catch (Exception e){
			e.printStackTrace();
		}
		}
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
		JPanel auxPan = createAuxPanel();
		JPanel inputPan = createInputPanel();
		JPanel outputPan = createOutputPanel();
		
		String s = resources.getString("tabs.aux");
		tabs.add(s, auxPan);
		s = resources.getString("tabs.input");
		tabs.add(s, inputPan);
		s = resources.getString("tabs.output");
		tabs.add(s, outputPan);
		return tabs;
	}

	/**
	 * Create auxiliary data panel
	 * @return the result
	 */
	private JPanel createAuxPanel() {
		Character mnemonic = null;
		JButton button = null;
		JLabel label = null;
		String buttonName = "";
		String buttonToolTip = "";
		String buttonIconName = "";
		
		auxListSp = new JScrollPane(auxDataList);
		auxListSp.setPreferredSize(sizeLst2);
		
		//-------------------------
		efforderbyCb = effdbagent.getOrderByComboBox();
		efforderbyCb.setMaximumRowCount(5);
		efforderbyCb.setPreferredSize(sizeOrderCb);
		efforderbyCb.addItemListener(this);
		JPanel nucorderP = new JPanel();//it is efforderP
		nucorderP.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 2));
		label = new JLabel(resources.getString("sort.by"));//"Sort by: ");
		label.setForeground(AlphaBetaAnalysis.foreColor);
		nucorderP.add(label);
		nucorderP.add(efforderbyCb);
		nucorderP.setBackground(AlphaBetaAnalysis.bkgColor);
		label = new JLabel(resources.getString("records.count"));//"Records count: ");
		label.setForeground(AlphaBetaAnalysis.foreColor);
		nucorderP.add(label);
		nucorderP.add(effdbagent.getRecordsLabel());// recordsCount);
				
		bkgorderbyCb = bkgdbagent.getOrderByComboBox();
		bkgorderbyCb.setMaximumRowCount(5);
		bkgorderbyCb.setPreferredSize(sizeOrderCb);
		bkgorderbyCb.addItemListener(this);
		JPanel bkgorderP = new JPanel();
		bkgorderP.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 2));
		label = new JLabel(resources.getString("sort.by"));//"Sort by: ");
		label.setForeground(AlphaBetaAnalysis.foreColor);
		bkgorderP.add(label);
		bkgorderP.add(bkgorderbyCb);
		bkgorderP.setBackground(AlphaBetaAnalysis.bkgColor);
		label = new JLabel(resources.getString("records.count"));//"Records count: ");
		label.setForeground(AlphaBetaAnalysis.foreColor);
		bkgorderP.add(label);
		bkgorderP.add(bkgdbagent.getRecordsLabel());
		//-----------------------------------------
		
		JPanel pl2P = new JPanel();
		pl2P.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 2));
		pl2P.add(numRb);
		pl2P.add(denRb);
		label = new JLabel(resources.getString("sample.valueTf"));
		label.setForeground(AlphaBetaAnalysis.foreColor);
		pl2P.add(label);
		pl2P.add(valueTf);
		label = new JLabel(resources.getString("sample.uncTf"));
		label.setForeground(AlphaBetaAnalysis.foreColor);
		pl2P.add(label);
		pl2P.add(uncTf);
		buttonName = resources.getString("bkg.add");
		buttonToolTip = resources.getString("bkg.add.toolTip");
		buttonIconName = resources.getString("img.insert");
		button = FrameUtilities.makeButton(buttonIconName, ADD_AUX_COMMAND,
				buttonToolTip, buttonName, this, this);
		mnemonic = (Character) resources.getObject("bkg.add.mnemonic");
		button.setMnemonic(mnemonic.charValue());
		pl2P.add(button);
		pl2P.setBackground(AlphaBetaAnalysis.bkgColor);
		
		JPanel listP = new JPanel(new BorderLayout());
		listP.add(auxListSp, BorderLayout.CENTER);
		listP.setBackground(AlphaBetaAnalysis.bkgColor);

		JPanel listButP = new JPanel();
		BoxLayout bllistButP = new BoxLayout(listButP, BoxLayout.Y_AXIS);
		listButP.setLayout(bllistButP);
		buttonName = resources.getString("bkg.remove");
		buttonToolTip = resources.getString("bkg.remove.toolTip");
		buttonIconName = resources.getString("img.delete");
		button = FrameUtilities.makeButton(buttonIconName, REMOVE_AUX_COMMAND,
				buttonToolTip, buttonName, this, this);
		mnemonic = (Character) resources.getObject("bkg.remove.mnemonic");
		button.setMnemonic(mnemonic.charValue());
		listButP.add(button);
		
		JPanel pl3P = new JPanel();
		pl3P.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 2));
		pl3P.add(listP);
		pl3P.add(listButP);
		pl3P.setBackground(AlphaBetaAnalysis.bkgColor);
		
		JPanel pl4P = new JPanel();
		pl4P.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 2));
		label = new JLabel(resources.getString("sample.umTf"));
		label.setForeground(AlphaBetaAnalysis.foreColor);
		pl4P.add(label);
		pl4P.add(umTf);
		umTf.setText(resources.getString("sample.um.default"));
		pl4P.setBackground(AlphaBetaAnalysis.bkgColor);
		
		JPanel pl5P = new JPanel();
		BoxLayout blpl5P = new BoxLayout(pl5P, BoxLayout.Y_AXIS);
		pl5P.setLayout(blpl5P);
		pl5P.setBorder(FrameUtilities.getGroupBoxBorder(
				resources.getString("sample.aux.border"),
				AlphaBetaAnalysis.foreColor));
		pl5P.add(pl2P);
		pl5P.add(pl3P);
		pl5P.add(pl4P);
		pl5P.setBackground(AlphaBetaAnalysis.bkgColor);
		//===================================================
		//================================
		JScrollPane scrollPane = new JScrollPane(effmainTable);
		effmainTable.setFillsViewportHeight(true);
		suportSpNuc.add(scrollPane);
		//====================================
				
		JPanel pNP = new JPanel();
		BoxLayout blpNP = new BoxLayout(pNP, BoxLayout.Y_AXIS);//@@@@@@@
		pNP.setLayout(blpNP);//new FlowLayout(FlowLayout.CENTER, 20, 2));
		pNP.add(suportSpNuc);
		pNP.add(nucorderP);//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
		pNP.setBackground(AlphaBetaAnalysis.bkgColor);
		pNP.setBorder(FrameUtilities.getGroupBoxBorder(
				resources.getString("sample.eff.border"),
				AlphaBetaAnalysis.foreColor));
		//==================
		JPanel p4P = new JPanel();
		BoxLayout blp4P = new BoxLayout(p4P, BoxLayout.Y_AXIS);
		p4P.setLayout(blp4P);
		p4P.add(pl5P);		
		p4P.add(pNP);
		p4P.setBackground(AlphaBetaAnalysis.bkgColor);
		//=================
		//================================
		JScrollPane bkgscrollPane = new JScrollPane(bkgmainTable);
		bkgmainTable.setFillsViewportHeight(true);
		suportSpBkg.add(bkgscrollPane);
		//====================================		
		JPanel pBP = new JPanel();
		pBP.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 2));
		pBP.add(suportSpBkg);
		pBP.setBackground(AlphaBetaAnalysis.bkgColor);
		
		JPanel p5P = new JPanel();
		BoxLayout blp5P = new BoxLayout(p5P, BoxLayout.Y_AXIS);
		p5P.setLayout(blp5P);
		p5P.setBorder(FrameUtilities.getGroupBoxBorder(
				resources.getString("eff.bkg.border"),
				AlphaBetaAnalysis.foreColor));
		p5P.add(pBP);
		p5P.add(bkgorderP);//@@@@@@@@@@@@@@@@@@@@@@@@@@
		p5P.setBackground(AlphaBetaAnalysis.bkgColor);
		
		suportSpNuc.setPreferredSize(tableDimension);
		suportSpBkg.setPreferredSize(tableDimension);
		//----------
		JPanel p6P = new JPanel();
		BoxLayout blp6P = new BoxLayout(p6P, BoxLayout.Y_AXIS);
		p6P.setLayout(blp6P);
		p6P.add(p4P);
		p6P.add(p5P);		
		p6P.setBackground(AlphaBetaAnalysis.bkgColor);
		//==========================================
		JPanel mainP = new JPanel(new BorderLayout());
		mainP.add(p6P, BorderLayout.CENTER);// main dimension !!
		mainP.setBackground(AlphaBetaAnalysis.bkgColor);
		return mainP;
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
		// ==========
				
		// ==========		
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
		mnemonic = (Character) resources.getObject("bkg.delete.mnemonic2");
		button.setMnemonic(mnemonic.charValue());
		p4P.add(button);
		p4P.add(orderP);//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
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
		jScrollPane1.setBorder(new javax.swing.border.TitledBorder(
				new javax.swing.border.LineBorder(
						new java.awt.Color(0, 51, 255), 1, true),
				this.resources.getString("results.title"),
				javax.swing.border.TitledBorder.CENTER,
				javax.swing.border.TitledBorder.TOP));
		jScrollPane1
				.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		jScrollPane1
				.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		jScrollPane1.setAutoscrolls(true);
		//textArea.setColumns(1);
		textArea.setEditable(false);
		///*****************************
		jScrollPane1.setPreferredSize(ta_PREFERRED_SIZE);

		//simTa.setCaretPosition(0);
		//simTa.setEditable(false);
		//simTa.setLineWrap(true);
		//simTa.setWrapStyleWord(true);
		// simTa.setFocusable(false);// THE KEY TO AVOID THREAD BUG WHEN SELECT
		// TEXTAREA!!!!
		//simTa.setBackground(AlphaBetaAnalysis.textAreaBkgColor);// setForeground
		//simTa.setForeground(AlphaBetaAnalysis.textAreaForeColor);// setForeground
		
		//jScrollPane1.setBackground(AlphaBetaAnalysis.textAreaBkgColor);// setForeground
		//jScrollPane1.setForeground(AlphaBetaAnalysis.textAreaForeColor);// setForeground

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
		
		buttonName = resources.getString("bkg.report");
		buttonToolTip = resources.getString("bkg.report.toolTip");
		buttonIconName = resources.getString("img.report");
		button = FrameUtilities.makeButton(buttonIconName, REPORT_COMMAND,
				buttonToolTip, buttonName, this, this);
		mnemonic = (Character) resources.getObject("bkg.report.mnemonic");
		button.setMnemonic(mnemonic.charValue());
		p1P.add(button);
		p1P.setBackground(AlphaBetaAnalysis.bkgColor);

		JPanel resultP = new JPanel(new BorderLayout());
		//jScrollPane1
		resultP.add(new JScrollPane(jScrollPane1), BorderLayout.CENTER);
		//resultP.add(new JScrollPane(simTa), BorderLayout.CENTER);
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
			performSampleAnalysis();
		} else if (command.equals(SAVE_COMMAND)) {
			save();
		} else if (command.equals(TODAY_COMMAND)) {
			today();
		}  else if (command.equals(ADD_COMMAND)) {
			add();
		} else if (command.equals(REMOVE_COMMAND)) {
			remove();
		} else if (command.equals(CLEAR_COMMAND)) {
			clear();
		} else if (command.equals(DELETE_COMMAND)) {
			delete();
		} else if (command.equals(REPORT_COMMAND)) {
			report();
		} else if (command.equals(ADD_AUX_COMMAND)) {
			add_aux();
		} else if (command.equals(REMOVE_AUX_COMMAND)) {
			remove_aux();
		} else if (arg0.getSource() == countsTf) {// enter!
			add();
		}
	}
	
	/**
	 * JTable related actions are set here
	 */
	public void valueChanged(ListSelectionEvent e) {
		if (e.getSource() == mainTable.getSelectionModel()) {
			updateDetailTable();			
		} else if (e.getSource() == nestedTable.getSelectionModel()) {

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
		else if (e.getSource() == efforderbyCb) {
			effsort();
		} if (e.getSource() == bkgorderbyCb) {
			bkgsort();
		}
	}
	
	/**
	 * Sorts data from main table
	 */
	private void sort() {
		orderbyS = (String) orderbyCb.getSelectedItem();
		dbagent.performSelection(orderbyS);
	}
	
	/**
	 * Sorts data from eff table
	 */
	private void effsort() {
		efforderbyS = (String) efforderbyCb.getSelectedItem();
		effdbagent.performSelection(efforderbyS);
	}
	
	/**
	 * Sorts data from bkg table
	 */
	private void bkgsort() {
		bkgorderbyS = (String) bkgorderbyCb.getSelectedItem();
		bkgdbagent.performSelection(bkgorderbyS);
	}
	
	/**
	 * Add an auxiliary entry. Here the conversion from measured activity to sample activity is set. For instance, we measured 40 mg coming 
	 * from a total residue of 100 mg when evaporates 2 liters of sample. Set 40 and 2 as denominator and 100 as numerator. The final activity 
	 * will be given in Bq/l.
	 */
	private void add_aux() {
		double value = 0.0;
		double unc = 0.0;
		boolean nulneg = false;
		try {

			value = Convertor.stringToDouble(valueTf.getText());
			if (value <= 0)
				nulneg = true;
			unc = Convertor.stringToDouble(uncTf.getText());
			if (unc < 0)
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
		boolean numB=numRb.isSelected();
		positionV.addElement(numB);

		valueV.addElement(value);
		uncV.addElement(unc);
		double f = 10000.0;
		if (unc!=0.0)
			f=StatsUtil.evaluateDegreesOfFreedom(unc, value);
		fvalueV.addElement(f);
		
		String s = resources.getString("bkg.list.nrcrt") + valueV.size()
				+ "; "+ resources.getString("sample.list.value") + value + "; "
				+ resources.getString("sample.list.unc") + unc + "; "
				+ resources.getString("sample.list.position")
				+ numB;

		ListUtilities.add(s, auxDlm);
		ListUtilities.select(valueV.size()-1, auxDataList);

		valueTf.setText("");
		uncTf.setText("");
		valueTf.requestFocusInWindow();
	}

	/**
	 * Remove an auxiliary entry data.
	 */
	private void remove_aux() {
		int nData=valueV.size();
		if (nData != 0) {
			nData--;

			int index = ListUtilities.getSelectedIndex(auxDataList);

			positionV.removeElementAt(index);
			valueV.removeElementAt(index);
			uncV.removeElementAt(index);
			fvalueV.removeElementAt(index);
			// now reconstruct list:
			ListUtilities.removeAll(auxDlm);
			for (int i = 0; i < nData; i++) {

				String s = resources.getString("bkg.list.nrcrt") + (i + 1)
						+ "; " + resources.getString("sample.list.value")
						+ valueV.elementAt(i) + "; "
						+ resources.getString("sample.list.unc")
						+ uncV.elementAt(i) + "; "
						+ resources.getString("sample.list.position")
						+ positionV.elementAt(i);

				ListUtilities.add(s, auxDlm);
			}
			ListUtilities.select(nData - 1, auxDataList);
			
		} else {
			
		}
	}
	
	/**
	 * Set date as today.
	 */
	private void today() {
//double t=StatsUtil.getStudentFactor(10.0);
//double f= StatsUtil.getDegreesOfFreedomFromStudentFactor(t);
//System.out.println(""+f);
		String s = null;
		//TimeUtilities.today();
		TimeUtilities todayTu = new TimeUtilities();
		s = Convertor.intToString(todayTu.getDay());//TimeUtilities.iday);
		if (todayTu.getDay() < 10)//TimeUtilities.iday < 10)
			s = "0" + s;
		dayCb.setSelectedItem((Object) s);
		s = Convertor.intToString(todayTu.getMonth());//TimeUtilities.imonth);
		if (todayTu.getMonth() < 10)//TimeUtilities.imonth < 10)
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
		nesteddbagent.delete(sampleDetailsTable, IDlink, Convertor.intToString(selID));
			
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
			ResultSet res = s.executeQuery("SELECT * FROM " + sampleTable);
			PreparedStatement psUpdate = null;
			while (res.next()) {
				int id = res.getInt("ID");
				if (id == selID) {
					res.deleteRow();
				} else if (id > selID) {
					// since in this table ID is UNIQUE and ASCENDING, we can
					// make
					// on-the fly update
					psUpdate = con1.prepareStatement("update " + sampleTable
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
			res = s.executeQuery("SELECT * FROM " + sampleDetailsTable);
			while (res.next()) {
				int id = res.getInt("ID");
				if ((id == selID)) {
					res.deleteRow();
				}
			}
			if (selID + 1 <= rowTableCount)
				for (int i = selID + 1; i <= rowTableCount; i++) {
					psUpdate = con1.prepareStatement("update " + sampleDetailsTable
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
	 * Printing report
	 */
	private void report() {
		String FILESEPARATOR = System.getProperty("file.separator");
		String currentDir = System.getProperty("user.dir");
		File infile = null;

		String ext = resources.getString("file.extension.html");
		String pct = ".";
		String description = resources.getString("file.description.html");
		ExampleFileFilter eff = new ExampleFileFilter(ext, description);

		String myDir = currentDir + FILESEPARATOR;//
		// File select
		JFileChooser chooser = new JFileChooser(myDir);
		chooser.addChoosableFileFilter(eff);
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

		int returnVal = chooser.showSaveDialog(this);// parent=this frame
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			infile = chooser.getSelectedFile();
			outFilename = chooser.getSelectedFile().toString();

			int fl = outFilename.length();
			String test = outFilename.substring(fl - 5);// exstension lookup!!
			String ctest = pct + ext;
			if (test.compareTo(ctest) != 0)
				outFilename = chooser.getSelectedFile().toString() + pct + ext;

			if (infile.exists()) {
				String title = resources.getString("dialog.overwrite.title");
				String message = resources
						.getString("dialog.overwrite.message");

				Object[] options = (Object[]) resources
						.getObject("dialog.overwrite.buttons");
				int result = JOptionPane
						.showOptionDialog(this, message, title,
								JOptionPane.YES_NO_OPTION,
								JOptionPane.QUESTION_MESSAGE, null, options,
								options[0]);
				if (result != JOptionPane.YES_OPTION) {
					return;
				}

			}

			//new SampleReport(this);
			performPrintReport();
			//statusL.setText(resources.getString("status.save") + outFilename);
		} else {
			return;
		}
	}	

	/**
	 * Actual pdf renderer is called here. Called by printReport.
	 */
	public void performPrintReport(){
		/*PDDocument doc = new PDDocument();
		PDFRenderer renderer = new PDFRenderer(doc);
		try{
			renderer.setTitle(resources.getString("pdf.content.title"));
			renderer.setSubTitle(
					resources.getString("pdf.content.subtitle")+
					resources.getString("pdf.metadata.author")+ ", "+
							new Date());
						
			String str = " \n" + simTa.getText();
		
			//renderer.renderTextHavingNewLine(str);//works!!!!!!!!!!!!!!!!
			renderer.renderTextEnhanced(str);
			
			renderer.addPageNumber();
			renderer.close();		
			doc.save(new File(outFilename));
			doc.close();
		}catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		String htmlTemp = "temp.html";
		File temFile = new File(htmlTemp);
		if (!temFile.exists()){
			return;
		}
		
		String currentDir = System.getProperty("user.dir");
		String file_sep = System.getProperty("file.separator");
		String filename = currentDir + file_sep + htmlTemp; 
		
		//we can also do copy, this way...more advanced:
		try {
			FileOperation.copyFile(filename, outFilename);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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

		String s = resources.getString("bkg.list.nrcrt") + (nDataList + 1)//(NRCRTnested+1)//(nDataList + 1)//
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
	 * Retrieve auxiliary data and return true on success.
	 * @return the result
	 */
	private boolean retrieveAuxData(){
		JTable aspTable = effmainTable;//aspNuc.getTab();
		int selID = 0;// NO ZERO ID
		int selRow = aspTable.getSelectedRow();
		if (selRow != -1) {
			selID = (Integer) aspTable.getValueAt(selRow, 0);
		} else {
			return false;
		}
		
		JTable aspBkgTable = bkgmainTable;//aspBkg.getTab();
		int selBkgID = 0;// NO ZERO ID
		int selBkgRow = aspBkgTable.getSelectedRow();
		if (selBkgRow != -1) {
			selBkgID = (Integer) aspBkgTable.getValueAt(selBkgRow, 0);
		} else {
			return false;
		}		
		
		try {
			//String datas = resources.getString("data.load");
			//String currentDir = System.getProperty("user.dir");
			//String file_sep = System.getProperty("file.separator");
			//String opens = currentDir + file_sep + datas;
			//String dbName = alphaBetaDB;
			//opens = opens + file_sep + dbName;
			
			//=============main table
			//String s = "select * from " + effTable+ " where ID = "
			//+ selID;

			//Connection con1 = DBConnection.getDerbyConnection(opens, "", "");
			//DBOperation.select(s, con1);
			effName=(String)effmainTable.getValueAt(selRow, 1);
			effDate=(String)effmainTable.getValueAt(selRow, 2);
			nucName=(String)effmainTable.getValueAt(selRow, 8);//DBOperation.getValueAt(0, 8);//r 0; c 8!			
			eff = (Double)effmainTable.getValueAt(selRow, 3);//DBOperation.getValueAt(0, 3);
			eff_extUnc_Gauss = (Double)effmainTable.getValueAt(selRow, 4);//DBOperation.getValueAt(0, 4);
			eff_extUnc_Poisson = (Double)effmainTable.getValueAt(selRow, 5);//DBOperation.getValueAt(0, 5);
			eff_coverageFactorGauss = (Double)effmainTable.getValueAt(selRow, 6);//DBOperation.getValueAt(0, 6);
			eff_coverageFactorPoisson = (Double)effmainTable.getValueAt(selRow, 7);//DBOperation.getValueAt(0, 7);
			//=====================

			//s = "select * from " + bkgTable+ " where ID = "
			//+ selBkgID;
			//con1 = DBConnection.getDerbyConnection(opens, "", "");
			//DBOperation.select(s, con1);
			bkgName = (String)bkgmainTable.getValueAt(selBkgRow, 1);
			bkgDate = (String)bkgmainTable.getValueAt(selBkgRow, 2);
			bkgCPSmean=(Double)bkgmainTable.getValueAt(selBkgRow, 4);//DBOperation.getValueAt(0, 4);
			bkgCPS_GaussUnc=(Double)bkgmainTable.getValueAt(selBkgRow, 5);//DBOperation.getValueAt(0, 5);
			bkgCPS_PoissonUnc=(Double)bkgmainTable.getValueAt(selBkgRow, 6);//DBOperation.getValueAt(0, 6);

			// now the job:
			bkgnesteddbagent.setLinks(IDlink, Convertor.intToString(selBkgID));
			bkgnesteddbagent.performSelection(bkgnestedorderbyS);//DatabasaAgent is updated!
			//s = "select * from " + bkgDetailsTable + " where ID = "
			//		+ selBkgID + " ORDER BY NRCRT";
			// IF press header=>selRow=-1=>ID=0=>NO ZERO ID DATA=>
			// so display an empty table!
			//con1 = DBConnection.getDerbyConnection(opens, "", "");
			//DBOperation.select(s, con1);

			tbkg=(Double)DatabaseAgent.getValueAt(0, 3);//DBOperation.getValueAt(0, 3);
			int nbkg = DatabaseAgent.getRowCount();//DBOperation.getRowCount();
			bkgCPS=new double[nbkg];
			for (int i = 0; i < nbkg; i++) {
				bkgCPS[i]=(Double)DatabaseAgent.getValueAt(i, 4);//DBOperation.getValueAt(i, 4);
			}
			
			//if (con1 != null)
			//	con1.close();
			
			return true;

		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
		
		//return true;
	}

	/**
	 * Perform sample analysis.
	 */
	private void performSampleAnalysis() {
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
		//now check valid source nuclide info and BKG info!
		boolean validAuxB=retrieveAuxData();
		if (!validAuxB){
			String title = resources.getString("aux.error.title");
			String message = resources.getString("aux.error");
			JOptionPane.showMessageDialog(null, message, title,
					JOptionPane.ERROR_MESSAGE);

			return;
		}
		activityUnits=umTf.getText();
		// =======================
		double time = timeV.elementAt(0);// constant!!
		double CPS_std_gauss = 0.0;
		double CPS_std_poisson = 0.0;		
		
		double[] cps = new double[cpsV.size()];
		for (int i = 0; i < cps.length; i++) {
			cps[i] = cpsV.elementAt(i);
		}

		Stats.avevar(cps, cps.length);
		CPSmean = Stats.ave_avevar;//>0
		CPS_unc_gauss = Stats.var_avevar;
		CPS_std_gauss = Math.sqrt(CPS_unc_gauss);
		CPS_unc_gauss = Math.sqrt(CPS_unc_gauss) / Math.sqrt(cps.length);

		CPS_unc_poisson = Math.sqrt(CPSmean) / Math.sqrt(cps.length * time);
		CPS_std_poisson = Math.sqrt(CPSmean) / Math.sqrt(time);

		if (cps.length < 2) {
			CPS_unc_gauss = CPS_unc_poisson;// 1 measurement!//>0
		}
		
		double f_gauss = cpsV.size() - 1.0;// minim 0!!!
		if (f_gauss == 0.0) {
			//f_gauss = 10000.0;// formal, 1 measurements=>inf degrees of freedom!!
			//better estimate because we have mean here:
			f_gauss=StatsUtil.evaluateDegreesOfFreedom(CPS_unc_gauss, CPSmean);
		}
		double f_poisson = f_gauss;
		//================		
		double cor=0.0;
		int nbkg=bkgCPS.length;
		int ncps=cps.length;
		int n0=Math.min(nbkg, ncps);
		if (n0>1){
			//compute cor; both bkg and source counts are >1!
			for (int i=0;i<n0;i++){
				cor=cor+(cps[i]-CPSmean)*(bkgCPS[i]-bkgCPSmean);
			}
			cor=cor/(CPS_unc_gauss*bkgCPS_GaussUnc);
			cor=cor/Math.sqrt(nbkg*(nbkg-1.0)*ncps*(ncps-1.0));
			//above are n(n-1) but geometric mean is the best estimate!!
		}
		//----------
		double f_bkg_gauss = nbkg - 1.0;// minim 0!!!
		if (f_bkg_gauss == 0.0) {
			//f_bkg_gauss = 10000.0;// formal, 1 measurements=>inf degrees of freedom!!
			f_bkg_gauss=StatsUtil.evaluateDegreesOfFreedom(bkgCPS_GaussUnc, bkgCPSmean);
		}
		double f_bkg_poisson = f_bkg_gauss;
		//-----------
		double netCPS=CPSmean-bkgCPSmean;
		double netCPS_GaussUnc=Math.sqrt(Math.abs(CPS_unc_gauss*CPS_unc_gauss+
				bkgCPS_GaussUnc*bkgCPS_GaussUnc-2.0*CPS_unc_gauss*bkgCPS_GaussUnc*cor));
		double netCPS_PoissonUnc=Math.sqrt(Math.abs(CPS_unc_poisson*CPS_unc_poisson+
				bkgCPS_PoissonUnc*bkgCPS_PoissonUnc-2.0*CPS_unc_poisson*bkgCPS_PoissonUnc*cor));
//System.out.println("fgauss= "+f_gauss+" fbkggauss= "+f_bkg_gauss);		
		double abcomp=netCPS_GaussUnc;
		double[] ab = new double[2];
		double[] fab = new double[2];
		ab[0]=CPS_unc_gauss;fab[0]=f_gauss;
		ab[1]=bkgCPS_GaussUnc;fab[1]=f_bkg_gauss;
//System.out.println("abcomp "+abcomp+" cpsUnc "+CPS_unc_gauss+ " cpsBkgUnc "+bkgCPS_GaussUnc);		
		double f_net_gauss=StatsUtil
		.getEffectiveDegreesOfFreedom(abcomp, ab, fab);
//System.out.println("fa= "+f_nucActivity+" fnet= "+f_net_gauss);			
		abcomp=netCPS_PoissonUnc;
		ab[0]=CPS_unc_poisson;fab[0]=f_poisson;
		ab[1]=bkgCPS_PoissonUnc;fab[1]=f_bkg_poisson;
		double f_net_poisson=StatsUtil
		.getEffectiveDegreesOfFreedom(abcomp, ab, fab);
		//=====================
		double tsample=timeV.elementAt(0);//constant!
		//here detection limit is evaluate from single measurements=>no stdev of mean!
		double detectionLimit=(2.706025/tsample)+
			3.29*Math.sqrt(bkgCPSmean/tsample);//>0 always!
		double detectionLimitUnc=1.645/(Math.sqrt(tsample*tbkg));
		//to be consistent with Poisson Unc of mean:
		detectionLimitUnc=detectionLimitUnc/Math.sqrt(f_bkg_gauss+1.0);
		//fgauss==fpoisson; f+1=>n!!!		
		//==================extended Unc-->simple (standard of mean) unc:
		eff_extUnc_Gauss=eff_extUnc_Gauss/eff_coverageFactorGauss;
		eff_extUnc_Poisson=eff_extUnc_Poisson/eff_coverageFactorPoisson;
		//------------------------------------------------------------
		activity=0.0;activity_extUnc_Gauss=0.0;activity_extUnc_Poisson=0.0;
		mda=0.0;mda_unc=0.0;
		//measured activity and mda without multiplying with preset fraction!!
		double a0=0.0;double a0_uncGauss=0.0;
		double a0_uncPoisson=0.0;double mda0=0.0;double mda0_unc=0.0;
		if (netCPS<0.0)
			netCPS=0.0;
		if (eff>0.0){//always!
			activity=netCPS/eff;	
			mda=detectionLimit/eff;
			a0=activity;
			mda0=mda;
			for (int i=0;i<valueV.size();i++){
				boolean bb=positionV.elementAt(i);
				if (bb){//num
					activity=activity*valueV.elementAt(i);
					mda=mda*valueV.elementAt(i);
				} else {
					activity=activity/valueV.elementAt(i);
					mda=mda/valueV.elementAt(i);
				}
			}
			//-------------------------------
			
			if (netCPS>0.0){
				double dbl_Gauss=Math.pow(netCPS_GaussUnc/netCPS,2.0)+
					Math.pow(eff_extUnc_Gauss/eff,2.0);
				double dbl_Poisson=Math.pow(netCPS_PoissonUnc/netCPS,2.0)+
					Math.pow(eff_extUnc_Poisson/eff,2.0);
				
				a0_uncGauss=a0*Math.sqrt(dbl_Gauss);
				a0_uncPoisson=a0*Math.sqrt(dbl_Poisson);
				
				for (int i=0;i<valueV.size();i++){
					dbl_Gauss=dbl_Gauss+Math.pow(uncV.elementAt(i)/valueV.elementAt(i),2.0);
					dbl_Poisson=dbl_Poisson+Math.pow(uncV.elementAt(i)/valueV.elementAt(i),2.0);
				}
				activity_extUnc_Gauss=activity*Math.sqrt(dbl_Gauss);
				activity_extUnc_Poisson=activity*Math.sqrt(dbl_Poisson);						
			}
			
			double dbl=Math.pow(detectionLimitUnc/detectionLimit,2.0)+
				Math.pow(eff_extUnc_Gauss/eff,2.0);
			mda0_unc=mda0*Math.sqrt(dbl);	
			for (int i=0;i<valueV.size();i++){				
				dbl=dbl+Math.pow(uncV.elementAt(i)/valueV.elementAt(i),2.0);
			}
			mda_unc=mda*Math.sqrt(dbl);		
		}
		double f_eff_Gauss=0.0;
		f_eff_Gauss=StatsUtil.getDegreesOfFreedomFromStudentFactor(eff_coverageFactorGauss);

		double f_eff_Poisson=0.0;
		f_eff_Poisson=StatsUtil.getDegreesOfFreedomFromStudentFactor(eff_coverageFactorPoisson);
		
		//System.out.println("fa= "+f_nucActivity+" fnet= "+f_net_gauss);		
		double abcomp0=0.0;
		abcomp=0.0;
		if (activity!= 0.0)
			abcomp = activity_extUnc_Gauss / activity;
		if (a0!=0.0)
			abcomp0=a0_uncGauss/a0;
		double[] ffab=new double[2+valueV.size()];
		double[] aab=new double[2+valueV.size()];
		ffab[0]= f_net_gauss;
		ffab[1]= f_eff_Gauss;
		fab[0]=ffab[0];
		fab[1]=ffab[1];
		if (netCPS!= 0.0){
			aab[0]= netCPS_GaussUnc/netCPS;			
		}else{
			aab[0]=0.0;
		}
		ab[0]=aab[0];
		if (eff!=0.0){
			aab[1]=eff_extUnc_Gauss/eff;
		} else {
			aab[1]=0.0;
		}
		ab[1]=aab[1];
		for (int i=0;i<valueV.size();i++){
			ffab[2+i]=fvalueV.elementAt(i);
			aab[2+i]=uncV.elementAt(i)/valueV.elementAt(i);
		}
		double f_a0_gauss=StatsUtil
		.getEffectiveDegreesOfFreedom(abcomp0, ab, fab);
		if (StatsUtil.failB)
			f_a0_gauss=10000.0;//virtual!
		double a0_coverageFactorGauss= StatsUtil
		.getStudentFactor(f_a0_gauss);

		double f_activity_gauss=StatsUtil
		.getEffectiveDegreesOfFreedom(abcomp, aab, ffab);
		if (StatsUtil.failB)
			f_activity_gauss=10000.0;//virtual!
		activity_coverageFactorGauss = StatsUtil
		.getStudentFactor(f_activity_gauss);
		//--------------
		abcomp=0.0;abcomp0=0.0;
		if (mda!= 0.0)
			abcomp = mda_unc / mda;
		if (mda0!=0.0)
			abcomp0=mda0_unc/mda0;
		ffab=new double[2+valueV.size()];
		aab=new double[2+valueV.size()];
		ffab[0]= f_bkg_gauss;//detection limit has bkg related df!!!
		ffab[1]= f_eff_Gauss;
		fab[0]=ffab[0];
		fab[1]=ffab[1];
		if (detectionLimit!= 0.0)
			aab[0]= detectionLimitUnc/detectionLimit;
		else
			aab[0]=0.0;
		ab[0]=aab[0];
		if (eff!=0.0){
			aab[1]=eff_extUnc_Gauss/eff;
		} else {
			aab[1]=0.0;
		}
		ab[1]=aab[1];
		for (int i=0;i<valueV.size();i++){
			ffab[2+i]=fvalueV.elementAt(i);
			aab[2+i]=uncV.elementAt(i)/valueV.elementAt(i);
		}
		double f_mda0_gauss=StatsUtil
		.getEffectiveDegreesOfFreedom(abcomp0, ab, fab);
		if (StatsUtil.failB)
			f_mda0_gauss=10000.0;//virtual!
		double mda0_coverageFactorGauss= StatsUtil
		.getStudentFactor(f_mda0_gauss);
		
		double f_mda_gauss=StatsUtil
		.getEffectiveDegreesOfFreedom(abcomp, aab, ffab);
		if (StatsUtil.failB)
			f_mda_gauss=10000.0;//virtual!
		double mda_coverageFactorGauss = StatsUtil
		.getStudentFactor(f_mda_gauss);
		//=================================
				
		abcomp=0.0;abcomp0=0.0;
		if (activity!= 0.0)
			abcomp = activity_extUnc_Poisson / activity;
		if (a0!=0.0)
			abcomp0=a0_uncPoisson/a0;
		ffab[0]= f_net_poisson;
		ffab[1]= f_eff_Poisson;
		fab[0]=ffab[0];
		fab[1]=ffab[1];
		if (netCPS!= 0.0)
			aab[0]= netCPS_PoissonUnc/netCPS;
		else
			aab[0]=0.0;
		ab[0]=aab[0];
		if (eff!=0.0){
			aab[1]=eff_extUnc_Poisson/eff;
		} else {
			aab[1]=0.0;
		}
		ab[1]=aab[1];
		for (int i=0;i<valueV.size();i++){
			ffab[2+i]=fvalueV.elementAt(i);
			aab[2+i]=uncV.elementAt(i)/valueV.elementAt(i);
		}
		double f_a0_poisson=StatsUtil
		.getEffectiveDegreesOfFreedom(abcomp0, ab, fab);
		if (StatsUtil.failB)
			f_a0_poisson=10000.0;//virtual!
		double a0_coverageFactorPoisson= StatsUtil
		.getStudentFactor(f_a0_poisson);
		
		double f_activity_poisson=StatsUtil
		.getEffectiveDegreesOfFreedom(abcomp, aab, ffab);
		if (StatsUtil.failB)
			f_activity_poisson=10000.0;//virtual!
		activity_coverageFactorPoisson = StatsUtil
		.getStudentFactor(f_activity_poisson);
		
		//------------------ONLY for consistency..not USED:
		abcomp=0.0;
		if (mda!= 0.0)
			abcomp = mda_unc / mda;
		if (mda0!=0.0)
			abcomp0=mda0_unc/mda0;
		ffab=new double[2+valueV.size()];
		aab=new double[2+valueV.size()];
		ffab[0]= f_bkg_poisson;//detection limit has bkg related df!!!
		ffab[1]= f_eff_Poisson;
		if (detectionLimit!= 0.0)
			aab[0]= detectionLimitUnc/detectionLimit;
		else
			aab[0]=0.0;
		if (eff!=0.0){
			aab[1]=eff_extUnc_Poisson/eff;
		} else {
			aab[1]=0.0;
		}
		for (int i=0;i<valueV.size();i++){
			ffab[2+i]=fvalueV.elementAt(i);
			aab[2+i]=uncV.elementAt(i)/valueV.elementAt(i);
		}
		double f_mda_poisson=StatsUtil
		.getEffectiveDegreesOfFreedom(abcomp, aab, ffab);
		if (StatsUtil.failB)
			f_mda_poisson=10000.0;//virtual!
		double mda_coverageFactorPoisson = StatsUtil
		.getStudentFactor(f_mda_poisson);
		//======================================================
		//Note: The above=redundant since only GAUSS mda_unc is considered!!!
		//---------------------------------				
		//now compare netrate with detectionLimit
		boolean diffB = StatsUtil.ttest_default_unc(
				netCPS, detectionLimit, netCPS_GaussUnc,
				detectionLimitUnc, f_net_gauss,	f_bkg_gauss);
		if (netCPS<=detectionLimit){
			diffB=false;
		}
		significance=""+diffB;
		//======================	
//System.out.println(eff_extUnc_Gauss+" cover: "+eff_coverageFactorGauss+" f "+f_eff_gauss);		
		activity_extUnc_Gauss=activity_extUnc_Gauss*activity_coverageFactorGauss;
		activity_extUnc_Poisson=activity_extUnc_Poisson*activity_coverageFactorPoisson;
		mda_unc=mda_unc*mda_coverageFactorGauss;
		
		a0_uncGauss=a0_uncGauss*a0_coverageFactorGauss;
		a0_uncPoisson=a0_uncPoisson*a0_coverageFactorPoisson;
		mda0_unc=mda0_unc*mda0_coverageFactorGauss;
		//detectionLimitUnc=detectionLimitUnc*ld_coverageFactor;
		
		//Note: we can test for difference after applying coverage factor but it is
		//rigurosly to take into account only stdevofmean as old statistics said!
		//besides, we must evaluate extUnc for netCPS_GaussUnc also!
		//So: Both approach are good for any purpose!
		//MDA====================
		//mda=detectionLimit/eff;
		//==================================================================
		StatsUtil.confidenceLevel = 0.95;//redundant!!
		boolean differentB_ttest = StatsUtil.ttest_deviation(CPS_std_gauss,
				CPS_std_poisson, f_gauss, f_poisson);
		// -----------------------------		
		//simTa.selectAll();
		//simTa.replaceSelection("");
		String imageRelFilePath = this.resources.getString("icon.package.url");
		String currentDir = System.getProperty("user.dir");
		String file_sep = System.getProperty("file.separator");
		String filename1 = currentDir + file_sep + imageRelFilePath; 
		java.net.URL filenamURL1 = null;
		try {
			filenamURL1 = new File(filename1).toURI().toURL();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//===================header=================
		String sampleName = descriptionTf.getText();
		//activityUnits = Bq/l
		//=========================================
		String imgS="";
		imgS = "<img src="+
				filenamURL1//ClassLoader.getSystemResource(resources.getString("icon.package.url")).toString()//"personal.png"//resources.getString("icon.url")
							+" width='64' height='64' > <br>";
		
		String resultS = imgS + this.resources.getString("results.sample.name")
				+ sampleName + "; "
				+ this.resources.getString("results.sample.date")
				+ measurementDate+ "; "
				+ this.resources.getString("results.sample.time")
				+ time
				+"<br>"				
				+ this.resources.getString("results.bkg.name")
				+ bkgName+ "; "
				+ this.resources.getString("results.bkg.date")
				+ bkgDate
				+"<br>"
				+this.resources.getString("results.eff.name")
				+ effName+"; "
				+ this.resources.getString("results.eff.date")
				+ effDate
				+"<br><br>"
				;
		
		String s = resources.getString("report.cps");
		s = s + Convertor.formatNumber(CPSmean, 5) 
		+ "<br>";//"\n";
		resultS=resultS+s;//simTa.append(s);
		s = resources.getString("report.err.gauss");
		s = s + Convertor.formatNumber(CPS_unc_gauss, 5)
		+ "<br>";//"\n";
		resultS=resultS+s;//simTa.append(s);
		s = resources.getString("report.err.poisson");
		s = s + Convertor.formatNumber(CPS_unc_poisson, 5)
		+ "<br>";//"\n";
		resultS=resultS+s;//simTa.append(s);

		if (!StatsUtil.failB) {
			s = resources.getString("report.err.diff1");
			if (differentB_ttest) {
				s = s + resources.getString("diff.yes")
				+"<br>";// "\n";
				resultS=resultS+s;//simTa.append(s);

				if (CPS_std_poisson > CPS_std_gauss) {
					s = resources.getString("report.err.warning")
							+"<br>";// "\n";
					resultS=resultS+s;//simTa.append(s);
				}
			} else {
				s = s + resources.getString("diff.no") 
				+ "<br>";//"\n";
				resultS=resultS+s;//simTa.append(s);
			}
		} else {
			s = resources.getString("report.err.fail1") 
					+ "<br>";//"\n";
			resultS=resultS+s;//simTa.append(s);
		}
		// ---------------------------------------
		boolean differentB_ftest = StatsUtil.ftest_deviation(CPS_unc_gauss,
				CPS_unc_poisson, f_gauss, f_poisson);

		if (!StatsUtil.failB) {
			s = resources.getString("report.err.diff2");
			if (differentB_ftest) {
				s = s + resources.getString("diff.yes")
				+"<br>";// "\n";
				resultS=resultS+s;//simTa.append(s);

				if (CPS_unc_poisson > CPS_unc_gauss) {
					s = resources.getString("report.err.warning") 
							+"<br>";// "\n";
					resultS=resultS+s;//simTa.append(s);
				}
			} else {
				s = s + resources.getString("diff.no")
				+"<br>";// "\n";
				resultS=resultS+s;//simTa.append(s);
			}
		} else {
			s = resources.getString("report.err.fail2")
					+"<br>";// "\n";
			resultS=resultS+s;//simTa.append(s);
		}
		// ============
		s=" "
		+"<br>";//"\n";
		resultS=resultS+s;//simTa.append(s);
		
		s = resources.getString("report.cps.net");
		s = s + Convertor.formatNumber(netCPS, 5) 
		+ resources.getString("report.plusminus")
		+Convertor.formatNumber(netCPS_GaussUnc, 5)
		+"<br>";//"\n";
		resultS=resultS+s;//simTa.append(s);
		
		s = resources.getString("report.ld");
		s = s + Convertor.formatNumber(detectionLimit, 5) 
		+ resources.getString("report.plusminus")
		+Convertor.formatNumber(detectionLimitUnc, 5)
		+"<br>";//"\n";
		resultS=resultS+s;//simTa.append(s);
		
		s = resources.getString("report.cps.ld");
		s = s + diffB
				+"<br>";//"\n";
		resultS=resultS +" <b><font color='blue'>";
		resultS=resultS+s;//simTa.append(s);
		resultS=resultS + "</font></b> ";
		
		s=" "
		+"<br>";//"\n";
		resultS=resultS+s;//simTa.append(s);
		
		s = resources.getString("report.nuclide");
		s = s + nucName
				+"<br>";//"\n";
		resultS=resultS+s;//simTa.append(s);
				
		s = resources.getString("report.eff");
		s = s + Convertor.formatNumber(eff, 5)
		+"<br>";//"\n";
		resultS=resultS+s;//simTa.append(s);
		s = resources.getString("report.eff.extUnc.Gauss1");
		s = s + Convertor.formatNumber(eff_extUnc_Gauss, 5)
		+"<br>";//"\n";
		resultS=resultS+s;//simTa.append(s);
		s = resources.getString("report.eff.extUnc.Poisson1");
		s = s + Convertor.formatNumber(eff_extUnc_Poisson, 5)
		+"<br>";//"\n";
		resultS=resultS+s;//simTa.append(s);

		s=" "
		+"<br>";//"\n";
		resultS=resultS+s;//simTa.append(s);
		s = resources.getString("report.a0");
		s = s + Convertor.formatNumber(a0, 5) 
		+ resources.getString("report.plusminus")
		+Convertor.formatNumber(a0_uncGauss, 5)
		+"<br>";//"\n";
		resultS=resultS+s;//simTa.append(s);
		s = resources.getString("report.a0.extUnc.Gauss");
		s = s + Convertor.formatNumber(a0_uncGauss, 5)
		+"<br>";//"\n";
		resultS=resultS+s;//simTa.append(s);
		s = resources.getString("report.a0.extUnc.Poisson");
		s = s + Convertor.formatNumber(a0_uncPoisson, 5)
		+"<br>";//"\n";
		resultS=resultS+s;//simTa.append(s);
		s = resources.getString("report.mda0");
		s = s + Convertor.formatNumber(mda0, 5) 
		+ resources.getString("report.plusminus")
		+Convertor.formatNumber(mda0_unc, 5)
		+"<br>";//"\n";
		resultS=resultS+s;//simTa.append(s);
		
		s=" "
		+"<br>";//"\n";
		resultS=resultS+s;//simTa.append(s);
		s = resources.getString("report.a")+activityUnits+resources.getString("report.]");
		s = s + Convertor.formatNumber(activity, 5) 
		+ resources.getString("report.plusminus")
		+Convertor.formatNumber(activity_extUnc_Gauss, 5)
		+"<br>";//"\n";
		
		resultS=resultS + ( (diffB)?
				" <b><font color='green'>":" <b><font color='blue'>");
		resultS=resultS+s;//simTa.append(s);
		resultS=resultS+ "</font></b> ";
		
		s = resources.getString("report.a.extUnc.Gauss");
		s = s + Convertor.formatNumber(activity_extUnc_Gauss, 5)
		+"<br>";//"\n";
		resultS=resultS+s;//simTa.append(s);
		s = resources.getString("report.a.extUnc.Poisson");
		s = s + Convertor.formatNumber(activity_extUnc_Poisson, 5)
		+"<br>";//"\n";
		resultS=resultS+s;//simTa.append(s);
		
		s = resources.getString("report.mda")+activityUnits+resources.getString("report.]");
		s = s + Convertor.formatNumber(mda, 5) 
		+ resources.getString("report.plusminus")
		+Convertor.formatNumber(mda_unc, 5)
		+"<br>";//"\n";
		
		resultS=resultS + ( (diffB)?
				" <b><font color='blue'>":" <b><font color='green'>");
		resultS=resultS+s;//simTa.append(s);
		resultS=resultS+ "</font></b> ";
		//========
		s=" "
		+"<br>";//"\n";
		resultS=resultS+s;//simTa.append(s);
		if (diffB){
			//sample
			s = resources.getString("report.final")+activityUnits+resources.getString("report.]");
			s = s + Convertor.formatNumber(activity, 5) 
			+ resources.getString("report.plusminus")
			+Convertor.formatNumber(activity_extUnc_Gauss, 5)
			+ "; (&lt;"+Convertor.formatNumber(
					activity+activity_extUnc_Gauss, 3)+")"
			+"<br>";//"\n";
			resultS=resultS +" <b><font color='green'>";
			resultS=resultS+s;//simTa.append(s);
			resultS=resultS+ "</font></b> ";
		} else {
			//MDA
			s = resources.getString("report.final")+activityUnits+resources.getString("report.]");
			s = s + Convertor.formatNumber(mda, 5) 
			+ resources.getString("report.plusminus")
			+Convertor.formatNumber(mda_unc, 5)
			+"; (&lt;"+Convertor.formatNumber(
					mda+mda_unc, 3)+")"
			+"<br>";//"\n";
			resultS=resultS +" <b><font color='green'>";
			resultS=resultS+s;//simTa.append(s);
			resultS=resultS+ "</font></b> ";
		}
		
		//simTa.append(resultS);
		htmlToPrint = resultS;
		//create HTML document============
				String htmlTemp = "temp.html";
				currentDir = System.getProperty("user.dir");
				file_sep = System.getProperty("file.separator");
				String filename = currentDir + file_sep + htmlTemp; 
				macroFile=new File(filename);
				try {
					FileWriter sigfos = new FileWriter(macroFile);//filename);
					sigfos.write(htmlToPrint);
					sigfos.close();			
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				//////and display it		
				try {
					java.net.URL filenamURL = new File(filename).toURI().toURL();
					//helpURL = new java.net.URL(filename);		
						//ClassLoader.getSystemResource(filename);//"input.html");
					textArea.setPage(filenamURL);//helpURL);
				//System.out.println(helpURL.toString());
							
				} catch (Exception  e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		//===========
		tabs.setSelectedIndex(2);

		// double t_coverageFactor=StatsUtil.getStudentFactor(f_gauss);
		// CPS_unc_gauss=t_coverageFactor*CPS_unc_gauss;
	}

	/**
	 * Save sample analysis results in database.
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
		data[kCol] = Convertor.doubleToString(activity);
		kCol++;
		data[kCol] = Convertor.doubleToString(activity_extUnc_Gauss);
		kCol++;
		data[kCol] = Convertor.doubleToString(activity_extUnc_Poisson);
		kCol++;
		data[kCol] = Convertor.doubleToString(activity_coverageFactorGauss);
		kCol++;
		data[kCol] = Convertor.doubleToString(activity_coverageFactorPoisson);
		kCol++;
		data[kCol] = activityUnits;
		kCol++;
		data[kCol] = nucName;
		kCol++;
		data[kCol] = Convertor.doubleToString(mda);
		kCol++;
		data[kCol] = Convertor.doubleToString(mda_unc);
		kCol++;
		data[kCol] = ""+significance;
		kCol++;
				
		dbagent.insert(data);//, orderbyS);	
		
		//now the detail table
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
		// ==================
		/*try {
			String datas = resources.getString("data.load");
			String currentDir = System.getProperty("user.dir");
			String file_sep = System.getProperty("file.separator");
			String opens = currentDir + file_sep + datas;
			String dbName = alphaBetaDB;
			opens = opens + file_sep + dbName;

			String s = "select * from " + sampleTable;

			Connection con1 = DBConnection.getDerbyConnection(opens, "", "");
			DBOperation.select(s, con1);

			int id = DBOperation.getRowCount();

			// insert always!!

			id = id + 1;// id where we make the insertion
			PreparedStatement psInsert = null;

			psInsert = con1.prepareStatement("insert into " + sampleTable
					+ " values " + "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
			psInsert.setString(1, Convertor.intToString(id));
			psInsert.setString(2, descriptionTf.getText());
			psInsert.setString(3, measurementDate);
			psInsert.setString(4, Convertor.doubleToString(activity));
			psInsert.setString(5, Convertor.doubleToString(activity_extUnc_Gauss));
			psInsert.setString(6, Convertor.doubleToString(activity_extUnc_Poisson));
			psInsert.setString(7, Convertor.doubleToString(activity_coverageFactorGauss));
			psInsert.setString(8, Convertor.doubleToString(activity_coverageFactorPoisson));
			
			psInsert.setString(9, activityUnits);
			psInsert.setString(10, nucName);
			psInsert.setString(11, Convertor.doubleToString(mda));
			psInsert.setString(12, Convertor.doubleToString(mda_unc));
			psInsert.setString(13, ""+significance);

			psInsert.executeUpdate();

			int n = cpsV.size();
			psInsert = con1.prepareStatement("insert into " + sampleDetailsTable
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

	/**
	 * Display data.
	 */
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
		//====================
		dbagent.init();
		orderbyS = mainTablePrimaryKey;// when start-up...ID is default!!
		
		nesteddbagent.init();
		nestedorderbyS = nestedTablePrimaryKey;

		effdbagent.init();
		efforderbyS = effmainTablePrimaryKey;// when start-up...ID is default!!

		bkgdbagent.init();
		bkgorderbyS = bkgmainTablePrimaryKey;// when start-up...ID is default!!
		
		bkgnesteddbagent.init();
		bkgnestedorderbyS = bkgnestedTablePrimaryKey;
		
		mainTable = dbagent.getMainTable();
		// allow single selection of rows...not multiple rows!
		ListSelectionModel rowSM = mainTable.getSelectionModel();
		rowSM.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		rowSM.addListSelectionListener(this);// listener!	
		
		nestedTable = nesteddbagent.getMainTable();
		ListSelectionModel rowSM2 = nestedTable.getSelectionModel();
		rowSM2.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);//ListSelectionModel.SINGLE_SELECTION);
		rowSM2.addListSelectionListener(this);
		
		effmainTable = effdbagent.getMainTable();
		// allow single selection of rows...not multiple rows!
		ListSelectionModel nucrowSM = effmainTable.getSelectionModel();
		nucrowSM.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		nucrowSM.addListSelectionListener(this);// listener!	
		
		bkgmainTable = bkgdbagent.getMainTable();
		// allow single selection of rows...not multiple rows!
		ListSelectionModel bkgrowSM = bkgmainTable.getSelectionModel();
		bkgrowSM.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		bkgrowSM.addListSelectionListener(this);// listener!	
		
		bkgnestedTable = bkgnesteddbagent.getMainTable();
		ListSelectionModel bkgrowSM2 = bkgnestedTable.getSelectionModel();
		bkgrowSM2.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);//ListSelectionModel.SINGLE_SELECTION);
		bkgrowSM2.addListSelectionListener(this);
		
		IDBKG = dbagent.getAIPrimaryKeyValue();
		//NRCRTnested = nesteddbagent.getAIPrimaryKeyValue();//max value
		
		if (mainTable.getRowCount() > 0){
			//select last row!
			mainTable.setRowSelectionInterval(mainTable.getRowCount() - 1,
					mainTable.getRowCount() - 1); // last ID
		} else{
			IDBKG = 1;// initialization!
			nDataList = 0;
		}		
		
		if (effmainTable.getRowCount() > 0) {
			// always display last row!
			effmainTable.setRowSelectionInterval(effmainTable.getRowCount() - 1,
					effmainTable.getRowCount() - 1); // last ID
			
		}
		if (bkgmainTable.getRowCount() > 0) {
			// always display last row!
			bkgmainTable.setRowSelectionInterval(bkgmainTable.getRowCount() - 1,
					bkgmainTable.getRowCount() - 1); // last ID
			
		}
		//=============
		/*try {
			String datas = resources.getString("data.load");
			String currentDir = System.getProperty("user.dir");
			String file_sep = System.getProperty("file.separator");
			String opens = currentDir + file_sep + datas;
			String dbName = alphaBetaDB;
			opens = opens + file_sep + dbName;

			String s = "select * from " + sampleTable;

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
	 * Update nested table based on selection in main table. Here, it updates controls (including the count list) based on selected sample 
	 * record.
	 */
	private void updateDetailTable() {
		JTable aspTable = mainTable;//asp.getTab();
		int selID = 0;// NO ZERO ID
		int selRow = aspTable.getSelectedRow();
		if (selRow != -1) {
			selID = (Integer) aspTable.getValueAt(selRow, 0);
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
			String s = "select * from " + sampleTable+ " where ID = "
			+ selID;

			Connection con1 = DBConnection.getDerbyConnection(opens, "", "");
			DBOperation.select(s, con1);
			
			descriptionS=(String)DBOperation.getValueAt(0, 1);
			descriptionTf.setText(descriptionS);
			measurementDate=(String)DBOperation.getValueAt(0, 2);
			
			TimeUtilities tu = new TimeUtilities(measurementDate);
			//TimeUtilities.unformatDate(measurementDate);
			dayCb.setSelectedItem((Object) tu.getDayS());//TimeUtilities.idayS);
			monthCb.setSelectedItem((Object) tu.getMonthS());//TimeUtilities.imonthS);
			yearTf.setText(tu.getYearS());//TimeUtilities.iyearS);

			//=====================
			// now the job:
			s = "select * from " + sampleDetailsTable + " where ID = "
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
	
	/**
	 * Dummy method.
	 */
	private void initDBComponents() {
		//nucV = new Vector<String>();
		//nucCb = new JComboBox();
		//nucCb.setMaximumRowCount(15);
		//nucCb.setPreferredSize(sizeCb2);
		
		//Connection conn = null;

		//Statement s = null;
		//ResultSet rs = null;
		//try {
		//	String datas = resources.getString("data.load");// Data
		//	String currentDir = System.getProperty("user.dir");
		//	String file_sep = System.getProperty("file.separator");
		//	String opens = currentDir + file_sep + datas;
		//	String dbName = icrpDB;// "ICRP38"; // the name of the database
		//	opens = opens + file_sep + dbName;
			
		//	conn = DBConnection.getDerbyConnection(opens, "", "");

		//	conn.setAutoCommit(false);

		//	s = conn.createStatement();
		//	rs = s.executeQuery("SELECT * FROM " + icrpTable);

		//	if (rs != null)
		//		while (rs.next()) {
		//			String ss = rs.getString(2);
					//nucV.addElement(ss);
					//nucCb.addItem(ss);
		//		}

		//	conn.commit();

		//} catch (Exception ex) {
		//	ex.printStackTrace();
		//} finally {
			// release all open resources to avoid unnecessary memory usage

			// ResultSet
		//	try {
		//		if (rs != null) {
		//			rs.close();
		//			rs = null;
		//		}
		//	} catch (Exception sqle) {
		//		sqle.printStackTrace();
		//	}

			// Connection
		//	try {
		//		if (conn != null) {
		//			conn.close();
		//			conn = null;
		//		}
		//	} catch (Exception sqle) {
		//		sqle.printStackTrace();
		//	}
		//}
		
		//initAuxTables();
	}
	
	/*private void initAuxTables(){
		try {
			String datas = resources.getString("data.load");
			String currentDir = System.getProperty("user.dir");
			String file_sep = System.getProperty("file.separator");
			String opens = currentDir + file_sep + datas;
			String dbName = alphaBetaDB;
			opens = opens + file_sep + dbName;

			String s = "select * from " + effTable;

			Connection con1 = DBConnection.getDerbyConnection(opens, "", "");
			DBOperation.select(s, con1);

			aspNuc = new AdvancedSelectPanel();
			suportSpNuc.add(aspNuc, BorderLayout.CENTER);

			JTable mainTable = aspNuc.getTab();

			ListSelectionModel rowSM = mainTable.getSelectionModel();
			rowSM.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
						
			if (mainTable.getRowCount() > 0) {
				// always display last row!
				mainTable.setRowSelectionInterval(mainTable.getRowCount() - 1,
						mainTable.getRowCount() - 1); // last ID
				
			} else {
				
			}
			//====================
			s = "select * from " + bkgTable;
			DBOperation.select(s, con1);

			aspBkg = new AdvancedSelectPanel();
			suportSpBkg.add(aspBkg, BorderLayout.CENTER);

			mainTable = aspBkg.getTab();

			rowSM = mainTable.getSelectionModel();
			rowSM.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
						
			if (mainTable.getRowCount() > 0) {
				// always display last row!
				mainTable.setRowSelectionInterval(mainTable.getRowCount() - 1,
						mainTable.getRowCount() - 1); // last ID
				
			} else {
				
			}
			//----------------------------
			if (con1 != null)
				con1.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}*/

	// ===========
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

			//conng = DriverManager.getConnection(protocol + opens
			//		+ ";create=true", "", "");

			conng = DBConnection.getDerbyConnection(opens, "", "");
			String str = "";
			// ------------------
			conng.setAutoCommit(false);
			s = conng.createStatement();

			// delete the table
			// s.execute("drop table " +
			// resources.getString("main.db.eff.alphaTable"));
			// s.execute("drop table " +
			//		 resources.getString("main.db.eff.betaTable"));
			///* s.execute("drop table " +
			//		 resources.getString("main.db.eff.alphaDetailsTable"));
			 //s.execute("drop table " +
			//		 resources.getString("main.db.eff.betaDetailsTable"));
			 //s.execute("drop table " +
			//		 resources.getString("main.db.nuc.alphaTable"));
			 //s.execute("drop table " +
			//		 resources.getString("main.db.nuc.betaTable"));

			str = "create table "
					+ resources.getString("main.db.sample.alphaTable")
					+ " ( ID integer, "
					+ "description VARCHAR(50), date VARCHAR(50), "
					+ "sample_activity DOUBLE PRECISION, "
					+ "sample_activity_extUnc_GAUSS DOUBLE PRECISION, " +
					"sample_activity_extUnc_POISSON DOUBLE PRECISION, " +
					"coverage_Factor_Gauss DOUBLE PRECISION, " +
					"coverage_Factor_Poisson DOUBLE PRECISION"+
					", activity_units VARCHAR(50), "+
					"equivalent VARCHAR(50), "+
					"mda DOUBLE PRECISION, "+
					"mda_Unc DOUBLE PRECISION, "+
					"significance VARCHAR(50))"
					;
			s.execute(str);

			str = "create table "
					+ resources.getString("main.db.sample.betaTable")
					+ " ( ID integer, "
					+ "description VARCHAR(50), date VARCHAR(50), "
					+ "sample_activity DOUBLE PRECISION, "
					+ "sample_activity_extUnc_GAUSS DOUBLE PRECISION, " +
					"sample_activity_extUnc_POISSON DOUBLE PRECISION, " +
					"coverage_Factor_Gauss DOUBLE PRECISION, " +
					"coverage_Factor_Poisson DOUBLE PRECISION"+
					", activity_units VARCHAR(50), "+
					"equivalent VARCHAR(50), "+
					"mda DOUBLE PRECISION, "+
					"mda_Unc DOUBLE PRECISION, "+
					"significance VARCHAR(50))"
					;
			s.execute(str);

			str = "create table "
					+ resources.getString("main.db.sample.alphaDetailsTable")
					+ " ( NRCRT integer, ID integer, "
					+ "Counts DOUBLE PRECISION, time_sec DOUBLE PRECISION, "
					+ "CPS DOUBLE PRECISION)";
			s.execute(str);

			str = "create table "
					+ resources.getString("main.db.sample.betaDetailsTable")
					+ " ( NRCRT integer, ID integer, "
					+ "Counts DOUBLE PRECISION, time_sec DOUBLE PRECISION, "
					+ "CPS DOUBLE PRECISION)";
			s.execute(str);
			
			//str = "create table "
			//	+ resources.getString("main.db.nuc.alphaTable")
			//	+ " ( ID integer, nuclide VARCHAR(50), "
			//	+ "description VARCHAR(50), date VARCHAR(50), "
			//	+ "activ_Bq DOUBLE PRECISION, "
			//	+ "activ_Bq_extUnc DOUBLE PRECISION, halfLife_sec DOUBLE PRECISION, coverageFactor DOUBLE PRECISION)";
			//s.execute(str);
			
			//str = "create table "
			//	+ resources.getString("main.db.nuc.betaTable")
			//	+ " ( ID integer, nuclide VARCHAR(50), "
			//	+ "description VARCHAR(50), date VARCHAR(50), "
			//	+ "activ_Bq DOUBLE PRECISION, "
			//	+ "activ_Bq_extUnc DOUBLE PRECISION, halfLife_sec DOUBLE PRECISION, coverageFactor DOUBLE PRECISION)";
			//s.execute(str);

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
