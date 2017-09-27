import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import sx.blah.discord.handle.obj.IMessage;

public class CommandPlayers implements CommandExecutor
{
	@Command(aliases = {"!players"}, description = "Check players currently playing")
	public void onCommand(IMessage message)
	{
		GatherObject gather = DiscordBot.getGatherObjectForChannel(message.getChannel());
		if(gather==null) return;
		
		String currentPlayers = gather.playersString();
		if(!currentPlayers.isEmpty())
		{
			DiscordBot.sendMessage(gather.getCommandChannel(), "Current games: \n" + currentPlayers);
			return;
		}
		else
		{
			DiscordBot.sendMessage(gather.getCommandChannel(), "No players currently playing");
			return;
		}
	}
}