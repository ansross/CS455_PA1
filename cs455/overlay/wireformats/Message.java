package cs455.overlay.wireformats;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import cs455.overlay.node.nodeInformation;

public class Message implements Event {
	private int type = Protocol.MESSAGE;
	private ArrayList<String> shortestPathIDs;
	private int message;

	@Override
	public int getType() {
		return type;
		// TODO Auto-generated method stub

	}
	
	public Message(ArrayList<String> shortestPArg, int mesArg){
		this.shortestPathIDs = new ArrayList<String>(shortestPArg);
		message=mesArg;
	}
	
	public ArrayList<String> getShortestPathIDs(){
		return shortestPathIDs;
	}
	
	public int getMessage(){
		return message;
	}
	
	public Message(byte[] marshalledBytes) throws IOException{
		ByteArrayInputStream baInStr = 
				new ByteArrayInputStream(marshalledBytes);
		DataInputStream din = 
				new DataInputStream(new BufferedInputStream(baInStr));
		
		//type
		int msgType = din.readInt();
		if(msgType != type){
			System.out.println("ERROR: types do not match. Actual type: "+type+", passed type: "+msgType);
		}
		
		int numPaths = din.readInt(); 
		
		shortestPathIDs = new ArrayList<String>(numPaths);
		for(int i=0; i<numPaths; ++i){
			int IDLength = din.readInt();
			byte [] IDBytes = new byte[IDLength];
			din.readFully(IDBytes);
			shortestPathIDs.add(new String(IDBytes));
		}
		
		message = din.readInt();
	
		baInStr.close();
		din.close();
	}

	@Override
	public byte[] getByte() throws IOException {
		byte[] marshalledBytes=null;
		ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
		DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));
		dout.writeInt(type);
		
		//write number of nodes in shortest path
		dout.writeInt(shortestPathIDs.size());
		//write each node ID, one by one
		for(String name: shortestPathIDs){
			byte[] nameInfoBytes = name.getBytes();
			int nameLength = nameInfoBytes.length;
			dout.writeInt(nameLength);
			dout.write(nameInfoBytes);
		}
				
		dout.writeInt(message);
		
		dout.flush();
		marshalledBytes = baOutputStream.toByteArray();
		
		baOutputStream.close();
		dout.close();
		return marshalledBytes;
	}

}
