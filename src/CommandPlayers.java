import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import sx.blah.discord.handle.obj.IMessage;

public class CommandPlayers implements CommandExecutor
{
	@Command(aliases = {"!players"}, description = "Check players currently playing")
	public void onCommand(IMessage message)
	{
		if(message.getGuild() == null) return;
		GatherObject gather = DiscordBot.getGatherObjectForGuild(message.getGuild());
		if(message.getChannel() != gather.getCommandChannel()) return;
		
		String currentPlayers = gather.playersString();
		if(!currentPlayers.isEmpty())
		{
			DiscordBot.bot.sendMessage(gather.getCommandChannel(), currentPlayers);
			return;
		}
		else
		{
			DiscordBot.bot.sendMessage(gather.getCommandChannel(), "No players currently playing");
			return;
		}
	}
}