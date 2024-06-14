package baccarat;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Transparency;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;

@SuppressWarnings("serial")
public class GamePanel extends JPanel{
	
	private static int PIXEL_WIDTH;
	private static int PIXEL_HEIGHT;
	
	private static final int FIRST_CARD_INDEX 	= 0;
	private static final int SECOND_CARD_INDEX 	= 1;
	private static final int THIRD_CARD_INDEX 	= 2;
	private static final int FOURTH_CARD_INDEX 	= 3;
	private static final int FIFTH_CARD_INDEX 	= 4;
	private static final int SIXTH_CARD_INDEX 	= 5;
	
	private static final int PLAYER_WRITE 		= 0;
	private static final int BANKER_WRITE 		= 1;
	private static final int GAME_RESULT_WRITE 	= 2;
	
	private static final int BANKER_WINS_WRITE 	= 2;
	private static final int PLAYER_WINS_WRITE 	= 3;
	private static final int TIE_GAME_WRITE 	= 4;
	
	private BufferedImage[] cards;
	private BufferedImage[] writes;
	
	private JLabel playersName;
	
	private JLabel countDownWrite;
	private JLabel bankerScores;
	private JLabel playerScores;
		
	private JLabel yourBetPharse;
	private JLabel yourBet;
	
	private JLabel betPhrase;
	
	private JLabel yourCreditPharse;
	private JLabel yourCredit;
	
	private Font customFont;
	
	private JTextArea textArea;
	private JScrollPane scroll;
	
	private JButton bankerButtonBet;
	private JButton tieButtonBet;
	private JButton playerButtonBet;
	private JButton placeBet;
	private JButton cancelBet;
	
	private SpinnerModel model;  
	private JSpinner spinner;
	
	static private byte BANKER_BET 	= 2;
	static private byte PLAYER_BET  = 3;
	static private byte TIE_BET  	= 4;
	
	private volatile byte    whoBet;
	private volatile double  amountBet;
	private volatile boolean betReady;
	private volatile boolean toCancelBet;
	
	private int[][] cardPositioning;	/* Rows = card number, column = x and y positioning (for cards) */
	private int[][] writesPositioning;	/* Rows = card number, column = x and y positioning (for banker and player writes) */ 
	
	private static final byte GAME_STATUS 	= 0;
	private static final byte FIRST_CARD 	= 1;
	private static final byte SECOND_CARD 	= 2;
	private static final byte THIRD_CARD 	= 3;
	private static final byte FOURTH_CARD 	= 4;
	private static final byte FIFTH_CARD 	= 5;
	private static final byte SIXTH_CARD 	= 6;
	private static final byte BANKER_SCORE 	= 7;
	private static final byte PLAYER_SCORE 	= 8;
	private static final byte AMOUNT_BET 	= 9;
	private static final byte TARGET_BET 	= 10;
	private static final byte BET_COUNTDOWN = 11;
	/* Array structure :
	 * 1° byte 	[0] 	=> Game status : 1 in game, 2 banker wins, 3 player wins, 4 tie game, 5 bets open
	 * 2° byte 	[1] 	=> First banker card
	 * 3° byte 	[2] 	=> First player card
	 * 4° byte 	[3] 	=> Second banker card
	 * 5° byte 	[4] 	=> Second player card
	 * 6° byte 	[5] 	=> Banker extra card (eventually)
	 * 7° byte 	[6] 	=> Player extra card (eventually)
	 * 8° byte 	[7] 	=> Banker score
	 * 9° byte 	[8] 	=> Player score
	 * 10° byte [9] 	=> Amount bet
	 * 11° byte [10] 	=> Target bet
	 * 
	 * - All data are offset by +1 -
	 */
	private int[] gameInfo;			/* Contains info about the game  */
	
	private final byte INGAME = 1, BANKERWINS = 2, PLAYERWINS = 3, TIEGAME = 4, BETSOPEN = 5, EMPTY = 0;
	
