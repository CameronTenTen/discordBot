import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import sx.blah.discord.Discord4J;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;

/**Object that represents one gather game, with players, sub history, server, and other status variables. 
 * @author cameron
 *
 */
public class GatherGame
{
	//currently unused
	private int gameID;
	private int currentRound;
	private gameState currState;
	private int blueTickets;
	private int redTickets;
	private List<PlayerObject> eliminatedPlayers;
	
	private long startTime;
	private int gameLengthSeconds;
	
	private int winningTeam;

	private List<PlayerObject> players;
	private List<PlayerObject> bluePlayerList;
	private List<PlayerObject> redPlayerList;
	private GatherServer server;
	
	private IRole blueRole;
	private IRole redRole;

	private Set<PlayerObject> redDeserted;
	private Set<PlayerObject> blueDeserted;
	private Set<PlayerObject> redSubbedIn;
	private Set<PlayerObject> blueSubbedIn;

	private Set<PlayerObject> scrambleVotes;
	private Set<PlayerObject> cancelVotes;

	private int scrambleVotesReq = 5;
	private int cancelVotesReq = 7;

	public enum gameState
	{
		PREGAME,
		BUILDINGTIME,
		ROUNDINPROGRESS,
		LASTSTAND,
		ENDED
	}

	GatherGame(int matchId, List<PlayerObject> players, List<PlayerObject> blueTeam, List<PlayerObject> redTeam, GatherServer server, IRole blueTeamRole, IRole redTeamRole)
	{
		this.gameID = matchId;
		this.players = players;
		this.bluePlayerList = blueTeam;
		this.redPlayerList = redTeam;
		this.server = server;
		this.blueRole = blueTeamRole;
		this.redRole = redTeamRole;
		this.setCurrentRound(0);
		this.setRedTickets(0);
		this.setBlueTickets(0);
		this.setCurrState(gameState.PREGAME);
		
		this.startTime = System.nanoTime();
		this.gameLengthSeconds = 0;
		this.setWinningTeam(-2);

		this.redDeserted = new HashSet<PlayerObject>();
		this.blueDeserted = new HashSet<PlayerObject>();
		this.redSubbedIn = new HashSet<PlayerObject>();
		this.blueSubbedIn = new HashSet<PlayerObject>();

		this.scrambleVotes = new HashSet<PlayerObject>();
		this.cancelVotes = new HashSet<PlayerObject>();
	}

	/**Checks if the bot is connected with the server that this game is being played on. 
	 * @return false if we have any indication the server is not connected, true otherwise
	 * @see GatherServer#isConnected()
	 */
	public boolean isConnectedToServer()
	{
		return server.isConnected();
	}
	
	/** Helper function for generating the command that should be sent to the kag server in order to specify the blue team
	 * @return a string that the kag server will be able to process in order to set the blue team players
	 */
	private String generateBlueTeamMsg() {
		String msg = "";
		if(!bluePlayerList.isEmpty())
		{
			msg+="string[] blue={'";
			for(PlayerObject p : bluePlayerList)
			{
				msg+=p.getKagName();
				msg+="', '";
			}
			msg=msg.substring(0, msg.length()-3);
			msg+="}; getRules().set('blueTeam',blue);";
		}
		else
		{
			msg="string[] blue={}; getRules().set('blueTeam',blue);";
		}
		return msg;
	}
	
	/** Helper function for generating the command that should be sent to the kag server in order to specify the red team
	 * @return a string that the kag server will be able to process in order to set the red team players
	 */
	private String generateRedTeamMsg() {
		String msg="";
		if(!redPlayerList.isEmpty())
		{
			msg+="string[] red={'";
			for(PlayerObject p : redPlayerList)
			{
				msg+=p.getKagName();
				msg+="', '";
			}
			msg=msg.substring(0, msg.length()-3);
			msg+="}; getRules().set('redTeam',red);";
		}
		else
		{
			msg="string[] red={}; getRules().set('redTeam',red);";
		}
		return msg;
	}

