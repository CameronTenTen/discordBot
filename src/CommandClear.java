import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import sx.blah.discord.handle.obj.IMessage;

public class CommandClear implements CommandExecutor
{
	@Command(aliases = {"!clearqueue"}, description = "Admin only - clear the queue")
	public void onCommand(IMessage message)
	{
		GatherObject gather = DiscordBot.getGatherObjectForGuild(message.getGuild());
		
		if(!gather.isAdmin(message.getAuthor()))
		{
			gather.getCommandChannel().sendMessage("Only admins can do that "+message.getAuthor().getNicknameForGuild(message.getGuild())+"!");
			return;
		
		}
		
		gather.clearQueue();
		DiscordBot.setPlayingText(gather.numPlayersInQueue()+"/"+gather.maxQueueSize()+" in queue");
		DiscordBot.setChannelCaption(gather.getGuild() , gather.numPlayersInQueue()+"-in-q");
		gather.getCommandChannel().sendMessage("Queue is **empty**");
		return;
	}
}