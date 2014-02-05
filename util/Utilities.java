package util;

import java.net.Socket;

public class Utilities {
	public static String createKeyFromSocket(Socket socket){
		return socket.getInetAddress().getHostName()+":"+socket.getPort();
	}
}
