package commands;
import java.util.Arrays;

import core.DiscordBot;
import core.GatherObject;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

/**Command for checking the players in currently running games.  Must be used in command channel. 
 * @author cameron
 * @see GatherObject#playersString()
 */
public class CommandPlayers extends Command<IMessage, IUser, IChannel, IGuild>
{
	public CommandPlayers(Commands<IMessage, IUser, IChannel, IGuild> commands)
	{
		super(commands, Arrays.asList("players", "teams"), "Check players currently playing");
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
		
		String currentPlayers = gather.playersString();
		if(!currentPlayers.isEmpty())
		{
			return "Current games: \n" + currentPlayers;
		}
		else
		{
			return "No players currently playing";
		}
	}
}