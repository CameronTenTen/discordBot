
/**Object representing a substitution that was made. Contains a PlayerObject for the player replaced and the player subbing in, as well as the GatherGame object. 
 * @author cameron
 * @see CommandSub
 */
public class SubstitutionObject {

	SubstitutionObject(PlayerObject beingReplaced, PlayerObject player, GatherGame game)
	{
		this.playerToBeReplaced = beingReplaced;
		this.playerSubbingIn = player;
		this.game = game;
	}
	PlayerObject playerToBeReplaced;
	PlayerObject playerSubbingIn;
	GatherGame game;
}
