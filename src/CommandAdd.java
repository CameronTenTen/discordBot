import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
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
		DiscordBot.setPlayingText(gather.numPlayersInQueue()+"/"+gather.maxQueueSize()+" in queue");
		DiscordBot.setChannelCaption(gather.getGuild() , gather.numPlayersInQueue()+"-in-q");
		
		switch(addReturnVal)
		{
		case 1:
			gather.getCommandChannel().sendMessage(gather.fullUserString(message.getAuthor())+" **added** to the queue! ("+gather.numPlayersInQueue()+"/"+gather.maxQueueSize()+")");
			return;
		case 2:
			//gather.getCommandChannel().sendMessage(gather.fullUserString(message.getAuthor())+" **added** to the queue! ("+gather.numPlayersInQueue()+"/"+gather.maxQueueSize()+")");
			gather.startGame();
			return;
		case 0:
			gather.getCommandChannel().sendMessage("You are already in the queue "+message.getAuthor().getDisplayName(message.getGuild())+"!");
			return;
		}
		gather.getCommandChannel().sendMessage("An unexpected error occured adding "+message.getAuthor().getDisplayName(message.getGuild())+" to the queue");
		return;
	}
}