	/**Helper function for sending the teams to the KAG server when a game is created
	 */
	public void sendTeamsToServer()
	{
		//set the teams for the kag server by doing these commands
		//getRules().set_s32('numPlayers',10);			//if we want to adjust the game size
		//string[] blue={'player1', 'player2', 'etc'}; getRules().set('blueTeam',blue);
		//string[] red={'player1', 'player2', 'etc'}; getRules().set('redTeam',red);
		//getRules().set_bool('teamsSet',true);
		server.sendMessage("getRules().set_s32('numPlayers',"+this.getPlayerCount()+");");
		server.sendMessage(this.generateBlueTeamMsg());
		server.sendMessage(this.generateRedTeamMsg());
		server.sendMessage("getRules().set_bool('teamsSet',true);");
	}

	/**Helper function for updating the teams on the KAG server when the teams have been scrambled (forces a check of all players teams). 
	 */
	public void sendScrambledTeamsToServer()
	{
		//update the teams for the kag server by doing these commands
		//getRules().set_s32('numPlayers',10);			//if we want to adjust the game size
		//string[] blue={'player1', 'player2', 'etc'}; getRules().set('blueTeam',blue);
		//string[] red={'player1', 'player2', 'etc'}; getRules().set('redTeam',red);
		//"getRules().set_bool('teamsUpdated',true);"
		server.sendMessage("getRules().set_s32('numPlayers',"+this.getPlayerCount()+");");
		server.sendMessage(this.generateBlueTeamMsg());
		server.sendMessage(this.generateRedTeamMsg());
		server.sendMessage("getRules().set_bool('teamsScrambled',true);");

	}

	/**Helper function for updating the teams on the KAG server when the teams are changed or for any other reason. 
	 */
	public void updateTeamsOnServer()
	{
		//update the teams for the kag server by doing these commands
		//getRules().set_s32('numPlayers',10);			//if we want to adjust the game size
		//string[] blue={'player1', 'player2', 'etc'}; getRules().set('blueTeam',blue);
		//string[] red={'player1', 'player2', 'etc'}; getRules().set('redTeam',red);
		//"getRules().set_bool('teamsUpdated',true);"
		server.sendMessage("getRules().set_s32('numPlayers',"+this.getPlayerCount()+");");
		server.sendMessage(this.generateBlueTeamMsg());
		server.sendMessage(this.generateRedTeamMsg());
		server.sendMessage("getRules().set_bool('teamsUpdated',true);");

	}

	/**Replaces one player in the game with another, this is the place where subs are actually subbed in. 
	 * @param playerBeingReplaced a PlayerObject representing the player that is being removed
	 * @param player a PlayerObject representing the player that is taking their place
	 */
	public void replacePlayer(PlayerObject playerBeingReplaced, PlayerObject player)
	{
		int blueIndex = bluePlayerList.indexOf(playerBeingReplaced);
		int redIndex = redPlayerList.indexOf(playerBeingReplaced);
		if(blueIndex>=0 && blueIndex<bluePlayerList.size())
		{
			bluePlayerList.set(blueIndex, player);
			blueDeserted.add(playerBeingReplaced);
			blueSubbedIn.add(player);
			DiscordBot.removeRole(playerBeingReplaced.getDiscordUserInfo(), this.getBlueRole());
			DiscordBot.addRole(player.getDiscordUserInfo(), this.getBlueRole());
		}
		else if(redIndex>=0 && redIndex<redPlayerList.size())
		{
			redPlayerList.set(redIndex, player);
			redDeserted.add(playerBeingReplaced);
			redSubbedIn.add(player);
			DiscordBot.removeRole(playerBeingReplaced.getDiscordUserInfo(), this.getRedRole());
			DiscordBot.addRole(player.getDiscordUserInfo(), this.getRedRole());
		}
		//seems that the shuffle method (setting team arrays as subLists of the player array)
		//makes it so that replacing the player in the team list makes the replacement in the players one too
		//will keep this check in here in case something changes (maybe new shuffle function or match making will change this) 
		int index = players.indexOf(playerBeingReplaced);
		if(index >=0 && index<players.size()) players.set(index, player);
		updateTeamsOnServer();
		return;
	}

