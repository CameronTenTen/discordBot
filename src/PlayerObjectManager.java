import java.util.HashSet;
import java.util.Set;

import sx.blah.discord.Discord4J;
import sx.blah.discord.handle.obj.IUser;

//class to keep track of player objects so that they can be updated when player data is changed (for example when a user links their accounts)
public class PlayerObjectManager
{
	private Set<PlayerObject> playerObjects;
	
	PlayerObjectManager()
	{
		playerObjects = new HashSet<PlayerObject>();
	}
	
	private PlayerObject checkExists(long discordid)
	{
		for(PlayerObject p : playerObjects)
		{
			if(p.getDiscordUserInfo().getLongID() == discordid ) return p;
		}
		return null;
	}
	
	private PlayerObject checkExists(String kagName)
	{
		for(PlayerObject p : playerObjects)
		{
			if(p.getKagName()== kagName ) return p;
		}
		return null;
	}
	
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
	
	public PlayerObject getObject(IUser user)
	{
		return getObject(user.getLongID());
	}
	
	public PlayerObject getObject(String kagName)
	{
		PlayerObject p = checkExists(kagName);
		if(p!=null) return p;
		//if the player doesnt have an object, create one
		return addObject(kagName);
	}
	
	public PlayerObject getObject(long discordid)
	{
		PlayerObject p = checkExists(discordid);
		if(p!=null) return p;
		//if the player doesnt have an object, create one
		return addObject(discordid);
	}
	
	//these functions are called when someone changes their player info
	public void update(IUser user)
	{
		update(user.getLongID());
	}
	
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
