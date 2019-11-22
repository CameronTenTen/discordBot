package core;

/**Object used for holding token information when it is retreived from the kag2d api. 
 * @author cameron
 * @see #CommandLink
 * @see https://developers.thd.vg/api/players.html
 * @see https://api.kag2d.com/v1/player/username/token/new
 */
public class TokenCheckObject {
	
	public TokenCheckObject()
	{
		playerTokenStatus=false;
	}
	
	public boolean playerTokenStatus;
}
