package alphaBetaAnalysis;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ResourceBundle;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import danfulea.utils.ScanDiskLFGui;
import danfulea.db.DatabaseAgent;
//import jdf.db.DBConnection;
//import jdf.db.DBOperation;
import danfulea.math.Convertor;
import danfulea.utils.FrameUtilities;

/**
 * The AlphaBetaAnalysis class. <br>
 * It is designed to perform gross (global) alpha and beta analysis, i.e. computing sample activity based on pulses recorded by 
 * alpha or beta detectors. All uncertainties are properly handled. <br>
 * First and foremost - user must set the dead-time which is required for input pulses dead-time correction. In short, dead time is the small 
 * amount of time when detector is unable to score a pulse.<br>
 * Second - user must record the background pulses in order to perform background correction of counts rate.<br>
 * Third - user must provide either by computation or from other sources (for instance, metrology certificate) the source-detector detection efficiency 
 * for a specific nuclide. This efficiency can also be theoretical computed using Monte Carlo simulation technique. <br>
 * Last - perform actual sample computation providing the above quantities are properly set.
 * @author Dan Fulea, 06 Jul. 2011
 * 
 */
@SuppressWarnings("serial")
public class AlphaBetaAnalysis extends JFrame implements ActionListener{
	public static final int IALPHA=0;
	public static final int IBETA=1;
	public static int IMODE=0;
	private final Dimension PREFERRED_SIZE = new Dimension(500, 300);
	//private final Dimension sizeCb = new Dimension(60, 21);
	
	public static Color bkgColor = new Color(230, 255, 210, 255);//Linux mint green alike
	public static Color foreColor = Color.black;//Color.white;
	public static Color textAreaBkgColor = Color.white;//Color.black;
	public static Color textAreaForeColor = Color.black;//Color.yellow;
	public static boolean showLAF=true;
	
	private static final String BASE_RESOURCE_CLASS = "alphaBetaAnalysis.resources.AlphaBetaAnalysisResources";
	private ResourceBundle resources = ResourceBundle
			.getBundle(BASE_RESOURCE_CLASS);
	private Window parent = null;
	private boolean standalone=true;
	
	private String stabilityDB;
	private String deadTimeTable;
	protected double deadTime=-1.0;
	
	protected JTextField deadTimeTf=new JTextField(5);
	
	private static final String EXIT_COMMAND = "EXIT";
	private static final String ABOUT_COMMAND = "ABOUT";
	private static final String LOOKANDFEEL_COMMAND = "LOOKANDFEEL";
	private static final String SET_COMMAND = "SET";
	private static final String COMPUTE_COMMAND = "COMPUTE";
	private static final String BACKGROUND_COMMAND = "BACKGROUND";
	private static final String EFFICIENCY_COMMAND = "EFFICIENCY";
	private static final String SAMPLE_COMMAND = "SAMPLE";
	private String command = null;
	
