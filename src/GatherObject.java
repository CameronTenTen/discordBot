import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import sx.blah.discord.Discord4J;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.IVoiceChannel;
import sx.blah.discord.handle.obj.Permissions;
import sx.blah.discord.util.PermissionUtils;

/**This object contains various variables and functions for one gather queue/channel. 
 * @author cameron
 *
 */
public class GatherObject
{
	private GatherQueueObject queue;

	private IGuild guild;
	private IChannel commandChannel = null;
	private IRole adminRole = null;
	private IRole queueRole = null;
	private IRole softQueueRole = null;
	private IVoiceChannel blueVoiceChannel = null;
	private IVoiceChannel redVoiceChannel = null;
	private IVoiceChannel generalVoiceChannel = null;
	private IChannel scoreReportChannel = null;
	private IMessage scoreboardMessage = null;
	public long guildID = 0L;
	public String commandChannelString = "";
	public long commandChannelID = 0L;
	public long blueVoiceID = 0L;
	public long redVoiceID = 0L;
	public long generalVoiceID = 0L;
	public long scoreReportID = 0L;
	public long adminRoleID = 0L;
	public long queueRoleID = 0L;
	public long softQueueRoleID = 0L;
	public long scoreboardMessageID = 0L;
	public long scoreboardChannelID = 0L;

	public SubManager substitutions = null;

	public Set<GatherServer> servers;

	private List<GatherGame> runningGames;

	GatherObject()
	{
		queue = new GatherQueueObject();
		servers = new HashSet<GatherServer>();
		runningGames = new ArrayList<GatherGame>();
		substitutions = new SubManager(this);
	}

	/**
	 * Function to be called when the bot is ready for setup. Initialises all the Discord4J related objects such as channels and roles. Also initialises the scoreboard. 
	 */
	public void setDiscordObjects()
	{
		setGuild(DiscordBot.client.getGuildByID(guildID));
		if(guild == null)
		{
			Discord4J.LOGGER.error("Could not find guild with id: "+guildID);
			return;
		}

		setCommandChannel(DiscordBot.client.getChannelByID(commandChannelID));
		setScoreReportChannel(DiscordBot.client.getChannelByID(scoreReportID));
		setBlueVoiceChannel(DiscordBot.client.getVoiceChannelByID(blueVoiceID));
		setRedVoiceChannel(DiscordBot.client.getVoiceChannelByID(redVoiceID));
		setGeneralVoiceChannel(DiscordBot.client.getVoiceChannelByID(generalVoiceID));
		setAdminRole(DiscordBot.client.getRoleByID(adminRoleID));
		setQueueRole(DiscordBot.client.getRoleByID(queueRoleID));
		setSoftQueueRole(DiscordBot.client.getRoleByID(softQueueRoleID));

		if(scoreboardChannelID!=0 && scoreboardMessageID==0)
		{
			IChannel chan = DiscordBot.client.getChannelByID(scoreboardChannelID);
			if(chan == null)
			{
				Discord4J.LOGGER.warn("Error getting scoreboard channel, null returned");
				return;
			}
			scoreboardMessageID = chan.sendMessage("scoreboard").getLongID();
			setScoreboardMessage(DiscordBot.client.getMessageByID(scoreboardMessageID));
			this.updateScoreboard();
			System.out.println("new scoreboard message has been created, please enter the message id in the config or a scoreboard will be created each time the bot starts: "+scoreboardMessageID);
		}
		else
		{
			setScoreboardMessage(DiscordBot.client.getMessageByID(scoreboardMessageID));
		}

		if(this.getScoreboardMessage()!=null)this.updateScoreboard();

		//no command channel found
		if(commandChannel==null) System.out.println("Error: no command channel found for guild: "+guild.getName());
	}

	/**
	 * @return the Discord guild this gather object is associated with. 
	 */
	public IGuild getGuild() {
		return guild;
	}

	/**
	 * @param guild the Discord guild to associate this gather object with. 
	 */
	public void setGuild(IGuild guild)
	{
		if(guild == null) Discord4J.LOGGER.warn("guild is being set as null");
		this.guild = guild;
	}

	/**
	 * @return the Discord channel this gather object is associated with.
	 */
	public IChannel getCommandChannel() {
		return commandChannel;
	}

	/**
	 * @param commandChannel the Discord channel to associate this gather object with.
	 */
	public void setCommandChannel(IChannel commandChannel) {
		if(commandChannel == null) Discord4J.LOGGER.warn("command channel is being set as null");
		this.commandChannel = commandChannel;
	}

	/**
	 * @return the Discord channel where the bot puts score reports at the end of each game. 
	 */
	public IChannel getScoreReportChannel() {
		return scoreReportChannel;
	}

	/**
	 * @param scoreReportChannel the Discord channel where the bot should put score reports at the end of each game. 
	 */
	public void setScoreReportChannel(IChannel scoreReportChannel) {
		if(scoreReportChannel == null) Discord4J.LOGGER.warn("score report channel is being set as null");
		this.scoreReportChannel = scoreReportChannel;
	}

	/**
	 * @return the Discord role of gather admins for using admin commands
	 */
	public IRole getAdminRole() {
		return adminRole;
	}

	/**
	 * @param adminRole the Discord role that should be used as the admin role for using admin commands
	 */
	public void setAdminRole(IRole adminRole) {
		if(adminRole == null) Discord4J.LOGGER.warn("admin role is being set as null");
		this.adminRole = adminRole;
	}

	/**
	 * @return the queue role for displaying the gather queue in the members list
	 */
	public IRole getQueueRole() {
		return queueRole;
	}

	/**
	 * @param queueRole the role that should be used for displaying the current queue in the members list
	 */
	public void setQueueRole(IRole queueRole) {
		if(queueRole == null) Discord4J.LOGGER.warn("queue role is being set as null");
		this.queueRole = queueRole;
	}

