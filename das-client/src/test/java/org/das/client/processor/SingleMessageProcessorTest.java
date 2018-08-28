package org.das.client.processor;

import org.das.client.MessagePublisher;
import org.das.client.exception.ConnectionDataLinkException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class SingleMessageProcessorTest {

	private static final int port = 61000;
	private MessagePublisher messagePublisher = null;
	
	@Before
	public void setUp() {
		messagePublisher = new SingleMessageProcessorImpl();
	}
	
	@Test
	public void testSingleMessage() throws ConnectionDataLinkException {
		final String result = messagePublisher.pushMessage("Test Message", "127.0.0.1", port);
		
		Assert.assertNotNull(result);
	}
	
	@Test(expected=ConnectionDataLinkException.class)
	public void testWithInvalidHost() throws ConnectionDataLinkException {
		final String result = messagePublisher.pushMessage("Test Message", "123", port);
		
		Assert.assertNotNull(result);
	}
	
	@Test(expected=ConnectionDataLinkException.class)
	public void testWithInvalidPort() throws ConnectionDataLinkException {
		final String result = messagePublisher.pushMessage("Test Message", "123", 9000);
		
		Assert.assertNotNull(result);
	}
}