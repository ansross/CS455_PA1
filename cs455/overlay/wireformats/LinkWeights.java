package cs455.overlay.wireformats;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class LinkWeights implements Event {
	private int type=Protocol.LINK_WEIGHTS;
	private long numOfLinks;
	//string in form hostA:portA hostB:portB weight
	private ArrayList<String> links;
	
	public ArrayList<String> getLinks(){
		return links;
	}
	
	public LinkWeights(long numLinksArg, ArrayList<String> linkArg){
		numOfLinks = numLinksArg;
		links = new ArrayList<String>();
		for(String s: linkArg){
			links.add(s);
		}
		if(Protocol.DEBUG){
			System.out.println("link info strings:" );
			for(String s: links){
				System.out.println(s);
			}
		}
		
		//TODO!!!
//		for(Link l: linkArg){
//			links.add(l.clone());
//			
//		}
	}

	public LinkWeights(byte[] marshalledBytes) throws IOException {
		links = new ArrayList<String>();
		ByteArrayInputStream baInStr = 
				new ByteArrayInputStream(marshalledBytes);
		DataInputStream din = 
				new DataInputStream(new BufferedInputStream(baInStr));
		
		//type
		int msgType = din.readInt();
		if(msgType != type){
			System.out.println("ERROR: types do not match. Actual type: "+type+", passed type: "+msgType);
		}
		
		this.numOfLinks = din.readLong();
		//System.out.println("Num of links: "+numOfLinks);
		
		for(int i=0; i<numOfLinks; ++i){
			int linkInfoLength = din.readInt();
			byte [] linkInfoBytes = new byte[linkInfoLength];
			din.readFully(linkInfoBytes);
			this.links.add(new String(linkInfoBytes));
			
		}
			
		baInStr.close();
		din.close();
		
		if(Protocol.DEBUG){
			System.out.println("link info strings:" );
			for(String s: links){
				System.out.println(s);
			}
		}
	}

	@Override
	public int getType() {
		return type;
		// TODO Auto-generated method stub

	}

	@Override
	public byte[] getByte() throws IOException {
		byte[] marshalledBytes=null;
		ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
		DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));

		dout.writeInt(type);
		dout.writeLong(numOfLinks);
		
		if(Protocol.DEBUG){
			System.out.println("link info strings in marshalling:" );
			for(String s: links){
				System.out.println(s);
			}
		}
		
		for(int i=0; i<numOfLinks; ++i){
			//for each link write hostnameA:portNumA hostnameB:portNumB weight
			byte[] linkInfoBytes = links.get(i).getBytes();//.
					//getFirstNode().messageNodeInfo().getBytes();
			int elementLength = linkInfoBytes.length;
			dout.writeInt(elementLength);
			dout.write(linkInfoBytes);
			/*
			byte[] nodeBInfoBytes = links.get(i).getBytes();//.getSecondNode().
					//messageNodeInfo().getBytes();
			elementLength = nodeBInfoBytes.length;
			dout.writeInt(elementLength);
			dout.write(nodeBInfoBytes);*/
					
		}
		
		dout.flush();
		marshalledBytes = baOutputStream.toByteArray();
		
		baOutputStream.close();
		dout.close();
		return marshalledBytes;
		// TODO Auto-generated method stub

	}

}