	GamePanel(int PIXEL_WIDTH_, int PIXEL_HEIGHT_, Font customFont_)
	{
		this.setLayout(null);
		
		PIXEL_WIDTH = PIXEL_WIDTH_;
		PIXEL_HEIGHT = PIXEL_HEIGHT_;
		
		customFont = customFont_;
		
		cards = new BufferedImage[52];
		writes = new BufferedImage[5];
		
		gameInfo = null;
		
		toCancelBet = false;
		
		betReady = false;
		whoBet = 0;
		
		cardPositioning = new int[6][2];
		cardPositioning[FIRST_CARD_INDEX][0] = 615;
		cardPositioning[FIRST_CARD_INDEX][1] = 150;
		
		cardPositioning[SECOND_CARD_INDEX][0] = 175;
		cardPositioning[SECOND_CARD_INDEX][1] = 150;
		
		cardPositioning[THIRD_CARD_INDEX][0] = 550;
		cardPositioning[THIRD_CARD_INDEX][1] = 150;
		
		cardPositioning[FOURTH_CARD_INDEX][0] = 110;
		cardPositioning[FOURTH_CARD_INDEX][1] = 150;
		
		cardPositioning[FIFTH_CARD_INDEX][0] = 680;
		cardPositioning[FIFTH_CARD_INDEX][1] = 150;
		
		cardPositioning[SIXTH_CARD_INDEX][0] = 19;
		cardPositioning[SIXTH_CARD_INDEX][1] = 150;
		
		writesPositioning = new int[3][2];

		writesPositioning[BANKER_WRITE][0] = 50;
		writesPositioning[BANKER_WRITE][1] = 230;
		
		writesPositioning[PLAYER_WRITE][0] = 450;
		writesPositioning[PLAYER_WRITE][1] = 230;
		
		writesPositioning[GAME_RESULT_WRITE][0] = 0;
		writesPositioning[GAME_RESULT_WRITE][1] = 50;
		
		textArea = new JTextArea();
		textArea.setLineWrap(true);
		textArea.setEditable(false);
		textArea.setWrapStyleWord(true);
		textArea.setFont(new Font(customFont.getName(), Font.PLAIN, 18));
		textArea.append("[INFO] Welcome to baccarat versions 1.0\n[INFO] Here you will recive all info about the game, server status and more about the game itselft!\n");

		scroll = new JScrollPane(textArea);
		scroll.setBounds(2, PIXEL_HEIGHT/30*13 + 1, 400, PIXEL_HEIGHT/20*13 - PIXEL_HEIGHT/30*13 - 4);
		this.add(scroll);
		
		countDownWrite = new JLabel();
		countDownWrite.setForeground(Color.white);
		countDownWrite.setFont(new Font(customFont.getName(), Font.PLAIN, 50));
		this.add(countDownWrite);
		
		playersName = new JLabel("-");
		playersName.setForeground(Color.white);
		playersName.setFont(new Font(customFont.getName(), Font.PLAIN, 25));
		playersName.setBounds(10, -10, PIXEL_WIDTH, 60);
		this.add(playersName);
		
		bankerScores = new JLabel();
		bankerScores.setForeground(Color.white);
		bankerScores.setFont(new Font(customFont.getName(), Font.PLAIN, 50));
		bankerScores.setBounds(PIXEL_WIDTH/2 - 150, 150, 60, 60);
		this.add(bankerScores);
		
		playerScores = new JLabel();
		playerScores.setForeground(Color.white);
		playerScores.setFont(new Font(customFont.getName(), Font.PLAIN, 50));
		playerScores.setBounds(PIXEL_WIDTH/2 + 108, 150, 60, 60);
		this.add(playerScores);
		
		bankerButtonBet = new JButton("BANKER");
		bankerButtonBet.setFont(new Font(customFont.getName(), Font.PLAIN, 17));
		bankerButtonBet.setBounds(PIXEL_WIDTH/2, 420 + 2, PIXEL_WIDTH/6, 30);
		bankerButtonBet.addActionListener(new ActionListener(){
		    @Override
		    public void actionPerformed(ActionEvent e)
		    {
		    	whoBet = BANKER_BET;
		    	textArea.append("[INFO] Your bet is for BANKER.\n");
		    }
		});
		
		this.add(bankerButtonBet);
		
		tieButtonBet = new JButton("TIE");
		tieButtonBet.setFont(new Font(customFont.getName(), Font.PLAIN, 17));
		tieButtonBet.setBounds(PIXEL_WIDTH/2 + PIXEL_WIDTH/6 - 1, 420 + 2, PIXEL_WIDTH/6 - 15, 30);
		tieButtonBet.addActionListener(new ActionListener(){
		    @Override
		    public void actionPerformed(ActionEvent e)
		    {
		    	whoBet = TIE_BET;
		    	textArea.append("[INFO] Your bet is for TIE.\n");
		    }
		});
		this.add(tieButtonBet);
		
		playerButtonBet = new JButton("PLAYER");
		playerButtonBet.setFont(new Font(customFont.getName(), Font.PLAIN, 17));
		playerButtonBet.setBounds(PIXEL_WIDTH/2 + PIXEL_WIDTH/3 - 17, 420 + 2, PIXEL_WIDTH/6, 30);
		playerButtonBet.addActionListener(new ActionListener(){
		    @Override
		    public void actionPerformed(ActionEvent e)
		    {
		    	whoBet = PLAYER_BET;
		    	textArea.append("[INFO] Your bet is for PLAYER.\n");
		    }
		});
		this.add(playerButtonBet);
		
		placeBet = new JButton("CONFIRM");
		placeBet.setFont(new Font(customFont.getName(), Font.PLAIN, 17));
		placeBet.setBounds(PIXEL_WIDTH/2, 390 + 2, PIXEL_WIDTH/4 - 8, 30);
		placeBet.addActionListener(new ActionListener(){
		    @Override
		    public void actionPerformed(ActionEvent e)
		    {
		    	amountBet = (double)spinner.getValue();
		    	betReady = true;
		    }
		});
		this.add(placeBet);
		
		cancelBet = new JButton("CANCEL");
		cancelBet.setFont(new Font(customFont.getName(), Font.PLAIN, 17));
		cancelBet.setBounds(PIXEL_WIDTH/2 + PIXEL_WIDTH/4 - 8, 390 + 2, PIXEL_WIDTH/4 - 10, 30);
		cancelBet.addActionListener(new ActionListener(){
		    @Override
		    public void actionPerformed(ActionEvent e)
		    {
		    	toCancelBet = true;
		    }
		});
		this.add(cancelBet);
		
		model = new SpinnerNumberModel(0.2, 0.2, 2000, 0.01);     
		spinner = new JSpinner(model);
		spinner.setBounds(PIXEL_WIDTH/2 + PIXEL_WIDTH/4 - 8, 360 + 2, PIXEL_WIDTH/4 - 10, 30);
		this.add(spinner);
		
		betPhrase = new JLabel("BET AMOUNT : ");
		betPhrase.setForeground(Color.white);
		betPhrase.setFont(new Font(customFont.getName(), Font.PLAIN, 24));
		betPhrase.setBounds(PIXEL_WIDTH/2, 364, PIXEL_WIDTH/4 - 10, 30);
		this.add(betPhrase);
		
		yourCreditPharse = new JLabel("YOUR CREDIT : ");
		yourCreditPharse.setForeground(Color.white);
		yourCreditPharse.setFont(new Font(customFont.getName(), Font.PLAIN, 24));
		yourCreditPharse.setBounds(PIXEL_WIDTH/2, 334, PIXEL_WIDTH/4 - 10, 30);
		this.add(yourCreditPharse);
		
		
		yourCredit = new JLabel("0.0€");
		yourCredit.setForeground(Color.white);
		yourCredit.setFont(new Font(customFont.getName(), Font.PLAIN, 24));
		yourCredit.setBounds(PIXEL_WIDTH/2 + 310, 334, PIXEL_WIDTH/4 - 10, 30);
		this.add(yourCredit);
		
		yourBetPharse = new JLabel("YOUR BET : ");
		yourBetPharse.setForeground(Color.white);
		yourBetPharse.setFont(new Font(customFont.getName(), Font.PLAIN, 24));
		yourBetPharse.setBounds(PIXEL_WIDTH/2, 304, PIXEL_WIDTH/4 - 10, 30);
		this.add(yourBetPharse);
		
		yourBet = new JLabel("NONE");
		yourBet.setForeground(Color.white);
		yourBet.setFont(new Font(customFont.getName(), Font.PLAIN, 24));
		yourBet.setBounds(PIXEL_WIDTH/2 + 130, 304, PIXEL_WIDTH/4 - 10, 30);
		this.add(yourBet);
		
		/*
		 * Loading banker and player writes
		 */
		try
		{
			writes[BANKER_WRITE]  		= ImageIO.read(new File("C:\\Users\\lucag\\eclipse-workspace\\baccarat\\src\\images\\banker_write.png"));
			writes[PLAYER_WRITE]  		= ImageIO.read(new File("C:\\Users\\lucag\\eclipse-workspace\\baccarat\\src\\images\\player_write.png"));
			writes[BANKER_WINS_WRITE]  	= ImageIO.read(new File("C:\\Users\\lucag\\eclipse-workspace\\baccarat\\src\\images\\banker_wins.png"));
			writes[PLAYER_WINS_WRITE]  	= ImageIO.read(new File("C:\\Users\\lucag\\eclipse-workspace\\baccarat\\src\\images\\player_wins.png"));
			writes[TIE_GAME_WRITE]  	= ImageIO.read(new File("C:\\Users\\lucag\\eclipse-workspace\\baccarat\\src\\images\\tie_wins.png"));
		}
		catch(IOException ex)
		{
		     System.out.println("faild loading images...");
		}
		
		/*
		 * Loading of all card images
		 */
		for(int i = 1; i <= 13; i++)
		{
			try
			{
				cards[i - 1]  = ImageIO.read(new File("C:\\Users\\lucag\\eclipse-workspace\\baccarat\\src\\cards\\"+i+'C'+".png"));
				cards[i + 12] = ImageIO.read(new File("C:\\Users\\lucag\\eclipse-workspace\\baccarat\\src\\cards\\"+i+'D'+".png"));
				cards[i + 25] = ImageIO.read(new File("C:\\Users\\lucag\\eclipse-workspace\\baccarat\\src\\cards\\"+i+'H'+".png"));
				cards[i + 38] = ImageIO.read(new File("C:\\Users\\lucag\\eclipse-workspace\\baccarat\\src\\cards\\"+i+'S'+".png"));
			}
			catch(IOException ex)
			{
			     System.out.println("faild loading images...");
			}
			
		}
		
		this.setBackground(new Color(0, 128, 43));
		this.setBounds(0, 0, PIXEL_WIDTH, PIXEL_HEIGHT/20*13);
	}
	
