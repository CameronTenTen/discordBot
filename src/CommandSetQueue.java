import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.MessageTokenizer;

public class CommandSetQueue implements CommandExecutor
{
	@Command(aliases = {"!setqueue"}, description = "Admin only - change the queue size")
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
		
		int newSize;
		MessageTokenizer tokens = message.tokenize();
		tokens.nextWord();
		//use the second argument as the queue size
		if(!tokens.hasNextWord())
		{
			DiscordBot.bot.sendMessage(gather.getCommandChannel(), "Invalid command format, queue size as a number must be provided");
			return;
		}
		try
		{
			newSize = Integer.parseInt(tokens.nextWord().toString());
		}
		catch (NumberFormatException e)
		{
			DiscordBot.bot.sendMessage(gather.getCommandChannel(), "Invalid command format, queue size as a number must be provided");
			e.printStackTrace();
			return;
		}
		if(newSize<=gather.numPlayersInQueue())
		{
			DiscordBot.bot.sendMessage(gather.getCommandChannel(), "Cannot set queue size less than or equal to current queue size: "+gather.numPlayersInQueue());
			return;
		}
		gather.setMaxQueueSize(newSize);
		DiscordBot.bot.sendMessage(gather.getCommandChannel(), "Queue size has been set to "+gather.getMaxQueueSize());
		
		DiscordBot.setPlayingText(gather.numPlayersInQueue()+"/"+gather.getMaxQueueSize()+" in queue");
		DiscordBot.setChannelCaption(gather.getGuild() , gather.numPlayersInQueue()+"-in-q");
		return;
	}
}