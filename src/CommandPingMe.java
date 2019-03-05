import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import sx.blah.discord.handle.obj.IMessage;

/**
 * Chat command for players to add to/remove from the soft queue/ping list. Must be used in command channel. 
 * Prints a response message to the command channel depending on the result of the command.
 * The soft queue is for players to join if they are interested in a game but do not want to commit to a full queue.
 * This means that they do not mind other players mentioning them
 * <p>
 * 
 * @author cameron
 *
 */
public class CommandPingMe implements CommandExecutor
{
	/**The function that is called when the command is used
	 * @param message
	 * @see https://github.com/BtoBastian/sdcf4j
	 * @see #CommandPingMe
	 */
	@Command(aliases = {"!pingme","!softqueue","!softadd","!soft","!interested","!interest","!int","!uninterested","!uninterest", "!unint", "!role", "!gather"}, description = "Add or remove yourself from the soft queue")
	public void onCommand(IMessage message)
	{
		GatherObject gather = DiscordBot.getGatherObjectForChannel(message.getChannel());
		if(gather==null) return;

		PlayerObject player = DiscordBot.players.getObject(message.getAuthor());
		if(player==null)
		{
			DiscordBot.sendMessage(gather.getCommandChannel(), "You must be linked to play gather " + message.getAuthor().getDisplayName(message.getGuild()) + "! Use **!link KAGUsernameHere** to get started or **!linkhelp** for more information");
			return;
		}
		
		int val = gather.toggleInterested(message.getAuthor());
		
		switch(val)
		{
		case 2:
			DiscordBot.sendMessage(gather.getCommandChannel(), message.getAuthor().getDisplayName(message.getGuild())+" is no longer **interested**");
			return;
		case 0:
			DiscordBot.sendMessage(gather.getCommandChannel(), message.getAuthor().getDisplayName(message.getGuild())+" is **interested** they want to be notified when there is enough players for a game!");
			return;
			
		}
	}
}