	public void paintComponent(Graphics g)
	{	
		super.paintComponent(g);
		
		g.setColor(Color.white);
		g.drawLine(0, PIXEL_HEIGHT/20*13 - 1, PIXEL_WIDTH, PIXEL_HEIGHT/20*13 - 1);
		g.drawLine(0, PIXEL_HEIGHT/30*13 - 1, PIXEL_WIDTH, PIXEL_HEIGHT/30*13 - 1);
		
		g.drawImage(writes[0], writesPositioning[0][0], writesPositioning[0][1], 300, 60, this);
		g.drawImage(writes[1], writesPositioning[1][0], writesPositioning[1][1], 300, 60, this);
		
		if(gameInfo != null)
		{	
			if(gameInfo[GAME_STATUS] != 0)
			{
				if(gameInfo[FIRST_CARD] != EMPTY)
				{
					g.drawImage(cards[gameInfo[FIRST_CARD] - 1], cardPositioning[FIRST_CARD_INDEX][0], cardPositioning[FIRST_CARD_INDEX][1], 60, 86, this);
					
					if(gameInfo[SECOND_CARD] != EMPTY)
					{
						g.drawImage(cards[gameInfo[SECOND_CARD] - 1], cardPositioning[SECOND_CARD_INDEX][0], cardPositioning[SECOND_CARD_INDEX][1], 60, 86, this);
						
						if(gameInfo[THIRD_CARD] != EMPTY)
						{
							g.drawImage(cards[ gameInfo[THIRD_CARD] - 1], cardPositioning[THIRD_CARD_INDEX][0], cardPositioning[THIRD_CARD_INDEX][1], 60, 86, this);
						
							if(gameInfo[FOURTH_CARD] != EMPTY)
							{
								g.drawImage(cards[ gameInfo[FOURTH_CARD] - 1], cardPositioning[FOURTH_CARD_INDEX][0], cardPositioning[FOURTH_CARD_INDEX][1], 60, 86, this);

								if(gameInfo[FIFTH_CARD] != EMPTY)
									g.drawImage(RotateImage(cards[gameInfo[FIFTH_CARD] - 1], -3.14/2), cardPositioning[FIFTH_CARD_INDEX][0], cardPositioning[FIFTH_CARD_INDEX][1], 86, 60, this);
								
								if(gameInfo[SIXTH_CARD] != EMPTY)
									g.drawImage(RotateImage(cards[ gameInfo[SIXTH_CARD] - 1], 3.14/2), cardPositioning[SIXTH_CARD_INDEX][0], cardPositioning[SIXTH_CARD_INDEX][1], 86, 60, this);
							}
						}
					}
				}
				
				if(gameInfo[GAME_STATUS] > INGAME && gameInfo[GAME_STATUS] < BETSOPEN)	//if the game is over
				{
					g.drawImage(writes[ gameInfo[GAME_STATUS] ], writesPositioning[GAME_RESULT_WRITE][0], writesPositioning[GAME_RESULT_WRITE][1], 805, 50, this);
				}
				else if(gameInfo[GAME_STATUS] == BETSOPEN)
				{
					drawCountDown(g, gameInfo[BET_COUNTDOWN]);
				}
				
				bankerScores.setText(Integer.toString(gameInfo[BANKER_SCORE]));
				playerScores.setText(Integer.toString(gameInfo[PLAYER_SCORE]));
			}
		}
		
		this.repaint();
	}
	
