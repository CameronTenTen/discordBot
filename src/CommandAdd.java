import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import sx.blah.discord.Discord4J;
import sx.blah.discord.handle.obj.IMessage;

public class CommandAdd implements CommandExecutor
{
	@Command(aliases = {"!add"}, description = "Add yourself to the queue")
	public void onCommand(IMessage message)
	{
		if(message.getGuild() == null) return;
		GatherObject gather = DiscordBot.getGatherObjectForGuild(message.getGuild());
		if(message.getChannel() != gather.getCommandChannel()) return;
		
		int addReturnVal = gather.addToQueue(new PlayerObject(message.getAuthor(), false));
		DiscordBot.setPlayingText(gather.numPlayersInQueue()+"/"+gather.getMaxQueueSize()+" in queue");
		DiscordBot.setChannelCaption(gather.getGuild() , gather.numPlayersInQueue()+"-in-q");
		
		switch(addReturnVal)
		{
		case 1:
			Discord4J.LOGGER.info("Adding player to queue: "+message.getAuthor().getDisplayName(message.getGuild()));
			DiscordBot.bot.sendMessage(gather.getCommandChannel(), gather.fullUserString(message.getAuthor())+" **added** to the queue! ("+gather.numPlayersInQueue()+"/"+gather.getMaxQueueSize()+")");
			return;
		case 2:
			Discord4J.LOGGER.info("Adding player to queue: "+message.getAuthor().getDisplayName(message.getGuild()));
			DiscordBot.bot.sendMessage(gather.getCommandChannel(), gather.fullUserString(message.getAuthor())+" **added** to the queue! ("+gather.numPlayersInQueue()+"/"+gather.getMaxQueueSize()+")");
			gather.startGame();
			return;
		case 3:
			DiscordBot.bot.sendMessage(gather.getCommandChannel(),"You cannot add to the queue when you are **already in a game** "+message.getAuthor().getDisplayName(message.getGuild())+"!");
			return;
		case 0:
			DiscordBot.bot.sendMessage(gather.getCommandChannel(), "You are already in the queue "+message.getAuthor().getDisplayName(message.getGuild())+"!");
			return;
		}
		gather.getCommandChannel().sendMessage("An unexpected error occured adding "+message.getAuthor().getDisplayName(message.getGuild())+" to the queue");
		return;
	}
}