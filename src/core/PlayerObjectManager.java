package core;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

import  discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.User;

/**class to keep track of player objects so that they can be updated when player data is changed (for example when a user links their accounts). All player objects should be created here. If they are created elsewhere they will become invalid if a player changes their linked accounts.
 * also periodically checks the last used time of each of the player objects and frees them for garbage collection if they have not been used for some time.
 * @author cameron
 *
 */
public class PlayerObjectManager
{
	//want this to be a map for efficiency reasons, feel like there should be a better way of doing this than maintaining two maps
	private Map<String, PlayerObject> kagNameToPlayerObjectMap;
	private Map<Snowflake, PlayerObject> discordidToPlayerObjectMap;
	//player objects are moved to the weak hash map when they are old, this is so the garbage collector can clean them up
	//don't want to just remove them without keeping the weak map in case there is still a reference to them used somewhere else
	//in that case, the player object will remain in the weak map and will be moved back to the strong one next time it is used
	private Map<String, WeakReference<PlayerObject>> weakKagNameToPlayerObjectMap;
	private Map<Snowflake, WeakReference<PlayerObject>> weakDiscordidToPlayerObjectMap;

	//functions for keeping the player cache clean
	private Timer timer;

	/**Wrapper for allowing a timer to take a lambda as its task
	 * @param r the function to be used
	 * @return the TimerTask object that can be used by a timer
	 */
	private static TimerTask wrap(Runnable r) {
	  return new TimerTask() {

	    @Override
	    public void run() {
	      r.run();
	    }
	  };
	}

	//the age threshold after which entries should be weakened
	private static int AGE_TO_DELETE = 129600000;			//1.5 days
	//how often to check the cache for old entries
	private static int CACHE_CLEAN_FREQUENCY = 7200000;		//2 hours

	/**Checks the last used time of all the currently cached player objects and changes them to a weak reference if they are too old
	 * The weak reference allows java to remove the object if no other part of the code is still using it too
	 */
	private void cleanCacheAndweakenOldReferences()
	{
		//check if any of the current weak references have been garbage collected yet
		Iterator<Entry<Snowflake, WeakReference<PlayerObject>>> weakDiscordidIterator = this.weakDiscordidToPlayerObjectMap.entrySet().iterator();
		while(weakDiscordidIterator.hasNext())
		{
			WeakReference<PlayerObject> weakRef = weakDiscordidIterator.next().getValue();
			if(weakRef==null || weakRef.get()==null)
			{
				weakDiscordidIterator.remove();
			}
		}
		Iterator<Entry<String, WeakReference<PlayerObject>>> weakKagNameIterator = this.weakKagNameToPlayerObjectMap.entrySet().iterator();
		while(weakKagNameIterator.hasNext())
		{
			WeakReference<PlayerObject> weakRef = weakKagNameIterator.next().getValue();
			if(weakRef==null || weakRef.get()==null)
			{
				weakKagNameIterator.remove();
			}
		}
		this.weakenOldReferences(false);
		this.printMaps();
	}

	/**Weaken all player object references that are older than {@value #AGE_TO_DELETE} milliseconds
	 * @param force override the last used age check - just weaken all the references anyway
	 */
	private void weakenOldReferences(boolean force)
	{
		long currentTime = System.currentTimeMillis();
		Iterator<Entry<String, PlayerObject>> i = this.kagNameToPlayerObjectMap.entrySet().iterator();
		while(i.hasNext())
		{
			PlayerObject playerObj = i.next().getValue();
			if(force || currentTime - playerObj.getLastUsed() > AGE_TO_DELETE)
			{
				//remove it from both strong maps
				i.remove();
				this.discordidToPlayerObjectMap.remove(playerObj.getDiscordid());
				//keep it in the weak map
				this.addToWeakMap(playerObj);
			}
		}
	}

	/**Helper for printing the current state of the cache to std out
	 */
	public void printMaps()
	{
		System.out.println(kagNameToPlayerObjectMap);
		System.out.println(discordidToPlayerObjectMap);
		System.out.println(weakKagNameToPlayerObjectMap);
		System.out.println(weakDiscordidToPlayerObjectMap);
	}

