import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import sx.blah.discord.handle.obj.IMessage;

public class CommandReconnect implements CommandExecutor
{
	@Command(aliases = {"!reconnect"}, description = "Admin only - reconnect to the kag servers")
	public void onCommand(IMessage message, String[] args)
	{
		GatherObject gather = DiscordBot.getGatherObjectForChannel(message.getChannel());
		if(gather==null) return;

		if(!gather.isAdmin(message.getAuthor()))
		{
			DiscordBot.sendMessage(gather.getCommandChannel(), "Only **admins** can do that" + " "+message.getAuthor().getDisplayName(message.getGuild())+"!");
			return;
		
		}
		
		gather.disconnectKAGServers();
		gather.connectKAGServers();
		return;
	}
}