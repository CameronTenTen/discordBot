import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import sx.blah.discord.handle.obj.IMessage;

/**Command for saying the current player list and queue size for the gather object associated with this channel.  Must be used in command channel. 
 * @author cameron
 * @see #GatherQueueObject
 * @see GatherObject#queueString()
 */
public class CommandList implements CommandExecutor
{
	/**The function that is called when the command is used
	 * @param message
	 * @see https://github.com/BtoBastian/sdcf4j
	 * @see #CommandList
	 */
	@Command(aliases = {"!list"}, description = "Check the current player list")
	public void onCommand(IMessage message)
	{
		GatherObject gather = DiscordBot.getGatherObjectForChannel(message.getChannel());
		if(gather==null) return;
		
		String currentQueue = gather.queueString();
		if(!currentQueue.isEmpty())
		{
			DiscordBot.sendMessage(gather.getCommandChannel(), "Current **queue** ("+gather.numPlayersInQueue()+"/"+gather.getMaxQueueSize()+"): "+currentQueue);
			return;
		}
		else
		{
			DiscordBot.sendMessage(gather.getCommandChannel(), "Queue is **empty**");
			return;
		}
	}
}