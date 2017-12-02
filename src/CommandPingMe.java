import java.util.List;

import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IRole;

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
	@Command(aliases = {"!pingme","!softqueue","!softadd","!soft","!interested","!interest"}, description = "Add or remove yourself from the soft queue")
	public void onCommand(IMessage message)
	{
		GatherObject gather = DiscordBot.getGatherObjectForChannel(message.getChannel());
		if(gather==null) return;
		
		//check if the player has the role
		List<IRole> roles = message.getAuthor().getRolesForGuild(gather.getGuild());
		for(IRole role : roles)
		{
			if(role.equals(gather.getSoftQueueRole()))
			{
				//if they have the role, remove them from it
				DiscordBot.removeRole(message.getAuthor(), gather.getSoftQueueRole());
				DiscordBot.sendMessage(gather.getCommandChannel(), message.getAuthor().getDisplayName(message.getGuild())+" has been **removed** from the **soft queue**");
				return;
			}
		}
		//otherwise give them the role
		DiscordBot.addRole(message.getAuthor(), gather.getSoftQueueRole());
		DiscordBot.sendMessage(gather.getCommandChannel(), message.getAuthor().getDisplayName(message.getGuild())+" has been **added** to the **soft queue**, type the same command again to leave it");
	}
}