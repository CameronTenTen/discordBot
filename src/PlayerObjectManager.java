import java.util.HashSet;
import java.util.Set;

import sx.blah.discord.handle.obj.IUser;

/**class to keep track of player objects so that they can be updated when player data is changed (for example when a user links their accounts). All player objects should be created here. If they are created elsewhere they will become invalid if a player changes their linked accounts. 
 * @author cameron
 *
 */
public class PlayerObjectManager
{
	private Set<PlayerObject> playerObjects;
	
	PlayerObjectManager()
	{
		playerObjects = new HashSet<PlayerObject>();
	}
	
	/**Returns a player if they exist, null otherwise. 
	 * @param discordid the Discord id of the player to find
	 * @return the PlayerObject of the player, or null if the player doesn't have an object yet
	 */
	private PlayerObject checkExists(long discordid)
	{
		for(PlayerObject p : playerObjects)
		{
			if(p.getDiscordUserInfo().getLongID() == discordid ) return p;
		}
		return null;
	}
	
	/**Returns a player if they exist, null otherwise. 
	 * @param kagName the KAG username of the player to find
	 * @return the PlayerObject of the player, or null if the player doesn't have an object yet
	 */
	private PlayerObject checkExists(String kagName)
	{
		for(PlayerObject p : playerObjects)
		{
			if(p.getKagName().equalsIgnoreCase(kagName) ) return p;
		}
		return null;
	}
	
	/**Create a new managed PlayerObject. Takes their KAG username and gets their Discord id from the database. 
	 * @param kagName the KAG username of the user to instantiate
	 * @return null if no Discord id was found for this KAG username (the player is not linked), or the new PlayerObject if creation is successful
	 */
	private PlayerObject addObject(String kagName)
	{
		//get their info from sql
		long id = DiscordBot.database.getDiscordID(kagName);
		//return null if they have no sql entry
		if(id==-1) return null;
		//we dont need to check the player doesnt already exist with the new data, this should be prevented by update
		//p = checkExists(id);
		PlayerObject p = new PlayerObject(id, kagName);
		//add the new player object to the list for next time its needed
		playerObjects.add(p);
		return p;
	}

	/**Create a new managed PlayerObject. Takes their Discord id and gets their KAG username from the database. 
	 * @param discordid the Discord id of the user to instantiate
	 * @return null if no KAG username was found for this Discord id (the player is not linked), or the new PlayerObject if creation is successful
	 */
	private PlayerObject addObject(long discordid)
	{
		//get their info from sql
		String kagname = DiscordBot.database.getKagName(discordid);
		//return null if they have no sql entry
		if(kagname=="") return null;
		//we dont need to check the player doesnt already exist with the new data, this should be prevented by update
		//p = checkExists(kagname);
		PlayerObject p = new PlayerObject(discordid, kagname);
		//add the new player object to the list for next time its needed
		playerObjects.add(p);
		return p;
	}
	
	/**Wrapper for getting a players PlayerObject. Creates the player object if they don't already have one. 
	 * @param user the Discord User object of the wanted player
	 * @return their PlayerObject
	 * @see #getObject(long)
	 */
	public PlayerObject getObject(IUser user)
	{
		return getObject(user.getLongID());
	}
	
	/**Getter for a players player object. Creates the player object if they don't already have one. 
	 * @param kagName the KAG username of the wanted player
	 * @return their PlayerObject
	 */
	public PlayerObject getObject(String kagName)
	{
		PlayerObject p = checkExists(kagName);
		if(p!=null) return p;
		//if the player doesnt have an object, create one
		return addObject(kagName);
	}

	/**Getter for a players player object. Creates the player object if they don't already have one. 
	 * @param discordid the Discord id of the wanted player
	 * @return their PlayerObject
	 */
	public PlayerObject getObject(long discordid)
	{
		PlayerObject p = checkExists(discordid);
		if(p!=null) return p;
		//if the player doesnt have an object, create one
		return addObject(discordid);
	}

	/**Wrapper for update(long discordid). Called when someones player info changes. 
	 * @param user the user object of the player that has changed
	 * @see #update(long)
	 */
	public void update(IUser user)
	{
		update(user.getLongID());
	}
	
	/**Called when someones player info is changed on the database. Gets their new info from the database. 
	 * @param kagName the KAG username of the player that has changed
	 */
	public void update(String kagName)
	{
		PlayerObject p = checkExists(kagName);
		//if the player exists update them based on current sql data
		if(p!=null)
		{
			//get their info from sql
			long id = DiscordBot.database.getDiscordID(kagName);
			p.setDiscordUserInfo(DiscordBot.client.getUserByID(id));
		}
		//could add their player object here, but will do lazy approach and only do that when the object is needed
		//addObject(kagName);
	}
	
	/**Called when someones player info is changed on the database. Gets their new info from the database. 
	 * @param discordid the Discord id of the player that has changed
	 */
	public void update(long discordid)
	{
		PlayerObject p = checkExists(discordid);
		//if the player exists update them based on current sql data
		if(p!=null)
		{
			//get their info from sql
			String kagname = DiscordBot.database.getKagName(discordid);
			p.setKagName(kagname);
		}
		//could add their player object here, but will do lazy approach and only do that when the object is needed
		//addObject(discordid);
	}

}
