package core;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**Listens to the tcp socket that is connected to the KAG server and sends received messages to its listeners. 
 * @author cameron
 *
 */
public class KagServerChecker implements Runnable
{
	static final Logger LOGGER = LoggerFactory.getLogger(KagServerChecker.class);
	private Socket socket;
	private List<RconListener> listeners;
	private PrintWriter out;
	private BufferedReader in;
	private String lastMsg;
	private boolean connected;
	private boolean reconnecting;
	private int reconnectTimer;			//milliseconds
	
	private String ip;
	private int port;
	private String rconPassword;
	
	private Queue<String> sendMessageQueue;
	
	KagServerChecker(String ip, int port, String rconPassword) throws UnknownHostException, IOException
	{
		listeners = new ArrayList<RconListener>();
		sendMessageQueue = new LinkedList<String>();
		this.ip=ip;
		this.port=port;
		this.rconPassword=rconPassword;
		this.reconnectTimer = 120000;
		connect();
	}
	
	/**Checks if this object thinks it is connected with the server. 
	 * @return the value of the connected variable
	 */
	public boolean isConnected()
	{
		return connected;
	}
	
	/**Changes the connection status of this object. 
	 * @param val true connected, false otherwise
	 */
	public void setConnected(boolean val)
	{
		connected = val;
	}
	
	/**Checks if this object thinks it is currently disconnected, but will try to reconnect soon.
	 * @return the value of the reconnecting variable
	 */
	public boolean isReconnecting()
	{
		return reconnecting;
	}
	
	/**Changes the reconnecting status of this object. 
	 * @param val true if attempting to reconnect, false otherwise
	 */
	public void setReconnecting(boolean val)
	{
		reconnecting = val;
	}
	
	/**Triggered whenever the connection is lost. Disconnects the socket and sets up a task to reconnect later. 
	 * If the sleep is interrupted, the reconnect attempt is canceled
	 */
	public void connectionLost()
	{
		//disconnect
		this.disconnect();
		//reconnect later
		this.setReconnecting(true);
		while(this.isReconnecting())
		{
			if(Thread.interrupted()) return;
			try {
				LOGGER.info("Attempting to reconnect to KAG server in "+reconnectTimer/60.0f+" seconds");
				Thread.sleep(reconnectTimer);
				try {
					this.connect();
				} catch (IOException e) {
					this.disconnect();
					LOGGER.error("An error occured connecting to the gather KAG server("+e.getMessage()+"): "+ip+":"+port);
				}
			} catch (InterruptedException e) {
				//if the sleep is interrupted then we want to stay disconnected
				this.setReconnecting(false);
				//have to recreate the interrupt because sleep consumes it when it throws the interrupted exception
				Thread.currentThread().interrupt();
				return;
			}
		}
	}
	
	/**Initiates the connection with the server. 
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	public void connect() throws UnknownHostException, IOException
	{

		socket = new Socket(ip, port);
		out = new PrintWriter(socket.getOutputStream(), true);
		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		LOGGER.info("Connected to KAG server: "+ip+":"+port);
		out.println(rconPassword);
		this.setReconnecting(false);
		this.setConnected(true);
	}
	
	/**Sends text to the KAG server. 
	 * @param msg the string to send
	 */
	public void sendMessage(String msg)
	{
		sendMessageQueue.add(msg);
	}
	
	/**Calls {@link #disconnect()}. 
	 * @see java.lang.Object#finalize()
	 */
	protected void finalize()
	{
		this.disconnect();
	}
	
	/**Disconnect from the kag server and close the apporpriate resources. 
	 */
	public void disconnect()
	{
		try {
			LOGGER.info("Disconnecting from KAG server: "+ip+":"+port);
			socket.close();
			in.close();
			out.close();
			setConnected(false);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**Add a listener object to be passed messages when they are received. 
	 * @param listener an RconListener object
	 * @see #RconListener
	 */
	public void addListener(RconListener listener)
	{
		listeners.add(listener);
	}
	
	/**Remove a listener so that it is no longer passed messages when they are received. 
	 * @param listener an RconListener object
	 * @see #RconListener
	 */
	public void removeListener(RconListener listener)
	{
		listeners.remove(listener);
	}
	
	
	/**runs the thread that checks the socket for messages. When a message is received it is sent to all listeners. Checks for messages to send and sends them all. Tries to detect disconnects by detection server shutdown messages.
	 * TODO implement a heartbeat in order to detect when the server is disconnected. 
	 * @see java.lang.Runnable#run()
	 */
	public void run()
	{
		while(!Thread.interrupted())
		{
			try {
				//check if there is any incoming messages from the server
				if(in.ready())
				{
					lastMsg = in.readLine();
					if(lastMsg == null || lastMsg.endsWith("server shutting down."))
					{
						LOGGER.info("connection loss detected: "+lastMsg);
						connectionLost();
					}
					else
					{
						for(RconListener listener : listeners)
						{
							listener.messageReceived(lastMsg, ip, port);
						}
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			//check for any messages waiting to be sent to the server
			while(sendMessageQueue.peek() != null)
			{
				out.println(sendMessageQueue.poll());
			}
		}
		this.disconnect();
	}
}
