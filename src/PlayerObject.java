import sx.blah.discord.handle.obj.IUser;

/**PlayerObject for holding all the variables assoiciated with a player. 
 * @author cameron
 * @see PlayerObjectManager
 */
public class PlayerObject
{
	private IUser discordUserInfo;
	private String kagName;
	private boolean captainsVote;
	private boolean inQueue;

	/**Variable for keeping track of how long it has been since this player object has been used
	 */
	private long lastUsed;

	private PlayerObject(IUser user, boolean capVote)
	{
		setDiscordUserInfo(user);
		setCaptainsVote(capVote);
		this.used();
	}

	private PlayerObject(IUser user)
	{
		this(user, false);
		this.used();
	}

	/**WARNING: DO NOT USE: PLAYER OBJECTS SHOULD NOT BE INSTANTIATED OUTSIDE OF THE PLAYER OBJECT MANAGER. Player objects instantiated outside of the manager will not be updated when a player changes their linked accounts. 
	 * @param user
	 * @param kagName
	 * @see PlayerObjectManager
	 */
	PlayerObject(IUser user, String kagName)
	{
		this(user);
		this.setKagName(kagName);
		this.used();
	}

	/**WARNING: DO NOT USE: PLAYER OBJECTS SHOULD NOT BE INSTANTIATED OUTSIDE OF THE PLAYER OBJECT MANAGER. Player objects instantiated outside of the manager will not be updated when a player changes their linked accounts. 
	 * @param id
	 * @param kagName
	 * @see PlayerObjectManager
	 */
	PlayerObject(long id, String kagName)
	{
		this(DiscordBot.fetchUser(id), kagName);
		this.used();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((discordUserInfo == null) ? 0 : discordUserInfo.hashCode());
		result = prime * result + ((kagName == null) ? 0 : kagName.toLowerCase().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PlayerObject other = (PlayerObject) obj;
		if (discordUserInfo == null) {
			if (other.discordUserInfo != null)
				return false;
		} else if (!discordUserInfo.equals(other.discordUserInfo))
			return false;
		if (kagName == null) {
			if (other.kagName != null)
				return false;
		} else if (!kagName.equalsIgnoreCase(other.kagName))
			return false;
		return true;
	}

	/**Getter for the players Discord user object. 
	 * Updates the players last used time.
	 * @return the IUser object associated with this player
	 */
	public IUser getDiscordUserInfo() {
		this.used();
		return discordUserInfo;
	}

	/**Setter for the players Discord user object. 
	 * Updates the players last used time.
	 * @param user the IUser object to be associated with this player
	 */
	public void setDiscordUserInfo(IUser user) {
		this.discordUserInfo = user;
		this.used();
	}

	/**Getter for the players KAG Username. 
	 * Updates the players last used time.
	 * @return the players KAG Username as a string
	 */
	public String getKagName() {
		this.used();
		return kagName;
	}

	/**Setter for the players KAG Username. 
	 * Updates the players last used time.
	 * @param kagName the KAG username to set
	 */
	public void setKagName(String kagName) {
		this.kagName = kagName;
		this.used();
	}


	public boolean isCaptainsVote() {
		this.used();
		return captainsVote;
	}

	public void setCaptainsVote(boolean captainsVote) {
		this.captainsVote = captainsVote;
		this.used();
	}

	public boolean isInQueue() {
		this.used();
		return inQueue;
	}

	public void setInQueue(boolean inQueue) {
		this.inQueue = inQueue;
		this.used();
	}

	/**Getter for the epoch time since this object was last used in milliseconds
	 * @return the last use time in milliseconds
	 */
	public long getLastUsed()
	{
		return lastUsed;
	}

	/** Variable for keeping track of how long it has been since the player object has been used
	 *  This is tracked and when the object gets too old it is changed to a weak reference
	 * @param lastUsed
	 */
	private void setLastUsed(long lastUsed)
	{
		this.lastUsed = lastUsed;
	}

	/**Updates the last used time for this player object to the current time
	 * Should be called every time this object is used
	 * @see PlayerObjectManager
	 */
	public void used()
	{
		this.setLastUsed(System.currentTimeMillis());
	}

	/**Getter for the players discord user id.
	 * @return the players discord user id
	 */
	public long getDiscordid()
	{
		return this.discordUserInfo.getLongID();
	}

	/**Getter for the users mention string for use in discord messages. 
	 * Updates the players last used time.
	 * @return the mention string
	 */
	public String getMentionString()
	{
		this.used();
		return getDiscordUserInfo().mention();
	}

	/**Get a player as a string formated as KAG Username(Discord Name#Discord Discriminator). 
	 * Does not update the players last used time.
	 * @return the players string
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		//dont want to call used here otherwise debug prints impact it
		return this.kagName +" ("+ this.discordUserInfo.getName()+"#"+this.discordUserInfo.getDiscriminator()+")";
	}
}