	public void drawCountDown(Graphics g, int Seconds)
	{
		Graphics2D g2 = (Graphics2D) g;
		
		if(Seconds>0)
			countDownWrite.setText(Integer.toString(Seconds));
		else
			countDownWrite.setText("");
		
		countDownWrite.setBounds(PIXEL_WIDTH/2 - 10 - (int)(Seconds/10)*10, PIXEL_HEIGHT/20 - 3, 60, 60);
		
		double MultiplayerGreen;
		
		if(Seconds > 15)
			MultiplayerGreen = 1;
		else
			MultiplayerGreen = (double)1/15*Seconds;
		

		g2.setColor(new Color((int)(255*(1-MultiplayerGreen)), (int)(255*MultiplayerGreen), 0));
		g2.setStroke(new BasicStroke(10));
		g2.drawArc(PIXEL_WIDTH/2 - 32, PIXEL_HEIGHT/20, 60, 60, 90, 360*Seconds/15);
		g2.setStroke(new BasicStroke(1));
	}
	
	public void setGameInfos(int GameInfo_[])
	{
		gameInfo = new int[GameInfo_.length];
		
		for(int i = 0; i < GameInfo_.length; i++)
			gameInfo[i] = GameInfo_[i];
	}
	
	public Boolean isBetReady()
	{
		Boolean result = false;
		
		if(betReady)
			betReady = !(result = true);
		
		return result;
	}
	