	/**Helper for getting a set containing all currently cached player objects
	 * @return a set containing all currently cached player objects
	 */
	public Set<PlayerObject> listPlayerCache()
	{
		Set<PlayerObject> returnSet = new HashSet<PlayerObject>();
		returnSet.addAll(kagNameToPlayerObjectMap.values());
		returnSet.addAll(discordidToPlayerObjectMap.values());
		returnSet.addAll(weakKagNameToPlayerObjectMap.values().stream().map(weakRef -> weakRef.get()).collect(Collectors.toSet()));
		returnSet.addAll(weakDiscordidToPlayerObjectMap.values().stream().map(weakRef -> weakRef.get()).collect(Collectors.toSet()));
		return returnSet;
	}

	/**Helper function for moving a player from the weak map to the strong map.
	 * Should be called whenever a player object is requested and it is found in the weak map
	 * @param p the player object to move
	 */
	private void moveFromWeakToStrongMap(PlayerObject p)
	{
		this.removeFromWeakMap(p);
		this.addToStrongMap(p);
		p.used();
	}

	/**Helper function for adding a player to the weak map
	 * @param p the player object to add
	 */
	private void addToWeakMap(PlayerObject p)
	{
		if(p==null) return;
		this.weakKagNameToPlayerObjectMap.put(p.getKagName().toLowerCase(), new WeakReference<PlayerObject>(p));
		this.weakDiscordidToPlayerObjectMap.put(p.getDiscordid(), new WeakReference<PlayerObject>(p));
	}

	/**Helper function for removing a player from the weak map
	 * @param p the player object to remove
	 */
	private void removeFromWeakMap(PlayerObject p)
	{
		weakDiscordidToPlayerObjectMap.remove(p.getDiscordid());
		weakKagNameToPlayerObjectMap.remove(p.getKagName());
	}

	/**Helper function for adding a player to the strong map
	 * @param p the player object to add
	 */
	private void addToStrongMap(PlayerObject p)
	{
		if(p==null) return;
		this.kagNameToPlayerObjectMap.put(p.getKagName().toLowerCase(), p);
		this.discordidToPlayerObjectMap.put(p.getDiscordid(), p);
	}

	/**Helper function for removing a player from the strong map
	 * @param p the player object to remove
	 */
	private void removeFromStrongMap(PlayerObject p)
	{
		this.discordidToPlayerObjectMap.remove(p.getDiscordid());
		this.kagNameToPlayerObjectMap.remove(p.getKagName().toLowerCase());
	}

	PlayerObjectManager()
	{
		kagNameToPlayerObjectMap = new HashMap<String, PlayerObject>();
		discordidToPlayerObjectMap = new HashMap<Snowflake, PlayerObject>();
		weakKagNameToPlayerObjectMap = new HashMap<String, WeakReference<PlayerObject>>();
		weakDiscordidToPlayerObjectMap = new HashMap<Snowflake, WeakReference<PlayerObject>>();

		//initialise the task for cleaning up old player objects
		timer = new Timer(true);
		timer.scheduleAtFixedRate(wrap(() -> this.cleanCacheAndweakenOldReferences()), CACHE_CLEAN_FREQUENCY, CACHE_CLEAN_FREQUENCY);
	}

	/**Returns a player if they exist, null otherwise.
	 * <p>
	 * Doesn't modify the cache in any way, useful for debug (the returned player object will update its used timer as normal)
	 * @param discordid the Discord id of the player to find
	 * @return the PlayerObject of the player, or null if the player doesn't have an object yet
	 */
	public PlayerObject checkCache(Snowflake discordid)
	{
		PlayerObject p = discordidToPlayerObjectMap.get(discordid);
		if(p!=null)
		{
			return p;
		}
		WeakReference<PlayerObject> weakRef = weakDiscordidToPlayerObjectMap.get(discordid);
		if(weakRef!=null)
		{
			p = weakRef.get();
			if(p!=null)
			{
				return p;
			}
		}
		return null;
	}

