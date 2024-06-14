package baccarat_server;

import java.net.Socket;

import org.json.JSONObject;

public class ClientData {
	private byte whoBet;
	private double amountBet;
	
	private Boolean checkedLastGame;
	
	private int userId;
	private String username;
	
	public static int UNLOGGED = 0;
	
	private JSONObject backResponse;
	
	public ClientData()
	{
		backResponse = null;
		amountBet = userId = whoBet  = 0;
		username = null;
		checkedLastGame = false;
	}
	
	public void setWhoBet(byte whoBet_)
	{
		whoBet = whoBet_;
	}
	
	public void setToCheckGame()
	{
		checkedLastGame = true;
	}
	
	public Boolean getAndResetToCheckGame()
	{
		Boolean result = false;
		
		if(checkedLastGame)
			checkedLastGame = !(result = true);
		
		return result;
	}
	
	public byte getWhoBet()
	{
		return whoBet;
	}
	
	public void setAmountBet(double amountBet_)
	{
		amountBet = amountBet_;
	}
	
	public double getAmountBet()
	{
		return amountBet;
	}
	
	public Boolean isInBet()
	{
		if(whoBet != 0)
			return true;
		else
			return false;
	}
	
	public void setUsername(String username_)
	{
		username = username_;
	}
	
	public String getUsername()
	{
		return username;
	}
	
	public void setUserId(int id)
	{
		userId = id;
	}
	
	public int getUserId()
	{
		return userId;
	}
	
	void setJSONResponse(JSONObject response_)
	{
		backResponse = response_;
	}
	
	JSONObject getJSONResponse()
	{
		return backResponse;
	}
	
	void resetJSONResponse()
	{
		backResponse = null;
	}
}
