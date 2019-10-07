import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import sx.blah.discord.Discord4J;
import sx.blah.discord.handle.obj.IMessage;

/**Command for players to remove themselves from the queue. Must be used in command channel. 
 * @author cameron
 *
 */
public class CommandRem implements CommandExecutor
{
	/**The function that is called when the command is used
	 * @param message
	 * @see https://github.com/BtoBastian/sdcf4j
	 * @see #CommandRem
	 */
	@Command(aliases = {"!rem","!remove","!leave"}, description = "Remove yourself from the queue")
	public void onCommand(IMessage message)
	{
		GatherObject gather = DiscordBot.getGatherObjectForChannel(message.getChannel());
		if(gather==null) return;
		
		synchronized(gather)
		{
			int remReturnVal = gather.remFromQueue(message.getAuthor());
			switch(remReturnVal)
			{
			case 1:
				Discord4J.LOGGER.info("Removing player from queue: "+message.getAuthor().getDisplayName(message.getGuild()));
				DiscordBot.sendMessage(gather.getCommandChannel(), gather.fullUserString(message.getAuthor())+" **left** the queue! ("+gather.numPlayersInQueue()+"/"+gather.getMaxQueueSize()+")");
				return;
			case 0:
				DiscordBot.sendMessage(gather.getCommandChannel(), "You are already not in the queue "+message.getAuthor().getDisplayName(message.getGuild())+"!");
				return;
			}
			DiscordBot.sendMessage(gather.getCommandChannel(), "An unexpected error occured attempting to remove "+message.getAuthor().getDisplayName(message.getGuild())+" from the queue");
			return;
		}
	}
}