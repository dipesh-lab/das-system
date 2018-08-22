package org.das.network.server;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

import org.apache.log4j.Logger;

public abstract class AbstractRequestProcessor implements RequestProcessor {
	
	private static final Logger LOG = Logger.getLogger(AbstractRequestProcessor.class);
	
	protected Selector selector;
	private int nextOps;
	
	protected AbstractRequestProcessor(Selector sel, int ops) {
		selector = sel;
		nextOps = ops;
	}
	
	@Override
	public void handleRequest(SelectionKey key) throws Exception {
		SocketChannel channel = process(key);
		if(null != channel) {
			channel.register(key.selector(), nextOps, key.attachment());
		}
	}
	
	@Override
	public abstract SocketChannel process(SelectionKey key);
	
	protected void closeRequest(SelectionKey key, SocketChannel channel) {		
		key.cancel();
		try {
			channel.socket().close();
			channel.close();
		} catch (IOException e) {
			LOG.error(e);
		}
	}

}