	/**Returns a player if they exist, null otherwise.
	 * <p>
	 * Doesn't modify the cache in any way, useful for debug (the returned player object will update its used timer as normal)
	 * @param kagName the KAG username of the player to find
	 * @return the PlayerObject of the player, or null if the player doesn't have an object yet
	 */
	public PlayerObject checkCache(String kagName)
	{
		kagName = kagName.toLowerCase();
		PlayerObject p = kagNameToPlayerObjectMap.get(kagName);
		if(p!=null)
		{
			return p;
		}
		WeakReference<PlayerObject> weakRef = weakKagNameToPlayerObjectMap.get(kagName);
		if(weakRef!=null)
		{
			p = weakRef.get();
			if(p!=null)
			{
				return p;
			}
		}
		return null;
	}

	/**Clear the current player object cache of all unused objects.
	 * If a discord id is specified, only that player is removed from the cache, otherwise the whole cache is cleared
	 * @param discordid the player to clear
	 * @return false either the player is not cached, or the specified player was not removed from the cache
	 */
	public boolean clearPlayerCache(Snowflake discordid)
	{
		this.printMaps();
		if(discordid != null)
		{
			//if an id was specified, weaken the reference to this user
			PlayerObject p = discordidToPlayerObjectMap.get(discordid);
			if(p==null)
			{
				//trying to remove a user that is not currently cached
				return false;
			}
			this.removeFromStrongMap(p);
			this.addToWeakMap(p);
		}
		else
		{
			//otherwise, weaken everything currently in the cache
			this.weakenOldReferences(true);
		}
		//garbage collect anything that can be collected
		System.gc();
		//move everything that didn't get collected back from the weak to the strong map
		for(WeakReference<PlayerObject> w : weakKagNameToPlayerObjectMap.values())
		{
			PlayerObject p = w.get();
			addToStrongMap(p);
		}
		//clear the weak map
		weakKagNameToPlayerObjectMap.clear();
		weakDiscordidToPlayerObjectMap.clear();
		this.printMaps();
		if(discordid != null && discordidToPlayerObjectMap.containsKey(discordid))
		{
			//a player to remove was specified, but they are still in the cache
			return false;
		}
		else
		{
			return true;
		}
	}

	/**Returns a player if they exist, null otherwise.
	 * @param discordid the Discord id of the player to find
	 * @return the PlayerObject of the player, or null if the player doesn't have an object yet
	 */
	public PlayerObject getIfExists(Snowflake discordid)
	{
		PlayerObject p = discordidToPlayerObjectMap.get(discordid);
		//System.out.println("discordid in strong map?"+p);
		if(p!=null)
		{
			p.used();
			return p;
		}
		WeakReference<PlayerObject> weakRef = weakDiscordidToPlayerObjectMap.get(discordid);
		if(weakRef!=null)
		{
			p = weakRef.get();
			//System.out.println("discordid in weak map?"+p);
			if(p!=null)
			{
				moveFromWeakToStrongMap(p);
				return p;
			}
		}
		return null;
	}

	/**Returns a player if they exist, null otherwise.
	 * @param kagName the KAG username of the player to find
	 * @return the PlayerObject of the player, or null if the player doesn't have an object yet
	 */
	public PlayerObject getIfExists(String kagName)
	{
		if(kagName == null) return null;
		kagName = kagName.toLowerCase();
		PlayerObject p = kagNameToPlayerObjectMap.get(kagName);
		//System.out.println("kagName in strong map?"+p);
		if(p!=null)
		{
			p.used();
			return p;
		}
		WeakReference<PlayerObject> weakRef = weakKagNameToPlayerObjectMap.get(kagName);
		if(weakRef!=null)
		{
			p = weakRef.get();
			//System.out.println("kagName in weak map?"+p);
			if(p!=null)
			{
				moveFromWeakToStrongMap(p);
				return p;
			}
		}
		return null;
	}

