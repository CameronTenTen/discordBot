import java.util.List;

import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import sx.blah.discord.handle.obj.IMessage;

public class CommandRefreshServers implements CommandExecutor
{
	@Command(aliases = {"!refreshservers"}, description = "Refresh the player list in currently running games, useful in case of a server disconnect")
	public void onCommand(IMessage message)
	{
		GatherObject gather = DiscordBot.getGatherObjectForChannel(message.getChannel());
		if(gather==null) return;

		if(!gather.isAdmin(message.getAuthor()))
		{
			DiscordBot.sendMessage(gather.getCommandChannel(), "Only **admins** can do that "+message.getAuthor().getNicknameForGuild(message.getGuild())+"!");
			return;
		}
		List<GatherGame> games = gather.getRunningGames();
		for(GatherGame game : games)
		{
			game.updateTeamsOnServer();
		}
	}
}