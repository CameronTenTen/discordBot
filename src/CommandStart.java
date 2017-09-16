import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import sx.blah.discord.handle.obj.IMessage;

public class CommandStart implements CommandExecutor
{
	@Command(aliases = {"!start"}, description = "Admin only - move users from general chat to team rooms")
	public void onCommand(IMessage message)
	{
		if(message.getGuild() == null) return;
		GatherObject gather = DiscordBot.getGatherObjectForGuild(message.getGuild());
		if(message.getChannel() != gather.getCommandChannel()) return;

		if(!gather.isAdmin(message.getAuthor()))
		{
			DiscordBot.bot.sendMessage(gather.getCommandChannel(), "Only **admins** can do that "+message.getAuthor().getNicknameForGuild(message.getGuild())+"!");
			return;
		
		}
		gather.movePlayersIntoTeamRooms();
		return;
	}
}