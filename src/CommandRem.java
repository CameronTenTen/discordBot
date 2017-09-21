import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import sx.blah.discord.Discord4J;
import sx.blah.discord.handle.obj.IMessage;

public class CommandRem implements CommandExecutor
{
	@Command(aliases = {"!rem","!remove"}, description = "Remove yourself from the queue")
	public void onCommand(IMessage message)
	{
		if(message.getGuild() == null) return;
		GatherObject gather = DiscordBot.getGatherObjectForGuild(message.getGuild());
		if(message.getChannel() != gather.getCommandChannel()) return;
		
		int remReturnVal = gather.remFromQueue(message.getAuthor());
		switch(remReturnVal)
		{
		case 1:
			Discord4J.LOGGER.info("Removing player from queue: "+message.getAuthor().getDisplayName(message.getGuild()));
			DiscordBot.bot.sendMessage(gather.getCommandChannel(), gather.fullUserString(message.getAuthor())+" **left** the queue! ("+gather.numPlayersInQueue()+"/"+gather.getMaxQueueSize()+")");
			return;
		case 0:
			DiscordBot.bot.sendMessage(gather.getCommandChannel(), "You are already not in the queue "+message.getAuthor().getDisplayName(message.getGuild())+"!");
			return;
		}
		DiscordBot.bot.sendMessage(gather.getCommandChannel(), "An unexpected error occured attempting to remove "+message.getAuthor().getDisplayName(message.getGuild())+" from the queue");
		return;
	}
}