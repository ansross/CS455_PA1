package cs455.overlay.wireformats;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class TaskComplete implements Event {
	private int type;
	private String nodeIPAddress;
	private long nodePortNum;
	
	@Override
	public int getType() {
		// TODO Auto-generated method stub

	}

	@Override
	public byte[] getByte() throws IOException {
		// TODO Auto-generated method stub
		byte[] marshalledBytes=null;
		ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
		DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));
		
		dout.writeInt(type);
		
		byte[] nodeIPAddBytes = nodeIPAddress.getBytes();
		int elementLength = nodeIPAddBytes.length;
		dout.writeInt(elementLength);
		dout.write(nodeIPAddBytes);
		
		dout.writeLong(nodePortNum);
		
		dout.flush();
		marshalledBytes = baOutputStream.toByteArray();
		
		baOutputStream.close();
		dout.close();
		return marshalledBytes;
		
	}

}
