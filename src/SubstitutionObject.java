
public class SubstitutionObject {
	
	SubstitutionObject(PlayerObject beingReplaced, PlayerObject player, int t)
	{
		playerBeingReplaced = beingReplaced;
		playerSubbingIn = player;
		team = t;
	}
	PlayerObject playerBeingReplaced;
	PlayerObject playerSubbingIn;
	int team;
}
