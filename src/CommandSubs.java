import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import sx.blah.discord.handle.obj.IMessage;

/**Command for checking the current sub requests. Must be used in command channel. 
 * @author cameron
 * @see SubManager#toString()
 */
public class CommandSubs implements CommandExecutor
{
	/**The function that is called when the command is used
	 * @param message
	 * @see https://github.com/BtoBastian/sdcf4j
	 * @see #CommandSubs
	 */
	@Command(aliases = {"!subs"}, description = "Check current sub requests")
	public void onCommand(IMessage message)
	{
		GatherObject gather = DiscordBot.getGatherObjectForChannel(message.getChannel());
		if(gather==null) return;
		
		String currentSubs = gather.substitutions.toString();
		if(!currentSubs.isEmpty())
		{
			DiscordBot.sendMessage(gather.getCommandChannel(), "There is currently sub requests for: "+currentSubs);
			return;
		}
		else
		{
			DiscordBot.sendMessage(gather.getCommandChannel(), "No subs currently requested");
			return;
		}
	}
}