	/**
	 * @return the soft queue role for allowing people express interest in a game without committing
	 */
	public IRole getSoftQueueRole() {
		return softQueueRole;
	}

	/**
	 * @param softQueueRole the role that should be used for displaying the current soft queue in the members list
	 */
	public void setSoftQueueRole(IRole softQueueRole) {
		if(softQueueRole == null) Discord4J.LOGGER.warn("queue role is being set as null");
		this.softQueueRole = softQueueRole;
	}

	/**
	 * @return the voice channel for blue team
	 */
	public IVoiceChannel getBlueVoiceChannel() {
		return blueVoiceChannel;
	}

	/**
	 * @param blueVoiceChannel the voice channel that should be used for blue team voice chat
	 */
	public void setBlueVoiceChannel(IVoiceChannel blueVoiceChannel) {
		if(blueVoiceChannel == null) Discord4J.LOGGER.warn("blue voice channel is being set as null");
		this.blueVoiceChannel = blueVoiceChannel;
	}

	/**
	 * @return the voice channel for red team
	 */
	public IVoiceChannel getRedVoiceChannel() {
		return redVoiceChannel;
	}

	/**
	 * @param blueVoiceChannel the voice channel that should be used for red team voice chat
	 */
	public void setRedVoiceChannel(IVoiceChannel redVoiceChannel) {
		if(redVoiceChannel == null) Discord4J.LOGGER.warn("red voice channel is being set as null");
		this.redVoiceChannel = redVoiceChannel;
	}

	/**
	 * @return the general voice channel used for before/after game chat
	 */
	public IVoiceChannel getGeneralVoiceChannel() {
		return generalVoiceChannel;
	}

	/**
	 * @param generalVoiceChannel the general voice channel used for before/after game chat
	 */
	public void setGeneralVoiceChannel(IVoiceChannel generalVoiceChannel) {
		if(generalVoiceChannel == null) Discord4J.LOGGER.warn("general voice channel is being set as null");
		this.generalVoiceChannel = generalVoiceChannel;
	}

	/**
	 * @return the IMessage used to display the scoreboard
	 */
	public IMessage getScoreboardMessage() {
		return scoreboardMessage;
	}

	/**
	 * @param scoreboardMessage the IMessage that should be used to display the scoreboard
	 */
	public void setScoreboardMessage(IMessage scoreboardMessage) {
		this.scoreboardMessage = scoreboardMessage;
	}

	/**Check if a player has the admin role. 
	 * @param user the user to check
	 * @return true if one of their roles matches the admin role, false otherwise
	 */
	public boolean isAdmin(IUser user)
	{
		List<IRole> roles = user.getRolesForGuild(this.guild);
		for(IRole role : roles)
		{
			if(role.equals(this.getAdminRole()))
			{
				return true;
			}
		}
		return false;
	}

	/**Function for checking if the bot has the appropriate permissions to set the a particular role. The highest bot permission must be greater than the permission it is trying to set. 
	 * @param role the role to check
	 * @return false if the bot does not have the necessary permission, true otherwise
	 * @see PermissionUtils#hasPermissions(IGuild, IUser, Permissions...)
	 */
	public boolean canSetRole(IRole role)
	{
		if(role == null)
		{
			return false;
		}
		if(!PermissionUtils.hasPermissions(getGuild(), DiscordBot.client.getOurUser(), Permissions.MANAGE_ROLES))
		{
			Discord4J.LOGGER.warn("bot does not have MANAGE_ROLES permission, some functionality may be lost");
			return false;
		}
		return true;
	}

	/**Gets a string representing the specified Discord user. Formatted as DisplayName(Username#Discriminator).
	 * @param user the PlayerObject to convert to string
	 * @return a string representing the users name, formatted as DisplayName(Username#Discriminator)
	 */
	public String fullUserString(IUser user)
	{
		return user.getDisplayName(getGuild()) + "(" + user.getName() + "#" + user.getDiscriminator() + ")";
	}

	/**Gets a string representing the specified Discord user. Formatted as DisplayName(Username#Discriminator).
	 * @param player the PlayerObject to convert to string
	 * @return a string representing the users name, formatted as DisplayName(Username#Discriminator)
	 * @see #fullUserString(IUser)
	 */
	public String fullUserString(PlayerObject player)
	{
		return fullUserString(player.getDiscordUserInfo());
	}

	/**Gets a string representing the specified gather player. Formatted as KAGName(DiscordNick). 
	 * @param player the player to convert to string. 
	 * @param currGuild the guild to use for getting the players discord nick
	 * @return a string representing the users name, formatted as KAGName(DiscordNick)
	 */
	public String playerString(PlayerObject player, IGuild currGuild)
	{
		return player.getKagName()+" ("+player.getDiscordUserInfo().getDisplayName(currGuild)+")";
	}

	/**Adds a player to the gather queue. 
	 * @return 0 if the player already in queue or something else went wrong, 1 if the player added to the queue, 2 if player added to queue and the queue is now full, or 4 if the player added after the queue is already full
	 */
	public int addToQueue(PlayerObject player)
	{
		if(isInGame(player))
		{
			return 3;
		}
		if(queue.add(player))
		{
			updateChannelCaption();
			DiscordBot.addRole(player.getDiscordUserInfo(), getQueueRole());
			if(isQueueFull())
			{
				return 2;
			}
			return 1;
		}
		if(isQueueFull())
		{
			return 4;
		}
		return 0;

	}

	/**Wrapper function for adding a player to the queue by Discord user. 
	 * @param user the discord user to add
	 * @return 0 if the player already in queue or something else went wrong, 1 if the player added to the queue, 2 if player added to queue and the queue is now full, 4 if the player added after the queue is already full, or -1 if an error occured getting the player object
	 * @see #addToQueue(PlayerObject)
	 */
	public int addToQueue(IUser user)
	{
		PlayerObject player = DiscordBot.players.getObject(user);
		//player is null if they are not linked
		if(player==null) return -1;
		return this.addToQueue(player);
	}

