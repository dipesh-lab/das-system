package org.das.network.server;

import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;

public interface RequestProcessor {

	public void handleRequest(SelectionKey key) throws Exception;

	public SelectableChannel process(SelectionKey key);
	
}