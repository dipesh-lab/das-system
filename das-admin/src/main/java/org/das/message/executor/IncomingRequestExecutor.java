package org.das.message.executor;

import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class IncomingRequestExecutor {
	
	private final ExecutorService executorService;
	
	public IncomingRequestExecutor() {
		executorService = Executors.newFixedThreadPool(500);
	}
	
	public void process(final SelectionKey key, final SocketChannel channel) {
		executorService.execute(new RequestProcessorThread(channel));
	}
	
	public void shutdown() {
		executorService.shutdownNow();
	}

}