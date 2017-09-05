import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import sx.blah.discord.handle.obj.IMessage;

public class CommandList implements CommandExecutor
{
	@Command(aliases = {"!list"}, description = "Check the current player list")
	public String onCommand(IMessage message)
	{
		GatherObject gather = DiscordBot.getGatherObjectForGuild(message.getGuild());
		if(gather == null) return "";
		String currentQueue = gather.queueString();
		if(!currentQueue.isEmpty())
		{
			return "Current **queue** ("+gather.numPlayersInQueue()+"/"+gather.maxQueueSize()+"): "+currentQueue;
		}
		else
		{
			return "Queue is **empty**";
		}
	}
}