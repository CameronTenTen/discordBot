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
		GatherObject gather = DiscordBot.getGatherObjectForGuild(message.getGuild());
		
		if(!gather.isAdmin(message.getAuthor()))
		{
			gather.getCommandChannel().sendMessage("Only admins can do that "+message.getAuthor().getNicknameForGuild(message.getGuild())+"!");
			return;
		
		}
		
		List<IUser> mentions = message.getMentions();
		for(IUser user : mentions)
		{
			if(1==gather.remFromQueue(new PlayerObject(user, false)))
			{
				gather.getCommandChannel().sendMessage(user.getDisplayName(gather.getGuild())+" ("+user.getName()+"#"+user.getDiscriminator()+") was **removed** from the queue (admin) ("+gather.numPlayersInQueue()+"/"+gather.maxQueueSize()+")");
			}
		}
		
		DiscordBot.setPlayingText(gather.numPlayersInQueue()+"/"+gather.maxQueueSize()+" in queue");
		DiscordBot.setChannelCaption(gather.getGuild() , gather.numPlayersInQueue()+"-in-q");
		return;
	}
}