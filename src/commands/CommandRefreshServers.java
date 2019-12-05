package commands;
import java.util.Arrays;
import java.util.List;

import core.DiscordBot;
import core.GatherGame;
import core.GatherObject;
import discord4j.core.object.entity.Channel;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.Member;

/**Command for sending the current player lists to the appropriate servers. Must be used in command channel.
 * <p>
 * Useful for when a server was not connected when a game started or when a sub was made.
 * @author cameron
 * @see GatherGame#updateTeamsOnServer()
 */
public class CommandRefreshServers extends Command<Message, Member, Channel>
{
	public CommandRefreshServers(Commands<Message, Member, Channel> commands)
	{
		super(commands, Arrays.asList("refreshservers", "refreshplayers", "refresh", "refreshgames", "refreshteams"), "Admin only - Refresh the player list in currently running games, useful in case of a server disconnect");
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
