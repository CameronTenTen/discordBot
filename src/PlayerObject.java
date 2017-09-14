import sx.blah.discord.handle.obj.IUser;

public class PlayerObject
{
	
	private IUser discordUserInfo;
	private String kagName;
	private boolean captainsVote;
	private boolean inQueue;
	
	PlayerObject(IUser user)
	{
		this(user, false);
	}

	PlayerObject(IUser user, boolean capVote)
	{
		setDiscordUserInfo(user);
		setCaptainsVote(capVote);
	}

	public boolean equals(Object player)
	{
		if (player == null)
		{
			return false;
		}
		//id should be a 100% check for if its the same player
		if(this.discordUserInfo.getLongID() == ((PlayerObject)player).discordUserInfo.getLongID())
		{
			return true;
		}
		return false;
	}
	
	public IUser getDiscordUserInfo() {
		return discordUserInfo;
	}

	public void setDiscordUserInfo(IUser author) {
		this.discordUserInfo = author;
	}
	
	public String getKagName() {
		return kagName;
	}

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
	
	public String getMentionString()
	{
		return getDiscordUserInfo().mention();
	}
	
	public String toString()
	{
		return discordUserInfo.getName()+"#"+discordUserInfo.getDiscriminator();
	}
}