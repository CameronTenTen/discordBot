import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import sx.blah.discord.handle.obj.IMessage;

public class CommandRem implements CommandExecutor
{
	@Command(aliases = {"!rem","!remove"}, description = "Remove yourself from the queue")
	public String onCommand(IMessage message)
	{
		GatherObject gather = DiscordBot.getGatherObjectForGuild(message.getGuild());
		int remReturnVal = gather.remFromQueue(new PlayerObject(message.getAuthor(), false));
		DiscordBot.setPlayingText(gather.numPlayersInQueue()+"/"+gather.maxQueueSize()+" in queue");
		DiscordBot.setChannelCaption(gather.getGuild() , gather.numPlayersInQueue()+"-in-q");
		switch(remReturnVal)
		{
		case 1:
			return message.getAuthor().getDisplayName(message.getGuild())+" ("+message.getAuthor().getName()+"#"+message.getAuthor().getDiscriminator()+")"+" **left** the queue! ("+gather.numPlayersInQueue()+"/"+gather.maxQueueSize()+")";
		case 0:
			return "You are already not in the queue "+message.getAuthor().getDisplayName(message.getGuild())+"!";
		}
		return "An unexpected error occured attempting to remove "+message.getAuthor().getDisplayName(message.getGuild())+" from the queue";
	}
}