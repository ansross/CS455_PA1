package cs455.overlay.node;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

import cs455.overlay.transport.*;
import cs455.overlay.wireformats.Event;
import cs455.overlay.wireformats.RegisterRequest;
import cs455.overlay.transport.TCPServerThread;

public class MessagingNode implements Node {
	private String IPAddress;
	private int portNum;
	private int numMessagesSent;
	private long sumMessagesSent;
	private int numMessagesRecieved;
	private long sumMessagesRecieved;
	private int numMessagesRelayed;
	private String hostName;
	
	//private TCPSender sender;
	
	public String getIPAddress(){
		return IPAddress;
	}
	
	public int getPortNum(){
		return portNum;
	}
	
	
	public static void main(String[] args) throws IOException{
		if(args.length != 2){
			System.err.println("Usage: "
					+ "java cs455.overlay.node.MessagingNOde "
					+ "<registry-host> <registry-port>" );
			System.exit(1);
		}
		
		String registryHostName = args[0];
		int registryPortNum = Integer.parseInt(args[1]);
		//start a server for every messaging node to listen for requests
		new TCPServerThread().start();
		System.out.println("Message made server");
		
		try(	//try to connect to registry
				
				Socket socket = new Socket(registryHostName, registryPortNum);

				
				)						
				{
			System.out.println("got Socket");
			TCPSender sender = new TCPSender(socket);
			System.out.println("port num "+socket.getLocalPort());
			RegisterRequest regReq = new RegisterRequest(InetAddress.getLocalHost().getHostName(), socket.getLocalPort(), "Tester");
			sender.sendData(regReq.getByte());
			System.out.println("Message: "+new String(regReq.getByte()));
			System.out.println("Request Sent");
			
			
		}catch(IOException e){
			System.out.println("IOExecption Message Node");
					System.out.println(e);
		}
	}
	
	public MessagingNode(){
		numMessagesSent = 0;
		sumMessagesSent =0;
		numMessagesRecieved =0;
		sumMessagesRecieved = 0;
		numMessagesRelayed =0;
		
	}
	@Override
	public void onEvent(Event event) {
		// TODO Auto-generated method stub

	}
	
	public String messageNodeInfo(){
		return hostName + ":"+portNum;
	}

}
