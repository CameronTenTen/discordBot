import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import sx.blah.discord.handle.obj.IMessage;

/**Admin command for making the discord bot to disconnect from one or all KAG servers. If the bot thinks it is already disconnected, it will say so, but still try to disconnect anyway. 
 * Must be used in command channel. 
 * @author cameron
 * @see GatherObject#disconnectKAGServers()
 * @see GatherObject#disconnectFromServer(serverId)
 */
public class CommandDisconnect implements CommandExecutor
{
	/**The function that is called when the command is used
	 * @param message
	 * @param args
	 * @see https://github.com/BtoBastian/sdcf4j
	 * @see #CommandDisconnect
	 */
	@Command(aliases = {"!disconnect", "!disconn", "!discon"}, description = "Admin only - disconnect from a kag server, or all kag servers")
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
			DiscordBot.sendMessage(gather.getCommandChannel(), "Incorrect parameters, usage is: !disconnect serverID or !disconnect ALL");
			return;
		}
		else if(args.length>0)
		{
			String serverID = args[0];
			if("ALL".equalsIgnoreCase(serverID)) {
				DiscordBot.sendMessage(gather.getCommandChannel(), "Disconnecting from all servers:");
				gather.disconnectKAGServers();
				return;
			}
			else
			{
				if(!gather.disconnectFromServer(serverID))
				{
					DiscordBot.sendMessage(gather.getCommandChannel(), "Could not find server id \""+serverID+"\", usage is !disconnect serverID or !disconnect ALL");
				}
			}
		}
	}
}