	/**Removes a player from the gather queue. 
	 * @param player to remove from the queue
	 * @return 1 if the player was removed from the queue, 0 otherwise
	 */
	public int remFromQueue(PlayerObject player)
	{
		if(queue.remove(player))
		{
			updateChannelCaption();
			DiscordBot.removeRole(player.getDiscordUserInfo(), getQueueRole());
			return 1;
		}
		else
		{
			return 0;
		}
	}

	/**Wrapper function for removing a discord user from the queue
	 * @param user the user to be removed from the queue
	 * @return 1 if the player was removed from the queue, -1 if the user wasnt found, 0 otherwise
	 * @see #remFromQueue(PlayerObject)
	 */
	public int remFromQueue(IUser user)
	{
		PlayerObject player = DiscordBot.players.getObject(user);
		//player is null if they are not linked
		if(player==null) return -1;
		return this.remFromQueue(player);
	}

	/**Checks all the currently running games for the specified player, returns the GatherGame object if the player is found, or null otherwise
	 * @param player the player to search for
	 * @return the GatherGame that they are playing in
	 * @see #GatherGame
	 */
	public GatherGame getPlayersGame(PlayerObject player)
	{
		if(player==null) return null;
		for(GatherGame game : runningGames)
		{
			if(game.isPlayerPlaying(player)) return game;
		}
		return null;
	}

	/**Wrapper for getting a players game from user object
	 * @param user the user to search for
	 * @return the GatherGame that they are playing, or null if the player couldnt be found
	 * @see #getPlayersGame(PlayerObject)
	 */
	public GatherGame getPlayersGame(IUser user)
	{
		return getPlayersGame(DiscordBot.players.getObject(user));
	}

	/**Wrapper of getPlayersGame() that returns true if they are found, or false otherwise
	 * @param player the player to search for
	 * @return false if getPlayersGame() returned null, false otherwise
	 * @see #getPlayersGame(PlayerObject)
	 */
	public boolean isInGame(PlayerObject player)
	{
		GatherGame game = getPlayersGame(player);
		if(game==null) return false;
		else return true;
	}

	/**
	 * Helper function for updating the channel name to reflect the queue size and set the playing text based on queue size.
	 */
	public void updateChannelCaption()
	{
		DiscordBot.setPlayingText(this.numPlayersInQueue()+"/"+this.getMaxQueueSize()+" in queue");
		DiscordBot.setChannelCaption(this.getGuild(), this.getCommandChannel(), this.numPlayersInQueue()+"-in-q"+ "_" + this.commandChannelString);
	}

	/**Function for adding a vote to scramble the teams for the game the player is currently in. 
	 * @param player the player making the vote
	 */
	public void addScrambleVote(PlayerObject player)
	{
		GatherGame game = this.getPlayersGame(player);
		if(game==null)
		{
			DiscordBot.sendMessage(this.getCommandChannel(), "There is **no game** to scramble "+player.getDiscordUserInfo().getDisplayName(this.getGuild())+"!");
			return;
		}
		if(!game.getCurrState().equals(GatherGame.gameState.PREGAME))
		{
			DiscordBot.sendMessage(this.getCommandChannel(), "You cannot vote to scramble once the **game has started** "+player.getDiscordUserInfo().getDisplayName(this.getGuild())+"!");
			return;
		}
		int returnVal = game.addScrambleVote(player);
		switch(returnVal)
		{
		case 0:
			DiscordBot.sendMessage(getCommandChannel(), "Teams have been shuffled!", true);
			DiscordBot.sendMessage(getCommandChannel(), "__**Blue**__: "+game.blueMentionList().toString());
			DiscordBot.sendMessage(getCommandChannel(), "__**Red**__:  "+game.redMentionList().toString());
			Discord4J.LOGGER.info("Teams shuffled: "+game.blueMentionList().toString()+game.redMentionList().toString());
			this.sortTeamRoomsAfterShuffle();
			this.removePlayerTeamRoles(game);
			this.addPlayersToTeamRoles(game);
			return;
		case -1:
			DiscordBot.sendMessage(getCommandChannel(), "You have already voted to scramble the teams "+player.getDiscordUserInfo().getNicknameForGuild(getGuild())+"("+game.getNumScrambleVotes()+"/"+game.getScrambleVotesReq()+")");
			return;
		}
		DiscordBot.sendMessage(getCommandChannel(), "**Vote to scramble** teams has been counted for "+player.getDiscordUserInfo().getNicknameForGuild(getGuild())+" ("+returnVal+"/"+game.getScrambleVotesReq()+")");
	}

