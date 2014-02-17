package cs455.overlay.transport;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

import util.ResultSetter;
import util.Utilities;
import cs455.overlay.node.Node;
import cs455.overlay.wireformats.Event;
import cs455.overlay.wireformats.EventFactory;

public class TCPReceiverThread extends Thread{
	private Socket socket;
	private DataInputStream din;
	private Node myNode;
	
	public TCPReceiverThread(Node node, Socket socket) throws IOException{
		this.socket = socket;
		din = new DataInputStream( new BufferedInputStream(socket.getInputStream()));
		this.myNode = node;
	}
	
	public void run(){
		int dataLength;
		while(socket != null){
			try{
				dataLength = din.readInt();
				byte[] data = new byte[dataLength];
				din.readFully(data, 0, dataLength);
				if(dataLength>0){
					Event event = EventFactory.getEvent(data);
					if(event != null){
						myNode.onEvent(event, socket);
					}
					else{
						System.out.println("MESSAGE DROPPED from : " +myNode.getHostServerName()+" to " + Utilities.createKeyFromSocket(socket));
					}
				}
			}catch(EOFException eof){
				System.out.println("EOF Exception");
				System.out.println(eof.getMessage());
				eof.printStackTrace();
				break;
			}
			catch (SocketException se){
				System.out.println("Reciever Socket Exception");
				System.out.println(se.getMessage());
				se.printStackTrace();
				break;
			} catch (IOException ioe){
				System.out.println("IOException.");
				System.out.println("Message: "+ ioe.getMessage());
				System.out.println(ioe.getCause());
				ioe.printStackTrace();
				
				break;
			}
		}
	}

}
