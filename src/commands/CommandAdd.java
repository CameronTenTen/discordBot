package commands;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import core.DiscordBot;
import core.GatherObject;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.Channel;
import discord4j.core.object.presence.Presence;
import discord4j.core.object.presence.Status;

/**
 * Chat command for players to add to the queue. Must be used in command channel. 
 * Prints a response message to the command channel depending on the result of the command.
 * Starts a game if queue is filled by the add request.
 * <p>
 * Does not allow players to add while offline(invisible mode). This is done to prevent people adding while invisible, then going offline (which the bot cant detect). 
 * The bot needs to remove players when they go offline to prevent games starting after people have left. 
 * <p>
 * Adding to queue and starting game are contained in a synchronized statement to prevent players removing while a game is being started.
 * 
 * @author cameron
 *
 */
public class CommandAdd extends Command<Message, Member, Channel>
{
	static final Logger LOGGER = LoggerFactory.getLogger(CommandAdd.class);

	public CommandAdd(Commands<Message, Member, Channel> commands)
	{
		super(commands, Arrays.asList("add","join"), "Add yourself to the queue");
	}

	@Override
	public boolean isChannelValid(Channel channel) {
		GatherObject gather = DiscordBot.getGatherObjectForChannel(channel);
		if(gather==null) return false;
		else return true;
	}

	@Override
	public String onCommand(String[] splitMessage, String messageString, Message messageObject, Member member, Channel channel)
	{
		GatherObject gather = DiscordBot.getGatherObjectForChannel(channel);
		if(gather==null) return null;

		Presence presence = member.getPresence().block();
		if (presence != null && presence.getStatus().equals(Status.INVISIBLE))
		{
			return "You cannot add while you are offline "+member.getDisplayName()+"!";
		}

		synchronized(gather)
		{
			int addReturnVal = gather.addToQueue(member);

			switch(addReturnVal)
			{
			case -1:
				return "You must link before you can add to the queue "+member.getDisplayName()+" type **!link KAGUsernameHere** to get started or **!linkhelp** for more information";
			case 1:
				LOGGER.info("Adding player to queue: "+member.getDisplayName());
				return gather.fullUserString(member)+" **added** to the queue! ("+gather.numPlayersInQueue()+"/"+gather.getMaxQueueSize()+")";
			case 2:
				this.reply(messageObject, gather.fullUserString(member)+" **added** to the queue! ("+gather.numPlayersInQueue()+"/"+gather.getMaxQueueSize()+")");
				LOGGER.info("Adding player to queue: "+member.getDisplayName());
				gather.startGame();
				return null;
			case 3:
				return"You cannot add to the queue when you are **already in a game** "+member.getDisplayName()+"!";
			case 0:
				return "You are already in the queue "+member.getDisplayName()+"!";
			case 4:
				return "You were not added because the queue is already full, try again later "+member.getDisplayName()+"!";
			}
			return "An unexpected error occured adding "+member.getDisplayName()+" to the queue";
		}
	}
}
