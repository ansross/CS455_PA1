package cs455.overlay.node;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import util.ResultSetter;
import util.Utilities;
import cs455.overlay.dijkstra.GraphNode;
import cs455.overlay.transport.Connection;
import cs455.overlay.transport.TCPSender;
import cs455.overlay.transport.TCPServerThread;
import cs455.overlay.wireformats.*;

public class Registry implements Node {

	/******* LOCAL VARIABLES *******/
	//registry sends: link_weights, messaging_nodes_list, register_response, task_initiate, task_summary_request
	//registry recieves: deregister_request, register_response, task_complete, task_summary_response
	private ArrayList<nodeInformation> registeredNodes;// = new ArrayList<nodeInformation>();

	//to map the socket between register and node to the node Id comprised of node's serverSocket port
	private Hashtable<String, String> socketToNodeID;

	private Hashtable<String, Connection> establishedConnections;// = new Hashtable<String, Connection>();
	//to recieve events from reciving threads (aka from server thread)
	//private ArrayList<Event> receivedEvents = new ArrayList<Event>();

	//to store overlay
	Hashtable<String, ArrayList<LinkInfo>> overlay; 
	int numLinks;

	int serverSocketPortNum;
	private TCPServerThread server;

	//initialized when start since won't be sending completes until then
	private Lock taskCompRecLock = new ReentrantLock();
	private int taskCompletesReceived;
	//initialzed when request summaries
	//private Lock numSumRecLock = new ReentrantLock();
	ArrayList<TaskSummaryResponse> receivedSummaries;

	/********* CTORS ***********/
	public Registry(int portNum){
		//numLinks = 0;
		//overlay initilized in setup overlay call
		//overlay = new Hashtable<String, ArrayList<String>>();
		socketToNodeID= new Hashtable<String,String>();
		establishedConnections = new Hashtable<String, Connection>();
		registeredNodes = new ArrayList<nodeInformation>();
		new TCPServerThread(this, portNum).start();
		receivedSummaries = new ArrayList<TaskSummaryResponse>();

	}


	/******* SETTERS AND GETTERS ********/
	public void setServerThread(TCPServerThread arg){
		this.server = arg;
	}

	public TCPServerThread getServerThread(){
		return server;
	}
	
	@Override
	public void setServerSocketPortNum(int localPort) {
		serverSocketPortNum=localPort;
	}

