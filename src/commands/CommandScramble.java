package commands;
import java.util.Arrays;

import core.DiscordBot;
import core.GatherObject;
import core.PlayerObject;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

/**Command for voting to scramble the teams of a game. Must be used in command channel. 
 * @author cameron
 * @see GatherObject#addScrambleVote(PlayerObject)
 */
public class CommandScramble extends Command<IMessage, IUser, IChannel, IGuild>
{
	public CommandScramble(Commands<IMessage, IUser, IChannel, IGuild> commands)
	{
		super(commands, Arrays.asList("scramble"), "Vote to scramble the teams");
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
		gather.addScrambleVote(DiscordBot.players.getIfExists(user));
		return null;
	}
}