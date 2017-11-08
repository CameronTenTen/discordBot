import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.MessageTokenizer;

/**Admin only command for setting queue size. Useful for testing. Must be used in command channel. 
 * @author cameron
 *
 */
public class CommandSetQueue implements CommandExecutor
{
	/**The function that is called when the command is used
	 * @param message
	 * @see https://github.com/BtoBastian/sdcf4j
	 * @see #CommandSetQueue
	 */
	@Command(aliases = {"!setqueue"}, description = "Admin only - change the queue size")
	public void onCommand(IMessage message)
	{
		GatherObject gather = DiscordBot.getGatherObjectForChannel(message.getChannel());
		if(gather==null) return;
		
		if(!gather.isAdmin(message.getAuthor()))
		{
			DiscordBot.sendMessage(gather.getCommandChannel(), "Only **admins** can do that "+message.getAuthor().getNicknameForGuild(message.getGuild())+"!");
			return;
		
		}
		
		int newSize;
		MessageTokenizer tokens = message.tokenize();
		tokens.nextWord();
		//use the second argument as the queue size
		if(!tokens.hasNextWord())
		{
			DiscordBot.sendMessage(gather.getCommandChannel(), "Invalid command format, queue size as a number must be provided");
			return;
		}
		try
		{
			newSize = Integer.parseInt(tokens.nextWord().toString());
		}
		catch (NumberFormatException e)
		{
			DiscordBot.sendMessage(gather.getCommandChannel(), "Invalid command format, queue size as a number must be provided");
			e.printStackTrace();
			return;
		}
		if(newSize<=gather.numPlayersInQueue())
		{
			DiscordBot.sendMessage(gather.getCommandChannel(), "Cannot set queue size less than or equal to current queue size: "+gather.numPlayersInQueue());
			return;
		}
		gather.setMaxQueueSize(newSize);
		DiscordBot.sendMessage(gather.getCommandChannel(), "Queue size has been set to "+gather.getMaxQueueSize());
		
		gather.updateChannelCaption();
		return;
	}
}