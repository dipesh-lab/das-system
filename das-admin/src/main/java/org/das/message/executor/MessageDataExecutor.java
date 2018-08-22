package org.das.message.executor;

import java.nio.channels.SelectionKey;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

public class MessageDataExecutor {
	
	private static final Logger LOG = Logger.getLogger(MessageDataExecutor.class);

	private final ExecutorService executorService;
	
	private final AtomicInteger index = new AtomicInteger(1);
	
	public MessageDataExecutor() {
		executorService = Executors.newFixedThreadPool(100);
	}
	
	public void analyseData(final String data) {
		InputDataProcessor dataProcessor = new InputDataProcessor(data);
		executorService.execute(dataProcessor);
		LOG.debug("JSON Message number " + index.getAndIncrement());
	}
	
	public void analyseData(final String data, final SelectionKey key) {
		MessageProcessor messageProcessor = new MessageProcessor(key, data);
		executorService.execute(messageProcessor);
		LOG.debug("Executor Counter " + index.getAndIncrement());
	}
	
}