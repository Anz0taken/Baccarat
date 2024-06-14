package baccarat_server;

import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.Semaphore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class BaccaratServer{
//	static List<SocketGetHandler> handlerGet;
//	static List<SocketSendHandler> handlerSend;
	private static Selector selector = null;
	private static Semaphore semaphore;
	private static Hashtable<java.net.Socket, ClientData> userDict;
	private static List<ClientData> lastUsersBet;
	private static String	contentToSend;
	
	private static int fullAnswer[];
	private final static byte GAME_INFO = 1;
	
	//to recive
    private static final byte NO_ACTION 	  = 0;
    private static final byte LOGIN_ACTION    = 2;
    private static final byte REGISTER_ACTION = 3;
    private static final byte BET_ACTION 	  = 5;
	private static final byte GIVE_BET_SUBACTION 	= 6;
	private static final byte CANCEL_BET_SUBACTION 	= 7;
    
    //to send
    private static final byte DIALOG_RESPONSE = 4;
    private static final byte INFO_TEXT_RESPONSE = 5;
    private static final byte ACTION_GAME_RESULT = 6;
    
	//to sent and recive
	private static final byte CHAT_MESSAGE_ACTION = 8;
    
    private final static int COUNTDOWN = 5;
    
    static Connection dbConnection;
    static Statement stmt;
    static BaccaratGame baccaratHandler;
	
	public static void main(String[] args) throws SQLException, IOException, InterruptedException {
		
		dbConnection = initializeSQL();
	    stmt = dbConnection.createStatement();
	    
	    ByteBuffer buffer = ByteBuffer.allocate(1024);
	    
	    semaphore = new Semaphore(1);
	    
	    userDict = new Hashtable<java.net.Socket, ClientData>();
	    lastUsersBet = new LinkedList<ClientData>();
	    contentToSend = "";
	    
	    baccaratHandler = new BaccaratGame();
	    baccaratHandler.initializeCards();
	    
	    try
	    {
            selector = Selector.open();
            // We have to set connection host,port and
            // non-blocking mode
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            ServerSocket serverSocket = serverSocketChannel.socket();
            serverSocket.setSoTimeout(2000);
            
            serverSocket.bind(new InetSocketAddress("localhost", 2212));
            serverSocketChannel.configureBlocking(false);
            
            int ops = serverSocketChannel.validOps();
            
            serverSocketChannel.register(selector, ops, SelectionKey.OP_ACCEPT | SelectionKey.OP_READ | SelectionKey.OP_WRITE);
            
            fullAnswer = null;
            
            Thread t = new Thread(() -> {
                int  sendMessageToAll = 500;
                int  nextGamePhase   = 2000;
                
                baccaratHandler.startNewGame();
                
                long lastSendMessage = System.currentTimeMillis();
                long lastGamePhase   = System.currentTimeMillis();
                
                StringBuilder msgToSend = new StringBuilder("              ");
            	
                while(true)
                {
                    if(System.currentTimeMillis() - lastGamePhase > nextGamePhase)
                    {
                    	lastGamePhase = System.currentTimeMillis();
                    	
                    	fullAnswer = baccaratHandler.nextAction();
                    	
                    	for(int i = 0; i < 13; i++)
                    		msgToSend.setCharAt(i, (char)(fullAnswer[i]));
                    	
                    	nextGamePhase = fullAnswer[12];
                    	
                    	if(baccaratHandler.getAndResetPlayerChecks())	//If the game is over and all players have to check the win
                    	{
                    		setAllPlayerCheck();
                    		
                    		try
                    		{
								checkLeftedPlayers();
							}
                    		catch(SQLException e)
                    		{
								e.printStackTrace();
							}
                    	}
                    }
                	
                    if(System.currentTimeMillis() - lastSendMessage > sendMessageToAll)
                    {
                    	lastSendMessage = System.currentTimeMillis();

                        try {
							semaphore.acquire();
						} catch (InterruptedException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
                        
                        try {
							selector.select();
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
                        
                        Set<SelectionKey> selectedKeys = selector.selectedKeys();
                        Iterator<SelectionKey> i = selectedKeys.iterator();
                                           	
                        while(i.hasNext())
                        {
                        	SelectionKey key = i.next();
                        	
                        	if(key.isWritable())
                            {
                        		SocketChannel client = (SocketChannel)key.channel();
                        		ClientData clientResp = userDict.get(client.socket());
                        		
                            	try
                            	{
                            		if(fullAnswer != null)
                            			sendGameInfo(key, fullAnswer);
                            		
                            		JSONObject response = clientResp.getJSONResponse();
                            		
                            		if(response != null)
                            		{
                            			sendJSONResponse(key, response);
                            			clientResp.resetJSONResponse();
                            		}

                            		if(baccaratHandler.getGamePhase() == COUNTDOWN && clientResp.getAndResetToCheckGame())
                            		{
                            			sendJSONResponse(key, actionPlayerWinCheck(key, clientResp));
                            		}
                            		
                            		if(!contentToSend.equals(""))
                            		{
                            			sendJSONResponse(key, createJSONMessage(contentToSend));
                            		}
                            		
								} catch (IOException e) {
									e.printStackTrace();
								} catch (JSONException e) {
									e.printStackTrace();
								} catch (SQLException e) {
									e.printStackTrace();
								}
                            }
                        }
                        
                        contentToSend = "";
                        
                        semaphore.release();
                    }
                }
            });
            
            t.start();
            
            String msg_to_read = "";
            
            while(true)
            {
            	semaphore.acquire();
            	
                selector.select();
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> i = selectedKeys.iterator();
            	
                while(i.hasNext())
                { 	
                    SelectionKey key = i.next();
  
                    if (key.isAcceptable())
                    {
                        // New client has been  accepted
                        handleAccept(serverSocketChannel, key);
                        
                        //userDict.put((SocketChannel)key.channel(), new ClientData());
                    }
                    else if(key.isReadable())
                    {
                        // We can run non-blocking operation
                        // READ on our client
                    	handleRead(key);
                    }
                    
                    i.remove();
                }
                
                semaphore.release();
            }
        }
        catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }
	
	private static Connection initializeSQL()
	{
		Connection dbConnection = null;
		
		try
	    {
	    	String url = "jdbc:mysql://localhost:3306/baccarat";
	    	Properties info = new Properties();
			info.put("user", "root");
			info.put("password", Data.DATABASE_PSWD);
			
			dbConnection = DriverManager.getConnection(url, info);
			
			if(dbConnection != null){
				System.out.println("Successfully connected to MySQL database test");
		    }

		}
	    catch (SQLException ex) {
			System.out.println("An error occurred while connecting MySQL databse");
			ex.printStackTrace();
		}
		
		return dbConnection;
	}
	
    private static void handleAccept(ServerSocketChannel mySocket, SelectionKey key) throws IOException
    {
        // Accept the connection and set non-blocking mode
        SocketChannel client = mySocket.accept();
        client.configureBlocking(false);
        
        client.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
        userDict.put(client.socket(), new ClientData());
        
        System.out.println("Connection Accepted; Client infos : "+client.socket());
    }
  
    private static void handleRead(SelectionKey key) throws JSONException, SQLException
    {   	
        // create a ServerSocketChannel to read the request
        SocketChannel client = (SocketChannel)key.channel();
        
        ClientData clientInfos = userDict.get(client.socket());
        
        // Create buffer to read data
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        
        try
        {
			client.read(buffer);
		}
        catch(IOException e)
        {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
			try
			{			
				if(clientInfos.isInBet())	//if the user is in bet, wait till the end of the game
					lastUsersBet.add(clientInfos);
				
				userDict.remove(client.socket());
				client.close();
			}
			catch(IOException e1)
			{
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
        
        // Parse data from buffer to String
        String data = new String(buffer.array()).trim();
        
        if(data.length() > 0)
        {
        	JSONObject response = new JSONObject(data);
        	
            if(response.getInt("action") == LOGIN_ACTION || response.getInt("action") == REGISTER_ACTION)
            {
            	if(clientInfos.getUserId() == ClientData.UNLOGGED)
            	{
            		/*
            		*  No protection from SQL injection, has to be integrated.
            		*/
            		String username = response.getString("username").toLowerCase();
            		String password = response.getString("password");         		
            		
            		if(response.getInt("action") == LOGIN_ACTION)
            		{
					    JSONObject backResponse = new JSONObject();
					    backResponse.put("action",  	DIALOG_RESPONSE);
					    backResponse.put("subaction", 	LOGIN_ACTION);
					    backResponse.put("title", 		"Login status");
					    
					    try
					    {
					    	ResultSet rs = stmt.executeQuery("SELECT * FROM user WHERE username = '"+username+"' AND password = '"+password+"'");
					    	
					    	if(rs.next())
					    	{
					    		if(!checkUserAlreadyConnected(username))	//if this account is not currently playing
					    		{
						    		clientInfos.setUserId(rs.getInt("iduser"));
						    		clientInfos.setUsername(username);
						    		
						    		backResponse.put("money", rs.getDouble("money"));
						    		backResponse.put("username", username);
						    		backResponse.put("success", 1);
						    		backResponse.put("message", "Login into the account with success. Account : "+username);
					    		}
					    		else
					    		{
						    		backResponse.put("success", 0);
						    		backResponse.put("message", "Account already online...");
					    		}
					    	}
					    	else
					    	{
					    		backResponse.put("success", 0);
					    		backResponse.put("message", "No username found.");
					    	}
						}
					    catch (SQLException e)
					    {
							e.printStackTrace();
				    		backResponse.put("success", 0);
				    		backResponse.put("message", "Error with your login.");
						}
					    
					    clientInfos.setJSONResponse(backResponse);
            		}
            		else if(response.getInt("action") == REGISTER_ACTION)
            		{
            			int success;
					    JSONObject backResponse = new JSONObject();
					    backResponse.put("action", DIALOG_RESPONSE);
					    backResponse.put("subaction", REGISTER_ACTION);
					    backResponse.put("title", "Register status");
					    
						try
						{
							success = stmt.executeUpdate("INSERT INTO user (username, password, money) VALUES ('"+username+"','"+password+"', 10)");
							
	            			if(success > 0)
	            			{
	            				System.out.println("New user addedd with success, username : "+username);
	            				backResponse.put("message", "New user addedd with success, username : "+username);
	            				backResponse.put("success", success);
	            			}
	            			
						}
						catch (SQLException e)
						{
							// TODO Auto-generated catch block
							e.printStackTrace();
							
							System.out.println("Error occurred.");
							backResponse.put("message", "Error occurred, maybe another account with the same username already exists.");
							backResponse.put("success", 0);
						}
            			
						clientInfos.setJSONResponse(backResponse);
            		}
            	}
            }
            else if(response.getInt("action") == BET_ACTION)
            {
            	if(clientInfos.getUserId() != ClientData.UNLOGGED)
            	{		
				    JSONObject backResponse = new JSONObject();
				    backResponse.put("action",  	INFO_TEXT_RESPONSE);
				    backResponse.put("subaction", 	BET_ACTION);
				    backResponse.put("moneyremaining", 0.0);
				    
				    if(clientInfos.getWhoBet() == 0 && response.getInt("subaction") != CANCEL_BET_SUBACTION)	//if the user hasn't placed a bet already
				    {
						ResultSet rs = stmt.executeQuery("SELECT * FROM user WHERE iduser = "+clientInfos.getUserId()+"");
						
				    	if(baccaratHandler.getGameStatus() == COUNTDOWN)	//if the bet is placed during countdown
					    {
							if(rs.next())
							{
								if(response.getDouble("amountbet") <= rs.getDouble("money") && response.getInt("whobet") != 0 && response.getDouble("amountbet") > 0)
								{
									double moneyRemaining = rs.getDouble("money") - response.getDouble("amountbet");
									int success = stmt.executeUpdate("UPDATE user SET money = "+round(moneyRemaining, 2)+" WHERE iduser = "+clientInfos.getUserId());
								
									if(success > 0)
									{
										clientInfos.setWhoBet((byte) response.getInt("whobet"));
										clientInfos.setAmountBet(response.getDouble("amountbet"));
										
								    	backResponse.put("message", "[SERVER] Bet accepted. Good luck!\n");
								    	backResponse.put("moneyremaining", round(moneyRemaining, 2));
								    	backResponse.put("whobet", (byte) response.getInt("whobet"));
								    	backResponse.put("success", 1);
									}
								}
								else
								{
							    	backResponse.put("message", "[SERVER] Please check that your bet is valid. You might have betted more then you have or not have chosed banker or player.\n");
							    	backResponse.put("success", 0);
								}
							}
					    }
					    else
					    {
					    	backResponse.put("message", "[SERVER] Bet refused, please bet during countdown!\n");
					    	backResponse.put("success", 0);
					    }			    	
				    }
				    else
				    {
				    	if(response.getInt("subaction") == CANCEL_BET_SUBACTION && baccaratHandler.getGameStatus() == COUNTDOWN)
				    	{
				    		ResultSet rs = stmt.executeQuery("SELECT money FROM user WHERE iduser = "+clientInfos.getUserId()+"");
				    		
				    		if(rs.next())
				    		{
				    			double moneyRemaining = rs.getDouble("money") + clientInfos.getAmountBet();
				    			int success = stmt.executeUpdate("UPDATE user SET money = "+round(moneyRemaining, 2)+" WHERE iduser = "+clientInfos.getUserId());
				    			
				    			clientInfos.setWhoBet((byte) 0);
				    			clientInfos.setAmountBet(0);
				    			
				    			backResponse.put("moneyremaining", round(moneyRemaining, 2));
				    			backResponse.put("message", "[SERVER] Your bet has been removed.\n");
				    			backResponse.put("success", 2);
				    		}
				    	}
				    	else
				    	{
					    	backResponse.put("message", "[SERVER] You got already a bet placed! Or trying to remove while in game.\n");
					    	backResponse.put("success", 0);
				    	}
				    }

	            	clientInfos.setJSONResponse(backResponse);
            	}
            }
            else if(response.getInt("action") == CHAT_MESSAGE_ACTION)
            {
            	contentToSend += ("["+clientInfos.getUsername()+"] : " + response.getString("message")+"\n");
            }
        }
    }
    
    private static void sendGameInfo(SelectionKey key, int GameInfo[]) throws IOException, JSONException
    {   
        // create a ServerSocketChannel to read the request
        SocketChannel client = (SocketChannel)key.channel();
  
        // Create buffer to read data
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("action", GAME_INFO);
        
        JSONArray jsArray = new JSONArray();
        for (int i = 0; i < GameInfo.length; i++)
        {
            jsArray.put(GameInfo[i]);
         }
        
        jsonObject.put("game_info", jsArray);
        
        buffer.put((jsonObject.toString().getBytes()));
        buffer.flip();
        
        client.write(buffer);
    }
    
    private static void sendJSONResponse(SelectionKey key, JSONObject response) throws IOException, JSONException
    {   
        // create a ServerSocketChannel to read the request
        SocketChannel client = (SocketChannel)key.channel();
  
        // Create buffer to read data
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        
        buffer.put((response.toString().getBytes()));
        buffer.flip();
        
        client.write(buffer);
    }
    
    private static JSONObject actionPlayerWinCheck(SelectionKey key, ClientData player) throws JSONException, SQLException, IOException
    {
	    JSONObject backResponse = new JSONObject();
	    backResponse.put("action", 		INFO_TEXT_RESPONSE);
	    backResponse.put("subaction", 	ACTION_GAME_RESULT);
	    backResponse.put("success", 2);
	    
	    double gameMolt[] = new double[3];
	    gameMolt[0] = 0.95;
	    gameMolt[1] = 1;
	    gameMolt[2] = 8;
	    
	    ResultSet rs = stmt.executeQuery("SELECT money FROM user WHERE iduser = "+player.getUserId()+"");
	    
	    if(rs.next())
	    {
		    if(player.getWhoBet() == baccaratHandler.getWhoWon())
		    {
				Double moneyRecived = player.getAmountBet()*gameMolt[ player.getWhoBet() - 2 ] + player.getAmountBet();
		    	backResponse.put("moneyrecived", moneyRecived);
		    	
		    	backResponse.put("totalmoney", round((rs.getDouble("money") + moneyRecived),2));
		    	backResponse.put("message", "[SERVER] Congratulation, you won!\n");
		    	
		    	String sql = "UPDATE user SET money = "+round((rs.getDouble("money") + moneyRecived),2)+" WHERE iduser = "+player.getUserId();
		    	
		    	stmt.executeUpdate(sql);
		    }
		    else
		    {
		    	backResponse.put("totalmoney", rs.getDouble("money"));
		    	backResponse.put("message", "[SERVER] Try again.\n");
			    backResponse.put("moneyrecived", 0);
		    }
	    }
	    
    	player.setWhoBet((byte) 0);
    	player.setAmountBet(0);
	    
	    return backResponse;
    }
    
    private static JSONObject createJSONMessage(String content) throws JSONException
    {
    	JSONObject backResponse = new JSONObject();
	    backResponse.put("action", 		CHAT_MESSAGE_ACTION);
	    backResponse.put("content", 	content);
	    
    	return backResponse;
    }
    
    private static void setAllPlayerCheck()
    {
		Set<java.net.Socket> setOfKeys = userDict.keySet();
	       
        for(java.net.Socket searchKey : setOfKeys)
        {
        	ClientData player = userDict.get(searchKey);
        	
        	if(player.getWhoBet() != 0)
        		player.setToCheckGame();
        }
    }
    
    private static void checkLeftedPlayers() throws SQLException
    {
    	for(int i = 0; i < lastUsersBet.size(); i++)
    	{
    		ClientData player = lastUsersBet.get(i);
    		   		
    	    double gameMolt[] = new double[3];
    	    gameMolt[0] = 0.95;
    	    gameMolt[1] = 1;
    	    gameMolt[2] = 8;
    	    
    	    ResultSet rs = stmt.executeQuery("SELECT money FROM user WHERE iduser = "+player.getUserId()+"");
    	    
    	    if(rs.next())
    	    {
    		    if(player.getWhoBet() == baccaratHandler.getWhoWon())
    		    {
    				Double moneyRecived = player.getAmountBet()*gameMolt[ player.getWhoBet() - 2 ] + player.getAmountBet();
    		    	    				
    		    	String sql = "UPDATE user SET money = "+round((rs.getDouble("money") + moneyRecived),2)+" WHERE iduser = "+player.getUserId();

    		    	stmt.executeUpdate(sql);
    		    }
    	    }
    	}
    	
    	lastUsersBet.clear();
    }
    
    private static boolean checkUserAlreadyConnected(String username)
    {
    	Boolean found = false;
    	
		Set<java.net.Socket> setOfKeys = userDict.keySet();
	       
        for(java.net.Socket searchKey : setOfKeys) if (!found)
        {
        	ClientData clientCheck = userDict.get(searchKey);
        	
        	if(clientCheck.getUsername() != null)
        		if(clientCheck.getUsername().equals(username))
        			found = true;
        }
        
    	return found;
    }
    
    private static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}