	/**Function for saving the result of the game to the database. 
	 * @param winningTeam the team that won this game
	 * @param subObj the SubManager object that tracks subs for this game
	 */
	public void saveResultToDB(SubManager subObj)
	{
		DiscordBot.database.addGame(this);
		//TODO remove games played variable at the end of the season
		DiscordBot.database.incrementGamesPlayed();
		for(PlayerObject p : bluePlayerList)
		{
			//if they subbed in or left they dont get a win/loss stat
			if(blueSubbedIn.contains(p) || subObj.hasSubRequest(p))
			{
				continue;
			}
			else if(this.getWinningTeam()==0)
			{
				int val = DiscordBot.database.addWin(p.getKagName());
				Discord4J.LOGGER.info("Adding win for "+p.getKagName()+" "+val);
			}
			else if(this.getWinningTeam()==1)
			{
				int val = DiscordBot.database.addLoss(p.getKagName());
				Discord4J.LOGGER.info("Adding loss for "+p.getKagName()+" "+val);
			}
			//TODO warning, draws not accounted for
		}
		for(PlayerObject p : redPlayerList)
		{
			//if they subbed in or left they dont get a win/loss stat
			if(redSubbedIn.contains(p) || subObj.hasSubRequest(p))
			{
				continue;
			}
			else if(this.getWinningTeam()==1)
			{
				int val = DiscordBot.database.addWin(p.getKagName());
				Discord4J.LOGGER.info("Adding win for "+p.getKagName()+" "+val);
			}
			else if(this.getWinningTeam()==0)
			{
				int val = DiscordBot.database.addLoss(p.getKagName());
				Discord4J.LOGGER.info("Adding loss for "+p.getKagName()+" "+val);
			}
			//TODO warning, draws not accounted for
		}
		for(PlayerObject p : blueDeserted)
		{
			if(this.getWinningTeam()==0)
			{
				int val = DiscordBot.database.addDesertion(p.getKagName());
				Discord4J.LOGGER.info("Adding desertion for "+p.getKagName()+" "+val);
			}
			else if(this.getWinningTeam()==1)
			{
				int val = DiscordBot.database.addDesertionLoss(p.getKagName());
				Discord4J.LOGGER.info("Adding desertion for "+p.getKagName()+" "+val);
			}
		}
		for(PlayerObject p : redDeserted)
		{
			if(this.getWinningTeam()==1)
			{
				int val = DiscordBot.database.addDesertion(p.getKagName());
				Discord4J.LOGGER.info("Adding desertion for "+p.getKagName()+" "+val);
			}
			else if(this.getWinningTeam()==0)
			{
				int val = DiscordBot.database.addDesertionLoss(p.getKagName());
				Discord4J.LOGGER.info("Adding desertion for "+p.getKagName()+" "+val);
			}
		}
		for(PlayerObject p : blueSubbedIn)
		{
			if(this.getWinningTeam()==0)
			{
				int val = DiscordBot.database.addSubstitutionWin(p.getKagName());
				Discord4J.LOGGER.info("Adding substitution for "+p.getKagName()+" "+val);
			}
			else if(this.getWinningTeam()==1)
			{
				int val = DiscordBot.database.addSubstitution(p.getKagName());
				Discord4J.LOGGER.info("Adding substitution for "+p.getKagName()+" "+val);
			}
		}
		for(PlayerObject p : redSubbedIn)
		{
			if(this.getWinningTeam()==1)
			{
				int val = DiscordBot.database.addSubstitutionWin(p.getKagName());
				Discord4J.LOGGER.info("Adding substitution for "+p.getKagName()+" "+val);
			}
			else if(this.getWinningTeam()==0)
			{
				int val = DiscordBot.database.addSubstitution(p.getKagName());
				Discord4J.LOGGER.info("Adding substitution for "+p.getKagName()+" "+val);
			}
		}

		List<PlayerObject> subs = subObj.getOpenSubs(this);
		if(subs!=null)
		{
			for(PlayerObject p : subs)
			{
				if(this.getPlayerTeam(p) == this.getWinningTeam() || this.getWinningTeam() == -1)
				{
					//if they were on the winning team or its a draw add a desertion
					DiscordBot.database.addDesertion(p.getKagName());
				}
				else if (this.getWinningTeam()>=0 && this.getPlayerTeam(p) != this.getWinningTeam())
				{
					//if a team won and the player wasnt on it then they get a desertion loss
					DiscordBot.database.addDesertionLoss(p.getKagName());
				}
			}
		}
	}

	/**Shuffles the current player list, then puts the first half into the blue team, and the second half into the red team. 
	 * @see Collections#shuffle(List)
	 */
	public void shuffleTeams()
	{
		Collections.shuffle(players);
		bluePlayerList = players.subList(0, players.size()/2);
		redPlayerList = players.subList(players.size()/2, players.size());
	}

