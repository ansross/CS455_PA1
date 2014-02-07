package cs455.overlay.node;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Scanner;

import util.ResultSetter;
import util.Utilities;
import cs455.overlay.transport.Connection;
import cs455.overlay.transport.TCPSender;
import cs455.overlay.transport.TCPServerThread;
import cs455.overlay.wireformats.*;

public class Registry implements Node {
	//registry sends: link_weights, messaging_nodes_list, register_response, task_initiate, task_summary_request
	//registry recieves: deregister_request, register_response, task_complete, task_summary_response
	private ArrayList<nodeInformation> registeredNodes;// = new ArrayList<nodeInformation>();
	
	//to map the socket between register and node to the node Id comprised of node's serverSocket port
	private Hashtable<String, String> socketToNodeID;
	
	private Hashtable<String, Connection> establishedConnections;// = new Hashtable<String, Connection>();
	//to recieve events from reciving threads (aka from server thread)
	//private ArrayList<Event> receivedEvents = new ArrayList<Event>();
	
	int serverSocketPortNum;
	
	public Registry(int portNum){
		socketToNodeID= new Hashtable<String,String>();
		establishedConnections = new Hashtable<String, Connection>();
		registeredNodes = new ArrayList<nodeInformation>();
		new TCPServerThread(this, portNum).start();
		
	}
	
	public static void main(String [] args) throws IOException{
		if(args.length != 1){
			System.err.println("Usage: java cs455.overlay.node.Registry <port number>");
			System.exit(1);
		}
		int portNum = Integer.parseInt(args[0]);
		Registry reg = new Registry(portNum);
		while(true){
			reg.getCommandlineInput();
		}
		
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
		nodeInformation newNodeInfo = new nodeInformation(regReq.getIPAddress(), regReq.getServerPortNum(), socket.getInetAddress().getHostName(), socket.getPort());
		newNodeInfo.print();
		for(nodeInformation n: registeredNodes){
			if(n.equals(newNodeInfo)){
				success = 0;
				message = "Node with IPAddress " + regReq.getIPAddress() + " and port number "
						+ regReq.getServerPortNum() + " is already registered.";
				break;
			}
		}
		if(success==1){
			registeredNodes.add(newNodeInfo);
			message = "Registration Requestion successful. The number of messaging nodes currently "
					+ "in the overlay is ("+registeredNodes.size()+")";
			
		}
		
		System.out.println("Response Message: "+message);
		System.out.println("From Registration: "+Utilities.createKeyFromSocket(socket));
		TCPSender sender = establishedConnections.get(Utilities.createKeyFromSocket(socket)).getSender();
		sender.sendData(new RegisterResponse(success, message).getByte());
		System.out.println("Sent response");

		
		return success;
	}

	@Override
	public void registerConnection(Connection connection){
		establishedConnections.put(connection.getName(), connection);
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deregisterConnection(Connection connection) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void getCommandlineInput() {
		Scanner input = new Scanner(System.in);
		boolean exit = false;
		while(!exit){
			
			String command = input.next();
			switch(command){
			case "list-messaging-nodes":
				listMessagingingNodes();
				break;
			case "list-weights":
				System.out.println("Your Command: " + command +" does nothing!!");
				listWeights();
				break;
			case "setup-overlay":
				int numberOfConnections = input.nextInt();
				if(Protocol.DEBUG){
					System.out.println("Number of connections: "+numberOfConnections);
				}
				setupOverlay(numberOfConnections);
				break;
			case "send-overlay-link-weights":
				System.out.println("Your Command: " + command +" does nothing!!");
				sendOverlayLinkWeights();
				break;
			case "start":
				System.out.println("Your Command: " + command +" does nothing!!");
				start();
				break;
			case "list-commands":
				
				System.out.println("list-messaging-nodes \nlist-weights\nsetup-overlay <num-links (must = 4)>\nsend-overlay-link-weights\nstart");
				break;
			default:
				System.out.println("ERROR: command "+command+" not supported. \n To see supported commands use command list-commands");
				break;
				
			}
		}
		input.close();
		
		// TODO Auto-generated method stub
		
	}

	private void start() {
		// TODO Auto-generated method stub
		
	}

	private void sendOverlayLinkWeights() {
		// TODO Auto-generated method stub
		
	}

	private void setupOverlay(int numberOfConnections) {
		if(numberOfConnections!=4){
			System.out.println("SYSTEM DOES NOT SUPPORT NUMBER OF CONNECTIONS != 4");
		}
		else{
			//map each node to the list of nodes it needs to connect with
			Hashtable<String, ArrayList<String>> overlay = new Hashtable<String, ArrayList<String>>(registeredNodes.size());
			createOverlay(overlay);
			sendOverlay(overlay);
		}
	}
	
	private void sendOverlay(Hashtable<String, ArrayList<String>> overlay){
		for(nodeInformation node: registeredNodes){
			ArrayList<String> nodesConnections = overlay.get(node.getHostServerPort());
			/*ArrayList<String> nodeConnectionsNames = new ArrayList<String>();
			for(String connection: nodesConnections){
				nodeConnectionsNames.add(connection);
			}*/
			//don't send if has no necessary connections
			if(!nodesConnections.isEmpty()){
				TCPSender sender = establishedConnections.get(node.getHostRegPort()).getSender();
				MessagingNodesList overlayMessage = new MessagingNodesList(nodesConnections.size(), nodesConnections);
				try {
					sender.sendData(overlayMessage.getByte());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	private void createOverlay(Hashtable<String, ArrayList<String>> overlay){
		new Hashtable<String, ArrayList<nodeInformation>>(registeredNodes.size());
		for(nodeInformation regedNode: registeredNodes){
			overlay.put(regedNode.getHostServerPort(), new ArrayList<String>());
			overlay.get(0);
		}
		for(nodeInformation currentNode: registeredNodes){
			int currentIndex = registeredNodes.indexOf(currentNode);
			//to avoid negative indicies add the size, since doing modular, won't affect 
			currentIndex += registeredNodes.size();
			//try connecting to next, previous, two forward and two nodes backward
			int[] connectionIndicies ={-1,-2,1,2};// {(currentIndex-1)%registeredNodes.size(), (currentIndex-2)%registeredNodes.size(),
					//(currentIndex+1)%registeredNodes.size(), (currentIndex+2)%registeredNodes.size()};
		
			for(int i=0; i<4; ++i){
				connectionIndicies[i] = (currentIndex-connectionIndicies[i])%registeredNodes.size();
			}
			for(int index: connectionIndicies){

				nodeInformation connectionNode = registeredNodes.get(index);
				
				if(overlay.get(connectionNode.getHostServerPort()).isEmpty() || !(overlay.get(connectionNode.getHostServerPort()).contains(currentNode.getHostServerPort()))){
					overlay.get(currentNode.getHostServerPort()).add(connectionNode.getHostServerPort());
				}
			}
		}

	}

	private void listWeights() {
		// TODO Auto-generated method stub
		
	}

	private void listMessagingingNodes() {
		for(nodeInformation node: registeredNodes){
			node.print();
		}
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setServerSocketPortNum(int localPort) {
		serverSocketPortNum=localPort;
		// TODO Auto-generated method stub
		
	}


}


