import java.util.Vector;

public class GatherQueueObject extends Vector<PlayerObject>
{
	private static int maxQueueSize = 10;
	
	public static int getMaxQueueSize() {
		return maxQueueSize;
	}

	public static void setMaxQueueSize(int maxQueueSize) {
		GatherQueueObject.maxQueueSize = maxQueueSize;
	}

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
	
	
	public int numPlayersInQueue()
	{
		return super.size();
	}
	
	public boolean isFull()
	{
		return this.numPlayersInQueue()>=getMaxQueueSize();
	}
	
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