	/**Add a vote to scramble teams. 
	 * @param player the player voting to scramble
	 * @return -1 if the player has already voted, 0 if enough votes were counted to shuffle the teams, otherwise the current number of scramble votes counted
	 */
	public int addScrambleVote(PlayerObject player)
	{
		if(!this.scrambleVotes.add(player))
		{
			//player already voted
			return -1;
		}
		if(this.scrambleVotes.size()>=this.scrambleVotesReq)
		{
			this.shuffleTeams();
			this.sendScrambledTeamsToServer();
			this.scrambleVotes.clear();
			return 0;
		}
		return this.scrambleVotes.size();
	}

	/**Add a vote to cancel game. 
	 * @param player the player voting to cancel
	 * @return -1 if the player has already voted, 0 if enough votes were counted to cancel the game, otherwise the current number of cancel votes counted
	 */
	public int addCancelVote(PlayerObject player)
	{
		if(!this.cancelVotes.add(player))
		{
			//player already voted
			return -1;
		}
		if(this.cancelVotes.size()>=this.cancelVotesReq)
		{
			return 0;
		}
		return this.cancelVotes.size();
	}

	/**Helper function for getting a list of mention strings of each player on blue team. 
	 * @return a list of mention strings of blue team
	 */
	public List<String> blueMentionList()
	{
		ArrayList<String> list = new ArrayList<String>();
		for(PlayerObject player : bluePlayerList)
		{
			list.add(player.getMentionString());
		}
		return list;
	}

	/**Helper function for getting a list of mention strings of each player on red team. 
	 * @return a list of mention strings of red team
	 */
	public List<String> redMentionList()
	{
		ArrayList<String> list = new ArrayList<String>();
		for(PlayerObject player : redPlayerList)
		{
			list.add(player.getMentionString());
		}
		return list;
	}

	/**Gets the id of this game. 
	 * @return the game id number
	 */
	public int getGameID() {
		return gameID;
	}

	/**Sets the id of this game. 
	 * @param gameID the game id number
	 */
	public void setGameId(int gameID) {
		this.gameID = gameID;
	}

	/**Find the team of a player. 
	 * @param user the Discord4J user object for the player
	 * @return the team number of the player
	 */
	public int getPlayerTeam(IUser user)
	{
		if(user==null) return -1;
		for(PlayerObject p : bluePlayerList)
		{
			if(user.equals(p.getDiscordUserInfo()))return 0;
		}
		for(PlayerObject p : redPlayerList)
		{
			if(user.equals(p.getDiscordUserInfo()))return 1;
		}
		return -1;
	}

	/**Wrapper function for finding the team of a player using their PlayerObject. 
	 * @param player the PlayerObject of the player
	 * @return the team number of the player
	 * @see #getPlayerTeam(IUser)
	 */
	public int getPlayerTeam(PlayerObject player)
	{
		return getPlayerTeam(player.getDiscordUserInfo());
	}

	/**Check if this player is playing in the game from Discord4J user object. 
	 * @param user the Discord4J user object for the player
	 * @return true if the players team number is valid, false if not
	 */
	public boolean isPlayerPlaying(IUser user)
	{
		return getPlayerTeam(user)!=-1;
	}

	/**Check if this player is playing in the game from PlayerObject. 
	 * @param player the PlayerObject of the player
	 * @return true if the players team number is valid, false if not
	 */
	public boolean isPlayerPlaying(PlayerObject player)
	{
		return getPlayerTeam(player)!=-1;
	}

	/**Getter for the server ip address. 
	 * @return the ip address of the server this game is being played on
	 */
	public String getServerIp()
	{
		return server.getIp();
	}

	/**Getter for the server port.
	 * @return the port of the server this game is being played on. 
	 */
	public int getServerPort()
	{
		return server.getPort();
	}

	/**Getter for the GatherServer object
	 * @return the GatherServer that this game is being played on. 
	 */
	public GatherServer getServer() {
		return server;
	}

	/**Setter for the GatherServer object
	 * @param server the GatherServer this game will be played on
	 */
	public void setServer(GatherServer server) {
		this.server = server;
	}

