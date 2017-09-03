import java.util.Vector;

public class GatherQueueObject extends Vector<PlayerObject>
{
	public static int maxQueueSize = 10;
	
	public boolean add(PlayerObject player)
	{
		int index = this.indexOf(player);
		if(index==-1)
		{
			super.add(player);
			return true;
		}
		else
		{
			return false;
		}
	}
	
	public boolean rem(PlayerObject player)
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
		return this.numPlayersInQueue()>=maxQueueSize;
	}
}