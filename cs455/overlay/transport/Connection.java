package cs455.overlay.transport;

import java.io.IOException;
import java.net.Socket;

import util.Utilities;
import cs455.overlay.node.*;

public class Connection {
	private Node node;
	private String name;
	private TCPReceiverThread receiver;
	private TCPSender sender;
	
	public Connection(Node node, Socket socket) throws IOException{
		
		this.name = Utilities.createKeyFromSocket(socket);
		System.out.println("Connection name: "+Utilities.createKeyFromSocket(socket));
		this.node = node;
		this.receiver = new TCPReceiverThread(node, socket);
		receiver.start();
		sender = new TCPSender(socket);
		node.registerConnection(this);
		
	}
	
	public TCPSender getSender(){
		return sender;
	}
	
	public String getName(){
		return name;
	}
}
