package org.das.network.server;

import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public interface RequestProcessor {

	public void handleRequest(SelectionKey key) throws Exception;

	public SocketChannel process(SelectionKey key);
	
}