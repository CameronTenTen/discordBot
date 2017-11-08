import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import sx.blah.discord.handle.obj.IMessage;

/**
 * Admin only command for clearing the current gather queue. Must be used in command channel.
 * Calls GatherObject.clearQueue()
 * @author cameron
 * @see GatherObject#clearQueue()
 */
public class CommandClearQueue implements CommandExecutor
{
	/**The function that is called when the command is used
	 * @param message
	 * @see https://github.com/BtoBastian/sdcf4j
	 * @see #CommandClearQueue
	 */
	@Command(aliases = {"!clearqueue"}, description = "Admin only - clear the queue")
	public void onCommand(IMessage message)
	{
		GatherObject gather = DiscordBot.getGatherObjectForChannel(message.getChannel());
		if(gather==null) return;
		
		if(!gather.isAdmin(message.getAuthor()))
		{
			DiscordBot.sendMessage(gather.getCommandChannel(), "Only **admins** can do that "+message.getAuthor().getNicknameForGuild(message.getGuild())+"!");
			return;
		
		}
		
		gather.clearQueue();
		DiscordBot.sendMessage(gather.getCommandChannel(), "Queue is now **empty**");
		return;
	}
}