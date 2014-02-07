package cs455.overlay.node;

import java.net.Socket;

import cs455.overlay.transport.Connection;
import cs455.overlay.wireformats.Event;

public interface Node {
	public void registerConnection(Connection conection);
	public void deregisterConnection(Connection connection);
	public void onEvent(Event event, Socket socket);
	public void getCommandlineInput();
	public void setServerSocketPortNum(int localPort);
}
