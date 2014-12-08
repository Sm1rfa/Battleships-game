package edu.kea.networks.battleships.view;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;
import javax.swing.border.TitledBorder;

//import org.jdesktop.xswingx.PromptSupport;

/**
 * User Interface class extending the functionality of JFrame and representing
 * the Battleship game. The User Interface has a board showing the progress of
 * the game along with a chat-window showing system- and chat-messages.
 * <p>
 * The class uses the 3rd party library Prompt for showing placeholders inside
 * the JTextFields...
 * </p>
 * 
 * @author mfaarkrog
 * 
 */

/**
 *  The previous user interface is changed in the source a little bit
 *  <br /> Some methods for accessing the server are added
 * @author Stoyan Bonchev
 *
 */
public class GameUI {

	private Canvas playingBoard;
	private JTextField xField;
	private JTextField yField;
	private JButton shootButton;
	// myBoard shows the player's ship (alive/wrecked)
	private int[][] myBoard = new int[10][10];
	// enemyBoard shows where the player has been shooting (hit/misses)
	private int[][] enemyBoard = new int[10][10];
	BufferedReader in;
	PrintWriter out;
	JFrame frame;
	JTextField chatMessage;
	JTextArea chatArea;

	public GameUI() {
		buildUI();
	}

	private void buildUI() {
		frame = new JFrame();
		frame.setTitle("Battleship - The Game");
		frame.setLayout(new BorderLayout());
		frame.setSize(700, 425);
		frame.setLocationRelativeTo(null); // Center of screen
		frame.setResizable(false);

		createBoard();
		createChat();

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}

	public void setMyBoard(int[][] myBoard) {
		this.myBoard = myBoard;
	}

	public void setEnemyBoard(int[][] enemyBoard) {
		this.enemyBoard = enemyBoard;
	}

