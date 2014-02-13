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
	
	public Message(byte[] marshalledBytes) {//TODO RETURN TO THROWING throws IOException{
		ByteArrayInputStream baInStr = 
				new ByteArrayInputStream(marshalledBytes);
		DataInputStream din = 
				new DataInputStream(new BufferedInputStream(baInStr));
		
		//type
		int msgType=-999;
		try {
			msgType = din.readInt();
		} catch (IOException e) {
			System.out.println("failed at msgType");
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(msgType != type){
			System.out.println("ERROR: types do not match. Actual type: "+type+", passed type: "+msgType);
		}
		
		int numPaths=-99;
		try {
			numPaths = din.readInt();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("failed at numPaths");
			e.printStackTrace();
		} 
		
		shortestPathIDs = new ArrayList<String>(numPaths);
		for(int i=0; i<numPaths; ++i){
			int IDLength=0;
			try {
				IDLength = din.readInt();
			} catch (IOException e) {
				System.out.println("failed at readInt");
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			byte [] IDBytes = new byte[IDLength];
			try {
				din.readFully(IDBytes);
			} catch (IOException e) {
				System.out.println("failed at readfully ");
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			shortestPathIDs.add(new String(IDBytes));
		}
		
		try {
			message = din.readInt();
		} catch (IOException e) {
			System.out.println("failed at message");
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		try {
			baInStr.close();
			din.close();
		} catch (IOException e) {
			System.out.println("failed at end");
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

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
