import java.util.List;

import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

public class CommandForceRem implements CommandExecutor
{
	@Command(aliases = {"!forcerem"}, description = "Admin only - remove a user from the queue")
	public void onCommand(IMessage message)
	{
		if(message.getGuild() == null) return;
		GatherObject gather = DiscordBot.getGatherObjectForGuild(message.getGuild());
		if(message.getChannel() != gather.getCommandChannel()) return;
		
		if(!gather.isAdmin(message.getAuthor()))
		{
			DiscordBot.bot.sendMessage(gather.getCommandChannel(), "Only admins can do that "+message.getAuthor().getNicknameForGuild(message.getGuild())+"!");
			return;
		
		}
		
		List<IUser> mentions = message.getMentions();
		for(IUser user : mentions)
		{
			if(1==gather.remFromQueue(user))
			{
				DiscordBot.bot.sendMessage(gather.getCommandChannel(), gather.fullUserString(user)+" was **removed** from the queue (admin) ("+gather.numPlayersInQueue()+"/"+gather.getMaxQueueSize()+")");
			}
		}
		
		return;
	}
}