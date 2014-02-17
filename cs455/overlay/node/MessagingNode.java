package cs455.overlay.node;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import util.Utilities;
import cs455.overlay.dijkstra.Dijkstra;
import cs455.overlay.dijkstra.GraphNode;
import cs455.overlay.transport.*;
import cs455.overlay.wireformats.*;
import cs455.overlay.transport.TCPServerThread;

public class MessagingNode implements Node {
	
	private Hashtable<String, Connection> establishedConnections;
	//msgNodes Send: register_requests, deregister requests, message, task_complete, task_summary_response
	//msgNodes recieve: link_weights, message, messaging_nodes_list, register_response, task_initiate, task_summary_request

	private ArrayList<Socket> mySockets;
	private Hashtable<String, String> serverNameToSocketName;
	private ArrayList<String> otherNodes;
	//initialized when link weights received 
	private Hashtable<String, ArrayList<LinkInfo>> serverNametoLinkWeights;
	
	//store shortest path to each node in the overlay
	private Hashtable<String, ArrayList<String>> shortestPaths;
	
	private String IPAddress;
	private int portNum;
	//private Lock numSentLock = new ReentrantLock();
	//private AtomicInteger numMessagesSent;
	private int numMessagesSent; 
	private long sumMessagesSent;
	//private Lock numRecLock = new ReentrantLock();
	//private AtomicInteger 
	private int numMessagesRecieved;
	private long sumMessagesRecieved;
	private int numMessagesRelayed;
	private String hostName;
	private int serverSocketPortNum;
	private TCPServerThread server;
	private String registryPortServerName;
	
	private Lock sendMsgLock = new ReentrantLock();
	
	//Lock numRoundsLock = new ReentrantLock();
	private AtomicInteger numRoundsRunning;
	
	
	/******************** CTOR ****************************/
	
