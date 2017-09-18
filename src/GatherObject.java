import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import sx.blah.discord.Discord4J;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.IVoiceChannel;
import sx.blah.discord.handle.obj.Permissions;
import sx.blah.discord.util.PermissionUtils;

public class GatherObject
{
	private GatherQueueObject queue;
	
	private IGuild guild;
	private IChannel commandChannel = null;
	private IRole adminRole = null;
	private IRole queueRole = null;
	private IVoiceChannel blueVoiceChannel = null;
	private IVoiceChannel redVoiceChannel = null;
	private IVoiceChannel generalVoiceChannel = null;
	private IChannel scoreReportChannel = null;
	public long guildID = 0L;
	public String commandChannelString = "";
	public long commandChannelID = 0L;
	public long blueVoiceID = 0L;
	public long redVoiceID = 0L;
	public long generalVoiceID = 0L;
	public long scoreReportID = 0L;
	public long adminRoleID = 0L;
	public long queueRoleID = 0L;
	
	private int subVotesRequired = 3;

	public Set<GatherServer> servers;
	
	private List<GatherGame> runningGames;
	
	//list of games with sub in order of request time
	//will have the same game multiple times if more than one sub for that game
	//serves as a priority list replacing oldest sub requests first
	private List<GatherGame> gamesWithSub;
	
	GatherObject()
	{
		queue = new GatherQueueObject();
		servers = new HashSet<GatherServer>();
		runningGames = new ArrayList<GatherGame>();
		gamesWithSub = new ArrayList<GatherGame>();
	}
	
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
		
