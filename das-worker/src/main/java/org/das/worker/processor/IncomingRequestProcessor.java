package org.das.worker.processor;

import java.util.concurrent.BlockingQueue;

import org.apache.log4j.Logger;
import org.das.worker.listener.RequestProcessor;

public class IncomingRequestProcessor extends Thread {
	
	private static final Logger LOG = Logger.getLogger(IncomingRequestProcessor.class);

	private RequestProcessor processor;
	private final BlockingQueue<String> messageQueue;
	
	public IncomingRequestProcessor(final BlockingQueue<String> messageQueue) {
		this.messageQueue = messageQueue;
	}
	
	@Override
	public void run() {
		try {
			while(true) {
				final String message = messageQueue.take();
				LOG.debug("Processor Message Received " + message);
				processor.onRequest(message);
			}
		} catch(InterruptedException e) {
			LOG.error(e.getMessage(), e);
		}
		LOG.debug("Request Receiver Thread Existed.");
	}
	
	public void stopListener() {
		super.interrupt();
	}
	
	public void setProcessor(final RequestProcessor processor) {
		this.processor = processor;
	}
	
}
