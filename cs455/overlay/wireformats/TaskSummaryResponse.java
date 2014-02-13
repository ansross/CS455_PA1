package cs455.overlay.wireformats;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class TaskSummaryResponse implements Event {
	private int type=Protocol.TASK_SUMMARY_RESPONSE;
	//private String nodeIPAddress;
	//private int nodePortNum;
	private String nodeName;
	private int numSent;
	private long sumSent;
	private int numReceived;
	private long sumReceived;
	private int numRelayed;
	
	public TaskSummaryResponse(String nameArg, int numSentArg, long sumSentArg,
			int numRecArg, long sumRecArg, int numRelayedArg){
		this.nodeName=nameArg;
		this.numSent = numSentArg;
		this.sumSent = sumSentArg;
		this.numReceived = numRecArg;
		this.sumReceived = sumRecArg;
		this.numRelayed = numRelayedArg;
	}
	
	@Override
	public int getType() {
		return type;
		// TODO Auto-generated method stub

	}
	
	public int getNumSent(){
		return numSent;
	}
	
	public int getNumRec(){
		return numReceived;
	}
	
	public long getSumSent(){
		return sumSent;
	}
	
	public long getSumRec(){
		return sumReceived;
	}
	
	
	public TaskSummaryResponse(byte[] marshalledBytes) throws IOException{
		ByteArrayInputStream baInStr = 
				new ByteArrayInputStream(marshalledBytes);
		DataInputStream din = 
				new DataInputStream(new BufferedInputStream(baInStr));
		
		//type
		int msgType = din.readInt();
		if(msgType != type){
			System.out.println("ERROR: types do not match. Actual type: "+type+", passed type: "+msgType);
		}
		
		int nameLength = din.readInt();
		byte [] nameBytes = new byte[nameLength];
		din.readFully(nameBytes);
		this.nodeName = new String(nameBytes);
		
		//this.nodePortNum = din.readInt();
		this.numSent=din.readInt();
		this.sumSent=din.readLong();
		this.numReceived = din.readInt();
		this.sumReceived=din.readLong();
		this.numRelayed=din.readInt();
	
		baInStr.close();
		din.close();
	}

	@Override
	public byte[] getByte() throws IOException {
		byte[] marshalledBytes=null;
		ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
		DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));

		dout.writeInt(type);
		
		byte[] nodeNameBytes = nodeName.getBytes();
		int elementLength = nodeNameBytes.length;
		dout.writeInt(elementLength);
		dout.write(nodeNameBytes);
		
		//dout.writeInt(nodePortNum);
		dout.writeInt(numSent);
		dout.writeLong(sumSent);
		dout.writeInt(numReceived);
		dout.writeLong(sumReceived);
		dout.writeInt(numRelayed);
		
		dout.flush();
		marshalledBytes = baOutputStream.toByteArray();
		
		baOutputStream.close();
		dout.close();
		return marshalledBytes;
		// TODO Auto-generated method stub

	}

}
