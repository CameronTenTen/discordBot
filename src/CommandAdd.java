import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import sx.blah.discord.handle.obj.IMessage;

public class CommandAdd implements CommandExecutor
{
	@Command(aliases = {"!add"}, description = "Add yourself to the queue")
	public String onCommand(IMessage message)
	{
		int addReturnVal = DiscordBot.gatherInfo.addToQueue(new PlayerObject(message.getAuthor(), false));
		//DiscordBot.setPlayingText(DiscordBot.gatherInfo.numPlayersInQueue()+"/"+DiscordBot.gatherInfo.maxQueueSize()+" in queue");
		//DiscordBot.setChannel("("+DiscordBot.gatherInfo.numPlayersInQueue()+"/"+DiscordBot.gatherInfo.maxQueueSize()+")");
		switch(addReturnVal)
		{
		case 1:
			return message.getAuthor().getDisplayName(message.getGuild())+" ("+message.getAuthor().getName()+"#"+message.getAuthor().getDiscriminator()+")"+" **added** to the queue! ("+DiscordBot.gatherInfo.numPlayersInQueue()+"/"+DiscordBot.gatherInfo.maxQueueSize()+")";
		case 2:
			message.getChannel().sendMessage(message.getAuthor().getDisplayName(message.getGuild())+" ("+message.getAuthor().getName()+"#"+message.getAuthor().getDiscriminator()+")"+" **added** to the queue! ("+DiscordBot.gatherInfo.numPlayersInQueue()+"/"+DiscordBot.gatherInfo.maxQueueSize()+")");
			return "Gather game starting with players: "+DiscordBot.gatherInfo.getMentionList();
		case 0:
			return "You are already in the queue "+message.getAuthor().getDisplayName(message.getGuild())+"!";
		}
		return "An unexpected error occured adding "+message.getAuthor().getDisplayName(message.getGuild())+" to the queue";
	}
}