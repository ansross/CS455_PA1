package util;

import java.net.Socket;

public class Utilities {
	public static String createKeyFromSocket(Socket socket){
		return socket.getInetAddress().getHostName()+":"+socket.getPort();
	}
	
	public static String removeDotCS(String name){
		String[] tokens = name.split("\\.");
		name = tokens[0]+":"+tokens[3].split(":")[1];
		return name;
		
	}
}
