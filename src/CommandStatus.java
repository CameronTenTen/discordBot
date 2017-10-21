import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import sx.blah.discord.handle.obj.IMessage;

public class CommandStatus implements CommandExecutor
{
	@Command(aliases = {"!status"}, description = "Check status of current games")
	public void onCommand(IMessage message)
	{
		GatherObject gather = DiscordBot.getGatherObjectForChannel(message.getChannel());
		if(gather==null) return;
		
		String currentStatus = gather.statusString();
		if(!currentStatus.isEmpty())
		{
			DiscordBot.sendMessage(gather.getCommandChannel(), "Current games: \n" + currentStatus);
			return;
		}
		else
		{
			DiscordBot.sendMessage(gather.getCommandChannel(), "No games currently running");
			return;
		}
	}
}