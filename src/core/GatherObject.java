package core;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonSyntaxException;

import discord4j.common.util.Snowflake;
import discord4j.core.object.VoiceState;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.Role;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.core.object.entity.channel.VoiceChannel;
import discord4j.rest.util.Permission;

/**This object contains various variables and functions for one gather queue/channel. 
 * @author cameron
 *
 */
public class GatherObject
{
	static final Logger LOGGER = LoggerFactory.getLogger(GatherObject.class);

	private GatherQueueObject queue;

	private Guild guild;
	private TextChannel commandChannel = null;
	private Role adminRole = null;
	private Role queueRole = null;
	private Role softQueueRole = null;
	private VoiceChannel blueVoiceChannel = null;
	private VoiceChannel redVoiceChannel = null;
	private VoiceChannel generalVoiceChannel = null;
	private TextChannel scoreReportChannel = null;
	private Message scoreboardMessage = null;
	private String commandChannelBaseName = "";

	public SubManager substitutions = null;

	public Map<String, GatherServer> servers;

	private List<GatherGame> runningGames;

	GatherObject(GatherObjectConfig config)
	{
		queue = new GatherQueueObject();
		servers = new HashMap<String, GatherServer>();
		runningGames = new ArrayList<GatherGame>();
		substitutions = new SubManager(this);
		this.setDiscordObjects(config);
		this.initialiseServers(config);
	}

	/**
	 * Function to be called when the bot is ready for setup. Initialises all the Discord4J related objects such as channels and roles. Also initialises the scoreboard. 
	 */
	private void setDiscordObjects(GatherObjectConfig config)
	{
		setGuild(DiscordBot.client.getGuildById(Snowflake.of(config.guildID)).block());
		if(guild == null)
		{
			LOGGER.error("Could not find guild with id: "+config.guildID);
			return;
		}

		//TODO: shouldn't use so many blocks here, could be made a lot faster
		setCommandChannel(DiscordBot.client.getChannelById(Snowflake.of(config.commandChannelID)).ofType(TextChannel.class).block());
		setScoreReportChannel(DiscordBot.client.getChannelById(Snowflake.of(config.scoreReportID)).ofType(TextChannel.class).block());
		setBlueVoiceChannel(DiscordBot.client.getChannelById(Snowflake.of(config.blueVoiceID)).ofType(VoiceChannel.class).block());
		setRedVoiceChannel(DiscordBot.client.getChannelById(Snowflake.of(config.redVoiceID)).ofType(VoiceChannel.class).block());
		setGeneralVoiceChannel(DiscordBot.client.getChannelById(Snowflake.of(config.generalVoiceID)).ofType(VoiceChannel.class).block());
		setAdminRole(DiscordBot.client.getRoleById(Snowflake.of(config.guildID), Snowflake.of(config.adminRoleID)).block());
		setQueueRole(DiscordBot.client.getRoleById(Snowflake.of(config.guildID), Snowflake.of(config.queueRoleID)).block());
		setSoftQueueRole(DiscordBot.client.getRoleById(Snowflake.of(config.guildID), Snowflake.of(config.softQueueRoleID)).block());

		if(config.scoreboardChannelID!=0)
		{
			TextChannel chan = DiscordBot.client.getChannelById(Snowflake.of(config.scoreboardChannelID)).ofType(TextChannel.class).block();
			if(chan == null)
			{
				LOGGER.warn("Error getting scoreboard channel, null returned");
				return;
			}

			if(config.scoreboardMessageID==0)
			{
				config.scoreboardMessageID = DiscordBot.sendMessage(chan, "scoreboard").getId().asLong();
				System.out.println("new scoreboard message has been created, please enter the message id in the config or a scoreboard will be created each time the bot starts: "+config.scoreboardMessageID);
			}

			setScoreboardMessage(chan.getMessageById(Snowflake.of(config.scoreboardMessageID)).block());
		}

		if(this.getScoreboardMessage()!=null)this.updateScoreboard();

		//no command channel found
		if(commandChannel==null) System.out.println("Error: no command channel found for guild: "+guild.getName());
		
		this.commandChannelBaseName = config.commandChannelString;
	}

