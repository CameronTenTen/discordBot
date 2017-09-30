import java.util.List;

import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import sx.blah.discord.Discord4J;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

public class CommandRsub implements CommandExecutor
{
	@Command(aliases = {"!rsub"}, description = "request a sub")
	public void onCommand(IMessage message, String[] args)
	{
		GatherObject gather = DiscordBot.getGatherObjectForChannel(message.getChannel());
		if(gather==null) return;

		PlayerObject player = DiscordBot.players.getObject(message.getAuthor());
		if(player==null)
		{
			DiscordBot.sendMessage(gather.getCommandChannel(), "You must be linked to request a sub" + message.getAuthor().getDisplayName(message.getGuild()) + "! Use !link to get started or !linkhelp for more information");
			return;
		}

		List<IUser> mentions = message.getMentions();
		if(args.length == 0 || mentions.contains(message.getAuthor()))
		{
			//player wants to sub out themselves
			int returnVal = gather.substitutions.addSubRequest(message.getAuthor(), gather.getPlayersGame(message.getAuthor()));
			switch(returnVal)
			{
			case -1:
				DiscordBot.sendMessage(gather.getCommandChannel(), "You cannot request a sub for yourself when you are **not in a game** " + message.getAuthor().getDisplayName(message.getGuild()) + "!");
				return;
			case 0:
				DiscordBot.sendMessage(gather.getCommandChannel(), "A sub has **already** been requested for you " + message.getAuthor().getDisplayName(message.getGuild()) + "!");
				return;
			case 1:
				Discord4J.LOGGER.info("sub requested for: "+message.getAuthor().getDisplayName(message.getGuild()));
				DiscordBot.sendMessage(gather.getCommandChannel(), "**Sub request** added for player " + gather.fullUserString(message.getAuthor()) + " use **!sub** to sub into their place!");
				return;
			}
		}
		else
		{
			if(mentions.size()==0)
			{
				DiscordBot.sendMessage(gather.getCommandChannel(), "Incorrect command usage " + message.getAuthor().getDisplayName(message.getGuild()) +"! usage is !rsub with no arguments if you want to request a sub for yourself or !rsub @user if you want to request a sub for someone else");
				return;
			}
			//only use the first mention
			int returnVal = gather.substitutions.addSubVote(mentions.get(0), message.getAuthor());
			switch(returnVal)
			{
			case -1:
			case -2:
			case -3:
				DiscordBot.sendMessage(gather.getCommandChannel(), "You and the player you are voting for must be in the **same game** " + message.getAuthor().getDisplayName(message.getGuild()) + "!");
				return;
			case -4:
				DiscordBot.sendMessage(gather.getCommandChannel(), mentions.get(0).getNicknameForGuild(message.getGuild()) + " is **already** being subbed " + message.getAuthor().getDisplayName(message.getGuild()) + "!");
				return;
			case -5:
				DiscordBot.sendMessage(gather.getCommandChannel(), "You have **already voted** to sub this player " + message.getAuthor().getDisplayName(message.getGuild()) + "!");
				return;
			case 0:
				Discord4J.LOGGER.info("sub requested for: "+message.getAuthor().getDisplayName(message.getGuild()));
				DiscordBot.sendMessage(gather.getCommandChannel(), "A sub has been requested for player " + gather.fullUserString(message.getAuthor()) + " use **!sub** to sub into their place!");
				return;
			}
			//gets here if returnVal is greater than 0 which means the sub vote was added and the number is the vote count
			DiscordBot.sendMessage(gather.getCommandChannel(), "Vote to sub " + gather.fullUserString(mentions.get(0)) + " has been **counted** for " + message.getAuthor().getDisplayName(message.getGuild()) + " (" + returnVal +"/"+ gather.substitutions.getSubVotesRequired() +")");
			return;
		}
	}
}