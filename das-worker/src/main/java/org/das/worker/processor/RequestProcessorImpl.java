package org.das.worker.processor;

import org.apache.log4j.Logger;
import org.das.worker.listener.RequestProcessor;

public class RequestProcessorImpl implements RequestProcessor {

	private static final Logger LOG = Logger.getLogger(RequestProcessorImpl.class);
	
	@Override
	public void onRequest(String data) {
		LOG.debug("New Analytic Request Received. Data\n" + data);
	}

}