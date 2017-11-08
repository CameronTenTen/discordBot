import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import sx.blah.discord.handle.obj.IMessage;

/**
 * Admin only command for moving all players out of the red and blue voice channels into the general channel. Must be used in command channel. 
 * Calls GatherObject.movePlayersOutOfTeamRooms()
 * @author cameron
 * @see GatherObject#movePlayersOutOfTeamRooms()
 */
public class CommandEnd implements CommandExecutor
{
	/**The function that is called when the command is used
	 * @param message
	 * @see https://github.com/BtoBastian/sdcf4j
	 * @see #CommandEnd
	 */
	@Command(aliases = {"!end"}, description = "Admin only - move users from team chat to general chat")
	public void onCommand(IMessage message)
	{
		GatherObject gather = DiscordBot.getGatherObjectForChannel(message.getChannel());
		if(gather==null) return;

		if(!gather.isAdmin(message.getAuthor()))
		{
			DiscordBot.sendMessage(gather.getCommandChannel(), "Only **admins** can do that "+message.getAuthor().getNicknameForGuild(message.getGuild())+"!");
			return;
		
		}
		DiscordBot.sendMessage(gather.getCommandChannel(), "Moving players out of team rooms");
		gather.movePlayersOutOfTeamRooms();
		return;
	}
}