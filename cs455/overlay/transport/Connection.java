package cs455.overlay.transport;

import java.io.IOException;
import java.net.Socket;

import util.Utilities;
import cs455.overlay.node.*;

public class Connection {
	private Node node;
	private String nameFromSocket;
	private String nameFromServerSocket;
	private TCPReceiverThread receiver;
	private TCPSender sender;
	
	public void setNameFromServerSocket(String nameArg){
		nameFromServerSocket= nameArg;
	}
	
	public String getNameFromServerSocket(){
		return nameFromServerSocket;
	}
	
	public Connection(Node node, Socket socket) throws IOException{
		
		this.nameFromSocket = Utilities.createKeyFromSocket(socket);
		System.out.println("Connection name: "+Utilities.createKeyFromSocket(socket));
		this.node = node;
		this.receiver = new TCPReceiverThread(node, socket);
		receiver.start();
		sender = new TCPSender(socket);
		node.registerConnection(this);
		
	}
	
	public synchronized TCPSender getSender(){
		return sender;
	}
	
	public String getName(){
		return nameFromSocket;
	}
	
	public boolean equals(Connection rhs){
		return this.nameFromSocket.equals(rhs.nameFromSocket);
	}
	
	public boolean equals(String name){
		return this.nameFromSocket.equals(name);
	}
}
