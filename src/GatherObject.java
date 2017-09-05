
public class GatherObject
{
	private GatherQueueObject queue;
	
	GatherObject()
	{
		queue = new GatherQueueObject();
	}
	
	/**
	 * Adds a player to the gather queue
	 *
	 * @return 0 if player already in queue or something else went wrong
	 * @return 1 if player added to the queue
	 * @return 2 if player added to queue and queue is now full
	 */
	public int addToQueue(PlayerObject player)
	{
		if(queue.add(player))
		{
			if(isQueueFull())
			{
				return 2;
			}
			return 1;
		}
		return 0;
		
	}
	
	public int remFromQueue(PlayerObject player)
	{
		if(queue.remove(player))
		{
			return 1;
		}
		else
		{
			return 0;
		}
	}
	
	public void clearQueue()
	{
		queue.clear();
	}
	
	public int numPlayersInQueue()
	{
		return queue.numPlayersInQueue();
	}
	
	public boolean isQueueFull()
	{
		return queue.isFull();
	}
	
	public int maxQueueSize()
	{
		return GatherQueueObject.maxQueueSize;
	}
	
	public String getMentionList()
	{
		String returnString="";
		for(PlayerObject player : queue)
		{
			returnString += " ";
			returnString += player.getDiscordUserInfo().mention();
		}
		return returnString;
	}
	
	public String queueString()
	{
		return queue.toString();
	}
}