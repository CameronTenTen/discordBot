import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import sx.blah.discord.handle.obj.IMessage;

public class CommandScramble implements CommandExecutor
{
	
	@Command(aliases = {"!scramble"}, description = "Scramble the teams")
	public void onCommand(IMessage message)
	{
		GatherObject gather = DiscordBot.getGatherObjectForChannel(message.getChannel());
		if(gather==null) return;

		PlayerObject player = DiscordBot.players.getObject(message.getAuthor());
		if(player==null)
		{
			DiscordBot.sendMessage(gather.getCommandChannel(), "You must be **in the game** to scramble "+message.getAuthor().getDisplayName(message.getGuild())+"!");
			return;
		}
		gather.addScrambleVote(player);
	}
}