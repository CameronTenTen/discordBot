import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import sx.blah.discord.handle.obj.IMessage;

public class CommandRem implements CommandExecutor
{
	@Command(aliases = {"!rem","!remove"}, description = "Remove yourself from the queue")
	public void onCommand(IMessage message)
	{
		if(message.getGuild() == null) return;
		GatherObject gather = DiscordBot.getGatherObjectForGuild(message.getGuild());
		if(message.getChannel() != gather.getCommandChannel()) return;
		
		int remReturnVal = gather.remFromQueue(new PlayerObject(message.getAuthor(), false));
		DiscordBot.setPlayingText(gather.numPlayersInQueue()+"/"+gather.maxQueueSize()+" in queue");
		DiscordBot.setChannelCaption(gather.getGuild() , gather.numPlayersInQueue()+"-in-q");
		switch(remReturnVal)
		{
		case 1:
			gather.getCommandChannel().sendMessage(gather.fullUserString(message.getAuthor())+" **left** the queue! ("+gather.numPlayersInQueue()+"/"+gather.maxQueueSize()+")");
			return;
		case 0:
			gather.getCommandChannel().sendMessage("You are already not in the queue "+message.getAuthor().getDisplayName(message.getGuild())+"!");
			return;
		}
		gather.getCommandChannel().sendMessage("An unexpected error occured attempting to remove "+message.getAuthor().getDisplayName(message.getGuild())+" from the queue");
		return;
	}
}