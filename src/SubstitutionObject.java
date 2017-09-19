
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
