package org.das.client.processor;

import org.das.client.core.Publisher;
import org.das.client.exception.ConnectionDataLinkException;
import org.das.client.factory.PublisherProviderFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class SingleMessageProcessorTest {

	private Publisher messagePublisher = null;
	
	@Before
	public void setUp() {
		messagePublisher = PublisherProviderFactory.getInstance().getPublisher("127.0.0.1", 61000);
	}
	
	@After
	public void tearDown() {
		messagePublisher.close();
	}
	
	@Test
	public void testSingleMessage() throws ConnectionDataLinkException {
		messagePublisher.createSession();
		final String result = messagePublisher.pushMessage("Test Message");
		
		Assert.assertNotNull(result);
	}
	
	@Test(expected = ConnectionDataLinkException.class)
	public void testConnectionErrorWOSession() {
		messagePublisher.pushMessage("Test Message");
	}
	
}