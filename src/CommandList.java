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
			DiscordBot.bot.sendMessage(gather.getCommandChannel(), "Current **queue** ("+gather.numPlayersInQueue()+"/"+gather.getMaxQueueSize()+"): "+currentQueue);
			return;
		}
		else
		{
			DiscordBot.bot.sendMessage(gather.getCommandChannel(), "Queue is **empty**");
			return;
		}
	}
}