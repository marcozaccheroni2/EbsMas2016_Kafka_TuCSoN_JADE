package examples.playground.backup;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import java.awt.FlowLayout;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.BoxLayout;
import javax.swing.Box;
import javax.swing.JTextPane;
import java.awt.Component;
import javax.swing.border.LineBorder;
import java.awt.Color;
import javax.swing.JScrollPane;
import java.awt.Font;
import java.awt.Dimension;
import javax.swing.JTable;

public class playgroundGUI extends JFrame {

	private JPanel contentPane;
	
	private JScrollPane keeperScrollPane;
	private JTextArea keeperLog;
	private Box horizontalBox;
	private Box keeperBox;
	private Box playersBox;
	private JLabel lblKeeper;
	private JLabel lblPlayers;
	private JScrollPane playersScrollPane;
	private JTextArea playersLog;
	private Box scoreBoard;
	private Box scoreBoard_teams;
	private JLabel lblWhite;
	private Component verticalStrut;
	private Component verticalStrut_1;
	private JLabel lblBlack;
	private Component horizontalStrut;
	private Box scoreBox;
	private JLabel lblWhiteScore;
	private Component rigAreaScore;
	private JLabel lblBlackScore;
	private Component rigidArea;
	private Component verticalStrut_2;
	private Component verticalStrut_3;
	private Component verticalStrut_4;
	private Component verticalStrut_5;
	private Component verticalStrut_6;

	/**
	 * Create the frame.
	 */
	public playgroundGUI() {
		setTitle("The Basketball Playground Scenario");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setExtendedState(JFrame.MAXIMIZED_BOTH);
		setUndecorated(true);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.X_AXIS));
		
		horizontalBox = Box.createHorizontalBox();
		contentPane.add(horizontalBox);
		
		keeperBox = Box.createVerticalBox();
		keeperBox.setAlignmentX(Component.CENTER_ALIGNMENT);
		horizontalBox.add(keeperBox);
		
		verticalStrut_2 = Box.createVerticalStrut(10);
		keeperBox.add(verticalStrut_2);
		
		lblKeeper = new JLabel("Keeper Agent");
		lblKeeper.setAlignmentX(Component.CENTER_ALIGNMENT);
		lblKeeper.setHorizontalAlignment(SwingConstants.CENTER);
		keeperBox.add(lblKeeper);
		
		verticalStrut = Box.createVerticalStrut(10);
		keeperBox.add(verticalStrut);
		
		scoreBoard = Box.createVerticalBox();
		scoreBoard.setForeground(Color.BLACK);
		scoreBoard.setBackground(Color.BLACK);
		keeperBox.add(scoreBoard);
		
		verticalStrut_5 = Box.createVerticalStrut(20);
		scoreBoard.add(verticalStrut_5);
		
		scoreBoard_teams = Box.createHorizontalBox();
		scoreBoard.add(scoreBoard_teams);
		
		lblWhite = new JLabel("WHITE");
		lblWhite.setForeground(Color.WHITE);
		scoreBoard_teams.add(lblWhite);
		lblWhite.setAlignmentX(Component.CENTER_ALIGNMENT);
		
		horizontalStrut = Box.createRigidArea(new Dimension(50, 20));
		scoreBoard_teams.add(horizontalStrut);
		
		lblBlack = new JLabel("BLACK");
		lblBlack.setForeground(Color.WHITE);
		scoreBoard_teams.add(lblBlack);
		
		verticalStrut_1 = Box.createVerticalStrut(10);
		scoreBoard.add(verticalStrut_1);
		
		scoreBox = Box.createHorizontalBox();
		scoreBoard.add(scoreBox);
		
		lblWhiteScore = new JLabel("0");
		lblWhiteScore.setForeground(Color.YELLOW);
		lblWhiteScore.setFont(new Font("Digital Dismay", Font.PLAIN, 40));
		scoreBox.add(lblWhiteScore);
		
		rigAreaScore = Box.createRigidArea(new Dimension(60, 30));
		scoreBox.add(rigAreaScore);
		
		lblBlackScore = new JLabel("0");
		lblBlackScore.setForeground(Color.YELLOW);
		lblBlackScore.setFont(new Font("Digital Dismay", Font.PLAIN, 40));
		scoreBox.add(lblBlackScore);
		
		verticalStrut_6 = Box.createVerticalStrut(20);
		scoreBoard.add(verticalStrut_6);
		
		keeperLog = new JTextArea();
		keeperLog.setFont(new Font("Menlo", Font.PLAIN, 13));
		keeperLog.setEditable(false);
		keeperScrollPane = new JScrollPane(keeperLog);
		keeperBox.add(keeperScrollPane);
		
		playersBox = Box.createVerticalBox();
		horizontalBox.add(playersBox);
		
		verticalStrut_3 = Box.createVerticalStrut(10);
		playersBox.add(verticalStrut_3);
		
		lblPlayers = new JLabel("Players Agents");
		lblPlayers.setHorizontalAlignment(SwingConstants.CENTER);
		lblPlayers.setAlignmentX(0.5f);
		playersBox.add(lblPlayers);
		
		verticalStrut_4 = Box.createVerticalStrut(10);
		playersBox.add(verticalStrut_4);
		
		playersLog = new JTextArea();
		playersLog.setFont(new Font("Menlo", Font.PLAIN, 13));
		playersLog.setEditable(false);
		playersScrollPane = new JScrollPane(playersLog);
		playersBox.add(playersScrollPane);
		
	}
	
	synchronized void keeperLog(String from, String msg){
		String timeStamp = new SimpleDateFormat("HH:mm:ss.SS").format(new Date());
		keeperLog.append("[" + timeStamp + "] [" + from + "] "+ msg + "\n");
		keeperLog.update(keeperLog.getGraphics());
		keeperLog.setCaretPosition(keeperLog.getDocument().getLength());
		
	}
	
	synchronized void playerLog(String from, String msg){
		String timeStamp = new SimpleDateFormat("HH:mm:ss.SS").format(new Date());
		playersLog.append("[" + timeStamp + "] [" + from + "] "+ msg + "\n");
		playersLog.update(playersLog.getGraphics());
		playersLog.setCaretPosition(playersLog.getDocument().getLength());
		
	}
}
