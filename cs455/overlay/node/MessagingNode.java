package cs455.overlay.node;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Hashtable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;

import util.Utilities;
import cs455.overlay.transport.*;
import cs455.overlay.wireformats.*;
import cs455.overlay.transport.TCPServerThread;

public class MessagingNode implements Node {
	
	private Hashtable<String, Connection> establishedConnections;
	//msgNodes Send: register_requests, deregister requests, message, task_complete, task_summary_response
	//msgNodes recieve: link_weights, message, messaging_nodes_list, register_response, task_initiate, task_summary_request

	private ArrayList<Socket> mySockets;
	private Hashtable<String, String> serverNameToSocketName;
	//initialized when link weights received 
	private Hashtable<String, LinkInfo> serverNametoLinkWeights;
	
	private String IPAddress;
	private int portNum;
	private int numMessagesSent;
	private long sumMessagesSent;
	private int numMessagesRecieved;
	private long sumMessagesRecieved;
	private int numMessagesRelayed;
	private String hostName;
	private int serverSocketPortNum;
	private TCPServerThread server;
	
	public void setServerSocketPortNum(int portArg){
		this. serverSocketPortNum=portArg;
	}
	
	public String getHostServer(){
		return hostName+":"+serverSocketPortNum;
	}
	
	//private TCPSender sender;
	
	public String getIPAddress(){
		return IPAddress;
	}
	
	public int getPortNum(){
		return portNum;
	}
	
	public MessagingNode(){
		serverNameToSocketName = new Hashtable<String, String>();
		mySockets = new ArrayList<Socket>();
		establishedConnections = new Hashtable<String, Connection>();
		numMessagesSent = 0;
		sumMessagesSent =0;
		numMessagesRecieved =0;
		sumMessagesRecieved = 0;
		numMessagesRelayed =0;
		try {
			hostName = InetAddress.getLocalHost().getHostName();
			System.out.println("Host Name: " + hostName);
			new TCPServerThread(this).start();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
		
	}
	
	public void setServerThread(TCPServerThread arg){
		this.server = arg;
	}
	
	public TCPServerThread getServerThread(){
		return server;
	}
	
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
		
		try (
				Socket socket = new Socket(registryHostName, registryPortNum);
		)
		{
			msgNode.attemptRegistration(socket);
			msgNode.getCommandlineInput();
			
		}catch(IOException e){
			System.out.println("IOException Message Node");
			System.out.println(e);
		}
		
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
			System.out.println("Your Command: " + command +" does nothing!!");
			switch(command){
			case "print-shortest-path":
				printShortestPath();
				break;
			case "exit-overlay":
				exitOverlay();
				break;
				default:
					System.out.println("ERROR: Command \""+command+"\" not supported");
			}
		}
		input.close();
		
	}
	
	private void exitOverlay() {
		// TODO Auto-generated method stub
		
	}

	private void printShortestPath() {
		// TODO Auto-generated method stub
		
	}

	private void attemptRegistration(Socket socket){//
			//String registryHostName, int registryPortNum){
		try	
		{
			new Connection(this, socket);
			mySockets.add(socket);
			System.out.println("got Socket");
			System.out.println("port num "+socket.getLocalPort());
			RegisterRequest regReq = new RegisterRequest(InetAddress.getLocalHost().getHostName(), this.serverSocketPortNum, new String(this.hostName+":"+socket.getLocalPort()));
			establishedConnections.get(Utilities.createKeyFromSocket(socket)).getSender().sendData(regReq.getByte());
			//DELETE ME
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
			System.out.println("Recieved link weights");
			saveLinkInfo((LinkWeights) event);
			break;
		case Protocol.MESSAGE:
			try {
				parseMessage((Message) event);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		case Protocol.MESSAGING_NODES_LIST:
			System.out.println("got messaging nodes list");
			System.out.println("My name is: "+hostName+":"+serverSocketPortNum);
			System.out.println("I have "+((MessagingNodesList) event).getNumPeerNodes()+" peer nodes");
			((MessagingNodesList) event).printPeerNodes();
			setUpOverlay((MessagingNodesList)event);
			break;
		case Protocol.REGISTER_REQUEST:
			System.out.println("got response");
			registerPeerNode((RegisterRequest)event, socket);
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
	
	private void saveLinkInfo(LinkWeights event) {
		ArrayList<String> links = event.getLinks();
		serverNametoLinkWeights = new Hashtable<String, LinkInfo>(links.size());
		for(String str: links){
			String delims = ":| ";
			String[] tokens = str.split(delims);
			for(String s: tokens){
				System.out.println("Token: "+s);
			}
			LinkInfo li = new LinkInfo(tokens[0], Integer.parseInt(tokens[1]), tokens[2], Integer.parseInt(tokens[3]), Integer.parseInt(tokens[4]));
			if(Protocol.DEBUG){
				System.out.println("added link: "+li.getHostBPortB());
			}
			serverNametoLinkWeights.put(li.getHostBPortB(), li);
		}
		if(Protocol.DEBUG){
			System.out.println("Link weights: ");
			 Set entrySet = serverNametoLinkWeights.entrySet();
			 // Obtain an Iterator for the entries Set
			 Iterator it = entrySet.iterator();
			 // Iterate through Hashtable entries
			 while(it.hasNext()){
				 System.out.println(it.next());
			}
		}
		// TODO Auto-generated method stub
		
	}

	private void parseMessage(Message msg) throws IOException{
		ArrayList<String> shortestPathIDs = msg.getShortestPathIDs();
		if(shortestPathIDs.get(shortestPathIDs.size()-1).equals(this.getHostServer())){
			keepMessage(msg);
		}
		else{
			relayMessage(msg);
		}
		
	}
	
	private void keepMessage(Message msg){
		numMessagesRecieved++;
		this.sumMessagesRecieved += msg.getMessage();
	}
	
	private void relayMessage(Message msg) throws IOException{
		numMessagesRelayed++;
		ArrayList<String> shortestPathIDs = msg.getShortestPathIDs();
		for(int i=0; i<shortestPathIDs.size(); ++i){
			if(shortestPathIDs.get(i).equals(this.getHostServer())){
				String destinationName = shortestPathIDs.get(i+1);
				String destinationLocalName = serverNameToSocketName.get(destinationName);
				establishedConnections.get(destinationLocalName).getSender().sendData(msg.getByte());
			}
		}
	}
	
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
				System.out.println("PeerHost:PeerPort= "+peerHostName+":"+peerPortNum);
			}
			try{
				 Socket socket = new Socket(peerHostName, peerPortNum);
				//Connection newCon = 
				new Connection(this, socket);
				mySockets.add(socket);
				serverNameToSocketName.put(peerHostName+":"+peerPortNum, Utilities.createKeyFromSocket(socket));
				RegisterRequest regReq = new RegisterRequest(InetAddress.getLocalHost().getHostName(), socket.getLocalPort(), new String(this.hostName+":"+socket.getLocalPort()));
				establishedConnections.get(Utilities.createKeyFromSocket(socket)).getSender().sendData(regReq.getByte());
				System.out.println("Connected");
			}catch(IOException ioe){
				ioe.printStackTrace();
			}
			 
			//connect
			
			
		}
		// TODO Auto-generated method stub
		
	}
	
	public String messageNodeInfo(){
		return hostName + ":"+portNum;
	}

	@Override
	public void registerConnection(Connection connection) {
		establishedConnections.put(connection.getName(), connection);
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deregisterConnection(Connection connection) {
		// TODO Auto-generated method stub
		
	}
	
	

}
