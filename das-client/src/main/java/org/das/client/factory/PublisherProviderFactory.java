package org.das.client.factory;

import java.util.concurrent.atomic.AtomicInteger;

import org.das.client.core.Publisher;
import org.das.client.processor.MultipleMessagePublisherImpl;
import org.das.client.processor.SingleMessagePublisherImpl;

public class PublisherProviderFactory {

	private static final PublisherProviderFactory FACTORY = new PublisherProviderFactory();
	
	private static final AtomicInteger COUNTER = new AtomicInteger(1);
	
	private PublisherProviderFactory(){}
	
	public static PublisherProviderFactory getInstance() {
		return FACTORY;
	}
	
	public Publisher getPublisher(final String hostAddress, final int port) {
		Publisher publisher = new SingleMessagePublisherImpl(hostAddress, port);
		return publisher;
	}
	
	public Publisher getStreamingPublisher(final String hostAddress, final int port) {
		Publisher publisher = new MultipleMessagePublisherImpl(
				"Client-" + COUNTER.getAndIncrement(), hostAddress, port);
		return publisher;
	}

}