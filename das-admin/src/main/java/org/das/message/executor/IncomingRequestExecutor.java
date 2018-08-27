package org.das.message.executor;

import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class IncomingRequestExecutor {
	
	private final AtomicInteger counter = new AtomicInteger(1);
	
	private final ExecutorService executorService;
	
	public IncomingRequestExecutor() {
		executorService = Executors.newFixedThreadPool(500);
	}
	
	public void process(final SelectionKey key, final SocketChannel channel) {
		executorService.execute(new RequestProcessorThread(channel, counter));
	}
	
	public void shutdown() {
		executorService.shutdownNow();
	}

}