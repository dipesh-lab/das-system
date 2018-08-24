package org.das.network.server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;
import org.das.constant.AppConstant;
import org.das.exception.InvalidRequestException;

public class RequestReader extends AbstractRequestProcessor implements RequestProcessor {

	private static final Logger LOG = Logger.getLogger(RequestReader.class);
	
	private static volatile AtomicInteger ADMIN_REQ = new AtomicInteger(1);
	private static volatile AtomicInteger DAS_REQ = new AtomicInteger(1);
	
	private static final int bufferLimit = 256;
	
	public RequestReader(Selector sel) {
		super(sel, SelectionKey.OP_WRITE);
	}
	
	@Override
	public SocketChannel process(SelectionKey key) {
		SocketChannel channel = null;
		try {
			channel = (SocketChannel) key.channel();
			byte[] data = readMessage(channel);
			if(data.length == 1) throw new InvalidRequestException();
			
			final String message = new String(data, AppConstant.SOCKET_CHAR_SET);
			LOG.debug("Read Message. " + message);
			String resultData = null;
			if(message.startsWith("<")) {
				resultData = "!!ADMIN. Processing Complete!!-" + ADMIN_REQ.getAndIncrement();
			} else if(message.startsWith("[") || message.startsWith("{")) {
				resultData = "!!DAS Request. Processing Complete!!-" + DAS_REQ.getAndIncrement();
			}
			key.attach(resultData);
			LOG.debug("Request Result. " + resultData);
		} catch(IOException | InvalidRequestException e) {
			closeRequest(key, channel);
			if(e.getClass().equals(IOException.class))
				LOG.error(e.getMessage(), e);
		}
		return channel;
	}
	
	private byte[] readMessage(SocketChannel channel) throws IOException {
		ByteBuffer buffer = ByteBuffer.allocate(bufferLimit);
		int state;
		byte[] data = {1};
		int position = 0;		
		while( (state = channel.read(buffer)) > 0 ) {
			buffer.flip();
			byte[] d = Arrays.copyOf(buffer.array(), state);
			data = Arrays.copyOf(data, position + state);
			System.arraycopy(d, 0, data, position, state);
			position = position + state;
			buffer.clear();
		}
		return data;
	}

}