	/**Getter for the number of votes required to scramble the teams.
	 * @return the number of votes required to scramble the teams
	 */
	public int getScrambleVotesReq() {
		return scrambleVotesReq;
	}

	/**Setter for the number of votes required to scramble the teams. 
	 * @param scrambleVotesReq the new number of votes required to scramble the teams
	 */
	public void setScrambleVotesReq(int scrambleVotesReq) {
		this.scrambleVotesReq = scrambleVotesReq;
	}

	/**Getter for the current number of scramble votes. 
	 * @return the current number of votes to scramble the teams
	 */
	public int getNumScrambleVotes() {
		return this.scrambleVotes.size();
	}

	/**Getter for the number of votes required to cancel the game.
	 * @return the number of votes required to cancel the game
	 */
	public int getCancelVotesReq() {
		return cancelVotesReq;
	}

	/**Setter for the number of votes required to cancel the teams. 
	 * @param scrambleVotesReq the new number of votes required to cancel the game
	 */
	public void setCancelVotesReq(int scrambleVotesReq) {
		this.cancelVotesReq = scrambleVotesReq;
	}

	/**Getter for the current number of cancel votes. 
	 * @return the current number of votes to cancel the game
	 */
	public int getNumCancelVotes() {
		return this.cancelVotes.size();
	}

	/**Getter for the current round number. 
	 * @return the current round number
	 */
	public int getCurrentRound() {
		return currentRound;
	}

	/**Setter for the current round number. 
	 * @param currentRound the new current round number
	 */
	public void setCurrentRound(int currentRound) {
		this.currentRound = currentRound;
	}

	/**Getter for the current state of the game. e.g pre game, building time or in progress. 
	 * @return the current game state
	 */
	public gameState getCurrState() {
		return currState;
	}

	/**Setter for the current state of the game. 
	 * @param currState the new current state
	 */
	public void setCurrState(gameState currState) {
		this.currState = currState;
	}

	/**Getter for current blue ticket count
	 * @return the current number of blue tickets
	 */
	public int getBlueTickets() {
		return blueTickets;
	}

	/**Setter for current blue ticket count
	 * @param blueTickets the new number of blue tickets
	 */
	public void setBlueTickets(int blueTickets) {
		this.blueTickets = blueTickets;
	}

	/**Getter for current number of red tickets
	 * @return the current number of red tickets
	 */
	public int getRedTickets() {
		return redTickets;
	}

	/**Setter for current number of red tickets
	 * @param redTickets the new number of red tickets
	 */
	public void setRedTickets(int redTickets) {
		this.redTickets = redTickets;
	}

	/**Set the state enum to gameState.BUILDINGTIME
	 */
	public void setStateBuilding() {
		this.setCurrState(gameState.BUILDINGTIME);
		this.startTime = System.nanoTime();
	}
	/**Set the state enum to gameState.ROUNDINPROGRESS
	 */
	public void setStateInProgress() {
		this.setCurrState(gameState.ROUNDINPROGRESS);
	}
	/**Set the state enum to gameState.LASTSTAND
	 */
	public void setStateLastStand() {
		this.setCurrState(gameState.LASTSTAND);
	}
	/**Set the state enum to gameState.ENDED
	 */
	public void setStateEnded() {
		this.setCurrState(gameState.ENDED);
		this.setGameLengthSeconds((int)TimeUnit.SECONDS.convert(System.nanoTime() - this.startTime, TimeUnit.NANOSECONDS));
	}

	/**Getter for game length in seconds
	 * @return the game length in seconds
	 */
	public int getGameLengthSeconds() {
		return gameLengthSeconds;
	}

	/**Setter for game length in seconds
	 * @param gameLengthSeconds the game length in seconds
	 */
	public void setGameLengthSeconds(int gameLengthSeconds) {
		this.gameLengthSeconds = gameLengthSeconds;
	}

	/**Getter for winning team 0 is blue team 1 is red team, -1 is draw, -2 is no result
	 * @return the winning team
	 */
	public int getWinningTeam() {
		return winningTeam;
	}

	/**Setter for winning team 0 is blue team 1 is red team, -1 is draw, -2 is no result
	 * @param winningTeam the team that won the game
	 */
	public void setWinningTeam(int winningTeam) {
		this.winningTeam = winningTeam;
	}