	/**
	 * The connection
	 */
	private Connection stabilitydbcon = null;
	/**
	 * Constructor.
	 */
	public AlphaBetaAnalysis() {
		//DBConnection.startDerby();
		
		//this.setTitle(resources.getString("Application.NAME"));
		String titles="";
		if (IMODE==IALPHA){
			titles=resources.getString("mode.ALPHA");
			deadTimeTable=resources.getString("deadtime.table.alpha");
		} else if (IMODE==IBETA){
			titles=resources.getString("mode.BETA");
			deadTimeTable=resources.getString("deadtime.table.beta");
		}
		titles=titles+resources.getString("common.NAME");
		this.setTitle(titles);	
		
		stabilityDB = resources.getString("stability.db");
		//------------------------------------
		DatabaseAgent.ID_CONNECTION = DatabaseAgent.DERBY_CONNECTION;
		startDerbyConnection();// with stabilityDB
		//------------------------------------
		
		// the key to force decision made by attemptExit() method on close!!
		// otherwise...regardless the above decision, the application exit!
		// notes: solved this minor glitch in latest sun java!!
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				attemptExit();
			}
		});

		JMenuBar menuBar = createMenuBar(resources);
		setJMenuBar(menuBar);

		
		performQueryDb();
		createGUI();
		
		setDefaultLookAndFeelDecorated(true);
		FrameUtilities.createImageIcon(
				this.resources.getString("form.icon.url"), this);

		FrameUtilities.centerFrameOnScreen(this);

		setVisible(true);
		//===========TEMP==============
		
	}
	
	/**
	 * Starts derby connection and initializes the agents (if any).
	 */
	private void startDerbyConnection() {

		String datas = this.resources.getString("data.load");// "Data";
		String currentDir = System.getProperty("user.dir");
		String file_sep = System.getProperty("file.separator");
		String opens = currentDir + file_sep + datas;

		opens = opens + file_sep + stabilityDB;
		//-------------------------------------------------------------
		stabilitydbcon = DatabaseAgent.getConnection(opens, "", "");		
		//dbagent = new DatabaseAgentSupport(dbcon, mainTablePrimaryKey, deadTimeTable);
			
	}
	
	/**
	 * Second construction when this program is just a module, i.e. part of a larger computer program. 
	 * @param frame frame, i.e. the calling main program
	 */
	public AlphaBetaAnalysis(Window frame) {
		this();
		this.parent = frame;
		standalone=false;		
	}
	
	/**
	 * Setting up the frame size.
	 */
	public Dimension getPreferredSize() {
		return PREFERRED_SIZE;
	}

	/**
	 * Close program.
	 */
	private void attemptExit() {
		//exit without any warning!!
		
		//String title = resources.getString("dialog.exit.title");
		//String message = resources.getString("dialog.exit.message");

		//Object[] options = (Object[]) resources
		//		.getObject("dialog.exit.buttons");
		//int result = JOptionPane.showOptionDialog(this, message, title,
		//		JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null,
		//		options, options[0]);
		//if (result == JOptionPane.YES_OPTION) {
		if(standalone){
			try{
				if (stabilitydbcon != null)
					stabilitydbcon.close();
			}catch (Exception e){
				e.printStackTrace();
			}
			//DBConnection.shutdownDerby();
			DatabaseAgent.shutdownDerby();
			dispose();
			System.exit(0);
		}
		else{
			try{
				if (stabilitydbcon != null)
					stabilitydbcon.close();
			}catch (Exception e){
				e.printStackTrace();
			}
			parent.setVisible(true);
			dispose();
		}
		//}
	}
	
	/**
	 * GUI creation.
	 */
	private void createGUI() {
		JPanel content = new JPanel(new BorderLayout());
		
		JPanel mainPanel = createMainPanel();
		content.add(mainPanel, BorderLayout.CENTER);
		//content.add(statusBar, BorderLayout.PAGE_END);

		setContentPane(new JScrollPane(content));
		content.setOpaque(true); // content panes must be opaque
		pack();
	}
	
	/**
	 * Create main panel
	 * @return the result
	 */
	private JPanel createMainPanel() {
		Character mnemonic = null;
		JButton button = null;
		JLabel label = null;
		String buttonName = "";
		String buttonToolTip = "";
		String buttonIconName = "";

		JPanel p1=new JPanel();
		p1.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 4));
		p1.add(deadTimeTf);
		label = new JLabel(resources.getString("main.deadTime.label"));
		label.setForeground(foreColor);
		p1.add(label);
		p1.setBackground(bkgColor);
		
		deadTimeTf.setText(Convertor.doubleToString(deadTime));
		
		JPanel p2=new JPanel();
		p2.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 4));
		buttonName = resources.getString("main.set");
		buttonToolTip = resources.getString("main.set.toolTip");
		buttonIconName = null;//resources.getString("img.insert");
		button = FrameUtilities.makeButton(buttonIconName, SET_COMMAND,
				buttonToolTip, buttonName, this, this);
		mnemonic = (Character) resources.getObject("main.set.mnemonic");
		button.setMnemonic(mnemonic.charValue());
		p2.add(button);
		buttonName = resources.getString("main.compute");
		buttonToolTip = resources.getString("main.compute.toolTip");
		buttonIconName = null;//resources.getString("img.insert");
		button = FrameUtilities.makeButton(buttonIconName, COMPUTE_COMMAND,
				buttonToolTip, buttonName, this, this);
		mnemonic = (Character) resources.getObject("main.compute.mnemonic");
		button.setMnemonic(mnemonic.charValue());
		p2.add(button);
		p2.setBackground(bkgColor);
		
		JPanel rbPan = new JPanel();
		BoxLayout blrbPan = new BoxLayout(rbPan, BoxLayout.Y_AXIS);
		rbPan.setLayout(blrbPan);
		rbPan.add(p1, null);
		rbPan.add(p2, null);
		rbPan.setBackground(bkgColor);
		rbPan.setBorder(FrameUtilities.getGroupBoxBorder(
				resources.getString("main.deadTime.border"), foreColor));
		//========
		JPanel p11=new JPanel();
		p11.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 4));
		buttonName = resources.getString("main.background");
		buttonToolTip = resources.getString("main.background.toolTip");
		buttonIconName = null;//resources.getString("img.insert");
		button = FrameUtilities.makeButton(buttonIconName, BACKGROUND_COMMAND,
				buttonToolTip, buttonName, this, this);
		mnemonic = (Character) resources.getObject("main.background.mnemonic");
		button.setMnemonic(mnemonic.charValue());
		p11.add(button);
		p11.setBackground(bkgColor);
		
		JPanel p21=new JPanel();
		p21.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 4));
		buttonName = resources.getString("main.efficiency");
		buttonToolTip = resources.getString("main.efficiency.toolTip");
		buttonIconName = null;//resources.getString("img.insert");
		button = FrameUtilities.makeButton(buttonIconName, EFFICIENCY_COMMAND,
				buttonToolTip, buttonName, this, this);
		mnemonic = (Character) resources.getObject("main.efficiency.mnemonic");
		button.setMnemonic(mnemonic.charValue());
		p21.add(button);
		p21.setBackground(bkgColor);
		
		JPanel p31=new JPanel();
		p31.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 4));
		buttonName = resources.getString("main.sample");
		buttonToolTip = resources.getString("main.sample.toolTip");
		buttonIconName = null;//resources.getString("img.insert");
		button = FrameUtilities.makeButton(buttonIconName, SAMPLE_COMMAND,
				buttonToolTip, buttonName, this, this);
		mnemonic = (Character) resources.getObject("main.sample.mnemonic");
		button.setMnemonic(mnemonic.charValue());
		p31.add(button);
		p31.setBackground(bkgColor);
		
		JPanel rbPan1 = new JPanel();
		BoxLayout blrbPan1 = new BoxLayout(rbPan1, BoxLayout.Y_AXIS);
		rbPan1.setLayout(blrbPan1);
		rbPan1.add(p11, null);
		rbPan1.add(p21, null);
		rbPan1.add(p31, null);
		rbPan1.setBackground(bkgColor);
		rbPan1.setBorder(FrameUtilities.getGroupBoxBorder(
				resources.getString("main.analysis.border"), foreColor));
		//--------
		JPanel pan=new JPanel();
		pan.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 5));
		pan.add(rbPan);
		pan.add(rbPan1);
		pan.setBackground(bkgColor);

		JPanel mainP = new JPanel(new BorderLayout());
		//mainP.add(northPanel, BorderLayout.NORTH);
		mainP.add(pan, BorderLayout.CENTER);
		mainP.setBackground(bkgColor);
		return mainP;
	}
	
	/**
	 * Setting up the menu bar.
	 * 
	 * @param resources resources
	 * @return the menu bar
	 */
	private JMenuBar createMenuBar(ResourceBundle resources) {
		// create the menus
		JMenuBar menuBar = new JMenuBar();

		String label;
		Character mnemonic;
		ImageIcon img;
		String imageName = "";

		// the file menu
		label = resources.getString("menu.file");
		mnemonic = (Character) resources.getObject("menu.file.mnemonic");
		JMenu fileMenu = new JMenu(label, true);
		fileMenu.setMnemonic(mnemonic.charValue());
/*
		imageName = resources.getString("img.open.file");
		img = FrameUtilities.getImageIcon(imageName, this);
		label = resources.getString("openFile");
		mnemonic = (Character) resources.getObject("openFile.mnemonic");
		JMenuItem openFileItem = new JMenuItem(label, mnemonic.charValue());
		openFileItem.setActionCommand(OPENFILE_COMMAND);
		openFileItem.addActionListener(this);
		openFileItem.setIcon(img);
		openFileItem.setToolTipText(resources.getString("openFile.toolTip"));
		fileMenu.add(openFileItem);
*/
		

		fileMenu.addSeparator();

		imageName = resources.getString("img.close");
		img = FrameUtilities.getImageIcon(imageName, this);
		label = resources.getString("menu.file.exit");
		mnemonic = (Character) resources.getObject("menu.file.exit.mnemonic");
		JMenuItem exitItem = new JMenuItem(label, mnemonic.charValue());
		exitItem.setActionCommand(EXIT_COMMAND);
		exitItem.addActionListener(this);
		exitItem.setIcon(img);
		exitItem.setToolTipText(resources.getString("menu.file.exit.toolTip"));
		fileMenu.add(exitItem);

		// the help menu
		label = resources.getString("menu.help");
		mnemonic = (Character) resources.getObject("menu.help.mnemonic");
		JMenu helpMenu = new JMenu(label);
		helpMenu.setMnemonic(mnemonic.charValue());

		imageName = resources.getString("img.about");
		img = FrameUtilities.getImageIcon(imageName, this);
		label = resources.getString("menu.help.about");
		mnemonic = (Character) resources.getObject("menu.help.about.mnemonic");
		JMenuItem aboutItem = new JMenuItem(label, mnemonic.charValue());
		aboutItem.setActionCommand(ABOUT_COMMAND);
		aboutItem.addActionListener(this);
		aboutItem.setIcon(img);
		aboutItem
				.setToolTipText(resources.getString("menu.help.about.toolTip"));

		

		label = resources.getString("menu.help.LF");
		mnemonic = (Character) resources.getObject("menu.help.LF.mnemonic");
		JMenuItem lfItem = new JMenuItem(label, mnemonic.charValue());
		lfItem.setActionCommand(LOOKANDFEEL_COMMAND);
		lfItem.addActionListener(this);
		lfItem.setToolTipText(resources.getString("menu.help.LF.toolTip"));
		
		if(showLAF){
			helpMenu.add(lfItem);
			helpMenu.addSeparator();
		}

		

		helpMenu.add(aboutItem);


		// finally, glue together the menu and return it
		menuBar.add(fileMenu);		
		menuBar.add(helpMenu);

		return menuBar;
	}
	
	/**
	 * Perform database initialization.
	 */
	private void performQueryDb() {
		try {
			//String datas = resources.getString("data.load");
			//String currentDir = System.getProperty("user.dir");
			//String file_sep = System.getProperty("file.separator");
			//String opens = currentDir + file_sep + datas;
			//String dbName = stabilityDB;
			//opens = opens + file_sep + dbName;

			String s = "select * from " + deadTimeTable;

			//Connection con1 = DBConnection.getDerbyConnection(opens, "", "");
			//DBOperation.select(s, con1);
			
			DatabaseAgent.select(stabilitydbcon, s);
			
			//------------
			deadTime=-1.0;//a=1.2E-5 sec;b=9.996E-6 sec!!!
			int ndata = DatabaseAgent.getRowCount();//DBOperation.getRowCount();
			if (ndata>0)
				deadTime=(Double) DatabaseAgent.getValueAt(0, 1);//DBOperation.getValueAt(0, 1);//0 row, 1 col [ID,value]!
			
			//System.out.println("dt= "+deadTime);
			
			//if (con1 != null)
				//con1.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * All actions are defined here
	 */
	public void actionPerformed(ActionEvent arg0) {
		// TODO Auto-generated method stub
		command = arg0.getActionCommand();
		if (command.equals(ABOUT_COMMAND)) {
			about();
		} else if (command.equals(EXIT_COMMAND)) {
			attemptExit();
		} else if (command.equals(LOOKANDFEEL_COMMAND)) {
			lookAndFeel();
		} else if (command.equals(SET_COMMAND)) {
			setDeadTime();
		} else if (command.equals(COMPUTE_COMMAND)) {
			computeDeadTime();
		} else if (command.equals(BACKGROUND_COMMAND)) {
			backgroundAnalysis();
		} else if (command.equals(EFFICIENCY_COMMAND)) {
			efficiencyAnalysis();
		} else if (command.equals(SAMPLE_COMMAND)) {
			sampleAnalysis();
		} 

	}
	
	/**
	 * Set dead time
	 */
	private void setDeadTime(){
		//------------
		boolean nulneg=false;
		try {
			deadTime = Convertor.stringToDouble(deadTimeTf.getText());
			if (deadTime < 0)
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
		//--------------
		try {
			//String datas = resources.getString("data.load");
			//String currentDir = System.getProperty("user.dir");
			//String file_sep = System.getProperty("file.separator");
			//String opens = currentDir + file_sep + datas;
			//String dbName = stabilityDB;
			//opens = opens + file_sep + dbName;

			String s = "select * from " + deadTimeTable;

			//Connection con1 = DBConnection.getDerbyConnection(opens, "", "");
			//DBOperation.select(s, con1);
			DatabaseAgent.select(stabilitydbcon, s);

			int id = DatabaseAgent.getRowCount();//DBOperation.getRowCount();
			if(id>0){//in fact is always 1!!!!!!!!!!!!!
				String ids =DatabaseAgent.getValueAt(0, 0).toString();//DBOperation.getValueAt(0, 0).toString();
				PreparedStatement psUpdate = null;
				
				psUpdate = stabilitydbcon.prepareStatement("update "//con1.prepareStatement("update "
						+ deadTimeTable
						+ " set VALUE=? where ID=?");

				psUpdate.setString(1, Convertor.doubleToString(deadTime));
				psUpdate.setString(2, ids);

				psUpdate.executeUpdate();
				psUpdate.close();
			} else {
				//insert
				
				id = 1;// id where we make the insertion
				PreparedStatement psInsert = null;

				psInsert = stabilitydbcon.prepareStatement("insert into "//con1.prepareStatement("insert into "
						+ deadTimeTable + " values "
						+ "(?, ?)");
				psInsert.setString(1, Convertor.intToString(id));
				psInsert.setString(2, Convertor.doubleToString(deadTime));		

				psInsert.executeUpdate();				
				psInsert.close();
				
			}
			
			//if (con1 != null)
				//con1.close();	

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Initiate the dead time computation. Useful when this quantity is not given by detector manufacture.
	 */
	private void computeDeadTime(){
		new DeadTimeFrame(this);
	}
	
	/**
	 * Initiate background analysis
	 */
	private void backgroundAnalysis(){
		if (deadTime<0.0) {
			String title = resources.getString("deadTime.error.title");
			String message = resources.getString("deadTime.error");
			JOptionPane.showMessageDialog(null, message, title,
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		new BackgroundFrame(this);
	}
	
	/**
	 * Initiate efficiency analysis
	 */
	private void efficiencyAnalysis(){
		if (deadTime<0.0) {
			String title = resources.getString("deadTime.error.title");
			String message = resources.getString("deadTime.error");
			JOptionPane.showMessageDialog(null, message, title,
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		new EfficiencyFrame(this);
	}

	/**
	 * Initiate sample analysis
	 */
	private void sampleAnalysis(){
		if (deadTime<0.0) {
			String title = resources.getString("deadTime.error.title");
			String message = resources.getString("deadTime.error");
			JOptionPane.showMessageDialog(null, message, title,
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		new SampleFrame(this);
	}

	/**
	 * Changing the look and feel can be done here. Also display some gadgets.
	 */
	private void lookAndFeel() {
		setVisible(false);// setEnabled(false);
		new ScanDiskLFGui(this);
	}
	
	/**
	 * Shows the about window!
	 */
	private void about() {
		new AboutFrame(this);
	}
}
