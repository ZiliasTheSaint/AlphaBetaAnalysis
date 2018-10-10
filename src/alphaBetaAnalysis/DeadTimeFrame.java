package alphaBetaAnalysis;


import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ResourceBundle;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import danfulea.math.Convertor;
import danfulea.phys.PhysUtilities;
import danfulea.utils.FrameUtilities;


/**
 * Class for performing dead-time calculations.
 * @author Dan Fulea, 06 Jul. 2011
 *
 */
@SuppressWarnings("serial")
public class DeadTimeFrame extends JFrame implements ActionListener{
	
	private AlphaBetaAnalysis mf;
	private static final String BASE_RESOURCE_CLASS = "alphaBetaAnalysis.resources.AlphaBetaAnalysisResources";
	private ResourceBundle resources;
	
	private final Dimension PREFERRED_SIZE = new Dimension(800, 600);
	
	private JTextField r1Tf=new JTextField(5);
	private JTextField r2Tf=new JTextField(5);
	private JTextField a1diva2Tf=new JTextField(5);
	private JTextField r12Tf=new JTextField(5);
	private JLabel deadTimeLbl=new JLabel();
	private JRadioButton method1Rb, method2Rb = null;
	
	private static final String COMPUTE_COMMAND = "COMPUTE";
	private String command = null;
	
	/**
	 * Constructor.
	 * @param mf, mf - AlphaBetaAnalysis object
	 */
	public DeadTimeFrame(AlphaBetaAnalysis mf){
		resources = ResourceBundle.getBundle(BASE_RESOURCE_CLASS);
		String titles="";
		if (AlphaBetaAnalysis.IMODE==AlphaBetaAnalysis.IALPHA){
			titles=resources.getString("mode.ALPHA")+
			resources.getString("DeadTimeFrame.NAME");			
		} else if (AlphaBetaAnalysis.IMODE==AlphaBetaAnalysis.IBETA){
			titles=resources.getString("mode.BETA")+
			resources.getString("DeadTimeFrame.NAME");
			
		}		
		this.setTitle(titles);	
		this.mf=mf;
		
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
	 * Exit method
	 */
	private void attemptExit() {		
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
		Character mnemonic = null;
		JButton button = null;
		JLabel label = null;
		String buttonName = "";
		String buttonToolTip = "";
		String buttonIconName = "";

		method1Rb = new JRadioButton(resources.getString("dtf.method1Rb"));
		method1Rb.setForeground(AlphaBetaAnalysis.foreColor);
		method1Rb.setBackground(AlphaBetaAnalysis.bkgColor);
		
		method2Rb = new JRadioButton(resources.getString("dtf.method2Rb"));
		method2Rb.setForeground(AlphaBetaAnalysis.foreColor);
		method2Rb.setBackground(AlphaBetaAnalysis.bkgColor);
		
		ButtonGroup group = new ButtonGroup();
		group.add(method1Rb);
		group.add(method2Rb);
		method1Rb.setSelected(true);

		JPanel p1=new JPanel();
		p1.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 2));		
		label = new JLabel(resources.getString("dtf.r1Tf.label"));
		label.setForeground(AlphaBetaAnalysis.foreColor);
		p1.add(label);
		p1.add(r1Tf);
		p1.setBackground(AlphaBetaAnalysis.bkgColor);
		
