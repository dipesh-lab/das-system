package org.das.network.server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

public class RequestWriter extends AbstractRequestProcessor implements RequestProcessor {

	private static final Logger LOG = Logger.getLogger(RequestWriter.class);
	
	private final Map<SelectionKey, Long> socketKeySet = new ConcurrentHashMap<SelectionKey, Long>(10);
		
	RequestWriter(Selector sel) {
		super(sel, 0);
	}
	
	@Override
	public SocketChannel process(SelectionKey key) {
		SocketChannel channel = null;
		try {
			channel = (SocketChannel) key.channel();
			Object object = key.attachment();			
			if(Objects.nonNull(object)) {
				LOG.debug("Data Writing Request. Object Found");
				final String data = object.toString();
				LOG.debug("Now Writing the data " + data);
				ByteBuffer buffer = ByteBuffer.wrap(data.getBytes());
				channel.write(buffer);
			} else if(!socketKeySet.containsKey(key)) {
				socketKeySet.put(key, System.currentTimeMillis());
			}
			LOG.debug("Total SelectionKey " + socketKeySet.size());
		} catch(IOException ioe) {
			LOG.error(ioe.getMessage(), ioe);
		} finally {
			//closeRequest(key, channel);
		}
		return null;
	}

}