import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import sx.blah.discord.Discord4J;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.StatusType;

public class CommandAdd implements CommandExecutor
{
	@Command(aliases = {"!add"}, description = "Add yourself to the queue")
	public void onCommand(IMessage message)
	{
		if(message.getGuild() == null) return;
		GatherObject gather = DiscordBot.getGatherObjectForGuild(message.getGuild());
		if(message.getChannel() != gather.getCommandChannel()) return;
		
		if (message.getAuthor().getPresence().getStatus() == StatusType.OFFLINE)
		{
			DiscordBot.bot.sendMessage(gather.getCommandChannel(), "You cannot add while you are offline "+message.getAuthor().getDisplayName(message.getGuild())+"!");
			return;
		}
		
		int addReturnVal = gather.addToQueue(message.getAuthor());
		
		switch(addReturnVal)
		{
		case -1:
			DiscordBot.bot.sendMessage(gather.getCommandChannel(), "You must link before you can add to the queue "+message.getAuthor().getDisplayName(message.getGuild())+" type **!link** to get started or **!linkhelp** for more information");
			return;
		case 1:
			DiscordBot.bot.sendMessage(gather.getCommandChannel(), gather.fullUserString(message.getAuthor())+" **added** to the queue! ("+gather.numPlayersInQueue()+"/"+gather.getMaxQueueSize()+")");
			Discord4J.LOGGER.info("Adding player to queue: "+message.getAuthor().getDisplayName(message.getGuild()));
			return;
		case 2:
			DiscordBot.bot.sendMessage(gather.getCommandChannel(), gather.fullUserString(message.getAuthor())+" **added** to the queue! ("+gather.numPlayersInQueue()+"/"+gather.getMaxQueueSize()+")");
			Discord4J.LOGGER.info("Adding player to queue: "+message.getAuthor().getDisplayName(message.getGuild()));
			gather.startGame();
			return;
		case 3:
			DiscordBot.bot.sendMessage(gather.getCommandChannel(),"You cannot add to the queue when you are **already in a game** "+message.getAuthor().getDisplayName(message.getGuild())+"!");
			return;
		case 0:
			DiscordBot.bot.sendMessage(gather.getCommandChannel(), "You are already in the queue "+message.getAuthor().getDisplayName(message.getGuild())+"!");
			return;
		case 4:
			DiscordBot.bot.sendMessage(gather.getCommandChannel(), "You were not added, try again later "+message.getAuthor().getDisplayName(message.getGuild())+"!");
			return;
		}
		DiscordBot.bot.sendMessage(gather.getCommandChannel(), "An unexpected error occured adding "+message.getAuthor().getDisplayName(message.getGuild())+" to the queue");
		return;
	}
}