	/**Function for doing everything needed to start a gather game. 
	 * <p>
	 * This first gets a free server and sets it in use, then creates a gather game with the current queue and shuffles the teams, then adds the game to the list of running games, 
	 * then sends appropriate messages to discord and the KAG server, then clears the queue and moves players into their team rooms
	 * TODO: make this function not block for 5 seconds while the blocking movePlayersIntoTeamRooms(5) is called
	 */
	public int startGame()
	{
		GatherServer server = this.getFreeServer();
		if(server == null)
		{
			DiscordBot.sendMessage(getCommandChannel(), "There are currently **no servers** to play on! A game will be **started when** a server becomes **available**!");
			return -1;
		}
		//setup the game
		List<PlayerObject> list = queue.asList();
		server.setInUse(true);
		GatherGame game = new GatherGame(DiscordBot.database.reserveGameId(), list, null, null, server, null, null);
		game.shuffleTeams();
		runningGames.add(game);
		//reset the queue
		this.clearQueue();

		//announce the game
		//do the team messages in separate lines so that it highlights the players team
		String passwordString = server.getServerPassword();
		if(passwordString!=null && !passwordString.isEmpty()) passwordString = " with password "+passwordString;
		DiscordBot.sendMessage(getCommandChannel(), "Gather game #"+game.getGameID()+" starting on "+server.getServerName()+passwordString, true);
		if(server.getServerLink()!=null && server.getServerLink()!="") DiscordBot.sendMessage(getCommandChannel(), server.getServerLink());
		DiscordBot.sendMessage(getCommandChannel(), "__**Blue**__: "+game.blueMentionList().toString());
		DiscordBot.sendMessage(getCommandChannel(), "__**Red**__:  "+game.redMentionList().toString());
		Discord4J.LOGGER.info("Game started: "+game.blueMentionList().toString()+game.redMentionList().toString());
		game.sendTeamsToServer();
		//create the team roles
		this.generateAndSetTeamRoles(game);
		//put the players into the team roles
		this.addPlayersToTeamRoles(game);
		//do voice channel stuff
		movePlayersIntoTeamRooms(5);
		//send private messages last so they dont cause other things to be rate limited
		List<PlayerObject> blue = game.getBluePlayerList();
		List<PlayerObject> red = game.getRedPlayerList();
		for(PlayerObject p : blue)
		{
			DiscordBot.sendMessage(DiscordBot.getPMChannel(p.getDiscordUserInfo()), "Gather game #"+game.getGameID()+" is starting and you are on the **Blue** team");
		}
		for(PlayerObject p : red)
		{
			DiscordBot.sendMessage(DiscordBot.getPMChannel(p.getDiscordUserInfo()), "Gather game #"+game.getGameID()+" is starting and you are on the **Red** team");
		}
		return 0;
	}

	/**Gets the GatherGame object for the currently running game on the specified server. 
	 * @param serverIp the ip address of the server
	 * @param serverPort the port of the server
	 * @return the GatherGame object currently running on the server
	 * @see #GatherGame
	 */
	public GatherGame getRunningGame(String serverIp, int serverPort)
	{
		for(GatherGame game : runningGames)
		{
			if(game.getServerIp().equals(serverIp) && game.getServerPort() == serverPort)
			{
				return game;
			}
		}
		return null;
	}

	/**Get a gather game object using the id of the game. 
	 * @param id the id to search for
	 * @return the first GatherGame object found in the runningGames list that has the id, null if none found
	 * @see #GatherGame
	 */
	public GatherGame getRunningGame(int id)
	{
		for(GatherGame game : runningGames)
		{
			if(game.getGameID() == id)
			{
				return game;
			}
		}
		return null;
	}

	/**Returns an unmodifiable version of the running games list. 
	 * @return unmodifiable list of currently running games
	 * @see #GatherGame
	 */
	public List<GatherGame> getRunningGames()
	{
		return Collections.unmodifiableList(this.runningGames);
	}

	/**Function for checking if the this GatherObject has any currently running games. 
	 * @return false if the size of the runningGames list is less than or equal to 0, true otherwise
	 */
	public boolean hasRunningGames()
	{
		if(this.getRunningGames().size()<=0) return false;
		return true;
	}

	/**Helper function for ending a game by resetting the appropriate server object variables, removing the game from the list of running games, clearing all subs for the game, and clearing team roles. 
	 * @param game the game that is ending
	 */
	public void setGameEnded(GatherGame game)
	{
		GatherServer server = game.getServer();
		if(server !=null)
		{
			server.clearGame();
			server.setInUse(false);
		}
		runningGames.remove(game);
		substitutions.clearGame(game);
		
		//remove the team role from the players
		this.removePlayerTeamRoles(game);
		this.deleteTeamRoles(game);
	}

