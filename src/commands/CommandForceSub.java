package commands;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import core.DiscordBot;
import core.GatherObject;
import discord4j.core.object.entity.Channel;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.Member;

/**Admin only command for subbing a player from the game. Must be used in command channel. 
 * Gets a list of all the mentions in the command and subs those players from the game if they are in it. 
 * @author epsilon
 *
 */
public class CommandForceSub extends Command<Message, Member, Channel>
{
	static final Logger LOGGER = LoggerFactory.getLogger(CommandForceSub.class);
	public CommandForceSub(Commands<Message, Member, Channel> commands)
	{
		super(commands, Arrays.asList("forcesub"), "Admin only - sub out a user from the game", "forcesub @user...");
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
			if(1==gather.substitutions.addSubRequest(mentionedUser, gather.getPlayersGame(mentionedUser)))
			{
				LOGGER.info("sub requested for: "+mentionedUser.getDisplayName());
				return "**Sub request** added for " + mentionedUser.getMention() + " use **!sub "+gather.getPlayersGame(mentionedUser).getGameID()+"** to sub into their place! ("+gather.getQueueRole().getMention()+")";
			}
		}
		return null;
	}
}