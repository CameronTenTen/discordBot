import java.util.List;

import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

/**Admin only command for removing a player from the queue. Must be used in command channel. 
 * Gets a list of all the mentions in the command and removes those players from the queue if they are in it. 
 * @author cameron
 *
 */
public class CommandForceRem implements CommandExecutor
{
	/**The function that is called when the command is used
	 * @param message
	 * @see https://github.com/BtoBastian/sdcf4j
	 * @see #CommandForceRem
	 */
	@Command(aliases = {"!forcerem"}, description = "Admin only - remove a user from the queue")
	public void onCommand(IMessage message)
	{
		GatherObject gather = DiscordBot.getGatherObjectForChannel(message.getChannel());
		if(gather==null) return;
		
		if(!gather.isAdmin(message.getAuthor()))
		{
			DiscordBot.sendMessage(gather.getCommandChannel(), "Only admins can do that "+message.getAuthor().getDisplayName(message.getGuild())+"!");
			return;
		
		}
		
		List<IUser> mentions = message.getMentions();
		for(IUser user : mentions)
		{
			if(1==gather.remFromQueue(user))
			{
				DiscordBot.sendMessage(gather.getCommandChannel(), gather.fullUserString(user)+" was **removed** from the queue (admin) ("+gather.numPlayersInQueue()+"/"+gather.getMaxQueueSize()+")");
			}
		}
		
		return;
	}
}