	/**Wrapper for getting a players PlayerObject by discord user object if they exist, null otherwise.
	 * @param user the discord user object of the player to find
	 * @return the PlayerObject of the player, or null if the player doesn't have an object yet
	 */
	public PlayerObject getIfExists(User user)
	{
		if(user == null) return null;
		return getIfExists(user.getId());
	}

	/**Create a new managed PlayerObject. Takes their KAG username and gets their Discord id from the database.
	 * @param kagName the KAG username of the user to instantiate
	 * @return null if no Discord id was found for this KAG username (the player is not linked), or the new PlayerObject if creation is successful
	 */
	private PlayerObject addObject(String kagName, Snowflake guildId)
	{
		//get their info from sql
		long id = DiscordBot.database.getDiscordID(kagName);
		//also get the kag name incase the provided string was not the right case
		kagName = DiscordBot.database.getKagName(id);
		//return null if they have no sql entry
		if(id==-1) return null;
		//we dont need to check the player doesnt already exist with the new data, this should be prevented by update
		//p = checkExists(id);
		PlayerObject p = new PlayerObject(Snowflake.of(id), guildId, kagName);
		//add the new player object to the maps for next time its needed
		addToStrongMap(p);
		return p;
	}

	/**Create a new managed PlayerObject. Takes their Discord id and gets their KAG username from the database.
	 * @param discordid the Discord id of the user to instantiate
	 * @return null if no KAG username was found for this Discord id (the player is not linked), or the new PlayerObject if creation is successful
	 */
	private PlayerObject addObject(Member member)
	{
		//get their info from sql
		String kagname = DiscordBot.database.getKagName(member.getId().asLong());
		//return null if they have no sql entry
		if(kagname=="") return null;
		//we dont need to check the player doesnt already exist with the new data, this should be prevented by update
		//p = checkExists(kagname);
		PlayerObject p = new PlayerObject(member, kagname);
		//add the new player object to the list for next time its needed
		addToStrongMap(p);
		return p;
	}

	/**Wrapper for getting a players PlayerObject by discord user object. Creates the player object if they don't already have one.
	 * @param user the Discord User object of the wanted player
	 * @return their PlayerObject
	 * @see #getOrCreatePlayerObject(long)
	 */
	public PlayerObject getOrCreatePlayerObject(Member member)
	{
		PlayerObject p = getIfExists(member.getId());
		if(p!=null) return p;
		//if the player doesnt have an object, create one
		return addObject(member);
	}

	/**Getter for a players player object. Creates the player object if they don't already have one.
	 * @param kagName the KAG username of the wanted player
	 * @return their PlayerObject
	 */
	public PlayerObject getOrCreatePlayerObject(String kagName, Snowflake guildId)
	{
		PlayerObject p = getIfExists(kagName);
		if(p!=null) return p;
		//if the player doesnt have an object, create one
		return addObject(kagName, guildId);
	}

	/**Getter for a players player object. Creates the player object if they don't already have one.
	 * @param discordid the Discord id of the wanted player
	 * @return their PlayerObject
	 */
	public PlayerObject getOrCreatePlayerObject(Snowflake discordid, Snowflake guildId)
	{
		PlayerObject p = getIfExists(discordid);
		if(p!=null) return p;
		//if the player doesnt have an object, create one
		return addObject(DiscordBot.fetchMember(guildId, discordid));
	}

	/**Updates a player object in the cache with a new value for their discord id.
	 * <p>
	 * Removes any existing strong/weak discord map entries for {@link PlayerObject#getDiscordid()},
	 * then updates the discord id stored in the player object,
	 * and re-adds the player object to the map using the new discord id value as the map key.
	 * @param p the player object to update
	 * @param discordid the new discord id
	 */
	private void update(PlayerObject p, Member member) {
		//remove the player from the map (we need to update the key they are stored under)
		discordidToPlayerObjectMap.remove(p.getDiscordid());
		weakDiscordidToPlayerObjectMap.remove(p.getDiscordid());
		p.setDiscordUserInfo(member);
		//add them back into the map with the new key
		discordidToPlayerObjectMap.put(member.getId(), p);
	}

