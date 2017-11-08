import java.util.ArrayList;
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
	
	public IMessage getScoreboardMessage() {
		return scoreboardMessage;
	}

	public void setScoreboardMessage(IMessage scoreboardMessage) {
		this.scoreboardMessage = scoreboardMessage;
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
	
	public String fullUserString(IUser user)
	{
		return user.getDisplayName(getGuild()) + "(" + user.getName() + "#" + user.getDiscriminator() + ")";
	}
	
	public String fullUserString(PlayerObject player)
	{
		return fullUserString(player.getDiscordUserInfo());
	}
	
	public String playerString(PlayerObject player, IGuild currGuild)
	{
		return player.getKagName()+" ("+player.getDiscordUserInfo().getDisplayName(currGuild)+")";
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
			DiscordBot.addRole(player.getDiscordUserInfo(), getQueueRole());
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
	
	public int addToQueue(IUser user)
	{
		PlayerObject player = DiscordBot.players.getObject(user);
		//player is null if they are not linked
		if(player==null) return -1;
		return this.addToQueue(player);
	}
	
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
	
	public int remFromQueue(IUser user)
	{
		PlayerObject player = DiscordBot.players.getObject(user);
		//player is null if they are not linked
		if(player==null) return -1;
		return this.remFromQueue(player);
	}
	
	public GatherGame getPlayersGame(PlayerObject player)
	{
		if(player==null) return null;
		for(GatherGame game : runningGames)
		{
			if(game.isPlayerPlaying(player)) return game;
		}
		return null;
	}
	
	public GatherGame getPlayersGame(IUser user)
	{
		return getPlayersGame(DiscordBot.players.getObject(user));
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
	
	public void addScrambleVote(PlayerObject player)
	{
		GatherGame game = this.getPlayersGame(player);
		if(game==null)
		{
			DiscordBot.sendMessage(this.getCommandChannel(), "There is **no game** to scramble "+player.getDiscordUserInfo().getDisplayName(this.getGuild())+"!");
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
			return;
		case -1:
			DiscordBot.sendMessage(getCommandChannel(), "You have already voted to scramble the teams "+player.getDiscordUserInfo().getNicknameForGuild(getGuild())+"("+game.getNumScrambleVotes()+"/"+game.getScrambleVotesReq()+")");
			return;
		}
		DiscordBot.sendMessage(getCommandChannel(), "**Vote to scramble** teams has been counted for "+player.getDiscordUserInfo().getNicknameForGuild(getGuild())+" ("+returnVal+"/"+game.getScrambleVotesReq()+")");
	}
	
	public void startGame()
	{
		//setup the game
		List<PlayerObject> list = queue.asList();
		GatherServer server = this.getFreeServer();
		if(server != null) server.setInUse(true);
		//TODO probably shouldnt start a game if there is no free server
		GatherGame game = new GatherGame(-1, list, null, null, server);
		game.shuffleTeams();
		runningGames.add(game);
		
		//announce the game
		//do the team messages in separate lines so that it highlights the players team
		
		DiscordBot.sendMessage(getCommandChannel(), "Gather game starting: ", true);
		DiscordBot.sendMessage(getCommandChannel(), "http://125.63.63.59/joingame.html");
		DiscordBot.sendMessage(getCommandChannel(), "__**Blue**__: "+game.blueMentionList().toString());
		DiscordBot.sendMessage(getCommandChannel(), "__**Red**__:  "+game.redMentionList().toString());
		Discord4J.LOGGER.info("Game started: "+game.blueMentionList().toString()+game.redMentionList().toString());
		game.sendTeamsToServer();
		//reset the queue
		this.clearQueue();
		//do voice channel stuff
		movePlayersIntoTeamRooms(5);
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
	
	public List<GatherGame> getRunningGames()
	{
		if(runningGames.isEmpty()) return null;
		List<GatherGame> returnList = new ArrayList<GatherGame>();
		for(GatherGame game : runningGames)
		{
			returnList.add(game);
		}
		return returnList;
	}
	
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
	}
	
	public boolean endGame(GatherGame game, int winningTeam)
	{
		//tell everyone
		DiscordBot.sendMessage(getCommandChannel(), "A game has ended, "+teamString(winningTeam));
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
			//TODO save a record of the game in the db
			game.saveResultToDB(winningTeam, this.substitutions);
			this.updateScoreboard();
		}
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
	
	public void updateScoreboard()
	{
		if(this.getScoreboardMessage()==null)
		{
			Discord4J.LOGGER.warn("Scoreboard not set!");
			return;
		}
		List<StatsObject> list = DiscordBot.database.getTop10();
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
	
	public void setBuildingTimeEnded(String ip, int port)
	{
		this.getRunningGame(ip, port).setStateInProgress();
		DiscordBot.sendMessage(this.getCommandChannel(), "Building time ended on server");
	}
	
	public void setRoundStarted(String ip, int port)
	{
		this.getRunningGame(ip, port).setStateBuilding();
		DiscordBot.sendMessage(this.getCommandChannel(), "Building time started on server");
	}
	
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
			this.getServer(ip, port).say("Sub request added for player "+kagName+", use !sub in Discord to sub into their place!");
			DiscordBot.sendMessage(this.getCommandChannel(), "**Sub request** added for player " + this.fullUserString(playerToBeSubbed) + " use **!sub** to sub into their place! ("+this.getQueueRole().mention()+")");
		}
		return returnVal;
	}

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
			this.getServer(ip, port).say("Sub request added for "+playerVotedFor.getKagName()+", use !sub in Discord to sub into their place!");
			DiscordBot.sendMessage(this.getCommandChannel(), "A sub has been requested for player " + this.fullUserString(playerVotedFor) + " use **!sub** to sub into their place! ("+this.getQueueRole().mention()+")");
			return returnVal;
		}
		//gets here if returnVal is greater than 0 which means the sub vote was added and the number is the vote count
		//dont put this in case statement because that could cause issues if we changed the number of votes required
		this.getServer(ip, port).say("Vote to sub " + votedFor + " has been counted for " + voting + " (" + returnVal +"/"+ this.substitutions.getSubVotesRequired() +")");
		DiscordBot.sendMessage(this.getCommandChannel(), "Vote to sub " + votedFor + " has been counted for " + voting + " (" + returnVal +"/"+ this.substitutions.getSubVotesRequired() +")");
		return returnVal;
	}
	
	public void clearQueueRole()
	{
		List<IUser> list = getGuild().getUsersByRole(getQueueRole());
		for(IUser user : list)
		{
			DiscordBot.removeRole(user, getQueueRole());
		}
	}
	
	public void clearQueue()
	{
		for(PlayerObject player : queue)
		{
			DiscordBot.removeRole(player.getDiscordUserInfo(), getQueueRole());
		}
		queue.clear();
		updateChannelCaption();
	}
	
	public void clearGames()
	{
		while(!runningGames.isEmpty())
		{
			setGameEnded(runningGames.get(0));
		}
	}
	
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
	
	public void movePlayersIntoTeamRooms(int delay)
	{
		String countString = "Moving channels in ";
		IMessage countMsg = DiscordBot.sendMessage(this.getCommandChannel(), countString+delay, true);

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
	
	public void movePlayersOutOfTeamRooms(int delay)
	{
		String countString = "Moving channels in ";
		IMessage countMsg = DiscordBot.sendMessage(this.getCommandChannel(), countString+delay, true);

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
	
	public String statusString()
	{
		String returnString = "";

		for(GatherGame game : runningGames)
		{
			if(!game.isConnectedToServer())
			{
				returnString += "No server connection"+ "\n";
				continue;
			}
			returnString += "Game State: " + game.getStateString() + "\n";
			returnString += "Blue Tickets: " + game.getBlueTickets() + "\n";
			returnString += "Red Tickets: " + game.getRedTickets() + "\n";
			
		}
		
		return returnString;
	}
	
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
	
	@Override
	public boolean equals(Object obj)
	{
		return (this.getCommandChannel().equals(((GatherObject)obj).getCommandChannel()));
	}
}