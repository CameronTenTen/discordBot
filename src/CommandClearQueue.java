import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import sx.blah.discord.handle.obj.IMessage;

public class CommandClearQueue implements CommandExecutor
{
	@Command(aliases = {"!clearqueue"}, description = "Admin only - clear the queue")
	public void onCommand(IMessage message)
	{
		if(message.getGuild() == null) return;
		GatherObject gather = DiscordBot.getGatherObjectForGuild(message.getGuild());
		if(message.getChannel() != gather.getCommandChannel()) return;
		
		if(!gather.isAdmin(message.getAuthor()))
		{
			DiscordBot.bot.sendMessage(gather.getCommandChannel(), "Only **admins** can do that "+message.getAuthor().getNicknameForGuild(message.getGuild())+"!");
			return;
		
		}
		
		gather.clearQueue();
		DiscordBot.setPlayingText(gather.numPlayersInQueue()+"/"+gather.getMaxQueueSize()+" in queue");
		DiscordBot.setChannelCaption(gather.getGuild() , gather.numPlayersInQueue()+"-in-q");
		DiscordBot.bot.sendMessage(gather.getCommandChannel(), "Queue is now **empty**");
		return;
	}
}