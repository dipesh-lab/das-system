package org.das.network.server;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Objects;

import org.apache.log4j.Logger;

public abstract class AbstractRequestProcessor implements RequestProcessor {
	
	private static final Logger LOG = Logger.getLogger(AbstractRequestProcessor.class);
	
	protected Selector selector;
	private int nextOps;
	
	protected AbstractRequestProcessor(Selector sel, int ops) {
		selector = sel;
		nextOps = ops;
		LOG.debug("Abstract Processor " + nextOps + " : " + this.getClass().getName());
	}
	
	@Override
	public void handleRequest(SelectionKey key) throws Exception {
		SocketChannel channel = process(key);
		LOG.debug("Now Change the status " + nextOps);
		if(nextOps != 0 && Objects.nonNull(channel)) {
			LOG.debug("Now Set Ops " + nextOps);
			channel.register(selector, nextOps, key.attachment());
		}
	}
	
	@Override
	public abstract SocketChannel process(SelectionKey key);
	
	protected void closeRequest(SelectionKey key, SocketChannel channel) {
		key.cancel();
		try {
			channel.close();
		} catch (IOException e) {
			LOG.error(e);
		}
	}

}