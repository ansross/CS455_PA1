package cs455.overlay.wireformats;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class RegisterResponse implements Event {
	private int type = Protocol.REGISTER_RESPONSE;
	private byte statusCode; //Sucess or Failure
	private String additionalInfo;
	

	@Override
	public int getType() {
		return type;
		// TODO Auto-generated method stub

	}
	
	public String getAdditionalInfo(){
		return additionalInfo;
	}

	public RegisterResponse(byte statusArg, String infoArg){
		statusCode = statusArg;
		additionalInfo = infoArg;
	}
	
	public RegisterResponse(byte[] marshalledBytes) throws IOException{
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
		// TODO Auto-generated method stub

	}

}
