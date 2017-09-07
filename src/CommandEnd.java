import java.util.List;

import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.IVoiceChannel;

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
			gather.getCommandChannel().sendMessage("Only **admins** can do that "+message.getAuthor().getNicknameForGuild(message.getGuild())+"!");
			return;
		
		}
		//gather.getCommandChannel().sendMessage("Moving players out of team rooms");
		
		IVoiceChannel general = gather.getGeneralVoiceChannel();
		IVoiceChannel blue = gather.getBlueVoiceChannel();
		IVoiceChannel red = gather.getRedVoiceChannel();
		
		List<IUser> users;
		users = blue.getConnectedUsers();
		for( IUser user : users)
		{
			user.moveToVoiceChannel(general);
		}
		users = red.getConnectedUsers();
		for( IUser user : users)
		{
			user.moveToVoiceChannel(general);
		}
		
		return;
	}
}