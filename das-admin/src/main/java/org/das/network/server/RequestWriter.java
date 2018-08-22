package org.das.network.server;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Objects;

import org.apache.log4j.Logger;

public class RequestWriter extends AbstractRequestProcessor implements RequestProcessor {

	private static final Logger LOG = Logger.getLogger(RequestWriter.class);
	
	RequestWriter(Selector sel) {
		super(sel, -1);
	}
	
	@Override
	public SocketChannel process(SelectionKey key) {
		SocketChannel channel = null;
		try {
			channel = (SocketChannel) key.channel();
			Object object = key.attachment();
			if(Objects.nonNull(object)) {
				final String data = object.toString();
				ByteBuffer buffer = ByteBuffer.wrap(data.getBytes());
				channel.write(buffer);
			}
		} catch(Exception e) {
			LOG.error(e.getMessage(), e);
		} finally {
			closeRequest(key, channel);
		}
		return null;
	}

}