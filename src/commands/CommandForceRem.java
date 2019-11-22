package commands;
import java.util.Arrays;
import java.util.List;

import core.DiscordBot;
import core.GatherObject;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

/**Admin only command for removing a player from the queue. Must be used in command channel. 
 * Gets a list of all the mentions in the command and removes those players from the queue if they are in it. 
 * @author cameron
 *
 */
public class CommandForceRem extends Command<IMessage, IUser, IChannel, IGuild>
{
	public CommandForceRem(Commands<IMessage, IUser, IChannel, IGuild> commands)
	{
		super(commands, Arrays.asList("forcerem"), "Admin only - remove a user from the queue", "forcerem @user...");
	}

	@Override
	public boolean isChannelValid(IChannel channel) {
		GatherObject gather = DiscordBot.getGatherObjectForChannel(channel);
		if(gather==null) return false;
		else return true;
	}

	@Override
	public boolean hasPermission(IUser user, IChannel channel, IGuild guild)
	{
		GatherObject gather = DiscordBot.getGatherObjectForChannel(channel);
		if(gather==null) return false;
		return gather.isAdmin(user);
	}

	@Override
	public String onCommand(String[] splitMessage, String messageString, IMessage messageObject, IUser user, IChannel channel, IGuild guild)
	{
		GatherObject gather = DiscordBot.getGatherObjectForChannel(channel);
		if(gather==null) return null;

		List<IUser> mentions = messageObject.getMentions();
		for(IUser mentionedUser : mentions)
		{
			if(1==gather.remFromQueue(mentionedUser))
			{
				DiscordBot.sendMessage(gather.getCommandChannel(), gather.fullUserString(mentionedUser)+" was **removed** from the queue (admin) ("+gather.numPlayersInQueue()+"/"+gather.getMaxQueueSize()+")");
			}
		}
		return null;
	}
}