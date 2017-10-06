import java.util.List;

import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

public class CommandTop10 implements CommandExecutor
{
	@Command(aliases = {"!top10"}, description = "Get the top 10 players")
	public void onCommand(IMessage message, String[] args)
	{
		List<StatsObject> list = DiscordBot.database.getTop10();
		if(list == null)
		{
			DiscordBot.sendMessage(message.getChannel(), "An error occured retreiving top 10 from the database, tell someone that can do something about it");
			return;
		}
		String scoreboardString="**Top 10:**\n```  |      KAG name      |  Win %  | Games\n";
		int i=0;
		for(StatsObject stats : list)
		{
			i++;
			scoreboardString+=i;
			if(i<10)scoreboardString+=" ";
			scoreboardString+="|";
			//centre the kagname in the column
			if(stats.kagname.length()%2 != 0)
			{
				stats.kagname = stats.kagname + " ";
			}
			for(int j = stats.kagname.length()/2; j < 10; j++)
			{
				scoreboardString+=" ";
			}
			scoreboardString=scoreboardString+stats.kagname;
			for(int j = stats.kagname.length()/2; j < 10; j++)
			{
				scoreboardString+=" ";
			}
			scoreboardString+="|";
			scoreboardString=scoreboardString+" "+stats.winRateString()+"% ";
			if(stats.winRateString().length()<6)scoreboardString+=" ";
			scoreboardString=scoreboardString+"|  "+stats.gamesPlayed+"\n";
		}
		System.out.println(scoreboardString.length());
		DiscordBot.sendMessage(message.getChannel(), scoreboardString+"```");
		return;
	}
}