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
	private Lock numSumRecLock = new ReentrantLock();
	ArrayList<TaskSummaryResponse> receivedSummaries;
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
	
	
	
	public void setServerThread(TCPServerThread arg){
		this.server = arg;
	}
	
	public TCPServerThread getServerThread(){
		return server;
	}
	
	
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
		System.out.println("Server thread started");
		while(true){
			reg.getCommandlineInput();
		}
		
	}



	@Override
	public synchronized void onEvent(Event event, Socket socket) {
		switch(event.getType()){
		case Protocol.DEREGISTER:
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
				numSumRecLock.lock();
				receivedSummaries.add((TaskSummaryResponse) event);
				System.out.println("Recieved summary response. Now have: "+receivedSummaries.size()+
					". Need: "+registeredNodes.size());
				if(receivedSummaries.size() == registeredNodes.size()){
					parseSummaries();
				}
			}
			finally{
				numSumRecLock.unlock();
			}
			break;
		}
		// TODO Auto-generated method stub

	}

	private void parseSummaries() {
		long totalSumSent = 0;
		int totalNumSent = 0;
		long totalSumRec = 0;
		int totalNumRec = 0;
		
		for(TaskSummaryResponse tsi: receivedSummaries){
			totalSumSent += tsi.getSumSent();
			totalNumSent += tsi. getNumSent();
			totalSumRec += tsi.getSumRec();
			totalNumRec += tsi.getNumRec();
		}
		
		System.out.print("Sum Sent: "+totalSumSent+'\n'
				+ "Sum Rec:  "+totalSumRec+'\n'
				+ "Num Sent: " + totalNumSent +'\n'
				+ "Num Rec:  "+ totalNumRec +'\n');
		// TODO Auto-generated method stub
		
	}



	private void requestSummaries() throws IOException {
		for(nodeInformation node: registeredNodes){
			establishedConnections.get(node.getHostRegPort()).getSender().sendData((new TaskSummaryRequest()).getByte());
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
		
		//System.out.println("Response Message: "+message);
		//System.out.println("From Registration: "+Utilities.createKeyFromSocket(socket));
		TCPSender sender = establishedConnections.get(Utilities.createKeyFromSocket(socket)).getSender();
		sender.sendData(new RegisterResponse(success, message).getByte());
		System.out.println("Sent response");

		
		return success;
	}

	@Override
	public synchronized void registerConnection(Connection connection){
		establishedConnections.put(connection.getName(), connection);
		// TODO Auto-generated method stub
		
	}

	@Override
	public synchronized void deregisterConnection(Connection connection) {
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
				try {
					sendOverlayLinkWeights();
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

	private void start() throws IOException {
		taskCompletesReceived = 0;
		for(nodeInformation node: registeredNodes){
			establishedConnections.get(node.getHostRegPort()).getSender().sendData((new TaskInitiate()).getByte());
		}		
	}



	private void setupOverlay(int numberOfConnections) {
		//TODO CHANGE BACK
		//if(numberOfConnections!=4){
			//System.out.println("SYSTEM DOES NOT SUPPORT NUMBER OF CONNECTIONS != 4");
		//}
		//else
		{
			//map each node to the list of nodes it needs to connect with
			numLinks = 0;
			overlay = new Hashtable<String, ArrayList<LinkInfo>>(registeredNodes.size());
			createOverlay(numberOfConnections);
			sendOverlay();
		}
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
					sender.sendData(overlayMessage.getByte());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	/*
	private ArrayList<GraphNode> makeGraph(){
		ArrayList<GraphNode> graph = new ArrayList<GraphNode>();
		//make every node into a graphNode
		for(nodeInformation node: registeredNodes){
			GraphNode gnode = null;
			for(GraphNode n: graph){
				if(n.getID().equals(node.getHostServerPort())){
					gnode=n;
				}
				else{
					gnode = new GraphNode(node.getHostServerPort());
				}
			}
			for(LinkInfo li: overlay.get(node.getHostServerPort())){
				GraphNode neighborGNode = null;
				for(GraphNode n: graph){
					if(n.getID().equals(node.getHostServerPort())){
						neighborGNode=n;
					}
					else{
						neighborGNode = new GraphNode(li.getHostBPortB());
					}
				}
				if(neighborGNode==null || gnode == null){
					System.out.println("Error in makeGraph ");
					return null;
				}
				gnode.addNeighborWithWeight(neighborGNode, li);
				
			}
			graph.add(gnode);
		}
		return graph;
		
	}*/
	
	private void createOverlay(int numConnections){
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

	private void listWeights() {
		for(nodeInformation ni: registeredNodes){
			for(LinkInfo li: overlay.get(ni.getHostServerPort())){
				System.out.println(li.getFullInfo());
			}
		}

		// TODO Auto-generated method stub
		
	}
	
	private void sendOverlayLinkWeights() throws IOException {
		generateListWeights();
		sendListWeights();
		// TODO Auto-generated method stub
		
	}
	
	private void sendListWeights() throws IOException{
		ArrayList<String> linkInfoString = new ArrayList<String>(numLinks);
		for(nodeInformation regedNode: registeredNodes){ 
			for(LinkInfo li: overlay.get(regedNode.getHostServerPort())){
				//if(linkInfoString )
				linkInfoString.add(li.getFullInfo());
			}
		}
		if(Protocol.DEBUG){
			//System.out.println("link info strings:" );
			for(String s: linkInfoString){
				//System.out.println(s);
			}
			//System.out.println("in send list weights numLinks: "+numLinks);
		}
		LinkWeights lw = new LinkWeights(this.numLinks, linkInfoString);
		for(nodeInformation regedNode: registeredNodes){
			//System.out.println("Sending to "+regedNode.getHostRegPort());
			//System.out.println(establishedConnections.get(regedNode.getHostRegPort()));
			establishedConnections.get(regedNode.getHostRegPort()).getSender().sendData(lw.getByte());
		}
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



	@Override
	public String getHostServerName() {
		// TODO Auto-generated method stub
		try {
			return InetAddress.getLocalHost().getHostName()+":"+this.serverSocketPortNum;
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}


}


