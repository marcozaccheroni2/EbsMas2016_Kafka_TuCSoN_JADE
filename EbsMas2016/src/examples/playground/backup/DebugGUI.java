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

public class DebugGUI extends JFrame {

	private JPanel contentPane;
	private JScrollPane scrollPane;
	private JTextArea log;

	/**
	 * Create the frame.
	 */
	public DebugGUI() {
		setTitle("The Basketball Playground Scenario");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setExtendedState(JFrame.MAXIMIZED_BOTH);
		setUndecorated(true);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.X_AXIS));
		
		log = new JTextArea();
		log.setFont(new Font("Menlo", Font.PLAIN, 13));
		log.setEditable(false);
		scrollPane = new JScrollPane(log);
		contentPane.add(scrollPane);
		
	}
	
	public void log(String from, String msg){
		String timeStamp = new SimpleDateFormat("HH:mm:ss").format(new Date());
		log.append("[" + timeStamp + "] [" + from + "] "+ msg + "\n");
		log.update(log.getGraphics());
		log.setCaretPosition(log.getDocument().getLength());
		
	}
}
