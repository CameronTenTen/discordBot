import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import sx.blah.discord.handle.obj.IMessage;

/**Command for checking the players in currently running games.  Must be used in command channel. 
 * @author cameron
 * @see GatherObject#playersString()
 */
public class CommandPlayers implements CommandExecutor
{
	/**The function that is called when the command is used
	 * @param message
	 * @see https://github.com/BtoBastian/sdcf4j
	 * @see #CommandPlayers
	 */
	@Command(aliases = {"!players", "!teams"}, description = "Check players currently playing")
	public void onCommand(IMessage message)
	{
		GatherObject gather = DiscordBot.getGatherObjectForChannel(message.getChannel());
		if(gather==null) return;
		
		String currentPlayers = gather.playersString();
		if(!currentPlayers.isEmpty())
		{
			DiscordBot.sendMessage(gather.getCommandChannel(), "Current games: \n" + currentPlayers);
			return;
		}
		else
		{
			DiscordBot.sendMessage(gather.getCommandChannel(), "No players currently playing");
			return;
		}
	}
}