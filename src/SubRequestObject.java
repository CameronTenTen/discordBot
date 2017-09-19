
public class SubRequestObject {
	
	SubRequestObject(PlayerObject beingReplaced, GatherGame game)
	{
		this.playerToBeReplaced = beingReplaced;
		this.game = game;
	}
	PlayerObject playerToBeReplaced;
	GatherGame game;
	
	@Override
	public boolean equals(Object obj)
	{
		if(obj == null)
			return false;
		
		if(this.playerToBeReplaced.equals(((SubRequestObject)obj).playerToBeReplaced))
			return true;
		
		return false;
	}
	
	@Override
	public String toString()
	{
		return playerToBeReplaced.toString();
	}
}
