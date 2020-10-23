package commands;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import core.DiscordBot;
import core.GatherObject;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.Channel;

/**Admin only command for removing a player from the queue. Must be used in command channel. 
 * Gets a list of all the mentions in the command and removes those players from the queue if they are in it. 
 * @author cameron
 *
 */
public class CommandForceRem extends Command<Message, Member, Channel>
{
	public CommandForceRem(Commands<Message, Member, Channel> commands)
	{
		super(commands, Arrays.asList("forcerem"), "Admin only - remove a user from the queue", "forcerem @user...");
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

		List<Member> mentions = messageObject.getUserMentionIds().stream().map((snowflake) -> DiscordBot.fetchMember(gather.getGuild().getId(), snowflake)).collect(Collectors.toList());
		for(Member mentionedUser : mentions)
		{
			if(1==gather.remFromQueue(mentionedUser))
			{
				DiscordBot.sendMessage(gather.getCommandChannel(), gather.fullUserString(mentionedUser)+" was **removed** from the queue (admin) ("+gather.numPlayersInQueue()+"/"+gather.getMaxQueueSize()+")");
			}
		}
		return null;
	}
}