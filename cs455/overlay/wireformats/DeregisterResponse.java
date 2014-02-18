package cs455.overlay.wireformats;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class DeregisterResponse implements Event{
	private int type = Protocol.DEREGISTER_RESPONSE;
	private byte statusCode; ////success==1 is successful, ==0 is unsuccessful
	private String additionalInfo;
	@Override
	public int getType() {
		// TODO Auto-generated method stub
		return type;
	}
	
	public String getAdditionalInfo(){
		return additionalInfo;
	}
	
	public boolean isSuccess(){
		return statusCode==1;
	}

	public DeregisterResponse(byte statusArg, String infoArg){
		statusCode = statusArg;
		additionalInfo = infoArg;
	}
	
	public DeregisterResponse(byte[] marshalledBytes) throws IOException{
		ByteArrayInputStream baInStr = 
				new ByteArrayInputStream(marshalledBytes);
		DataInputStream din = 
				new DataInputStream(new BufferedInputStream(baInStr));
		
		//type
		int msgType = din.readInt();
		if(msgType != type){
			System.out.println("ERROR: types do not match. Actual type: "+type+", passed type: "+msgType);
		}
		
		statusCode = din.readByte();
		
			int addInfoLength = din.readInt();
		byte [] addInfoBytes = new byte[addInfoLength];
		din.readFully(addInfoBytes);
		this.additionalInfo = new String(addInfoBytes);
	
		baInStr.close();
		din.close();
	}

	
	@Override
	public byte[] getByte() throws IOException {
		byte[] marshalledBytes=null;
		ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
		DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));
		dout.writeInt(type);
		dout.writeByte(statusCode);
		
		byte[] addInfoBytes = additionalInfo.getBytes();
		int elementLength = addInfoBytes.length;
		dout.writeInt(elementLength);
		dout.write(addInfoBytes);
		
		dout.flush();
		marshalledBytes = baOutputStream.toByteArray();
		
		baOutputStream.close();
		dout.close();
		return marshalledBytes;
	}
	

}