	/**Helper function for doing all the things necessary at the end of a game. 
	 * @param game the GatherGame object that represents the game that has ended
	 * @param winningTeam the team that won the game, 0 for blue, 1 for red
	 * @return returns true
	 */
	public boolean endGame(GatherGame game, int winningTeam)
	{
		//set the game state as ended
		game.setStateEnded();
		if(winningTeam>=-1 && winningTeam<=1)
		{
			//print to score report
			String temp1 = game.blueMentionList().toString();
			if(winningTeam==0) temp1 += " +1";
			else if (winningTeam==1) temp1 += " -1";
			else temp1 += " 0";
			String temp2 = game.redMentionList().toString();
			if(winningTeam==1) temp2 += " +1";
			else if (winningTeam==0) temp2 += " -1";
			else temp2 += " 0";
			DiscordBot.sendMessage(getScoreReportChannel(), temp1);
			DiscordBot.sendMessage(getScoreReportChannel(), temp2);
			//store stats in database
			game.setWinningTeam(winningTeam);
			game.saveResultToDB(this.substitutions);
			this.updateScoreboard();
		}
		//tell everyone
		DiscordBot.sendMessage(getCommandChannel(), "Game #"+game.getGameID()+" has ended, "+teamString(winningTeam));
		//remove game object from list
		if(game.getServer() == null)
		{
			//THIS IS A WORKAROUND FOR WHEN WE HAVE NO SERVER LIST AND THERE IS ONLY 1 GAME AT A TIME
			Discord4J.LOGGER.warn("Server is null when giving win, clearing running games (if there is more than 1 running game this is a problem)");
			clearGames();
		}
		else
		{
			setGameEnded(game);
		}
		//do voice channel stuff
		movePlayersOutOfTeamRooms(5);
		//check if there is enough people in queue to start another game (after waiting for the channels to settle)
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
		}
		if(this.isQueueFull()) this.startGame();
		return true;
	}

	/**Wrapper function for ending a game using the server information. 
	 * @param serverIp the ip address of the server where a game has ended
	 * @param serverPort the port of the server where a game has ended
	 * @param winningTeam the team that won the game
	 * @return returns false if the GatherGame object was not found, true otherwise
	 * @see #endGame(GatherGame, int)
	 */
	public boolean endGame(String serverIp, int serverPort, int winningTeam)
	{
		GatherGame game = getRunningGame(serverIp, serverPort);
		if(game == null) return false;
		this.endGame(game, winningTeam);
		return true;
	}

	/**Wrapper function for ending a game using the match id
	 * @param matchid the id of the game to end
	 * @param winningTeam the team that won the game
	 * @return false if the GatherGame object was not found, true otherwise
	 * @see #endGame(GatherGame, int)
	 */
	public boolean endGame(int matchid, int winningTeam)
	{
		GatherGame game = getRunningGame(matchid);
		if(game == null) return false;
		this.endGame(game, winningTeam);
		return true;
	}

	/**Function for updating the scoreboard message with the stats retreived from the database. 
	 */
	public void updateScoreboard()
	{
		if(this.getScoreboardMessage()==null)
		{
			Discord4J.LOGGER.warn("Scoreboard not set!");
			return;
		}
		List<StatsObject> list = DiscordBot.database.getTopPlayers(20);
		if(list == null)
		{
			Discord4J.LOGGER.warn("Failed to get scoreboard data from the database!");
			return;
		}
		String scoreboardString="**Scoreboard:**\n```  |      KAG name      |  Win % |Games| Score\n";
		int i=0;
		for(StatsObject stats : list)
		{
			i++;
			scoreboardString+=i;
			if(i<10)scoreboardString+=" ";
			scoreboardString+="|";
			//centre the kagname in the column
			if(stats.kagname.length()%2 != 0)
			{
				stats.kagname = stats.kagname + " ";
			}
			for(int j = stats.kagname.length()/2; j < 10; j++)
			{
				scoreboardString+=" ";
			}
			scoreboardString=scoreboardString+stats.kagname;
			for(int j = stats.kagname.length()/2; j < 10; j++)
			{
				scoreboardString+=" ";
			}
			scoreboardString+="|";
			scoreboardString=scoreboardString+" "+stats.winRateString()+"%";
			if(stats.winRateString().length()<6)scoreboardString+=" ";
			scoreboardString=scoreboardString+"| "+stats.gamesplayed;
			if(stats.gamesplayed<100)scoreboardString+=" ";
			scoreboardString=scoreboardString+" | "+stats.mmr;
			scoreboardString=scoreboardString+"\n";
		}
		scoreboardString+="```";
		if(scoreboardString.length()>2000) Discord4J.LOGGER.warn("SCOREBOARD IS TOO LARGE: "+scoreboardString.length());
		this.getScoreboardMessage().edit(scoreboardString);
	}

	/**Gets a server from the server list that is not in use. 
	 * @return a GatherServer object representing the server that can be used
	 * @see #GatherServer
	 */
	public GatherServer getFreeServer()
	{
		// TODO make some kind of server priority? in case of high/low ping servers?
		// TODO only get servers that the bot is currently connected to (will then also need to check if the queue is full on server (re)connect) (probably want to implement better heartbeat/reconnect system before this)
		// not important now as there should only be 1 server anyway
		for(GatherServer server : servers)
		{
			if (!server.isInUse()) {
				return server;
			}
		}
		return null;
	}

	/**Gets the GatherServer object for the requested server. 
	 * @param ip the ip address of the server to find
	 * @param port the port of the server to find
	 * @return the GatherServer object if it was found, null otherwise
	 * @see #GatherServer
	 */
	public GatherServer getServer(String ip, int port)
	{
		for(GatherServer server : servers)
		{
			if(server.getIp().equals(ip) && server.getPort() == port)
			{
				return server;
			}
		}
		return null;
	}

	/**Helper function for establishing the TCPR connection with all the gather KAG servers. 
	 * Says a message in discord for each server it connects or fails to connect with. 
	 * @see GatherServer#connect()
	 */
	public void connectKAGServers()
	{
		for(GatherServer server : servers)
		{
			if(server.connect()) DiscordBot.sendMessage(this.getCommandChannel(), "Connected to "+server.getIp()+":"+server.getPort());
			else DiscordBot.sendMessage(this.getCommandChannel(), "Failed to connect to "+server.getIp()+":"+server.getPort());
		}
	}

	/**Helper function for disconnecting all TCPR connections with the gather KAG servers. 
	 * If the bot thinks the server was already connected, it will say a message in the discord channel that it has disconnected. 
	 * @see GatherServer#disconnect()
	 */
	public void disconnectKAGServers()
	{
		for(GatherServer server : servers)
		{
			if(server.isConnected())
			{
				server.disconnect();
				DiscordBot.sendMessage(this.getCommandChannel(), "Disconnected from "+server.getIp()+":"+server.getPort());
			}
			//disconnect anyway in case isConnected is wrong, but don't talk about it
			server.disconnect();
		}
	}

	/**Function called when an end building time message is received from a gather KAG server. 
	 * @param ip the ip address of the server
	 * @param port the port of the server
	 */
	public void setBuildingTimeEnded(String ip, int port)
	{
		GatherGame game = this.getRunningGame(ip, port);
		if(game==null) return;
		game.setStateInProgress();
		DiscordBot.sendMessage(this.getCommandChannel(), "Building time ended for game #"+game.getGameID());
	}

	/**Function called when a start building time message is received from a gather KAG server. 
	 * @param ip the ip address of the server
	 * @param port the port of the server
	 */
	public void setRoundStarted(String ip, int port)
	{
		GatherGame game = this.getRunningGame(ip, port);
		if(game==null) return;
		game.setStateBuilding();
		DiscordBot.sendMessage(this.getCommandChannel(), "Building time started for game #"+game.getGameID());
	}

	/**Adds a sub request for a player from server info. 
	 * @param kagName the KAG username of the player to be subbed
	 * @param ip the ip address of the server the request was made on
	 * @param port the port of the server the request was made on
	 * @return -1 if no game was found or if the player isnt playing on the server, 1 if a sub was requested
	 * @see SubManager#addSubRequest(PlayerObject, GatherGame)
	 */
	public int addSubRequest(String kagName, String ip, int port)
	{
		PlayerObject playerToBeSubbed = DiscordBot.players.getObject(kagName);

		GatherGame game = this.getRunningGame(ip, port);
		if(game==null)
		{
			//should never get here because the server only sends the request if there is a game running
			this.getServer(ip, port).say("An error occured adding sub request for "+kagName+", a game isn't running?");
			return -1;
		}
		int returnVal = substitutions.addSubRequest(playerToBeSubbed, game);
		if(returnVal==-1)
		{
			this.getServer(ip, port).say("An error occured adding sub request for "+kagName+", this player isn't playing?");
		}
		else if(returnVal==1)
		{
			this.getServer(ip, port).say("Sub request added for player "+kagName+", use !sub "+game.getGameID()+" in Discord to sub into their place!");
			DiscordBot.sendMessage(this.getCommandChannel(), "**Sub request** added for " + playerToBeSubbed.getMentionString() + " use **!sub "+game.getGameID()+"** to sub into their place! ("+this.getQueueRole().mention()+")");
		}
		return returnVal;
	}

	/**Adds a vote to sub a player from the server. 
	 * @param votedFor the player being voted to be subbed
	 * @param voting the player voting for the sub
	 * @param ip the ip address of the server the vote was placed on
	 * @param port the port of the server the vote was placed on
	 * @return -1 if the player voting is not in the game, -2 if player voted for not found or not in the game, 
	 * -3 if the server is not found or the players weren't both in the the right game, -4 if the player is already being subbed, 
	 * -5 if the player voting has already voted to sub this player, 0 if the a sub request was successfully added, 
	 * or any positive number representing the number of votes for this player after this vote was added
	 */
	public int addSubVote(String votedFor, String voting, String ip, int port)
	{
		PlayerObject playerVotedFor = DiscordBot.players.getObject(votedFor);
		PlayerObject playerVoting = DiscordBot.players.getObject(voting);

		GatherGame game = this.getRunningGame(ip, port);
		if(playerVotedFor==null)
		{
			this.getServer(ip, port).say("An error occured adding sub vote for "+votedFor+", a linked player with this username could not be found");
			return -2;
		}
		if(game==null)
		{
			//should never get here because the server only sends the request if there is a game running
			this.getServer(ip, port).say("An error occured adding sub vote for "+playerVotedFor.getKagName()+", a game isn't running?");
			return -3;
		}
		int returnVal = substitutions.addSubVote(playerVotedFor, playerVoting);
		switch(returnVal)
		{
		case -1:
		case -2:
		case -3:
			this.getServer(ip, port).say("You and the player you are voting for must be in the same game " + voting + "!");
			return returnVal;
		case -4:
			this.getServer(ip, port).say(playerVotedFor.getKagName() + " is already being subbed " + voting + "!");
			return returnVal;
		case -5:
			this.getServer(ip, port).say("You have already voted to sub " + playerVotedFor.getKagName() + ", " + voting + "!");
			return returnVal;
		case 0:
			Discord4J.LOGGER.info("sub requested for: "+this.fullUserString(playerVotedFor));
			this.getServer(ip, port).say("Sub request added for "+playerVotedFor.getKagName()+", use !sub "+game.getGameID()+" in Discord to sub into their place!");
			DiscordBot.sendMessage(this.getCommandChannel(), "**Sub request** added for " + playerVotedFor.getMentionString() + " use **!sub "+game.getGameID()+"** to sub into their place! ("+this.getQueueRole().mention()+")");
			return returnVal;
		}
		//gets here if returnVal is greater than 0 which means the sub vote was added and the number is the vote count
		//dont put this in case statement because that could cause issues if we changed the number of votes required
		this.getServer(ip, port).say("Vote to sub " + playerVotedFor.getKagName() + " has been counted for " + voting + " (" + returnVal +"/"+ this.substitutions.getSubVotesRequired() +")");
		DiscordBot.sendMessage(this.getCommandChannel(), "Vote to sub " + playerVotedFor.getMentionString() + " has been counted for " + playerVoting.toString() + " (" + returnVal +"/"+ this.substitutions.getSubVotesRequired() +")");
		return returnVal;
	}

	/**Helper function for removing all players in the queue role from that role. Used when the bot connects in case they the queue was not removed before the bot left. 
	 */
	public void clearQueueRole()
	{
		List<IUser> list = getGuild().getUsersByRole(getQueueRole());
		for(IUser user : list)
		{
			DiscordBot.removeRole(user, getQueueRole());
		}
	}

	/**Function for removing all players from the queue e.g. when a game starts. 
	 */
	public void clearQueue()
	{
		for(PlayerObject player : queue)
		{
			DiscordBot.removeRole(player.getDiscordUserInfo(), getQueueRole());
		}
		queue.clear();
		updateChannelCaption();
	}
	
	/**Helper function for setting a player as interested. This means that they get the soft queue role. 
	 * @param user
	 */
	public void setInterested(IUser user) {
		DiscordBot.addRole(user, this.getSoftQueueRole());
	}
	
	/**Helper function for setting a player as not interested. This means that they get the soft queue role role removed. 
	 * @param user
	 */
	public void setNotInterested(IUser user) {
		DiscordBot.removeRole(user, this.getSoftQueueRole());
	}
	
	/**Toggles the users current interested state. Checks if they currently have the interested role, and removes them from it if they do, returning 2.
	 * If they do not already have the role, they are given the role and removed from the queue. The result of {@link GatherObject#remFromQueue(IUser)} is reuturned. 
	 * 1 if the player was removed, -1 if the user wasn't found (only if they are not linked?) or 0 otherwise. 
	 * @param user
	 * @return 2 if the user was changed to not interested, 1 if the player was set to interested and removed from the queue, -1 if the player wasnt found, 0 otherwise
	 */
	public int toggleInterested(IUser user) {
		//check if the player has the role
		List<IRole> roles = user.getRolesForGuild(this.getGuild());
		for(IRole role : roles)
		{
			if(role.equals(this.getSoftQueueRole()))
			{
				//if they have the role, remove them from it
				setNotInterested(user);
				return 2;
			}
		}
		//otherwise give them the role
		setInterested(user);
		return this.remFromQueue(user);
	}

	/**Function for clearing all running games and ending them. 
	 */
	public void clearGames()
	{
		while(!runningGames.isEmpty())
		{
			setGameEnded(runningGames.get(0));
		}
	}

	/**Helper function for moving all players out of the general voice channel into their team channels. 
	 */
	public void movePlayersIntoTeamRooms()
	{
		IVoiceChannel general = this.getGeneralVoiceChannel();
		IVoiceChannel blue = this.getBlueVoiceChannel();
		IVoiceChannel red = this.getRedVoiceChannel();

		List<IUser> users = general.getConnectedUsers();
		for(IUser user : users)
		{
			GatherGame game = this.getPlayersGame(user);
			if(game!=null)
			{
				int team = game.getPlayerTeam(user);
				if(team==0)
				{
					DiscordBot.moveToVoiceChannel(user, blue);
				}
				else if(team==1)
				{
					DiscordBot.moveToVoiceChannel(user, red);
				}
			}
		}
	}

	/**Wrapper function for moving all players out of the general voice channel into their team channels. 
	 * @param delay the number of seconds to delay for before moving
	 * @see #movePlayersIntoTeamRooms()
	 */
	public void movePlayersIntoTeamRooms(int delay)
	{
		String countString = "Moving channels in ";
		IMessage countMsg = DiscordBot.sendMessage(this.getCommandChannel(), countString+delay);

		try {
			Thread.sleep(1000);
			for(int i=delay-1;i>0;i--)
			{
				DiscordBot.editMessage(countMsg,countString+"**"+i+"**");
				Thread.sleep(1000);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		movePlayersIntoTeamRooms();
		countMsg.delete();
	}

	/**Helper function for moving all players out of their team channels into the general voice channel. 
	 */
	public void movePlayersOutOfTeamRooms()
	{
		IVoiceChannel general = this.getGeneralVoiceChannel();
		IVoiceChannel blue = this.getBlueVoiceChannel();
		IVoiceChannel red = this.getRedVoiceChannel();

		List<IUser> users;
		users = blue.getConnectedUsers();
		for( IUser user : users)
		{
			DiscordBot.moveToVoiceChannel(user, general);
		}
		users = red.getConnectedUsers();
		for( IUser user : users)
		{
			DiscordBot.moveToVoiceChannel(user, general);
		}

	}

	/**Wrapper function for moving all players out of their team voice channel into the general voice channel. 
	 * @param delay the number of seconds to delay for before moving
	 * @see #movePlayersIntoTeamRooms()
	 */
	public void movePlayersOutOfTeamRooms(int delay)
	{
		String countString = "Moving channels in ";
		IMessage countMsg = DiscordBot.sendMessage(this.getCommandChannel(), countString+delay);

		try {
			Thread.sleep(1000);
			for(int i=delay-1;i>0;i--)
			{
				DiscordBot.editMessage(countMsg, countString+i);
				Thread.sleep(1000);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		movePlayersOutOfTeamRooms();
		countMsg.delete();
	}

	/**Helper function for sending players to the correct voice channel after the teams have been shuffled
	 */
	public void sortTeamRoomsAfterShuffle()
	{
		IVoiceChannel blue = this.getBlueVoiceChannel();
		IVoiceChannel red = this.getRedVoiceChannel();

		movePlayersIntoTeamRooms();
		List<IUser> users;
		users = blue.getConnectedUsers();
		for( IUser user : users)
		{
			GatherGame game = this.getPlayersGame(user);
			if(game.getPlayerTeam(user)==1)
			{
				DiscordBot.moveToVoiceChannel(user, red);
			}
		}
		users = red.getConnectedUsers();
		for( IUser user : users)
		{
			GatherGame game = this.getPlayersGame(user);
			if(game.getPlayerTeam(user)==0)
			{
				DiscordBot.moveToVoiceChannel(user, blue);
			}
		}

	}

	private void generateAndSetTeamRoles(GatherGame game) {
		//check there isnt already roles that should be deleted
		if(game.getBlueRole() != null && !game.getBlueRole().isDeleted()) DiscordBot.deleteRole(game.getBlueRole());
		if(game.getRedRole() != null && !game.getRedRole().isDeleted()) DiscordBot.deleteRole(game.getRedRole());
		
		//create the new roles
		IRole blue = DiscordBot.createRole(this.getGuild());
		IRole red = DiscordBot.createRole(this.getGuild());
		
		//apply the appropriate settings
		//.changeHoist() doesnt work, using .edit() instead
		/*blue.changeHoist(true);
		blue.changeName("#"+game.getGameID()+" Blue Team");
		blue.changeMentionable(true);
		red.changeHoist(true);
		red.changeName("#"+game.getGameID()+" Red Team");
		red.changeMentionable(true);*/
		blue.edit(blue.getColor(), true, "Blue Team #"+game.getGameID(), blue.getPermissions(), true);
		red.edit(red.getColor(), true, "Red Team #"+game.getGameID(), red.getPermissions(), true);
		
		//put the two team roles just after the queue role
		List<IRole> roles = DiscordBot.getRoles(this.getGuild());
		//they should be at the bottom of the role list, but remove them before getting the queue index just in case
		roles.remove(red);
		roles.remove(blue);
		int queueIndex = roles.indexOf(this.getQueueRole());
		roles.add(queueIndex+1, red);
		roles.add(queueIndex+1, blue);
		DiscordBot.reorderRoles(this.getGuild(), roles);
		
		//set the roles on the game object
		game.setBlueRole(blue);
		game.setRedRole(red);
	}

	private void addPlayersToTeamRoles(GatherGame game) {
		List<PlayerObject> players = game.getBluePlayerList();
		for(PlayerObject player : players)
		{
			DiscordBot.addRole(player.getDiscordUserInfo(), game.getBlueRole());
		}
		players = game.getRedPlayerList();
		for(PlayerObject player : players)
		{
			DiscordBot.addRole(player.getDiscordUserInfo(), game.getRedRole());
		}
	}

	private void removePlayerTeamRoles(GatherGame game) {
		List<PlayerObject> players = game.getBluePlayerList();
		for(PlayerObject player : players)
		{
			DiscordBot.removeRole(player.getDiscordUserInfo(), game.getBlueRole());
		}
		players = game.getRedPlayerList();
		for(PlayerObject player : players)
		{
			DiscordBot.removeRole(player.getDiscordUserInfo(), game.getRedRole());
		}
	}
	
	private void deleteTeamRoles(GatherGame game) {
		DiscordBot.deleteRole(game.getBlueRole());
		game.setBlueRole(null);
		DiscordBot.deleteRole(game.getRedRole());
		game.setRedRole(null);
	}

	/**Helper function for converting a team number into a team string for win messages
	 * @param team the team to convert
	 * @return the resulting string
	 * @see #endGame(GatherGame, int)
	 */
	public String teamString(int team)
	{
		if(team==0)
		{
			return "blue team won!";
		}
		else if(team==1)
		{
			return "red team won!";
		}
		else if(team==-1)
		{
			return "its a draw!";
		}
		else
		{
			return "no scores given";
		}
	}

	/**Getter for current queue size. 
	 * @return the current size of the queue
	 */
	public int numPlayersInQueue()
	{
		return queue.numPlayersInQueue();
	}

	/**
	 * @return true if the number of players in the queue is greater than or equal to the max queue size, false otherwise
	 */
	public boolean isQueueFull()
	{
		return queue.isFull();
	}

	/**Getter for the maximum queue size. 
	 * @return the maximum queue size
	 */
	public int getMaxQueueSize()
	{
		return GatherQueueObject.getMaxQueueSize();
	}

	/**Setter for maximum queue size. 
	 * @param size the size of queue to set
	 */
	public void setMaxQueueSize(int size)
	{
		GatherQueueObject.setMaxQueueSize(size);
	}

	/**Function called when an updated ticket count is sent from a server. 
	 * @param ip the ip address of the server this information is coming from
	 * @param port the port of the server this information is coming from
	 * @param team the team the tickets should be set for
	 * @param tickets the number of tickets the team has
	 */
	public void updateTickets(String ip, int port, int team, int tickets)
	{
		GatherGame game = getRunningGame(ip, port);
		if(game==null) return;
		if(team==0)
		{
			game.setBlueTickets(tickets);
		}
		else if(team==1)
		{
			game.setRedTickets(tickets);
		}
	}

	/*public String getMentionString()
	{
		String returnString="";
		for(PlayerObject player : queue)
		{
			returnString += " ";
			returnString += player.getDiscordUserInfo().mention();
		}
		return returnString;
	}*/

	/*public ArrayList<String> getMentionList()
	{
		ArrayList<String> returnList = new ArrayList<String>();
		for(PlayerObject player : queue)
		{
			returnList.add(player.getDiscordUserInfo().mention());
		}
		return returnList;
	}*/

	/**Helper function for creating the string returned by the !status command. Contains the current game state and tickets for each team. 
	 * @return a string containing information about the status of currently running games
	 * @see #CommandStatus
	 */
	public String statusString()
	{
		String returnString = "";

		for(GatherGame game : runningGames)
		{
			if(!game.isConnectedToServer())
			{
				returnString += "#"+game.getGameID()+" No server connection"+ "\n";
				continue;
			}
			returnString += "Game #" + game.getGameID() + " " + game.getStateString() + "\n";
			returnString += "Blue Tickets: " + game.getBlueTickets() + "\n";
			returnString += "Red Tickets: " + game.getRedTickets() + "\n";

		}

		return returnString;
	}

	/**Helper function for creating the string returned by the !players command. Contains a list of players on each team of all the currently running games. 
	 * @return a string containing the names of all the current players
	 * @see #CommandPlayers
	 */
	public String playersString()
	{
		String returnString = "";
		for(GatherGame game : runningGames)
		{
			returnString += game.toString(this.getGuild());
			returnString += "\n";
		}
		if(returnString.length()<=2)
		{
			return "";
		}
		return returnString.substring(0, returnString.length()-1);
	}

	/**Helper function for creating the current queue string returned by the !list command. Contains a count of the queue size and a list of the current players. 
	 * @return a string containing the names of all the players currently in the queue
	 * @see #CommandList
	 */
	public String queueString()
	{
		String returnString="";
		for(PlayerObject player : queue)
		{
			returnString+=playerString(player, this.getGuild());
			returnString+=", ";
		}
		if(returnString.length()<=2)
		{
			return "";
		}
		else
		{
			return returnString.substring(0, returnString.length()-2);
		}
	}

	/**Checks equality of GatherObject's by comparing the command channel
	 * @param obj the object to commpare
	 * @return true if the command channels are equal, false otherwise
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj)
	{
		return (this.getCommandChannel().equals(((GatherObject)obj).getCommandChannel()));
	}
}