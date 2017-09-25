import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import sx.blah.discord.handle.obj.IMessage;

public class CommandSubs implements CommandExecutor
{
	@Command(aliases = {"!subs"}, description = "Check current sub requests")
	public void onCommand(IMessage message)
	{
		GatherObject gather = DiscordBot.getGatherObjectForChannel(message.getChannel());
		if(gather==null) return;
		
		String currentSubs = gather.substitutions.toString();
		if(!currentSubs.isEmpty())
		{
			DiscordBot.sendMessage(gather.getCommandChannel(), "There are current sub requests for: "+currentSubs);
			return;
		}
		else
		{
			DiscordBot.sendMessage(gather.getCommandChannel(), "No subs currently requested");
			return;
		}
	}
}