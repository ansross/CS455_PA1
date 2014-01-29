package cs455.overlay.wireformats;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class EventFactory {
	private  static final EventFactory eventFactory = new EventFactory();
	
	public EventFactory getInstance(){
		return eventFactory;
	}
	
	private EventFactory(){
	}
	
	public static Event getEvent(byte[] marshalledBytes)throws IOException{
		Event retEvent = null;
		
		ByteArrayInputStream baInputStream =
				new ByteArrayInputStream(marshalledBytes);
		DataInputStream din = 
				new DataInputStream(new BufferedInputStream(baInputStream));
		
		int type = din.readInt();
		switch(type){
		//ctors w/ byte[]
		case Protocol.DEREGISTER:
			int IPAddressLength = din.readInt();
			byte[] IPAddressBytes = new byte[IPAddressLength];
			din.readFully(IPAddressBytes);
			String IPAddress = new String(IPAddressBytes);
			
			int nodePortNum = din.readInt();
			retEvent=new Deregister(IPAddress,nodePortNum);
			break;
		case Protocol.LINK_WEIGHTS:
			break;
		case Protocol.MESSAGE:
			break;
		case Protocol.MESSAGING_NODES_LIST:
			break;
		case Protocol.REGISTER_REQUEST:
			break;
		case Protocol.REGISTER_RESPONSE:
			break;
		case Protocol.TASK_COMPLETE:
			break;
		case Protocol.TASK_INITIATE:
			break;
		case Protocol.TASK_SUMMARY_REQUEST:
			break;
		case Protocol.TASK_SUMMARY_RESPONSE:
			break;
		}
		return retEvent;
	}
}
