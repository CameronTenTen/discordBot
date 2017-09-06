import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.IVoiceChannel;

public class goToTeamVoiceChannel implements GenericReactCallbackObject{
	public void execute(IUser user, IGuild guild)
	{
		GatherObject gather = DiscordBot.getGatherObjectForGuild(guild);
		IVoiceChannel channel = gather.getTeamVoiceChannel(user);
		if(channel !=null) user.moveToVoiceChannel(channel);
	}
}
