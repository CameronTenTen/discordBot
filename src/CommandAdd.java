import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import sx.blah.discord.Discord4J;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.StatusType;

/**
 * Chat command for players to add to the queue. Must be used in command channel. 
 * Prints a response message to the command channel depending on the result of the command.
 * Starts a game if queue is filled by the add request.
 * <p>
 * Does not allow players to add while offline(invisible mode). This is done to prevent people adding while invisible, then going offline (which the bot cant detect). 
 * The bot needs to remove players when they go offline to prevent games starting after people have left. 
 * <p>
 * Adding to queue and starting game are contined in a synchronized statement to prevent players removing while a game is being started.
 * 
 * @author cameron
 *
 */
public class CommandAdd implements CommandExecutor
{
	/**The function that is called when the command is used
	 * @param message
	 * @see https://github.com/BtoBastian/sdcf4j
	 * @see #CommandAdd
	 */
	@Command(aliases = {"!add","!join"}, description = "Add yourself to the queue")
	public void onCommand(IMessage message)
	{
		GatherObject gather = DiscordBot.getGatherObjectForChannel(message.getChannel());
		if(gather==null) return;
		
		if (message.getAuthor().getPresence().getStatus() == StatusType.OFFLINE)
		{
			DiscordBot.sendMessage(gather.getCommandChannel(), "You cannot add while you are offline "+message.getAuthor().getDisplayName(message.getGuild())+"!");
			return;
		}
		
		synchronized(gather)
		{
			int addReturnVal = gather.addToQueue(message.getAuthor());
			
			switch(addReturnVal)
			{
			case -1:
				DiscordBot.sendMessage(gather.getCommandChannel(), "You must link before you can add to the queue "+message.getAuthor().getDisplayName(message.getGuild())+" type **!link KAGUsernameHere** to get started or **!linkhelp** for more information");
				return;
			case 1:
				DiscordBot.sendMessage(gather.getCommandChannel(), gather.fullUserString(message.getAuthor())+" **added** to the queue! ("+gather.numPlayersInQueue()+"/"+gather.getMaxQueueSize()+")");
				gather.setNotInterested(message.getAuthor());
				Discord4J.LOGGER.info("Adding player to queue: "+message.getAuthor().getDisplayName(message.getGuild()));
				return;
			case 2:
				DiscordBot.sendMessage(gather.getCommandChannel(), gather.fullUserString(message.getAuthor())+" **added** to the queue! ("+gather.numPlayersInQueue()+"/"+gather.getMaxQueueSize()+")");
				gather.setNotInterested(message.getAuthor());
				Discord4J.LOGGER.info("Adding player to queue: "+message.getAuthor().getDisplayName(message.getGuild()));
				gather.startGame();
				return;
			case 3:
				DiscordBot.sendMessage(gather.getCommandChannel(),"You cannot add to the queue when you are **already in a game** "+message.getAuthor().getDisplayName(message.getGuild())+"!");
				return;
			case 0:
				DiscordBot.sendMessage(gather.getCommandChannel(), "You are already in the queue "+message.getAuthor().getDisplayName(message.getGuild())+"!");
				return;
			case 4:
				DiscordBot.sendMessage(gather.getCommandChannel(), "You were not added because the queue is already full, try again later "+message.getAuthor().getDisplayName(message.getGuild())+"!");
				return;
			}
			DiscordBot.sendMessage(gather.getCommandChannel(), "An unexpected error occured adding "+message.getAuthor().getDisplayName(message.getGuild())+" to the queue");
		}
		return;
	}
}