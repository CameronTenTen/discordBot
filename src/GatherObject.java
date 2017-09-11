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

public class GatherObject
{
	private GatherQueueObject queue;
	
	private IGuild guild;
	private IChannel commandChannel = null;
	private IRole adminRole = null;
	private IVoiceChannel blueVoiceChannel = null;
	private IVoiceChannel redVoiceChannel = null;
	private IVoiceChannel generalVoiceChannel = null;
	private IChannel scoreReportChannel = null;
	public long guildID;
	public String textChannelString;
	public String adminString;
	public String blueVoiceString;
	public String redVoiceString;
	public String generalVoiceString;
	public String scoreReportString;

	public Set<GatherServer> servers;
	
	private List<GatherGame> runningGames;
	
	GatherObject()
	{
		queue = new GatherQueueObject();
		servers = new HashSet<GatherServer>();
		runningGames = new ArrayList<GatherGame>();
	}
	
	public void setDiscordObjects()
	{
		setGuild(DiscordBot.client.getGuildByID(guildID));
		if(guild == null)
		{
			Discord4J.LOGGER.error("Could not find guild with id: "+guildID);
			return;
		}
		
		//search text channels for a command channel
		List<IChannel> channels = guild.getChannels();
		for(IChannel channel : channels)
		{
			if(channel.getName().contains(textChannelString))
			{
				setCommandChannel(channel);
				break;
			}
		}
		for(IChannel channel : channels)
		{
			if(channel.getName().contains(scoreReportString))
			{
				setScoreReportChannel(channel);
				break;
			}
		}
		
		List<IRole> roles = guild.getRoles();
		for(IRole role : roles)
		{
			if(role.getName().contains(adminString))
			{
				setAdminRole(role);
				break;
			}
		}
		
		//get the first voice channel matching the right string
		List<IVoiceChannel> voice = guild.getVoiceChannelsByName(blueVoiceString);
		if(!voice.isEmpty()) setBlueVoiceChannel(voice.get(0));
		
		voice = guild.getVoiceChannelsByName(redVoiceString);
		if(!voice.isEmpty()) setRedVoiceChannel(voice.get(0));
		
		voice = guild.getVoiceChannelsByName(generalVoiceString);
		if(!voice.isEmpty()) setGeneralVoiceChannel(voice.get(0));
		
		//no command channel found
		if(commandChannel==null) System.out.println("Error: no command channel found for guild: "+guild.getName());
	}
	
	public IGuild getGuild() {
		return guild;
	}

	public void setGuild(IGuild guild)
	{
		this.guild = guild;
	}

	public IChannel getCommandChannel() {
		return commandChannel;
	}

	public void setCommandChannel(IChannel commandChannel) {
		this.commandChannel = commandChannel;
	}
	
	public IChannel getScoreReportChannel() {
		return scoreReportChannel;
	}

	public void setScoreReportChannel(IChannel scoreReportChannel) {
		this.scoreReportChannel = scoreReportChannel;
	}

	public String getScoreReportString() {
		return scoreReportString;
	}

	public void setScoreReportString(String scoreReportString) {
		this.scoreReportString = scoreReportString;
	}
	
	public IRole getAdminRole() {
		return adminRole;
	}

	public void setAdminRole(IRole adminRole) {
		this.adminRole = adminRole;
	}
	
	public IVoiceChannel getBlueVoiceChannel() {
		return blueVoiceChannel;
	}

	public void setBlueVoiceChannel(IVoiceChannel blueVoiceChannel) {
		this.blueVoiceChannel = blueVoiceChannel;
	}

	public IVoiceChannel getRedVoiceChannel() {
		return redVoiceChannel;
	}

	public void setRedVoiceChannel(IVoiceChannel redVoiceChannel) {
		this.redVoiceChannel = redVoiceChannel;
	}

	public IVoiceChannel getGeneralVoiceChannel() {
		return generalVoiceChannel;
	}

	public void setGeneralVoiceChannel(IVoiceChannel generalVoiceChannel) {
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
			if(isQueueFull())
			{
				return 2;
			}
			return 1;
		}
		return 0;
		
	}
	
	public int remFromQueue(PlayerObject player)
	{
		if(queue.remove(player))
		{
			return 1;
		}
		else
		{
			return 0;
		}
	}
	
	public boolean isInGame(PlayerObject player)
	{
		for(GatherGame game : runningGames)
		{
			if(game.isPlayerPlaying(player)) return true;
		}
		return false;
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
		
		getCommandChannel().sendMessage("Gather game starting: ", true);
		getCommandChannel().sendMessage("http://125.63.63.59/joingame.html");
		getCommandChannel().sendMessage("__**Blue**__: "+game.blueMentionList().toString());
		getCommandChannel().sendMessage("__**Red**__:  "+game.redMentionList().toString());
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
	
	public boolean endGame(GatherGame game, int winningTeam)
	{
		//tell everyone
		commandChannel.sendMessage("A game has ended, "+teamString(winningTeam));
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
		scoreReportChannel.sendMessage(temp1);
		scoreReportChannel.sendMessage(temp2);
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
		runningGames.remove(game);
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
	
	public void connectToKAGServers()
	{
		for(GatherServer server : servers)
		{
			server.connect();
		}
	}
	
	public void clearQueue()
	{
		queue.clear();
		DiscordBot.setPlayingText(this.numPlayersInQueue()+"/"+this.getMaxQueueSize()+" in queue");
		DiscordBot.setChannelCaption(this.getGuild() , this.numPlayersInQueue()+"-in-q");
	}
	
	public void clearGames()
	{
		runningGames.clear();
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