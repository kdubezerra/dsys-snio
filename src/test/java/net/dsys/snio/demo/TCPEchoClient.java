/**
 * Copyright 2014 Ricardo Padilha
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.dsys.snio.demo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import net.dsys.commons.impl.lang.ByteBufferCopier;
import net.dsys.snio.api.buffer.MessageBufferConsumer;
import net.dsys.snio.api.buffer.MessageBufferProducer;
import net.dsys.snio.api.channel.MessageChannel;
import net.dsys.snio.api.pool.SelectorPool;
import net.dsys.snio.impl.channel.MessageChannels;
import net.dsys.snio.impl.handler.MessageHandlers;
import net.dsys.snio.impl.pool.SelectorPools;

/**
 * @author Ricardo Padilha
 */
public final class TCPEchoClient {

	private TCPEchoClient() {
		return;
	}

	public static void main(final String[] args) throws IOException, InterruptedException, ExecutionException {
		final int threads = Integer.parseInt(getArg("threads", "1", args));
		final int buffers = Integer.parseInt(getArg("buffers", "256", args));
		final int length = Integer.parseInt(getArg("length", "1024", args));
		final String host = getArg("host", "localhost", args);
		final int port = Integer.parseInt(getArg("port", "12345", args));
		final long bandwidthThreshold = 800_000_000; // 800 Mbps

		final SelectorPool pool = SelectorPools.open("client", threads);
		final MessageChannel<ByteBuffer> client = MessageChannels.newTCPChannel()
				.setPool(pool)
				.setBufferCapacity(buffers)
				.setMessageLength(length)
				//.setMessageCodec(new DeflateCodec(length))
				//.setMessageCodec(new LZ4CompressionCodec(length))
				//.setMessageCodec(new MaxTputCodec(length, bandwidthThreshold))
				.useRingBuffer()
				.open();

		//client.setOption(StandardSocketOptions.TCP_NODELAY, Boolean.TRUE);
		client.connect(new InetSocketAddress(host, port));
		client.getConnectFuture().get();

		final MessageBufferConsumer<ByteBuffer> in = client.getInputBuffer();
		final MessageBufferProducer<ByteBuffer> out = client.getOutputBuffer();

		final ByteBufferCopier bbc = new ByteBufferCopier();
		final ExecutorService executor = Executors.newCachedThreadPool(); // unbounded!
		executor.execute(MessageHandlers.asyncConsumer(in, new EchoConsumer(), ByteBuffer.allocate(length), bbc, bbc));
		executor.execute(MessageHandlers.asyncProducer(out, new EchoProducer(), ByteBuffer.allocate(length), bbc, bbc));

		pool.getCloseFuture().get();
		executor.shutdown();
	}

	private static String getArg(final String name, final String defaultValue, final String[] args) {
		if (args == null || name == null) {
			return defaultValue;
		}
		final String key = "--" + name;
		for (int i = 0, k = args.length - 1; i < k; i++) {
			if (key.equals(args[i])) {
				return args[i + 1];
			}
		}
		return defaultValue;
	}

}
