import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import sx.blah.discord.handle.obj.IMessage;

/**Admin command for making the discord bot connect to one or all disconnected KAG servers(only tries if it knows that it is disconnected). Must be used in command channel. 
 * @author cameron
 * @see GatherObject#connectKAGServers()
 * @see GatherObject#connectToServer(serverId)
 */
public class CommandConnect implements CommandExecutor
{
	/**The function that is called when the command is used
	 * @param message
	 * @param args
	 * @see https://github.com/BtoBastian/sdcf4j
	 * @see #CommandReconnect
	 */
	@Command(aliases = {"!connect", "!conn", "!con"}, description = "Admin only - connect to a kag server, or all kag servers")
	public void onCommand(IMessage message, String[] args)
	{
		GatherObject gather = DiscordBot.getGatherObjectForChannel(message.getChannel());
		if(gather==null) return;

		if(!gather.isAdmin(message.getAuthor()))
		{
			DiscordBot.sendMessage(gather.getCommandChannel(), "Only **admins** can do that" + " "+message.getAuthor().getDisplayName(message.getGuild())+"!");
			return;
		
		}
		if(args.length==0)
		{
			DiscordBot.sendMessage(gather.getCommandChannel(), "Incorrect parameters, usage is: !connect serverID or !connect ALL");
			return;
		}
		else if(args.length>0)
		{
			String serverID = args[0];
			if("ALL".equalsIgnoreCase(serverID)) {
				DiscordBot.sendMessage(gather.getCommandChannel(), "Connecting to all servers:");
				gather.connectKAGServers(false);
				return;
			}
			else
			{
				if(!gather.connectToServer(serverID))
				{
					DiscordBot.sendMessage(gather.getCommandChannel(), "Could not find server id \""+serverID+"\", usage is !connect serverID or !connect ALL");
				}
			}
		}
	}
}