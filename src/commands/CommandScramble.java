package commands;
import java.util.Arrays;

import core.DiscordBot;
import core.GatherObject;
import core.PlayerObject;
import discord4j.core.object.entity.Channel;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.Member;

/**Command for voting to scramble the teams of a game. Must be used in command channel. 
 * @author cameron
 * @see GatherObject#addScrambleVote(PlayerObject)
 */
public class CommandScramble extends Command<Message, Member, Channel>
{
	public CommandScramble(Commands<Message, Member, Channel> commands)
	{
		super(commands, Arrays.asList("scramble"), "Vote to scramble the teams");
	}

	@Override
	public boolean isChannelValid(Channel channel) {
		GatherObject gather = DiscordBot.getGatherObjectForChannel(channel);
		if(gather==null) return false;
		else return true;
	}

	@Override
	public String onCommand(String[] splitMessage, String messageString, Message messageObject, Member member, Channel channel)
	{
		GatherObject gather = DiscordBot.getGatherObjectForChannel(channel);
		if(gather==null) return null;
		gather.addScrambleVote(DiscordBot.players.getIfExists(member));
		return null;
	}
}