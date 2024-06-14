package baccarat;

import java.io.IOException;
import java.io.StringWriter;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Baccarat{
	static SocketChannel 	client;
    static MainFrame 		mainInterface;
    static volatile String	gameAction;
    static volatile boolean keepLogin;
    
    //to sent
	private static final int  LOGIN_ACTION 		 	= 2;
	private static final byte BET_ACTION 	  	 	= 5;
	private static final byte GIVE_BET_SUBACTION 	= 6;
	private static final byte CANCEL_BET_SUBACTION 	= 7;

	//to recive
    private static final byte DIALOG_RESPONSE 	 = 4;
    private static final byte GAME_INFO 		 = 1;
	private static final byte INFO_TEXT_RESPONSE = 5;
	private static final byte ACTION_GAME_RESULT = 6;
	
	//to sent and recive
	private static final byte CHAT_MESSAGE_ACTION = 8;
	
    
	public static void main(String[] args) throws UnknownHostException, IOException, NoSuchAlgorithmException
	{
		mainInterface = new MainFrame(808, 700);
	    
    	gameAction = "";
    	keepLogin = true;
		
		try
		{    
            client = SocketChannel.open(new InetSocketAddress("localhost", 2212));
            
            Thread reciverThread = new Thread(() -> {

        		System.out.println("Thread reciver started...\n");
        		
        		ByteBuffer buffer_in = ByteBuffer.allocate(1024);
                
        		while(true)
        		{
        			try {
        	        	buffer_in = ByteBuffer.allocate(1024);
        				client.read(buffer_in);
        			} catch (IOException e) {
        				// TODO Auto-generated catch block
        				e.printStackTrace();
        			}
        	        
        	        String data = new String(buffer_in.array());
        	        
        	        if(data.length() > 0)
        	        {
        	        	try
        	        	{
							JSONObject response = new JSONObject(data);
							
	        	        	if(response.getInt("action") == GAME_INFO)
	        	        	{
	        	        		JSONArray array = response.optJSONArray("game_info");
	        	        		
	        	        		int[] numbers = new int[array.length()];
	        	        		
	        	        		for (int i = 0; i < array.length(); ++i)
	        	        		{
	        	        		    numbers[i] = array.optInt(i);
	        	        		}
	        	        		
	        	        		mainInterface.gamePanel.setGameInfos(numbers);
	        	        	}
	        	        	else if(response.getInt("action") == DIALOG_RESPONSE)
	        	        	{
	        	        		mainInterface.generateGeneralDialog(response.getString("title"), response.getString("message"), "Ok");
	        	        		
	        	        		if(response.getInt("success") > 0 && response.getInt("subaction") == LOGIN_ACTION)
	        	        		{
	        	        			keepLogin = false;
	        	        			mainInterface.gamePanel.updateMoney(response.getDouble("money"));
	        	        			mainInterface.gamePanel.setUsername(response.getString("username"));
	        	        			mainInterface.disposeLoginDialog();
	        	        		}
	        	        	}
	        	        	else if(response.getInt("action") == INFO_TEXT_RESPONSE)
	        	        	{
	        	        		if(response.getInt("subaction") == BET_ACTION)
	        	        			mainInterface.gamePanel.updateBetStatus(response.getInt("success"), response.getString("message"), response.getDouble("moneyremaining"));
	        	        		else if(response.getInt("subaction") == ACTION_GAME_RESULT)
	        	        		{
	        	        			mainInterface.gamePanel.updateMoney(response.getDouble("totalmoney"));
	        	        			mainInterface.gamePanel.updateBetStatus(response.getInt("success"), response.getString("message"), response.getDouble("totalmoney"));
	        	        		}
	        	        	}
	        	        	else if(response.getInt("action") == CHAT_MESSAGE_ACTION)
	        	        	{
	        	        		mainInterface.chatPanel.addMessageToChat(response.getString("content"));
	        	        	}
	        	        	 	        	
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}   	
        	        }
        		}           	
            });
            
            reciverThread.start();

            Thread senderThread = new Thread(() ->
            {
            	System.out.println("Thread sender started...\n");            	
                ByteBuffer buffer_out;
                
            	do
                {
                	if(!gameAction.equals(""))
                	{         		
                		buffer_out = ByteBuffer.allocate(1024);
                    	buffer_out.put(gameAction.getBytes());
                    	buffer_out.flip();
                        
                        try
                        {
							client.write(buffer_out);
						}
                        catch (IOException e)
                        {
							e.printStackTrace();
						}
                        
                        gameAction = "";
                	}
                	
                	if(!mainInterface.chatPanel.getMessageToSend().equals(""))
                	{
                        JSONObject jsonObject = new JSONObject();
                        
                        try
                        {
                            jsonObject.put("action",    CHAT_MESSAGE_ACTION);
							jsonObject.put("message", 	mainInterface.chatPanel.getMessageToSend());
						}
                        catch (JSONException e)
                        {
							e.printStackTrace();
						}
                        
                        mainInterface.chatPanel.resetMessageToSend();
                        
                		buffer_out = ByteBuffer.allocate(1024);
                    	buffer_out.put(jsonObject.toString().getBytes());
                    	buffer_out.flip();
                        
                        try
                        {
							client.write(buffer_out);
						}
                        catch (IOException e)
                        {
							e.printStackTrace();
						}
                	}
                }
                while(!"close".equals(gameAction));   	
            });
            
            senderThread.start();
            
            do
            {
            	mainInterface.loginDialog.show();

                if(mainInterface.loginDialog.getActionType() != 0)
                {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("action", mainInterface.loginDialog.getActionType());
                    jsonObject.put("username", mainInterface.loginDialog.getUsername());
                    jsonObject.put("password", org.apache.commons.codec.digest.DigestUtils.sha256Hex(mainInterface.loginDialog.getPassword()));
                    
                    gameAction = jsonObject.toString();
                }
            }
            while(keepLogin);
            
            do //bet checker
            {
            	if(mainInterface.gamePanel.isBetReady())
            	{
            		byte   whoBet 	 = mainInterface.gamePanel.getWhoBet();
            		double amountBet = mainInterface.gamePanel.getAmountBet();
            		
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("action",    BET_ACTION);
                    jsonObject.put("subaction",    GIVE_BET_SUBACTION);
                    jsonObject.put("whobet", 	whoBet);
                    jsonObject.put("amountbet", amountBet);
                    
                    gameAction = jsonObject.toString();
            	}
            	else if(mainInterface.gamePanel.isCancelBet())
            	{
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("action",    BET_ACTION);
                    jsonObject.put("subaction", CANCEL_BET_SUBACTION);
                    
                    gameAction = jsonObject.toString();
            	}
            }
            while(true);
            
            /*
            client.close();
            System.out.println("Client connection closed");
            */
        }
        catch (IOException | JSONException e)
		{
            e.printStackTrace();
        }
	}
}