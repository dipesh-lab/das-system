package org.das.network.listener.impl;

import org.apache.log4j.Logger;

public class NetworkMessageProcessor {

	private static final Logger LOG = Logger.getLogger(NetworkMessageProcessor.class);
	
	private final String data;
	
	public NetworkMessageProcessor(final String message) {
		data = message;
	}
	
	public Object process() {
		LOG.debug("Received message\n" + data);
		return data;
	}

}