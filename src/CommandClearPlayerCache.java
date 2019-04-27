import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import sx.blah.discord.handle.obj.IMessage;

/**Admin command for clearing the current player cache of all unused player objects. Must be used in command channel. 
 * @author cameron
 * @see PlayerObjectManager#clearPlayerCache()
 */
public class CommandClearPlayerCache implements CommandExecutor
{
	/**The function that is called when the command is used
	 * @param message
	 * @param args
	 * @see https://github.com/BtoBastian/sdcf4j
	 * @see #CommandClearPlayerCache
	 */
	@Command(aliases = {"!clearPlayerCache", "!clear_player_cache"}, description = "Admin only - clear current player cache of all unused player objects")
	public void onCommand(IMessage message, String[] args)
	{
		GatherObject gather = DiscordBot.getGatherObjectForChannel(message.getChannel());
		if(gather==null) return;

		if(!gather.isAdmin(message.getAuthor()))
		{
			DiscordBot.sendMessage(gather.getCommandChannel(), "Only **admins** can do that" + " "+message.getAuthor().getDisplayName(message.getGuild())+"!");
			return;
		
		}
		DiscordBot.players.clearPlayerCache();
		DiscordBot.sendMessage(gather.getCommandChannel(), "Cleared Cache: " + DiscordBot.players.listPlayerCache());
	}
}