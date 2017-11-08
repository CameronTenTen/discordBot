import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import sx.blah.discord.handle.obj.IMessage;

/**Admin only command for sending all players in the gather general voice channel into their team rooms. Must be used in command channel. 
 * @author cameron
 *@see GatherObject#movePlayersIntoTeamRooms()
 */
public class CommandStart implements CommandExecutor
{
	/**The function that is called when the command is used
	 * @param message
	 * @see https://github.com/BtoBastian/sdcf4j
	 * @see #CommandStart
	 */
	@Command(aliases = {"!start"}, description = "Admin only - move users from general chat to team rooms")
	public void onCommand(IMessage message)
	{
		GatherObject gather = DiscordBot.getGatherObjectForChannel(message.getChannel());
		if(gather==null) return;

		if(!gather.isAdmin(message.getAuthor()))
		{
			DiscordBot.sendMessage(gather.getCommandChannel(), "Only **admins** can do that "+message.getAuthor().getNicknameForGuild(message.getGuild())+"!");
			return;
		
		}
		DiscordBot.sendMessage(gather.getCommandChannel(), "Moving players in to team rooms");
		gather.movePlayersIntoTeamRooms();
		return;
	}
}