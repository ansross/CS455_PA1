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
		
		//System.out.println("marshalled bytes size: "+marshalledBytes.length);
		
		ByteArrayInputStream baInputStream =
				new ByteArrayInputStream(marshalledBytes);
		DataInputStream din = 
				new DataInputStream(new BufferedInputStream(baInputStream));
		
		
		
		int type=-1;
		try {
			type = din.readInt();
		} catch (IOException e) {
			System.out.println(e.getCause());
			System.out.println(e.getMessage());
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		baInputStream.close();
		din.close();
		switch(type){
		//ctors w/ byte[]
		case Protocol.DEREGISTER:
			retEvent=new Deregister(marshalledBytes);
			break;
		case Protocol.LINK_WEIGHTS:
			retEvent = new LinkWeights(marshalledBytes);
			break;
		case Protocol.MESSAGE:
			retEvent = new Message(marshalledBytes);
			break;
		case Protocol.MESSAGING_NODES_LIST:
			retEvent = new MessagingNodesList(marshalledBytes);
			break;
		case Protocol.REGISTER_REQUEST:
			retEvent = new RegisterRequest(marshalledBytes);
			break;
		case Protocol.REGISTER_RESPONSE:
			retEvent = new RegisterResponse(marshalledBytes);
			break;
		case Protocol.TASK_COMPLETE:
			retEvent= new TaskComplete(marshalledBytes);
			break;
		case Protocol.TASK_INITIATE:
			retEvent = new TaskInitiate(marshalledBytes);
			break;
		case Protocol.TASK_SUMMARY_REQUEST:
			retEvent = new TaskSummaryRequest(marshalledBytes);
			break;
		case Protocol.TASK_SUMMARY_RESPONSE:
			retEvent = new TaskSummaryResponse(marshalledBytes);
			break;
		default:
			System.out.println("Unknown type: "+type);
			break;
				
		}
		return retEvent;
	}
}
