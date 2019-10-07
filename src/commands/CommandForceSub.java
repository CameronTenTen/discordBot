import java.util.List;

import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import sx.blah.discord.Discord4J;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

/**Admin only command for subbing a player from the game. Must be used in command channel. 
 * Gets a list of all the mentions in the command and subs those players from the game if they are in it. 
 * @author epsilon
 *
 */
public class CommandForceSub implements CommandExecutor
{
	/**The function that is called when the command is used
	 * @param message
	 * @see https://github.com/BtoBastian/sdcf4j
	 * @see #CommandForceSub
	 */
	@Command(aliases = {"!forcesub"}, description = "Admin only - sub out a user from the game")
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
			if(1==gather.substitutions.addSubRequest(user, gather.getPlayersGame(user)))
			{
				Discord4J.LOGGER.info("sub requested for: "+user.getDisplayName(message.getGuild()));
				DiscordBot.sendMessage(gather.getCommandChannel(), "**Sub request** added for " + user.mention() + " use **!sub "+gather.getPlayersGame(user).getGameID()+"** to sub into their place! ("+gather.getQueueRole().mention()+")");
				return;
			}
		}
		
		return;
	}
}