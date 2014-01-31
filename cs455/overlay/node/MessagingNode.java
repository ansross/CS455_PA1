package cs455.overlay.node;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

import cs455.overlay.transport.*;
import cs455.overlay.wireformats.Event;
import cs455.overlay.wireformats.Protocol;
import cs455.overlay.wireformats.RegisterRequest;
import cs455.overlay.wireformats.*;
import cs455.overlay.transport.TCPServerThread;

public class MessagingNode implements Node {
	//msgNodes Send: register_requests, deregister requests, message, task_complete, task_summary_response
	//msgNodes recieve: link_weights, message, messaging_nodes_list, register_response, task_initiate, task_summary_request
	
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
	
	public MessagingNode(){
		
		numMessagesSent = 0;
		sumMessagesSent =0;
		numMessagesRecieved =0;
		sumMessagesRecieved = 0;
		numMessagesRelayed =0;
		
		new TCPServerThread(this).start();
	}
	
	public static void main(String[] args) throws IOException{
		if(args.length != 2){
			System.err.println("Usage: "
					+ "java cs455.overlay.node.MessagingNOde "
					+ "<registry-host> <registry-port>" );
			System.exit(1);
		}
		
		MessagingNode msgNode = new MessagingNode();
		
		String registryHostName = args[0];
		int registryPortNum = Integer.parseInt(args[1]);
		
		msgNode.attemptRegistration(registryHostName, registryPortNum);
	}
	
	private void attemptRegistration(String registryHostName, int registryPortNum){
		try(	//try to connect to registry
				
				Socket socket = new Socket(registryHostName, registryPortNum);

				
				)						
				{
			new TCPReceiverThread(this, socket).start();
			System.out.println("got Socket");
			TCPSender sender = new TCPSender(socket);
			System.out.println("port num "+socket.getLocalPort());
			RegisterRequest regReq = new RegisterRequest(InetAddress.getLocalHost().getHostName(), socket.getLocalPort(), "Tester");
			sender.sendData(regReq.getByte());
			System.out.println("Request Sent");
			
			
		}catch(IOException e){
			System.out.println("IOExecption Message Node");
					System.out.println(e);
		}
	}

	@Override
	public void onEvent(Event event, Socket socket) {
		switch(event.getType()){
		case Protocol.LINK_WEIGHTS:
			break;
		case Protocol.MESSAGE:
			break;
		case Protocol.MESSAGING_NODES_LIST:
			break;
		case Protocol.REGISTER_RESPONSE:
			System.out.println("got response");
			System.out.println(((RegisterResponse) event).getAdditionalInfo());
			break;
		case Protocol.TASK_INITIATE:
			break;
		case Protocol.TASK_SUMMARY_REQUEST:
			break;
		case Protocol.TASK_SUMMARY_RESPONSE:
			break;
		}
		// TODO Auto-generated method stub

	}
	
	public String messageNodeInfo(){
		return hostName + ":"+portNum;
	}

}