	public void initialiseServers(GatherObjectConfig config)
	{
		for(GatherServer server : config.serverList)
		{
			if (servers.containsKey(server.getServerID().toUpperCase()))
			{
				throw new JsonSyntaxException("duplicate key: "+server.getServerID()+" - server's must all have unique id's");
			}
			servers.put(server.getServerID().toUpperCase(), server);
		}
	}

	/**
	 * @return the Discord guild this gather object is associated with. 
	 */
	public Guild getGuild() {
		return guild;
	}

	/**
	 * @param guild the Discord guild to associate this gather object with. 
	 */
	public void setGuild(Guild guild)
	{
		if(guild == null) LOGGER.warn("guild is being set as null");
		this.guild = guild;
	}

	/**
	 * @return the Discord channel this gather object is associated with.
	 */
	public TextChannel getCommandChannel() {
		return commandChannel;
	}

	/**
	 * @param commandChannel the Discord channel to associate this gather object with.
	 */
	public void setCommandChannel(TextChannel commandChannel) {
		if(commandChannel == null) LOGGER.warn("command channel is being set as null");
		this.commandChannel = commandChannel;
	}

	/**
	 * @return the Discord channel where the bot puts score reports at the end of each game. 
	 */
	public TextChannel getScoreReportChannel() {
		return scoreReportChannel;
	}

	/**
	 * @param scoreReportChannel the Discord channel where the bot should put score reports at the end of each game. 
	 */
	public void setScoreReportChannel(TextChannel scoreReportChannel) {
		if(scoreReportChannel == null) LOGGER.warn("score report channel is being set as null");
		this.scoreReportChannel = scoreReportChannel;
	}

	/**
	 * @return the Discord role of gather admins for using admin commands
	 */
	public Role getAdminRole() {
		return adminRole;
	}

	/**
	 * @param adminRole the Discord role that should be used as the admin role for using admin commands
	 */
	public void setAdminRole(Role adminRole) {
		if(adminRole == null) LOGGER.warn("admin role is being set as null");
		this.adminRole = adminRole;
	}

	/**
	 * @return the queue role for displaying the gather queue in the members list
	 */
	public Role getQueueRole() {
		return queueRole;
	}

	/**
	 * @param queueRole the role that should be used for displaying the current queue in the members list
	 */
	public void setQueueRole(Role queueRole) {
		if(queueRole == null) LOGGER.warn("queue role is being set as null");
		this.queueRole = queueRole;
	}

	/**
	 * @return the soft queue role for allowing people express interest in a game without committing
	 */
	public Role getSoftQueueRole() {
		return softQueueRole;
	}

	/**
	 * @param softQueueRole the role that should be used for displaying the current soft queue in the members list
	 */
	public void setSoftQueueRole(Role softQueueRole) {
		if(softQueueRole == null) LOGGER.warn("queue role is being set as null");
		this.softQueueRole = softQueueRole;
	}

	/**
	 * @return the voice channel for blue team
	 */
	public VoiceChannel getBlueVoiceChannel() {
		return blueVoiceChannel;
	}

	/**
	 * @param blueVoiceChannel the voice channel that should be used for blue team voice chat
	 */
	public void setBlueVoiceChannel(VoiceChannel blueVoiceChannel) {
		if(blueVoiceChannel == null) LOGGER.warn("blue voice channel is being set as null");
		this.blueVoiceChannel = blueVoiceChannel;
	}

	/**
	 * @return the voice channel for red team
	 */
	public VoiceChannel getRedVoiceChannel() {
		return redVoiceChannel;
	}

	/**
	 * @param blueVoiceChannel the voice channel that should be used for red team voice chat
	 */
	public void setRedVoiceChannel(VoiceChannel redVoiceChannel) {
		if(redVoiceChannel == null) LOGGER.warn("red voice channel is being set as null");
		this.redVoiceChannel = redVoiceChannel;
	}

	/**
	 * @return the general voice channel used for before/after game chat
	 */
	public VoiceChannel getGeneralVoiceChannel() {
		return generalVoiceChannel;
	}

	/**
	 * @param generalVoiceChannel the general voice channel used for before/after game chat
	 */
	public void setGeneralVoiceChannel(VoiceChannel generalVoiceChannel) {
		if(generalVoiceChannel == null) LOGGER.warn("general voice channel is being set as null");
		this.generalVoiceChannel = generalVoiceChannel;
	}

