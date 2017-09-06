import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.vdurmont.emoji.Emoji;
import com.vdurmont.emoji.EmojiManager;

import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IEmoji;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IReaction;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.IVoiceChannel;

public class GatherObject
{
	private GatherQueueObject queue;
	
	private IGuild guild;
	private IChannel commandChannel = null;
	private IRole adminRole;
	private IVoiceChannel blueVoiceChannel;
	private IVoiceChannel redVoiceChannel;
	private IVoiceChannel generalVoiceChannel;
	public String textChannelString = "gather-general";
	public String adminString = "Gather Admin";
	public String blueVoiceString = "Gather Team BLUE";
	public String redVoiceString = "Gather Team RED";
	public String generalVoiceString = "Gather General";
	
	public String voiceChatEmote = "teamColour";
	public IMessage lastMatchStartMessage;
	
	//temporary arrays for teams
	//TODO: move these to a game object
	ArrayList<String> list;
	List<String> blueTeam;
	List<String> redTeam;
	
	GatherObject(IGuild guild)
	{
		queue = new GatherQueueObject();
		setGuild(guild);
		
	}
	
	public IGuild getGuild() {
		return guild;
	}

	public void setGuild(IGuild guild)
	{
		this.guild = guild;
		
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

	public IChannel getCommandChannel() {
		return commandChannel;
	}

	public void setCommandChannel(IChannel commandChannel) {
		this.commandChannel = commandChannel;
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
	
	public void shuffleTeams()
	{
		Collections.shuffle(list);
		blueTeam = list.subList(0, list.size()/2);
		redTeam = list.subList(list.size()/2, list.size());
	}
	
	public void startGame()
	{
		//set the teams
		list = this.getMentionList();
		this.shuffleTeams();
		//setup the game
		//TODO: once server communication has been implemented more game setup is needed here
		
		//announce the game
		//do the team messages in seperate lines so that it highlights the players team

		IEmoji emoji = guild.getEmojiByName(voiceChatEmote);
		DiscordBot.bot.removeMessageReactWatch(lastMatchStartMessage, emoji);
		
		lastMatchStartMessage = getCommandChannel().sendMessage("Gather game starting with teams:");

		lastMatchStartMessage.addReaction(emoji);
		DiscordBot.bot.addMessageReactWatch(lastMatchStartMessage, emoji, (GenericReactCallbackObject) new goToTeamVoiceChannel());
		
		getCommandChannel().sendMessage("__**Blue**__: "+blueTeam.toString());
		getCommandChannel().sendMessage("__**Red**__:  "+redTeam.toString());
		//reset the queue
		this.clearQueue();
	}
	
	public IVoiceChannel getTeamVoiceChannel(IUser user)
	{
		if(0==getTeam(user))return getBlueVoiceChannel();
		else if(1==getTeam(user))return getRedVoiceChannel();
		return null;
	}
	
	public int getTeam(IUser user)
	{
		if(blueTeam.contains(user.mention())) return 0;
		else if(redTeam.contains(user.mention())) return 1;
		return -1;
	}
	
	public void clearQueue()
	{
		queue.clear();
	}
	
	public int numPlayersInQueue()
	{
		return queue.numPlayersInQueue();
	}
	
	public boolean isQueueFull()
	{
		return queue.isFull();
	}
	
	public int maxQueueSize()
	{
		return GatherQueueObject.maxQueueSize;
	}
	
	public String getMentionString()
	{
		String returnString="";
		for(PlayerObject player : queue)
		{
			returnString += " ";
			returnString += player.getDiscordUserInfo().mention();
		}
		return returnString;
	}
	
	public ArrayList<String> getMentionList()
	{
		ArrayList<String> returnList = new ArrayList<String>();
		for(PlayerObject player : queue)
		{
			returnList.add(player.getDiscordUserInfo().mention());
		}
		return returnList;
	}
	
	public String queueString()
	{
		return queue.toString();
	}
	
	@Override
	public boolean equals(Object obj)
	{
		return (this.guild == ((GatherObject)obj).guild);
	}
}