	//get name based on hostName:serverSocketPort
	@Override
	public String getHostServerName() {
		try {
			return InetAddress.getLocalHost().getHostName()+":"+this.serverSocketPortNum;
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/****** MAIN AND CORE FUNCTIONALITY ******/

	public static void main(String [] args) throws IOException{
		System.out.println("Registry Starts");
		if(args.length != 1){
			System.err.println("Usage: java cs455.overlay.node.Registry <port number>");
			System.exit(1);
		}
		int portNum = Integer.parseInt(args[0]);
		Registry reg = new Registry(portNum);
		//reg.setServerThread(new TCPServerThread(reg));
		//reg.getServerThread().start();
		//System.out.println("Server thread started");
		while(true){
			reg.getCommandlineInput();
		}

	}

	@Override
	public synchronized void onEvent(Event event, Socket socket) {
		switch(event.getType()){
		case Protocol.DEREGISTER:
			try {
				attemptDeregistration((Deregister) event, socket);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			break;
		case Protocol.REGISTER_REQUEST:
			//System.out.println("Recieved request");
			try {

				byte registrationSuccess = attemptRegistration((RegisterRequest)event, socket);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		case Protocol.TASK_COMPLETE:
			try{
				taskCompRecLock.lock();
				taskCompletesReceived++;
				//System.out.println("Recieved taskComplete. Now have: "+taskCompletesReceived);
				//System.out.println("Witing for: "+registeredNodes.size());
				if(taskCompletesReceived == registeredNodes.size()){
					try {
						requestSummaries();
						System.out.println("Requested Summaries");
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}finally{
				taskCompRecLock.unlock();
			}

			break;
		case Protocol.TASK_SUMMARY_RESPONSE:
			try{
				//numSumRecLock.lock();
				receivedSummaries.add((TaskSummaryResponse) event);
				//System.out.println("Recieved summary response. Now have: "+receivedSummaries.size()+
				//	". Need: "+registeredNodes.size());
				if(receivedSummaries.size() == registeredNodes.size()){
					parseSummaries();
				}
			}
			finally{
				//numSumRecLock.unlock();
			}
			break;
		}
		// TODO Auto-generated method stub

	}

	//@Override
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
				listWeights();
				break;
			case "setup-overlay":
				int numberOfConnections = input.nextInt();
				if(Protocol.DEBUG){
					System.out.println("Number of connections: "+numberOfConnections);
				}
				setupOverlay(numberOfConnections);
				System.out.println("Overlay Setup");
				break;
			case "send-overlay-link-weights":
				try {
					sendOverlayLinkWeights();
					System.out.println("Link Weights Sent");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			case "start":
				try {
					start();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
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
	/******** EVENTS AND COMMAND RESPONSES **********/

	/*** EVENT: Registration_Request -> EVENT: Register_Response ***/

	//if a node with the same port and IPAddress is already registered, registry fails 
	//else, node is registered
	//a response is sent to requesting node
	private byte attemptRegistration(RegisterRequest regReq, Socket socket) throws IOException {
		//success==1 is successful, ==0 is unsuccessful
		byte success = 1;
		String message="";
		nodeInformation newNodeInfo = new nodeInformation(regReq.getHostName(), regReq.getServerPortNum(), 
				socket.getInetAddress().getHostName(), socket.getPort());
		System.out.print("Attempting to register ");
		newNodeInfo.print();
		//System.out.println(regReq.getIPAddress() + " from socket: "+socket.getInetAddress().getHostAddress());
		if(!regReq.getIPAddress().equals(socket.getInetAddress().getHostAddress())){
			success = 0;
			message = "Sent IP address does not correspond to IP address of sender.";
		}
		//if passed first test, look for duplicates
		if(success==1){
			for(nodeInformation n: registeredNodes){
				if(n.equals(newNodeInfo)){
					success = 0;
					message = "Node with IPAddress " + regReq.getHostName() + " and port number "
							+ regReq.getServerPortNum() + " is already registered.";
					break;
				}
			}
		}
		if(success==1){
			registeredNodes.add(newNodeInfo);
			message = "Registration Request successful. \nThe number of messaging nodes currently "
					+ "in the overlay is ("+registeredNodes.size()+")\n";

		}

		System.out.println(message);
		//System.out.println("From Registration: "+Utilities.createKeyFromSocket(socket));
		TCPSender sender = establishedConnections.get(Utilities.createKeyFromSocket(socket)).getSender();
		sender.sendData(new RegisterResponse(success, message).getByte());
		//System.out.println("Sent response");


		return success;
	}

	/*** EVENT: Deregister 
	 * @throws IOException ***/
	private void attemptDeregistration(Deregister deregReq, Socket socket) throws IOException{
		byte success = 1;
		String message="";
		nodeInformation newNodeInfo = new nodeInformation(deregReq.getIPAddress(), deregReq.getServerPortNum(), socket.getInetAddress().getHostName(), socket.getPort());
		System.out.print("Attempting to deregister ");
		newNodeInfo.print();
		System.out.println(deregReq.getIPAddress() +" "+socket.getInetAddress().getHostAddress());
		if(!deregReq.getIPAddress().equals(socket.getInetAddress().getHostAddress())){
			success = 0;
			message = "Sent IP address does not correspond to IP address of sender.";
		}
		if(success==1){
			for(nodeInformation ni: registeredNodes){
				if(ni.getHostServerPort().equals(newNodeInfo.getHostServerPort())){
					success = 1;
					System.out.println("Node with IPAddress "+deregReq.getIPAddress() +" and port number "+deregReq.getServerPortNum() +" deregistered"); 
					registeredNodes.remove(ni);
					message = "Deregistration Request successful. The number of messaging nodes currently "
							+ "in the overlay is ("+registeredNodes.size()+")";
					break;
				}
			}
		}
		//if didn't find it and failure wasn't caused by invalid IP check
		if(success == 1 && message.equals("")){
			message = "Node was never registered with this registry";
		}
		System.out.println(message);
		establishedConnections.get(Utilities.createKeyFromSocket(socket)).getSender().sendData(new DeregisterResponse(success, message).getByte());
		establishedConnections.remove(Utilities.createKeyFromSocket(socket));
	}
	
	/*** COM: Setup-Overlay  -> EVENT: Messaging_Nodes_List***/
	//create overlay from existing nodes based on number of connections (ONLY SUPPORTS 10 NODES WITH 4 CONNECTIONS
	//send each node a message of who they should connect to
	private void setupOverlay(int numberOfConnections) {
		//TODO CHANGE BACK
		if(numberOfConnections!=4){
			System.out.println("SYSTEM DOES NOT SUPPORT NUMBER OF CONNECTIONS != 4");
			System.out.println("please retry setting up overlay with 10 nodes and 4 connections");
		}
		else
		{
			//System.out.println("Here");
			//map each node to the list of nodes it needs to connect with
			numLinks = 0;
			overlay = new Hashtable<String, ArrayList<LinkInfo>>(registeredNodes.size());
			createOverlay(numberOfConnections);
			sendOverlay();
		}
	}

	private void createOverlay(int numConnections){
		//System.out.println("create");
		new Hashtable<String, ArrayList<nodeInformation>>(registeredNodes.size());
		for(nodeInformation regedNode: registeredNodes){
			overlay.put(regedNode.getHostServerPort(), new ArrayList<LinkInfo>());
			overlay.get(0);
		}
		for(nodeInformation currentNode: registeredNodes){
			int currentIndex = registeredNodes.indexOf(currentNode);
			//to avoid negative indicies add the size, since doing modular, won't affect 
			currentIndex += registeredNodes.size();
			//try connecting to next, previous, two forward and two nodes backward
			int[] connectionIndicies ={1,-1,2,-2};// {(currentIndex-1)%registeredNodes.size(), (currentIndex-2)%registeredNodes.size(),
			//(currentIndex+1)%registeredNodes.size(), (currentIndex+2)%registeredNodes.size()};
			int[] sizedConnectionIndicies = new int[numConnections];
			for(int i=0; i<numConnections; ++i){
				sizedConnectionIndicies[i] = (currentIndex-connectionIndicies[i])%registeredNodes.size();
			}
			for(int index: sizedConnectionIndicies){

				nodeInformation connectionNode = registeredNodes.get(index);
				//if it hasn't been connected the other way, conenct it 
				if(overlay.get(connectionNode.getHostServerPort()).isEmpty() || 
						!(overlay.get(connectionNode.getHostServerPort()).contains(currentNode.getHostServerPort())))
				{
					overlay.get(currentNode.getHostServerPort()).add(new LinkInfo(currentNode.getHost(), currentNode.getServerPort(),
							connectionNode.getHost(), connectionNode.getServerPort()));
					numLinks++;
				}
			}
		}
		//System.out.println("num links after overlay creation: " + numLinks);

	}

	private void sendOverlay(){
		
		for(nodeInformation node: registeredNodes){
			ArrayList<LinkInfo> nodesConnections = overlay.get(node.getHostServerPort());
			ArrayList<String> nodeConnectionsNames = new ArrayList<String>();
			for(LinkInfo connection: nodesConnections){
				nodeConnectionsNames.add(connection.getHostBPortB());
			}
			//don't send if has no necessary connections
			if(!nodesConnections.isEmpty()){
				TCPSender sender = establishedConnections.get(node.getHostRegPort()).getSender();
				MessagingNodesList overlayMessage = new MessagingNodesList(nodeConnectionsNames.size(), nodeConnectionsNames);
				try {
					//System.out.println("send");
					sender.sendData(overlayMessage.getByte());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	/*** COM: list-messaging-nodes ***/
	//list all currently registered messaging nodes
	private void listMessagingingNodes() {
		for(nodeInformation node: registeredNodes){
			node.print();
		}
		// TODO Auto-generated method stub

	}
	
	/*** COM: List-weights ***/
	//list weights of all connections between all nodes in overlay
	//only meaningful after send-overlay-link-weights is called
	private void listWeights() {
		for(nodeInformation ni: registeredNodes){
			for(LinkInfo li: overlay.get(ni.getHostServerPort())){
				System.out.println(li.getFullInfo());
			}
		}
	}

	/*** COM: send-overlay-link-weights -> EVENT: Link_Weights ***/
	//generate random weights for all links in overlay, 
	//then send the complete list to all nodes
	private void sendOverlayLinkWeights() throws IOException {
		generateListWeights();
		sendListWeights();
	}
	
	private void generateListWeights(){
		Random rand = new Random();
		for(nodeInformation regedNode: registeredNodes){
			for(LinkInfo li: overlay.get(regedNode.getHostServerPort())){
				if(regedNode.getHostServerPort().equals(li.getHostBPortB())){
					System.out.println("FLIP B TO A");
				}
				if(li.getWeight() < 0){
					int linkWeight = rand.nextInt(10)+1;
					li.setWeight(linkWeight);
					ArrayList<LinkInfo> arli = overlay.get(li.getHostBPortB());
					for(int i=0; i<arli.size(); ++i){
						if(arli.get(i).getHostBPortB().equals(regedNode.getHostServerPort())){
							arli.get(i).setWeight(linkWeight);
							break;
						}
					}
				}
			}
		}
	}
	
	private void sendListWeights() throws IOException{
		ArrayList<String> linkInfoString = new ArrayList<String>(numLinks);
		for(nodeInformation regedNode: registeredNodes){ 
			for(LinkInfo li: overlay.get(regedNode.getHostServerPort())){
				//if(linkInfoString )
				linkInfoString.add(li.getFullInfo());
			}
		}
		LinkWeights lw = new LinkWeights(this.numLinks, linkInfoString);
		for(nodeInformation regedNode: registeredNodes){
			//System.out.println("Sending to "+regedNode.getHostRegPort());
			//System.out.println(establishedConnections.get(regedNode.getHostRegPort()));
			establishedConnections.get(regedNode.getHostRegPort()).getSender().sendData(lw.getByte());
		}
	}
	/** Task_Summray_Request -> all complete and print final **/
	//request information from all nodes
	private void requestSummaries() throws IOException {
		for(nodeInformation node: registeredNodes){
			establishedConnections.get(node.getHostRegPort()).getSender().sendData((new TaskSummaryRequest()).getByte());
		}
	}

	//parse all currently obtained summaries and print into table
	private void parseSummaries() {
		long totalSumSent = 0;
		int totalNumSent = 0;
		long totalSumRec = 0;
		int totalNumRec = 0;

		//print header
		System.out.printf("%-20s | %-15s | %-20s | %-20s | %-20s | %-20s |\n",
				"Node Name", "Num Msgs Sent", "Num Msgs Received", 
				"Sum of Sent Msgs","Sum of Received Msgs","Num Msgs Relayed");

		String horizontalLine ="";
		for(int i =0; i<132; ++i){
			horizontalLine+="-";
		}
		System.out.println(horizontalLine);
		for(TaskSummaryResponse tsi: receivedSummaries){
			totalSumSent += tsi.getSumSent();
			totalNumSent += tsi. getNumSent();
			totalSumRec += tsi.getSumRec();
			totalNumRec += tsi.getNumRec();
			System.out.printf("%-20s | %15d | %20d | % 20d | % 20d | %-20d |\n", Utilities.removeDotCS(tsi.getName()), tsi.getNumSent(),
					tsi.getNumRec(), tsi.getSumSent(),tsi.getSumRec(),tsi.getNumRelayed());
		}
		System.out.println(horizontalLine);
		System.out.printf("%-20s | %15d | %20d | % 20d | % 20d | %-20s |\n", "Total ",
				totalNumSent, totalNumRec, totalSumSent, totalSumRec, "");

	}

	/*** COM: start -> Initiate_Task -> Task_Summary_Request -> Task_Summary_Response ***/
	//tells nodes to complete all messaging rounds and inform registry of completion
	//upon completion, will ask nodes for task information and display in table
	private void start() throws IOException {
		taskCompletesReceived = 0;
		for(nodeInformation node: registeredNodes){
			establishedConnections.get(node.getHostRegPort()).getSender().sendData((new TaskInitiate()).getByte());
		}		
	}
	
	/****** UTILITES *****/

	//register connections
	@Override
	public synchronized void registerConnection(Connection connection){
		establishedConnections.put(connection.getName(), connection);

	}

	//deregister connections
	@Override
	public synchronized void deregisterConnection(Connection connection) {
		// TODO Auto-generated method stub

	}



	







}


