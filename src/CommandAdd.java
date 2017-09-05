import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import sx.blah.discord.handle.obj.IMessage;

public class CommandAdd implements CommandExecutor
{
	@Command(aliases = {"!add"}, description = "Add yourself to the queue")
	public String onCommand(IMessage message)
	{
		GatherObject gather = DiscordBot.getGatherObjectForGuild(message.getGuild());
		int addReturnVal = gather.addToQueue(new PlayerObject(message.getAuthor(), false));
		DiscordBot.setPlayingText(gather.numPlayersInQueue()+"/"+gather.maxQueueSize()+" in queue");
		DiscordBot.setChannelCaption(gather.getGuild() , gather.numPlayersInQueue()+"-in-q");
		
		switch(addReturnVal)
		{
		case 1:
			return message.getAuthor().getDisplayName(message.getGuild())+" ("+message.getAuthor().getName()+"#"+message.getAuthor().getDiscriminator()+")"+" **added** to the queue! ("+gather.numPlayersInQueue()+"/"+gather.maxQueueSize()+")";
		case 2:
			message.getChannel().sendMessage(message.getAuthor().getDisplayName(message.getGuild())+" ("+message.getAuthor().getName()+"#"+message.getAuthor().getDiscriminator()+")"+" **added** to the queue! ("+gather.numPlayersInQueue()+"/"+gather.maxQueueSize()+")");
			gather.startGame();
			return "";
		case 0:
			return "You are already in the queue "+message.getAuthor().getDisplayName(message.getGuild())+"!";
		}
		return "An unexpected error occured adding "+message.getAuthor().getDisplayName(message.getGuild())+" to the queue";
	}
}