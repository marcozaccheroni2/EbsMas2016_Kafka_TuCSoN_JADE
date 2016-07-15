package examples.playground;

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
import java.util.ArrayList;
import java.util.Date;

import javax.swing.BoxLayout;
import javax.swing.Box;
import javax.swing.JTextPane;
import java.awt.Component;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;

import java.awt.Color;
import javax.swing.JScrollPane;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.Dimension;
import javax.swing.JTable;
import javax.swing.UIManager;

public class PlaygroundGUI extends JFrame {

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
	private Component verticalStrut_2;
	private Component verticalStrut_3;
	private Component verticalStrut_4;
	private Component verticalStrut_5;
	private Component verticalStrut_6;
	private JTextArea txtPlayers;
	
	private ArrayList<String[]> players;
	private Box playersListBox;
	private Component horizontalStrut_1;

	/**
	 * Create the frame.
	 */
	public PlaygroundGUI() {
		
		players = new ArrayList<>();
		
		setTitle("The Basketball Playground Scenario");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(1280,800);
		setUndecorated(false);
		
		Rectangle frame_size = this.getBounds();
		
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.X_AXIS));
		
		horizontalBox = Box.createHorizontalBox();
		contentPane.add(horizontalBox);
		
		keeperBox = Box.createVerticalBox();
		keeperBox.setMinimumSize(new Dimension(640, 800));
		keeperBox.setMaximumSize(new Dimension(640, 800));
		keeperBox.setPreferredSize(new Dimension(640, 800));
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
		scoreBoard.setMinimumSize(new Dimension(640, 130));
		scoreBoard.setMaximumSize(new Dimension(640, 130));
		scoreBoard.setPreferredSize(new Dimension(640, 130));
		scoreBoard.setAlignmentX(Component.CENTER_ALIGNMENT);
		scoreBoard.setOpaque(true);
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
		lblWhiteScore.setFont(new Font("Digital Dismay", Font.PLAIN, 50));
		scoreBox.add(lblWhiteScore);
		
		rigAreaScore = Box.createRigidArea(new Dimension(60, 30));
		scoreBox.add(rigAreaScore);
		
		lblBlackScore = new JLabel("0");
		lblBlackScore.setForeground(Color.YELLOW);
		lblBlackScore.setFont(new Font("Digital Dismay", Font.PLAIN, 50));
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
		
		DefaultTableModel dtm = new DefaultTableModel(0, 4);
		String columnNames[] = { "Name", "Team","Ball", "Mood" };
		dtm.setColumnIdentifiers(columnNames);
		
		playersListBox = Box.createHorizontalBox();
		playersListBox.setMinimumSize(new Dimension(500, 130));
		playersListBox.setMaximumSize(new Dimension(500, 130));
		playersListBox.setPreferredSize(new Dimension(500, 130));
		playersBox.add(playersListBox);
		
		horizontalStrut_1 = Box.createHorizontalStrut(120);
		playersListBox.add(horizontalStrut_1);
		
		txtPlayers = new JTextArea();
		txtPlayers.setTabSize(10);
		txtPlayers.setColumns(50);
		txtPlayers.setFont(new Font("Menlo", Font.PLAIN, 16));
		playersListBox.add(txtPlayers);
		txtPlayers.setEditable(false);
		txtPlayers.setBackground(UIManager.getColor("Panel.background"));
		
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
	
	synchronized void updateScore(int white, int black){
		lblWhiteScore.setText("" + white);
		lblWhiteScore.update(lblWhiteScore.getGraphics());
		
		lblBlackScore.setText("" + black);
		lblBlackScore.update(lblBlackScore.getGraphics());
	}
	
	synchronized void playerLog(String from, String msg){
		String timeStamp = new SimpleDateFormat("HH:mm:ss.SSS").format(new Date());
		playersLog.append("[" + timeStamp + "] [" + from + "] "+ msg + "\n");
		playersLog.update(playersLog.getGraphics());
		playersLog.setCaretPosition(playersLog.getDocument().getLength());
		
	}
	
	synchronized void editPlayer(String name, String team, String mood) {
		boolean found = false;
		int i;
		for (i = 0; i < players.size(); i++) {
			if (players.get(i)[0].equals(name)){
				found = true;
				break;
			}
		}
		
		if (found){
			players.remove(i);
			players.add(i, new String[]{name, team, mood});
		} else {
			players.add(new String[]{name, team, mood});
		}
		
		txtPlayers.setText("NAME\t TEAM\tMOOD\n\n");
		for (String[] player : players) {
			txtPlayers.append(player[0] + "\t" + player[1] + "\t" + player[2] + "\n");
		}
		txtPlayers.update(txtPlayers.getGraphics());
	}
}
