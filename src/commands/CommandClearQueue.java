package commands;
import java.util.Arrays;

import core.DiscordBot;
import core.GatherObject;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.Channel;

/**
 * Admin only command for clearing the current gather queue. Must be used in command channel.
 * Calls GatherObject.clearQueue()
 * @author cameron
 * @see GatherObject#clearQueue()
 */
public class CommandClearQueue extends Command<Message, Member, Channel>
{
	public CommandClearQueue(Commands<Message, Member, Channel> commands)
	{
		super(commands, Arrays.asList("clearqueue"), "Admin only - clear the queue");
	}

	@Override
	public boolean isChannelValid(Channel channel) {
		GatherObject gather = DiscordBot.getGatherObjectForChannel(channel);
		if(gather==null) return false;
		else return true;
	}

	@Override
	public boolean hasPermission(Member member, Channel channel)
	{
		GatherObject gather = DiscordBot.getGatherObjectForChannel(channel);
		if(gather==null) return false;
		return gather.isAdmin(member);
	}

	@Override
	public String onCommand(String[] splitMessage, String messageString, Message messageObject, Member member, Channel channel)
	{
		GatherObject gather = DiscordBot.getGatherObjectForChannel(channel);
		if(gather==null) return null;

		gather.clearQueue();
		return "Queue is now **empty**";
	}
}