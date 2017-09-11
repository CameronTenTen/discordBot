import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import sx.blah.discord.handle.obj.IMessage;

public class CommandGiveWin implements CommandExecutor
{
	@Command(aliases = {"!givewin"}, description = "Admin only - give win in the current game to a particular team")
	public void onCommand(IMessage message, String[] args)
	{
		if(message.getGuild() == null) return;
		GatherObject gather = DiscordBot.getGatherObjectForGuild(message.getGuild());
		if(message.getChannel() != gather.getCommandChannel()) return;

		if(!gather.isAdmin(message.getAuthor()))
		{
			gather.getCommandChannel().sendMessage("Only **admins** can do that "+message.getAuthor().getNicknameForGuild(message.getGuild())+"!");
			return;
		
		}
		int team = -2;
		try
		{
			team = Integer.parseInt(args[0]);
		}
		catch (NumberFormatException e)
		{
			e.printStackTrace();
		}
		
		
		if(!gather.endGame(-1, team))
		{
			gather.getCommandChannel().sendMessage("An error occured setting win, did you type the command correctly "+message.getAuthor().getNicknameForGuild(message.getGuild())+"?");
		}
		
		return;
	}
}