import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import sx.blah.discord.handle.obj.IMessage;

public class CommandList implements CommandExecutor
{
	@Command(aliases = {"!list"}, description = "Check the current player list")
	public String onCommand(IMessage message)
	{
		return "Current queue: "+DiscordBot.gatherInfo.queueString();
	}
}