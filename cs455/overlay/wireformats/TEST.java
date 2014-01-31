package cs455.overlay.wireformats;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class TEST {
	public static void main(String [] args) throws IOException{
		
		byte[] marshalledBytes = null;
		
		int first = 11023;
		int two = 21230;
		String testSt = "hello, friend";
		ByteArrayOutputStream baOutput = new ByteArrayOutputStream();
		DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutput));
		
		dout.writeInt(first);
		dout.writeInt(two);
		
		byte[] stringBytes = testSt.getBytes();
		int elemLen = stringBytes.length;
		dout.writeInt(elemLen);
		dout.write(stringBytes);
		
		dout.flush();
		marshalledBytes = baOutput.toByteArray();
		baOutput.close();
		dout.close();
		
		ByteArrayInputStream baIn = new ByteArrayInputStream(marshalledBytes);
		DataInputStream din1 = new DataInputStream(new BufferedInputStream(baIn));
		
		int firstRead = din1.readInt();
		System.out.println("firstRead: " + firstRead);
		
		baIn.close();
		din1.close();
		
		ByteArrayInputStream baIn2 = new ByteArrayInputStream(marshalledBytes);
		DataInputStream din2 = new DataInputStream(new BufferedInputStream(baIn2));
		//int stringLen = din2.readInt();
		int secondRead = din2.readInt();
		System.out.println("secondRead: "+secondRead);
		
		/*
		byte [] stringByptes = new byte[stringLen];
		din2.readFully(stringByptes);
		String stringRead = new String(stringByptes);
		System.out.println("String "+stringRead);
		*/
		baIn2.close();
		din2.close();

	}
}
