import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import sx.blah.discord.handle.obj.IMessage;

public class CommandRem implements CommandExecutor
{
	@Command(aliases = {"!rem","!remove"}, description = "Remove yourself from the queue")
	public String onCommand(IMessage message)
	{
		int remReturnVal = DiscordBot.gatherInfo.remFromQueue(new PlayerObject(message.getAuthor(), false));
		DiscordBot.setPlayingText(DiscordBot.gatherInfo.numPlayersInQueue()+"/"+DiscordBot.gatherInfo.maxQueueSize()+" in queue");
		//DiscordBot.setChannel("("+DiscordBot.gatherInfo.numPlayersInQueue()+"/"+DiscordBot.gatherInfo.maxQueueSize()+")");
		switch(remReturnVal)
		{
		case 1:
			return message.getAuthor().getDisplayName(message.getGuild())+" ("+message.getAuthor().getName()+"#"+message.getAuthor().getDiscriminator()+")"+" **left** the queue! ("+DiscordBot.gatherInfo.numPlayersInQueue()+"/"+DiscordBot.gatherInfo.maxQueueSize()+")";
		case 0:
			return "You are already not in the queue: "+message.getAuthor().getDisplayName(message.getGuild())+"!";
		}
		return "An unexpected error occured attempting to remove "+message.getAuthor().getDisplayName(message.getGuild())+" from the queue";
	}
}