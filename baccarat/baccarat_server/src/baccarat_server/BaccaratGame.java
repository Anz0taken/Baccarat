package baccarat_server;

public class BaccaratGame{
	private int cardIndex;
	private int shoeCards[];

	private int gameStatus;
	
	private int bankerScore;
	private int bankerCards[];
	private int banInxCards;
	
	private int playerScore;
	private int playerCards[];
	private int plyInxCards;
	
	private int secondsCountdown;
	
	private int gamePhase;
	private int lastWinner;
	
	private final int NUMBERCARDS = 416;
	
	private final int INITIALCARDS    	= 1;
	private final int COMPARESCORES    	= 2;
	private final int BANKEREXTRACHECK 	= 3;
	private final int FINALCOMPARE 		= 4;
	private final int COUNTDOWN 		= 5;
	
	private final byte INGAME = 1, BANKERWINS = 2, PLAYERWINS = 3, TIEGAME = 4, BETSOPEN = 5, EMPTY = 0;
	
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
	private static final byte NEXT_ACTION_TIME = 12;	//Next action time in ms
	
	private Boolean setPlayersChecks;
	
	public BaccaratGame()
	{
		shoeCards   = new int[NUMBERCARDS];
		bankerCards = new int[3];
		playerCards = new int[3];
		gameStatus = EMPTY;
	}
	
	public void initializeCards()
	{
		lastWinner = 0;
		cardIndex = 0;
		loadCards();
		shuffleCards();
	}
	
	/*
	 * Game phases...
	 */
	
	public void startNewGame()
	{
		if(cardIndex > 364)	//cutting card
			initializeCards();
		
		gameStatus 	= INGAME;
		gamePhase 	= INITIALCARDS;
		banInxCards = 0;
		plyInxCards = 0;
		setPlayersChecks = false;
		
		bankerCards[0] = bankerCards[1] = bankerCards[2] = playerCards[0] = playerCards[1] = playerCards[2] = EMPTY;
	}
	
	public int[] nextAction()
	{
		if(gamePhase == INITIALCARDS)
		{
			if(plyInxCards > banInxCards)	//if the player has to get a card
			{
				bankerCards[banInxCards++] = shoeCards[cardIndex++];
			}
			else if(banInxCards == 2)
			{
				gamePhase = COMPARESCORES;
			}
			else
			{
				playerCards[plyInxCards++] = shoeCards[cardIndex++];
			}
		}
		
		if(gamePhase == BANKEREXTRACHECK)
		{
			if((playerScore == 6 || playerScore == 7) && (bankerScore >= 0 && bankerScore <= 7)) //Evolution of the banker
				bankerCards[banInxCards++] = shoeCards[cardIndex++];
			else if(bankerScore >= 0 && bankerScore <= 2)	//General rule
				bankerCards[banInxCards++] = shoeCards[cardIndex++];
			else if(plyInxCards == 3)
			{
				int playerExValue =  (getScoreCard( ((playerCards[2]%52) + 1  - 1)%13 + 1 ))%10;
				
				if(bankerScore == 3 && playerExValue != 8)
					bankerCards[banInxCards++] = shoeCards[cardIndex++];
				else if(bankerScore == 4 && playerExValue >= 2 && playerExValue <= 7)
					bankerCards[banInxCards++] = shoeCards[cardIndex++];
				else if(bankerScore == 5 && playerExValue >= 4 && playerExValue <= 7)
					bankerCards[banInxCards++] = shoeCards[cardIndex++];
				else if(bankerScore == 6 && playerExValue == 6 || playerExValue == 7)
					bankerCards[banInxCards++] = shoeCards[cardIndex++];
			}
			
			gamePhase = FINALCOMPARE;
		}
		else if(gamePhase == COMPARESCORES)
		{
			if(playerScore == 8 || bankerScore == 8 || playerScore == 9 || bankerScore == 9)
			{
				if(playerScore > bankerScore)
					gameStatus = PLAYERWINS;
				else if(playerScore < bankerScore)
					gameStatus = BANKERWINS;
				else
					gameStatus = TIEGAME;
				
				setPlayersChecks = true;
				lastWinner = gameStatus;
				gamePhase = COUNTDOWN;
				secondsCountdown = 15;
			}
			else if(playerScore <= 5)
			{
				playerCards[plyInxCards++] = shoeCards[cardIndex++];
			}
			
			if(gameStatus == INGAME)
				gamePhase = BANKEREXTRACHECK;
		}
		else if(gamePhase == FINALCOMPARE)
		{
			if(playerScore > bankerScore)
				gameStatus = PLAYERWINS;
			else if(playerScore < bankerScore)
				gameStatus = BANKERWINS;
			else
				gameStatus = TIEGAME;
			
			setPlayersChecks = true;
			lastWinner = gameStatus;
			gamePhase = COUNTDOWN;
			secondsCountdown = 15;
		}
		else if(gamePhase == COUNTDOWN)
		{
			secondsCountdown--;
			gameStatus = BETSOPEN;
			
			if(secondsCountdown == -1)
				this.startNewGame();
		}
		
		return builFullAnswer();
	}
	