	/**Get a list of player KAG usernames for blue team
	 * @return a List<String> of blue's KAG usernames
	 */
	public List<String> getBlueKagNames() {
		List<String> returnString = new ArrayList<String>();
		for(PlayerObject p : bluePlayerList)
		{
			returnString.add(p.getKagName());
		}
		return returnString;
	}

	/**Get a list of player KAG usernames for red team
	 * @return a List<String> of red's KAG usernames
	 */
	public List<String> getRedKagNames() {
		List<String> returnString = new ArrayList<String>();
		for(PlayerObject p : redPlayerList)
		{
			returnString.add(p.getKagName());
		}
		return returnString;
	}
	
	/**Helper function for getting the list of players in this game. Returns an unmodifiable version of the player list
	 * @return an unmodifiable List<PlayerObject> of the current players in this game
	 */
	public List<PlayerObject> getPlayerList()
	{
		return Collections.unmodifiableList(this.players);
	}
	
	/**Helper function for getting the list of blue players in this game. Returns an unmodifiable version of the blue player list
	 * @return an unmodifiable List<PlayerObject> of the players currently in blue team
	 */
	public List<PlayerObject> getBluePlayerList()
	{
		return Collections.unmodifiableList(this.bluePlayerList);
	}
	
	/**Helper function for getting the list of red players in this game. Returns an unmodifiable version of the red player list
	 * @return an unmodifiable List<PlayerObject> of the players currently in red team
	 */
	public List<PlayerObject> getRedPlayerList()
	{
		return Collections.unmodifiableList(this.redPlayerList);
	}

	public int getPlayerCount()
	{
		return this.getPlayerList().size();
	}

	/**Getter for blue team role associated with this game (for showing teams in members list)
	 * @return the role object
	 */
	public IRole getBlueRole() {
		return blueRole;
	}

	/**Setter for blue team role associated with this game (for showing teams in members list)
	 * @return the role object
	 */
	public void setBlueRole(IRole blueRole) {
		this.blueRole = blueRole;
	}

	/**Getter for red team role associated with this game (for showing teams in members list)
	 * @return the role object
	 */
	public IRole getRedRole() {
		return redRole;
	}

	/**setter for red team role associated with this game (for showing teams in members list)
	 * @return the role object
	 */
	public void setRedRole(IRole redRole) {
		this.redRole = redRole;
	}

	/**Gets the current state as a user readable string. 
	 * @return a string representing the current game state
	 */
	public String getStateString()
	{
		switch(getCurrState())
		{
		case PREGAME:
			return "Pregame";
		case BUILDINGTIME:
			return "Building Time";
		case ROUNDINPROGRESS:
			return "Round In Progress";
		case LASTSTAND:
			return "Last Stand";
		case ENDED:
			return "Game Ended";
		}
		return"";
	}

	/**Returns a formatted string of the teams playing this game
	 * @param guild the guild to use for getting user nicks
	 * @return a string containing a list of both teams with player names formatted as KAG username(Discord Nick)
	 */
	public String toString(IGuild guild)
	{
		if(players.isEmpty()) return "";

		String blueString = "**Blue:** ";
		for(PlayerObject player : bluePlayerList)
		{
			blueString += player.getKagName()+" ("+player.getDiscordUserInfo().getDisplayName(guild)+") ";
		}
		String redString = "**Red:** ";
		for(PlayerObject player : redPlayerList)
		{
			redString += player.getKagName()+" ("+player.getDiscordUserInfo().getDisplayName(guild)+") ";
		}
		return blueString + "\n" + redString;
	}

	/**Checks if the GatherGame's are equal by comparing the server they are being played on
	 * @param obj the object to compare this one with
	 * @return true if the servers are equal, or false if not
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj)
	{
		if(this.getGameID() == -1) return this.server.equals(((GatherGame)obj).server);
		if(this.getGameID() == ((GatherGame)obj).getGameID()) return true;
		return false;
	}

	/**Converts the game to a string as a list of each of the teams using the PlayerObject.toString() method for player name formatting. 
	 * @return the string representation of the teams
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		if(players.isEmpty()) return "";

		String blueString = "**Blue:** ";
		for(PlayerObject player : bluePlayerList)
		{
			blueString += player.toString();
		}
		String redString = "**Red:** ";
		for(PlayerObject player : redPlayerList)
		{
			redString += player.toString();
		}
		return blueString + "\n" + redString;
	}
}