	public Boolean isCancelBet()
	{
		Boolean result = false;
		
		if(toCancelBet) toCancelBet = !(result = true);
		
		return result;
	}
	
	public void updateBetStatus(int success, String message, Double moneyRemaining)
	{
		if(success == 1)		//if the bet was accepted
		{
			String Names[] = {"Banker","Player","Tie"};
			
			yourBet.setText(Names[whoBet - 2]+" : "+amountBet+"€");
			yourCredit.setText(moneyRemaining+"€");
		}
		else if(success == 2)	//when got bet response
		{
			yourBet.setText("NONE");
			yourCredit.setText(moneyRemaining+"€");
		}
		
		textArea.append(message);
	}
	
	void setUsername(String username)
	{
		playersName.setText(username);
	}
	
	public void updateMoney(Double money)
	{
		yourCredit.setText(money+"€");
	}
	
	public byte getWhoBet()
	{
		return whoBet;
	}
	
	public double getAmountBet()
	{
		return amountBet;
	}
	
	private static GraphicsConfiguration getDefaultConfiguration()
	{
	    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
	    GraphicsDevice gd = ge.getDefaultScreenDevice();
	    return gd.getDefaultConfiguration();
	}
	
	public static BufferedImage RotateImage(BufferedImage image, double angle)
	{
	    double sin = Math.abs(Math.sin(angle)), cos = Math.abs(Math.cos(angle));
	    int w = image.getWidth(), h = image.getHeight();
	    int neww = (int)Math.floor(w*cos+h*sin), newh = (int) Math.floor(h * cos + w * sin);
	    
	    GraphicsConfiguration gc = getDefaultConfiguration();
	    BufferedImage result = gc.createCompatibleImage(neww, newh, Transparency.TRANSLUCENT);
	    Graphics2D g = result.createGraphics();
	    
	    g.translate((neww - w) / 2, (newh - h) / 2);
	    g.rotate(angle, w / 2, h / 2);
	    g.drawRenderedImage(image, null);
	    g.dispose();
	    
	    return result;
	}
}
