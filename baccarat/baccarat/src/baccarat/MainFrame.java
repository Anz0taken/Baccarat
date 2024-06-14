package baccarat;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;

import javax.swing.JFrame;

@SuppressWarnings("serial")
public class MainFrame extends JFrame{
	
	static int PIXEL_WIDTH;
	static int PIXEL_HEIGHT;
	
	public GamePanel 	gamePanel;
	public ChatFrame 	chatPanel;
	public LoginDialog 	loginDialog;
	
	Font customFont;
	
	MainFrame(int PIXEL_WIDTH_, int PIXEL_HEIGHT_)
	{
		PIXEL_WIDTH = PIXEL_WIDTH_;
		PIXEL_HEIGHT = PIXEL_HEIGHT_;
		
	    try
	    {
	    	customFont = Font.createFont(Font.TRUETYPE_FONT, new File("C:\\Users\\lucag\\eclipse-workspace\\baccarat\\src\\fonts\\EBGaramond12-Regular.ttf")).deriveFont(40f);
			GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			ge.registerFont(customFont);
	    }
	    catch(FontFormatException e)
	    {
	    	System.out.println("Error font loading...");
			e.printStackTrace();
		}
	    catch(IOException e)
	    {
	    	System.out.println("Error font loading...");
			e.printStackTrace();
		}
		
		gamePanel = new GamePanel(PIXEL_WIDTH, PIXEL_HEIGHT, customFont);
		chatPanel = new ChatFrame(PIXEL_WIDTH, PIXEL_HEIGHT, customFont);
		
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setTitle("Baccarat");
		this.setLayout(null);
		this.setSize(PIXEL_WIDTH, PIXEL_HEIGHT);
		this.setVisible(true);
		this.setResizable(false);
		
		this.add(gamePanel);
		this.add(chatPanel);
		
		loginDialog = new LoginDialog(this);
	}
	
	public void disposeLoginDialog()
	{
		loginDialog.dispose();
	}
	
	public void generateGeneralDialog(String dialogName, String message, String buttonMessage)
	{
		new GeneralDialog(this, dialogName, message, buttonMessage);
	}
}
