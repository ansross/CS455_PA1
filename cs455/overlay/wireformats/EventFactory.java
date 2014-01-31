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
		baInputStream.close();
		din.close();
		switch(type){
		//ctors w/ byte[]
		case Protocol.DEREGISTER:
			retEvent=new Deregister(marshalledBytes);
			break;
		case Protocol.LINK_WEIGHTS:
			//retEvent = new LinkWeights(marshalledBytes);
			break;
		case Protocol.MESSAGE:
			//retEvent = new Message(marshalledBytes);
			break;
		case Protocol.MESSAGING_NODES_LIST:
			break;
		case Protocol.REGISTER_REQUEST:
			System.out.println("EventFact 5");
			retEvent = new RegisterRequest(marshalledBytes);
			break;
		case Protocol.REGISTER_RESPONSE:
			retEvent = new RegisterResponse(marshalledBytes);
			break;
		case Protocol.TASK_COMPLETE:
			break;
		case Protocol.TASK_INITIATE:
			break;
		case Protocol.TASK_SUMMARY_REQUEST:
			break;
		case Protocol.TASK_SUMMARY_RESPONSE:
			break;
		default:
			System.out.println("Unknown type: "+type);
			break;
				
		}
		return retEvent;
	}
}