	/**
	 * @return the IMessage used to display the scoreboard
	 */
	public Message getScoreboardMessage() {
		return scoreboardMessage;
	}

	/**
	 * @param scoreboardMessage the IMessage that should be used to display the scoreboard
	 */
	public void setScoreboardMessage(Message scoreboardMessage) {
		this.scoreboardMessage = scoreboardMessage;
	}

	/**Check if a player has the admin role. 
	 * @param user the user to check
	 * @return true if one of their roles matches the admin role, false otherwise
	 */
	public boolean isAdmin(Member user)
	{
		if(user.getId().asLong()==207442663178240011L) return false;
		List<Role> roles = user.getRoles().collectList().block();
		for(Role role : roles)
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
	public boolean canSetRole(Role role)
	{
		/*if(role == null)
		{
			return false;
		}
		if(!PermissionUtils.hasPermissions(getGuild(), DiscordBot.client.getOurUser(), Permissions.MANAGE_ROLES))
		{
			LOGGER.warn("bot does not have MANAGE_ROLES permission, some functionality may be lost");
			return false;
		}
		return true;*/
		//TODO: actually check the role
		return DiscordBot.client.getSelf().block().asMember(this.getGuild().getId()).block().getBasePermissions().block().contains(Permission.MANAGE_ROLES);
	}

	/**Gets a string representing the specified Discord user. Formatted as DisplayName(Username#Discriminator).
	 * @param member the PlayerObject to convert to string
	 * @return a string representing the users name, formatted as DisplayName(Username#Discriminator)
	 */
	public String fullUserString(Member member)
	{
		return member.getDisplayName() + "(" + member.getUsername() + "#" + member.getDiscriminator() + ")";
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
	public String playerString(PlayerObject player)
	{
		if(player==null) return "";
		return player.getKagName()+" ("+player.getDiscordUserInfo().getDisplayName()+")";
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
	 * @param member the discord user to add
	 * @return 0 if the player already in queue or something else went wrong, 1 if the player added to the queue, 2 if player added to queue and the queue is now full, 4 if the player added after the queue is already full, or -1 if an error occured getting the player object
	 * @see #addToQueue(PlayerObject)
	 */
	public int addToQueue(Member member)
	{
		PlayerObject player = DiscordBot.players.getOrCreatePlayerObject(member);
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
	
	public PlayerObject checkInQueue(User user)
	{
		for(PlayerObject p : queue.asList())
		{
			if(user.getId().equals(p.getDiscordid())) return p;
		}
		return null;
	}

	/**Searches the queue for a player with a matching discord id, then removes that player object if the player is found
	 * @param user the user to be removed from the queue
	 * @return 1 if the player was removed from the queue, 0 if the player was not in the queue
	 * @see #remFromQueue(PlayerObject)
	 */
	public int remFromQueue(User user)
	{
		//check the queue by discord user first, so that we don't create unnecessary player objects
		PlayerObject player = checkInQueue(user);
		if(player==null) return 0;
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
	public GatherGame getPlayersGame(User user)
	{
		return getPlayersGame(DiscordBot.players.getIfExists(user));
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
	
	public String getPlayingText() {
		return this.numPlayersInQueue()+"/"+this.getMaxQueueSize()+" in queue";
	}

	/**
	 * Helper function for updating the channel name to reflect the queue size and set the playing text based on queue size.
	 */
	public void updateChannelCaption()
	{
		DiscordBot.setPlayingText(this.getPlayingText());
		//DiscordBot.setChannelCaption(this.getCommandChannel(), this.numPlayersInQueue()+"-in-q"+ "_" + this.commandChannelBaseName);
	}

	/**Function for adding a vote to cancel the game the player is currently in. 
	 * @param player the player making the vote
	 */
	public void addCancelVote(PlayerObject player)
	{
		GatherGame game = this.getPlayersGame(player);
		if(game==null)
		{
			DiscordBot.sendMessage(this.getCommandChannel(), "You are not **in a game** to cancel "+player.getDiscordUserInfo().getDisplayName()+"!");
			return;
		}
		int returnVal = game.addCancelVote(player);
		switch(returnVal)
		{
		case 0:
			DiscordBot.sendMessage(getCommandChannel(), "Game #"+game.getGameID()+" has been canceled!", true);
			LOGGER.info("Game cancelled: "+game.getGameID());
			this.endGame(game, -2);
			return;
		case -1:
			DiscordBot.sendMessage(getCommandChannel(), "You have already voted to cancel the game "+player.getDiscordUserInfo().getDisplayName()+"("+game.getNumCancelVotes()+"/"+game.getCancelVotesReq()+")");
			return;
		}
		DiscordBot.sendMessage(getCommandChannel(), "**Vote to cancel** game has been counted for "+player.getDiscordUserInfo().getDisplayName()+" ("+returnVal+"/"+game.getCancelVotesReq()+")");
	}

	/**Function for adding a vote to scramble the teams for the game the player is currently in. 
	 * @param player the player making the vote
	 */
	public void addScrambleVote(PlayerObject player)
	{
		GatherGame game = this.getPlayersGame(player);
		if(game==null)
		{
			DiscordBot.sendMessage(this.getCommandChannel(), "You must be **in the game** to scramble "+player.getDiscordUserInfo().getDisplayName()+"!");
			return;
		}
		if(!game.getCurrState().equals(GatherGame.gameState.PREGAME))
		{
			DiscordBot.sendMessage(this.getCommandChannel(), "You cannot vote to scramble once the **game has started** "+player.getDiscordUserInfo().getDisplayName()+"!");
			return;
		}
		int returnVal = game.addScrambleVote(player);
		switch(returnVal)
		{
		case 0:
			game.doShuffle();
			DiscordBot.sendMessage(getCommandChannel(), "Teams have been shuffled for game #"+game.getGameID()+"!", true);
			DiscordBot.sendMessage(getCommandChannel(), "__**Blue**__: "+game.blueMentionList().toString());
			DiscordBot.sendMessage(getCommandChannel(), "__**Red**__:  "+game.redMentionList().toString());
			LOGGER.info("Teams shuffled: "+game.getBlueKagNames().toString()+game.getRedKagNames().toString());
			this.sortTeamRoomsAfterShuffle(game);
			this.removePlayerTeamRoles(game);
			this.addPlayersToTeamRoles(game);
			return;
		case -1:
			DiscordBot.sendMessage(getCommandChannel(), "You have already voted to scramble the teams "+player.getDiscordUserInfo().getDisplayName()+"("+game.getNumScrambleVotes()+"/"+game.getScrambleVotesReq()+")");
			return;
		}
		DiscordBot.sendMessage(getCommandChannel(), "**Vote to scramble** teams has been counted for "+player.getDiscordUserInfo().getDisplayName()+" ("+returnVal+"/"+game.getScrambleVotesReq()+")");
	}

	/**Function for doing everything needed to start a gather game. 
	 * <p>
	 * This first gets a free server and sets it in use, then creates a gather game with the current queue and shuffles the teams, then adds the game to the list of running games, 
	 * then sends appropriate messages to discord and the KAG server, then clears the queue and moves players into their team rooms
	 * TODO: make this function not block for 5 seconds while the blocking movePlayersIntoTeamRooms(5) is called
	 */
	public int startGame()
	{
		String serverId = this.getFreeServer();
		GatherServer server = this.getServer(serverId);
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
		//gather game announcement message is separate because it is text to speech
		String serverIdString = "";
		if(serverId != null && !serverId.isEmpty()) serverIdString = serverId+": ";
		String passwordString = "";
		if(server.getServerPassword()!=null && !server.getServerPassword().isEmpty()) passwordString = " with password "+server.getServerPassword();
		DiscordBot.sendMessage(getCommandChannel(), "Gather game #"+game.getGameID()+" starting on "+server.getServerName()+passwordString, true);
		if(server.getServerLink()!=null && server.getServerLink()!="") DiscordBot.sendMessage(getCommandChannel(), serverIdString + server.getServerLink());
		DiscordBot.sendMessage(getCommandChannel(), "__**Blue**__: "+game.blueMentionList().toString());
		DiscordBot.sendMessage(getCommandChannel(), "__**Red**__:  "+game.redMentionList().toString());
		LOGGER.info("Game started: "+game.getBlueKagNames().toString()+game.getRedKagNames().toString());
		game.sendTeamsToServer();
		//create the team roles
		this.generateAndSetTeamRoles(game);
		//put the players into the team roles
		this.addPlayersToTeamRoles(game);
		//do voice channel stuff
		this.movePlayersIntoTeamRooms(game, 5);
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
			//TODO: fix this? I don't remember why this is done.
			LOGGER.warn("Server is null when giving win, clearing running games (if there is more than 1 running game this is a problem)");
			clearGames();
		}
		else
		{
			setGameEnded(game);
		}
		//do voice channel stuff
		movePlayersOutOfTeamRooms(game, 5);
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
			LOGGER.warn("Scoreboard not set!");
			return;
		}
		List<StatsObject> list = DiscordBot.database.getTopPlayers(30);
		if(list == null)
		{
			LOGGER.warn("Failed to get scoreboard data from the database!");
			return;
		}
		String scoreboardString="```md\n" + "# Scoreboard #" + "\n``````diff\n++|      KAG name      |Games|  Win % | Score\n";
		int i=0;
		for(StatsObject stats : list)
		{
			i++;
			//ranking
			if(i<10) scoreboardString += " ";
			scoreboardString += i;
			scoreboardString += "|";
			//centred kagname
			String centredName = "";
			for(int j = 0; j < Math.floor((20 - stats.kagname.length()) / 2); j++) {
				centredName += " ";
			}
			centredName += stats.kagname;
			while(centredName.length() < 20) {
				centredName += " ";
			}
			scoreboardString += centredName+"| ";
			//games played
			if(stats.gamesplayed<10) scoreboardString += "  ";
			else if(stats.gamesplayed<100) scoreboardString += " ";
			scoreboardString += stats.gamesplayed+" |";
			//win percentage
			if(stats.winRateString().length()==5) scoreboardString += " ";
			else if(stats.winRateString().length()==4) scoreboardString += "  ";
			scoreboardString += stats.winRateString()+"% | ";
			//mmr
			if(stats.getMmrInteger()<10) scoreboardString += "   ";
			else if(stats.getMmrInteger()<100) scoreboardString += "  ";
			else if(stats.getMmrInteger()<1000) scoreboardString += " ";
			scoreboardString += stats.getMmrInteger()+"\n";
		}
		scoreboardString+="\n           Total games played: " +DiscordBot.database.getGamesPlayed("+numgames+")+ "```";
		if(scoreboardString.length()>2000) LOGGER.warn("SCOREBOARD IS TOO LARGE: "+scoreboardString.length());
		DiscordBot.editMessage(this.getScoreboardMessage(), scoreboardString);
	}

	/**Gets a server from the server list that is not in use. Returns the server id. 
	 * @return a String representing the server id of the server that can be used
	 */
	public String getFreeServer()
	{
		// TODO make some kind of server priority? in case of high/low ping servers?
		// TODO only get servers that the bot is currently connected to (will then also need to check if the queue is full on server (re)connect) (probably want to implement better heartbeat/reconnect system before this)
		// not important now as there should only be 1 server anyway
		for(Entry<String, GatherServer> entry : servers.entrySet())
		{
			if (!entry.getValue().isInUse()) {
				return entry.getKey();
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
		for(GatherServer server : servers.values())
		{
			if(server.getIp().equals(ip) && server.getPort() == port)
			{
				return server;
			}
		}
		return null;
	}

	/**Gets the GatherServer object for the requested server. Loops through the list of servers and compares the given string with the server id case insensitively.
	 * @param the server id to look for
	 * @return the GatherServer object if it was found, null otherwise
	 * @see #GatherServer
	 */
	public GatherServer getServer(String serverId)
	{
		return this.servers.get(serverId.toUpperCase());
		/*if(serverId == null) return null;
		for(GatherServer server : servers.values())
		{
			if(serverId.equalsIgnoreCase(server.getServerID()))
			{
				return server;
			}
		}
		return null;*/
	}

	/**Connect to the specified gather server if it is not currently connected. 
	 * <p>
	 * Outputs a message saying either connected, already connected, or failed to connect in the command channel depending on what happens.
	 * @param serverId - the gather server id of the server to connect with
	 * @return true if attempted connection, false if no server found for specified id
	 */
	public boolean connectToServer(String serverId)
	{
		return this.connectToServer(serverId, false);
	}
	
	/**Connect to the specified gather server if it is not currently connected. 
	 * <p>
	 * Outputs a message saying either connected, already connected, or failed to connect in the command channel depending on what happens, unless silent is set to true.
	 * @param serverId - the gather server id of the server to connect with
	 * @param silent - prevents status messages from being sent to the gather channel if true
	 * @return true if attempted connection, false if no server found for specified id
	 */
	public boolean connectToServer(String serverId, boolean silent)
	{
		GatherServer server = this.getServer(serverId);
		if(server == null)
		{
			return false;
		}
		if(server.isConnected())
		{
			if(!silent) DiscordBot.sendMessage(this.getCommandChannel(), "Already connected to "+serverId+" ("+server.getIp()+":"+server.getPort()+")");
		}
		else if(server.connect())
		{
			if(!silent) DiscordBot.sendMessage(this.getCommandChannel(), "Connected to "+serverId+" ("+server.getIp()+":"+server.getPort()+")");
		}
		else 
		{
			if(!silent) DiscordBot.sendMessage(this.getCommandChannel(), "Failed to connect to "+serverId+" ("+server.getIp()+":"+server.getPort()+")");
		}
		return true;
	}

	/**Disconnect from the specified gather server. 
	 * <p>
	 * Says either disconnecting, or not connected in the command channel depending on what happens.
	 * <p>
	 * Always tries to disconnect, even if no connection was detected and a "not connected" message was sent to the discord channel. (Just in case the connected status was wrong)
	 * @param serverId - the gather server id of the server to disconnect from
	 * @return true if attempted disconnection, false if no server found for specified id
	 */
	public boolean disconnectFromServer(String serverId)
	{
		return this.disconnectFromServer(serverId, false);
	}

	/**Disconnect from the specified gather server. 
	 * <p>
	 * Says either disconnecting, or not connected in the command channel depending on what happens, unless silent is set to true.
	 * <p>
	 * Always tries to disconnect, even if no connection was detected and a "not connected" message was sent to the discord channel. (Just in case the connected status was wrong)
	 * @param server - the gather server object to disconnect from
	 * @param silent - prevents status messages from being sent to the gather channel if true
	 * @return true if attempted disconnection, false if no server found for specified id
	 */
	public boolean disconnectFromServer(String serverId, boolean silent)
	{
		GatherServer server = this.getServer(serverId);
		if(server == null)
		{
			return false;
		}
		if(server.isConnected() || server.isReconnecting())
		{
			if(!silent) DiscordBot.sendMessage(this.getCommandChannel(), "Disconnecting from "+serverId+" ("+server.getIp()+":"+server.getPort()+")");
		}
		else
		{
			if(!silent) DiscordBot.sendMessage(this.getCommandChannel(), "Not connected to "+serverId+" ("+server.getIp()+":"+server.getPort()+")");
		}
		//disconnect either way (in case isConnected is wrong)
		server.disconnect();
		return true;
	}

	/**Helper function for establishing the TCPR connection with all the gather KAG servers. 
	 * Says a message in discord for each server it connects, fails to connect, or is already connected to. Unless silent is set to true.
	 * @param silent if true, this function will not print any connnection messages to the command channel
	 * @see GatherServer#connect()
	 */
	public void connectKAGServers(boolean silent)
	{
		for(String serverId : servers.keySet())
		{
			this.connectToServer(serverId, silent);
		}
	}

	/**Helper function for disconnecting all TCPR connections with the gather KAG servers. 
	 * If the bot thinks the server was already connected, it will say a message in the discord channel that it has disconnected. 
	 * @see GatherServer#disconnect()
	 */
	public void disconnectKAGServers()
	{
		for(String serverId : servers.keySet())
		{
			this.disconnectFromServer(serverId);
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
	 * @return -1 if no game was found or if the player isnt playing on the server, 1 if a sub was requested, 0 if the sub request already existed
	 * @see SubManager#addSubRequest(PlayerObject, GatherGame)
	 */
	public int addSubRequest(String kagName, String ip, int port)
	{
		PlayerObject playerToBeSubbed = DiscordBot.players.getIfExists(kagName);
		if(playerToBeSubbed==null)
		{
			this.getServer(ip, port).say(kagName+" is not in this game!");
			return -1;
		}

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
			LOGGER.error("adding server sub request encountered an error: "+returnVal+" for player: "+playerToBeSubbed+" game: "+game);
			this.getServer(ip, port).say("An error occured adding sub request for "+kagName+", this player isn't playing?");
		}
		else if(returnVal==1)
		{
			this.getServer(ip, port).say("Sub request added for player "+kagName+", use !sub "+game.getGameID()+" in Discord to sub into their place!");
			DiscordBot.sendMessage(this.getCommandChannel(), "**Sub request** added for " + playerToBeSubbed.getMentionString() + " use **!sub "+game.getGameID()+"** to sub into their place! ("+this.getQueueRole().getMention()+")");
		}
		else if(returnVal==0)
		{
			this.getServer(ip, port).say("You are already requesting a sub "+kagName+"!");
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
		PlayerObject playerVotedFor = DiscordBot.players.getIfExists(votedFor);
		PlayerObject playerVoting = DiscordBot.players.getIfExists(voting);

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
			LOGGER.info("sub requested for: "+this.fullUserString(playerVotedFor));
			this.getServer(ip, port).say("Sub request added for "+playerVotedFor.getKagName()+", use !sub "+game.getGameID()+" in Discord to sub into their place!");
			DiscordBot.sendMessage(this.getCommandChannel(), "**Sub request** added for " + playerVotedFor.getMentionString() + " use **!sub "+game.getGameID()+"** to sub into their place! ("+this.getQueueRole().getMention()+")");
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
		this.guild.getMembers().subscribe(member -> 
		{
			for(Snowflake id : member.getRoleIds())
			{
				if (id.equals(this.getQueueRole().getId()))
				{
					DiscordBot.removeRole(member, getQueueRole());
				}
			}
		});
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
	public void setInterested(Member member) {
		DiscordBot.addRole(member, this.getSoftQueueRole());
	}
	
	/**Helper function for setting a player as not interested. This means that they get the soft queue role role removed. 
	 * @param user
	 */
	public void setNotInterested(Member member) {
		DiscordBot.removeRole(member, this.getSoftQueueRole());
	}
	
	/**Toggles the users current interested state. Checks if they currently have the interested role, and removes them from it if they do, returning 2.
	 * If they do not already have the role, they are given the role returning 0. 
	 * @param user
	 * @return 2 if the user was changed to not interested, 0 otherwise
	 */
	public int toggleInterested(Member member) {
		//check if the player has the role
		List<Role> roles = member.getRoles().collectList().block();
		for(Role role : roles)
		{
			if(role.equals(this.getSoftQueueRole()))
			{
				//if they have the role, remove them from it
				setNotInterested(member);
				return 2;
			}
		}
		//otherwise give them the role
		setInterested(member);
		return 0;
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
	public void movePlayersIntoTeamRooms(GatherGame game)
	{
		VoiceChannel general = this.getGeneralVoiceChannel();
		VoiceChannel blue = this.getBlueVoiceChannel();
		VoiceChannel red = this.getRedVoiceChannel();

		for( PlayerObject p : game.getBluePlayerList())
		{
			Member member = p.getDiscordUserInfo();
			VoiceState voiceState = member.getVoiceState().block();
			if(voiceState != null && voiceState.getChannelId().isPresent() && (general.getId().equals(voiceState.getChannelId().get()) || red.getId().equals(voiceState.getChannelId().get())))
			{
				DiscordBot.moveToVoiceChannel(member, blue);
			}
		}
		for( PlayerObject p : game.getRedPlayerList())
		{
			Member member = p.getDiscordUserInfo();
			VoiceState voiceState = member.getVoiceState().block();
			if(voiceState != null && voiceState.getChannelId().isPresent() && (general.getId().equals(voiceState.getChannelId().get()) || blue.getId().equals(voiceState.getChannelId().get())))
			{
				DiscordBot.moveToVoiceChannel(member, red);
			}
		}
	}

	/**Wrapper function for moving all players out of the general voice channel into their team channels. 
	 * @param delay the number of seconds to delay for before moving
	 * @see #movePlayersIntoTeamRooms()
	 */
	public void movePlayersIntoTeamRooms(GatherGame game, int delay)
	{
		String countString = "Moving channels in ";
		Message countMsg = DiscordBot.sendMessage(this.getCommandChannel(), countString+delay);

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
		movePlayersIntoTeamRooms(game);
		DiscordBot.deleteMessage(countMsg);
	}

	/**Helper function for moving all players out of their team channels into the general voice channel. 
	 */
	public void movePlayersOutOfTeamRooms(GatherGame game)
	{
		VoiceChannel general = this.getGeneralVoiceChannel();
		VoiceChannel blue = this.getBlueVoiceChannel();
		VoiceChannel red = this.getRedVoiceChannel();

		for( PlayerObject p : game.getBluePlayerList())
		{
			Member member = p.getDiscordUserInfo();
			VoiceState voiceState = member.getVoiceState().block();
			if(voiceState != null && voiceState.getChannelId().isPresent() && (blue.getId().equals(voiceState.getChannelId().get()) || red.getId().equals(voiceState.getChannelId().get())))
			{
				DiscordBot.moveToVoiceChannel(member, general);
			}
		}
		for( PlayerObject p : game.getRedPlayerList())
		{
			Member member = p.getDiscordUserInfo();
			VoiceState voiceState = member.getVoiceState().block();
			if(voiceState != null && voiceState.getChannelId().isPresent() && (blue.getId().equals(voiceState.getChannelId().get()) || red.getId().equals(voiceState.getChannelId().get())))
			{
				DiscordBot.moveToVoiceChannel(member, general);
			}
		}

	}

	/**Wrapper function for moving all players out of their team voice channel into the general voice channel. 
	 * @param delay the number of seconds to delay for before moving
	 * @see #movePlayersIntoTeamRooms()
	 */
	public void movePlayersOutOfTeamRooms(GatherGame game, int delay)
	{
		String countString = "Moving channels in ";
		Message countMsg = DiscordBot.sendMessage(this.getCommandChannel(), countString+delay);

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
		movePlayersOutOfTeamRooms(game);
		DiscordBot.deleteMessage(countMsg);
	}

	/**Helper function for sending players to the correct voice channel after the teams have been shuffled
	 */
	public void sortTeamRoomsAfterShuffle(GatherGame game)
	{
		//just move all of the players to their correct room
		this.movePlayersIntoTeamRooms(game);
	}

	private void generateAndSetTeamRoles(GatherGame game) {
		//check there isnt already roles that should be deleted
		this.deleteTeamRoles(game);

		int queueRolePosition = this.getQueueRole().getPosition().block();
		//create the new roles
		Role blue = this.getGuild().createRole(roleSpec ->{
			roleSpec.setName("Blue Team #"+game.getGameID());
			roleSpec.setHoist(true);
			roleSpec.setMentionable(true);
		}).doOnSuccess(role -> role.changePosition(queueRolePosition+1)).block();
		Role red = this.getGuild().createRole(roleSpec ->{
			roleSpec.setName("Red Team #"+game.getGameID());
			roleSpec.setHoist(true);
			roleSpec.setMentionable(true);
		}).doOnSuccess(role -> role.changePosition(queueRolePosition+2)).block();
		blue.changePosition(queueRolePosition+1);
		red.changePosition(queueRolePosition+2);
		
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
		List<PlayerObject> players = game.getPlayerList();
		for(PlayerObject player : players)
		{
			//DiscordBot.removeRoleIfPresent(player.getDiscordUserInfo(), game.getBlueRole());
			//DiscordBot.removeRoleIfPresent(player.getDiscordUserInfo(), game.getRedRole());
			DiscordBot.removeRole(player.getDiscordUserInfo(), game.getBlueRole());
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
		return queue.getMaxQueueSize();
	}

	/**Setter for maximum queue size. 
	 * @param size the size of queue to set
	 */
	public void setMaxQueueSize(int size)
	{
		queue.setMaxQueueSize(size);
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
			returnString += game.toString();
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
			returnString+=playerString(player);
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