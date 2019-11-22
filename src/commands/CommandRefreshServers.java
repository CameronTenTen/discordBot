package commands;
import java.util.Arrays;
import java.util.List;

import core.DiscordBot;
import core.GatherGame;
import core.GatherObject;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

/**Command for sending the current player lists to the appropriate servers. Must be used in command channel. 
 * <p>
 * Useful for when a server was not connected when a game started or when a sub was made. 
 * @author cameron
 * @see GatherGame#updateTeamsOnServer()
 */
public class CommandRefreshServers extends Command<IMessage, IUser, IChannel, IGuild>
{
	public CommandRefreshServers(Commands<IMessage, IUser, IChannel, IGuild> commands)
	{
		super(commands, Arrays.asList("refreshservers", "refreshplayers", "refresh", "refreshgames", "refreshteams"), "Admin only - Refresh the player list in currently running games, useful in case of a server disconnect");
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

		List<GatherGame> games = gather.getRunningGames();
		if(games.isEmpty())
		{
			return "There is currently **no running games** to refresh";
		}
		for(GatherGame game : games)
		{
			game.updateTeamsOnServer();
			return "**Sent current teams** to "+game.getServerIp()+":"+game.getServerPort();
		}
		return null;
	}
}