		JPanel p2=new JPanel();
		p2.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 2));		
		label = new JLabel(resources.getString("dtf.r2Tf.label"));
		label.setForeground(AlphaBetaAnalysis.foreColor);
		p2.add(label);
		p2.add(r2Tf);
		p2.setBackground(AlphaBetaAnalysis.bkgColor);
		
		JPanel p3=new JPanel();
		p3.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 2));		
		label = new JLabel(resources.getString("dtf.a1diva2Tf.label"));
		label.setForeground(AlphaBetaAnalysis.foreColor);
		p3.add(method1Rb);
		p3.add(label);
		p3.add(a1diva2Tf);
		p3.setBackground(AlphaBetaAnalysis.bkgColor);
		
		JPanel p4=new JPanel();
		p4.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 2));		
		label = new JLabel(resources.getString("dtf.or.label"));
		label.setForeground(AlphaBetaAnalysis.foreColor);
		p4.add(label);		
		p4.setBackground(AlphaBetaAnalysis.bkgColor);
		
		JPanel p5=new JPanel();
		p5.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 2));		
		label = new JLabel(resources.getString("dtf.r12Tf.label"));
		label.setForeground(AlphaBetaAnalysis.foreColor);
		p5.add(method2Rb);
		p5.add(label);
		p5.add(r12Tf);
		p5.setBackground(AlphaBetaAnalysis.bkgColor);
		
		buttonName = resources.getString("dtf.compute");
		buttonToolTip = resources.getString("dtf.compute.toolTip");
		buttonIconName = resources.getString("img.set");
		button = FrameUtilities.makeButton(buttonIconName, COMPUTE_COMMAND,
				buttonToolTip, buttonName, this, this);
		mnemonic = (Character) resources.getObject("dtf.compute.mnemonic");
		button.setMnemonic(mnemonic.charValue());
		JPanel p6=new JPanel();
		p6.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 2));	
		p6.add(Box.createRigidArea(new Dimension(200, 0)));// some space
		p6.add(button);
		label = new JLabel(resources.getString("dtf.deadtime.label"));
		label.setForeground(AlphaBetaAnalysis.foreColor);
		p6.add(label);		
		p6.add(deadTimeLbl);
		p6.setBackground(AlphaBetaAnalysis.bkgColor);
		//------
		JPanel mainP = new JPanel();
		BoxLayout blmainP = new BoxLayout(mainP, BoxLayout.Y_AXIS);
		mainP.setLayout(blmainP);
		mainP.add(p1, null);
		mainP.add(p2, null);
		mainP.add(p3, null);
		mainP.add(p4, null);
		mainP.add(p5, null);
		mainP.add(p6, null);
		mainP.setBackground(AlphaBetaAnalysis.bkgColor);
		
		JPanel content = new JPanel(new BorderLayout());		
		content.add(mainP, BorderLayout.CENTER);
		setContentPane(new JScrollPane(content));
		content.setOpaque(true); // content panes must be opaque
		pack();
	}
	
	/**
	 * Setting up actions!
	 */
	public void actionPerformed(ActionEvent arg0) {
		// TODO Auto-generated method stub
		command = arg0.getActionCommand();
		if (command.equals(COMPUTE_COMMAND)) {
			computeDeadTime();
		}
	}
	
	/**
	 * Compute the dead time
	 */
	private void computeDeadTime(){
		double r1=0.0;
		double r2=0.0;
		double a1a2=0.0;
		double r12=0.0;
		boolean nulneg=false;
		try {
			r1 = Convertor.stringToDouble(r1Tf.getText());
			if (r1 <= 0)
				nulneg = true;	
			r2 = Convertor.stringToDouble(r2Tf.getText());
			if (r2 <= 0)
				nulneg = true;	
			if (method1Rb.isSelected()){
				a1a2=Convertor.stringToDouble(a1diva2Tf.getText());
				if (a1a2 <= 0)
					nulneg = true;	

			} else if (method2Rb.isSelected()){
				r12=Convertor.stringToDouble(r12Tf.getText());
				if (r12 <= 0)
					nulneg = true;	
				
			}
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
		
		if (method1Rb.isSelected()){
			mf.deadTime=PhysUtilities.computeDeadTime(a1a2, r1, r2);
		} else if (method2Rb.isSelected()){
			mf.deadTime=PhysUtilities.computeDeadTime2(r12, r1, r2);
		}
		
		//deadTimeLbl.setText(Convertor.doubleToString(mf.deadTime));
		//mf.deadTimeTf.setText(Convertor.doubleToString(mf.deadTime));
		
		deadTimeLbl.setText(Convertor.formatNumberScientific(mf.deadTime));
		mf.deadTimeTf.setText(Convertor.formatNumberScientific(mf.deadTime));
	}
}
