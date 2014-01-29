package cs455.overlay.node;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import cs455.overlay.transport.TCPServerThread;
import cs455.overlay.wireformats.Deregister;
import cs455.overlay.wireformats.Event;
import cs455.overlay.wireformats.Protocol;
import cs455.overlay.wireformats.RegisterRequest;

public class Registry implements Node {
	private ArrayList<nodeInformation> registeredNodes;
	
/*	public static void main(String [] args) throws IOException{
		if(args.length != 1){
			System.err.println("Usage: java cs455.overlay.node.Registry <port number>");
			System.exit(1);
		}
		try(
				ServerSocket serverSocket = new ServerSocket(0);
				Socket msgNodeClientSocket = serverSocket.accept();
				
		){
		}catch(IOException e){
			System.out.println("Exception caught when trying to listen on port or"
					+ "listening for a connection");
			System.out.println(e.getMessage());
		}
		
	}*/
	
	public static void main(String [] args) throws IOException{
		if(args.length != 1){
			System.err.println("Usage: java cs455.overlay.node.Registry <port number>");
			System.exit(1);
		}
		new TCPServerThread().start();
		try(
				ServerSocket serverSocket = new ServerSocket(0);)
				{
			while(true){
				new TCPServerThread(serverSocket.accept()).start();
			}
		}
			/*	Socket msgNodeClientSocket = serverSocket.accept();
				DataInputStream din = new DataInputStream(msgNodeClientSocket.getInputStream());
				DataOutputStream dout = new DataOutputStream(msgNodeClientSocket.getOutputStream());
			*/	
		//){
		catch(IOException e){
			System.out.println("Exception caught when trying to listen on port or"
					+ "listening for a connection");
			System.out.println(e.getMessage());
		}
		
	}


	public Registry(){
		
	}
	@Override
	public void onEvent(Event event) {
		switch(event.getType()){
		case Protocol.DEREGISTER:
			break;
		case Protocol.LINK_WEIGHTS:
			break;
		case Protocol.MESSAGE:
			break;
		case Protocol.MESSAGING_NODES_LIST:
			break;
		case Protocol.REGISTER_REQUEST:
			boolean registrationSuccess = attemptRegistration((RegisterRequest)event);
			break;
		case Protocol.REGISTER_RESPONSE:
			break;
		case Protocol.TASK_COMPLETE:
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

	//if a node with the same port and IPAddress is already registered, registry fails 
	//else, node is registered
	//a response is sent to requesting node
	private boolean attemptRegistration(RegisterRequest regReq) {
		boolean success = true;
		String message;
		nodeInformation newNodeInfo = new nodeInformation(regReq.getIPAddress(), regReq.getPortNum());
		for(nodeInformation n: registeredNodes){
			if(n.equals(newNodeInfo)){
				success = false;
				message = "Node with IPAddress " + regReq.getIPAddress() + " and port number "
						+ regReq.getPortNum() + " is already registered.";
				break;
			}
		}
		if(success){
			registeredNodes.add(newNodeInfo);
			
		}
		return false;
	}


}

