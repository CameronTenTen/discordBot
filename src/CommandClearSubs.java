import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import sx.blah.discord.handle.obj.IMessage;

/**
 * Admin only command for clearing all current sub requests. Must be used in command channel. 
 * Calls GatherObject.SubstitutionObject.ClearSubs()
 * @author cameron
 * @see SubstitutionObject#ClearSubs()
 */
public class CommandClearSubs implements CommandExecutor
{
	/**The function that is called when the command is used
	 * @param message
	 * @see https://github.com/BtoBastian/sdcf4j
	 * @see #CommandClearSubs
	 */
	@Command(aliases = {"!clearsubs"}, description = "Admin only - clear all current sub requests")
	public void onCommand(IMessage message)
	{
		GatherObject gather = DiscordBot.getGatherObjectForChannel(message.getChannel());
		if(gather==null) return;
		
		if(!gather.isAdmin(message.getAuthor()))
		{
			DiscordBot.sendMessage(gather.getCommandChannel(), "Only **admins** can do that "+message.getAuthor().getNicknameForGuild(message.getGuild())+"!");
			return;
		
		}
		
		gather.substitutions.clearSubs();
		DiscordBot.sendMessage(gather.getCommandChannel(), "Sub list **cleared**");
		return;
	}
}