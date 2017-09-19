import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import sx.blah.discord.handle.obj.IMessage;

public class CommandSubs implements CommandExecutor
{
	@Command(aliases = {"!subs"}, description = "Check current sub requests")
	public void onCommand(IMessage message)
	{
		if(message.getGuild() == null) return;
		GatherObject gather = DiscordBot.getGatherObjectForGuild(message.getGuild());
		if(message.getChannel() != gather.getCommandChannel()) return;
		
		String currentSubs = gather.substitutions.toString();
		if(!currentSubs.isEmpty())
		{
			DiscordBot.bot.sendMessage(gather.getCommandChannel(), "There are current sub requests for: "+currentSubs);
			return;
		}
		else
		{
			DiscordBot.bot.sendMessage(gather.getCommandChannel(), "No subs currently requested");
			return;
		}
	}
}