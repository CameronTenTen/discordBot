
/**Object for holding the data needed for a sub request in the SubManager. Includes the player to be subbed, and the game they are playing in. 
 * @author cameron
 * @see SubManager
 */
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
		return playerToBeReplaced.toString()+" - Game #"+game.getGameID();
	}
}
