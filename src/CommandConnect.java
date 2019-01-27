import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import sx.blah.discord.handle.obj.IMessage;

/**Rough admin command for making the discord bot connect to all disconnected KAG servers(only tries if it knows that it is disconnected). Must be used in command channel. 
 * @author cameron
 * @see GatherObject#disconnectKAGServers()
 * @see GatherObject#connectKAGServers()
 */
public class CommandConnect implements CommandExecutor
{
	/**The function that is called when the command is used
	 * @param message
	 * @param args
	 * @see https://github.com/BtoBastian/sdcf4j
	 * @see #CommandReconnect
	 */
	@Command(aliases = {"!connect", "!conn", "!con"}, description = "Admin only - connect to any disconnected kag servers")
	public void onCommand(IMessage message, String[] args)
	{
		GatherObject gather = DiscordBot.getGatherObjectForChannel(message.getChannel());
		if(gather==null) return;

		if(!gather.isAdmin(message.getAuthor()))
		{
			DiscordBot.sendMessage(gather.getCommandChannel(), "Only **admins** can do that" + " "+message.getAuthor().getDisplayName(message.getGuild())+"!");
			return;
		
		}

		gather.connectKAGServersIfDisconnected();
		return;
	}
}