		//no command channel found
		if(commandChannel==null) System.out.println("Error: no command channel found for guild: "+guild.getName());
	}
	
	public IGuild getGuild() {
		return guild;
	}

	public void setGuild(IGuild guild)
	{
		if(guild == null) Discord4J.LOGGER.warn("guild is being set as null");
		this.guild = guild;
	}

	public IChannel getCommandChannel() {
		return commandChannel;
	}

	public void setCommandChannel(IChannel commandChannel) {
		if(commandChannel == null) Discord4J.LOGGER.warn("command channel is being set as null");
		this.commandChannel = commandChannel;
	}
	
	public IChannel getScoreReportChannel() {
		return scoreReportChannel;
	}

	public void setScoreReportChannel(IChannel scoreReportChannel) {
		if(scoreReportChannel == null) Discord4J.LOGGER.warn("score report channel is being set as null");
		this.scoreReportChannel = scoreReportChannel;
	}
	
	public IRole getAdminRole() {
		return adminRole;
	}

	public void setAdminRole(IRole adminRole) {
		if(adminRole == null) Discord4J.LOGGER.warn("admin role is being set as null");
		this.adminRole = adminRole;
	}

	public IRole getQueueRole() {
		return queueRole;
	}

	public void setQueueRole(IRole queueRole) {
		if(queueRole == null) Discord4J.LOGGER.warn("queue role is being set as null");
		this.queueRole = queueRole;
	}

	public IVoiceChannel getBlueVoiceChannel() {
		return blueVoiceChannel;
	}

	public void setBlueVoiceChannel(IVoiceChannel blueVoiceChannel) {
		if(blueVoiceChannel == null) Discord4J.LOGGER.warn("blue voice channel is being set as null");
		this.blueVoiceChannel = blueVoiceChannel;
	}

	public IVoiceChannel getRedVoiceChannel() {
		return redVoiceChannel;
	}

	public void setRedVoiceChannel(IVoiceChannel redVoiceChannel) {
		if(redVoiceChannel == null) Discord4J.LOGGER.warn("red voice channel is being set as null");
		this.redVoiceChannel = redVoiceChannel;
	}

	public IVoiceChannel getGeneralVoiceChannel() {
		return generalVoiceChannel;
	}

	public void setGeneralVoiceChannel(IVoiceChannel generalVoiceChannel) {
		if(generalVoiceChannel == null) Discord4J.LOGGER.warn("general voice channel is being set as null");
		this.generalVoiceChannel = generalVoiceChannel;
	}
	
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
	
	//check if the bot can set the role
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

	public int getSubVotesRequired() {
		return subVotesRequired;
	}

	public void setSubVotesRequired(int subVotesRequired) {
		this.subVotesRequired = subVotesRequired;
	}
	
	public String fullUserString(IUser user)
	{
		return user.getDisplayName(getGuild()) + "(" + user.getName() + "#" + user.getDiscriminator() + ")";
	}

	/**
	 * Adds a player to the gather queue
	 *
	 * @return 0 if player already in queue or something else went wrong
	 * @return 1 if player added to the queue
	 * @return 2 if player added to queue and queue is now full
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
			DiscordBot.bot.addRole(player.getDiscordUserInfo(), getQueueRole());
			if(isQueueFull())
			{
				return 2;
			}
			return 1;
		}
		if(isQueueFull())
		{	//this happens if someone adds after a queue fills but before a game has been started
			return 4;
		}
		return 0;
		
	}
	
	public int remFromQueue(PlayerObject player)
	{
		if(queue.remove(player))
		{
			updateChannelCaption();
			DiscordBot.bot.removeRole(player.getDiscordUserInfo(), getQueueRole());
			return 1;
		}
		else
		{
			return 0;
		}
	}
	
	public GatherGame getPlayersGame(PlayerObject player)
	{
		for(GatherGame game : runningGames)
		{
			if(game.isPlayerPlaying(player)) return game;
		}
		return null;
	}
	
	public GatherGame getPlayersGame(IUser user)
	{
		return getPlayersGame(new PlayerObject(user));
	}
	
	public boolean isInGame(PlayerObject player)
	{
		GatherGame game = getPlayersGame(player);
		if(game==null) return false;
		else return true;
	}
	
	public void updateChannelCaption()
	{
		DiscordBot.setPlayingText(this.numPlayersInQueue()+"/"+this.getMaxQueueSize()+" in queue");
		DiscordBot.setChannelCaption(this.getGuild(), this.getCommandChannel(), this.numPlayersInQueue()+"-in-q"+ "_" + this.commandChannelString);
	}
	
	public void startGame()
	{
		//setup the game
		List<PlayerObject> list = queue.asList();
		GatherServer server = DiscordBot.bot.getFreeServer(this.guild);
		GatherGame game = new GatherGame(-1, list, null, null, server);
		game.shuffleTeams();
		runningGames.add(game);
		
		//announce the game
		//do the team messages in separate lines so that it highlights the players team
		
		DiscordBot.bot.sendMessage(getCommandChannel(), "Gather game starting: ", true);
		DiscordBot.bot.sendMessage(getCommandChannel(), "http://125.63.63.59/joingame.html");
		DiscordBot.bot.sendMessage(getCommandChannel(), "__**Blue**__: "+game.blueMentionList().toString());
		DiscordBot.bot.sendMessage(getCommandChannel(), "__**Red**__:  "+game.redMentionList().toString());
		Discord4J.LOGGER.info("Game started: "+game.blueMentionList().toString()+game.redMentionList().toString());
		//reset the queue
		this.clearQueue();
	}
	
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
	
	public void removeRunningGame(GatherGame game)
	{
		runningGames.remove(game);
		while(gamesWithSub.remove(game));
	}
	
	public boolean endGame(GatherGame game, int winningTeam)
	{
		//tell everyone
		DiscordBot.bot.sendMessage(getCommandChannel(), "A game has ended, "+teamString(winningTeam));
		if(winningTeam<-1 || winningTeam>1) return true;
		//print to score report
		String temp1 = game.blueMentionList().toString();
		if(winningTeam==0) temp1 += " +1";
		else if (winningTeam==1) temp1 += " -1";
		else temp1 += " 0";
		String temp2 = game.redMentionList().toString();
		if(winningTeam==1) temp2 += " +1";
		else if (winningTeam==0) temp2 += " -1";
		else temp2 += " 0";
		DiscordBot.bot.sendMessage(getScoreReportChannel(), temp1);
		DiscordBot.bot.sendMessage(getScoreReportChannel(), temp2);
		//store stats in database
		//TODO
		//remove game object from list
		if(game.getServer() == null)
		{
			//THIS IS A WORKAROUND FOR WHEN WE HAVE NO SERVER LIST AND THERE IS ONLY 1 GAME AT A TIME
			Discord4J.LOGGER.warn("Server is null when giving win, clearing running games (if there is more than 1 running game this is a problem)");
			clearGames();
			return true;
		}
		removeRunningGame(game);
		//set server unused?
		//TODO
		return true;
	}
	
	public boolean endGame(String serverIp, int serverPort, int winningTeam)
	{
		GatherGame game = getRunningGame(serverIp, serverPort);
		if(game == null) return false;
		this.endGame(game, winningTeam);
		return true;
	}
	
	public boolean endGame(int matchid, int winningTeam)
	{
		GatherGame game = getRunningGame(matchid);
		if(game == null) return false;
		this.endGame(game, winningTeam);
		return true;
	}
	
	public GatherServer getFreeServer()
	{
		// TODO make some kind of server priority? in case of high/low ping servers?
		// not important now as there should only be 1 server anyway
		for(GatherServer server : servers)
		{
			if (!server.isInUse()) {
				return server;
			}
		}
		return null;
	}
	
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
	
	public void connectKAGServers()
	{
		for(GatherServer server : servers)
		{
			server.connect();
		}
	}
	
	public void disconnectKAGServers()
	{
		for(GatherServer server : servers)
		{
			server.disconnect();
		}
	}
	
	public void clearQueueRole()
	{
		List<IUser> list = getGuild().getUsersByRole(getQueueRole());
		for(IUser user : list)
		{
			DiscordBot.bot.removeRole(user, getQueueRole());
		}
	}
	
	public void clearQueue()
	{
		for(PlayerObject player : queue)
		{
			DiscordBot.bot.removeRole(player.getDiscordUserInfo(), getQueueRole());
		}
		queue.clear();
		updateChannelCaption();
	}
	
	public void clearGames()
	{
		runningGames.clear();
	}
	
	public int addSubRequest(PlayerObject playerToBeSubbed)
	{
		GatherGame game = getPlayersGame(playerToBeSubbed);
		if(game==null)
		{
			//player isnt playing a game
			return -1;
		}
		else
		{
			if(game.addSubRequest(playerToBeSubbed))
			{
				//successfully added sub request
				gamesWithSub.add(game);
				return 1;
			}
			else
			{
				//player already has sub request
				return 0;
			}
		}
	}
	
	public int addSubRequest(IUser user)
	{
		return addSubRequest(new PlayerObject(user));
	}
	
	public int addSubVote(PlayerObject playerVotedFor, PlayerObject playerVoting)
	{
		GatherGame voterGame = getPlayersGame(playerVoting);
		GatherGame votedGame = getPlayersGame(playerVotedFor);
		if(voterGame == null)
		{
			//voter not in game
			return -1;
		}
		if(votedGame == null)
		{
			//voted on in a game
			return -2;
		}
		if(!votedGame.equals(voterGame))
		{
			//voter and voted must be in same game
			return -3;
		}
		int returnval = votedGame.addSubVote(playerVotedFor, playerVoting);
		if(returnval == -1)
		{
			//sub request already exists for this player
			return -4;
		}
		else if(returnval == -2)
		{
			//voter already voted to sub this player
			return -5;
		}
		
		if(returnval >= getSubVotesRequired())
		{
			votedGame.removeSubVotes(playerVotedFor);
			addSubRequest(playerVotedFor);
			//votes filled and sub request added
			return 0;
		}
		//vote added
		return returnval;
	}
	
	public int addSubVote(IUser playerVotedFor, IUser playerVoting)
	{
		return addSubVote(new PlayerObject(playerVotedFor), new PlayerObject(playerVoting));
	}
	
	private GatherGame getFirstGameWithSub() {
		if(gamesWithSub.isEmpty()) return null;
		return gamesWithSub.remove(0);
	}
	
	public SubstitutionObject subPlayerIntoGame(IUser user)
	{
		System.out.print("number of games with sub: "+gamesWithSub.size());
		GatherGame game = getFirstGameWithSub();
		System.out.print("number of games with sub: "+gamesWithSub.size());
		if(game == null) return null;
		else return game.subPlayerIntoGame(user);
	}
	
	public void clearSubs()
	{
		for(GatherGame game : runningGames)
		{
			game.clearSubs();
		}
		for(GatherGame game : gamesWithSub)
		{
			//these should already be cleared by the previous loop, but just to make sure in case an old game is in this list
			game.clearSubs();
		}
		gamesWithSub.clear();
	}

	public void movePlayersIntoTeamRooms()
	{
		DiscordBot.bot.sendMessage(this.getCommandChannel(), "Moving players into team rooms");

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
					DiscordBot.bot.moveToVoiceChannel(user, blue);
				}
				else if(team==1)
				{
					DiscordBot.bot.moveToVoiceChannel(user, red);
				}
			}
		}
	}
	
	public void movePlayersOutOfTeamRooms()
	{
		DiscordBot.bot.sendMessage(this.getCommandChannel(), "Moving players out of team rooms");
		
		IVoiceChannel general = this.getGeneralVoiceChannel();
		IVoiceChannel blue = this.getBlueVoiceChannel();
		IVoiceChannel red = this.getRedVoiceChannel();
		
		List<IUser> users;
		users = blue.getConnectedUsers();
		for( IUser user : users)
		{
			DiscordBot.bot.moveToVoiceChannel(user, general);
		}
		users = red.getConnectedUsers();
		for( IUser user : users)
		{
			DiscordBot.bot.moveToVoiceChannel(user, general);
		}
		
	}
	
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
	
	public int numPlayersInQueue()
	{
		return queue.numPlayersInQueue();
	}
	
	public boolean isQueueFull()
	{
		return queue.isFull();
	}
	
	public int getMaxQueueSize()
	{
		return GatherQueueObject.getMaxQueueSize();
	}
	
	public void setMaxQueueSize(int size)
	{
		GatherQueueObject.setMaxQueueSize(size);
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
	
	public String playersString()
	{
		String returnString = "";
		for(GatherGame game : runningGames)
		{
			returnString += game.toString();
			returnString += "\n";
		}
		if(returnString.length()<=2)
		{
			return "";
		}
		return returnString.substring(0, returnString.length()-1);
	}
	
	public String queueString()
	{
		String returnString="";
		for(PlayerObject player : queue)
		{
			returnString+=fullUserString(player.getDiscordUserInfo());
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
	
	@Override
	public boolean equals(Object obj)
	{
		return (this.guild == ((GatherObject)obj).guild);
	}
}