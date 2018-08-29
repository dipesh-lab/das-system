package org.das.client.processor;

import org.das.client.core.Publisher;
import org.das.client.exception.ConnectionDataLinkException;
import org.das.client.factory.PublisherProviderFactory;
import org.junit.Before;
import org.junit.Test;

public class MultipleMessageProcessorTest {

	private static final int port = 61000;
	private Publisher messagePublisher = null;
	
	@Before
	public void setUp() {
		messagePublisher = PublisherProviderFactory.getInstance().getStreamingPublisher("127.0.0.1", port);
	}
	
	@Before
	public void teatDown() {
		messagePublisher.close();
	}
	
	@Test
	public void testSingleMessagePush() throws InterruptedException {
		messagePublisher.createSession();
		messagePublisher.pushMessage("Client-1 Test Message 1");
		Thread.sleep(3000);
	}
	
	@Test
	public void testMultipleMessagePush() throws InterruptedException {
		messagePublisher.createSession();
		messagePublisher.pushMessage("Client-1 Test Message 2");
		messagePublisher.pushMessage("Client-1 Test Message 3");
		messagePublisher.pushMessage("Client-1 Test Message 4");
		Thread.sleep(5000);
	}
	
	@Test(expected=ConnectionDataLinkException.class)
	public void testConnectionErrorWOSession() {
		messagePublisher.pushMessage("Client-1 Test Message");
	}
}
