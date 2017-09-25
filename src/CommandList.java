import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import sx.blah.discord.handle.obj.IMessage;

public class CommandList implements CommandExecutor
{
	@Command(aliases = {"!list"}, description = "Check the current player list")
	public void onCommand(IMessage message)
	{
		GatherObject gather = DiscordBot.getGatherObjectForChannel(message.getChannel());
		if(gather==null) return;
		
		String currentQueue = gather.queueString();
		if(!currentQueue.isEmpty())
		{
			DiscordBot.sendMessage(gather.getCommandChannel(), "Current **queue** ("+gather.numPlayersInQueue()+"/"+gather.getMaxQueueSize()+"): "+currentQueue);
			return;
		}
		else
		{
			DiscordBot.sendMessage(gather.getCommandChannel(), "Queue is **empty**");
			return;
		}
	}
}