import sx.blah.discord.handle.obj.IUser;

/**PlayerObject for holding all the variables assoiciated with a player. 
 * @author cameron\
 * @see PlayerObjectManager
 */
public class PlayerObject
{
	
	private IUser discordUserInfo;
	private String kagName;
	private boolean captainsVote;
	private boolean inQueue;

	private PlayerObject(IUser user, boolean capVote)
	{
		setDiscordUserInfo(user);
		setCaptainsVote(capVote);
	}
	
	private PlayerObject(IUser user)
	{
		this(user, false);
	}
	
	/**WARNING: PLAYER OBJECTS SHOULD NOT BE INSTANTIATED OUTSIDE OF THE PLAYER OBJECT MANAGER. Player objects instantiated outside of the manager will not be updated when a player changes their linked accounts. 
	 * @param user
	 * @param kagName
	 * @see PlayerObjectManager
	 */
	PlayerObject(IUser user, String kagName)
	{
		this(user);
		this.setKagName(kagName);
	}
	
	/**WARNING: PLAYER OBJECTS SHOULD NOT BE INSTANTIATED OUTSIDE OF THE PLAYER OBJECT MANAGER. Player objects instantiated outside of the manager will not be updated when a player changes their linked accounts. 
	 * @param id
	 * @param kagName
	 * @see PlayerObjectManager
	 */
	PlayerObject(long id, String kagName)
	{
		this(DiscordBot.client.fetchUser(id), kagName);
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
	 * @return the IUser object associated with this player
	 */
	public IUser getDiscordUserInfo() {
		return discordUserInfo;
	}

	/**Setter for the players Discord user object. 
	 * @param user the IUser object to be associated with this player
	 */
	public void setDiscordUserInfo(IUser user) {
		this.discordUserInfo = user;
	}
	
	/**Getter for the players KAG Username. 
	 * @return the players KAG Username as a string
	 */
	public String getKagName() {
		return kagName;
	}

	/**Setter for the players KAG Username. 
	 * @param kagName the KAG username to set
	 */
	public void setKagName(String kagName) {
		this.kagName = kagName;
	}


	public boolean isCaptainsVote() {
		return captainsVote;
	}

	public void setCaptainsVote(boolean captainsVote) {
		this.captainsVote = captainsVote;
	}
	
	public boolean isInQueue() {
		return inQueue;
	}

	public void setInQueue(boolean inQueue) {
		this.inQueue = inQueue;
	}
	
	/**Getter for the users mention string for use in discord messages. 
	 * @return the mention string
	 */
	public String getMentionString()
	{
		return getDiscordUserInfo().mention();
	}
	
	/**Get a player as a string formated as KAG Username(Discord Name#Discord Discriminator). 
	 * @return the players string
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		return getKagName() +" ("+ getDiscordUserInfo().getName()+"#"+getDiscordUserInfo().getDiscriminator()+")";
	}
}