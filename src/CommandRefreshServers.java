import java.util.List;

import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import sx.blah.discord.handle.obj.IMessage;

/**Command for sending the current player lists to the appropriate servers. Must be used in command channel. 
 * <p>
 * Useful for when a server was not connected when a game started or when a sub was made. 
 * @author cameron
 * @see GatherGame#updateTeamsOnServer()
 */
public class CommandRefreshServers implements CommandExecutor
{
	/**The function that is called when the command is used
	 * @param message
	 * @see https://github.com/BtoBastian/sdcf4j
	 * @see #CommandRefreshServers
	 */
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