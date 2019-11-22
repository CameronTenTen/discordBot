package commands;
import java.util.Arrays;

import core.DiscordBot;
import core.GatherObject;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

/**Command for saying the current player list and queue size for the gather object associated with this channel.  Must be used in command channel. 
 * @author cameron
 * @see #GatherQueueObject
 * @see GatherObject#queueString()
 */
public class CommandList extends Command<IMessage, IUser, IChannel, IGuild>
{
	public CommandList(Commands<IMessage, IUser, IChannel, IGuild> commands)
	{
		super(commands, Arrays.asList("list", "queue"), "Check the current player list");
	}

	@Override
	public boolean isChannelValid(IChannel channel) {
		GatherObject gather = DiscordBot.getGatherObjectForChannel(channel);
		if(gather==null) return false;
		else return true;
	}

	@Override
	public String onCommand(String[] splitMessage, String messageString, IMessage messageObject, IUser user, IChannel channel, IGuild guild)
	{
		GatherObject gather = DiscordBot.getGatherObjectForChannel(channel);
		if(gather==null) return null;
		
		String currentQueue = gather.queueString();
		if(!currentQueue.isEmpty())
		{
			return "Current **queue** ("+gather.numPlayersInQueue()+"/"+gather.getMaxQueueSize()+"): "+currentQueue;
		}
		else
		{
			return "Queue is **empty**";
		}
	}
}