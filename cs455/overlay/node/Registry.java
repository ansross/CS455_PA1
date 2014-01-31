package cs455.overlay.node;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import util.ResultSetter;
import cs455.overlay.transport.TCPSender;
import cs455.overlay.transport.TCPServerThread;
import cs455.overlay.wireformats.*;
import cs455.overlay.wireformats.Event;
import cs455.overlay.wireformats.Protocol;
import cs455.overlay.wireformats.RegisterRequest;

public class Registry implements Node {
	//registry sends: link_weights, messaging_nodes_list, register_response, task_initiate, task_summary_request
	//registry recieves: deregister_request, register_response, task_complete, task_summary_response
	private ArrayList<nodeInformation> registeredNodes = new ArrayList<nodeInformation>();
	
	//to recieve events from reciving threads (aka from server thread)
	//private ArrayList<Event> receivedEvents = new ArrayList<Event>();
	
	public Registry(){
		new TCPServerThread(this, 15004).start();
		
	}
	
	public static void main(String [] args) throws IOException{
		if(args.length != 1){
			System.err.println("Usage: java cs455.overlay.node.Registry <port number>");
			System.exit(1);
		}
		
		Registry reg = new Registry();
		
	}



	@Override
	public void onEvent(Event event, Socket socket) {
		switch(event.getType()){
		case Protocol.DEREGISTER:
			break;
		case Protocol.REGISTER_REQUEST:
			System.out.println("Recieved request");
			try {
				byte registrationSuccess = attemptRegistration((RegisterRequest)event, socket);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		case Protocol.TASK_COMPLETE:
			break;
		case Protocol.TASK_SUMMARY_RESPONSE:
			break;
		}
		// TODO Auto-generated method stub

	}

	//if a node with the same port and IPAddress is already registered, registry fails 
	//else, node is registered
	//a response is sent to requesting node
	private byte attemptRegistration(RegisterRequest regReq, Socket socket) throws IOException {
		//success==1 is successful, ==0 is unsuccessful
		byte success = 1;
		String message="";
		nodeInformation newNodeInfo = new nodeInformation(regReq.getIPAddress(), regReq.getPortNum());
		for(nodeInformation n: registeredNodes){
			if(n.equals(newNodeInfo)){
				success = 0;
				message = "Node with IPAddress " + regReq.getIPAddress() + " and port number "
						+ regReq.getPortNum() + " is already registered.";
				break;
			}
		}
		if(success==1){
			message = "Registration Requestion successful. The number of messaging nodes currently "
					+ "in the overlay is ("+registeredNodes.size()+")";
			registeredNodes.add(newNodeInfo);
			
		}
		
		System.out.println("Sent response");
		TCPSender sender = new TCPSender(socket);
		System.out.println(socket.getLocalPort());
		sender.sendData(new RegisterResponse(success, message).getByte());
		
		return success;
	}


}


