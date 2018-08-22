package org.das.message.executor;

import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

public class MessageProcessor implements Runnable {
	
	private static final Logger LOG = Logger.getLogger(MessageProcessor.class);
	
	private static final AtomicInteger COUNTER = new AtomicInteger(0);
	
	private final SelectionKey key;
	
	private final String message;
	
	public MessageProcessor(final SelectionKey key, final String message) {
		this.key = key;
		this.message = message;
	}
	
	@Override
	public void run() {
		LOG.debug("Processed message NO. " + COUNTER.incrementAndGet() + "\n" + message);		
		if(message.startsWith("<")) {
			// Data for Administration. XML data
			key.attach(new String("!!ADMIN. Processing Complete!!-" + COUNTER.get()));			
		} else if(message.startsWith("\\[") || message.startsWith("\\{")) {
			// Data for Analysis. JSON Data
			key.attach(new String("!!Data Analysis Complete!!-" + COUNTER.get()));
		}
		try {
			SocketChannel channel = (SocketChannel) key.channel();
			channel.register(key.selector(), SelectionKey.OP_WRITE, key.attachment());
		} catch (ClosedChannelException e) {
			LOG.error(e.getMessage(), e);
		}
	}

}