	/**Updates a player object in the cache with a new value for their kagName.
	 * <p>
	 * Removes any existing strong/weak discord map entries for {@link PlayerObject#getKagName()},
	 * then updates the kagName stored in the player object,
	 * and re-adds the player object to the map using the new kagName value as the map key.
	 * @param p the player object to update
	 * @param discordid the new discord id
	 */
	private void update(PlayerObject p, String kagName) {
		//remove the player from the map (we need to update the key they are stored under)
		kagNameToPlayerObjectMap.remove(p.getKagName());
		weakKagNameToPlayerObjectMap.remove(p.getKagName());
		p.setKagName(kagName);
		//add them back into the map with the new key
		kagNameToPlayerObjectMap.put(kagName, p);
	}

	/**Wrapper for update(long discordid). Called when someones player info changes.
	 * @param user the user object of the player that has changed
	 * @see #refresh(long)
	 */
	public void refresh(User user)
	{
		refresh(user.getId());
	}

	/**Called when someones player info is changed on the database. Gets their new info from the database.
	 * @param kagName the KAG username of the player that has changed
	 */
	public void refresh(String kagName, Snowflake guildId)
	{
		PlayerObject p = getIfExists(kagName);
		//if the player exists update them based on current sql data
		if(p!=null)
		{
			//get their info from sql
			long id = DiscordBot.database.getDiscordID(kagName);
			this.update(p, DiscordBot.fetchMember(guildId, Snowflake.of(id)));
		}
		//could add their player object here, but will do lazy approach and only do that when the object is needed
		//addObject(kagName);
	}

	/**Called when someones player info is changed on the database. Gets their new info from the database.
	 * @param discordid the Discord id of the player that has changed
	 */
	public void refresh(Snowflake discordId)
	{
		PlayerObject p = getIfExists(discordId);
		//if the player exists update them based on current sql data
		if(p!=null)
		{
			//get their info from sql
			String kagName = DiscordBot.database.getKagName(discordId.asLong());
			this.update(p, kagName);
		}
		//could add their player object here, but will do lazy approach and only do that when the object is needed
		//addObject(discordid);
	}
	/**Called when someones player info has been changed, and we want to force the bot to recognise it without checking the database.
	 * This is done in case the database table is not forcing discord ids and kag usernames to be unique, which would cause problems if we retrive from the database on update.
	 * If we force the bot to recognise the new info, at least they will be able to play for now, although there will probably be problems when the data is next retrieved from the database.
	 * @param kagName the KAG username of the player that has changed
	 * @return returns false if the force update failed
	 */
	public boolean forceUpdate(String kagName, Member member)
	{
		PlayerObject playerByKagname = getIfExists(kagName);
		PlayerObject playerByDiscordid = getIfExists(member);
		if(playerByKagname==null && playerByDiscordid==null)
		{
			PlayerObject p = new PlayerObject(member, kagName);
			//add the new player object to the list for next time its needed
			addToStrongMap(p);
			return true;
		}
		else
		{
			if(playerByKagname!=null && playerByDiscordid!=null && playerByKagname != playerByDiscordid)
			{
				//two different players and discord accounts are linked, and one of the discord users is trying to link with some other account
				//not much we can do to recover from this situation, need to remove one of the player objects, but that is difficult to guarantee (they might be in a queue, need to check everything)
				//the false return should be used to tell the user off
				return false;
			}
			if(playerByKagname!=null)
			{
				this.update(playerByKagname, member);
			}
			if(playerByDiscordid!=null)
			{
				this.update(playerByDiscordid, kagName);
			}
			return true;
		}
	}
	
	public boolean forceUpdate(String kagName, long userId, long guildId)
	{
		return this.forceUpdate(kagName, DiscordBot.fetchMember(Snowflake.of(guildId), Snowflake.of(userId)));
	}
}
