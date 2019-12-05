package core;

import java.util.Set;

/**This object is used to load the gather object configuration from json
 * @author cameron
 *
 */
public class GatherObjectConfig
{
	public long guildID = 0L;
	public String commandChannelString = "";
	public long commandChannelID = 0L;
	public long blueVoiceID = 0L;
	public long redVoiceID = 0L;
	public long generalVoiceID = 0L;
	public long scoreReportID = 0L;
	public long adminRoleID = 0L;
	public long queueRoleID = 0L;
	public long softQueueRoleID = 0L;
	public long scoreboardMessageID = 0L;
	public long scoreboardChannelID = 0L;

	public Set<GatherServer> serverList;

}