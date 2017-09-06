import sx.blah.discord.handle.obj.IEmoji;
import sx.blah.discord.handle.obj.IMessage;

public class ReactWatchObject {
	public IMessage message;
	public IEmoji emoji;
	public GenericReactCallbackObject callback;
	
	ReactWatchObject(IMessage msg, IEmoji react, GenericReactCallbackObject cb)
	{
		message=msg;
		emoji=react;
		callback=cb;
	}
	
}
