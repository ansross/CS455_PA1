package cs455.overlay.transport;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

import util.ResultSetter;
import cs455.overlay.node.Node;
import cs455.overlay.wireformats.Event;
import cs455.overlay.wireformats.EventFactory;

public class TCPReceiverThread extends Thread{
	//to have a way to get the event from the receiver thread
	//private ResultSetter setter;
	private Socket socket;
	private DataInputStream din;
	private Node myNode;
	
	public void setResultSetter(ResultSetter setter){
		//this.setter = setter;
	}
	
	public TCPReceiverThread(Node node, Socket socket) throws IOException{
		this.socket = socket;
		din = new DataInputStream(socket.getInputStream());
		this.myNode = node;
	}
	
	public void run(){
		int dataLength;
		while(socket != null){
			try{
				System.out.println("listening");
				dataLength = din.readInt();
				System.out.println("read DataLength");
				
				byte[] data = new byte[dataLength];
				System.out.println("1");
				din.readFully(data, 0, dataLength);
				System.out.println("2");
				System.out.println(dataLength);
				System.out.println(new String(data));
				//if(dataLength>1){
					System.out.println("2.5");
					Event event = EventFactory.getEvent(data);
					System.out.println("3");
					myNode.onEvent(event);
					//setter.addResult(event);
				//}
				
			} catch (SocketException se){
				System.out.println("Socket Exception");
				System.out.println(se.getMessage());
				break;
			} catch (IOException ioe){
				System.out.println("IOException.");
				System.out.println(ioe.getMessage());
				break;
			}
		}
		System.out.println("here");
		System.out.println("Socket!=null: ");
		System.out.println(socket!=null);
	}

}
