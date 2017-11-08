import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import sx.blah.discord.handle.obj.IMessage;

/**
 * Admin only command for clearing all currently running games for a gather object. Must be used in command channel. 
 * Calls GatherObject.clearGames()
 * @author cameron
 * @see GatherObject#clearGames()
 */
public class CommandClearGames implements CommandExecutor
{
	/**The function that is called when the command is used
	 * @param message
	 * @see https://github.com/BtoBastian/sdcf4j
	 * @see #CommandClearGames
	 */
	@Command(aliases = {"!cleargames"}, description = "Admin only - clear all currently running games")
	public void onCommand(IMessage message)
	{
		GatherObject gather = DiscordBot.getGatherObjectForChannel(message.getChannel());
		if(gather==null) return;
		
		if(!gather.isAdmin(message.getAuthor()))
		{
			DiscordBot.sendMessage(gather.getCommandChannel(), "Only **admins** can do that "+message.getAuthor().getNicknameForGuild(message.getGuild())+"!");
			return;
		
		}
		
		gather.clearGames();
		DiscordBot.sendMessage(gather.getCommandChannel(), "cleared all currently running games");
		return;
	}
}