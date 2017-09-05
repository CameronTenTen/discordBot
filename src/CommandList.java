import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import sx.blah.discord.handle.obj.IMessage;

public class CommandList implements CommandExecutor
{
	@Command(aliases = {"!list"}, description = "Check the current player list")
	public void onCommand(IMessage message)
	{
		if(message.getGuild() == null) return;
		GatherObject gather = DiscordBot.getGatherObjectForGuild(message.getGuild());
		if(message.getChannel() != gather.getCommandChannel()) return;
		
		String currentQueue = gather.queueString();
		if(!currentQueue.isEmpty())
		{
			gather.getCommandChannel().sendMessage("Current **queue** ("+gather.numPlayersInQueue()+"/"+gather.maxQueueSize()+"): "+currentQueue);
			return;
		}
		else
		{
			gather.getCommandChannel().sendMessage("Queue is **empty**");
			return;
		}
	}
}