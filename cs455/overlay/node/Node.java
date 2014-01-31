package cs455.overlay.node;

import java.net.Socket;

import cs455.overlay.wireformats.Event;

public interface Node {
	public void onEvent(Event event, Socket socket);

}
