package cs455.overlay.wireformats;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class TaskSummaryResponse implements Event {
	private int type;
	private String nodeIPAddress;
	private int nodePortNum;
	private int numSent;
	private long sumSent;
	private int numReceived;
	private long sumReceived;
	private int numRelayed;
	
	@Override
	public int getType() {
		// TODO Auto-generated method stub

	}

	@Override
	public byte[] getByte() throws IOException {
		byte[] marshalledBytes=null;
		ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
		DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));

		dout.writeInt(type);
		
		byte[] nodeIPAddBytes = nodeIPAddress.getBytes();
		int elementLength = nodeIPAddBytes.length;
		dout.writeInt(elementLength);
		dout.write(nodeIPAddBytes);
		
		dout.writeInt(nodePortNum);
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
