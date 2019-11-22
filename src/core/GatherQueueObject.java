package core;
import java.util.ArrayList;
import java.util.List;

/**
 * Holds a list of players that are currently in the gather queue
 * @author cameron
 *
 */
public class GatherQueueObject extends ArrayList<PlayerObject>
{
	private int maxQueueSize = 10;
	
	/**
	 * @return The maximum queue size
	 */
	public int getMaxQueueSize() {
		return maxQueueSize;
	}

	/**
	 * @param The new max queue size
	 */
	public void setMaxQueueSize(int maxQueueSize) {
		this.maxQueueSize = maxQueueSize;
	}
	
	/** 
	 * @param player - The player to add to the queue
	 * @return true if player was added to the queue
	 * @see java.util.Vector#add(java.lang.Object)
	 */
	@Override
	public boolean add(PlayerObject player)
	{
		int index = this.indexOf(player);
		if(index==-1 && this.size()<getMaxQueueSize())
		{
			super.add(player);
			return true;
		}
		else
		{
			return false;
		}
	}
	
	/**
	 * @param player The player to remove from the queue
	 * @return true if the player was removed from the queue
	 * @see java.util.Vector#remove(java.lang.Object)
	 */
	@Override
	public boolean remove(Object player)
	{
		int index = this.indexOf(player);
		if(index!=-1)
		{
			super.remove(player);
			return true;
		}
		else
		{
			return false;
		}
	}
	
	
	/**
	 * @return The number of players currently in the queue
	 */
	public int numPlayersInQueue()
	{
		return super.size();
	}
	
	/**
	 * @return True if the current queue size is greater than or equal to the max queue size
	 */
	public boolean isFull()
	{
		return this.numPlayersInQueue()>=getMaxQueueSize();
	}
	
	/**
	 * @return A copy of the current list as a List of PlayerObjects
	 */
	public List<PlayerObject> asList()
	{
		ArrayList<PlayerObject> list = new ArrayList<PlayerObject>();
		list.addAll(this);
		return list;
	}
	
	/**
	 * @return A comma delimited representation of the players in the queue using PlayerObject.toString()
	 * @see java.util.Vector#toString()
	 * @see PlayerObject#toString()
	 */
	public String toString()
	{
		String returnString="";
		for(PlayerObject player : this)
		{
			returnString+=player.toString();
			returnString+=", ";
		}
		if(returnString.length()<=2)
		{
			return "";
		}
		else
		{
			return returnString.substring(0, returnString.length()-2);
		}
	}
}