	public Boolean getAndResetPlayerChecks()
	{
		Boolean result = false;
		
		if(setPlayersChecks)
			setPlayersChecks = !(result = true);
		
		return result;
	}
	
	public int getGamePhase()
	{
		return gamePhase;
	}
	
	public int getWhoWon()
	{
		return lastWinner;
	}
	
	public void logCards()
	{
		for(int i = 0; i < NUMBERCARDS; i++)
		{
			System.out.println(shoeCards[i]);
		}
	}
	
	private void loadCards()
	{
		for(int i = 0; i < NUMBERCARDS; i++)
		{
			shoeCards[i] = i + 1;	//offset "+ 2" -> 1 is empty
		}
	}
	
	private void shuffleCards()
	{
		for(int i = 0; i < 100000; i++)
		{
			int first = (int)(Math.random() * (415)), second = (int)(Math.random() * (415));
			
			int s = shoeCards[first];
			shoeCards[first] = shoeCards[second];
			shoeCards[second] = s;
		}
	}
	
	private int[] buildCardAnswer()
	{
		int result[];
		result = new int[6];
		for(int i = 0; i < 6; i++) result[i] = EMPTY;
		
		for(int i = 0; i < 3; i++)
		{		
			if(bankerCards[i] != EMPTY)
				result[i*2 + 1] = (bankerCards[i]%52) + 1;
			
			if(playerCards[i] != EMPTY)
				result[i*2] = (playerCards[i]%52) + 1;
		}

		return result;
	}
	
	private int[] builFullAnswer()
	{
		int fullAnswer[] = new int[13];
		
		fullAnswer[GAME_STATUS] = gameStatus;
		
		for(int i = 0, cardAnswer[] = buildCardAnswer(); i < 6; i++)
			fullAnswer[i + 1] = cardAnswer[i];
			
		playerScore = fullAnswer[PLAYER_SCORE] = ( getScoreCard( (fullAnswer[FIRST_CARD]  - 1)%13 + 1 ) + getScoreCard( (fullAnswer[THIRD_CARD]  - 1)%13 + 1) + getScoreCard( (fullAnswer[FIFTH_CARD] - 1)%13 + 1))%10;
		bankerScore = fullAnswer[BANKER_SCORE] = ( getScoreCard( (fullAnswer[SECOND_CARD] - 1)%13 + 1 ) + getScoreCard( (fullAnswer[FOURTH_CARD] - 1)%13 + 1) + getScoreCard( (fullAnswer[SIXTH_CARD] - 1)%13 + 1))%10;
		
		fullAnswer[AMOUNT_BET] = 1;
		fullAnswer[TARGET_BET] = 1;
		fullAnswer[BET_COUNTDOWN] = secondsCountdown;
		
		if(gameStatus == INGAME)
			fullAnswer[NEXT_ACTION_TIME] = 2000;
		else if(gameStatus == BETSOPEN)
			fullAnswer[NEXT_ACTION_TIME] = 1000;
		else
			fullAnswer[NEXT_ACTION_TIME] = 6000;
		
		return fullAnswer;
	}
	
	private int getScoreCard(int cardValue)
	{
		if(cardValue > 10)
			cardValue = 10;
		
		return cardValue;
	}
	
	public int getGameStatus()
	{
		return gameStatus;
	}
}
