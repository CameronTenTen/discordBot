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

import sx.blah.discord.Discord4J;

public class KagServerChecker implements Runnable
{
	private Socket socket;
	private List<RconListener> listeners;
	private PrintWriter out;
	private BufferedReader in;
	private String lastMsg;
	private boolean connected;
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
	
	public boolean isConnected()
	{
		return connected;
	}
	
	public void setConnected(boolean val)
	{
		if(connected == true && val == false)
		{
			connected = val;
			connectionLost();
		}
		else
		{
			connected = val;
		}
	}
	
	public void connectionLost()
	{
		//disconnect
		disconnect();
		//reconnect later
		try {
			Discord4J.LOGGER.info("Attempting to reconnect to KAG server in "+reconnectTimer+" milliseconds");
			Thread.sleep(reconnectTimer);
		} catch (InterruptedException e) {
		}
		try {
			connect();
		} catch (IOException e) {
			Discord4J.LOGGER.error("An error occured connecting to the gather KAG server("+e.getMessage()+"): "+ip+":"+port);
			connectionLost();
		}
	}
	
	public void connect() throws UnknownHostException, IOException
	{

		socket = new Socket(ip, port);
		out = new PrintWriter(socket.getOutputStream(), true);
		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		Discord4J.LOGGER.info("Connected to KAG server: "+ip+":"+port);
		out.println(rconPassword);
		connected=true;
	}
	
	public void sendMessage(String msg)
	{
		sendMessageQueue.add(msg);
	}
	
	protected void finalize()
	{
		this.disconnect();
	}
	
	public void disconnect()
	{
		try {
			socket.close();
			in.close();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void addListener(RconListener listener)
	{
		listeners.add(listener);
	}
	
	public void removeListener(RconListener listener)
	{
		listeners.remove(listener);
	}
	
	
	public void run()
	{
		while(!Thread.interrupted())
		{
			try {
				if(in.ready())
				{
					lastMsg = in.readLine();
					System.out.println("msg rd: "+lastMsg);
					if(lastMsg == null || lastMsg.endsWith("server shutting down."))
					{
						Discord4J.LOGGER.info("connection loss detected: "+lastMsg);
						setConnected(false);
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
			while(sendMessageQueue.peek() != null)
			{
				out.println(sendMessageQueue.poll());
			}
		}
		this.disconnect();
	}
}
