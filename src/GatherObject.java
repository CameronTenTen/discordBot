import java.util.List;

import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;

public class GatherObject
{
	private GatherQueueObject queue;
	
	private IGuild guild;
	private IChannel commandChannel;
	public String textChannelString = "gather-general";
	
	GatherObject()
	{
		queue = new GatherQueueObject();
	}
	
	GatherObject(IGuild guild)
	{
		queue = new GatherQueueObject();
		setGuild(guild);
	}
	
	public IGuild getGuild() {
		return guild;
	}

	public void setGuild(IGuild guild) {
		this.guild = guild;
		
		//search text channels for a command channel
		List<IChannel> channels = guild.getChannels();
		
		for(IChannel channel : channels)
		{
			if(channel.getName().contains(textChannelString))
			{
				commandChannel = channel;
				return;
			}
		}
		//no command channel found
		System.out.println("Error: no command channel found for guild: "+guild.getName());
		
	}

	public IChannel getCommandChannel() {
		return commandChannel;
	}

	public void setCommandChannel(IChannel commandChannel) {
		this.commandChannel = commandChannel;
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
	
	public String getMentionList()
	{
		String returnString="";
		for(PlayerObject player : queue)
		{
			returnString += " ";
			returnString += player.getDiscordUserInfo().mention();
		}
		return returnString;
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