	public MessagingNode(){
		otherNodes = new ArrayList<String>();
		shortestPaths = new Hashtable<String, ArrayList<String>>(); 
		serverNameToSocketName = new Hashtable<String, String>();
		mySockets = new ArrayList<Socket>();
		establishedConnections = new Hashtable<String, Connection>();
		numMessagesSent = 0;//new AtomicInteger(0);
		sumMessagesSent =0;
		
		numMessagesRecieved = 0;//new AtomicInteger(0);
		sumMessagesRecieved = 0;
		numMessagesRelayed =0;
		try {
			hostName = addDotCS(InetAddress.getLocalHost().getHostName());
			//System.out.println("Host Name: " + hostName);
			new TCPServerThread(this).start();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
		
	}
	
	
	/******************* GETTERS AND SETTERS **************/
	
	
	public ArrayList<String> getOtherNodes(){
		return otherNodes;
	}
	
	public void setServerSocketPortNum(int portArg){
		this.serverSocketPortNum=portArg;
	}
	
	public String getHostServerName(){
		return hostName+":"+serverSocketPortNum;
	}
	
	public ArrayList<String> getNeighbors(){
		ArrayList<String> neighbors = new ArrayList<String>();
		for(Socket socket: mySockets){
			neighbors.add(establishedConnections.get(Utilities.createKeyFromSocket(socket)).getNameFromServerSocket());
		}
		return neighbors;
	}
	//private TCPSender sender;
	
	public String getIPAddress(){
		return IPAddress;
	}
	
	public int getPortNum(){
		return portNum;
	}
	
	
	public void setServerThread(TCPServerThread arg){
		this.server = arg;
	}
	
	public TCPServerThread getServerThread(){
		return server;
	}
	
	
	/********************* MAIN ****************************/
	public static void main(String[] args) throws IOException{
		if(args.length != 2){
			System.err.println("Usage: "
					+ "java cs455.overlay.node.MessagingNode "
					+ "<registry-host> <registry-port>" );
			System.exit(1);
		}
		
		MessagingNode msgNode = new MessagingNode();
		//msgNode.setServerThread(new TCPServerThread(msgNode));
		//msgNode.getServerThread().start();
		
		String registryHostName = args[0];
		int registryPortNum = Integer.parseInt(args[1]);

		msgNode.attemptRegistration(registryHostName, registryPortNum);
		msgNode.getCommandlineInput();
		
		while(true){
			msgNode.getCommandlineInput();
		}

//msgNode.attemptRegistration(registryHostName, registryPortNum);
	}
	
	public void getCommandlineInput(){
		Scanner input = new Scanner(System.in);
		boolean exit = false;
		while(!exit){
			
			String command = input.next();

			switch(command){
			case "print-shortest-path":
				printShortestPath();
				break;
			case "test-send":
				/*
				try {
					//sendMessage();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}*/
			case "test-print-things":
				printStats();
				break;
				
			case "exit-overlay":
				System.out.println("Your Command: " + command +" does nothing!!");
				exitOverlay();
				break;
				default:
					System.out.println("ERROR: Command \""+command+"\" not supported");
			}
		}
		input.close();
		
	}
	
	@Override
	public synchronized void onEvent(Event event, Socket socket) {
		switch(event.getType()){
		case Protocol.REGISTER_RESPONSE:
			parseRegResponse((RegisterResponse) event);
			break;
		case Protocol.LINK_WEIGHTS:
			System.out.println("Recieved link weights");
			saveLinkInfo((LinkWeights) event);
			break;
		case Protocol.MESSAGE:
			//System.out.println("Got message");
			//System.out.println("Got message: "+((Message) event).getMessage());
			try {
				parseMessage((Message) event);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		case Protocol.MESSAGING_NODES_LIST:
			//System.out.println("got messaging nodes list");
			System.out.println("My name is: "+hostName+":"+serverSocketPortNum);
			System.out.println("I have "+((MessagingNodesList) event).getNumPeerNodes()+" peer nodes");
			((MessagingNodesList) event).printPeerNodes();
			setUpOverlay((MessagingNodesList)event);
			break;
			//recieved "register request" from peer nodes connecting to it with their server socket number
			//for identificaiton purposes
		case Protocol.REGISTER_REQUEST:
			//System.out.println("got response");
			registerPeerNode((RegisterRequest)event, socket);
			break;
		case Protocol.TASK_INITIATE:
			try {
				System.out.println("Starting task");
				startTask();
				//sendCompletionNotification();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			break;
		case Protocol.TASK_SUMMARY_REQUEST:
			//System.out.println("Recieved summary request");
			try {
				respondWithSummary();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		case Protocol.TASK_SUMMARY_RESPONSE:
			break;
		}
		// TODO Auto-generated method stub

	}
	



	/** Registration_Request***/
	private void attemptRegistration(String registryHostName, int registryPortNum){//
		this.registryPortServerName = registryHostName+":"+registryPortNum;
		try	
		{	Socket socket = new Socket(registryHostName, registryPortNum);
			Connection regConnection = new Connection(this, socket);
			mySockets.add(socket);
			//System.out.println("got Socket");
			//System.out.println("port num "+socket.getLocalPort());
			RegisterRequest regReq = new RegisterRequest(InetAddress.getLocalHost().getHostName(), this.serverSocketPortNum, new String(this.hostName+":"+socket.getLocalPort()));
			establishedConnections.get(Utilities.createKeyFromSocket(socket)).getSender().sendData(regReq.getByte());
			//System.out.println("Request Sent");
			
		}catch(IOException e){
			System.out.println("IOExecption Message Node");
					System.out.println(e);
		}
	}
	
	
	/********************** UTIL **************************/
	private void printStats() {
		System.out.println("numSent: "+this.numMessagesSent+'\n'
				+ "sumSent: "+this.sumMessagesSent +'\n'
				+ "numRec: "+this.numMessagesRecieved+'\n'
				+ "sumRec: " +this.sumMessagesRecieved +'\n'
				+ "numRel: " +this.numMessagesRelayed);
		// TODO Auto-generated method stub
		
	}

	//to alter between the host name returning with .cs.colostate.edu and without that
	private String addDotCS(String host){
		//String [] tokens = word.split(":");
		return host+".cs.colostate.edu";
	}
	


	/********************* COMMAND LINE FUNCTIONS ********/
	private void printShortestPath() {
		
		Enumeration<String> enumKey = shortestPaths.keys();
		while(enumKey.hasMoreElements()){
			String targetName = enumKey.nextElement();
			
			ArrayList<String> path = shortestPaths.get(targetName);
			if(Protocol.DEBUG){
				//System.out.println("target name: "+targetName);
				//System.out.println("path size: "+path.size());
			}
			if(path.size()>1){
				//System.out.print(this.getHostServer());
			}


			for(int i=0; i<path.size(); ++i){
				System.out.print(path.get(i));
				int weight = -1;
				//if not last element
				if(i+1 != path.size()){
					
					for(LinkInfo li: serverNametoLinkWeights.get(path.get(i))){
						if(li.getHostAPortA().equals(path.get(i)) && li.getHostBPortB().equals(path.get(i+1))){
							//System.out.println("ERROR PRINT SHORTEST EVERYTHING IS BACKWARD");
							weight = li.getWeight();
							break;
						}
					}
					System.out.print("--"+weight+"--");
				}
			}
			System.out.print('\n');
		}
		System.out.println("Shortest paths size in print: "+shortestPaths.size());
		
	}
	
	private void exitOverlay() {
		// TODO Auto-generated method stub
		
	}
	/********************* MESSAGE RESPONSES ************/
	
	/*** Register_Response ***/
	private void parseRegResponse(RegisterResponse event) {
		String status = "";
		if(event.isSuccess()){
			status = "succeeded";
		}
		else{
			status = "failed";
		}
		System.out.println("Registration "+status+". "+event.getAdditionalInfo());
		
	}
	
	/*** Link_Weights ***/
	
	private void saveLinkInfo(LinkWeights event) {
		ArrayList<String> links = event.getLinks();
		serverNametoLinkWeights = new Hashtable<String, ArrayList<LinkInfo>>(links.size());
		for(String str: links){
			String delims = ":| ";
			String[] tokens = str.split(delims);
			for(String s: tokens){
			//	System.out.println("Token: "+s);
			}
			LinkInfo li = new LinkInfo(tokens[0], Integer.parseInt(tokens[1]), tokens[2], Integer.parseInt(tokens[3]), Integer.parseInt(tokens[4]));
			if(Protocol.DEBUG){
				//System.out.println("added link: "+li.getHostBPortB());
			}
			if(serverNametoLinkWeights.containsKey(li.getHostAPortA())){
				serverNametoLinkWeights.get(li.getHostAPortA()).add(li);
			}
			else{
				if(!otherNodes.contains(li.getHostAPortA()) && !(li.getHostAPortA().equals(this.getHostServerName()))){
					otherNodes.add(li.getHostAPortA());
				}
				serverNametoLinkWeights.put(li.getHostAPortA(), new ArrayList<LinkInfo>());
				serverNametoLinkWeights.get(li.getHostAPortA()).add(li);
			}
		}
		/*if(Protocol.DEBUG){
			System.out.println("Link weights: ");
			 Set<String> keySet = serverNametoLinkWeights.keySet();
			 // Obtain an Iterator for the entries Set
			 Iterator<String> it = keySet.iterator();
			 // Iterate through Hashtable entries
			 while(it.hasNext()){
				 String name = it.next();
				 
				 for(LinkInfo li: serverNametoLinkWeights.get(name)){
					 System.out.println(name+": "+li.getWeight());
				 }
			}
		}*/
		calculateShortestPaths();
		if(Protocol.DEBUG){
			System.out.println("shortest paths calculated");
		}
		// TODO Auto-generated method stub
		
	}

	
	private void calculateShortestPaths(){
		ArrayList<GraphNode> overlayGraph = makeGraph();
		//Enumeration<String> enumKey = serverNametoLinkWeights.keys();
		for(String name: otherNodes){
		//while(enumKey.hasMoreElements()){
			//String name = enumKey.nextElement();
			ArrayList<String> shortestPath = Dijkstra.getShortestPath(overlayGraph, this.getHostServerName(), name);
			if(Protocol.DEBUG){
				//System.out.println("shortest path size: "+shortestPath.size());
				//System.out.println("name from graph: " + name);
			}
			shortestPaths.put(name, shortestPath);
			
		}
	}
	
	private ArrayList<GraphNode> makeGraph(){
		ArrayList<GraphNode> overlayGraph = new ArrayList<GraphNode>();
		//make a node for each node in overlay
		Enumeration<String> enumKey = serverNametoLinkWeights.keys();
		while(enumKey.hasMoreElements()){
			String name = enumKey.nextElement();
			ArrayList<String> neighbors = new ArrayList<String>();
			Hashtable<String, LinkInfo> neighborWeights = new Hashtable<String, LinkInfo>(); 
			getNeighborWeights(name, neighborWeights, neighbors);
			for(String n: neighbors){
				//System.out.println("weight: "+neighborWeights.get(n).getWeight());
			}
			overlayGraph.add(new GraphNode(name,neighborWeights , neighbors));
		}
		return overlayGraph;
		
	}

	private void getNeighborWeights(String name, Hashtable<String, LinkInfo> neighborWeights,
			ArrayList<String> neighbors) {
		ArrayList<LinkInfo> nodeNeighborInfo = serverNametoLinkWeights.get(name);
		for(LinkInfo li: nodeNeighborInfo){
			neighborWeights.put(li.getHostBPortB(), li);
			neighbors.add(li.getHostBPortB());
		}
	}
	
	/***Task_Summary_Request -> Task_Summary_Response***/
	private void respondWithSummary() throws IOException{
		//create 
		TaskSummaryResponse tsi = new TaskSummaryResponse(getHostServerName(),
				this.numMessagesSent, this.sumMessagesSent, 
				this.numMessagesRecieved, this.sumMessagesRecieved,
				this.numMessagesRelayed);
		//send
		establishedConnections.get(registryPortServerName).getSender().sendData(tsi.getByte());	
		//System.out.println("Send summary response");
	}

	/***Task_Initiate -> Task_Complete ***/
	
	private void startTask() throws IOException, InterruptedException{
		RoundThread rt = new RoundThread(this);
		rt.start();
	}
	
	public void decrementRoundRun(){;
		numRoundsRunning.addAndGet(-1);
	}
	
	//sendMessage from this node to targetNode through shortest path 
	public void sendMessage(String targetNode) throws IOException{
		Random rand = new Random();
		int message = rand.nextInt();
		numMessagesSent++;
		Message msg = new Message(shortestPaths.get(targetNode), message);
		//the first node in the shortest path is this current node
		if(Protocol.DEBUG){
			if(shortestPaths.get(targetNode).size()>1){
				//System.out.println("sending to "+shortestPaths.get(targetNode).get(1));
			}else{
				System.out.println("ERROR: ");
				System.out.println("target "+targetNode);
			}
		}
		TCPSender sender = establishedConnections.get(shortestPaths.get(targetNode).get(1)).getSender();
		sender.sendData(msg.getByte());
		this.sumMessagesSent+=message;
		if(Protocol.DEBUG){
			//System.out.println("sent message: "+message);
		}
		System.out.println(Thread.currentThread().getName()+" released lock");
	}
	//when receive message
	private void parseMessage(Message msg) throws IOException{
		ArrayList<String> shortestPathIDs = msg.getShortestPathIDs();
		if(shortestPathIDs.get(shortestPathIDs.size()-1).equals(this.getHostServerName())){
			if(Protocol.DEBUG){
			//	System.out.println("kept message with msg: " + msg.getMessage());
			}
			keepMessage(msg);
		}
		else{
			relayMessage(msg);
		}
	}
	
	private void keepMessage(Message msg){
		//System.out.println("Kept message from " + msg.getShortestPathIDs().get(0));
		System.out.println("got message 3.1");
		numMessagesRecieved++;//.dddAndGet(1);
		System.out.println("got message 3.2");
		this.sumMessagesRecieved += msg.getMessage();
	}
	
	private void relayMessage(Message msg) throws IOException{
		//System.out.println("got message 4.1");
		numMessagesRelayed++;
		//System.out.println("got message 4.2");
		ArrayList<String> shortestPathIDs = msg.getShortestPathIDs();
		int i;
		for(i=0; i<shortestPathIDs.size()-1; ++i){
			//System.out.println("got message 4.3");
			//System.out.println(shortestPathIDs.get(i)+" equal? " +this.getHostServerName());
			if(shortestPathIDs.get(i).equals(this.getHostServerName())){
				String destinationName = shortestPathIDs.get(i+1);
				String destinationLocalName = serverNameToSocketName.get(destinationName);
				establishedConnections.get(destinationLocalName).getSender().sendData(msg.getByte());
				break;
			}
		}
		if(i==shortestPathIDs.size()-1)
			System.out.println("ERROR: OH MY GOODNESS I'M NOT ON THE LIST!!!!!");
		
	}
	

	public void incrementNumMessagesSent() {
		numMessagesSent++;
	}
	
	public void sendCompletionNotification() throws IOException {
		establishedConnections.get(registryPortServerName).getSender().sendData((new TaskComplete(this.getHostServerName()).getByte()));
		
	}
	

	/***** REGISTER_RESQUEST (from peer) *****/
	
	private void registerPeerNode(RegisterRequest event, Socket socket){
		serverNameToSocketName.put(event.getID(), Utilities.createKeyFromSocket(socket));
	}
	
	private void setUpOverlay(MessagingNodesList event) {
		ArrayList<String> peerNames = event.getNodeNames();
		for(int peer=0; peer<event.getNumPeerNodes(); peer++){
			//get port number and host name
			String delims = ":";
			String[] tokens = peerNames.get(peer).split(delims);
			String peerHostName = tokens[0];
			int peerPortNum = Integer.parseInt(tokens[1]);
			if(Protocol.DEBUG){
				//System.out.println("PeerHost:PeerPort= "+peerHostName+":"+peerPortNum);
			}
			try{
				 Socket socket = new Socket(peerHostName, peerPortNum);
				//Connection newCon = 
				new Connection(this, socket);
				mySockets.add(socket);
				serverNameToSocketName.put(peerHostName+":"+peerPortNum, Utilities.createKeyFromSocket(socket));
				RegisterRequest regReq = new RegisterRequest(InetAddress.getLocalHost().getHostName(), socket.getLocalPort(), new String(this.hostName+":"+socket.getLocalPort()));
				establishedConnections.get(Utilities.createKeyFromSocket(socket)).getSender().sendData(regReq.getByte());
			}catch(IOException ioe){
				ioe.printStackTrace();
			}			
		}
	}
	
	public String messageNodeInfo(){
		return hostName + ":"+portNum;
	}

	@Override
	public synchronized void registerConnection(Connection connection) {
		establishedConnections.put(connection.getName(), connection);
		// TODO Auto-generated method stub
		
	}

	@Override
	public synchronized void deregisterConnection(Connection connection) {
		// TODO Auto-generated method stub
		
	}

	

}