	private void createChat() {
		JPanel chatPanel = new JPanel(new BorderLayout());
		chatPanel.setBackground(UIColors.LIGHTBLUE);
		chatPanel.setBorder(new TitledBorder(null, "Chat", TitledBorder.CENTER,
				TitledBorder.TOP));

		chatArea = new JTextArea(20, 30);
		chatArea.setFont(new Font(Font.DIALOG, Font.PLAIN, 12));
		chatArea.setWrapStyleWord(true);
		chatArea.setLineWrap(true);
		chatArea.setEditable(false);

		JPanel actionPanel = new JPanel();
		actionPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
		actionPanel.setBackground(UIColors.DARKBLUE);
		chatMessage = new JTextField(16);
//		PromptSupport.setPrompt("Enter message here...", chatMessage);

		actionPanel.add(chatMessage);

		chatPanel.add(new JScrollPane(chatArea), BorderLayout.CENTER);
		chatPanel.add(actionPanel, BorderLayout.SOUTH);

		frame.add(chatPanel, BorderLayout.EAST);

		chatMessage.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				out.println(getMessage());
				chatMessage.setText("");
			}
		});
	}

	private void createBoard() {
		JPanel boardPanel = new JPanel(new BorderLayout());
		boardPanel.setBackground(UIColors.LIGHTBLUE);
		boardPanel.setBorder(new TitledBorder(null, "Game Board",
				TitledBorder.CENTER, TitledBorder.TOP));

		// Graphics board
		playingBoard = new Canvas();

		// Action panel (For shooting)
		JPanel actionPanel = new JPanel();
		actionPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
		actionPanel.setBackground(UIColors.DARKBLUE);

		JLabel xLabel = new JLabel("x: ", JLabel.CENTER);
		xLabel.setForeground(Color.WHITE);
		xField = new JTextField(4);
//		PromptSupport.setPrompt("1-10", xField);

		JLabel yLabel = new JLabel("y: ", JLabel.CENTER);
		yLabel.setForeground(Color.WHITE);
		yField = new JTextField(4);
//		PromptSupport.setPrompt("1-10", yField);

		shootButton = new JButton("Shoot!");

		JLabel waitLabel = new JLabel("Please Wait...", JLabel.CENTER);
		waitLabel.setForeground(Color.WHITE);

		// Add components to ActionPanel
		actionPanel.add(xLabel);
		actionPanel.add(xField);
		actionPanel.add(yLabel);
		actionPanel.add(yField);
		actionPanel.add(shootButton);
		actionPanel.add(waitLabel);

		boardPanel.add(playingBoard, BorderLayout.CENTER);
		boardPanel.add(actionPanel, BorderLayout.SOUTH);

		frame.add(boardPanel, BorderLayout.WEST);

		shootButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (e.getSource() == shootButton) {
					out.println("I am shooting at: " + "X " + getXPos() + " Y "
							+ getYPos());
					xField.setText("");
					yField.setText("");
				}
			}
		});
	}

	// @Override
	public void repaint() {
		playingBoard.repaint();
	}

	public void addChatMessage(String message) {
		chatArea.append(message + "\n\n");
		chatArea.setCaretPosition(chatArea.getDocument().getLength());
	}

	public String getMessage() {
		return chatMessage.getText();
	}

	public String getXPos() {
		return xField.getText();
	}

	public String getYPos() {
		return yField.getText();
	}

	/**
	 * 
	 * @return the server address
	 */
	private String getServerAddress() {
		return JOptionPane.showInputDialog(frame,
				"Enter IP Address of the Server:", "Welcome to the Chatter",
				JOptionPane.QUESTION_MESSAGE);
	}

	/**
	 * 
	 * @return the choosen nick name
	 */
	private String getName() {
		return JOptionPane.showInputDialog(frame, "Choose a nick name:",
				"Screen name selection", JOptionPane.PLAIN_MESSAGE);
	}

	/**
	 * The connection to the server
	 * 
	 * @throws IOException
	 */
	private void run() throws IOException {

		// Make connection and initialize streams
		String serverAddress = getServerAddress();
		Socket socket = new Socket(serverAddress, 9999);
		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		out = new PrintWriter(socket.getOutputStream(), true);

		while (true) {
			String line = in.readLine();
			if (line.startsWith("SUBMITNAME")) {
				out.println(getName());
			} else if (line.startsWith("NAMEACCEPTED")) {
				chatMessage.setEditable(true);
			} else if (line.startsWith("MESSAGE")) {
				chatArea.append(line.substring(8) + "\n");
			}
		}
	}

	public static void main(String[] args) throws Exception {
		GameUI client = new GameUI();
		client.run();
	}

	private class Canvas extends JComponent {

		private static final long serialVersionUID = 6914956209775435327L;

		@Override
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			drawGrid(g);
			drawXY(g);
			drawFatLines(g);
			drawMyBoard(g);
			drawEnemyBoard(g);
		}

		// Draws the grid the game is played on
		public void drawGrid(Graphics g) {
			g.setColor(Color.WHITE);
			g.fillRect(0, 0, 330, 330);
			g.setColor(Color.black);

			for (int i = 0; i < 11; i++)
				for (int j = 0; j < 11; j++)
					g.drawRect(j * 30, i * 30, 30, 30);
		}

		// Draws 1-10 on the x and y axis
		public void drawXY(Graphics g) {

			g.drawString("x", 18, 12);
			g.drawString("y", 6, 24);
			g.drawLine(0, 0, 30, 30);

			for (int j = 1; j < 11; j++) {
				g.drawString(j + "", j * 30 + 11, 20);
				g.drawString(j + "", 10, j * 30 + 20);
			}
		}

		// Outlines the playing board
		public void drawFatLines(Graphics g) {
			Graphics2D g2 = (Graphics2D) g;
			g2.setStroke(new BasicStroke(4));
			g2.drawLine(30, 0, 30, 330);
			g2.drawLine(0, 30, 330, 30);
			g2.drawRect(0, 0, 330, 330);
		}

		// Draws the MyBoard information on the board (where are my ships and
		// have
		// they been hit?)
		public void drawMyBoard(Graphics g) {
			for (int i = 0; i < 10; i++)
				for (int j = 0; j < 10; j++)
					if (myBoard[i][j] == 1) { // Alive Ship-part
						g.setColor(Color.LIGHT_GRAY);
						g.fillRect(j * 30 + 36, i * 30 + 36, 19, 19);
						g.setColor(Color.BLACK);
						g.drawRect(j * 30 + 36, i * 30 + 36, 19, 19);
					} else if (myBoard[i][j] == 2) { // Wrecked Ship-part
						g.setColor(Color.WHITE);
						g.fillRect(j * 30 + 36, i * 30 + 36, 19, 19);
						g.setColor(Color.BLACK);
						g.drawRect(j * 30 + 36, i * 30 + 36, 19, 19);
					}
		}

		// Draws the EnemyBoard information on the board (where have I been
		// shooting
		// and have I been hitting the enemy?)
		public void drawEnemyBoard(Graphics g) {
			Graphics2D g2 = (Graphics2D) g;
			g2.setStroke(new BasicStroke(3));
			for (int i = 0; i < 10; i++)
				for (int j = 0; j < 10; j++) {

					int x = j * 30 + 36;
					int y = i * 30 + 36;

					if (enemyBoard[i][j] == 1) { // Miss
						g.setColor(Color.BLUE);

						g2.drawLine(x, y, x + 20, y + 20);
						g2.drawLine(x, y + 20, x + 20, y);
					} else if (enemyBoard[i][j] == 2) { // Hit
						g.setColor(Color.RED);

						g2.drawLine(x, y, x + 20, y + 20);
						g2.drawLine(x, y + 20, x + 20, y);
					}
				}
		}

	}

}
