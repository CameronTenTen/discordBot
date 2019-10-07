import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import sx.blah.discord.handle.obj.IMessage;

/**Command for checking the status of any currently running games. Must be used in command channel. 
 * @author cameron
 * @see GatherObject#statusString()
 */
public class CommandStatus implements CommandExecutor
{
	/**The function that is called when the command is used
	 * @param message
	 * @see https://github.com/BtoBastian/sdcf4j
	 * @see #CommandStatus
	 */
	@Command(aliases = {"!status"}, description = "Check status of current games")
	public void onCommand(IMessage message)
	{
		GatherObject gather = DiscordBot.getGatherObjectForChannel(message.getChannel());
		if(gather==null) return;
		
		String currentStatus = gather.statusString();
		if(!currentStatus.isEmpty())
		{
			DiscordBot.sendMessage(gather.getCommandChannel(), "Current games: \n" + currentStatus);
			return;
		}
		else
		{
			DiscordBot.sendMessage(gather.getCommandChannel(), "No games currently running");
			return;
		}
	}
}