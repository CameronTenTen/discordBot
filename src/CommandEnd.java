import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import sx.blah.discord.handle.obj.IMessage;

public class CommandEnd implements CommandExecutor
{
	@Command(aliases = {"!end"}, description = "Admin only - move users from team chat to general chat")
	public void onCommand(IMessage message)
	{
		if(message.getGuild() == null) return;
		GatherObject gather = DiscordBot.getGatherObjectForGuild(message.getGuild());
		if(message.getChannel() != gather.getCommandChannel()) return;

		if(!gather.isAdmin(message.getAuthor()))
		{
			DiscordBot.sendMessage(gather.getCommandChannel(), "Only **admins** can do that "+message.getAuthor().getNicknameForGuild(message.getGuild())+"!");
			return;
		
		}
		gather.movePlayersOutOfTeamRooms();
		return;
	}
}