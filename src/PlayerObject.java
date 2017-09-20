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
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((discordUserInfo == null) ? 0 : discordUserInfo.hashCode());
		result = prime * result + ((kagName == null) ? 0 : kagName.hashCode());
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
		} else if (!kagName.equals(other.kagName))
			return false;
		return true;
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