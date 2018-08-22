package org.das.message.executor;

import org.apache.log4j.Logger;

public class InputDataProcessor implements Runnable {
	
	private static final Logger LOG = Logger.getLogger(InputDataProcessor.class);

	private final String data;
	
	public InputDataProcessor(final String message) {
		data = message;
	}
	
	@Override
	public void run() {
		LOG.debug("Processing message received\n" + data);
	}
}
