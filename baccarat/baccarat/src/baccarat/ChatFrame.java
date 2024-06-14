package baccarat;

import java.awt.Color;
import java.awt.Desktop.Action;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class ChatFrame extends JPanel{
	
	static int PIXEL_WIDTH;
	static int PIXEL_HEIGHT;
	
	private JTextArea chatTextArea;
	private JScrollPane scroll;
	
	private JButton sendMessage;
	
	private Font customFont;
	
	private JTextField enterChat;
    private AbstractAction action;
    
    private String messageToSend;
	
	ChatFrame(int PIXEL_WIDTH_, int PIXEL_HEIGHT_, Font customFont_)
	{
		this.setLayout(null);
		
		messageToSend = "";
		
		PIXEL_WIDTH = PIXEL_WIDTH_;
		PIXEL_HEIGHT = PIXEL_HEIGHT_;
		
		customFont = customFont_;
		
		this.setBackground(new Color(0, 128, 43));
		this.setBounds(0, PIXEL_HEIGHT/20*13, PIXEL_WIDTH, PIXEL_HEIGHT);

		chatTextArea = new JTextArea();
		chatTextArea.setLineWrap(true);
		chatTextArea.setEditable(false);
		chatTextArea.setWrapStyleWord(true);
		chatTextArea.setFont(new Font(customFont.getName(), Font.PLAIN, 18));
		chatTextArea.append("[INFO] Here is player chat!\n");
		
		scroll = new JScrollPane(chatTextArea);
		scroll.setBounds(2, 2, PIXEL_WIDTH - 20, 175);
		
		this.add(scroll);
		
		action = new AbstractAction(){
		    @Override
		    public void actionPerformed(ActionEvent e)
		    {
		    	messageToSend = enterChat.getText();
		    	enterChat.setText("");
		    }
		};
		
		enterChat = new JTextField("");
		enterChat.setBounds(2, 175, PIXEL_WIDTH - 120,30);
		enterChat.setFont(new Font(customFont.getName(), Font.PLAIN, 18));
		enterChat.addActionListener(action);
		
		this.add(enterChat);
		
		sendMessage = new JButton("SEND");
		sendMessage.setFont(new Font(customFont.getName(), Font.PLAIN, 17));
		sendMessage.setBounds(PIXEL_WIDTH - 120, 175, 102,30);
		
		sendMessage.addActionListener(new ActionListener(){
		    @Override
		    public void actionPerformed(ActionEvent e)
		    {
		    	messageToSend = enterChat.getText();
		    	enterChat.setText("");
		    }
		});
		
		this.add(sendMessage);
	}
	
	public void addMessageToChat(String msg)
	{
		chatTextArea.append(msg);
	}
	
	public String getMessageToSend()
	{
		return messageToSend;
	}
	
	public void resetMessageToSend()
	{
		messageToSend = "";
	}
}
